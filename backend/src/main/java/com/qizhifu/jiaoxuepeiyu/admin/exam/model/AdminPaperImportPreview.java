package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminPaperImportPreview {

    private String fileName;
    private Long fileSize;
    private Integer validCount;
    private Integer errorCount;
    private List<AdminPaperImportRow> validRows = new ArrayList<AdminPaperImportRow>();
    private List<AdminPaperImportError> errors = new ArrayList<AdminPaperImportError>();

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

    public List<AdminPaperImportRow> getValidRows() {
        return validRows;
    }

    public void setValidRows(List<AdminPaperImportRow> validRows) {
        this.validRows = validRows;
    }

    public List<AdminPaperImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<AdminPaperImportError> errors) {
        this.errors = errors;
    }
}
