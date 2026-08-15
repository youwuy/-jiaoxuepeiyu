package com.qizhifu.jiaoxuepeiyu.auth;

import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginCommand;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginResult;
import com.qizhifu.jiaoxuepeiyu.auth.model.Portal;
import com.qizhifu.jiaoxuepeiyu.auth.model.UserAccount;
import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import com.qizhifu.jiaoxuepeiyu.auth.port.SessionRepository;
import com.qizhifu.jiaoxuepeiyu.auth.port.TokenGenerator;
import com.qizhifu.jiaoxuepeiyu.auth.port.UserAccountRepository;
import com.qizhifu.jiaoxuepeiyu.common.validation.PasswordPolicy;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final SessionRepository sessions;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;

    @Autowired
    public AuthService(UserAccountRepository users,
                       SessionRepository sessions,
                       PasswordHasher passwordHasher,
                       TokenGenerator tokenGenerator) {
        this(users, sessions, passwordHasher, tokenGenerator, Clock.systemDefaultZone());
    }

    AuthService(UserAccountRepository users,
                SessionRepository sessions,
                PasswordHasher passwordHasher,
                TokenGenerator tokenGenerator,
                Clock clock) {
        this.users = users;
        this.sessions = sessions;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
    }

    @Transactional
    public LoginResult login(LoginCommand command) {
        UserAccount user = users.findByIdentity(command.getIdentityType(), command.getAccount())
                .orElseThrow(() -> new AuthenticationException("Invalid account or password"));

        if (user.getStatus() != 1) {
            throw new AuthenticationException("Account is disabled");
        }
        if (!belongsToPortal(user, command.getPortal())) {
            throw new AuthenticationException("Account does not belong to this portal");
        }
        if (!passwordHasher.matches(command.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid account or password");
        }

        Instant now = Instant.now(clock);
        if (sessions.hasActiveSession(user.getId(), now)) {
            throw new AuthenticationException("Account already has an active session");
        }

        String token = tokenGenerator.generate(user.getId());
        Instant expiresAt = now.plus(24, ChronoUnit.HOURS);
        sessions.createSession(user.getId(), token, command.getPortal(), command.getLoginIp(), expiresAt);

        return new LoginResult(token, expiresAt, user.toAuthenticatedUser());
    }

    public AuthenticatedUser currentUser(String token) {
        String normalizedToken = requireToken(token);
        return sessions.findActiveUserByToken(normalizedToken, Instant.now(clock))
                .orElseThrow(() -> new AuthenticationException("Invalid or expired token"));
    }

    @Transactional
    public void logout(String token) {
        sessions.invalidateToken(requireToken(token));
    }

    @Transactional
    public void changePassword(String token, String currentPassword, String newPassword, String confirmPassword) {
        AuthenticatedUser currentUser = currentUser(token);
        PasswordPolicy.Result result = PasswordPolicy.validateChange(currentPassword, newPassword, confirmPassword);
        if (!result.isValid()) {
            throw new AuthenticationException(firstError(result.getErrors()));
        }

        UserAccount user = users.findById(currentUser.getId())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired token"));
        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }
        users.updatePasswordHash(user.getId(), passwordHasher.hash(newPassword));
    }

    private boolean belongsToPortal(UserAccount user, Portal portal) {
        if (portal == Portal.STUDENT) {
            return "student".equalsIgnoreCase(user.getUserType());
        }
        return "admin".equalsIgnoreCase(user.getUserType()) || "teacher".equalsIgnoreCase(user.getUserType());
    }

    private String requireToken(String token) {
        if (token == null || token.trim().length() == 0) {
            throw new AuthenticationException("Missing token");
        }
        return token.trim();
    }

    private String firstError(List<String> errors) {
        return errors.isEmpty() ? "Invalid password" : errors.get(0);
    }
}
