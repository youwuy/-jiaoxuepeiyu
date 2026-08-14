package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminPaperPreview {

    private String paperName;
    private String courseName;
    private String composeMode;
    private Integer totalScore;
    private List<AdminPaperQuestion> questions = new ArrayList<AdminPaperQuestion>();

    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getComposeMode() { return composeMode; }
    public void setComposeMode(String composeMode) { this.composeMode = composeMode; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public List<AdminPaperQuestion> getQuestions() { return questions; }
    public void setQuestions(List<AdminPaperQuestion> questions) {
        this.questions = questions == null ? new ArrayList<AdminPaperQuestion>() : questions;
    }
}
