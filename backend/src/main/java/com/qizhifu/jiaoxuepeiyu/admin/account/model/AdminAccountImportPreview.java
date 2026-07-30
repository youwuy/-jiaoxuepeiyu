package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountImportPreview {

    private Integer totalCount;
    private Integer validCount;
    private Integer errorCount;
    private List<AdminAccountImportRow> rows = new ArrayList<AdminAccountImportRow>();

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getValidCount() {
        return validCount;
    }

    public void setValidCount(Integer validCount) {
        this.validCount = validCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public List<AdminAccountImportRow> getRows() {
        return rows;
    }

    public void setRows(List<AdminAccountImportRow> rows) {
        this.rows = rows == null ? new ArrayList<AdminAccountImportRow>() : rows;
    }
}
