package com.oa.common.util;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String like(String value) {
        return "%" + value.trim() + "%";
    }
}
