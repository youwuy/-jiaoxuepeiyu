package com.qizhifu.jiaoxuepeiyu.admin.course.model;

public class AdminCourseContentCommand {

    private String itemType;
    private String title;
    private Long resourceId;
    private Long assignmentId;
    private Integer requiredDurationSeconds;
    private Integer sortOrder;

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

    public Integer getRequiredDurationSeconds() {
        return requiredDurationSeconds;
    }

    public void setRequiredDurationSeconds(Integer requiredDurationSeconds) {
        this.requiredDurationSeconds = requiredDurationSeconds;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
