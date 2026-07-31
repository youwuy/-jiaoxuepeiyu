package com.qizhifu.jiaoxuepeiyu.student.assignment.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentAssignmentDetail {

    private Long assignmentId;
    private Long courseId;
    private String assignmentTitle;
    private String assignmentType;
    private LocalDateTime deadline;
    private LocalDateTime answerStartTime;
    private LocalDateTime answerEndTime;
    private String completionRule;
    private Integer passScore;
    private String publishMode;
    private int totalScore;
    private String status;
    private LocalDateTime submittedAt;
    private List<StudentAssignmentQuestion> questions = new ArrayList<StudentAssignmentQuestion>();

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
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

    public String getCompletionRule() {
        return completionRule;
    }

    public void setCompletionRule(String completionRule) {
        this.completionRule = completionRule;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }

    public String getPublishMode() {
        return publishMode;
    }

    public void setPublishMode(String publishMode) {
        this.publishMode = publishMode;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<StudentAssignmentQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<StudentAssignmentQuestion> questions) {
        this.questions = questions;
    }
}
