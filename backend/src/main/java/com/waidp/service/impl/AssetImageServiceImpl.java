package com.waidp.service.impl;

import com.waidp.entity.Asset;
import com.waidp.repository.AssetRepository;
import com.waidp.service.AssetImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 资产图片服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetImageServiceImpl implements AssetImageService {

    private final AssetRepository assetRepository;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    @Transactional
    public List<String> uploadImages(Long assetId, MultipartFile[] files) {
        // 验证资产是否存在
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        List<String> imageUrls = new ArrayList<>();

        // 创建资产图片目录
        String assetDir = uploadPath + File.separator + "assets" + File.separator + assetId;
        File directory = new File(assetDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 上传每个文件
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                // 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
                String filename = UUID.randomUUID().toString() + extension;
                
                // 保存文件
                Path filePath = Paths.get(assetDir, filename);
                Files.copy(file.getInputStream(), filePath);
                
                // 生成访问URL
                String imageUrl = "/api/files/assets/" + assetId + "/" + filename;
                imageUrls.add(imageUrl);
                
                log.info("上传资产图片成功: {}, 资产ID: {}", filename, assetId);
            } catch (IOException e) {
                log.error("上传资产图片失败: {}", e.getMessage());
                throw new RuntimeException("上传图片失败: " + e.getMessage());
            }
        }

        // 更新资产的图片字段
        if (!imageUrls.isEmpty()) {
            String existingImages = asset.getImages() != null ? asset.getImages() : "";
            String allImages = existingImages.isEmpty() ? String.join(",", imageUrls) 
                : existingImages + "," + String.join(",", imageUrls);
            asset.setImages(allImages);
            asset.setUpdateTime(LocalDateTime.now());
            assetRepository.save(asset);
        }

        return imageUrls;
    }

    @Override
    @Transactional
    public List<String> importFromDirectory(Long assetId, String directory) {
        // 验证资产是否存在
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        List<String> imageUrls = new ArrayList<>();
        File sourceDir = new File(directory);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new RuntimeException("目录不存在或不是有效的目录: " + directory);
        }

        // 创建资产图片目录
        String assetDir = uploadPath + File.separator + "assets" + File.separator + assetId;
        File targetDir = new File(assetDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // 复制目录中的所有图片文件
        File[] files = sourceDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") 
                || lowerName.endsWith(".png") || lowerName.endsWith(".gif") 
                || lowerName.endsWith(".webp") || lowerName.endsWith(".avif");
        });

        if (files != null) {
            for (File file : files) {
                try {
                    // 生成唯一文件名
                    String originalFilename = file.getName();
                    String extension = originalFilename.contains(".") 
                        ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                        : "";
                    String filename = UUID.randomUUID().toString() + extension;
                    
                    // 复制文件
                    Path sourcePath = file.toPath();
                    Path targetPath = Paths.get(assetDir, filename);
                    Files.copy(sourcePath, targetPath);
                    
                    // 生成访问URL
                    String imageUrl = "/api/files/assets/" + assetId + "/" + filename;
                    imageUrls.add(imageUrl);
                    
                    log.info("导入资产图片成功: {}, 资产ID: {}", filename, assetId);
                } catch (IOException e) {
                    log.error("导入资产图片失败: {}", e.getMessage());
                    // 继续处理其他文件，不中断整个导入过程
                }
            }
        }

        // 更新资产的图片字段
        if (!imageUrls.isEmpty()) {
            String existingImages = asset.getImages() != null ? asset.getImages() : "";
            String allImages = existingImages.isEmpty() ? String.join(",", imageUrls) 
                : existingImages + "," + String.join(",", imageUrls);
            asset.setImages(allImages);
            asset.setUpdateTime(LocalDateTime.now());
            assetRepository.save(asset);
        }

        return imageUrls;
    }

    @Override
    public List<String> getAssetImages(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        String images = asset.getImages();
        if (images == null || images.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 分割图片URL字符串
        String[] imageArray = images.split(",");
        List<String> imageList = new ArrayList<>();
        
        for (String image : imageArray) {
            if (!image.trim().isEmpty()) {
                imageList.add(image.trim());
            }
        }

        return imageList;
    }
}