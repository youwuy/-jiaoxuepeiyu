package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingRole {

    private Long roleId;
    private Long trainingId;
    private Long topicId;
    private String roleName;
    private Boolean aiFillEnabled;
    private Integer sortOrder;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Boolean getAiFillEnabled() { return aiFillEnabled; }
    public void setAiFillEnabled(Boolean aiFillEnabled) { this.aiFillEnabled = aiFillEnabled; }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
