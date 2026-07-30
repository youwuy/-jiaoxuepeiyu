package com.qizhifu.jiaoxuepeiyu.admin.device.model;

public class AdminDeviceMonthlyTrend {

    private String month;
    private Integer usageMinutes;
    private Integer usageCount;
    private Double utilizationRate;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getUsageMinutes() {
        return usageMinutes;
    }

    public void setUsageMinutes(Integer usageMinutes) {
        this.usageMinutes = usageMinutes;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Double getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(Double utilizationRate) {
        this.utilizationRate = utilizationRate;
    }
}
