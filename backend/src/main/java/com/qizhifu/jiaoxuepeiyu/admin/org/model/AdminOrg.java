package com.qizhifu.jiaoxuepeiyu.admin.org.model;

import java.util.ArrayList;
import java.util.List;

public class AdminOrg {

    private Long orgId;
    private Long parentId;
    private String orgName;
    private int sortOrder;
    private boolean enabled;
    private List<AdminOrg> children = new ArrayList<AdminOrg>();

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<AdminOrg> getChildren() {
        return children;
    }

    public void setChildren(List<AdminOrg> children) {
        this.children = children == null ? new ArrayList<AdminOrg>() : children;
    }
}
