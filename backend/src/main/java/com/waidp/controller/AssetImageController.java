package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.service.AssetImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 资产图片控制器
 */
@RestController
@RequestMapping("/api/asset-images")
@RequiredArgsConstructor
@Slf4j
public class AssetImageController {

    private final AssetImageService assetImageService;

    /**
     * 上传资产图片
     */
    @PostMapping("/upload/{assetId}")
    public Result<List<String>> uploadImages(
            @PathVariable Long assetId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> imageUrls = assetImageService.uploadImages(assetId, files);
            return Result.success("上传成功", imageUrls);
        } catch (Exception e) {
            log.error("上传资产图片失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 从目录导入资产图片
     */
    @PostMapping("/import-from-directory/{assetId}")
    public Result<List<String>> importFromDirectory(
            @PathVariable Long assetId,
            @RequestParam String directory) {
        try {
            List<String> imageUrls = assetImageService.importFromDirectory(assetId, directory);
            return Result.success("导入成功", imageUrls);
        } catch (Exception e) {
            log.error("从目录导入资产图片失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取资产图片列表
     */
    @GetMapping("/asset/{assetId}")
    public Result<List<String>> getAssetImages(@PathVariable Long assetId) {
        try {
            List<String> imageUrls = assetImageService.getAssetImages(assetId);
            return Result.success(imageUrls);
        } catch (Exception e) {
            log.error("获取资产图片失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}