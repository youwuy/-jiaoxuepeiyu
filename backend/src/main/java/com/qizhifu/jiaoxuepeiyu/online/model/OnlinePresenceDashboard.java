package com.qizhifu.jiaoxuepeiyu.online.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OnlinePresenceDashboard {

    private LocalDateTime generatedAt;
    private Long totalCount;
    private Long onlineCount;
    private Long offlineCount;
    private Integer heartbeatIntervalSeconds;
    private Integer offlineTimeoutSeconds;
    private List<OnlineUser> users = new ArrayList<OnlineUser>();

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(Long onlineCount) {
        this.onlineCount = onlineCount;
    }

    public Long getOfflineCount() {
        return offlineCount;
    }

    public void setOfflineCount(Long offlineCount) {
        this.offlineCount = offlineCount;
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

    public List<OnlineUser> getUsers() {
        return users;
    }

    public void setUsers(List<OnlineUser> users) {
        this.users = users == null ? new ArrayList<OnlineUser>() : users;
    }
}
