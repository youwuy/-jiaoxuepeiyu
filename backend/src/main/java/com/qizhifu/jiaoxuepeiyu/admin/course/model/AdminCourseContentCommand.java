package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.time.LocalDateTime;

public class AdminCourseContentCommand {

    private String itemType;
    private String title;
    private Long resourceId;
    private Long assignmentId;
    private Integer requiredDurationSeconds;
    private LocalDateTime learningStartTime;
    private LocalDateTime learningEndTime;
    private String assignmentCompletionRule;
    private Integer passScore;
    private String assignmentPublishMode;
    private LocalDateTime answerStartTime;
    private LocalDateTime answerEndTime;
    private Integer assignmentTotalScore;
    private Integer sortOrder;

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getRequiredDurationSeconds() {
        return requiredDurationSeconds;
    }

    public void setRequiredDurationSeconds(Integer requiredDurationSeconds) {
        this.requiredDurationSeconds = requiredDurationSeconds;
    }

    public LocalDateTime getLearningStartTime() {
        return learningStartTime;
    }

    public void setLearningStartTime(LocalDateTime learningStartTime) {
        this.learningStartTime = learningStartTime;
    }

    public LocalDateTime getLearningEndTime() {
        return learningEndTime;
    }

    public void setLearningEndTime(LocalDateTime learningEndTime) {
        this.learningEndTime = learningEndTime;
    }

    public String getAssignmentCompletionRule() {
        return assignmentCompletionRule;
    }

    public void setAssignmentCompletionRule(String assignmentCompletionRule) {
        this.assignmentCompletionRule = assignmentCompletionRule;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }

    public String getAssignmentPublishMode() {
        return assignmentPublishMode;
    }

    public void setAssignmentPublishMode(String assignmentPublishMode) {
        this.assignmentPublishMode = assignmentPublishMode;
    }

    public LocalDateTime getAnswerStartTime() {
        return answerStartTime;
    }

    public void setAnswerStartTime(LocalDateTime answerStartTime) {
        this.answerStartTime = answerStartTime;
    }

    public LocalDateTime getAnswerEndTime() {
        return answerEndTime;
    }

    public void setAnswerEndTime(LocalDateTime answerEndTime) {
        this.answerEndTime = answerEndTime;
    }

    public Integer getAssignmentTotalScore() {
        return assignmentTotalScore;
    }

    public void setAssignmentTotalScore(Integer assignmentTotalScore) {
        this.assignmentTotalScore = assignmentTotalScore;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
