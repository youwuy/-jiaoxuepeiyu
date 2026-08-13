package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingTopic {
    private Long topicId;
    private String topicName;
    private String category;
    private String trainingMode;
    private Integer durationMinutes;
    private Integer score;
    private String roleNames;

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTrainingMode() { return trainingMode; }
    public void setTrainingMode(String trainingMode) { this.trainingMode = trainingMode; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }
}
