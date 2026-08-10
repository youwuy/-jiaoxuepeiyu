package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

public class AdminRolePageDataScope {

    private Long pagePermissionId;
    private String dataScope;

    public AdminRolePageDataScope() {
    }

    public AdminRolePageDataScope(Long pagePermissionId, String dataScope) {
        this.pagePermissionId = pagePermissionId;
        this.dataScope = dataScope;
    }

    public Long getPagePermissionId() {
        return pagePermissionId;
    }

    public void setPagePermissionId(Long pagePermissionId) {
        this.pagePermissionId = pagePermissionId;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }
}
