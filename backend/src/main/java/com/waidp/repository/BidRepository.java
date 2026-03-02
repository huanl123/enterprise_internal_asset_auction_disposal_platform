package com.waidp.repository;

import com.waidp.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 竞价 Repository
 */
@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * 查询拍卖的所有竞价记录（按时间降序）
     */
    List<Bid> findByAuctionIdOrderByBidTimeDesc(Long auctionId);

    /**
     * 查询拍卖的所有竞价记录（按时间升序）
     */
    List<Bid> findByAuctionIdOrderByBidTimeAsc(Long auctionId);

    /**
     * 查询用户的最高出价
     */
    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auctionId = :auctionId AND b.bidderId = :userId")
    java.math.BigDecimal findMaxBidByUserAndAuction(
            @Param("auctionId") Long auctionId,
            @Param("userId") Long userId
    );

    /**
     * 查询拍卖的最高出价
     */
    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auctionId = :auctionId")
    java.math.BigDecimal findMaxBidByAuction(@Param("auctionId") Long auctionId);

    /**
     * 按时间范围查询用户竞价记录
     */
    @Query("SELECT b FROM Bid b WHERE b.bidderId = :userId " +
            "AND (:startTime IS NULL OR b.bidTime >= :startTime) " +
            "AND (:endTime IS NULL OR b.bidTime <= :endTime)")
    List<Bid> findByBidderIdAndBidTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime
    );

    /**
     * 查询用户在某拍卖的最新出价
     */
    Bid findFirstByAuctionIdAndBidderIdOrderByBidTimeDesc(Long auctionId, Long bidderId);

    /**
     * 清除拍卖的最高出价标记
     */
    @Modifying
    @Query("UPDATE Bid b SET b.isHighest = false WHERE b.auctionId = :auctionId")
    void clearHighestByAuctionId(@Param("auctionId") Long auctionId);
}
