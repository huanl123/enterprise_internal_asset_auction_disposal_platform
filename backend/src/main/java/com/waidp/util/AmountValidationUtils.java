package com.waidp.util;

import java.math.BigDecimal;

/**
 * Amount validation utility for positive values with up to 2 decimals.
 */
public final class AmountValidationUtils {

    private AmountValidationUtils() {
    }

    public static BigDecimal requirePositiveAmount(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0 || !hasTwoDecimalPlacesOrLess(value)) {
            throw new IllegalArgumentException(fieldName + "必须大于0且最多保留2位小数");
        }
        return value;
    }

    public static boolean hasTwoDecimalPlacesOrLess(BigDecimal value) {
        if (value == null) {
            return false;
        }
        return value.stripTrailingZeros().scale() <= 2;
    }
}
