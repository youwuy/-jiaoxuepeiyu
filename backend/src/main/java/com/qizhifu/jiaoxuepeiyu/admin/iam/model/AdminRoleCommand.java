package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminRoleCommand {

    private String roleName;
    private String roleCode;
    private String dataScope;
    private String remark;
    private List<Long> permissionIds = new ArrayList<Long>();
    private List<AdminRolePageDataScope> pageDataScopes = new ArrayList<AdminRolePageDataScope>();

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds == null ? new ArrayList<Long>() : permissionIds;
    }

    public List<AdminRolePageDataScope> getPageDataScopes() {
        return pageDataScopes;
    }

    public void setPageDataScopes(List<AdminRolePageDataScope> pageDataScopes) {
        this.pageDataScopes = pageDataScopes == null
                ? new ArrayList<AdminRolePageDataScope>() : pageDataScopes;
    }
}
