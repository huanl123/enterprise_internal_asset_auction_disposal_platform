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

    /**
     * 拍卖状态定时检查任务
     * 执行频率: 每分钟执行一次
     * 功能: 更新拍卖活动状态，自动结束已到期的拍卖
     */
    @Scheduled(fixedRate = 60_000)
    public void checkAuctions() {
        try {
            auctionService.checkAndEndAuctions();
            log.debug("拍卖状态检查完成");
        } catch (Exception e) {
            log.error("检查拍卖状态时发生错误", e);
        }
    }

    /**
     * 过期交易定时检查任务
     * 执行时间: 每天凌晨2点执行
     * 功能:
     *   1. 检查并处理超时未确认成交的交易
     *   2. 检查并处理超时未付款的交易
     */
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
