package com.waidp.service;

import com.waidp.dto.BiddingStatistics;
import com.waidp.dto.DashboardStatistics;
import com.waidp.dto.DepartmentDisposalStatistics;
import com.waidp.dto.DisposalStatistics;

import java.util.List;
import java.util.Map;

/**
 * Statistics service.
 */
public interface StatisticsService {

    List<BiddingStatistics> getBiddingStatistics(Long userId, String startDate, String endDate, String period,
                                                 Long currentUserId, String currentRole);

    List<BiddingStatistics> getAllBiddingStatistics(String startDate, String endDate, String period, String role,
                                                    Long departmentId, Long currentUserId, String currentRole);

    DisposalStatistics getDisposalStatistics(String startDate, String endDate, String period,
                                             Long departmentId, Long currentUserId, String currentRole);

    List<DepartmentDisposalStatistics> getDepartmentDisposalStatistics(String startDate, String endDate, String period,
                                                                       Long departmentId, Long currentUserId, String currentRole);

    List<Map<String, Object>> getDisposalTrend(String period, Long currentUserId, String currentRole);

    List<DepartmentDisposalStatistics> getDepartmentDisposalComparison(String period, String point,
                                                                       Long departmentId, Long currentUserId, String currentRole);

    DashboardStatistics getDashboardStatistics(Long currentUserId, String currentRole);

    List<DashboardStatistics.TrendPoint> getDealTrend(String period, String month, Long currentUserId, String currentRole);
}
