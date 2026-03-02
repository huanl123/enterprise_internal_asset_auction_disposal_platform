package com.waidp.controller;

import com.waidp.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件访问控制器
 */
@RestController
@RequestMapping("/api/files")
@Slf4j
public class FileController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传单个文件
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "dir", required = false) String dir) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String safeDir = (dir == null || dir.isBlank()) ? "files" : dir.trim();
            safeDir = safeDir.replace("..", "").replace("\\", "/");

            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot);
            }

            String filename = UUID.randomUUID() + ext;
            Path targetDir = Paths.get(uploadPath, safeDir, date);
            Files.createDirectories(targetDir);

            Path target = targetDir.resolve(filename);
            file.transferTo(target.toFile());

            String url = "/uploads/" + safeDir + "/" + date + "/" + filename;

            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", original);
            data.put("size", file.getSize());
            data.put("contentType", file.getContentType());

            return Result.success("上传成功", data);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     * POST /api/files/upload/images
     */
    @PostMapping("/upload/images")
    public Result<Map<String, Object>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("文件不能为空");
        }

        try {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String safeDir = "images";
            Path targetDir = Paths.get(uploadPath, safeDir, date);
            Files.createDirectories(targetDir);

            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
                String ext = "";
                int dot = original.lastIndexOf('.');
                if (dot >= 0 && dot < original.length() - 1) {
                    ext = original.substring(dot);
                }

                String filename = UUID.randomUUID() + ext;
                Path target = targetDir.resolve(filename);
                file.transferTo(target.toFile());
                urls.add("/uploads/" + safeDir + "/" + date + "/" + filename);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("urls", urls);
            data.put("count", urls.size());
            return Result.success("上传成功", data);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取资产图片
     */
    @GetMapping("/assets/{assetId}/{filename}")
    public ResponseEntity<Resource> getAssetImage(
            @PathVariable Long assetId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadPath, "assets", assetId.toString(), filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("获取文件失败", e);
            return ResponseEntity.notFound().build();
        }
    }
}
