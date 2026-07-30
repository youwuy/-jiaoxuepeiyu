package com.qizhifu.jiaoxuepeiyu.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import org.junit.jupiter.api.Test;

class BootstrapAdminServiceTests {

    @Test
    void skipsWhenBootstrapCredentialsAreNotConfigured() {
        FakeBootstrapAdminRepository repository = new FakeBootstrapAdminRepository();
        BootstrapAdminService service = new BootstrapAdminService(repository, new PrefixHasher());

        BootstrapAdminResult result = service.initialize(new BootstrapAdminCommand("", "", "", ""));

        assertEquals(BootstrapAdminResult.SKIPPED_NOT_CONFIGURED, result);
        assertEquals(0, repository.createdCount);
    }

    @Test
    void rejectsPartialBootstrapCredentials() {
        BootstrapAdminService service = new BootstrapAdminService(new FakeBootstrapAdminRepository(), new PrefixHasher());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.initialize(new BootstrapAdminCommand("admin", "", "Administrator", ""));
        });

        assertEquals("Bootstrap admin username and password must be configured together", exception.getMessage());
    }

    @Test
    void skipsWhenAdminAlreadyExists() {
        FakeBootstrapAdminRepository repository = new FakeBootstrapAdminRepository();
        repository.adminExists = true;
        BootstrapAdminService service = new BootstrapAdminService(repository, new PrefixHasher());

        BootstrapAdminResult result = service.initialize(new BootstrapAdminCommand("admin", "Admin12345", "Administrator", ""));

        assertEquals(BootstrapAdminResult.SKIPPED_ADMIN_EXISTS, result);
        assertEquals(0, repository.createdCount);
    }

    @Test
    void rejectsWeakBootstrapPassword() {
        BootstrapAdminService service = new BootstrapAdminService(new FakeBootstrapAdminRepository(), new PrefixHasher());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.initialize(new BootstrapAdminCommand("admin", "12345678", "Administrator", ""));
        });

        assertEquals("Bootstrap admin password must contain letters and digits", exception.getMessage());
    }

    @Test
    void rejectsBootstrapUsernameCollisionBeforeCreate() {
        FakeBootstrapAdminRepository repository = new FakeBootstrapAdminRepository();
        repository.usernameExists = true;
        BootstrapAdminService service = new BootstrapAdminService(repository, new PrefixHasher());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.initialize(new BootstrapAdminCommand("admin", "Admin12345", "Administrator", ""));
        });

        assertEquals("Bootstrap admin username already exists", exception.getMessage());
        assertEquals(0, repository.createdCount);
    }

    @Test
    void createsAdminWithHashedPassword() {
        FakeBootstrapAdminRepository repository = new FakeBootstrapAdminRepository();
        BootstrapAdminService service = new BootstrapAdminService(repository, new PrefixHasher());

        BootstrapAdminResult result = service.initialize(
                new BootstrapAdminCommand(" admin ", "Admin12345", " Administrator ", " 13812345678 "));

        assertEquals(BootstrapAdminResult.CREATED, result);
        assertEquals(1, repository.createdCount);
        assertEquals("admin", repository.username);
        assertEquals("Administrator", repository.realName);
        assertEquals("13812345678", repository.phone);
        assertEquals("hashed:Admin12345", repository.passwordHash);
    }

    private static class FakeBootstrapAdminRepository implements BootstrapAdminRepository {
        private boolean adminExists;
        private boolean usernameExists;
        private int createdCount;
        private String username;
        private String realName;
        private String phone;
        private String passwordHash;

        @Override
        public boolean hasAnyAdmin() {
            return adminExists;
        }

        @Override
        public boolean usernameExists(String username) {
            return usernameExists;
        }

        @Override
        public void createAdmin(String username, String realName, String phone, String passwordHash) {
            this.createdCount++;
            this.username = username;
            this.realName = realName;
            this.phone = phone;
            this.passwordHash = passwordHash;
        }
    }

    private static class PrefixHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return ("hashed:" + rawPassword).equals(passwordHash);
        }
    }
}
