package com.waidp.scheduler;

import com.waidp.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionService auctionService;

    // Every minute: update auction status and end auctions.
    @Scheduled(fixedRate = 60_000)
    public void checkAuctions() {
        try {
            auctionService.checkAndEndAuctions();
            log.debug("拍卖状态检查完成");
        } catch (Exception e) {
            log.error("检查拍卖状态时发生错误", e);
        }
    }

    // Every day at 02:00: expire confirmations past deadline.
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiredTransactions() {
        try {
            auctionService.checkExpiredConfirmations();
            auctionService.checkExpiredPayments();
            log.info("过期交易检查完成");
        } catch (Exception e) {
            log.error("检查过期交易时发生错误", e);
        }
    }
}
