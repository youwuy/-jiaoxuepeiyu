package com.qizhifu.jiaoxuepeiyu.student.archive.model;

import java.math.BigDecimal;

public class StudentTrainingArchiveStep {

    private Long stepId;
    private String stepName;
    private String standardOperation;
    private String actualOperation;
    private BigDecimal score;
    private BigDecimal maxScore;
    private int durationSeconds;
    private int videoStartSecond;

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
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

    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getVideoStartSecond() {
        return videoStartSecond;
    }

    public void setVideoStartSecond(int videoStartSecond) {
        this.videoStartSecond = videoStartSecond;
    }
}
