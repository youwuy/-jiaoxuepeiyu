package com.qizhifu.jiaoxuepeiyu.admin.device.repository;

import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyQuery;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencySummary;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceHeatRank;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceMonthlyTrend;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceRealtimeState;
import com.qizhifu.jiaoxuepeiyu.admin.device.port.AdminDeviceEfficiencyRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminDeviceEfficiencyRepository implements AdminDeviceEfficiencyRepository {

    private final AdminDeviceEfficiencyMapper mapper;

    public MyBatisAdminDeviceEfficiencyRepository(AdminDeviceEfficiencyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AdminDeviceEfficiencySummary findSummary(AdminDeviceEfficiencyQuery query) {
        return mapper.findSummary(query);
    }

    @Override
    public List<AdminDeviceRealtimeState> findRealtimeStates(AdminDeviceEfficiencyQuery query) {
        return mapper.findRealtimeStates(query);
    }

    @Override
    public List<AdminDeviceMonthlyTrend> findMonthlyTrends(AdminDeviceEfficiencyQuery query) {
        return mapper.findMonthlyTrends(query);
    }

    @Override
    public List<AdminDeviceHeatRank> findHeatRanking(AdminDeviceEfficiencyQuery query) {
        return mapper.findHeatRanking(query);
    }
}
