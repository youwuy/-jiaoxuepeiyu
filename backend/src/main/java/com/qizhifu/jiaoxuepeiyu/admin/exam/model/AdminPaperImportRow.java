package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.List;

public class AdminPaperImportRow {

    private Integer rowNumber;
    private String paperName;
    private String composeMode;
    private List<AdminPaperQuestionCommand> questions;
    private List<AdminPaperAutoRule> autoRules;

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
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

    public List<AdminPaperQuestionCommand> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AdminPaperQuestionCommand> questions) {
        this.questions = questions;
    }

    public List<AdminPaperAutoRule> getAutoRules() {
        return autoRules;
    }

    public void setAutoRules(List<AdminPaperAutoRule> autoRules) {
        this.autoRules = autoRules;
    }
}
