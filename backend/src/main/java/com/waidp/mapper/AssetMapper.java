package com.waidp.mapper;

import com.waidp.dto.AssetDTO;
import com.waidp.entity.Asset;
import org.springframework.stereotype.Component;

/**
 * 资产实体与DTO之间的映射器
 */
@Component
public class AssetMapper {
    
    /**
     * 将实体转换为DTO
     */
    public static AssetDTO toDTO(Asset asset) {
        if (asset == null) {
            return null;
        }
        
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setCode(asset.getCode());
        dto.setName(asset.getName());
        dto.setCategory(asset.getCategory());
        dto.setSpecification(asset.getSpecification());
        dto.setImages(asset.getImages());
        
        dto.setDepartmentId(asset.getDepartmentId());
        // 避免懒加载问题，直接检查department是否为null
        if (asset.getDepartment() != null) {
            dto.setDepartmentName(asset.getDepartment().getName());
        }
        
        dto.setPurchaseDate(asset.getPurchaseDate());
        dto.setOriginalValue(asset.getOriginalValue());
        dto.setCurrentValue(asset.getCurrentValue());
        dto.setStartPrice(asset.getStartPrice());
        dto.setReservePrice(asset.getReservePrice());
        dto.setHasReservePrice(asset.getHasReservePrice());
        
        dto.setDepreciationRuleId(asset.getDepreciationRuleId());
        // 避免懒加载问题，直接检查depreciationRule是否为null
        if (asset.getDepreciationRule() != null) {
            dto.setDepreciationRuleName(asset.getDepreciationRule().getName());
        }
        
        dto.setStatus(asset.getStatus());
        dto.setCreateTime(asset.getCreateTime());
        dto.setUpdateTime(asset.getUpdateTime());
        
        return dto;
    }
}