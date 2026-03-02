package com.waidp.scheduler;

import com.waidp.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 拍卖定时任务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionService auctionService;

    /**
     * 每分钟检查一次拍卖是否结束
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void checkAuctions() {
        try {
            auctionService.checkAndEndAuctions();
            log.debug("拍卖状态检查完成");
        } catch (Exception e) {
            log.error("检查拍卖状态时发生错误", e);
        }
    }

    /**
     * 每天凌晨2点清理过期的待确认交易
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiredTransactions() {
        try {
            auctionService.checkExpiredConfirmations();
            log.info("过期交易检查完成");
        } catch (Exception e) {
            log.error("检查过期交易时发生错误", e);
        }
    }
}
