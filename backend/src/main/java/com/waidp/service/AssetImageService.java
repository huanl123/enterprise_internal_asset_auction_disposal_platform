package com.waidp.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 资产图片服务接口
 */
public interface AssetImageService {

    /**
     * 上传资产图片
     */
    List<String> uploadImages(Long assetId, MultipartFile[] files);

    /**
     * 从目录导入资产图片
     */
    List<String> importFromDirectory(Long assetId, String directory);

    /**
     * 获取资产图片列表
     */
    List<String> getAssetImages(Long assetId);
}