package com.qizhifu.jiaoxuepeiyu.admin.score.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminSemesterScore {

    private Long scoreId;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long classId;
    private String className;
    private Long majorId;
    private String majorName;
    private Long semesterId;
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
    private Integer rankNo;
    private LocalDateTime publishedAt;

    public Long getScoreId() {
        return scoreId;
    }

    public void setScoreId(Long scoreId) {
        this.scoreId = scoreId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

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

    public Integer getRankNo() {
        return rankNo;
    }

    public void setRankNo(Integer rankNo) {
        this.rankNo = rankNo;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
