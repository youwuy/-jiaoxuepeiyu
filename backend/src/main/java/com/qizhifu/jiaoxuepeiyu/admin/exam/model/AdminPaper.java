package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.Date;
import java.util.List;

public class AdminPaper {

    private Long paperId;
    private String paperName;
    private String composeMode;
    private Integer totalScore;
    private Integer questionCount;
    private String publishStatus;
    private Long creatorId;
    private String creatorName;
    private Date createdAt;
    private Date updatedAt;
    private List<AdminPaperQuestion> questions;

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getComposeMode() {
        return composeMode;
    }

    public void setComposeMode(String composeMode) {
        this.composeMode = composeMode;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AdminPaperQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AdminPaperQuestion> questions) {
        this.questions = questions;
    }
}
