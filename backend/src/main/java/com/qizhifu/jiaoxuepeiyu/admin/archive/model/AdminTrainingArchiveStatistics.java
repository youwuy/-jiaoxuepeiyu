package com.qizhifu.jiaoxuepeiyu.admin.archive.model;

public class AdminTrainingArchiveStatistics {

    private Integer archiveCount;
    private Integer normalSubmitCount;
    private Integer abnormalSubmitCount;
    private Integer roomDissolvedCount;
    private Double averagePersonalScore;
    private Double averageDurationSeconds;

    public Integer getArchiveCount() {
        return archiveCount;
    }

    public void setArchiveCount(Integer archiveCount) {
        this.archiveCount = archiveCount;
    }

    public Integer getNormalSubmitCount() {
        return normalSubmitCount;
    }

    public void setNormalSubmitCount(Integer normalSubmitCount) {
        this.normalSubmitCount = normalSubmitCount;
    }

    public Integer getAbnormalSubmitCount() {
        return abnormalSubmitCount;
    }

    public void setAbnormalSubmitCount(Integer abnormalSubmitCount) {
        this.abnormalSubmitCount = abnormalSubmitCount;
    }

    public Integer getRoomDissolvedCount() {
        return roomDissolvedCount;
    }

    public void setRoomDissolvedCount(Integer roomDissolvedCount) {
        this.roomDissolvedCount = roomDissolvedCount;
    }

    public Double getAveragePersonalScore() {
        return averagePersonalScore;
    }

    public void setAveragePersonalScore(Double averagePersonalScore) {
        this.averagePersonalScore = averagePersonalScore;
    }

    public Double getAverageDurationSeconds() {
        return averageDurationSeconds;
    }

    public void setAverageDurationSeconds(Double averageDurationSeconds) {
        this.averageDurationSeconds = averageDurationSeconds;
    }
}
