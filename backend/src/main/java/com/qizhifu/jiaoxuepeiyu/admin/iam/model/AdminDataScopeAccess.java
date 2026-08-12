package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminDataScopeAccess {

    private String dataScope;
    private List<Long> managedOrgIds = new ArrayList<Long>();

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public List<Long> getManagedOrgIds() {
        return managedOrgIds;
    }

    public void setManagedOrgIds(List<Long> managedOrgIds) {
        this.managedOrgIds = managedOrgIds == null ? new ArrayList<Long>() : managedOrgIds;
    }
}
