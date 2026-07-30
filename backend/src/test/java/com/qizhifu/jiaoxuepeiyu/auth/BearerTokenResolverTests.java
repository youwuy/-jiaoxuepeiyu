package com.qizhifu.jiaoxuepeiyu.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class BearerTokenResolverTests {

    @Test
    void extractsBearerToken() {
        Optional<String> token = BearerTokenResolver.resolve("Bearer token-1");

        assertEquals("token-1", token.get());
    }

    @Test
    void ignoresBlankOrUnsupportedAuthorizationHeader() {
        assertFalse(BearerTokenResolver.resolve(null).isPresent());
        assertFalse(BearerTokenResolver.resolve("Basic abc").isPresent());
        assertFalse(BearerTokenResolver.resolve("Bearer   ").isPresent());
    }
}
