package com.qizhifu.jiaoxuepeiyu.controller;

import java.time.LocalDateTime;

public class HealthStatus {

    private String status;
    private String service;
    private String javaVersion;
    private String databaseVersionTarget;
    private LocalDateTime time;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getDatabaseVersionTarget() {
        return databaseVersionTarget;
    }

    public void setDatabaseVersionTarget(String databaseVersionTarget) {
        this.databaseVersionTarget = databaseVersionTarget;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
