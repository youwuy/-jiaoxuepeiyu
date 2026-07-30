package com.qizhifu.jiaoxuepeiyu.student.assignment.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentAssignmentReport {

    private Long assignmentId;
    private String assignmentTitle;
    private String status;
    private Integer score;
    private String reviewComment;
    private LocalDateTime submittedAt;
    private List<StudentAssignmentReportAnswer> answers = new ArrayList<StudentAssignmentReportAnswer>();

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<StudentAssignmentReportAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<StudentAssignmentReportAnswer> answers) {
        this.answers = answers;
    }
}
