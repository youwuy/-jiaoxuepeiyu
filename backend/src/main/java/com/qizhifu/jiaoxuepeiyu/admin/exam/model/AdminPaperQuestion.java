package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.List;

public class AdminPaperQuestion {

    private Long questionId;
    private String questionType;
    private String title;
    private String optionsJson;
    private String standardAnswer;
    private Integer score;
    private Integer sortOrder;
    private List<AdminQuestionOption> options;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<AdminQuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<AdminQuestionOption> options) {
        this.options = options;
    }
}
