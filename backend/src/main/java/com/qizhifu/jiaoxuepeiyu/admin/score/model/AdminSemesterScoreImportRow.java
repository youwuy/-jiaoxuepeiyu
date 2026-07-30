package com.qizhifu.jiaoxuepeiyu.admin.score.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminSemesterScoreImportRow {

    private Integer rowNo;
    private String studentNo;
    private Long studentId;
    private Long semesterId;
    private BigDecimal coursewareLearningScore;
    private BigDecimal trainingPracticeScore;
    private BigDecimal courseAssignmentScore;
    private BigDecimal examScore;
    private Integer coursewareWeight;
    private Integer trainingPracticeWeight;
    private Integer assignmentWeight;
    private Integer examWeight;
    private BigDecimal comprehensiveScore;
    private Boolean valid;
    private List<String> errors = new ArrayList<String>();

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
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

    public Integer getCoursewareWeight() {
        return coursewareWeight;
    }

    public void setCoursewareWeight(Integer coursewareWeight) {
        this.coursewareWeight = coursewareWeight;
    }

    public Integer getTrainingPracticeWeight() {
        return trainingPracticeWeight;
    }

    public void setTrainingPracticeWeight(Integer trainingPracticeWeight) {
        this.trainingPracticeWeight = trainingPracticeWeight;
    }

    public Integer getAssignmentWeight() {
        return assignmentWeight;
    }

    public void setAssignmentWeight(Integer assignmentWeight) {
        this.assignmentWeight = assignmentWeight;
    }

    public Integer getExamWeight() {
        return examWeight;
    }

    public void setExamWeight(Integer examWeight) {
        this.examWeight = examWeight;
    }

    public BigDecimal getComprehensiveScore() {
        return comprehensiveScore;
    }

    public void setComprehensiveScore(BigDecimal comprehensiveScore) {
        this.comprehensiveScore = comprehensiveScore;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors == null ? new ArrayList<String>() : errors;
    }
}
