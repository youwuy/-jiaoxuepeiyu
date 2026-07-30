package com.qizhifu.jiaoxuepeiyu.student.course.model;

public class StudentCourseItem {

    private Long contentId;
    private String itemType;
    private String title;
    private Long resourceId;
    private Long assignmentId;
    private int requiredDurationSeconds;
    private int studiedSeconds;
    private boolean completed;
    private int sortOrder;

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

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getRequiredDurationSeconds() {
        return requiredDurationSeconds;
    }

    public void setRequiredDurationSeconds(int requiredDurationSeconds) {
        this.requiredDurationSeconds = requiredDurationSeconds;
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
