package com.qizhifu.jiaoxuepeiyu.online.model;

import java.time.LocalDateTime;

public class OnlineHeartbeatResult {

    private LocalDateTime heartbeatAt;
    private Integer heartbeatIntervalSeconds;
    private Integer offlineTimeoutSeconds;

    public LocalDateTime getHeartbeatAt() {
        return heartbeatAt;
    }

    public void setHeartbeatAt(LocalDateTime heartbeatAt) {
        this.heartbeatAt = heartbeatAt;
    }

    public Integer getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(Integer heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public Integer getOfflineTimeoutSeconds() {
        return offlineTimeoutSeconds;
    }

    public void setOfflineTimeoutSeconds(Integer offlineTimeoutSeconds) {
        this.offlineTimeoutSeconds = offlineTimeoutSeconds;
    }
}
