package com.qizhifu.jiaoxuepeiyu.admin.score.model;

public class AdminSemesterScoreStatistics {

    private Integer studentCount;
    private Double averageScore;
    private Double maxScore;
    private Double minScore;
    private Integer excellentCount;
    private Integer passCount;

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public Integer getExcellentCount() {
        return excellentCount;
    }

    public void setExcellentCount(Integer excellentCount) {
        this.excellentCount = excellentCount;
    }

    public Integer getPassCount() {
        return passCount;
    }

    public void setPassCount(Integer passCount) {
        this.passCount = passCount;
    }
}
