package com.qizhifu.jiaoxuepeiyu.admin.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyQuery;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyReport;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencySummary;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceHeatRank;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceMonthlyTrend;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceRealtimeState;
import com.qizhifu.jiaoxuepeiyu.admin.device.port.AdminDeviceEfficiencyRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminDeviceEfficiencyServiceTests {

    @Test
    void returnsDashboardWithDefaultCurrentMonthWindow() {
        FakeDeviceEfficiency repository = new FakeDeviceEfficiency();
        AdminDeviceEfficiencyService service = new AdminDeviceEfficiencyService(repository, fixedClock());

        AdminDeviceEfficiencyReport report = service.getDashboard(new AdminDeviceEfficiencyQuery());

        assertEquals(LocalDate.of(2026, 7, 1), repository.lastQuery.getStartDate());
        assertEquals(LocalDate.of(2026, 7, 30), repository.lastQuery.getEndDate());
        assertEquals(10, report.getSummary().getTotalDeviceCount().intValue());
        assertEquals(1, report.getRealtimeStates().size());
        assertEquals(1, report.getMonthlyTrends().size());
        assertEquals(1, report.getHeatRanking().size());
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        AdminDeviceEfficiencyService service = new AdminDeviceEfficiencyService(new FakeDeviceEfficiency(), fixedClock());
        AdminDeviceEfficiencyQuery query = new AdminDeviceEfficiencyQuery();
        query.setStartDate(LocalDate.of(2026, 8, 1));
        query.setEndDate(LocalDate.of(2026, 7, 1));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.getDashboard(query));

        assertEquals("Device efficiency end date must not be before start date", exception.getMessage());
    }

    @Test
    void rejectsQueryWindowLongerThanOneYear() {
        AdminDeviceEfficiencyService service = new AdminDeviceEfficiencyService(new FakeDeviceEfficiency(), fixedClock());
        AdminDeviceEfficiencyQuery query = new AdminDeviceEfficiencyQuery();
        query.setStartDate(LocalDate.of(2025, 1, 1));
        query.setEndDate(LocalDate.of(2026, 7, 30));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.getSummary(query));

        assertEquals("Device efficiency date range cannot exceed 366 days", exception.getMessage());
    }

    @Test
    void defaultsEmptySummaryNumbers() {
        FakeDeviceEfficiency repository = new FakeDeviceEfficiency();
        repository.summary = null;
        AdminDeviceEfficiencyService service = new AdminDeviceEfficiencyService(repository, fixedClock());

        AdminDeviceEfficiencySummary summary = service.getSummary(new AdminDeviceEfficiencyQuery());

        assertEquals(0, summary.getTotalDeviceCount().intValue());
        assertEquals(0, summary.getAverageUtilizationRate().doubleValue(), 0.001);
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-07-30T10:00:00Z"), ZoneId.of("UTC"));
    }

    private static class FakeDeviceEfficiency implements AdminDeviceEfficiencyRepository {
        private AdminDeviceEfficiencyQuery lastQuery;
        private AdminDeviceEfficiencySummary summary = summary();

        @Override
        public AdminDeviceEfficiencySummary findSummary(AdminDeviceEfficiencyQuery query) {
            this.lastQuery = query;
            return summary;
        }

        @Override
        public List<AdminDeviceRealtimeState> findRealtimeStates(AdminDeviceEfficiencyQuery query) {
            this.lastQuery = query;
            return Arrays.asList(new AdminDeviceRealtimeState());
        }

        @Override
        public List<AdminDeviceMonthlyTrend> findMonthlyTrends(AdminDeviceEfficiencyQuery query) {
            this.lastQuery = query;
            return Arrays.asList(new AdminDeviceMonthlyTrend());
        }

        @Override
        public List<AdminDeviceHeatRank> findHeatRanking(AdminDeviceEfficiencyQuery query) {
            this.lastQuery = query;
            return Arrays.asList(new AdminDeviceHeatRank());
        }

        private static AdminDeviceEfficiencySummary summary() {
            AdminDeviceEfficiencySummary summary = new AdminDeviceEfficiencySummary();
            summary.setTotalDeviceCount(10);
            summary.setOnlineDeviceCount(8);
            summary.setActiveDeviceCount(3);
            summary.setTotalUsageMinutes(1200);
            summary.setAverageUtilizationRate(25.0);
            summary.setActiveTrainingCount(2);
            return summary;
        }
    }
}
