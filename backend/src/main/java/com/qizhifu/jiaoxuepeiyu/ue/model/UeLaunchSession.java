package com.qizhifu.jiaoxuepeiyu.ue.model;

import java.time.LocalDateTime;

public class UeLaunchSession {

    private String launchToken;
    private Long studentId;
    private Long trainingId;
    private Long roomId;
    private LocalDateTime expiresAt;

    public String getLaunchToken() {
        return launchToken;
    }

    public void setLaunchToken(String launchToken) {
        this.launchToken = launchToken;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
