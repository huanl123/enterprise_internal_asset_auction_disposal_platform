package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.dto.BiddingStatistics;
import com.waidp.dto.DashboardStatistics;
import com.waidp.dto.DepartmentDisposalStatistics;
import com.waidp.dto.DisposalStatistics;
import com.waidp.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Statistics controller.
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/bids")
    public Result<List<BiddingStatistics>> getBiddingStatistics(
            @RequestParam Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("role");
        return Result.success(statisticsService.getBiddingStatistics(
                userId, startDate, endDate, period, currentUserId, currentRole
        ));
    }

    @GetMapping("/bids/summary")
    public Result<List<BiddingStatistics>> getAllBiddingStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long departmentId,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("role");
        return Result.success(statisticsService.getAllBiddingStatistics(
                startDate, endDate, period, role, departmentId, currentUserId, currentRole
        ));
    }

    @GetMapping("/disposal")
    public Result<DisposalStatistics> getDisposalStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Long departmentId,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("role");
        return Result.success(statisticsService.getDisposalStatistics(
                startDate, endDate, period, departmentId, currentUserId, currentRole
        ));
    }

    @GetMapping("/disposal/department")
    public Result<List<DepartmentDisposalStatistics>> getDepartmentDisposalStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Long departmentId,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("role");
        return Result.success(statisticsService.getDepartmentDisposalStatistics(
                startDate, endDate, period, departmentId, currentUserId, currentRole
        ));
    }

    @GetMapping("/dashboard")
    public Result<DashboardStatistics> getDashboardStatistics(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("role");
        return Result.success(statisticsService.getDashboardStatistics(currentUserId, currentRole));
    }

    @GetMapping("/trend")
    public Result<List<DashboardStatistics.TrendPoint>> getDealTrend(
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false) String month,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("role");
        return Result.success(statisticsService.getDealTrend(period, month, currentUserId, currentRole));
    }
}
