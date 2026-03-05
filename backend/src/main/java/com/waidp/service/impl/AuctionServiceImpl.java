package com.waidp.service.impl;

import com.waidp.dto.AuctionDTO;
import com.waidp.entity.*;
import com.waidp.mapper.AuctionMapper;
import com.waidp.repository.*;
import com.waidp.service.AssetHistoryService;
import com.waidp.service.AuctionService;
import com.waidp.util.AmountValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 拍卖服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final TransactionRepository transactionRepository;
    private final AssetHistoryService assetHistoryService;
    private final AuctionConfirmationExpiryService auctionConfirmationExpiryService;

    @Override
    public Page<Auction> getAuctions(String name, String status, Pageable pageable) {
        return auctionRepository.searchAuctions(name, status, pageable);
    }

    @Override
    public Page<AuctionDTO> getAuctionsDTO(String name, String status, Pageable pageable) {
        checkAndEndAuctions();
        Page<Auction> auctionPage = auctionRepository.searchAuctions(name, status, pageable);
        
        // 转换为DTO列表
        List<AuctionDTO> auctionDTOs = auctionPage.getContent().stream()
                .map(AuctionMapper::toDTO)
                .collect(Collectors.toList());
        
        // 创建新的Page对象
        return new PageImpl<>(auctionDTOs, pageable, auctionPage.getTotalElements());
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
        AmountValidationUtils.requirePositiveAmount(auction.getIncrementAmount(), "加价幅度");

        Asset asset = assetRepository.findById(auction.getAssetId())
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        AmountValidationUtils.requirePositiveAmount(asset.getStartPrice(), "起拍价");

        // 检查资产状态是否允许创建拍卖（"待拍卖"状态）
        if (!"待拍卖".equals(asset.getStatus())) {
            throw new RuntimeException("资产状态不允许创建拍卖，当前状态：" + asset.getStatus());
        }

        // 检查是否已经存在该资产的拍卖
        List<Auction> existingAuctions = auctionRepository.findByAssetId(asset.getId());
        for (Auction existingAuction : existingAuctions) {
            if (!"ended".equals(existingAuction.getStatus())) {
                throw new RuntimeException("该资产已存在未结束的拍卖");
            }
        }

        auction.setAsset(asset);
        auction.setStartPrice(asset.getStartPrice());
        auction.setCurrentPrice(asset.getStartPrice());
        if (Boolean.TRUE.equals(auction.getHasReservePrice())) {
            BigDecimal reservePrice = auction.getReservePrice() != null ? auction.getReservePrice() : asset.getReservePrice();
            if (reservePrice == null) {
                reservePrice = asset.getCurrentValue();
            }
            AmountValidationUtils.requirePositiveAmount(reservePrice, "保留价");
            if (asset.getStartPrice() != null && reservePrice.compareTo(asset.getStartPrice()) < 0) {
                throw new IllegalArgumentException("保留价不能低于起拍价");
            }
            auction.setReservePrice(reservePrice);
        } else {
            auction.setReservePrice(null);
            auction.setHasReservePrice(false);
        }
        // 统一状态值：not_started / in_progress / ended
        auction.setStatus("not_started");
        auction.setBidCount(0);
        auction.setCreateTime(LocalDateTime.now());
        auction.setUpdateTime(LocalDateTime.now());

        auctionRepository.save(auction);

        // 更新资产状态
        asset.setStatus("拍卖中");
        assetRepository.save(asset);
    }

    @Override
    @Transactional
    public void bid(Long auctionId, Long userId, BigDecimal price) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查用户状态
        if (!user.getStatus()) {
            throw new RuntimeException("账号已被禁用，无法参与竞拍");
        }

        // 检查竞拍资格（违约限制）
        LocalDateTime now = LocalDateTime.now();
        if (user.getBidBanUntil() != null && user.getBidBanUntil().isAfter(now)) {
            throw new RuntimeException("您已被限制竞拍，解禁时间：" + user.getBidBanUntil());
        }

        // 检查拍卖状态
        if (!"in_progress".equals(auction.getStatus())) {
            throw new RuntimeException("拍卖未进行中");
        }

        AmountValidationUtils.requirePositiveAmount(price, "出价金额");
        if (auction.getCurrentPrice() == null || auction.getIncrementAmount() == null) {
            throw new RuntimeException("拍卖价格配置异常，请联系管理员");
        }

        // 检查参与部门范围
        if (!isDepartmentAllowed(auction, user)) {
            throw new RuntimeException("当前拍卖不允许您所在部门参与");
        }



        // 检查出价是否满足加价幅度
        BigDecimal minBid = auction.getCurrentPrice().add(auction.getIncrementAmount());
        if (price.compareTo(minBid) < 0) {
            throw new RuntimeException("出价必须大于当前价 + 加价幅度");
        }


        // 检查是否在最后5分钟，如果是则延长5分钟
        if (auction.getEndTime().minusMinutes(5).isBefore(LocalDateTime.now())) {
            auction.setEndTime(auction.getEndTime().plusMinutes(5));
        }

        // 更新拍卖信息
        auction.setCurrentPrice(price);
        auction.setBidCount(auction.getBidCount() + 1);
        // Auction 实体中 winnerId 字段是 insertable=false，因此必须设置关联对象才能写入 winner_id
        auction.setWinner(user);
        auction.setFinalPrice(price);
        auction.setUpdateTime(LocalDateTime.now());
        auctionRepository.save(auction);

        // 清除旧的最高出价标记，保证只有一个“当前最高”
        bidRepository.clearHighestByAuctionId(auctionId);

        // 创建竞价记录
        LocalDateTime bidTime = LocalDateTime.now();
        Bid bid = new Bid();
        // Bid 实体中 auctionId / bidderId 字段是 insertable=false，因此必须设置关联对象，JPA 才能写入 auction_id / bidder_id
        bid.setAuction(auction);
        bid.setBidder(user);
        bid.setPrice(price);
        bid.setIsHighest(true);
        bid.setBidTime(bidTime);
        bid.setCreateTime(bidTime);
        bidRepository.save(bid);
    }

    @Override
    @Transactional
    public void deleteAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));

        // 只能删除未开始的拍卖
        if (!"not_started".equals(auction.getStatus())) {
            throw new RuntimeException("只能删除未开始的拍卖");
        }

        // 恢复资产状态
        Asset asset = auction.getAsset();
        asset.setStatus("待拍卖");
        assetRepository.save(asset);

        auctionRepository.deleteById(auctionId);
    }

    @Override
    @Transactional
    public void quickBid(Long auctionId, Long userId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));

        // 按加价幅度自动出价
        BigDecimal quickBidPrice = auction.getCurrentPrice().add(auction.getIncrementAmount());
        bid(auctionId, userId, quickBidPrice);
    }

    @Override
    public List<Bid> getBidsByAuctionId(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId);
    }

    @Override
    @Transactional
    public void confirmTransaction(Long auctionId, Long userId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("拍卖不存在"));

        if (!userId.equals(auction.getWinnerId())) {
            throw new RuntimeException("您不是中标者");
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查是否已确认/是否过期
        Transaction existing = transactionRepository.findByAuctionId(auctionId).orElse(null);
        if (existing != null) {
            String status = existing.getConfirmStatus() == null ? "" : existing.getConfirmStatus().trim().toLowerCase();
            if (!"pending".equals(status)) {
                throw new RuntimeException("已经确认过成交");
            }
            if (existing.getConfirmDeadline() != null && existing.getConfirmDeadline().isBefore(now)) {
                auctionConfirmationExpiryService.expireInNewTransaction(existing.getId(), "中标者超时未确认");
                throw new RuntimeException("已超过确认时限，视为放弃成交");
            }
        }

        // 如果交易单不存在则创建
        if (existing == null) {
            existing = new Transaction();
            existing.setCode(generateTransactionCode());
            existing.setAuction(auction);
            existing.setAsset(auction.getAsset());
            existing.setWinner(auction.getWinner() != null ? auction.getWinner() : userRepository.findById(userId).orElse(null));
            existing.setFinalPrice(auction.getFinalPrice());
            existing.setConfirmStatus("confirmed");
            existing.setConfirmTime(now);
            existing.setConfirmDeadline(now);
            existing.setPaymentStatus("pending");
            existing.setPaymentDeadline(now.plusDays(2));
            existing.setCreateTime(now);
            existing.setUpdateTime(now);
            transactionRepository.save(existing);
        } else {
            if (existing.getAuction() == null) {
                existing.setAuction(auction);
            }
            if (existing.getAsset() == null) {
                existing.setAsset(auction.getAsset());
            }
            if (existing.getWinner() == null) {
                existing.setWinner(auction.getWinner() != null ? auction.getWinner() : userRepository.findById(userId).orElse(null));
            }
            existing.setConfirmStatus("confirmed");
            existing.setConfirmTime(now);
            existing.setPaymentDeadline(now.plusDays(2));
            existing.setUpdateTime(now);
            transactionRepository.save(existing);
        }

        // 更新资产状态为"待处置"
        Asset asset = auction.getAsset();
        asset.setStatus("待处置");
        assetRepository.save(asset);
    }

    @Override
    public List<Auction> getMyAuctions(Long userId) {
        checkAndEndAuctions();
        return auctionRepository.findMyAuctions(userId);
    }

    @Override
    public List<AuctionDTO> getMyAuctionsDTO(Long userId, String status) {
        checkAndEndAuctions();
        List<Auction> auctions = auctionRepository.findMyAuctions(userId);
        List<AuctionDTO> dtos = auctions.stream()
                .map(AuctionMapper::toDTO)
                .collect(Collectors.toList());

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
                boolean success = auction.getWinnerId() != null
                        && (!Boolean.TRUE.equals(auction.getHasReservePrice())
                        || (auction.getFinalPrice() != null && auction.getReservePrice() != null
                        && auction.getFinalPrice().compareTo(auction.getReservePrice()) >= 0));

                if (success && userId.equals(auction.getWinnerId())) {
                    dto.setResult("won");
                    dto.setWinTime(auction.getEndTime());
                    Transaction tx = transactionRepository.findByAuctionId(auction.getId()).orElse(null);
                    dto.setConfirmStatus(tx != null ? tx.getConfirmStatus() : "pending");
                } else {
                    dto.setResult("lost");
                }
            } else {
                dto.setResult("in_progress");
            }
        }

        return filterMyAuctionsByStatus(dtos, status);
    }

    @Override
    @Transactional
    public void checkAndEndAuctions() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 检查应该开始的拍卖
        List<Auction> notStartedAuctions = auctionRepository.findByStatus("not_started");
        for (Auction auction : notStartedAuctions) {
            if (auction.getStartTime().isBefore(now) || auction.getStartTime().isEqual(now)) {
                // 拍卖应该开始
                auction.setStatus("in_progress");
                auction.setUpdateTime(now);
                auctionRepository.save(auction);
                log.info("拍卖 {} 已开始", auction.getName());
            }
        }
        
        // 2. 检查应该结束的拍卖
        List<Auction> inProgressAuctions = auctionRepository.findByStatus("in_progress");

        for (Auction auction : inProgressAuctions) {
            if (auction.getEndTime().isBefore(now)) {
                // 拍卖结束，检查是否有中标者
                if (auction.getWinnerId() != null) {
                    // 有中标者，检查是否达到保留价
                    if (auction.getHasReservePrice() &&
                        auction.getFinalPrice().compareTo(auction.getReservePrice()) < 0) {
                        // 未达到保留价，流拍
                        endAuction(auction, "ended");
                    } else {
                        // 达到保留价或未设置保留价，成交
                        endAuction(auction, "ended");
                        
                        // 创建交易记录
                        createTransaction(auction);
                    }
                } else {
                    // 无中标者，流拍
                    endAuction(auction, "ended");
                }
            }
        }
    }

    @Override
    @Transactional
    public void checkExpiredConfirmations() {
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> expired = transactionRepository.findByConfirmStatusInAndConfirmDeadlineBefore(
                List.of("pending", "PENDING"), now);
        if (expired == null || expired.isEmpty()) {
            return;
        }
        for (Transaction tx : expired) {
            auctionConfirmationExpiryService.expireInNewTransaction(tx.getId(), "中标者超时未确认");
        }
    }

    private void recordAuctionHistory(Auction auction, Asset asset, String result, String extra) {
        String content = "拍卖结束：" + result + "，拍卖名称：" + auction.getName();
        if (extra != null && !extra.isBlank()) {
            content += "，" + extra;
        }
        assetHistoryService.addHistory(asset.getId(), "拍卖结果", content, null);
    }

    /**
     * 结束拍卖
     */
    private void endAuction(Auction auction, String status) {
        auction.setStatus(status);
        auction.setUpdateTime(LocalDateTime.now());
        auctionRepository.save(auction);

        // 更新资产状态
        Asset asset = auction.getAsset();
        if ("ended".equals(status)) {
            if (auction.getWinnerId() != null && 
                (!auction.getHasReservePrice() || 
                 auction.getFinalPrice().compareTo(auction.getReservePrice()) >= 0)) {
                // 有中标者且达到保留价或未设置保留价
                asset.setStatus("待处置");
                String winnerInfo = auction.getWinner() != null ? auction.getWinner().getName() : String.valueOf(auction.getWinnerId());
                recordAuctionHistory(auction, asset, "成交", "中标者：" + winnerInfo + "，成交价：" + auction.getFinalPrice());
            } else {
                // 无中标者或未达到保留价
                asset.setStatus("待拍卖");
                String reason = (auction.getWinnerId() == null) ? "无人出价" : "未达保留价";
                recordAuctionHistory(auction, asset, "流拍", reason);
            }
        }
        assetRepository.save(asset);
    }
    
    /**
     * 创建交易记录
     */
    private void createTransaction(Auction auction) {
        Transaction transaction = new Transaction();
        transaction.setCode(generateTransactionCode());
        transaction.setAuction(auction);
        transaction.setAsset(auction.getAsset());
        transaction.setWinner(auction.getWinner());
        LocalDateTime now = LocalDateTime.now();
        transaction.setFinalPrice(auction.getFinalPrice());
        transaction.setConfirmStatus("pending");
        transaction.setConfirmDeadline(now.plusDays(1));
        transaction.setPaymentStatus("pending");
        // 先给一个默认值，确认成交时会重置为确认时间+48小时
        transaction.setPaymentDeadline(now.plusDays(2));
        transaction.setCreateTime(now);
        transaction.setUpdateTime(now);
        
        transactionRepository.save(transaction);
        log.info("为拍卖 {} 创建交易记录成功", auction.getName());
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
        if ("all".equalsIgnoreCase(raw.trim())) {
            return ids;
        }
        Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(s -> {
                    try {
                        ids.add(Long.parseLong(s));
                    } catch (NumberFormatException ignored) {
                    }
                });
        return ids;
    }

    private List<AuctionDTO> filterMyAuctionsByStatus(List<AuctionDTO> list, String status) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return list;
        }
        return list.stream().filter(item -> {
            if ("in_progress".equalsIgnoreCase(status)) {
                return "in_progress".equals(item.getStatus());
            }
            if ("ended".equalsIgnoreCase(status)) {
                return "ended".equalsIgnoreCase(item.getStatus());
            }
            if ("won".equalsIgnoreCase(status)) {
                return "won".equalsIgnoreCase(item.getResult());
            }
            if ("lost".equalsIgnoreCase(status)) {
                return "lost".equalsIgnoreCase(item.getResult());
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 生成交易单编号
     */
    private String generateTransactionCode() {
        return "TXN" + System.currentTimeMillis();
    }
}
