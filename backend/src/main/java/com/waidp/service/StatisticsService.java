package com.waidp.service;

import com.waidp.dto.*;

import java.util.List;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取员工竞拍记录
     */
    List<BiddingStatistics> getBiddingStatistics(Long userId, String startDate, String endDate, String period);

    /**
     * 获取所有员工的竞拍记录汇总
     */
    List<BiddingStatistics> getAllBiddingStatistics(String startDate, String endDate, String period, String role);

    /**
     * 获取资产处置统计
     */
    DisposalStatistics getDisposalStatistics(String startDate, String endDate, String period);

    /**
     * 获取部门资产处置统计
     */
    List<DepartmentDisposalStatistics> getDepartmentDisposalStatistics(String startDate, String endDate, String period);

    /**
     * 获取仪表盘统计数据
     */
    DashboardStatistics getDashboardStatistics();

    /**
     * 获取近6个月成交趋势
     */
    List<DashboardStatistics.TrendPoint> getDealTrend(String period, String month);
}
