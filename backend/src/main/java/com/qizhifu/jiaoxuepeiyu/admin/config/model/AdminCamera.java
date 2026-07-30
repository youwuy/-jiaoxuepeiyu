package com.qizhifu.jiaoxuepeiyu.admin.config.model;

public class AdminCamera {

    private Long cameraId;
    private Long classroomId;
    private String nvrHost;
    private int nvrPort;
    private String adminUsername;
    private String adminPassword;
    private String nvrChannel;
    private String streamUrl;
    private int sortOrder;

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
