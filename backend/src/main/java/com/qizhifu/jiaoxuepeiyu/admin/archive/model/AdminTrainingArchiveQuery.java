package com.qizhifu.jiaoxuepeiyu.admin.archive.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminTrainingArchiveQuery {

    private Long trainingId;
    private Long studentId;
    private Long classId;
    private String studentNo;
    private String studentName;
    private String trainingMode;
    private String submitType;
    private String keyword;
    private LocalDate submittedStartDate;
    private LocalDate submittedEndDate;
    private LocalDateTime submittedStartTime;
    private LocalDateTime submittedEndExclusiveTime;
    private int page = 1;
    private int pageSize = 20;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTrainingMode() {
        return trainingMode;
    }

    public void setTrainingMode(String trainingMode) {
        this.trainingMode = trainingMode;
    }

    public String getSubmitType() {
        return submitType;
    }

    public void setSubmitType(String submitType) {
        this.submitType = submitType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LocalDate getSubmittedStartDate() {
        return submittedStartDate;
    }

    public void setSubmittedStartDate(LocalDate submittedStartDate) {
        this.submittedStartDate = submittedStartDate;
    }

    public LocalDate getSubmittedEndDate() {
        return submittedEndDate;
    }

    public void setSubmittedEndDate(LocalDate submittedEndDate) {
        this.submittedEndDate = submittedEndDate;
    }

    public LocalDateTime getSubmittedStartTime() {
        return submittedStartTime;
    }

    public void setSubmittedStartTime(LocalDateTime submittedStartTime) {
        this.submittedStartTime = submittedStartTime;
    }

    public LocalDateTime getSubmittedEndExclusiveTime() {
        return submittedEndExclusiveTime;
    }

    public void setSubmittedEndExclusiveTime(LocalDateTime submittedEndExclusiveTime) {
        this.submittedEndExclusiveTime = submittedEndExclusiveTime;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
