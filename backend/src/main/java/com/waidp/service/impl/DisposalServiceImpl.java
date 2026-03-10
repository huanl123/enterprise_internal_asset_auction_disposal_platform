package com.waidp.service.impl;

import com.waidp.dto.AssetDisposalDetail;
import com.waidp.dto.TransactionDTO;
import com.waidp.entity.*;
import com.waidp.mapper.TransactionMapper;
import com.waidp.repository.*;
import com.waidp.service.DisposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 资产处置服务实现
 */
@Service
@RequiredArgsConstructor
public class DisposalServiceImpl implements DisposalService {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final AssetHistoryRepository assetHistoryRepository;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public Page<Asset> getPendingDisposalAssets(Pageable pageable) {
        return assetRepository.findByStatus("待处置", pageable);
    }

    @Override
    @Transactional
    public void confirmDisposal(Long assetId, MultipartFile voucher, String voucherUrls, String remark, Long operatorId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        String assetStatus = asset.getStatus() == null ? "" : asset.getStatus().trim();
        if (!"待处置".equals(assetStatus)) {
            throw new RuntimeException("资产状态不允许处置，当前状态：" + asset.getStatus());
        }

        // 查找对应的交易单（以交易单状态为准，不依赖拍卖状态中文/英文的混用）
        Optional<Transaction> txOpt = transactionRepository.findFirstByAssetIdAndPaymentStatusAndDisposalStatus(assetId, "approved", "pending")
                .or(() -> transactionRepository.findFirstByAssetIdAndPaymentStatusAndDisposalStatus(assetId, "APPROVED", "PENDING"))
                .or(() -> transactionRepository.findFirstByAssetIdAndPaymentStatusAndDisposalStatus(assetId, "approved", "PENDING"))
                .or(() -> transactionRepository.findFirstByAssetIdAndPaymentStatusAndDisposalStatus(assetId, "APPROVED", "pending"));

        Transaction transaction = txOpt.orElseThrow(() -> new RuntimeException("未找到待处置的交易单"));

        LocalDateTime now = LocalDateTime.now();

        // 处置凭证：支持（1）直接上传文件（2）先调用 /api/files/upload 后把 URL 传进来（逗号分隔）
        String merged = (voucherUrls == null) ? "" : voucherUrls.trim();
        if (voucher != null && !voucher.isEmpty()) {
            String saved = uploadFile(voucher);
            merged = merged.isEmpty() ? saved : (merged + "," + saved);
        }
        if (merged.isEmpty()) {
            throw new RuntimeException("处置凭证不能为空");
        }

        // 更新交易单处置信息
        transaction.setDisposalStatus("completed");
        transaction.setDisposalTime(now);
        transaction.setDisposalVoucher(merged);
        transaction.setDisposalRemark(remark);
        transaction.setUpdateTime(now);
        transactionRepository.save(transaction);

        // 更新资产状态
        asset.setStatus("已处置");
        asset.setUpdateTime(now);
        assetRepository.save(asset);

        // 记录资产历史
        String content = "确认处置完成";
        if (remark != null && !remark.trim().isEmpty()) {
            content += "，备注：" + remark.trim();
        }
        Long resolvedOperatorId = operatorId;
        if (resolvedOperatorId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                resolvedOperatorId = userRepository.findByUsername(authentication.getName())
                        .map(User::getId)
                        .orElse(null);
            }
        }
        recordHistory(assetId, resolvedOperatorId, "确认处置", content);
    }

    @Override
    public Page<Asset> getDisposedAssets(String code, String name, Pageable pageable, Long currentUserId, String currentRole) {
        ArchiveAccessScope accessScope = resolveArchiveAccessScope(currentUserId, currentRole);
        Long currentDepartmentId = resolveCurrentDepartmentId(currentUserId);
        Specification<Asset> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "已处置"));
            if (code != null && !code.isEmpty()) {
                predicates.add(cb.like(root.get("code"), "%" + code + "%"));
            }
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (accessScope == ArchiveAccessScope.DEPARTMENT_RELATED) {
                if (currentDepartmentId == null) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(cb.equal(root.get("departmentId"), currentDepartmentId));
                }
            } else if (accessScope == ArchiveAccessScope.SELF_RELATED) {
                if (currentUserId == null) {
                    predicates.add(cb.disjunction());
                } else {
                    var subquery = query.subquery(Long.class);
                    var txRoot = subquery.from(Transaction.class);
                    subquery.select(txRoot.get("assetId"));
                    subquery.where(
                            cb.equal(txRoot.get("assetId"), root.get("id")),
                            cb.equal(txRoot.get("winnerId"), currentUserId),
                            cb.equal(cb.lower(txRoot.get("disposalStatus")), "completed")
                    );
                    predicates.add(cb.exists(subquery));
                }
            } else if (accessScope == ArchiveAccessScope.DENY_ALL) {
                predicates.add(cb.disjunction());
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return assetRepository.findAll(spec, pageable);
    }

    @Override
    public AssetDisposalDetail getDisposedAssetDetail(Long assetId, Long currentUserId, String currentRole) {
        ArchiveAccessScope accessScope = resolveArchiveAccessScope(currentUserId, currentRole);
        Long currentDepartmentId = resolveCurrentDepartmentId(currentUserId);
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        if (!"已处置".equals(asset.getStatus())) {
            throw new RuntimeException("资产未处置");
        }

        // 获取交易单（优先按资产ID取最新）
        Transaction transaction = transactionRepository.findFirstByAssetIdOrderByCreateTimeDesc(assetId)
                .orElseThrow(() -> new RuntimeException("未找到交易单"));

        if (accessScope == ArchiveAccessScope.DEPARTMENT_RELATED) {
            Long assetDepartmentId = asset.getDepartmentId();
            if (currentDepartmentId == null || assetDepartmentId == null || !assetDepartmentId.equals(currentDepartmentId)) {
                throw new RuntimeException("无权限查看该资产处置档案");
            }
        } else if (accessScope == ArchiveAccessScope.SELF_RELATED) {
            if (currentUserId == null || transaction.getWinnerId() == null || !transaction.getWinnerId().equals(currentUserId)) {
                throw new RuntimeException("无权限查看该资产处置档案");
            }
        } else if (accessScope == ArchiveAccessScope.DENY_ALL) {
            throw new RuntimeException("无权限查看该资产处置档案");
        }

        // 获取对应拍卖记录
        Auction auction = null;
        if (transaction.getAuctionId() != null) {
            auction = auctionRepository.findById(transaction.getAuctionId()).orElse(null);
        }
        if (auction == null) {
            auction = auctionRepository.findByAssetId(assetId).stream()
                    .filter(a -> "ended".equals(a.getStatus()))
                    .findFirst()
                    .orElse(null);
        }

        // 获取资产历史
        List<AssetHistory> history = assetHistoryRepository.findByAssetIdOrderByCreateTimeDesc(assetId);

        if (history != null && !history.isEmpty()) {
            List<Long> operatorIds = history.stream()
                    .map(AssetHistory::getOperatorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Map<Long, User> operatorMap = operatorIds.isEmpty()
                    ? java.util.Collections.emptyMap()
                    : userRepository.findAllById(operatorIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            history.forEach(h -> {
                User operator = h.getOperatorId() != null ? operatorMap.get(h.getOperatorId()) : null;
                String name = operator != null ? (operator.getName() != null ? operator.getName() : operator.getUsername()) : "-";
                h.setOperatorName(name);
            });
        }

        // 获取竞价记录
        List<Bid> bids = auction != null
                ? bidRepository.findByAuctionIdOrderByBidTimeAsc(auction.getId())
                : java.util.Collections.emptyList();

        String winnerName = "";
        String winnerPhone = "";
        String winnerDepartment = "";
        if (transaction.getWinnerId() != null) {
            User winner = userRepository.findById(transaction.getWinnerId()).orElse(null);
            if (winner != null) {
                winnerName = winner.getName() != null ? winner.getName() : winner.getUsername();
                winnerPhone = winner.getPhone() != null ? winner.getPhone() : "";
                if (winner.getDepartmentId() != null) {
                    Department department = departmentRepository.findById(winner.getDepartmentId()).orElse(null);
                    winnerDepartment = department != null ? department.getName() : "";
                }
            }
        }

        java.time.LocalDateTime winTime = auction != null ? auction.getEndTime() : null;
        return new AssetDisposalDetail(asset, transaction, history, bids, winnerName, winnerPhone, winnerDepartment, winTime);
    }

    private Long resolveCurrentDepartmentId(Long currentUserId) {
        if (currentUserId == null) {
            return null;
        }
        return userRepository.findById(currentUserId)
                .map(User::getDepartmentId)
                .orElse(null);
    }

    private ArchiveAccessScope resolveArchiveAccessScope(Long currentUserId, String fallbackRole) {
        String roleFromDb = null;
        if (currentUserId != null) {
            roleFromDb = userRepository.findById(currentUserId)
                    .map(User::getRole)
                    .orElse(null);
        }
        String effectiveRole = StringUtils.hasText(roleFromDb) ? roleFromDb : fallbackRole;
        return resolveArchiveAccessScope(effectiveRole);
    }

    private ArchiveAccessScope resolveArchiveAccessScope(String role) {
        if (role == null || role.trim().isEmpty()) {
            return ArchiveAccessScope.DENY_ALL;
        }
        String normalized = role.trim();
        if (isAdminRole(normalized)) {
            return ArchiveAccessScope.ALL;
        }
        if (isSpecialistRole(normalized)) {
            return ArchiveAccessScope.DEPARTMENT_RELATED;
        }
        if (isOrdinaryUserRole(normalized)) {
            return ArchiveAccessScope.SELF_RELATED;
        }
        return ArchiveAccessScope.DENY_ALL;
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role)
                || "SYSTEM_ADMIN".equalsIgnoreCase(role)
                || "admin".equalsIgnoreCase(role)
                || "system_admin".equalsIgnoreCase(role)
                || "系统管理员".equals(role);
    }

    private boolean isSpecialistRole(String role) {
        return "ASSET_SPECIALIST".equalsIgnoreCase(role)
                || "FINANCE_SPECIALIST".equalsIgnoreCase(role)
                || "asset_specialist".equalsIgnoreCase(role)
                || "finance_specialist".equalsIgnoreCase(role)
                || "资产专员".equals(role)
                || "财务专员".equals(role);
    }

    private boolean isOrdinaryUserRole(String role) {
        return "NORMAL_USER".equalsIgnoreCase(role)
                || "EMPLOYEE".equalsIgnoreCase(role)
                || "employee".equalsIgnoreCase(role)
                || "普通员工".equals(role);
    }

    private enum ArchiveAccessScope {
        ALL,
        DEPARTMENT_RELATED,
        SELF_RELATED,
        DENY_ALL
    }

    @Override
    public Page<TransactionDTO> getPendingTransactions(Pageable pageable, Long currentUserId, String currentRole) {
        ArchiveAccessScope accessScope = resolveArchiveAccessScope(currentUserId, currentRole);
        Long currentDepartmentId = resolveCurrentDepartmentId(currentUserId);
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.or(
                    cb.equal(root.get("paymentStatus"), "approved"),
                    cb.equal(root.get("paymentStatus"), "APPROVED")
            ));
            predicates.add(cb.or(
                    cb.equal(root.get("disposalStatus"), "pending"),
                    cb.equal(root.get("disposalStatus"), "PENDING")
            ));

            if (accessScope == ArchiveAccessScope.DEPARTMENT_RELATED) {
                if (currentDepartmentId == null) {
                    predicates.add(cb.disjunction());
                } else {
                    var assetJoin = root.join("asset");
                    predicates.add(cb.equal(assetJoin.get("departmentId"), currentDepartmentId));
                }
            } else if (accessScope == ArchiveAccessScope.SELF_RELATED) {
                if (currentUserId == null) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(cb.equal(root.get("winnerId"), currentUserId));
                }
            } else if (accessScope == ArchiveAccessScope.DENY_ALL) {
                predicates.add(cb.disjunction());
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        if (transactions.getContent() == null || transactions.getContent().isEmpty()) {
            return new PageImpl<>(java.util.Collections.emptyList(), pageable, transactions.getTotalElements());
        }

        List<Long> assetIds = transactions.getContent().stream()
                .map(Transaction::getAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Asset> assetMap = assetIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : assetRepository.findAllById(assetIds).stream().collect(Collectors.toMap(Asset::getId, a -> a));

        List<Long> winnerIds = transactions.getContent().stream()
                .map(Transaction::getWinnerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = winnerIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(winnerIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<TransactionDTO> dtoList = transactions.getContent().stream()
                .map(t -> TransactionMapper.toDTO(t, assetMap.get(t.getAssetId()), userMap.get(t.getWinnerId())))
                .toList();

        return new PageImpl<>(dtoList, pageable, transactions.getTotalElements());
    }

    @Override
    public Page<TransactionDTO> getCompletedTransactions(Pageable pageable, Long currentUserId, String currentRole) {
        ArchiveAccessScope accessScope = resolveArchiveAccessScope(currentUserId, currentRole);
        Long currentDepartmentId = resolveCurrentDepartmentId(currentUserId);
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.or(
                    cb.equal(root.get("paymentStatus"), "approved"),
                    cb.equal(root.get("paymentStatus"), "APPROVED")
            ));
            predicates.add(cb.or(
                    cb.equal(root.get("disposalStatus"), "completed"),
                    cb.equal(root.get("disposalStatus"), "COMPLETED")
            ));

            if (accessScope == ArchiveAccessScope.DEPARTMENT_RELATED) {
                if (currentDepartmentId == null) {
                    predicates.add(cb.disjunction());
                } else {
                    var assetJoin = root.join("asset");
                    predicates.add(cb.equal(assetJoin.get("departmentId"), currentDepartmentId));
                }
            } else if (accessScope == ArchiveAccessScope.SELF_RELATED) {
                if (currentUserId == null) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(cb.equal(root.get("winnerId"), currentUserId));
                }
            } else if (accessScope == ArchiveAccessScope.DENY_ALL) {
                predicates.add(cb.disjunction());
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        if (transactions.getContent() == null || transactions.getContent().isEmpty()) {
            return new PageImpl<>(java.util.Collections.emptyList(), pageable, transactions.getTotalElements());
        }

        List<Long> assetIds = transactions.getContent().stream()
                .map(Transaction::getAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Asset> assetMap = assetIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : assetRepository.findAllById(assetIds).stream().collect(Collectors.toMap(Asset::getId, a -> a));

        List<Long> winnerIds = transactions.getContent().stream()
                .map(Transaction::getWinnerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = winnerIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(winnerIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<TransactionDTO> dtoList = transactions.getContent().stream()
                .map(t -> TransactionMapper.toDTO(t, assetMap.get(t.getAssetId()), userMap.get(t.getWinnerId())))
                .toList();

        return new PageImpl<>(dtoList, pageable, transactions.getTotalElements());
    }

    @Override
    public List<Map<String, Object>> getDisposalHistory() {
        List<Asset> disposedAssets = assetRepository.findByStatus("已处置");
        if (disposedAssets == null || disposedAssets.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<Long> assetIds = disposedAssets.stream()
                .map(Asset::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (assetIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<AssetHistory> histories = assetHistoryRepository.findByAssetIdInOrderByCreateTimeDesc(assetIds);
        if (histories == null || histories.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        Map<Long, Asset> assetMap = disposedAssets.stream().collect(Collectors.toMap(Asset::getId, a -> a));

        List<Long> operatorIds = histories.stream()
                .map(AssetHistory::getOperatorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = operatorIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(operatorIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        return histories.stream().map(h -> {
            Map<String, Object> item = new java.util.HashMap<>();
            Asset asset = assetMap.get(h.getAssetId());
            User operator = h.getOperatorId() != null ? userMap.get(h.getOperatorId()) : null;

            item.put("assetId", h.getAssetId());
            item.put("assetCode", asset != null ? asset.getCode() : "-");
            item.put("assetName", asset != null ? asset.getName() : "-");
            item.put("action", h.getOperation());
            item.put("operator", operator != null ? (operator.getName() != null ? operator.getName() : operator.getUsername()) : "-");
            item.put("operationTime", h.getCreateTime());
            item.put("remark", h.getContent());
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 上传文件
     */
    private String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String safeDir = "vouchers/disposal";

            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot);
            }

            String filename = UUID.randomUUID() + ext;
            Path targetDir = Paths.get(uploadPath, safeDir, date);
            Files.createDirectories(targetDir);

            Path target = targetDir.resolve(filename);
            file.transferTo(target.toFile());

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
