package com.qizhifu.jiaoxuepeiyu.auth.port;

import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.auth.model.Portal;
import java.time.Instant;
import java.util.Optional;

public interface SessionRepository {

    void invalidateActiveSessions(Long userId);

    void createSession(Long userId, String token, Portal portal, String loginIp, Instant expiresAt);

    Optional<AuthenticatedUser> findActiveUserByToken(String token, Instant now);
}
