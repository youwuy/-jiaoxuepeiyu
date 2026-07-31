package com.qizhifu.jiaoxuepeiyu.bootstrap;

import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapAdminService {

    private final BootstrapAdminRepository repository;
    private final PasswordHasher passwordHasher;

    public BootstrapAdminService(BootstrapAdminRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public BootstrapAdminResult initialize(BootstrapAdminCommand command) {
        String username = trimToNull(command == null ? null : command.getUsername());
        String password = trimToNull(command == null ? null : command.getPassword());
        if (username == null && password == null) {
            return BootstrapAdminResult.SKIPPED_NOT_CONFIGURED;
        }
        if (username == null || password == null) {
            throw new IllegalStateException("Bootstrap admin username and password must be configured together");
        }
        if (repository.hasAnyAdmin()) {
            return BootstrapAdminResult.SKIPPED_ADMIN_EXISTS;
        }
        validateUsername(username);
        validatePassword(password);
        if (repository.usernameExists(username)) {
            throw new IllegalStateException("Bootstrap admin username already exists");
        }
        String realName = trimToNull(command.getRealName());
        String phone = trimToNull(command.getPhone());
        repository.createAdmin(username,
                realName == null ? "System Administrator" : realName,
                phone,
                passwordHasher.hash(password));
        return BootstrapAdminResult.CREATED;
    }

    private void validateUsername(String username) {
        if (username.length() > 64) {
            throw new IllegalStateException("Bootstrap admin username cannot exceed 64 characters");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8 || password.length() > 20) {
            throw new IllegalStateException("Bootstrap admin password length must be 8-20 characters");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            hasLetter = hasLetter || Character.isLetter(c);
            hasDigit = hasDigit || Character.isDigit(c);
        }
        if (!hasLetter || !hasDigit) {
            throw new IllegalStateException("Bootstrap admin password must contain letters and digits");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
