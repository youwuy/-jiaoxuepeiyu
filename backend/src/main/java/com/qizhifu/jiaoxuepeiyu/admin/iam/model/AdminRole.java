package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminRole {

    private Long roleId;
    private String roleName;
    private String roleCode;
    private String dataScope;
    private String remark;
    private Boolean enabled;
    private Integer userCount;
    private List<Long> permissionIds = new ArrayList<Long>();
    private List<AdminRolePageDataScope> pageDataScopes = new ArrayList<AdminRolePageDataScope>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
