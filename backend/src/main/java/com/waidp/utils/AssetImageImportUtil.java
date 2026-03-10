package com.waidp.utils;

import com.waidp.WaidpBackendApplication;
import com.waidp.controller.AssetImageController;
import com.waidp.entity.Asset;
import com.waidp.repository.AssetRepository;
import com.waidp.service.impl.AssetServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 资产图片批量导入工具
 */
@Component
public class AssetImageImportUtil {

    private final AssetServiceImpl assetService;
    private final AssetRepository assetRepository;
    private final AssetImageController assetImageController;

    public AssetImageImportUtil(AssetServiceImpl assetService, 
                                AssetRepository assetRepository, 
                                AssetImageController assetImageController) {
        this.assetService = assetService;
        this.assetRepository = assetRepository;
        this.assetImageController = assetImageController;
    }

    /**
     * 从指定目录批量导入资产图片
     */
    public void importAssetImagesFromDirectory(String directoryPath) {
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
            
            // 调用资产图片控制器的方法导入图片
            try {
                var result = assetImageController.importFromDirectory(asset.getId(), directoryPath);
                if (result.getCode() == 200) {
                    System.out.println("成功为资产 " + asset.getName() + " 导入图片");
                } else {
                    System.out.println("为资产 " + asset.getName() + " 导入图片失败: " + result.getMessage());
                }
            } catch (Exception e) {
                System.out.println("为资产 " + asset.getName() + " 导入图片时发生错误: " + e.getMessage());
            }
        }

        System.out.println("资产图片批量导入完成");
    }

    /**
     * 主方法，用于命令行运行
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WaidpBackendApplication.class, args);
        
        // 获取工具实例
        AssetImageImportUtil importUtil = context.getBean(AssetImageImportUtil.class);
        
        // 从命令行参数获取目录路径
        if (args.length < 1) {
            System.out.println("请提供图片目录路径作为参数");
            System.out.println("示例: java -jar backend.jar \"E:\\\\Study\\\\code\\\\Java\\\\enterprise_internal_asset_auction_disposal_platform\\\\images\"");
            return;
        }
        
        String directoryPath = args[0];
        importUtil.importAssetImagesFromDirectory(directoryPath);
    }
}