package com.qizhifu.jiaoxuepeiyu.auth;

import java.util.Optional;

public final class BearerTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private BearerTokenResolver() {
    }

    public static Optional<String> resolve(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.length() == 0 ? Optional.<String>empty() : Optional.of(token);
    }
}
