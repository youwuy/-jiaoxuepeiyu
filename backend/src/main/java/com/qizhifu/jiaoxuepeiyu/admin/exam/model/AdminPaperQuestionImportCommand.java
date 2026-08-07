package com.qizhifu.jiaoxuepeiyu.admin.exam.model;

import java.util.List;

public class AdminPaperQuestionImportCommand {

    private String paperName;
    private String courseName;
    private String fileName;
    private Long fileSize;
    private List<AdminQuestionImportRow> rows;

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

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

    public List<AdminQuestionImportRow> getRows() {
        return rows;
    }

    public void setRows(List<AdminQuestionImportRow> rows) {
        this.rows = rows;
    }
}
