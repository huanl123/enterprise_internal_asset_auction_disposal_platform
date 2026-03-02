package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.dto.*;
import com.waidp.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计查询控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取员工竞拍记录
     */
    @GetMapping("/bids")
    public Result<List<BiddingStatistics>> getBiddingStatistics(
            @RequestParam Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period) {
        return Result.success(statisticsService.getBiddingStatistics(userId, startDate, endDate, period));
    }

    /**
     * 获取所有员工的竞拍记录汇总
     */
    @GetMapping("/bids/summary")
    public Result<List<BiddingStatistics>> getAllBiddingStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String role) {
        return Result.success(statisticsService.getAllBiddingStatistics(startDate, endDate, period, role));
    }

    /**
     * 获取资产处置统计
     */
    @GetMapping("/disposal")
    public Result<DisposalStatistics> getDisposalStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period) {
        return Result.success(statisticsService.getDisposalStatistics(startDate, endDate, period));
    }

    /**
     * 获取部门资产处置统计
     */
    @GetMapping("/disposal/department")
    public Result<List<DepartmentDisposalStatistics>> getDepartmentDisposalStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period) {
        return Result.success(statisticsService.getDepartmentDisposalStatistics(startDate, endDate, period));
    }

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardStatistics> getDashboardStatistics() {
        return Result.success(statisticsService.getDashboardStatistics());
    }

    /**
     * 获取近6个月成交趋势
     */
    @GetMapping("/trend")
    public Result<List<DashboardStatistics.TrendPoint>> getDealTrend(
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false) String month) {
        return Result.success(statisticsService.getDealTrend(period, month));
    }
}
