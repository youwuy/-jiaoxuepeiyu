package com.qizhifu.jiaoxuepeiyu.ue.model;

import java.math.BigDecimal;

public class TrainingAttemptStepCommand {

    private Long attemptId;
    private String stepName;
    private String standardOperation;
    private String actualOperation;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Integer durationSeconds;
    private Integer videoStartSecond;
    private Integer sortOrder;

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getStandardOperation() {
        return standardOperation;
    }

    public void setStandardOperation(String standardOperation) {
        this.standardOperation = standardOperation;
    }

    public String getActualOperation() {
        return actualOperation;
    }

    public void setActualOperation(String actualOperation) {
        this.actualOperation = actualOperation;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getVideoStartSecond() {
        return videoStartSecond;
    }

    public void setVideoStartSecond(Integer videoStartSecond) {
        this.videoStartSecond = videoStartSecond;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
