package com.qizhifu.jiaoxuepeiyu.auth.repository;

import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.auth.model.Portal;
import com.qizhifu.jiaoxuepeiyu.auth.port.SessionRepository;
import com.qizhifu.jiaoxuepeiyu.auth.security.TokenHash;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisSessionRepository implements SessionRepository {

    private final SessionMapper mapper;

    public MyBatisSessionRepository(SessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void invalidateActiveSessions(Long userId) {
        mapper.invalidateActiveSessions(userId);
    }

    @Override
    public void createSession(Long userId, String token, Portal portal, String loginIp, Instant expiresAt) {
        mapper.createSession(userId, TokenHash.sha256(token), portal.name(), loginIp, expiresAt);
    }

    @Override
    public Optional<AuthenticatedUser> findActiveUserByToken(String token, Instant now) {
        return Optional.ofNullable(mapper.findActiveUserByToken(TokenHash.sha256(token), now));
    }
}
