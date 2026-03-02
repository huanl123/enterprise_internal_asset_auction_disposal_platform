package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.dto.TransactionDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.Transaction;
import com.waidp.entity.User;
import com.waidp.mapper.TransactionMapper;
import com.waidp.repository.AssetRepository;
import com.waidp.repository.TransactionRepository;
import com.waidp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @GetMapping("/my/pending-payment-count")
    @PreAuthorize("hasAnyRole('EMPLOYEE','employee','NORMAL_USER','普通员工')")
    public Result<Long> getMyPendingPaymentCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        long count = transactionRepository.countPendingPaymentByWinnerId(userId);
        return Result.success(count);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE','employee','NORMAL_USER','普通员工')")
    public Result<List<TransactionDTO>> getMyTransactions(
            HttpServletRequest request,
            String confirmStatus,
            String paymentStatus) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }

        String confirmFilter = confirmStatus == null ? null : confirmStatus.trim().toLowerCase(Locale.ROOT);
        String paymentFilter = paymentStatus == null ? null : paymentStatus.trim().toLowerCase(Locale.ROOT);

        List<Transaction> transactions = transactionRepository.findByWinnerIdOrderByCreateTimeDesc(userId)
                .stream()
                .filter(tx -> confirmFilter == null || confirmFilter.isEmpty()
                        || (tx.getConfirmStatus() != null
                        && tx.getConfirmStatus().trim().toLowerCase(Locale.ROOT).equals(confirmFilter)))
                .filter(tx -> paymentFilter == null || paymentFilter.isEmpty()
                        || (tx.getPaymentStatus() != null
                        && tx.getPaymentStatus().trim().toLowerCase(Locale.ROOT).equals(paymentFilter)))
                .collect(Collectors.toList());

        if (transactions.isEmpty()) {
            return Result.success(List.of());
        }

        List<Long> assetIds = transactions.stream()
                .map(Transaction::getAssetId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Asset> assetMap = assetIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : assetRepository.findAllById(assetIds)
                        .stream()
                        .collect(Collectors.toMap(Asset::getId, a -> a));

        List<Long> winnerIds = transactions.stream()
                .map(Transaction::getWinnerId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = winnerIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(winnerIds)
                        .stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        List<TransactionDTO> list = transactions.stream()
                .map(t -> TransactionMapper.toDTO(t, assetMap.get(t.getAssetId()), userMap.get(t.getWinnerId())))
                .collect(Collectors.toList());

        return Result.success(list);
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('EMPLOYEE','employee','NORMAL_USER','普通员工')")
    public Result<Void> submitPaymentVoucher(
            @PathVariable Long id,
            @RequestParam("voucher") MultipartFile voucher,
            @RequestParam(value = "remark", required = false) String remark,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("交易单不存在"));

        if (!userId.equals(transaction.getWinnerId())) {
            throw new RuntimeException("无权提交该交易单付款凭证");
        }

        String confirmStatus = String.valueOf(transaction.getConfirmStatus()).trim().toLowerCase(Locale.ROOT);
        if (!"confirmed".equals(confirmStatus)) {
            throw new RuntimeException("交易单未确认成交，无法付款");
        }

        String paymentStatus = String.valueOf(transaction.getPaymentStatus()).trim().toLowerCase(Locale.ROOT);
        if (!"pending".equals(paymentStatus)) {
            throw new RuntimeException("交易单已处理，无法重复付款");
        }

        if (voucher == null || voucher.isEmpty()) {
            throw new RuntimeException("请上传付款凭证");
        }

        String savedUrl = uploadFile(voucher);
        transaction.setPaymentVoucher(savedUrl);
        if (remark != null && !remark.trim().isEmpty()) {
            transaction.setPaymentRemark(remark.trim());
        }
        transaction.setUpdateTime(LocalDateTime.now());
        transactionRepository.save(transaction);

        return Result.success("付款凭证已提交", null);
    }

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
            throw new RuntimeException("上传付款凭证失败: " + e.getMessage(), e);
        }
    }
}
