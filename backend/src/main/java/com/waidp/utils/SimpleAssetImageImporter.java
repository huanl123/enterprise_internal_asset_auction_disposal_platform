package com.waidp.utils;

import com.waidp.WaidpBackendApplication;
import com.waidp.entity.Asset;
import com.waidp.repository.AssetRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

/**
 * 资产图片批量导入工具（简化版）
 */
public class SimpleAssetImageImporter {

    public static void importAssetImagesFromDirectory(String directoryPath) {
        // 启动Spring Boot应用上下文
        ConfigurableApplicationContext context = SpringApplication.run(WaidpBackendApplication.class);
        
        try {
            // 获取必要的Bean
            AssetRepository assetRepository = context.getBean(AssetRepository.class);
            RestTemplate restTemplate = context.getBean(RestTemplate.class);
            
            System.out.println("开始从目录导入资产图片: " + directoryPath);

            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                System.out.println("指定目录不存在或不是有效目录: " + directoryPath);
                return;
            }

            // 获取所有资产
            List<Asset> assets = assetRepository.findAll();
            if (assets.isEmpty()) {
                System.out.println("数据库中没有找到任何资产");
                return;
            }

            File[] imageFiles = directory.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                       lowerName.endsWith(".png") || lowerName.endsWith(".gif") ||
                       lowerName.endsWith(".bmp");
            });

            if (imageFiles == null || imageFiles.length == 0) {
                System.out.println("目录中没有找到有效的图片文件");
                return;
            }

            System.out.println("找到 " + imageFiles.length + " 个图片文件");
            System.out.println("共有 " + assets.size() + " 个资产");

            // 为每个资产分配一张图片（如果图片数量足够）
            for (int i = 0; i < assets.size() && i < imageFiles.length; i++) {
                Asset asset = assets.get(i);
                File imageFile = imageFiles[i];
                
                System.out.println("为资产 '" + asset.getName() + "' (ID: " + asset.getId() + ") 导入图片: " + imageFile.getName());
                
                // 构建请求
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("files", new FileSystemResource(imageFile));
                
                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                
                // 发送请求到后端API
                try {
                    String url = "http://localhost:8081/api/asset-images/upload/" + asset.getId();
                    String response = restTemplate.postForObject(url, requestEntity, String.class);
                    System.out.println("API响应: " + response);
                    System.out.println("成功为资产 " + asset.getName() + " 导入图片");
                } catch (Exception e) {
                    System.out.println("为资产 " + asset.getName() + " 导入图片失败: " + e.getMessage());
                }
            }

            System.out.println("资产图片批量导入完成");
        } finally {
            // 关闭应用上下文
            context.close();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("请提供图片目录路径作为参数");
            System.out.println("示例: java -cp .;target/classes com.waidp.utils.SimpleAssetImageImporter \"E:\\\\Study\\\\code\\\\Java\\\\enterprise_internal_asset_auction_disposal_platform\\\\images\"");
            return;
        }
        
        String directoryPath = args[0];
        importAssetImagesFromDirectory(directoryPath);
    }
}