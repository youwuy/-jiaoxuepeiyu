package com.qizhifu.jiaoxuepeiyu.bootstrap;

public class BootstrapAdminCommand {

    private final String username;
    private final String password;
    private final String realName;
    private final String phone;

    public BootstrapAdminCommand(String username, String password, String realName, String phone) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRealName() {
        return realName;
    }

    public String getPhone() {
        return phone;
    }
}
