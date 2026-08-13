package com.qizhifu.jiaoxuepeiyu.ue.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TrainingMonitorSnapshotCommand {

    private Long trainingId;
    private Long studentId;
    private Long classroomId;
    private String deskStatus;
    private String progressStatus;
    private String currentTopicName;
    private Integer submittedTopicCount;
    private String desktopStreamUrl;
    private BigDecimal score;
    private BigDecimal teamScore;
    private LocalDateTime lastEventAt;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getDeskStatus() {
        return deskStatus;
    }

    public void setDeskStatus(String deskStatus) {
        this.deskStatus = deskStatus;
    }

    public String getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(String progressStatus) {
        this.progressStatus = progressStatus;
    }

    public String getCurrentTopicName() { return currentTopicName; }
    public void setCurrentTopicName(String currentTopicName) { this.currentTopicName = currentTopicName; }
    public Integer getSubmittedTopicCount() { return submittedTopicCount; }
    public void setSubmittedTopicCount(Integer submittedTopicCount) { this.submittedTopicCount = submittedTopicCount; }
    public String getDesktopStreamUrl() { return desktopStreamUrl; }
    public void setDesktopStreamUrl(String desktopStreamUrl) { this.desktopStreamUrl = desktopStreamUrl; }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(BigDecimal teamScore) {
        this.teamScore = teamScore;
    }

    public LocalDateTime getLastEventAt() {
        return lastEventAt;
    }

    public void setLastEventAt(LocalDateTime lastEventAt) {
        this.lastEventAt = lastEventAt;
    }
}
