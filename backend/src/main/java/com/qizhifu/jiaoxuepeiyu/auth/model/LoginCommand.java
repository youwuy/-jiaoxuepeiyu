package com.qizhifu.jiaoxuepeiyu.auth.model;

public class LoginCommand {

    private final Portal portal;
    private final LoginIdentityType identityType;
    private final String account;
    private final String password;
    private final String loginIp;

    public LoginCommand(Portal portal,
                        LoginIdentityType identityType,
                        String account,
                        String password,
                        String loginIp) {
        this.portal = portal;
        this.identityType = identityType;
        this.account = account;
        this.password = password;
        this.loginIp = loginIp;
    }

    public Portal getPortal() {
        return portal;
    }

    public LoginIdentityType getIdentityType() {
        return identityType;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getLoginIp() {
        return loginIp;
    }
}
