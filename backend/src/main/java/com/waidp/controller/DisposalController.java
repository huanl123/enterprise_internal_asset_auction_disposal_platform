package com.waidp.controller;

import com.waidp.common.PageResult;
import com.waidp.common.Result;
import com.waidp.dto.AssetDisposalDetail;
import com.waidp.entity.Asset;
import com.waidp.entity.Transaction;
import com.waidp.dto.TransactionDTO;
import com.waidp.service.DisposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产处置控制器
 */
@RestController
@RequestMapping("/api/disposal")
@RequiredArgsConstructor
public class DisposalController {

    private final DisposalService disposalService;

    /**
     * 获取待处置资产列表
     */
    @GetMapping("/assets")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<PageResult<Asset>> getPendingDisposalAssets (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Asset> assetPage = disposalService.getPendingDisposalAssets(pageable);
        return Result.success(PageResult.of(assetPage));
    }

    /**
     * 确认处置完成
     */
    @PostMapping("/assets/{id}/confirm")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> confirmDisposal (
            @PathVariable Long id,
            @RequestParam(value = "voucher", required = false) MultipartFile voucher,
            @RequestParam(value = "voucherUrls", required = false) String voucherUrls,
            @RequestParam(value = "voucherUrl", required = false) String voucherUrl,
            @RequestParam(value = "remark", required = false) String remark,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        String mergedUrls = voucherUrls;
        if ((mergedUrls == null || mergedUrls.isBlank()) && voucherUrl != null && !voucherUrl.isBlank()) {
            mergedUrls = voucherUrl;
        }
        disposalService.confirmDisposal(id, voucher, mergedUrls, remark, operatorId);
        return Result.success("确认处置完成", null);
    }

    /**
     * 获取已处置资产列表
     */
    @GetMapping("/assets/disposed")
    public Result<PageResult<Asset>> getDisposedAssets(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        // Asset实体没有disposalTime字段，已处置场景下使用updateTime近似表示处置完成时间
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("updateTime").descending());
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        Page<Asset> assetPage = disposalService.getDisposedAssets(code, name, pageable, currentUserId, role);
        return Result.success(PageResult.of(assetPage));
    }

    /**
     * 获取已处置资产详情（包含完整档案）
     */
    @GetMapping("/assets/{id}")
    public Result<AssetDisposalDetail> getDisposedAssetDetail(
            @PathVariable Long id,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        return Result.success(disposalService.getDisposedAssetDetail(id, currentUserId, role));
    }

    /**
     * 获取待处置交易单列表
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<PageResult<TransactionDTO>> getPendingTransactions (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<TransactionDTO> transactionPage = disposalService.getPendingTransactions(pageable);
        return Result.success(PageResult.of(transactionPage));
    }

    /**
     * 获取已处置交易单列表
     */
    @GetMapping("/transactions/completed")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<PageResult<TransactionDTO>> getCompletedTransactions (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<TransactionDTO> transactionPage = disposalService.getCompletedTransactions(pageable);
        return Result.success(PageResult.of(transactionPage));
    }

    /**
     * 获取处置历史
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<java.util.List<java.util.Map<String, Object>>> getDisposalHistory() {
        return Result.success(disposalService.getDisposalHistory());
    }

}
