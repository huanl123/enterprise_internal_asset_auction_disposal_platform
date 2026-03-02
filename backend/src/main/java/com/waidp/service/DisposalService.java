package com.waidp.service;

import com.waidp.dto.AssetDisposalDetail;
import com.waidp.dto.TransactionDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产处置服务接口
 */
public interface DisposalService {

    /**
     * 获取待处置资产列表
     */
    Page<Asset> getPendingDisposalAssets(Pageable pageable);

    /**
     * 确认处置完成
     *
     * @param voucher     直接上传的文件（可选）
     * @param voucherUrls 已上传文件URL（可选，支持多个，逗号分隔）
     */
    void confirmDisposal(Long assetId, MultipartFile voucher, String voucherUrls, String remark, Long operatorId);

    /**
     * 获取已处置资产列表
     */
    Page<Asset> getDisposedAssets(String code, String name, Pageable pageable, Long currentUserId, String currentRole);

    /**
     * 获取已处置资产详情（包含完整档案）
     */
    AssetDisposalDetail getDisposedAssetDetail(Long assetId, Long currentUserId, String currentRole);

    /**
     * 获取待处置交易单列表
     */
    Page<TransactionDTO> getPendingTransactions(Pageable pageable);

    /**
     * 获取已完成交易单列表
     */
    Page<TransactionDTO> getCompletedTransactions(Pageable pageable);

    /**
     * 获取处置历史
     */
    java.util.List<java.util.Map<String, Object>> getDisposalHistory();
}
