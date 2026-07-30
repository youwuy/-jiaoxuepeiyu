package com.qizhifu.jiaoxuepeiyu.auth.model;

import java.time.Instant;

public class LoginResult {

    private String token;
    private Instant expiresAt;
    private AuthenticatedUser user;

    public LoginResult() {
    }

    public LoginResult(String token, Instant expiresAt, AuthenticatedUser user) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public void setUser(AuthenticatedUser user) {
        this.user = user;
    }
}
