package com.waidp.repository;

import com.waidp.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 绔炰环 Repository
 */
@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * 鏌ヨ鎷嶅崠鐨勬墍鏈夌珵浠疯褰曪紙鎸夋椂闂撮檷搴忥級
     */
    List<Bid> findByAuctionIdOrderByBidTimeDesc(Long auctionId);

    /**
     * 鏌ヨ鎷嶅崠鐨勬墍鏈夌珵浠疯褰曪紙鎸夋椂闂村崌搴忥級
     */
    List<Bid> findByAuctionIdOrderByBidTimeAsc(Long auctionId);

    /**
     * 鏌ヨ鐢ㄦ埛鐨勬渶楂樺嚭浠?
     */
    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auctionId = :auctionId AND b.bidderId = :userId")
    java.math.BigDecimal findMaxBidByUserAndAuction(
            @Param("auctionId") Long auctionId,
            @Param("userId") Long userId
    );

    /**
     * 鏌ヨ鎷嶅崠鐨勬渶楂樺嚭浠?
     */
    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auctionId = :auctionId")
    java.math.BigDecimal findMaxBidByAuction(@Param("auctionId") Long auctionId);

    /**
     * 鎸夋椂闂磋寖鍥存煡璇㈢敤鎴风珵浠疯褰?
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
     * 鏌ヨ鐢ㄦ埛鍦ㄦ煇鎷嶅崠鐨勬渶鏂板嚭浠?
     */
    Bid findFirstByAuctionIdAndBidderIdOrderByBidTimeDesc(Long auctionId, Long bidderId);

    /**
     * 鑾峰彇鎷嶅崠褰撳墠鏈€楂樺嚭浠€?
     */
    Bid findFirstByAuctionIdAndIsHighestTrue(Long auctionId);

    /**
     * 鑾峰彇鎷嶅崠鏈€楂樺嚭浠凤紙浠锋牸涓哄ぇ锛屽鏋滀环鏍肩浉鍚屽垯鎸夋椂闂撮檷搴忥級
     */
    Bid findFirstByAuctionIdOrderByPriceDescBidTimeDesc(Long auctionId);

    /**
     * 缁熻鎷嶅崠鏈夋晥绔炰环璁板綍鏁?
     */
    long countByAuctionId(Long auctionId);

    /**
     * 娓呴櫎鎷嶅崠鐨勬渶楂樺嚭浠锋爣璁?
     */
    @Modifying
    @Query("UPDATE Bid b SET b.isHighest = false WHERE b.auctionId = :auctionId")
    void clearHighestByAuctionId(@Param("auctionId") Long auctionId);
}
