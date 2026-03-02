package com.waidp.mapper;

import com.waidp.dto.AuctionDTO;
import com.waidp.entity.Auction;
import org.springframework.stereotype.Component;

/**
 * 拍卖实体与DTO之间的映射器
 */
@Component
public class AuctionMapper {
    
    /**
     * 将实体转换为DTO
     */
    public static AuctionDTO toDTO(Auction auction) {
        if (auction == null) {
            return null;
        }
        
        AuctionDTO dto = new AuctionDTO();
        dto.setId(auction.getId());
        dto.setName(auction.getName());
        dto.setCategory(auction.getCategory());
        
        // 资产信息
        dto.setAssetId(auction.getAssetId());
        if (auction.getAsset() != null) {
            dto.setAssetName(auction.getAsset().getName());
            dto.setAssetCode(auction.getAsset().getCode());
            dto.setAssetSpecification(auction.getAsset().getSpecification());
        }
        
        // 价格信息
        dto.setStartPrice(auction.getStartPrice());
        dto.setCurrentPrice(auction.getCurrentPrice());
        dto.setIncrementAmount(auction.getIncrementAmount());
        dto.setHasReservePrice(auction.getHasReservePrice());
        dto.setReservePrice(auction.getReservePrice());
        
        // 时间信息
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());
        dto.setCreateTime(auction.getCreateTime());
        dto.setUpdateTime(auction.getUpdateTime());
        
        // 状态信息
        dto.setStatus(auction.getStatus());
        dto.setBidCount(auction.getBidCount());
        
        // 其他信息
        dto.setDescription(auction.getDescription());
        dto.setDepartmentIds(auction.getDepartmentIds());
        
        // 竞拍信息
        dto.setWinnerId(auction.getWinnerId());
        if (auction.getWinner() != null) {
            dto.setWinnerName(auction.getWinner().getName());
        }
        dto.setFinalPrice(auction.getFinalPrice());
        
        return dto;
    }
}