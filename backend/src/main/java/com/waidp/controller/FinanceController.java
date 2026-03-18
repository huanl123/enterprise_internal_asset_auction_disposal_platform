package com.waidp.controller;

import com.waidp.common.PageResult;
import com.waidp.common.Result;
import com.waidp.dto.ApproveRequest;
import com.waidp.dto.AssetDTO;
import com.waidp.dto.FinanceStatistics;
import com.waidp.entity.Asset;
import com.waidp.entity.Transaction;
import com.waidp.dto.TransactionDTO;
import com.waidp.service.FinanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 财务管理控制器
 * 提供财务审核相关的所有接口，包括：
 * 资产定价审核（待审核资产列表、通过/驳回）、设置保留价（通过审核时可设置保留价）、
 * 交易单列表查询（待付款、已付款等）、付款审核（确认收款、驳回付款）、
 * 财务统计数据等。
 * 
 * 权限要求：财务专员或系统管理员
 */
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    /**
     * 获取待审核资产列表
     */
    @GetMapping("/assets/pending")
    @PreAuthorize("hasAnyRole('FINANCE_SPECIALIST','finance_specialist','财务专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<PageResult<AssetDTO>> getPendingAssets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            System.out.println("FinanceController.getPendingAssets called with page: " + page + ", size: " + size);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
            Page<AssetDTO> assetPage = financeService.getPendingAssetsDTO(pageable);
            System.out.println("Found " + assetPage.getTotalElements() + " pending assets");
            return Result.success(PageResult.of(assetPage));
        } catch (Exception e) {
            System.err.println("Error in getPendingAssets: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取待审核资产列表失败: " + e.getMessage());
        }
    }

    /**
     * 审核资产（批准定价）
     */
    @PostMapping("/assets/{id}/approve")
    @PreAuthorize("hasAnyRole('FINANCE_SPECIALIST','finance_specialist','财务专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> approveAsset(@PathVariable Long id, @Valid @RequestBody ApproveRequest request, HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        financeService.approveAsset(
                id,
                request.startPrice(),
                request.hasReservePrice(),
                request.reservePrice(),
                operatorId,
                request.remark()
        );
        return Result.success("审核通过", null);
    }

    /**
     * 获取资产定价审核历史（财务审核）
     */
    @GetMapping("/review-history")
    @PreAuthorize("hasAnyRole('FINANCE_SPECIALIST','finance_specialist','财务专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<List<Map<String, Object>>> getReviewHistory() {
        return Result.success(financeService.getAssetReviewHistory());
    }

    /**
     * 获取交易单列表（分页）
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('FINANCE_SPECIALIST','finance_specialist','财务专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<PageResult<TransactionDTO>> getTransactions (
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String confirmStatus,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<TransactionDTO> transactionPage = financeService.getTransactionsDTO(code, confirmStatus, paymentStatus, pageable);
        return Result.success(PageResult.of(transactionPage));
    }

    /**
     * 获取交易单详情
     */
    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('FINANCE_SPECIALIST','finance_specialist','财务专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Transaction> getTransactionById(@PathVariable Long id) {
        return Result.success(financeService.getTransactionById(id));
    }

    /**
     * 审核交易单（确认收款）
     */
    @PostMapping("/transactions/{id}/payment")
    @PreAuthorize("hasAnyRole('FINANCE_SPECIALIST','finance_specialist','财务专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> reviewPayment (
            @PathVariable Long id,
            @RequestParam("passed") boolean passed,
            @RequestParam(value = "voucher", required = false) MultipartFile voucher,
            @RequestParam(value = "voucherUrl", required = false) String voucherUrl,
            @RequestParam(value = "remark", required = false) String remark,
            HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        financeService.reviewPayment(id, passed, voucher, voucherUrl, remark, operatorId);
        return Result.success(passed ? "审核通过" : "审核不通过", null);
    }

    /**
     * 获取财务统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('FINANCE_SPECIALIST','finance_specialist','财务专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<FinanceStatistics> getStatistics (
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(financeService.getStatistics(startDate, endDate));
    }
}
