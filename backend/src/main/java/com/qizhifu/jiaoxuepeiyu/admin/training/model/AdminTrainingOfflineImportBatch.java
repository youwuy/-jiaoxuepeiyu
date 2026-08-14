package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingOfflineImportBatch {
    private Long batchId;
    private Long trainingId;
    private String fileName;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private Long importedBy;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }
    public Long getImportedBy() { return importedBy; }
    public void setImportedBy(Long importedBy) { this.importedBy = importedBy; }
}
