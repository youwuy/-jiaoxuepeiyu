package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountImportResult {

    private Integer importedCount;
    private List<Long> userIds = new ArrayList<Long>();

    public Integer getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(Integer importedCount) {
        this.importedCount = importedCount;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds == null ? new ArrayList<Long>() : userIds;
    }
}
