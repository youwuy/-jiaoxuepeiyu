package com.qizhifu.jiaoxuepeiyu.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class PasswordPolicyTests {

    @Test
    void acceptsEightToTwentyCharactersWithLettersAndDigits() {
        PasswordPolicy.Result result = PasswordPolicy.validateChange("oldPass123", "newPass123", "newPass123");

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void rejectsBlankPasswordFields() {
        PasswordPolicy.Result result = PasswordPolicy.validateChange("", "", "");

        List<String> errors = result.getErrors();
        assertEquals(3, errors.size());
        assertTrue(errors.contains("Current password is required"));
        assertTrue(errors.contains("New password is required"));
        assertTrue(errors.contains("Confirm password is required"));
    }

    @Test
    void rejectsInvalidLengthAndComposition() {
        PasswordPolicy.Result shortResult = PasswordPolicy.validateChange("oldPass123", "abc12", "abc12");
        PasswordPolicy.Result noDigitResult = PasswordPolicy.validateChange("oldPass123", "abcdefgh", "abcdefgh");
        PasswordPolicy.Result noLetterResult = PasswordPolicy.validateChange("oldPass123", "12345678", "12345678");

        assertTrue(shortResult.getErrors().contains("Password length must be 8-20 characters"));
        assertTrue(noDigitResult.getErrors().contains("Password must contain letters and digits"));
        assertTrue(noLetterResult.getErrors().contains("Password must contain letters and digits"));
    }

    @Test
    void rejectsMismatchAndSameAsCurrent() {
        PasswordPolicy.Result mismatch = PasswordPolicy.validateChange("oldPass123", "newPass123", "newPass124");
        PasswordPolicy.Result same = PasswordPolicy.validateChange("oldPass123", "oldPass123", "oldPass123");

        assertTrue(mismatch.getErrors().contains("Confirm password does not match"));
        assertTrue(same.getErrors().contains("New password cannot equal current password"));
    }
}
