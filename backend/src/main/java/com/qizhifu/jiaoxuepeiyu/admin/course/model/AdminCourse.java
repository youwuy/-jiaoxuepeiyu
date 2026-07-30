package com.qizhifu.jiaoxuepeiyu.admin.course.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class AdminCourse {

    private Long courseId;
    private String courseName;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterName;
    private String academicTerm;
    private Long majorId;
    private String majorName;
    private Long classId;
    private String coverUrl;
    private String teacherNames;
    private String classNames;
    private String learningMode;
    private String assignmentCompletionRule;
    private Integer coursewareScoreCap;
    private Integer coursewareCount;
    private Integer assignmentCount;
    private String publishStatus;
    private LocalDateTime openStartTime;
    private LocalDateTime openEndTime;
    private Long createdBy;
    private String creatorName;
    private Date createdAt;
    private Date updatedAt;
    private List<Long> teacherIds;
    private List<Long> classIds;
    private List<AdminCourseChapter> chapters;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

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

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        this.academicTerm = academicTerm;
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

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getTeacherNames() {
        return teacherNames;
    }

    public void setTeacherNames(String teacherNames) {
        this.teacherNames = teacherNames;
    }

    public String getClassNames() {
        return classNames;
    }

    public void setClassNames(String classNames) {
        this.classNames = classNames;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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

    public List<AdminCourseChapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<AdminCourseChapter> chapters) {
        this.chapters = chapters;
    }
}
