package com.qizhifu.jiaoxuepeiyu.admin.device.model;

import java.util.Collections;
import java.util.List;

public class AdminDeviceEfficiencyReport {

    private AdminDeviceEfficiencySummary summary;
    private List<AdminDeviceRealtimeState> realtimeStates = Collections.emptyList();
    private List<AdminDeviceMonthlyTrend> monthlyTrends = Collections.emptyList();
    private List<AdminDeviceHeatRank> heatRanking = Collections.emptyList();

    public AdminDeviceEfficiencySummary getSummary() {
        return summary;
    }

    public void setSummary(AdminDeviceEfficiencySummary summary) {
        this.summary = summary;
    }

    public List<AdminDeviceRealtimeState> getRealtimeStates() {
        return realtimeStates;
    }

    public void setRealtimeStates(List<AdminDeviceRealtimeState> realtimeStates) {
        this.realtimeStates = realtimeStates == null
                ? Collections.<AdminDeviceRealtimeState>emptyList() : realtimeStates;
    }

    public List<AdminDeviceMonthlyTrend> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(List<AdminDeviceMonthlyTrend> monthlyTrends) {
        this.monthlyTrends = monthlyTrends == null
                ? Collections.<AdminDeviceMonthlyTrend>emptyList() : monthlyTrends;
    }

    public List<AdminDeviceHeatRank> getHeatRanking() {
        return heatRanking;
    }

    public void setHeatRanking(List<AdminDeviceHeatRank> heatRanking) {
        this.heatRanking = heatRanking == null ? Collections.<AdminDeviceHeatRank>emptyList() : heatRanking;
    }
}
