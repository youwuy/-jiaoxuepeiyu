package com.qizhifu.jiaoxuepeiyu.common.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static Result validateChange(String currentPassword, String newPassword, String confirmPassword) {
        List<String> errors = new ArrayList<String>();

        if (!InputValidator.hasText(currentPassword)) {
            errors.add("Current password is required");
        }
        if (!InputValidator.hasText(newPassword)) {
            errors.add("New password is required");
        }
        if (!InputValidator.hasText(confirmPassword)) {
            errors.add("Confirm password is required");
        }

        if (InputValidator.hasText(newPassword)) {
            if (newPassword.length() < 8 || newPassword.length() > 20) {
                errors.add("Password length must be 8-20 characters");
            }
            if (!containsLetter(newPassword) || !containsDigit(newPassword)) {
                errors.add("Password must contain letters and digits");
            }
        }

        if (InputValidator.hasText(newPassword) && InputValidator.hasText(confirmPassword)
                && !newPassword.equals(confirmPassword)) {
            errors.add("Confirm password does not match");
        }

        if (InputValidator.hasText(currentPassword) && InputValidator.hasText(newPassword)
                && currentPassword.equals(newPassword)) {
            errors.add("New password cannot equal current password");
        }

        return new Result(errors);
    }

    private static boolean containsLetter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static class Result {
        private final List<String> errors;

        private Result(List<String> errors) {
            this.errors = Collections.unmodifiableList(new ArrayList<String>(errors));
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
