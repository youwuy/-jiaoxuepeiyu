package com.qizhifu.jiaoxuepeiyu.ue.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TrainingAttemptSubmission {

    private Long attemptId;
    private String clientAttemptId;
    private Long studentId;
    private Long trainingId;
    private String trainingName;
    private String trainingMode;
    private String roleName;
    private LocalDateTime submittedAt;
    private String submitType;
    private Integer durationSeconds;
    private BigDecimal personalScore;
    private BigDecimal teamScore;
    private String recordingUrl;

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public String getClientAttemptId() {
        return clientAttemptId;
    }

    public void setClientAttemptId(String clientAttemptId) {
        this.clientAttemptId = clientAttemptId;
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

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getTrainingMode() {
        return trainingMode;
    }

    public void setTrainingMode(String trainingMode) {
        this.trainingMode = trainingMode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSubmitType() {
        return submitType;
    }

    public void setSubmitType(String submitType) {
        this.submitType = submitType;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public BigDecimal getPersonalScore() {
        return personalScore;
    }

    public void setPersonalScore(BigDecimal personalScore) {
        this.personalScore = personalScore;
    }

    public BigDecimal getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(BigDecimal teamScore) {
        this.teamScore = teamScore;
    }

    public String getRecordingUrl() {
        return recordingUrl;
    }

    public void setRecordingUrl(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }
}
