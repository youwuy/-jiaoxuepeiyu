package com.qizhifu.jiaoxuepeiyu.admin.training.model;

import java.util.ArrayList;
import java.util.List;

public class AdminTrainingOfflineScoreImportCommand {
    private Long trainingId;
    private String fileName;
    private List<AdminTrainingOfflineScoreImportRow> rows = new ArrayList<AdminTrainingOfflineScoreImportRow>();

    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public List<AdminTrainingOfflineScoreImportRow> getRows() { return rows; }
    public void setRows(List<AdminTrainingOfflineScoreImportRow> rows) {
        this.rows = rows == null ? new ArrayList<AdminTrainingOfflineScoreImportRow>() : rows;
    }
}
