package com.qizhifu.jiaoxuepeiyu.student.training.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentTraining {

    private Long trainingId;
    private String trainingName;
    private String trainingType;
    private String trainingMode;
    private String status;
    private LocalDateTime openStartTime;
    private LocalDateTime openEndTime;
    private int teamSize;
    private int roleCount;
    private boolean appRequired;
    private boolean appInstalled;
    private Long activeRoomId;
    private Long latestAttemptId;
    private List<StudentTrainingTopic> topics = new ArrayList<StudentTrainingTopic>();

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public String getTrainingMode() {
        return trainingMode;
    }

    public void setTrainingMode(String trainingMode) {
        this.trainingMode = trainingMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOpenStartTime() {
        return openStartTime;
    }

    public void setOpenStartTime(LocalDateTime openStartTime) {
        this.openStartTime = openStartTime;
    }

    public LocalDateTime getOpenEndTime() {
        return openEndTime;
    }

    public void setOpenEndTime(LocalDateTime openEndTime) {
        this.openEndTime = openEndTime;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getRoleCount() {
        return roleCount;
    }

    public void setRoleCount(int roleCount) {
        this.roleCount = roleCount;
    }

    public boolean isAppRequired() {
        return appRequired;
    }

    public void setAppRequired(boolean appRequired) {
        this.appRequired = appRequired;
    }

    public boolean isAppInstalled() {
        return appInstalled;
    }

    public void setAppInstalled(boolean appInstalled) {
        this.appInstalled = appInstalled;
    }

    public Long getActiveRoomId() {
        return activeRoomId;
    }

    public void setActiveRoomId(Long activeRoomId) {
        this.activeRoomId = activeRoomId;
    }

    public Long getLatestAttemptId() {
        return latestAttemptId;
    }

    public void setLatestAttemptId(Long latestAttemptId) {
        this.latestAttemptId = latestAttemptId;
    }

    public List<StudentTrainingTopic> getTopics() { return topics; }
    public void setTopics(List<StudentTrainingTopic> topics) {
        this.topics = topics == null ? new ArrayList<StudentTrainingTopic>() : topics;
    }
}
