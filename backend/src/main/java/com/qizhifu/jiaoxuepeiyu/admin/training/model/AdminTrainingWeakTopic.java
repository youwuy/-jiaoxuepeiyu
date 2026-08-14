package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingWeakTopic {

    private Long topicId;
    private String topicName;
    private Integer errorStudentCount;
    private Integer submittedStudentCount;
    private Double correctRate;

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public Integer getErrorStudentCount() { return errorStudentCount; }
    public void setErrorStudentCount(Integer errorStudentCount) { this.errorStudentCount = errorStudentCount; }
    public Integer getSubmittedStudentCount() { return submittedStudentCount; }
    public void setSubmittedStudentCount(Integer submittedStudentCount) { this.submittedStudentCount = submittedStudentCount; }
    public Double getCorrectRate() { return correctRate; }
    public void setCorrectRate(Double correctRate) { this.correctRate = correctRate; }
}
