package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingRoleCommand {

    private Long topicId;
    private String roleName;
    private Boolean aiFillEnabled;
    private Integer sortOrder;

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

    public AdminTrainingRole toRole() {
        AdminTrainingRole role = new AdminTrainingRole();
        role.setTopicId(topicId);
        role.setRoleName(roleName);
        role.setAiFillEnabled(Boolean.TRUE.equals(aiFillEnabled));
        role.setSortOrder(sortOrder);
        return role;
    }
}
