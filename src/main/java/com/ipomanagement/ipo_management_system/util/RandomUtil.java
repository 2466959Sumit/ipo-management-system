package com.ipomanagement.ipo_management_system.util;

import java.util.Random;

public class RandomUtil {
    private RandomUtil() {}

    public static Random seeded(long seed) {
        return new Random(seed);
    }
}