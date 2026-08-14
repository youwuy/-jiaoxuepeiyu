package com.qizhifu.jiaoxuepeiyu.admin.training.model;

import java.time.LocalDateTime;
import java.util.List;

public class AdminTraining {

    private Long trainingId;
    private String trainingName;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterName;
    private Long majorId;
    private String majorName;
    private String coverUrl;
    private String trainingType;
    private String trainingMode;
    private String paperMode;
    private Long paperId;
    private String paperName;
    private String publishStatus;
    private LocalDateTime openStartTime;
    private LocalDateTime openEndTime;
    private LocalDateTime examStartedAt;
    private Integer teamSize;
    private Boolean appRequired;
    private Boolean recordingEnabled;
    private Long classroomId;
    private List<Long> teacherIds;
    private String scoreBasis;
    private List<Long> topicIds;
    private String teacherNames;
    private String classroomName;
    private Integer topicCount;
    private String classNames;
    private List<Long> classIds;
    private List<Long> studentIds;
    private List<AdminTrainingRole> roles;
    private Integer participantCount;
    private Integer roomCount;
    private Integer startedRoomCount;
    private Double averageScore;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getAcademicYearName() {
        return academicYearName;
    }

    public void setAcademicYearName(String academicYearName) {
        this.academicYearName = academicYearName;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
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

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
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

    public LocalDateTime getExamStartedAt() { return examStartedAt; }
    public void setExamStartedAt(LocalDateTime examStartedAt) { this.examStartedAt = examStartedAt; }

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

    public Boolean getRecordingEnabled() {
        return recordingEnabled;
    }

    public void setRecordingEnabled(Boolean recordingEnabled) {
        this.recordingEnabled = recordingEnabled;
    }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public List<Long> getTeacherIds() { return teacherIds; }
    public void setTeacherIds(List<Long> teacherIds) { this.teacherIds = teacherIds; }
    public String getScoreBasis() { return scoreBasis; }
    public void setScoreBasis(String scoreBasis) { this.scoreBasis = scoreBasis; }
    public List<Long> getTopicIds() { return topicIds; }
    public void setTopicIds(List<Long> topicIds) { this.topicIds = topicIds; }
    public String getTeacherNames() { return teacherNames; }
    public void setTeacherNames(String teacherNames) { this.teacherNames = teacherNames; }
    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }
    public Integer getTopicCount() { return topicCount; }
    public void setTopicCount(Integer topicCount) { this.topicCount = topicCount; }

    public String getClassNames() {
        return classNames;
    }

    public void setClassNames(String classNames) {
        this.classNames = classNames;
    }

    public List<Long> getClassIds() {
        return classIds;
    }

    public void setClassIds(List<Long> classIds) {
        this.classIds = classIds;
    }

    public List<Long> getStudentIds() { return studentIds; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }

    public List<AdminTrainingRole> getRoles() {
        return roles;
    }

    public void setRoles(List<AdminTrainingRole> roles) {
        this.roles = roles;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public Integer getStartedRoomCount() {
        return startedRoomCount;
    }

    public void setStartedRoomCount(Integer startedRoomCount) {
        this.startedRoomCount = startedRoomCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
