package com.qizhifu.jiaoxuepeiyu.admin.training.model;

import java.time.LocalDateTime;
import java.util.List;

public class AdminTrainingCommand {

    private String trainingName;
    private Long academicYearId;
    private Long semesterId;
    private Long majorId;
    private String coverUrl;
    private String trainingType;
    private String trainingMode;
    private String paperMode;
    private Long paperId;
    private LocalDateTime openStartTime;
    private LocalDateTime openEndTime;
    private Integer teamSize;
    private Boolean appRequired;
    private Long classroomId;
    private List<Long> teacherIds;
    private String scoreBasis;
    private List<Long> topicIds;
    private List<Long> classIds;
    private List<Long> studentIds;
    private List<AdminTrainingRoleCommand> roles;
    private String publishStatus;

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public String getTrainingMode() {
        return trainingMode;
    }

    public void setTrainingMode(String trainingMode) {
        this.trainingMode = trainingMode;
    }

    public String getPaperMode() {
        return paperMode;
    }

    public void setPaperMode(String paperMode) {
        this.paperMode = paperMode;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public LocalDateTime getOpenStartTime() {
        return openStartTime;
    }

    public void setOpenStartTime(LocalDateTime openStartTime) {
        this.openStartTime = openStartTime;
    }

    public LocalDateTime getOpenEndTime() {
        return openEndTime;
    }

    public void setOpenEndTime(LocalDateTime openEndTime) {
        this.openEndTime = openEndTime;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public Boolean getAppRequired() {
        return appRequired;
    }

    public void setAppRequired(Boolean appRequired) {
        this.appRequired = appRequired;
    }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public List<Long> getTeacherIds() { return teacherIds; }
    public void setTeacherIds(List<Long> teacherIds) { this.teacherIds = teacherIds; }
    public String getScoreBasis() { return scoreBasis; }
    public void setScoreBasis(String scoreBasis) { this.scoreBasis = scoreBasis; }
    public List<Long> getTopicIds() { return topicIds; }
    public void setTopicIds(List<Long> topicIds) { this.topicIds = topicIds; }

    public List<Long> getClassIds() {
        return classIds;
    }

    public void setClassIds(List<Long> classIds) {
        this.classIds = classIds;
    }

    public List<Long> getStudentIds() { return studentIds; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }

    public List<AdminTrainingRoleCommand> getRoles() {
        return roles;
    }

    public void setRoles(List<AdminTrainingRoleCommand> roles) {
        this.roles = roles;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }
}
