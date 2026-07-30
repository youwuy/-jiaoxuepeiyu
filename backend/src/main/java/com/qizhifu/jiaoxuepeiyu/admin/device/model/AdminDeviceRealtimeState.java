package com.qizhifu.jiaoxuepeiyu.admin.device.model;

import java.time.LocalDateTime;

public class AdminDeviceRealtimeState {

    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String deviceStatus;
    private Long classroomId;
    private String classroomName;
    private Long currentTrainingId;
    private String currentTrainingName;
    private Long currentStudentId;
    private String currentStudentName;
    private LocalDateTime currentStartedAt;
    private Integer currentUsageMinutes;
    private LocalDateTime lastHeartbeatAt;

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public Long getCurrentTrainingId() {
        return currentTrainingId;
    }

    public void setCurrentTrainingId(Long currentTrainingId) {
        this.currentTrainingId = currentTrainingId;
    }

    public String getCurrentTrainingName() {
        return currentTrainingName;
    }

    public void setCurrentTrainingName(String currentTrainingName) {
        this.currentTrainingName = currentTrainingName;
    }

    public Long getCurrentStudentId() {
        return currentStudentId;
    }

    public void setCurrentStudentId(Long currentStudentId) {
        this.currentStudentId = currentStudentId;
    }

    public String getCurrentStudentName() {
        return currentStudentName;
    }

    public void setCurrentStudentName(String currentStudentName) {
        this.currentStudentName = currentStudentName;
    }

    public LocalDateTime getCurrentStartedAt() {
        return currentStartedAt;
    }

    public void setCurrentStartedAt(LocalDateTime currentStartedAt) {
        this.currentStartedAt = currentStartedAt;
    }

    public Integer getCurrentUsageMinutes() {
        return currentUsageMinutes;
    }

    public void setCurrentUsageMinutes(Integer currentUsageMinutes) {
        this.currentUsageMinutes = currentUsageMinutes;
    }

    public LocalDateTime getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(LocalDateTime lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }
}
