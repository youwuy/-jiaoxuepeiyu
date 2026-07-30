package com.qizhifu.jiaoxuepeiyu.student.assignment.model;

public class AssignmentQuestionRecord {

    private Long questionId;
    private String questionType;
    private String title;
    private String options;
    private String standardAnswer;
    private int score;
    private String answerContent;
    private int awardedScore;

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

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public int getAwardedScore() {
        return awardedScore;
    }

    public void setAwardedScore(int awardedScore) {
        this.awardedScore = awardedScore;
    }
}
