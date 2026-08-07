package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.List;

public class AdminQuestionImportRow {

    private Integer rowNumber;
    private String questionType;
    private String title;
    private String standardAnswer;
    private String explanation;
    private Integer score;
    private List<AdminQuestionOption> options;

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
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

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public List<AdminQuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<AdminQuestionOption> options) {
        this.options = options;
    }
}
