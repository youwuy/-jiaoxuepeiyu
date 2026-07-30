package com.qizhifu.jiaoxuepeiyu.auth.port;

public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
