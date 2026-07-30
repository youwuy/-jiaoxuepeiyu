package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

public class AdminPaperAutoRule {

    private String questionType;
    private Integer questionCount;
    private Integer scorePerQuestion;

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getScorePerQuestion() {
        return scorePerQuestion;
    }

    public void setScorePerQuestion(Integer scorePerQuestion) {
        this.scorePerQuestion = scorePerQuestion;
    }
}
