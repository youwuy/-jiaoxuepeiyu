package com.qizhifu.jiaoxuepeiyu.student.course.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentCourseDetail {

    private Long courseId;
    private String courseName;
    private String status;
    private String academicTerm;
    private int progressPercent;
    private String teacherNames;
    private LocalDateTime openStartTime;
    private LocalDateTime openEndTime;
    private Long lastContentId;
    private List<StudentCourseChapter> chapters = new ArrayList<StudentCourseChapter>();

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        this.academicTerm = academicTerm;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getTeacherNames() {
        return teacherNames;
    }

    public void setTeacherNames(String teacherNames) {
        this.teacherNames = teacherNames;
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

    public Long getLastContentId() {
        return lastContentId;
    }

    public void setLastContentId(Long lastContentId) {
        this.lastContentId = lastContentId;
    }

    public List<StudentCourseChapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<StudentCourseChapter> chapters) {
        this.chapters = chapters;
    }
}
