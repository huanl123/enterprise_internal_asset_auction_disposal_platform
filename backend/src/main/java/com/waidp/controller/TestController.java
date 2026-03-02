package com.waidp.controller;

import com.waidp.entity.Asset;
import com.waidp.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试控制器，用于调试
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final AssetRepository assetRepository;

    /**
     * 测试查询所有资产
     */
    @GetMapping("/assets/all")
    public Object testAllAssets() {
        try {
            List<Asset> assets = assetRepository.findAll();
            Map<String, Long> statusCount = assets.stream()
                    .collect(Collectors.groupingBy(Asset::getStatus, Collectors.counting()));
            
            return Map.of(
                    "total", assets.size(),
                    "statusCount", statusCount,
                    "assets", assets.stream().limit(5).toList()
            );
        } catch (Exception e) {
            log.error("测试查询所有资产失败", e);
            return Map.of("error", e.getMessage(), "stackTrace", getStackTrace(e));
        }
    }

    /**
     * 测试查询待审核资产
     */
    @GetMapping("/assets/pending")
    public Object testPendingAssets(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Asset> assetPage = assetRepository.findByStatus("待审核", pageable);
            
            return Map.of(
                    "total", assetPage.getTotalElements(),
                    "page", page,
                    "size", size,
                    "assets", assetPage.getContent()
            );
        } catch (Exception e) {
            log.error("测试查询待审核资产失败", e);
            return Map.of("error", e.getMessage(), "stackTrace", getStackTrace(e));
        }
    }

    /**
     * 测试查询多状态资产
     */
    @GetMapping("/assets/multi-status")
    public Object testMultiStatusAssets(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Asset> assetPage = assetRepository.findByStatusIn(List.of("待审核", "PENDING"), pageable);
            
            return Map.of(
                    "total", assetPage.getTotalElements(),
                    "page", page,
                    "size", size,
                    "assets", assetPage.getContent()
            );
        } catch (Exception e) {
            log.error("测试查询多状态资产失败", e);
            return Map.of("error", e.getMessage(), "stackTrace", getStackTrace(e));
        }
    }

    /**
     * 获取异常堆栈跟踪
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}