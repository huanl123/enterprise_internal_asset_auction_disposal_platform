package com.waidp.service;

import com.waidp.dto.AuctionDTO;
import com.waidp.entity.Auction;
import com.waidp.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拍卖服务接口
 */
public interface AuctionService {

    /**
     * 分页查询拍卖
     */
    Page<Auction> getAuctions(String name, String status, Pageable pageable);

    /**
     * 分页查询拍卖 - 返回DTO，避免懒加载问题
     */
    Page<AuctionDTO> getAuctionsDTO(String name, String status, Pageable pageable);

    /**
     * 根据ID获取拍卖
     */
    Auction getAuctionById(Long id);

    /**
     * 根据ID获取拍卖 - 返回DTO，避免懒加载问题
     */
    AuctionDTO getAuctionDTOById(Long id);

    /**
     * 创建拍卖活动
     */
    void createAuction(Auction auction);

    /**
     * 删除拍卖
     */
    void deleteAuction(Long auctionId);

    /**
     * 参与竞价
     */
    void bid(Long auctionId, Long userId, BigDecimal price);

    /**
     * 一键出价
     */
    void quickBid(Long auctionId, Long userId);

    /**
     * 确认成交
     */
    void confirmTransaction(Long auctionId, Long userId);

    /**
     * 查询我的拍卖
     */
    List<Auction> getMyAuctions(Long userId);

    /**
     * 查询我的拍卖 - 返回DTO
     */
    List<AuctionDTO> getMyAuctionsDTO(Long userId, String status);

    /**
     * 获取拍卖的竞价记录
     */
    List<Bid> getBidsByAuctionId(Long auctionId);

    /**
     * 自动检查和结束拍卖
     */
    void checkAndEndAuctions();

    /**
     * 检查并处理超时未确认的成交
     */
    void checkExpiredConfirmations();
}
