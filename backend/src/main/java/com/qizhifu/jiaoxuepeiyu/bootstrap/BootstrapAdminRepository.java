package com.qizhifu.jiaoxuepeiyu.bootstrap;

public interface BootstrapAdminRepository {

    boolean hasAnyAdmin();

    boolean usernameExists(String username);

    void createAdmin(String username, String realName, String phone, String passwordHash);
}
