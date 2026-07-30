package com.qizhifu.jiaoxuepeiyu.admin.score.model;

import java.util.ArrayList;
import java.util.List;

public class AdminSemesterScoreImportCommand {

    private List<AdminSemesterScoreImportRow> rows = new ArrayList<AdminSemesterScoreImportRow>();

    public List<AdminSemesterScoreImportRow> getRows() {
        return rows;
    }

    public void setRows(List<AdminSemesterScoreImportRow> rows) {
        this.rows = rows == null ? new ArrayList<AdminSemesterScoreImportRow>() : rows;
    }
}
