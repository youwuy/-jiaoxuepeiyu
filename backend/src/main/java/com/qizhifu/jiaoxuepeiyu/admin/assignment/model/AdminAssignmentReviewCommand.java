package com.qizhifu.jiaoxuepeiyu.admin.assignment.model;

import java.util.List;

public class AdminAssignmentReviewCommand {

    private String reviewComment;
    private List<AdminAssignmentReviewItem> answers;

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public List<AdminAssignmentReviewItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AdminAssignmentReviewItem> answers) {
        this.answers = answers;
    }
}
