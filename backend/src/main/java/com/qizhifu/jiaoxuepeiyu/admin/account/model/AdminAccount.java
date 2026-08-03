package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminAccount {

    private Long userId;
    private String accountNo;
    private String realName;
    private String phone;
    private String maskedPhone;
    private String idCard;
    private String maskedIdCard;
    private String jobTitle;
    private String userType;
    private Long orgId;
    private String orgName;
    private Long classId;
    private String className;
    private boolean enabled;
    private boolean faceRecorded;
    private boolean fingerprintRecorded;
    private LocalDateTime createdAt;
    private List<Long> roleIds = new ArrayList<Long>();
    private List<String> roleNames = new ArrayList<String>();
    private List<Long> managedOrgIds = new ArrayList<Long>();
    private List<Long> teachingClassIds = new ArrayList<Long>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMaskedPhone() {
        return maskedPhone;
    }

    public void setMaskedPhone(String maskedPhone) {
        this.maskedPhone = maskedPhone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getMaskedIdCard() {
        return maskedIdCard;
    }

    public void setMaskedIdCard(String maskedIdCard) {
        this.maskedIdCard = maskedIdCard;
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

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFaceRecorded() {
        return faceRecorded;
    }

    public void setFaceRecorded(boolean faceRecorded) {
        this.faceRecorded = faceRecorded;
    }

    public boolean isFingerprintRecorded() {
        return fingerprintRecorded;
    }

    public void setFingerprintRecorded(boolean fingerprintRecorded) {
        this.fingerprintRecorded = fingerprintRecorded;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds == null ? new ArrayList<Long>() : roleIds;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames == null ? new ArrayList<String>() : roleNames;
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
