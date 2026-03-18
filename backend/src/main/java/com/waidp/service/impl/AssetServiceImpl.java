package com.waidp.service.impl;

import com.waidp.dto.AssetDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.AssetHistory;
import com.waidp.entity.Department;
import com.waidp.entity.DepreciationRule;
import com.waidp.mapper.AssetMapper;
import com.waidp.repository.AssetRepository;
import com.waidp.repository.DepartmentRepository;
import com.waidp.repository.DepreciationRuleRepository;
import com.waidp.service.AssetService;
import com.waidp.service.AssetHistoryService;
import com.waidp.util.AmountValidationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 资产服务实现类
 * 实现资产管理的核心业务逻辑，包括：
 * 资产创建、查询、更新、删除、资产价值计算（根据折旧规则自动计算现值）、
 * 资产状态流转、资产历史记录、资产图片管理等。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final DepartmentRepository departmentRepository;
    private final DepreciationRuleRepository depreciationRuleRepository;
    private final AssetHistoryService assetHistoryService;
    
    private static final Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);

    @Override
    public Page<Asset> getAssets(String code, String name, String status, Long departmentId, Pageable pageable) {
        return assetRepository.searchAssets(
                code,
                name,
                status,
                departmentId,
                pageable
        );
    }

    @Override
    public Page<AssetDTO> getAssetsDTO(String code, String name, String status, Long departmentId, Pageable pageable) {
        Page<Asset> assetPage = assetRepository.searchAssets(
                code,
                name,
                status,
                departmentId,
                pageable
        );
        
        // 转换为DTO列表
        List<AssetDTO> assetDTOs = assetPage.getContent().stream()
                .map(AssetMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
        
        // 创建新的Page对象
        return new PageImpl<>(assetDTOs, pageable, assetPage.getTotalElements());
    }

    @Override
    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));
    }

    @Override
    public AssetDTO getAssetDTOById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        return AssetMapper.toDTO(asset);
    }

    @Override
    public List<AssetHistory> getAssetHistory(Long assetId) {
        return assetHistoryService.getByAssetId(assetId);
    }

    @Override
    public void createAsset(Asset asset, Long operatorId) {
        if (asset == null) {
            throw new IllegalArgumentException("资产信息不能为空");
        }
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("资产名称不能为空");
        }
        if (asset.getPurchaseDate() == null) {
            throw new IllegalArgumentException("购置日期不能为空");
        }
        if (asset.getDepreciationRuleId() == null) {
            throw new IllegalArgumentException("请选择折旧规则");
        }
        AmountValidationUtils.requirePositiveAmount(asset.getOriginalValue(), "资产原值");

        logger.info("开始创建资产，资产名称: {}", asset.getName());
        
        // 强制验证部门ID不能为空
        if (asset.getDepartmentId() == null) {
            throw new RuntimeException("使用部门不能为空，请选择使用部门");
        }
        
        // 验证部门是否存在，并绑定部门
        Department department = departmentRepository.findById(asset.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("选择的部门不存在"));
        asset.setDepartment(department);

        // 绑定折旧规则（用于持久化折旧规则关联）
        DepreciationRule rule = depreciationRuleRepository.findById(asset.getDepreciationRuleId())
                .orElseThrow(() -> new RuntimeException("选择的折旧规则不存在"));
        asset.setDepreciationRule(rule);
        
        // 如果资产编号为空，则自动生成
        if (asset.getCode() == null || asset.getCode().trim().isEmpty()) {
            asset.setCode(generateAssetCode());
        }
        
        // 确保currentValue不为null，使用originalValue作为默认值
        if (asset.getCurrentValue() == null) {
            if (asset.getOriginalValue() != null) {
                asset.setCurrentValue(asset.getOriginalValue());
                logger.debug("currentValue为null，使用originalValue作为默认值: {}", asset.getOriginalValue());
            } else {
                asset.setCurrentValue(BigDecimal.ZERO);
                logger.debug("currentValue和originalValue都为null，设置为0");
            }
        }
        
        // 确保startPrice不为null
        if (asset.getStartPrice() == null) {
            if (asset.getCurrentValue() != null) {
                asset.setStartPrice(asset.getCurrentValue().multiply(new BigDecimal("0.8")).setScale(2, RoundingMode.HALF_UP));
                logger.debug("startPrice为null，基于currentValue计算: {}", asset.getStartPrice());
            } else {
                asset.setStartPrice(BigDecimal.ZERO);
                logger.debug("startPrice和currentValue都为null，设置为0");
            }
        }
        
        logger.debug("资产创建前验证 - currentValue: {}, startPrice: {}", 
                    asset.getCurrentValue(), asset.getStartPrice());

        AmountValidationUtils.requirePositiveAmount(asset.getCurrentValue(), "当前价值");
        AmountValidationUtils.requirePositiveAmount(asset.getStartPrice(), "起拍价");

        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.save(asset);
        
        logger.info("资产创建成功，ID: {}", asset.getId());
        
        // 添加创建记录
        assetHistoryService.addHistory(asset.getId(), "创建", "资产创建", operatorId);
    }

    @Override
    public void updateAsset(Asset asset) {
        Asset existingAsset = getAssetById(asset.getId());

        existingAsset.setName(asset.getName());
        existingAsset.setSpecification(asset.getSpecification());
        existingAsset.setImages(asset.getImages());
        existingAsset.setUpdateTime(LocalDateTime.now());

        assetRepository.save(existingAsset);
        
        // 添加更新记录
        assetHistoryService.addHistory(existingAsset.getId(), "更新", "资产信息更新", null);
    }

    @Override
    public void deleteAsset(Long id) {
        getAssetById(id);
        assetRepository.deleteById(id);
        
        // 添加删除记录
        assetHistoryService.addHistory(id, "删除", "资产被删除", null);
    }

    @Override
    public void recalculateValue(Long id) {
        Asset asset = getAssetById(id);
        // 根据折旧规则重新计算当前价值
        if (asset.getDepreciationRule() != null && asset.getPurchaseDate() != null) {
            LocalDate purchaseDate = asset.getPurchaseDate();
            LocalDate now = LocalDate.now();
            
            long days = now.toEpochDay() - purchaseDate.toEpochDay();
            double years = days / 365.25;
            
            DepreciationRule rule = asset.getDepreciationRule();
            double salvageRate = rule.getSalvageRate() / 100.0;
            int usefulLife = rule.getUsefulLife();
            
            BigDecimal originalValue = asset.getOriginalValue();
            BigDecimal currentValue;
            
            if (years <= usefulLife) {
                currentValue = originalValue.multiply(BigDecimal.valueOf(1 - (1 - salvageRate) * (years / usefulLife)));
            } else {
                currentValue = originalValue.multiply(BigDecimal.valueOf(salvageRate));
            }
            
            asset.setCurrentValue(currentValue.max(BigDecimal.ZERO));
        }
        
        asset.setStatus("待审核");
        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 添加重新计算记录
        assetHistoryService.addHistory(id, "重新计算", "资产价值重新计算", null);
    }
    
    @Override
    public String generateAssetCode() {
        // 生成格式为: ASSET-YYYYMMDD-NNNN 的资产编号
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询当天最大的资产编号
        String maxCode = assetRepository.findMaxCodeByDate(datePart);
        
        int nextSerialNumber;
        if (maxCode == null || maxCode.isEmpty()) {
            // 如果没有当天的资产，从1001开始
            nextSerialNumber = 1001;
        } else {
            // 从最大编号的序列号递增
            try {
                String[] parts = maxCode.split("-");
                if (parts.length >= 3) {
                    String serialPart = parts[2];
                    nextSerialNumber = Integer.parseInt(serialPart) + 1;
                } else {
                    nextSerialNumber = 1001;
                }
            } catch (NumberFormatException e) {
                nextSerialNumber = 1001;
            }
        }
        
        // 确保序列号在合理范围内
        if (nextSerialNumber < 1000) {
            nextSerialNumber = 1000;
        }
        
        return "ASSET-" + datePart + "-" + String.format("%04d", nextSerialNumber);
    }
    
    /**
     * 更新资产图片
     */
    public void updateAssetImages(Long assetId, String imageUrls) {
        Asset asset = getAssetById(assetId);
        asset.setImages(imageUrls);
        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 添加图片更新记录
        assetHistoryService.addHistory(assetId, "更新图片", "资产图片更新", null);
    }
}
