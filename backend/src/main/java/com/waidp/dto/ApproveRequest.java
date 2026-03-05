package com.waidp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 审核请求 DTO
 */
public record ApproveRequest(
        @NotNull(message = "起拍价不能为空")
        BigDecimal startPrice,
        boolean hasReservePrice,
        BigDecimal reservePrice,
        String remark
) {
}
