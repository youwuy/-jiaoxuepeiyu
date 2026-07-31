package com.qizhifu.jiaoxuepeiyu.student.course.model;

import java.time.LocalDateTime;

public class StudentCourseContentRecord {

    private Long chapterId;
    private String chapterTitle;
    private int chapterSortOrder;
    private Long contentId;
    private String itemType;
    private String title;
    private Long assignmentId;
    private Long resourceId;
    private int requiredDurationSeconds;
    private LocalDateTime learningStartTime;
    private LocalDateTime learningEndTime;
    private int studiedSeconds;
    private boolean completed;
    private int sortOrder;

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public int getChapterSortOrder() {
        return chapterSortOrder;
    }

    public void setChapterSortOrder(int chapterSortOrder) {
        this.chapterSortOrder = chapterSortOrder;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public int getRequiredDurationSeconds() {
        return requiredDurationSeconds;
    }

    public void setRequiredDurationSeconds(int requiredDurationSeconds) {
        this.requiredDurationSeconds = requiredDurationSeconds;
    }

    public LocalDateTime getLearningStartTime() {
        return learningStartTime;
    }

    public void setLearningStartTime(LocalDateTime learningStartTime) {
        this.learningStartTime = learningStartTime;
    }

    public LocalDateTime getLearningEndTime() {
        return learningEndTime;
    }

    public void setLearningEndTime(LocalDateTime learningEndTime) {
        this.learningEndTime = learningEndTime;
    }

    public int getStudiedSeconds() {
        return studiedSeconds;
    }

    public void setStudiedSeconds(int studiedSeconds) {
        this.studiedSeconds = studiedSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
