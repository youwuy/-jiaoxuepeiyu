package com.qizhifu.jiaoxuepeiyu.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginCommand;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginIdentityType;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginResult;
import com.qizhifu.jiaoxuepeiyu.auth.model.Portal;
import com.qizhifu.jiaoxuepeiyu.auth.model.UserAccount;
import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import com.qizhifu.jiaoxuepeiyu.auth.port.SessionRepository;
import com.qizhifu.jiaoxuepeiyu.auth.port.TokenGenerator;
import com.qizhifu.jiaoxuepeiyu.auth.port.UserAccountRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthServiceTests {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-30T08:00:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Test
    void rejectsDisabledAccount() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "teacher", 0);

        AuthService service = service(users, new FakeSessions(), new PlainHasher(), new FixedTokenGenerator());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> service.login(
                new LoginCommand(Portal.ADMIN, LoginIdentityType.USERNAME, "teacher001", "secret123", "127.0.0.1")));

        assertEquals("Account is disabled", exception.getMessage());
    }

    @Test
    void rejectsStudentAccountOnAdminPortal() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "student", 1);

        AuthService service = service(users, new FakeSessions(), new PlainHasher(), new FixedTokenGenerator());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> service.login(
                new LoginCommand(Portal.ADMIN, LoginIdentityType.USERNAME, "student001", "secret123", "127.0.0.1")));

        assertEquals("Account does not belong to this portal", exception.getMessage());
    }

    @Test
    void createsTwentyFourHourSessionWhenNoSessionIsActive() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "teacher", 1);
        FakeSessions sessions = new FakeSessions();

        AuthService service = service(users, sessions, new PlainHasher(), new FixedTokenGenerator());

        LoginResult result = service.login(
                new LoginCommand(Portal.ADMIN, LoginIdentityType.USERNAME, "teacher001", "secret123", "127.0.0.1"));

        assertEquals("token-1", result.getToken());
        assertEquals(1L, result.getUser().getId());
        assertEquals("token-1", sessions.createdToken);
        assertEquals(Instant.parse("2026-07-31T08:00:00Z"), sessions.expiresAt);
    }

    @Test
    void rejectsLoginWhenAnotherActiveSessionExists() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "teacher", 1);
        FakeSessions sessions = new FakeSessions();
        sessions.activeSession = true;

        AuthService service = service(users, sessions, new PlainHasher(), new FixedTokenGenerator());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> service.login(
                new LoginCommand(Portal.ADMIN, LoginIdentityType.USERNAME, "teacher001", "secret123", "127.0.0.1")));

        assertEquals("Account already has an active session", exception.getMessage());
        assertEquals(null, sessions.createdToken);
    }

    @Test
    void rejectsWrongPassword() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "teacher", 1);

        AuthService service = service(users, new FakeSessions(), new PlainHasher(), new FixedTokenGenerator());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> service.login(
                new LoginCommand(Portal.ADMIN, LoginIdentityType.USERNAME, "teacher001", "wrong123", "127.0.0.1")));

        assertEquals("Invalid account or password", exception.getMessage());
    }

    @Test
    void returnsCurrentUserForActiveToken() {
        FakeSessions sessions = new FakeSessions();
        sessions.activeUser = authenticatedUser(1L, "teacher");

        AuthService service = service(new FakeUsers(), sessions, new PlainHasher(), new FixedTokenGenerator());

        AuthenticatedUser user = service.currentUser("token-1");

        assertEquals(1L, user.getId().longValue());
        assertEquals("teacher", user.getUserType());
    }

    @Test
    void rejectsCurrentUserForExpiredToken() {
        AuthService service = service(new FakeUsers(), new FakeSessions(), new PlainHasher(), new FixedTokenGenerator());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> service.currentUser("expired"));

        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    void logoutInvalidatesToken() {
        FakeSessions sessions = new FakeSessions();
        AuthService service = service(new FakeUsers(), sessions, new PlainHasher(), new FixedTokenGenerator());

        service.logout("token-1");

        assertEquals("token-1", sessions.invalidatedToken);
    }

    @Test
    void changesPasswordAfterCurrentPasswordAndPolicyValidation() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "teacher", 1);
        FakeSessions sessions = new FakeSessions();
        sessions.activeUser = authenticatedUser(1L, "teacher");
        AuthService service = service(users, sessions, new PlainHasher(), new FixedTokenGenerator());

        service.changePassword("token-1", "secret123", "newSecret123", "newSecret123");

        assertEquals(1L, users.updatedPasswordUserId.longValue());
        assertEquals("newSecret123", users.updatedPasswordHash);
    }

    @Test
    void rejectsPasswordChangeWhenCurrentPasswordIsWrong() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "teacher", 1);
        FakeSessions sessions = new FakeSessions();
        sessions.activeUser = authenticatedUser(1L, "teacher");
        AuthService service = service(users, sessions, new PlainHasher(), new FixedTokenGenerator());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            service.changePassword("token-1", "wrong123", "newSecret123", "newSecret123");
        });

        assertEquals("Current password is incorrect", exception.getMessage());
    }

    @Test
    void rejectsPasswordChangeWhenNewPasswordViolatesPolicy() {
        FakeUsers users = new FakeUsers();
        users.account = account(1L, "teacher", 1);
        FakeSessions sessions = new FakeSessions();
        sessions.activeUser = authenticatedUser(1L, "teacher");
        AuthService service = service(users, sessions, new PlainHasher(), new FixedTokenGenerator());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            service.changePassword("token-1", "secret123", "short1", "short1");
        });

        assertEquals("Password length must be 8-20 characters", exception.getMessage());
    }

    private AuthService service(UserAccountRepository users,
                                SessionRepository sessions,
                                PasswordHasher hasher,
                                TokenGenerator tokenGenerator) {
        return new AuthService(users, sessions, hasher, tokenGenerator, CLOCK);
    }

    private UserAccount account(Long id, String userType, int status) {
        UserAccount account = new UserAccount();
        account.setId(id);
        account.setUsername("teacher001");
        account.setPhone("13812345678");
        account.setRealName("Teacher One");
        account.setUserType(userType);
        account.setStatus(status);
        account.setPasswordHash("secret123");
        return account;
    }

    private AuthenticatedUser authenticatedUser(Long id, String userType) {
        AuthenticatedUser user = new AuthenticatedUser();
        user.setId(id);
        user.setUsername("teacher001");
        user.setRealName("Teacher One");
        user.setUserType(userType);
        return user;
    }

    private static class FakeUsers implements UserAccountRepository {
        private UserAccount account;
        private Long updatedPasswordUserId;
        private String updatedPasswordHash;

        @Override
        public Optional<UserAccount> findByIdentity(LoginIdentityType identityType, String accountValue) {
            return Optional.ofNullable(account);
        }

        @Override
        public Optional<UserAccount> findById(Long userId) {
            return Optional.ofNullable(account);
        }

        @Override
        public void updatePasswordHash(Long userId, String passwordHash) {
            this.updatedPasswordUserId = userId;
            this.updatedPasswordHash = passwordHash;
        }
    }

    private static class FakeSessions implements SessionRepository {
        private boolean activeSession;
        private Long invalidatedUserId;
        private String invalidatedToken;
        private String createdToken;
        private Instant expiresAt;
        private AuthenticatedUser activeUser;

        @Override
        public boolean hasActiveSession(Long userId, Instant now) {
            return activeSession;
        }

        @Override
        public void invalidateActiveSessions(Long userId) {
            this.invalidatedUserId = userId;
        }

        @Override
        public void createSession(Long userId, String token, Portal portal, String loginIp, Instant expiresAt) {
            this.createdToken = token;
            this.expiresAt = expiresAt;
        }

        @Override
        public Optional<AuthenticatedUser> findActiveUserByToken(String token, Instant now) {
            return Optional.ofNullable(activeUser);
        }

        @Override
        public void invalidateToken(String token) {
            this.invalidatedToken = token;
        }
    }

    private static class PlainHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return rawPassword.equals(passwordHash);
        }
    }

    private static class FixedTokenGenerator implements TokenGenerator {
        private final Map<Long, Integer> counts = new HashMap<Long, Integer>();

        @Override
        public String generate(Long userId) {
            Integer count = counts.get(userId);
            int next = count == null ? 1 : count + 1;
            counts.put(userId, next);
            return "token-" + next;
        }
    }
}
