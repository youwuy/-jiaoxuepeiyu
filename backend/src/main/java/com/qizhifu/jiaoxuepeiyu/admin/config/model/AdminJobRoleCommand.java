package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import javax.validation.constraints.NotBlank;

public class AdminJobRoleCommand {

    @NotBlank
    private String roleName;
    private Integer sortOrder;

    public AdminJobRoleCommand() {
    }

    public AdminJobRoleCommand(String roleName, Integer sortOrder) {
        this.roleName = roleName;
        this.sortOrder = sortOrder;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
