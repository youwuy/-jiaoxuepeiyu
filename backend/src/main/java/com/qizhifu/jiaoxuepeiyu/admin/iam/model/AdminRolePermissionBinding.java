package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

public class AdminRolePermissionBinding {

    private Long permissionId;
    private String dataScope;

    public AdminRolePermissionBinding(Long permissionId, String dataScope) {
        this.permissionId = permissionId;
        this.dataScope = dataScope;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public String getDataScope() {
        return dataScope;
    }
}
