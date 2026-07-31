package com.qizhifu.jiaoxuepeiyu.admin.score.model;

import java.util.ArrayList;
import java.util.List;

public class AdminSemesterScoreImportPreview {

    private Integer totalCount;
    private Integer validCount;
    private Integer errorCount;
    private List<AdminSemesterScoreImportRow> rows = new ArrayList<AdminSemesterScoreImportRow>();

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

    public List<AdminSemesterScoreImportRow> getRows() {
        return rows;
    }

    public void setRows(List<AdminSemesterScoreImportRow> rows) {
        this.rows = rows == null ? new ArrayList<AdminSemesterScoreImportRow>() : rows;
    }
}
