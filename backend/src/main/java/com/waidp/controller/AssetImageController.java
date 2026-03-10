package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.service.AssetImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/asset-images")
@RequiredArgsConstructor
@Slf4j
public class AssetImageController {

    private final AssetImageService assetImageService;

    @PostMapping("/upload/{assetId}")
    public Result<List<String>> upload(@PathVariable Long assetId, @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> urls = assetImageService.uploadImages(assetId, files);
            return Result.success("上传成功", urls);
        } catch (Exception e) {
            log.error("上传资产图片失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/import-from-directory/{assetId}")
    public Result<List<String>> importFromDirectory(@PathVariable Long assetId, @RequestParam String directory) {
        try {
            List<String> urls = assetImageService.importFromDirectory(assetId, directory);
            return Result.success("导入成功", urls);
        } catch (Exception e) {
            log.error("从目录导入资产图片失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/asset/{assetId}")
    public Result<List<String>> list(@PathVariable Long assetId) {
        return Result.success(assetImageService.getAssetImages(assetId));
    }
}

