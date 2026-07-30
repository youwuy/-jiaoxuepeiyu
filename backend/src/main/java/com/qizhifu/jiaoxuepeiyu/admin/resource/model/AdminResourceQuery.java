package com.qizhifu.jiaoxuepeiyu.admin.resource.model;

public class AdminResourceQuery {

    private String keyword;
    private String resourceType;
    private Long majorId;
    private String courseName;
    private Long uploaderId;
    private String publicStatus;
    private String uploadStartDate;
    private String uploadEndDate;
    private int page = 1;
    private int pageSize = 20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getPublicStatus() {
        return publicStatus;
    }

    public void setPublicStatus(String publicStatus) {
        this.publicStatus = publicStatus;
    }

    public String getUploadStartDate() {
        return uploadStartDate;
    }

    public void setUploadStartDate(String uploadStartDate) {
        this.uploadStartDate = uploadStartDate;
    }

    public String getUploadEndDate() {
        return uploadEndDate;
    }

    public void setUploadEndDate(String uploadEndDate) {
        this.uploadEndDate = uploadEndDate;
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
