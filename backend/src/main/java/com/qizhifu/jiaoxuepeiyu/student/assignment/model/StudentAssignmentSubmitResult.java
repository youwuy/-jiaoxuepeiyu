package com.qizhifu.jiaoxuepeiyu.student.assignment.model;

import java.time.LocalDateTime;

public class StudentAssignmentSubmitResult {

    private Long attemptId;
    private String status;
    private int autoScore;
    private LocalDateTime submittedAt;

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAutoScore() {
        return autoScore;
    }

    public void setAutoScore(int autoScore) {
        this.autoScore = autoScore;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
