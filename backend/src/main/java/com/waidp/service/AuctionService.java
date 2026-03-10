package com.waidp.service;

import com.waidp.dto.AuctionDTO;
import com.waidp.entity.Auction;
import com.waidp.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 鎷嶅崠鏈嶅姟鎺ュ彛
 */
public interface AuctionService {

    /**
     * 鍒嗛〉鏌ヨ鎷嶅崠
     */
    Page<Auction> getAuctions(String name, String status, Pageable pageable);

    /**
     * 鍒嗛〉鏌ヨ鎷嶅崠 - 杩斿洖DTO锛岄伩鍏嶆噿鍔犺浇闂
     */
    Page<AuctionDTO> getAuctionsDTO(String name, String status, Pageable pageable);

    /**
     * 鏍规嵁ID鑾峰彇鎷嶅崠
     */
    Auction getAuctionById(Long id);

    /**
     * 鏍规嵁ID鑾峰彇鎷嶅崠 - 杩斿洖DTO锛岄伩鍏嶆噿鍔犺浇闂
     */
    AuctionDTO getAuctionDTOById(Long id);

    /**
     * 鍒涘缓鎷嶅崠娲诲姩
     */
    void createAuction(Auction auction);

    /**
     * 鍒犻櫎鎷嶅崠
     */
    void deleteAuction(Long auctionId);

    /**
     * 鍙備笌绔炰环
     */
    void bid(Long auctionId, Long userId, BigDecimal price);

    /**
     * 涓€閿嚭浠?
     */
    void quickBid(Long auctionId, Long userId);

    /**
     * 鎾ゅ洖褰撳墠鏈€楂樺嚭浠凤紙缁撴潫鍓?12 灏忔椂澶栵級
     */
    void withdrawHighestBid(Long auctionId, Long userId);

    /**
     * 纭鎴愪氦
     */
    void confirmTransaction(Long auctionId, Long userId);

    /**
     * 鏌ヨ鎴戠殑鎷嶅崠
     */
    List<Auction> getMyAuctions(Long userId);

    /**
     * 鏌ヨ鎴戠殑鎷嶅崠 - 杩斿洖DTO
     */
    List<AuctionDTO> getMyAuctionsDTO(Long userId, String status, String confirmStatus);

    /**
     * 鑾峰彇鎷嶅崠鐨勭珵浠疯褰?
     */
    List<Bid> getBidsByAuctionId(Long auctionId);

    /**
     * 鑷姩妫€鏌ュ拰缁撴潫鎷嶅崠
     */
    void checkAndEndAuctions();

    /**
     * 妫€鏌ュ苟澶勭悊瓒呮椂鏈‘璁ょ殑鎴愪氦
     */
    void checkExpiredConfirmations();

    /**
     * 妫€鏌ュ苟澶勭悊付款超时（未确认收款则过期）
     */
    void checkExpiredPayments();
}
