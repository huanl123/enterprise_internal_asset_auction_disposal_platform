package com.waidp.repository;

import com.waidp.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拍卖 Repository
 */
@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {

    /**
     * 根据资产ID查询拍卖
     */
    List<Auction> findByAssetId(Long assetId);

    /**
     * 查询进行中的拍卖
     */
    List<Auction> findByStatusAndEndTimeAfter(String status, LocalDateTime time);

    /**
     * 根据状态查询拍卖
     */
    Page<Auction> findByStatus(String status, Pageable pageable);
    
    /**
     * 根据状态查询拍卖（不分页）
     */
    List<Auction> findByStatus(String status);

    /**
     * 查询我参与的拍卖
     */
    @Query("SELECT a FROM Auction a JOIN Bid b ON a.id = b.auctionId " +
           "WHERE b.bidderId = :userId AND a.status != 'not_started' " +
           "GROUP BY a.id ORDER BY a.createTime DESC")
    List<Auction> findMyAuctions(@Param("userId") Long userId);

    /**
     * 搜索拍卖
     */
    @Query("SELECT a FROM Auction a WHERE " +
           "((:name IS NULL OR :name = '') OR a.name LIKE %:name%) AND " +
           "((:status IS NULL OR :status = '') OR a.status = :status)")
    Page<Auction> searchAuctions(
            @Param("name") String name,
            @Param("status") String status,
            Pageable pageable
    );
}
