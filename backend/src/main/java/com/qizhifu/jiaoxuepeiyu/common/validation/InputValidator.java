package com.qizhifu.jiaoxuepeiyu.common.validation;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");

    private InputValidator() {
    }

    public static boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    public static boolean isPhone(String value) {
        return value != null && PHONE_PATTERN.matcher(value).matches();
    }

    public static boolean isIdCard(String value) {
        return value != null && ID_CARD_PATTERN.matcher(value).matches();
    }
}
