package com.qizhifu.jiaoxuepeiyu.admin.device.model;

public class AdminDeviceEfficiencySummary {

    private Integer totalDeviceCount;
    private Integer onlineDeviceCount;
    private Integer activeDeviceCount;
    private Integer faultDeviceCount;
    private Integer totalUsageMinutes;
    private Double averageUtilizationRate;
    private Integer activeTrainingCount;

    public Integer getTotalDeviceCount() {
        return totalDeviceCount;
    }

    public void setTotalDeviceCount(Integer totalDeviceCount) {
        this.totalDeviceCount = totalDeviceCount;
    }

    public Integer getOnlineDeviceCount() {
        return onlineDeviceCount;
    }

    public void setOnlineDeviceCount(Integer onlineDeviceCount) {
        this.onlineDeviceCount = onlineDeviceCount;
    }

    public Integer getActiveDeviceCount() {
        return activeDeviceCount;
    }

    public void setActiveDeviceCount(Integer activeDeviceCount) {
        this.activeDeviceCount = activeDeviceCount;
    }

    public Integer getFaultDeviceCount() {
        return faultDeviceCount;
    }

    public void setFaultDeviceCount(Integer faultDeviceCount) {
        this.faultDeviceCount = faultDeviceCount;
    }

    public Integer getTotalUsageMinutes() {
        return totalUsageMinutes;
    }

    public void setTotalUsageMinutes(Integer totalUsageMinutes) {
        this.totalUsageMinutes = totalUsageMinutes;
    }

    public Double getAverageUtilizationRate() {
        return averageUtilizationRate;
    }

    public void setAverageUtilizationRate(Double averageUtilizationRate) {
        this.averageUtilizationRate = averageUtilizationRate;
    }

    public Integer getActiveTrainingCount() {
        return activeTrainingCount;
    }

    public void setActiveTrainingCount(Integer activeTrainingCount) {
        this.activeTrainingCount = activeTrainingCount;
    }
}
