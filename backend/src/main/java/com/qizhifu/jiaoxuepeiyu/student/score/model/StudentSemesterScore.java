package com.qizhifu.jiaoxuepeiyu.student.score.model;

import java.math.BigDecimal;

public class StudentSemesterScore {

    private String academicTerm;
    private BigDecimal coursewareLearningScore;
    private BigDecimal trainingPracticeScore;
    private BigDecimal courseAssignmentScore;
    private BigDecimal examScore;
    private int coursewareWeight;
    private int trainingPracticeWeight;
    private int assignmentWeight;
    private int examWeight;
    private BigDecimal comprehensiveScore;

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        this.academicTerm = academicTerm;
    }

    public BigDecimal getCoursewareLearningScore() {
        return coursewareLearningScore;
    }

    public void setCoursewareLearningScore(BigDecimal coursewareLearningScore) {
        this.coursewareLearningScore = coursewareLearningScore;
    }

    public BigDecimal getTrainingPracticeScore() {
        return trainingPracticeScore;
    }

    public void setTrainingPracticeScore(BigDecimal trainingPracticeScore) {
        this.trainingPracticeScore = trainingPracticeScore;
    }

    public BigDecimal getCourseAssignmentScore() {
        return courseAssignmentScore;
    }

    public void setCourseAssignmentScore(BigDecimal courseAssignmentScore) {
        this.courseAssignmentScore = courseAssignmentScore;
    }

    public BigDecimal getExamScore() {
        return examScore;
    }

    public void setExamScore(BigDecimal examScore) {
        this.examScore = examScore;
    }

    public int getCoursewareWeight() {
        return coursewareWeight;
    }

    public void setCoursewareWeight(int coursewareWeight) {
        this.coursewareWeight = coursewareWeight;
    }

    public int getTrainingPracticeWeight() {
        return trainingPracticeWeight;
    }

    public void setTrainingPracticeWeight(int trainingPracticeWeight) {
        this.trainingPracticeWeight = trainingPracticeWeight;
    }

    public int getAssignmentWeight() {
        return assignmentWeight;
    }

    public void setAssignmentWeight(int assignmentWeight) {
        this.assignmentWeight = assignmentWeight;
    }

    public int getExamWeight() {
        return examWeight;
    }

    public void setExamWeight(int examWeight) {
        this.examWeight = examWeight;
    }

    public BigDecimal getComprehensiveScore() {
        return comprehensiveScore;
    }

    public void setComprehensiveScore(BigDecimal comprehensiveScore) {
        this.comprehensiveScore = comprehensiveScore;
    }
}
