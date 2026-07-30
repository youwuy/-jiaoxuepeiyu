package com.qizhifu.jiaoxuepeiyu.admin.config.model;

public class AdminCameraCommand {

    private String nvrHost;
    private int nvrPort;
    private String adminUsername;
    private String adminPassword;
    private String nvrChannel;
    private String streamUrl;

    public String getNvrHost() {
        return nvrHost;
    }

    public void setNvrHost(String nvrHost) {
        this.nvrHost = nvrHost;
    }

    public int getNvrPort() {
        return nvrPort;
    }

    public void setNvrPort(int nvrPort) {
        this.nvrPort = nvrPort;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getNvrChannel() {
        return nvrChannel;
    }

    public void setNvrChannel(String nvrChannel) {
        this.nvrChannel = nvrChannel;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }
}
