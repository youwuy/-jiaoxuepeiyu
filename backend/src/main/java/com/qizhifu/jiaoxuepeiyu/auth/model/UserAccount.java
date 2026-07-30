package com.qizhifu.jiaoxuepeiyu.auth.model;

public class UserAccount {

    private Long id;
    private String username;
    private String phone;
    private String realName;
    private String userType;
    private int status;
    private String passwordHash;

    public AuthenticatedUser toAuthenticatedUser() {
        AuthenticatedUser user = new AuthenticatedUser();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setUserType(userType);
        return user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
