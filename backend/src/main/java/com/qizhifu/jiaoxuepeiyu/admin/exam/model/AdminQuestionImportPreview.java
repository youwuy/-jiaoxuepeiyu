package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminQuestionImportPreview {

    private String fileName;
    private Long fileSize;
    private Integer validCount;
    private Integer errorCount;
    private List<AdminQuestionImportRow> validRows = new ArrayList<AdminQuestionImportRow>();
    private List<AdminQuestionImportError> errors = new ArrayList<AdminQuestionImportError>();

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

    public List<AdminQuestionImportRow> getValidRows() {
        return validRows;
    }

    public void setValidRows(List<AdminQuestionImportRow> validRows) {
        this.validRows = validRows;
    }

    public List<AdminQuestionImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<AdminQuestionImportError> errors) {
        this.errors = errors;
    }
}
