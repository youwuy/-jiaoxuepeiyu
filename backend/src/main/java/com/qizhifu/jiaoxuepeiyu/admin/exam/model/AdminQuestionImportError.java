package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

public class AdminQuestionImportError {

    private Integer rowNumber;
    private String message;

    public AdminQuestionImportError() {
    }

    public AdminQuestionImportError(Integer rowNumber, String message) {
        this.rowNumber = rowNumber;
        this.message = message;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
