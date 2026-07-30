package com.qizhifu.jiaoxuepeiyu.admin.device.controller;

import com.qizhifu.jiaoxuepeiyu.admin.device.AdminDeviceEfficiencyService;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyQuery;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyReport;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencySummary;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceHeatRank;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceMonthlyTrend;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceRealtimeState;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/devices/efficiency")
@Tag(name = "Admin Device Efficiency", description = "Device usage dashboard, real-time state, trends, utilization, and heat ranking APIs.")
public class AdminDeviceEfficiencyController {

    private final AdminDeviceEfficiencyService service;

    public AdminDeviceEfficiencyController(AdminDeviceEfficiencyService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get device efficiency dashboard", description = "Returns summary metrics, real-time device states, monthly trends, and heat ranking for the selected date range.")
    public ApiResponse<AdminDeviceEfficiencyReport> getDashboard(@ModelAttribute AdminDeviceEfficiencyQuery query) {
        return ApiResponse.ok(service.getDashboard(query));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get device efficiency summary", description = "Returns device totals, online count, active count, cumulative usage minutes, utilization rate, and active training count.")
    public ApiResponse<AdminDeviceEfficiencySummary> getSummary(@ModelAttribute AdminDeviceEfficiencyQuery query) {
        return ApiResponse.ok(service.getSummary(query));
    }

    @GetMapping("/realtime")
    @Operation(summary = "List real-time device states", description = "Returns current classroom, device status, active training, active student, and heartbeat metadata.")
    public ApiResponse<List<AdminDeviceRealtimeState>> listRealtimeStates(@ModelAttribute AdminDeviceEfficiencyQuery query) {
        return ApiResponse.ok(service.listRealtimeStates(query));
    }

    @GetMapping("/monthly-trends")
    @Operation(summary = "List monthly device usage trends", description = "Returns usage minutes, usage count, and utilization rate grouped by month.")
    public ApiResponse<List<AdminDeviceMonthlyTrend>> listMonthlyTrends(@ModelAttribute AdminDeviceEfficiencyQuery query) {
        return ApiResponse.ok(service.listMonthlyTrends(query));
    }

    @GetMapping("/heat-ranking")
    @Operation(summary = "List device heat ranking", description = "Returns devices ranked by usage minutes within the selected date range.")
    public ApiResponse<List<AdminDeviceHeatRank>> listHeatRanking(@ModelAttribute AdminDeviceEfficiencyQuery query) {
        return ApiResponse.ok(service.listHeatRanking(query));
    }
}
