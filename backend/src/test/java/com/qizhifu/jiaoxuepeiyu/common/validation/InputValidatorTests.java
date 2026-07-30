package com.qizhifu.jiaoxuepeiyu.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InputValidatorTests {

    @Test
    void validPhoneRequiresElevenDigits() {
        assertTrue(InputValidator.isPhone("13812345678"));
        assertFalse(InputValidator.isPhone("1381234567"));
        assertFalse(InputValidator.isPhone("138123456789"));
        assertFalse(InputValidator.isPhone("1381234abcd"));
    }

    @Test
    void validIdCardRequiresEighteenCharacters() {
        assertTrue(InputValidator.isIdCard("320100199001011234"));
        assertTrue(InputValidator.isIdCard("32010019900101123X"));
        assertFalse(InputValidator.isIdCard("32010019900101123"));
        assertFalse(InputValidator.isIdCard("3201001990010112Q4"));
    }

    @Test
    void blankTextIsRejectedAfterTrim() {
        assertTrue(InputValidator.hasText("teacher"));
        assertFalse(InputValidator.hasText(""));
        assertFalse(InputValidator.hasText("   "));
        assertFalse(InputValidator.hasText(null));
    }
}
