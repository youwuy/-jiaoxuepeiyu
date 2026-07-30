package com.qizhifu.jiaoxuepeiyu.admin.archive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminTrainingArchiveDetail {

    private Long archiveId;
    private Long trainingId;
    private String trainingName;
    private String trainingMode;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private String roleName;
    private LocalDateTime submittedAt;
    private String submitType;
    private int durationSeconds;
    private BigDecimal personalScore;
    private BigDecimal teamScore;
    private String recordingUrl;
    private List<AdminTrainingArchiveStep> steps = new ArrayList<AdminTrainingArchiveStep>();

    public Long getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(Long archiveId) {
        this.archiveId = archiveId;
    }

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getTrainingMode() {
        return trainingMode;
    }

    public void setTrainingMode(String trainingMode) {
        this.trainingMode = trainingMode;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSubmitType() {
        return submitType;
    }

    public void setSubmitType(String submitType) {
        this.submitType = submitType;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public BigDecimal getPersonalScore() {
        return personalScore;
    }

    public void setPersonalScore(BigDecimal personalScore) {
        this.personalScore = personalScore;
    }

    public BigDecimal getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(BigDecimal teamScore) {
        this.teamScore = teamScore;
    }

    public String getRecordingUrl() {
        return recordingUrl;
    }

    public void setRecordingUrl(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }

    public List<AdminTrainingArchiveStep> getSteps() {
        return steps;
    }

    public void setSteps(List<AdminTrainingArchiveStep> steps) {
        this.steps = steps;
    }
}
