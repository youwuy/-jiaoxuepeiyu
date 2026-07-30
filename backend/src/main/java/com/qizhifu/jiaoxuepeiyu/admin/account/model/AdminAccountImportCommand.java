package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountImportCommand {

    private List<AdminAccountImportRow> rows = new ArrayList<AdminAccountImportRow>();

    public List<AdminAccountImportRow> getRows() {
        return rows;
    }

    public void setRows(List<AdminAccountImportRow> rows) {
        this.rows = rows == null ? new ArrayList<AdminAccountImportRow>() : rows;
    }
}
