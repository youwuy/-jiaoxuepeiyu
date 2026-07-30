package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingRoleCommand {

    private String roleName;
    private Integer sortOrder;

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

    public AdminTrainingRole toRole() {
        AdminTrainingRole role = new AdminTrainingRole();
        role.setRoleName(roleName);
        role.setSortOrder(sortOrder);
        return role;
    }
}
