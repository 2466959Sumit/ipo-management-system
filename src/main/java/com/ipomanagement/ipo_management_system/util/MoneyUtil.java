package com.ipomanagement.ipo_management_system.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyUtil {
    private MoneyUtil() {}

    public static BigDecimal money(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}