package com.waidp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class UploadConfig implements ApplicationRunner {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void run(ApplicationArguments args) {
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                log.info("创建上传目录: {}", uploadDir.getAbsolutePath());
            }

            File assetsDir = new File(uploadPath, "assets");
            if (!assetsDir.exists()) {
                assetsDir.mkdirs();
                log.info("创建资产图片目录: {}", assetsDir.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("初始化上传目录失败", e);
        }
    }
}

