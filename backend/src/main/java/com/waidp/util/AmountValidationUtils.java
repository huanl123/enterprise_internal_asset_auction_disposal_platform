package com.waidp.util;

import java.math.BigDecimal;

/**
 * 金额验证工具，用于验证最多保留2位小数的正数值。
 */
public final class AmountValidationUtils {

    private AmountValidationUtils() {
    }

    /**
     * 验证金额必须大于0且最多保留2位小数，不满足条件则抛出异常
     *
     * @param value 待验证的金额值
     * @param fieldName 字段名称，用于异常提示信息
     * @return 验证通过的原始值
     * @throws IllegalArgumentException 当值为null、小于等于0或小数位超过2位时抛出
     */
    public static BigDecimal requirePositiveAmount(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0 || !hasTwoDecimalPlacesOrLess(value)) {
            throw new IllegalArgumentException(fieldName + "必须大于0且最多保留2位小数");
        }
        return value;
    }

    /**
     * 检查BigDecimal值的小数位数是否最多2位
     *
     * @param value 待检查的金额值
     * @return true表示小数位≤2位，false表示为null或小数位>2位
     */
    public static boolean hasTwoDecimalPlacesOrLess(BigDecimal value) {
        if (value == null) {
            return false;
        }
        // stripTrailingZeros() 去除末尾的0，scale() 获取小数位数
        return value.stripTrailingZeros().scale() <= 2;
    }
}
