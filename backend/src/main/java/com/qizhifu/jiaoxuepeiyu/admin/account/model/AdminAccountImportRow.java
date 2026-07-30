package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountImportRow {

    private Integer rowNo;
    private String accountNo;
    private String realName;
    private String phone;
    private String idCard;
    private String jobTitle;
    private Long orgId;
    private Long classId;
    private List<Long> roleIds = new ArrayList<Long>();
    private List<Long> managedOrgIds = new ArrayList<Long>();
    private List<Long> teachingClassIds = new ArrayList<Long>();
    private Boolean valid;
    private List<String> errors = new ArrayList<String>();

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
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

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors == null ? new ArrayList<String>() : errors;
    }
}
