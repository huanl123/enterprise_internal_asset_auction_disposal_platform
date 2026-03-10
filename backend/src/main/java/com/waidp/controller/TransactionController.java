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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @GetMapping("/my/pending-payment-count")
    @PreAuthorize("hasAnyRole('EMPLOYEE','employee','NORMAL_USER','普通员工')")
    public Result<Long> getMyPendingPaymentCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("Not logged in");
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
            return Result.error("Not logged in");
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
}
