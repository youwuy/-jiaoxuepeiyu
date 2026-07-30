package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.time.LocalDateTime;
import java.util.List;

public class AdminCourseCommand {

    private String courseName;
    private Long academicYearId;
    private Long semesterId;
    private Long majorId;
    private String coverUrl;
    private LocalDateTime openStartTime;
    private LocalDateTime openEndTime;
    private List<Long> teacherIds;
    private List<Long> classIds;
    private String learningMode;
    private String assignmentCompletionRule;
    private Integer coursewareScoreCap;
    private String publishStatus;
    private Integer coursewareCount;
    private Integer assignmentCount;
    private List<AdminCourseChapterCommand> chapters;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public List<Long> getTeacherIds() {
        return teacherIds;
    }

    public void setTeacherIds(List<Long> teacherIds) {
        this.teacherIds = teacherIds;
    }

    public List<Long> getClassIds() {
        return classIds;
    }

    public void setClassIds(List<Long> classIds) {
        this.classIds = classIds;
    }

    public String getLearningMode() {
        return learningMode;
    }

    public void setLearningMode(String learningMode) {
        this.learningMode = learningMode;
    }

    public String getAssignmentCompletionRule() {
        return assignmentCompletionRule;
    }

    public void setAssignmentCompletionRule(String assignmentCompletionRule) {
        this.assignmentCompletionRule = assignmentCompletionRule;
    }

    public Integer getCoursewareScoreCap() {
        return coursewareScoreCap;
    }

    public void setCoursewareScoreCap(Integer coursewareScoreCap) {
        this.coursewareScoreCap = coursewareScoreCap;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    public Integer getCoursewareCount() {
        return coursewareCount;
    }

    public void setCoursewareCount(Integer coursewareCount) {
        this.coursewareCount = coursewareCount;
    }

    public Integer getAssignmentCount() {
        return assignmentCount;
    }

    public void setAssignmentCount(Integer assignmentCount) {
        this.assignmentCount = assignmentCount;
    }

    public List<AdminCourseChapterCommand> getChapters() {
        return chapters;
    }

    public void setChapters(List<AdminCourseChapterCommand> chapters) {
        this.chapters = chapters;
    }
}
