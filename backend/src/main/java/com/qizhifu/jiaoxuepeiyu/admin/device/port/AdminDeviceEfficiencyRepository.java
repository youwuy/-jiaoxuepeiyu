package com.qizhifu.jiaoxuepeiyu.admin.device.port;

import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyQuery;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencySummary;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceHeatRank;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceMonthlyTrend;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceRealtimeState;
import java.util.List;

public interface AdminDeviceEfficiencyRepository {

    AdminDeviceEfficiencySummary findSummary(AdminDeviceEfficiencyQuery query);

    List<AdminDeviceRealtimeState> findRealtimeStates(AdminDeviceEfficiencyQuery query);

    List<AdminDeviceMonthlyTrend> findMonthlyTrends(AdminDeviceEfficiencyQuery query);

    List<AdminDeviceHeatRank> findHeatRanking(AdminDeviceEfficiencyQuery query);
}
