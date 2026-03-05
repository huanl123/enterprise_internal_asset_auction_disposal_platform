package com.waidp.service.impl;

import com.waidp.dto.AssetDTO;
import com.waidp.dto.FinanceStatistics;
import com.waidp.dto.TransactionDTO;
import com.waidp.entity.*;
import com.waidp.mapper.AssetMapper;
import com.waidp.mapper.TransactionMapper;
import com.waidp.repository.*;
import com.waidp.service.FinanceService;
import com.waidp.util.AmountValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 财务服务实现
 */
@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final AssetHistoryRepository assetHistoryRepository;

    @Override
    public Page<Asset> getPendingAssets(Pageable pageable) {
        try {
            System.out.println("FinanceServiceImpl.getPendingAssets called with pageable: " + pageable);
            // 查询状态为"待审核"或"PENDING"的资产，以兼容数据库中的不同状态值
            Page<Asset> result = assetRepository.findByStatusIn(List.of("待审核", "PENDING"), pageable);
            System.out.println("Found " + result.getTotalElements() + " assets with status '待审核' or 'PENDING'");
            return result;
        } catch (Exception e) {
            System.err.println("Error in getPendingAssets: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 获取待审核资产列表（返回DTO）
     */
    public Page<AssetDTO> getPendingAssetsDTO(Pageable pageable) {
        try {
            System.out.println("FinanceServiceImpl.getPendingAssetsDTO called with pageable: " + pageable);
            // 查询状态为"待审核"或"PENDING"的资产，以兼容数据库中的不同状态值
            Page<Asset> assetPage = assetRepository.findByStatusIn(List.of("待审核", "PENDING"), pageable);
            System.out.println("Found " + assetPage.getTotalElements() + " assets with status '待审核' or 'PENDING'");
            
            // 转换为DTO避免懒加载问题
            Page<AssetDTO> dtoPage = assetPage.map(AssetMapper::toDTO);
            return dtoPage;
        } catch (Exception e) {
            System.err.println("Error in getPendingAssetsDTO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public void approveAsset(Long assetId, BigDecimal startPrice, boolean hasReservePrice, BigDecimal reservePrice, Long operatorId, String remark) {
        System.out.println("开始审核资产，资产ID: " + assetId);
        
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> {
                    System.err.println("资产不存在，资产ID: " + assetId);
                    return new RuntimeException("资产不存在，资产ID: " + assetId);
                });

        System.out.println("找到资产，ID: " + asset.getId() + ", 名称: " + asset.getName() + ", 状态: " + asset.getStatus());
        
        String assetStatus = asset.getStatus();
        if (assetStatus == null) {
            System.err.println("资产状态为空，资产ID: " + assetId);
            throw new RuntimeException("资产状态不允许审核，当前状态: null");
        }
        
        // 使用trim()去除可能的空格
        String trimmedStatus = assetStatus.trim();
        System.out.println("资产状态（原始）: '" + assetStatus + "', 资产状态（去除空格）: '" + trimmedStatus + "'");
        
        if (!"待审核".equals(trimmedStatus) && !"PENDING".equals(trimmedStatus)) {
            System.err.println("资产状态不允许审核，资产ID: " + assetId + ", 状态: '" + assetStatus + "'");
            throw new RuntimeException("资产状态不允许审核，当前状态: " + assetStatus);
        }

        AmountValidationUtils.requirePositiveAmount(startPrice, "起拍价");
        BigDecimal finalReservePrice = reservePrice;
        if (hasReservePrice) {
            finalReservePrice = finalReservePrice != null ? finalReservePrice : asset.getCurrentValue();
            AmountValidationUtils.requirePositiveAmount(finalReservePrice, "保留价");
            if (finalReservePrice.compareTo(startPrice) < 0) {
                throw new IllegalArgumentException("保留价不能低于起拍价");
            }
        }

        // 更新起拍价和保留价
        asset.setStartPrice(startPrice);
        asset.setHasReservePrice(hasReservePrice);
        if (hasReservePrice && finalReservePrice != null) {
            asset.setReservePrice(finalReservePrice);
        } else {
            asset.setReservePrice(null);
        }

        asset.setStatus("待拍卖");
        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.save(asset);

        // 记录历史
        String content = "审核通过，起拍价：" + startPrice +
                (hasReservePrice ? "，保留价：" + finalReservePrice : "，未设置保留价");
        if (remark != null && !remark.trim().isEmpty()) {
            content += "，备注：" + remark.trim();
        }
        recordHistory(assetId, operatorId, "财务审核", content);
    }

    @Override
    public List<java.util.Map<String, Object>> getAssetReviewHistory() {
        // 这里以 AssetHistory 中 operation = "财务审核" 为准
        List<AssetHistory> histories = assetHistoryRepository.findByOperationOrderByCreateTimeDesc("财务审核");
        if (histories == null || histories.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 批量取资产信息
        List<Long> assetIds = histories.stream().map(AssetHistory::getAssetId).distinct().toList();
        java.util.Map<Long, Asset> assetMap = assetRepository.findAllById(assetIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(Asset::getId, a -> a));

        // 批量取审核人信息
        List<Long> operatorIds = histories.stream()
                .map(AssetHistory::getOperatorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        java.util.Map<Long, User> userMap = operatorIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(operatorIds)
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));

        return histories.stream().map(h -> {
            Asset asset = assetMap.get(h.getAssetId());
            User reviewer = h.getOperatorId() != null ? userMap.get(h.getOperatorId()) : null;

            String result = (h.getContent() != null && h.getContent().contains("审核通过")) ? "通过" : "-";

            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("assetId", h.getAssetId());
            item.put("assetCode", asset != null ? asset.getCode() : "-");
            item.put("assetName", asset != null ? asset.getName() : "-");
            item.put("reviewer", reviewer != null ? (reviewer.getName() != null ? reviewer.getName() : reviewer.getUsername()) : "-");
            item.put("result", result);
            item.put("reviewTime", h.getCreateTime());
            item.put("remark", h.getContent());
            return item;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<Transaction> getTransactions(String code, String confirmStatus, String paymentStatus, Pageable pageable) {
        return transactionRepository.searchTransactions(code, confirmStatus, paymentStatus, pageable);
    }

    @Override
    public Page<TransactionDTO> getTransactionsDTO(String code, String confirmStatus, String paymentStatus, Pageable pageable) {
        Page<Transaction> page = transactionRepository.searchTransactions(code, confirmStatus, paymentStatus, pageable);
        List<Transaction> list = page.getContent();
        if (list == null || list.isEmpty()) {
            return new PageImpl<>(java.util.Collections.emptyList(), pageable, page.getTotalElements());
        }

        List<Long> assetIds = list.stream()
                .map(Transaction::getAssetId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Asset> assetMap = assetIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : assetRepository.findAllById(assetIds).stream().collect(java.util.stream.Collectors.toMap(Asset::getId, a -> a));

        List<Long> winnerIds = list.stream()
                .map(Transaction::getWinnerId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = winnerIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(winnerIds).stream().collect(java.util.stream.Collectors.toMap(User::getId, u -> u));

        List<TransactionDTO> dtoList = list.stream()
                .map(t -> TransactionMapper.toDTO(t, assetMap.get(t.getAssetId()), userMap.get(t.getWinnerId())))
                .toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void reviewPayment(Long transactionId, boolean passed, MultipartFile voucher, String voucherUrl, String remark, Long operatorId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("交易单不存在"));

        String confirmStatus = Optional.ofNullable(transaction.getConfirmStatus()).orElse("").trim().toLowerCase();
        if (!"confirmed".equals(confirmStatus)) {
            throw new RuntimeException("交易单状态不允许审核");
        }

        String paymentStatus = Optional.ofNullable(transaction.getPaymentStatus()).orElse("").trim().toLowerCase();
        if (!"pending".equals(paymentStatus)) {
            throw new RuntimeException("交易单已审核过");
        }

        LocalDateTime now = LocalDateTime.now();

        if (passed) {
            // 审核通过
            transaction.setPaymentStatus("approved");
            transaction.setPaymentTime(now);

            String savedUrl = null;
            boolean hasNewVoucher = false;
            if (voucher != null && !voucher.isEmpty()) {
                savedUrl = uploadFile(voucher);
                hasNewVoucher = true;
            } else if (voucherUrl != null && !voucherUrl.trim().isEmpty()) {
                savedUrl = voucherUrl.trim();
                hasNewVoucher = true;
            }
            String existingVoucher = transaction.getPaymentVoucher();
            if (!hasNewVoucher && (existingVoucher == null || existingVoucher.trim().isEmpty())) {
                throw new RuntimeException("请上传付款凭证");
            }
            if (hasNewVoucher) {
                transaction.setPaymentVoucher(savedUrl);
            }

            transaction.setPaymentRemark(remark);
            transaction.setUpdateTime(now);
            transactionRepository.save(transaction);

            // 记录资产历史
            String content = "交易单审核通过：" + transaction.getCode();
            if (remark != null && !remark.trim().isEmpty()) {
                content += "，备注：" + remark.trim();
            }
            recordHistory(transaction.getAssetId(), operatorId, "确认收款", content);
        } else {
            // 审核不通过
            transaction.setPaymentStatus("rejected");
            transaction.setPaymentRemark(remark);
            transaction.setUpdateTime(now);
            transactionRepository.save(transaction);

            // 审核不通过：取消交易，资产恢复为待拍卖
            if (transaction.getAssetId() != null) {
                assetRepository.findById(transaction.getAssetId()).ifPresent(asset -> {
                    asset.setStatus("待拍卖");
                    asset.setUpdateTime(now);
                    assetRepository.save(asset);
                });
            }

            // 拍卖状态更新（可选）
            if (transaction.getAuctionId() != null) {
                auctionRepository.findById(transaction.getAuctionId()).ifPresent(auction -> {
                    auction.setStatus("已取消");
                    auction.setUpdateTime(now);
                    auctionRepository.save(auction);
                });
            }

            // 违约惩罚：限制中标者 3 个月竞拍资格
            if (transaction.getWinnerId() != null) {
                userRepository.findById(transaction.getWinnerId()).ifPresent(user -> {
                    user.setBidBanUntil(now.plusMonths(3));
                    userRepository.save(user);
                });
            }

            // 记录资产历史
            recordHistory(transaction.getAssetId(), operatorId, "拒绝付款", "交易单审核不通过：" + transaction.getCode() +
                    "，原因：" + (remark != null && !remark.trim().isEmpty() ? remark.trim() : "未说明"));
            recordHistory(transaction.getAssetId(), operatorId, "违约惩罚", "中标者付款未通过，限制竞拍 3 个月");
        }
    }

    @Override
    public FinanceStatistics getStatistics(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.MIN;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.MAX;

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);

        long totalTransactions = transactionRepository.countByCreateTimeBetween(startTime, endTime);
        BigDecimal totalAmount = transactionRepository.sumFinalPriceByPaymentStatusAndCreateTimeBetween("approved", startTime, endTime)
                .orElse(BigDecimal.ZERO);
        long confirmedTransactions = transactionRepository.countByConfirmStatusAndCreateTimeBetween("confirmed", startTime, endTime);
        long pendingTransactions = transactionRepository.countByPaymentStatusAndCreateTimeBetween("pending", startTime, endTime);
        long rejectedTransactions = transactionRepository.countByPaymentStatusAndCreateTimeBetween("rejected", startTime, endTime);

        return new FinanceStatistics(
                totalTransactions,
                totalAmount,
                confirmedTransactions,
                pendingTransactions,
                rejectedTransactions
        );
    }

    /**
     * 上传文件（保存到本地 ./uploads 下，并返回可访问的 /uploads/... URL）
     */
    private String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String safeDir = "vouchers/payment";

            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot);
            }

            String filename = UUID.randomUUID() + ext;
            Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path targetDir = baseDir.resolve(safeDir).resolve(date);
            Files.createDirectories(targetDir);

            Path target = targetDir.resolve(filename);
            try {
                file.transferTo(target.toFile());
            } catch (Exception ex) {
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + safeDir + "/" + date + "/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("上传凭证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 记录资产历史
     */
    private void recordHistory(Long assetId, Long operatorId, String operation, String content) {
        AssetHistory history = new AssetHistory();
        history.setAssetId(assetId);
        history.setOperatorId(operatorId);
        history.setOperation(operation);
        history.setContent(content);
        history.setCreateTime(LocalDateTime.now());
        assetHistoryRepository.save(history);
    }
}
