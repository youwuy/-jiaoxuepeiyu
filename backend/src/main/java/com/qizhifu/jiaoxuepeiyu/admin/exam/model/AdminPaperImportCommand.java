package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.List;

public class AdminPaperImportCommand {

    private String fileName;
    private Long fileSize;
    private List<AdminPaperImportRow> rows;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public List<AdminPaperImportRow> getRows() {
        return rows;
    }

    public void setRows(List<AdminPaperImportRow> rows) {
        this.rows = rows;
    }
}
