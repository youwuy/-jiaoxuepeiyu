package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountCommand {

    private Long userId;
    private String realName;
    private String accountNo;
    private String phone;
    private String idCard;
    private String jobTitle;
    private String userType;
    private Long orgId;
    private Long classId;
    private Long faceFileId;
    private Long fingerprintFileId;
    private String initialPassword;
    private List<Long> roleIds = new ArrayList<Long>();
    private List<Long> managedOrgIds = new ArrayList<Long>();
    private List<Long> teachingClassIds = new ArrayList<Long>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getFaceFileId() {
        return faceFileId;
    }

    public void setFaceFileId(Long faceFileId) {
        this.faceFileId = faceFileId;
    }

    public Long getFingerprintFileId() {
        return fingerprintFileId;
    }

    public void setFingerprintFileId(Long fingerprintFileId) {
        this.fingerprintFileId = fingerprintFileId;
    }

    public String getInitialPassword() {
        return initialPassword;
    }

    public void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds == null ? new ArrayList<Long>() : roleIds;
    }

    public List<Long> getManagedOrgIds() {
        return managedOrgIds;
    }

    public void setManagedOrgIds(List<Long> managedOrgIds) {
        this.managedOrgIds = managedOrgIds == null ? new ArrayList<Long>() : managedOrgIds;
    }

    public List<Long> getTeachingClassIds() {
        return teachingClassIds;
    }

    public void setTeachingClassIds(List<Long> teachingClassIds) {
        this.teachingClassIds = teachingClassIds == null ? new ArrayList<Long>() : teachingClassIds;
    }
}
