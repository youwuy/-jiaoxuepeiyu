package com.qizhifu.jiaoxuepeiyu.student.archive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentTrainingArchive {

    private Long archiveId;
    private String trainingName;
    private String trainingMode;
    private String roleName;
    private LocalDateTime submittedAt;
    private String submitType;
    private int durationSeconds;
    private BigDecimal personalScore;
    private BigDecimal teamScore;

    public Long getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(Long archiveId) {
        this.archiveId = archiveId;
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

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
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
}
