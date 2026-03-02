package com.waidp.controller;

import com.waidp.entity.Asset;
import com.waidp.entity.User;
import com.waidp.repository.AssetRepository;
import com.waidp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调试控制器，用于排查问题
 */
@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    /**
     * 检查当前用户信息和权限
     */
    @GetMapping("/current-user")
    public Map<String, Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> result = new HashMap<>();
        
        if (auth != null) {
            result.put("name", auth.getName());
            result.put("authorities", auth.getAuthorities());
            result.put("principal", auth.getPrincipal());
            result.put("isAuthenticated", auth.isAuthenticated());
        } else {
            result.put("message", "No authentication found");
        }
        
        return result;
    }

    /**
     * 检查财务专员用户信息
     */
    @GetMapping("/finance-user")
    public User getFinanceUser() {
        User user = userRepository.findByUsername("15815321902").orElse(null);
        if (user != null) {
            log.info("Found finance user: {}", user.getUsername());
            log.info("User role: {}", user.getRole());
        } else {
            log.error("Finance user not found");
        }
        return user;
    }

    /**
     * 检查资产状态
     */
    @GetMapping("/asset-status")
    public Map<String, Object> getAssetStatus() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有资产
        List<Asset> allAssets = assetRepository.findAll();
        result.put("totalAssets", allAssets.size());
        
        // 获取待审核资产
        Pageable pageable = PageRequest.of(0, 10);
        Page<Asset> pendingAssets = assetRepository.findByStatus("待审核", pageable);
        result.put("pendingAssets", pendingAssets.getContent());
        result.put("pendingCount", pendingAssets.getTotalElements());
        
        // 获取PENDING状态资产
        Page<Asset> pendingEnglishAssets = assetRepository.findByStatus("PENDING", pageable);
        result.put("pendingEnglishAssets", pendingEnglishAssets.getContent());
        result.put("pendingEnglishCount", pendingEnglishAssets.getTotalElements());
        
        return result;
    }

    /**
     * 检查资产详情
     */
    @GetMapping("/asset-detail")
    public Asset getAssetDetail(@RequestParam Long id) {
        return assetRepository.findById(id).orElse(null);
    }
    
    /**
     * 测试审核资产
     */
    @GetMapping("/test-approve")
    public Map<String, Object> testApproveAsset(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Asset asset = assetRepository.findById(id).orElse(null);
            if (asset == null) {
                result.put("error", "资产不存在");
                return result;
            }
            
            String status = asset.getStatus();
            String trimmedStatus = status != null ? status.trim() : null;
            
            result.put("assetId", asset.getId());
            result.put("assetName", asset.getName());
            result.put("assetStatus", status);
            result.put("trimmedStatus", trimmedStatus);
            result.put("statusLength", status != null ? status.length() : 0);
            result.put("canApprove", "待审核".equals(trimmedStatus) || "PENDING".equals(trimmedStatus));
            
            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    /**
     * 强制设置资产状态为待审核（仅用于测试）
     */
    @GetMapping("/force-pending")
    public Map<String, Object> forceAssetPending(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Asset asset = assetRepository.findById(id).orElse(null);
            if (asset == null) {
                result.put("error", "资产不存在");
                return result;
            }
            
            String oldStatus = asset.getStatus();
            asset.setStatus("待审核");
            assetRepository.save(asset);
            
            result.put("assetId", asset.getId());
            result.put("assetName", asset.getName());
            result.put("oldStatus", oldStatus);
            result.put("newStatus", asset.getStatus());
            result.put("success", true);
            
            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
}