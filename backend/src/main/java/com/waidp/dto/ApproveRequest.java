package com.waidp.dto;

import java.math.BigDecimal;

/**
 * 审核请求 DTO
 */
public record ApproveRequest(
        BigDecimal startPrice,
        boolean hasReservePrice,
        BigDecimal reservePrice,
        String remark
) {
}
