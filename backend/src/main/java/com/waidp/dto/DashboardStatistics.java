package com.waidp.dto;

/**
 * Dashboard statistics DTO.
 */
public record DashboardStatistics(
        Long totalAssets,
        Long pendingAssets,
        Long auctioningAssets,
        Long disposedAssets,
        Long totalAuctions,
        Long inProgressAuctions,
        Long totalTransactions,
        java.math.BigDecimal totalAmount,
        double failRate,
        java.util.List<RecentDisposal> recentDisposals,
        java.util.List<TrendPoint> monthlyTrend,
        java.util.List<DepartmentDisposalStatistics> departmentCompare
) {
    public record RecentDisposal(
            String assetCode,
            String assetName,
            String winnerName,
            java.math.BigDecimal finalPrice,
            String disposalTime,
            String department
    ) {
    }

    public record TrendPoint(
            String label,
            java.math.BigDecimal amount
    ) {
    }
}
