package com.qizhifu.jiaoxuepeiyu.admin.org.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AdminOrgCommand {

    private Long parentId;
    @NotBlank
    @Size(max = 20)
    private String orgName;
    private int sortOrder;

    public AdminOrgCommand() {
    }

    public AdminOrgCommand(Long parentId, String orgName, int sortOrder) {
        this.parentId = parentId;
        this.orgName = orgName;
        this.sortOrder = sortOrder;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
