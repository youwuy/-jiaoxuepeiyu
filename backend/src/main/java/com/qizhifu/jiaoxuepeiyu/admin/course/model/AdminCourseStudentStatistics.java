package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.math.BigDecimal;

public class AdminCourseStudentStatistics {

    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long classId;
    private String className;
    private BigDecimal progressPercent;
    private BigDecimal progressScore;
    private Integer assignmentCount;
    private BigDecimal assignmentScore;

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

    public BigDecimal getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(BigDecimal progressPercent) {
        this.progressPercent = progressPercent;
    }

    public BigDecimal getProgressScore() {
        return progressScore;
    }

    public void setProgressScore(BigDecimal progressScore) {
        this.progressScore = progressScore;
    }

    public Integer getAssignmentCount() {
        return assignmentCount;
    }

    public void setAssignmentCount(Integer assignmentCount) {
        this.assignmentCount = assignmentCount;
    }

    public BigDecimal getAssignmentScore() {
        return assignmentScore;
    }

    public void setAssignmentScore(BigDecimal assignmentScore) {
        this.assignmentScore = assignmentScore;
    }
}
