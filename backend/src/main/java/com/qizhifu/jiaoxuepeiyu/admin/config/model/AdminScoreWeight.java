package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import java.time.LocalDateTime;

public class AdminScoreWeight {

    private Long weightId;
    private Long semesterId;
    private int coursewareWeight;
    private int trainingPracticeWeight;
    private int assignmentWeight;
    private int examWeight;
    private LocalDateTime effectiveFrom;
    private Long createdBy;
    private LocalDateTime createdAt;

    public Long getWeightId() {
        return weightId;
    }

    public void setWeightId(Long weightId) {
        this.weightId = weightId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public int getCoursewareWeight() {
        return coursewareWeight;
    }

    public void setCoursewareWeight(int coursewareWeight) {
        this.coursewareWeight = coursewareWeight;
    }

    public int getTrainingPracticeWeight() {
        return trainingPracticeWeight;
    }

    public void setTrainingPracticeWeight(int trainingPracticeWeight) {
        this.trainingPracticeWeight = trainingPracticeWeight;
    }

    public int getAssignmentWeight() {
        return assignmentWeight;
    }

    public void setAssignmentWeight(int assignmentWeight) {
        this.assignmentWeight = assignmentWeight;
    }

    public int getExamWeight() {
        return examWeight;
    }

    public void setExamWeight(int examWeight) {
        this.examWeight = examWeight;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
