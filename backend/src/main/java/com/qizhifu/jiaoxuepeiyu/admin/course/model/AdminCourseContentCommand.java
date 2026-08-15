package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminCourseContentCommand {

    private Long contentId;
    private String itemType;
    private String title;
    private Long resourceId;
    private Long assignmentId;
    private String assignmentType;
    private Integer requiredDurationSeconds;
    private LocalDateTime learningStartTime;
    private LocalDateTime learningEndTime;
    private String assignmentCompletionRule;
    private Integer passScore;
    private String assignmentPublishMode;
    private LocalDateTime answerStartTime;
    private LocalDateTime answerEndTime;
    private Integer assignmentTotalScore;
    private List<Long> questionIds = new ArrayList<Long>();
    private List<Long> trainingIds = new ArrayList<Long>();
    private Integer sortOrder;

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

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

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
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

    public List<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds == null ? new ArrayList<Long>() : questionIds;
    }

    public List<Long> getTrainingIds() {
        return trainingIds;
    }

    public void setTrainingIds(List<Long> trainingIds) {
        this.trainingIds = trainingIds == null ? new ArrayList<Long>() : trainingIds;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
