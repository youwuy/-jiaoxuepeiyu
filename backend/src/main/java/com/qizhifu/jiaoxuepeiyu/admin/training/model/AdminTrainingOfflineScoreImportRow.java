package com.qizhifu.jiaoxuepeiyu.admin.training.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminTrainingOfflineScoreImportRow {
    private Integer rowNumber;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String className;
    private BigDecimal totalScore;
    private String remark;
    private Map<Long, BigDecimal> topicScores = new LinkedHashMap<Long, BigDecimal>();

    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Map<Long, BigDecimal> getTopicScores() { return topicScores; }
    public void setTopicScores(Map<Long, BigDecimal> topicScores) {
        this.topicScores = topicScores == null ? new LinkedHashMap<Long, BigDecimal>() : topicScores;
    }
}
