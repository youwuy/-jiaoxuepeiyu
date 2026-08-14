package com.qizhifu.jiaoxuepeiyu.admin.training.model;

import java.util.ArrayList;
import java.util.List;

public class AdminTrainingOfflineScoreImportResult {
    private Long batchId;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private List<RowError> errors = new ArrayList<RowError>();

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }
    public List<RowError> getErrors() { return errors; }
    public void setErrors(List<RowError> errors) { this.errors = errors == null ? new ArrayList<RowError>() : errors; }

    public static class RowError {
        private Integer rowNumber;
        private String studentNo;
        private String message;

        public RowError() { }
        public RowError(Integer rowNumber, String studentNo, String message) {
            this.rowNumber = rowNumber;
            this.studentNo = studentNo;
            this.message = message;
        }
        public Integer getRowNumber() { return rowNumber; }
        public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
