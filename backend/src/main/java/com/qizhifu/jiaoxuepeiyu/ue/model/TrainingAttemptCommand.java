package com.qizhifu.jiaoxuepeiyu.ue.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrainingAttemptCommand {

    private String clientAttemptId;
    private String submitType;
    private Integer durationSeconds;
    private BigDecimal personalScore;
    private BigDecimal teamScore;
    private String recordingUrl;
    private LocalDateTime submittedAt;
    private List<TrainingAttemptStepCommand> steps = new ArrayList<TrainingAttemptStepCommand>();

    public String getClientAttemptId() {
        return clientAttemptId;
    }

    public void setClientAttemptId(String clientAttemptId) {
        this.clientAttemptId = clientAttemptId;
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<TrainingAttemptStepCommand> getSteps() {
        return steps;
    }

    public void setSteps(List<TrainingAttemptStepCommand> steps) {
        this.steps = steps == null ? new ArrayList<TrainingAttemptStepCommand>() : steps;
    }
}
