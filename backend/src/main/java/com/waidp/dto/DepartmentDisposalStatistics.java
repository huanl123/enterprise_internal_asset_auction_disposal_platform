package com.waidp.dto;

/**
 * 部门资产处置统计 DTO
 */
public record DepartmentDisposalStatistics(
        Long departmentId,
        String departmentName,
        Long disposalCount,
        Long failedCount,
        java.math.BigDecimal totalAmount,
        double failureRate
) {
}
