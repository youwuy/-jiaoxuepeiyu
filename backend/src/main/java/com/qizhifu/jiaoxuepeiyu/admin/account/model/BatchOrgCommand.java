package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.util.ArrayList;
import java.util.List;

public class BatchOrgCommand {

    private List<Long> userIds = new ArrayList<Long>();
    private Long orgId;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds == null ? new ArrayList<Long>() : userIds;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }
}
