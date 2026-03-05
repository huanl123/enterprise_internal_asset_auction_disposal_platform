package com.waidp.service;

import com.waidp.dto.AssetDTO;
import com.waidp.dto.FinanceStatistics;
import com.waidp.dto.TransactionDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * 财务服务接口
 */
public interface FinanceService {

    /**
     * 获取待审核资产列表
     */
    Page<Asset> getPendingAssets(Pageable pageable);
    
    /**
     * 获取待审核资产列表（返回DTO）
     */
    Page<AssetDTO> getPendingAssetsDTO(Pageable pageable);

    /**
     * 审核资产定价
     */
    void approveAsset(Long assetId, BigDecimal startPrice, boolean hasReservePrice, BigDecimal reservePrice, Long operatorId, String remark);

    /**
     * 获取资产定价审核历史（财务审核）
     */
    java.util.List<java.util.Map<String, Object>> getAssetReviewHistory();

    /**
     * 获取交易单列表
     */
    Page<Transaction> getTransactions(String code, String confirmStatus, String paymentStatus, Pageable pageable);

    /**
     * 获取交易单列表（DTO，避免懒加载序列化问题）
     */
    Page<TransactionDTO> getTransactionsDTO(String code, String confirmStatus, String paymentStatus, Pageable pageable);

    /**
     * 获取交易单详情
     */
    Transaction getTransactionById(Long id);

    /**
     * 审核收款
     *
     * @param voucher    直接上传的文件（可选）
     * @param voucherUrl 已上传文件的URL（可选）
     */
    void reviewPayment(Long transactionId, boolean passed, MultipartFile voucher, String voucherUrl, String remark, Long operatorId);

    /**
     * 获取财务统计信息
     */
    FinanceStatistics getStatistics(String startDate, String endDate);
}
