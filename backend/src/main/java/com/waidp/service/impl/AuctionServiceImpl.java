package com.waidp.service.impl;

import com.waidp.dto.AuctionDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.Auction;
import com.waidp.entity.Bid;
import com.waidp.entity.Transaction;
import com.waidp.entity.User;
import com.waidp.mapper.AuctionMapper;
import com.waidp.repository.AssetRepository;
import com.waidp.repository.AuctionRepository;
import com.waidp.repository.BidRepository;
import com.waidp.repository.TransactionRepository;
import com.waidp.repository.UserRepository;
import com.waidp.service.AssetHistoryService;
import com.waidp.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 拍卖服务实现类
 * 实现拍卖活动的核心业务逻辑，包括：
 * 拍卖创建与管理、竞拍出价逻辑（支持加价幅度校验）、
 * 拍卖延时机制（结束前5分钟内出价自动延长5分钟）、
 * 撤价功能（结束前12小时内不可撤）、
 * 成交确认与超时处理、保留价校验、
 * 部门权限控制、定时任务：自动检查和结束拍卖、处理超时等。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    /**
     * 最小拍卖时长（秒），5分钟
     */
    private static final long MIN_AUCTION_SECONDS = 5 * 60;

    private final AuctionRepository auctionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final TransactionRepository transactionRepository;
    private final AssetHistoryService assetHistoryService;
    private final AuctionConfirmationExpiryService auctionConfirmationExpiryService;
    private final AuctionPaymentExpiryService auctionPaymentExpiryService;

    @Override
    public Page<Auction> getAuctions(String name, String status, Pageable pageable) {
        return auctionRepository.searchAuctions(name, status, pageable);
    }

    @Override
    public Page<AuctionDTO> getAuctionsDTO(String name, String status, Pageable pageable) {
        checkAndEndAuctions();
        Page<Auction> auctionPage = auctionRepository.searchAuctions(name, status, pageable);
        List<AuctionDTO> dtoList = auctionPage.getContent().stream()
                .map(AuctionMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, auctionPage.getTotalElements());
    }

    @Override
    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id).orElse(null);
    }

    @Override
    public AuctionDTO getAuctionDTOById(Long id) {
        checkAndEndAuctions();
        Auction auction = auctionRepository.findById(id).orElse(null);
        return auction != null ? AuctionMapper.toDTO(auction) : null;
    }

    @Override
    @Transactional
    public void createAuction(Auction auction) {
        // 创建拍卖活动
        // 业务规则：
        // 资产状态必须为"待拍卖"、拍卖时长不能少于5分钟、
        // 同一资产不能有多个未结束的拍卖、保留价不能低于起拍价
        // @param auction 拍卖活动信息
        // @throws IllegalArgumentException 参数验证失败
        // @throws RuntimeException 资产状态不合法或已存在未结束拍卖
        if (auction == null) {
            throw new IllegalArgumentException("拍卖信息不能为空");
        }
        if (auction.getAssetId() == null) {
            throw new IllegalArgumentException("请选择待拍卖资产");
        }
        if (auction.getName() == null || auction.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("拍卖名称不能为空");
        }
        if (auction.getStartTime() == null || auction.getEndTime() == null) {
            throw new IllegalArgumentException("拍卖开始时间和结束时间不能为空");
        }
        if (!auction.getEndTime().isAfter(auction.getStartTime())) {
            throw new IllegalArgumentException("拍卖结束时间必须晚于开始时间");
        }
        if (Duration.between(auction.getStartTime(), auction.getEndTime()).getSeconds() < MIN_AUCTION_SECONDS) {
            throw new IllegalArgumentException("拍卖时长不能少于5分钟");
        }
        requirePositiveAmount(auction.getIncrementAmount(), "加价幅度");

        Asset asset = assetRepository.findById(auction.getAssetId())
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        requirePositiveAmount(asset.getStartPrice(), "起拍价");

        if (!"待拍卖".equals(asset.getStatus())) {
            throw new RuntimeException("资产状态不允许创建拍卖，当前状态：" + asset.getStatus());
        }

        List<Auction> existingAuctions = auctionRepository.findByAssetId(asset.getId());
        for (Auction existing : existingAuctions) {
            if (!"ended".equals(existing.getStatus())) {
                throw new RuntimeException("该资产已存在未结束的拍卖");
            }
        }

        auction.setAsset(asset);
        auction.setStartPrice(asset.getStartPrice());
        auction.setCurrentPrice(asset.getStartPrice());

        if (Boolean.TRUE.equals(auction.getHasReservePrice())) {
            BigDecimal reserve = auction.getReservePrice();
            if (reserve == null) {
                reserve = asset.getReservePrice();
            }
            if (reserve == null) {
                reserve = asset.getCurrentValue();
            }
            requirePositiveAmount(reserve, "保留价");
            if (reserve.compareTo(asset.getStartPrice()) < 0) {
                throw new IllegalArgumentException("保留价不能低于起拍价");
            }
            auction.setReservePrice(reserve);
            auction.setHasReservePrice(true);
        } else {
            auction.setReservePrice(null);
            auction.setHasReservePrice(false);
        }

        auction.setStatus("not_started");
        auction.setBidCount(0);
        LocalDateTime now = LocalDateTime.now();
        auction.setCreateTime(now);
        auction.setUpdateTime(now);
        auctionRepository.save(auction);

        asset.setStatus("拍卖中");
        asset.setUpdateTime(now);
        assetRepository.save(asset);
    }

    @Override
    @Transactional
    public void deleteAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));

        if (!"not_started".equals(auction.getStatus())) {
            throw new RuntimeException("只能删除未开始的拍卖");
        }

        Asset asset = auction.getAsset();
        if (asset != null) {
            asset.setStatus("待拍卖");
            asset.setUpdateTime(LocalDateTime.now());
            assetRepository.save(asset);
        }
        auctionRepository.deleteById(auctionId);
    }

    @Override
    @Transactional
    public void bid(Long auctionId, Long userId, BigDecimal price) {
        // 用户参与竞拍出价
        // 业务规则：
        // 用户必须未被禁用且未被禁竞、拍卖必须处于"进行中"状态、
        // 用户所在部门必须被允许参与该拍卖、
        // 出价必须 ≥ 当前价 + 加价幅度、
        // 拍卖结束前5分钟内出价，自动延时5分钟
        // @param auctionId 拍卖活动ID
        // @param userId 出价用户ID
        // @param price 出价金额
        // @throws RuntimeException 各种业务规则校验失败
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new RuntimeException("账号已被禁用，无法参与竞拍");
        }

        LocalDateTime now = LocalDateTime.now();
        if (user.getBidBanUntil() != null && user.getBidBanUntil().isAfter(now)) {
            throw new RuntimeException("您已被限制竞拍，解禁时间：" + user.getBidBanUntil());
        }

        if (!"in_progress".equals(auction.getStatus())) {
            throw new RuntimeException("拍卖未进行中");
        }

        if (!isDepartmentAllowed(auction, user)) {
            throw new RuntimeException("当前拍卖不允许您所在部门参与");
        }

        requirePositiveAmount(price, "出价金额");
        requirePositiveAmount(auction.getIncrementAmount(), "加价幅度");
        requirePositiveAmount(auction.getCurrentPrice(), "当前价格");

        BigDecimal minBid = auction.getCurrentPrice().add(auction.getIncrementAmount());
        if (price.compareTo(minBid) < 0) {
            throw new RuntimeException("出价必须大于等于：" + minBid);
        }

        if (auction.getEndTime() != null && auction.getEndTime().minusMinutes(5).isBefore(now)) {
            auction.setEndTime(auction.getEndTime().plusMinutes(5));
        }

        auction.setCurrentPrice(price);
        auction.setBidCount((auction.getBidCount() == null ? 0 : auction.getBidCount()) + 1);
        auction.setWinner(user);
        auction.setFinalPrice(price);
        auction.setUpdateTime(now);
        auctionRepository.save(auction);

        bidRepository.clearHighestByAuctionId(auctionId);

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(user);
        bid.setPrice(price);
        bid.setIsHighest(true);
        bid.setBidTime(now);
        bid.setCreateTime(now);
        bidRepository.save(bid);
    }

    @Override
    @Transactional
    public void quickBid(Long auctionId, Long userId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));
        if (auction.getCurrentPrice() == null || auction.getIncrementAmount() == null) {
            throw new RuntimeException("拍卖价格配置异常");
        }
        bid(auctionId, userId, auction.getCurrentPrice().add(auction.getIncrementAmount()));
    }

    @Override
    @Transactional
    public void withdrawHighestBid(Long auctionId, Long userId) {
        // 撤回当前最高出价
        // 业务规则：
        // 拍卖必须处于"进行中"状态、拍卖结束前12小时内不可撤价、
        // 仅当前最高出价者可撤价、撤价后，次高出价自动成为最高出价
        // @param auctionId 拍卖活动ID
        // @param userId 出价用户ID
        // @throws RuntimeException 各种业务规则校验失败
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));

        if (!"in_progress".equals(auction.getStatus())) {
            throw new RuntimeException("拍卖未进行中");
        }
        if (auction.getEndTime() == null) {
            throw new RuntimeException("拍卖结束时间异常");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(auction.getEndTime().minusHours(12))) {
            throw new RuntimeException("拍卖结束前12小时内不可撤价");
        }

        Bid highest = bidRepository.findFirstByAuctionIdAndIsHighestTrue(auctionId);
        if (highest == null || highest.getId() == null) {
            throw new RuntimeException("当前没有可撤销的最高出价");
        }
        if (highest.getBidderId() == null || !highest.getBidderId().equals(userId)) {
            throw new RuntimeException("仅当前最高出价者可撤价");
        }

        bidRepository.deleteById(highest.getId());

        bidRepository.clearHighestByAuctionId(auctionId);
        Bid next = bidRepository.findFirstByAuctionIdOrderByPriceDescBidTimeDesc(auctionId);
        if (next != null) {
            next.setIsHighest(true);
            bidRepository.save(next);

            auction.setCurrentPrice(next.getPrice());
            auction.setWinner(next.getBidder());
            auction.setFinalPrice(next.getPrice());
        } else {
            auction.setCurrentPrice(auction.getStartPrice());
            auction.setWinner(null);
            auction.setFinalPrice(null);
        }

        auction.setBidCount((int) bidRepository.countByAuctionId(auctionId));
        auction.setUpdateTime(now);
        auctionRepository.save(auction);
    }

    @Override
    public void confirmTransaction(Long auctionId, Long userId) {
        // 中标者确认成交
        // 业务规则：
        // 仅拍卖中标者可以确认、必须在拍卖结束后24小时内确认、
        // 超时未确认视为放弃成交，并触发3个月竞拍禁用惩罚、
        // 确认后设置付款期限为确认后2天内
        // @param auctionId 拍卖活动ID
        // @param userId 中标用户ID
        // @throws RuntimeException 非中标者或已超时
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));

        if (!"ended".equals(auction.getStatus())) {
            throw new RuntimeException("拍卖未结束");
        }
        if (auction.getWinnerId() == null || !auction.getWinnerId().equals(userId)) {
            throw new RuntimeException("您不是中标者");
        }

        LocalDateTime now = LocalDateTime.now();
        Transaction tx = transactionRepository.findByAuctionId(auctionId).orElse(null);
        if (tx != null) {
            String status = tx.getConfirmStatus() == null ? "" : tx.getConfirmStatus().trim().toLowerCase(Locale.ROOT);
            if (!"pending".equals(status)) {
                throw new RuntimeException("已经确认过成交");
            }
            if (tx.getConfirmDeadline() != null && tx.getConfirmDeadline().isBefore(now)) {
                auctionConfirmationExpiryService.expireInNewTransaction(tx.getId(), "中标者超时未确认");
                throw new RuntimeException("已超过确认时限，视为放弃成交");
            }
            tx.setConfirmStatus("confirmed");
            tx.setConfirmTime(now);
            tx.setPaymentDeadline(now.plusDays(2));
            tx.setUpdateTime(now);
            transactionRepository.save(tx);
        } else {
            tx = new Transaction();
            tx.setCode(generateTransactionCode());
            tx.setAuction(auction);
            tx.setAsset(auction.getAsset());
            tx.setWinner(auction.getWinner());
            tx.setFinalPrice(auction.getFinalPrice());
            tx.setConfirmStatus("confirmed");
            tx.setConfirmTime(now);
            tx.setConfirmDeadline(now);
            tx.setPaymentStatus("pending");
            tx.setPaymentDeadline(now.plusDays(2));
            tx.setDisposalStatus("pending");
            tx.setCreateTime(now);
            tx.setUpdateTime(now);
            transactionRepository.save(tx);
        }

        Asset asset = auction.getAsset();
        if (asset != null) {
            asset.setStatus("待处置");
            asset.setUpdateTime(now);
            assetRepository.save(asset);
        }
    }

    @Override
    public List<Auction> getMyAuctions(Long userId) {
        checkAndEndAuctions();
        return auctionRepository.findMyAuctions(userId);
    }

    @Override
    public List<AuctionDTO> getMyAuctionsDTO(Long userId, String status, String confirmStatus) {
        checkAndEndAuctions();
        List<Auction> auctions = auctionRepository.findMyAuctions(userId);

        List<AuctionDTO> dtos = auctions.stream().map(AuctionMapper::toDTO).collect(Collectors.toList());
        for (int i = 0; i < auctions.size(); i++) {
            Auction auction = auctions.get(i);
            AuctionDTO dto = dtos.get(i);

            BigDecimal myMaxBid = bidRepository.findMaxBidByUserAndAuction(auction.getId(), userId);
            Bid latestBid = bidRepository.findFirstByAuctionIdAndBidderIdOrderByBidTimeDesc(auction.getId(), userId);
            BigDecimal myBid = latestBid != null ? latestBid.getPrice() : null;
            BigDecimal highest = bidRepository.findMaxBidByAuction(auction.getId());

            dto.setMyMaxBid(myMaxBid);
            dto.setMyBid(myBid);
            dto.setIsHighest(myMaxBid != null && highest != null && myMaxBid.compareTo(highest) == 0);

            if ("ended".equals(auction.getStatus())) {
                if (auction.getWinnerId() != null && auction.getWinnerId().equals(userId)) {
                    dto.setResult("won");
                    Transaction tx = transactionRepository.findByAuctionId(auction.getId()).orElse(null);
                    if (tx != null) {
                        String cs = tx.getConfirmStatus() == null ? null : tx.getConfirmStatus().trim().toLowerCase(Locale.ROOT);
                        dto.setConfirmStatus(cs);
                        dto.setWinTime(tx.getCreateTime() != null ? tx.getCreateTime() : auction.getEndTime());
                    } else {
                        dto.setConfirmStatus("pending");
                        dto.setWinTime(auction.getEndTime());
                    }
                } else if (auction.getWinnerId() == null) {
                    dto.setResult("failed");
                } else {
                    dto.setResult("lost");
                }
            } else {
                dto.setResult("in_progress");
            }
        }

        String statusFilter = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        if (!statusFilter.isBlank() && !"all".equals(statusFilter)) {
            if ("won".equals(statusFilter) || "lost".equals(statusFilter)) {
                if ("lost".equals(statusFilter)) {
                    dtos = dtos.stream()
                            .filter(dto -> "lost".equalsIgnoreCase(dto.getResult()) || "failed".equalsIgnoreCase(dto.getResult()))
                            .collect(Collectors.toList());
                } else {
                    dtos = dtos.stream()
                            .filter(dto -> statusFilter.equalsIgnoreCase(dto.getResult()))
                            .collect(Collectors.toList());
                }
            } else {
                dtos = dtos.stream()
                        .filter(dto -> statusFilter.equalsIgnoreCase(dto.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        String confirmStatusFilter = confirmStatus == null ? "" : confirmStatus.trim().toLowerCase(Locale.ROOT);
        if (!confirmStatusFilter.isBlank()) {
            dtos = dtos.stream()
                    .filter(dto -> confirmStatusFilter.equalsIgnoreCase(
                            dto.getConfirmStatus() == null ? "" : dto.getConfirmStatus().trim()))
                    .collect(Collectors.toList());
        }

        return dtos;
    }

    @Override
    public List<Bid> getBidsByAuctionId(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId);
    }

    @Override
    @Transactional
    public void checkAndEndAuctions() {
        // 自动检查并结束拍卖（定时任务调用）
        // 业务规则：
        // 检查"未开始"的拍卖，到达开始时间则自动改为"进行中"、
        // 检查"进行中"的拍卖，到达结束时间则自动结算、
        // 结算规则：
        // 无人出价 → 流拍，资产状态改回"待拍卖"、
        // 有出价但未达保留价 → 流拍，资产状态改回"待拍卖"、
        // 达到保留价 → 成交，资产状态改为"待处置"，生成交易单
        LocalDateTime now = LocalDateTime.now();

        List<Auction> notStarted = auctionRepository.findByStatus("not_started");
        for (Auction auction : notStarted) {
            if (auction.getStartTime() != null && !auction.getStartTime().isAfter(now)) {
                auction.setStatus("in_progress");
                auction.setUpdateTime(now);
                auctionRepository.save(auction);
            }
        }

        List<Auction> inProgress = auctionRepository.findByStatus("in_progress");
        for (Auction auction : inProgress) {
            if (auction.getEndTime() == null || auction.getEndTime().isAfter(now)) {
                continue;
            }

            Bid highest = bidRepository.findFirstByAuctionIdOrderByPriceDescBidTimeDesc(auction.getId());
            boolean hasWinner = highest != null && highest.getBidderId() != null && highest.getPrice() != null;

            if (hasWinner && Boolean.TRUE.equals(auction.getHasReservePrice()) && auction.getReservePrice() != null) {
                if (highest.getPrice().compareTo(auction.getReservePrice()) < 0) {
                    hasWinner = false;
                }
            }

            auction.setStatus("ended");
            auction.setUpdateTime(now);
            if (hasWinner) {
                auction.setWinner(highest.getBidder());
                auction.setFinalPrice(highest.getPrice());
                auction.setCurrentPrice(highest.getPrice());
            } else {
                auction.setWinner(null);
                auction.setFinalPrice(null);
                auction.setCurrentPrice(auction.getStartPrice());
            }
            auctionRepository.save(auction);

            Asset asset = auction.getAsset();
            if (asset != null) {
                if (hasWinner) {
                    asset.setStatus("待处置");
                    assetHistoryService.addHistory(asset.getId(), "拍卖结果",
                            "拍卖结束：成交，中标者：" + auction.getWinnerId() + "，成交价：" + auction.getFinalPrice(), null);
                } else {
                    asset.setStatus("待拍卖");
                    assetHistoryService.addHistory(asset.getId(), "拍卖结果",
                            "拍卖结束：流拍，原因：" + (highest == null ? "无人出价" : "未达保留价"), null);
                }
                asset.setUpdateTime(now);
                assetRepository.save(asset);
            }

            if (hasWinner) {
                ensureTransactionForEndedAuction(auction, now);
            }
        }
    }

    @Override
    @Transactional
    public void checkExpiredConfirmations() {
        // 检查并处理超时未确认的成交（定时任务调用）
        // 业务规则：
        // 检查"待确认"的交易单，如果超过确认截止时间则自动过期、
        // 过期后的处理：
        // 确认状态改为"expired"、付款状态改为"expired"、
        // 处置状态改为"cancelled"、中标者被禁用竞拍3个月、
        // 资产状态改回"待拍卖"
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> expired = transactionRepository.findByConfirmStatusInAndConfirmDeadlineBefore(
                List.of("pending", "PENDING"), now);
        if (expired == null || expired.isEmpty()) {
            reconcileExpiredConfirmationPaymentStatus(now);
            return;
        }
        for (Transaction tx : expired) {
            auctionConfirmationExpiryService.expireInNewTransaction(tx.getId(), "中标者超时未确认");
        }
        reconcileExpiredConfirmationPaymentStatus(now);
    }

    private void reconcileExpiredConfirmationPaymentStatus(LocalDateTime now) {
        List<Transaction> inconsistent = transactionRepository.findByConfirmStatusInAndPaymentStatusIn(
                List.of("expired", "EXPIRED"),
                List.of("pending", "PENDING")
        );
        if (inconsistent == null || inconsistent.isEmpty()) {
            return;
        }
        for (Transaction tx : inconsistent) {
            tx.setPaymentStatus("expired");
            if (tx.getDisposalStatus() != null) {
                String disposalStatus = tx.getDisposalStatus().trim().toLowerCase(Locale.ROOT);
                if ("pending".equals(disposalStatus)) {
                    tx.setDisposalStatus("cancelled");
                }
            }
            tx.setUpdateTime(now);
        }
        transactionRepository.saveAll(inconsistent);
    }

    @Override
    @Transactional
    public void checkExpiredPayments() {
        // 检查并处理超时未付款的订单（定时任务调用）
        // 业务规则：
        // 检查"已确认"但"待付款"的交易单，如果超过付款截止时间则自动过期、
        // 过期后的处理：
        // 付款状态改为"expired"、中标者被禁用竞拍3个月、
        // 资产状态改回"待拍卖"
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> expired = transactionRepository.findByConfirmStatusInAndPaymentStatusInAndPaymentDeadlineBefore(
                List.of("confirmed", "CONFIRMED"),
                List.of("pending", "PENDING"),
                now
        );
        if (expired == null || expired.isEmpty()) {
            return;
        }
        for (Transaction tx : expired) {
            auctionPaymentExpiryService.expireInNewTransaction(tx.getId(), "中标者付款超时未完成");
        }
    }

    private void ensureTransactionForEndedAuction(Auction auction, LocalDateTime now) {
        if (auction == null || auction.getId() == null) {
            return;
        }
        if (transactionRepository.findByAuctionId(auction.getId()).isPresent()) {
            return;
        }

        Transaction tx = new Transaction();
        tx.setCode(generateTransactionCode());
        tx.setAuction(auction);
        tx.setAsset(auction.getAsset());
        tx.setWinner(auction.getWinner());
        tx.setFinalPrice(auction.getFinalPrice());
        tx.setConfirmStatus("pending");
        tx.setConfirmDeadline(now.plusDays(1));
        tx.setPaymentStatus("pending");
        tx.setPaymentDeadline(now.plusDays(2));
        tx.setDisposalStatus("pending");
        tx.setCreateTime(now);
        tx.setUpdateTime(now);
        transactionRepository.save(tx);
    }

    private String generateTransactionCode() {
        return "TXN" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean isDepartmentAllowed(Auction auction, User user) {
        if (auction == null) {
            return false;
        }
        String raw = auction.getDepartmentIds();
        if (raw == null || raw.isBlank() || "all".equalsIgnoreCase(raw.trim())) {
            return true;
        }
        Long userDeptId = user != null ? user.getDepartmentId() : null;
        if (userDeptId == null) {
            return false;
        }
        Set<Long> allowed = parseDepartmentIds(raw);
        return allowed.isEmpty() || allowed.contains(userDeptId);
    }

    private Set<Long> parseDepartmentIds(String raw) {
        Set<Long> ids = new HashSet<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        String[] parts = raw.split(",");
        for (String p : parts) {
            String s = p == null ? "" : p.trim();
            if (s.isEmpty() || "all".equalsIgnoreCase(s)) {
                continue;
            }
            try {
                ids.add(Long.parseLong(s));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private static BigDecimal requirePositiveAmount(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + "必须大于0");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(fieldName + "最多保留2位小数");
        }
        return value;
    }
}
