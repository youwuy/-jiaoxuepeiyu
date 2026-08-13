package com.qizhifu.jiaoxuepeiyu.student.training.model;

public class TrainingRoomRole {

    private Long roleId;
    private String roleName;
    private boolean aiFillEnabled;
    private boolean claimed;
    private Long claimedByStudentId;

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

    public boolean isAiFillEnabled() { return aiFillEnabled; }
    public void setAiFillEnabled(boolean aiFillEnabled) { this.aiFillEnabled = aiFillEnabled; }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public Long getClaimedByStudentId() {
        return claimedByStudentId;
    }

    public void setClaimedByStudentId(Long claimedByStudentId) {
        this.claimedByStudentId = claimedByStudentId;
    }
}
