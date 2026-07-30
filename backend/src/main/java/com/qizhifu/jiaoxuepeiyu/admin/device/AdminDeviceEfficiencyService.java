package com.qizhifu.jiaoxuepeiyu.admin.device;

import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyQuery;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyReport;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencySummary;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceHeatRank;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceMonthlyTrend;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceRealtimeState;
import com.qizhifu.jiaoxuepeiyu.admin.device.port.AdminDeviceEfficiencyRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDeviceEfficiencyService {

    private static final int MAX_RANGE_DAYS = 366;
    private static final int DEFAULT_RANK_LIMIT = 10;
    private static final int MAX_RANK_LIMIT = 100;

    private final AdminDeviceEfficiencyRepository repository;
    private final Clock clock;

    @Autowired
    public AdminDeviceEfficiencyService(AdminDeviceEfficiencyRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    AdminDeviceEfficiencyService(AdminDeviceEfficiencyRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public AdminDeviceEfficiencyReport getDashboard(AdminDeviceEfficiencyQuery query) {
        AdminDeviceEfficiencyQuery normalized = normalizedQuery(query);
        AdminDeviceEfficiencyReport report = new AdminDeviceEfficiencyReport();
        report.setSummary(normalizedSummary(repository.findSummary(normalized)));
        report.setRealtimeStates(repository.findRealtimeStates(normalized));
        report.setMonthlyTrends(repository.findMonthlyTrends(normalized));
        report.setHeatRanking(withRankNumbers(repository.findHeatRanking(normalized)));
        return report;
    }

    public AdminDeviceEfficiencySummary getSummary(AdminDeviceEfficiencyQuery query) {
        return normalizedSummary(repository.findSummary(normalizedQuery(query)));
    }

    public List<AdminDeviceRealtimeState> listRealtimeStates(AdminDeviceEfficiencyQuery query) {
        return repository.findRealtimeStates(normalizedQuery(query));
    }

    public List<AdminDeviceMonthlyTrend> listMonthlyTrends(AdminDeviceEfficiencyQuery query) {
        return repository.findMonthlyTrends(normalizedQuery(query));
    }

    public List<AdminDeviceHeatRank> listHeatRanking(AdminDeviceEfficiencyQuery query) {
        return withRankNumbers(repository.findHeatRanking(normalizedQuery(query)));
    }

    private AdminDeviceEfficiencyQuery normalizedQuery(AdminDeviceEfficiencyQuery query) {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = query == null ? null : query.getStartDate();
        LocalDate endDate = query == null ? null : query.getEndDate();
        if (startDate == null && endDate == null) {
            startDate = today.withDayOfMonth(1);
            endDate = today;
        } else if (startDate == null) {
            startDate = endDate.withDayOfMonth(1);
        } else if (endDate == null) {
            endDate = today;
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(400, "Device efficiency end date must not be before start date");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_RANGE_DAYS) {
            throw new BusinessException(400, "Device efficiency date range cannot exceed 366 days");
        }

        AdminDeviceEfficiencyQuery normalized = new AdminDeviceEfficiencyQuery();
        normalized.setStartDate(startDate);
        normalized.setEndDate(endDate);
        normalized.setStartDateTime(startDate.atStartOfDay());
        normalized.setEndExclusiveDateTime(endDate.plusDays(1).atStartOfDay());
        normalized.setClassroomId(query == null ? null : query.getClassroomId());
        normalized.setDeviceType(upper(trimToNull(query == null ? null : query.getDeviceType())));
        normalized.setDeviceStatus(upper(trimToNull(query == null ? null : query.getDeviceStatus())));
        normalized.setDayCount(Integer.valueOf((int) days));
        normalized.setRankLimit(normalizedRankLimit(query == null ? 0 : query.getRankLimit()));
        normalized.setTotalAvailableMinutes(Long.valueOf(days * 24L * 60L));
        return normalized;
    }

    private int normalizedRankLimit(int rankLimit) {
        if (rankLimit <= 0) {
            return DEFAULT_RANK_LIMIT;
        }
        return Math.min(rankLimit, MAX_RANK_LIMIT);
    }

    private AdminDeviceEfficiencySummary normalizedSummary(AdminDeviceEfficiencySummary summary) {
        if (summary == null) {
            summary = new AdminDeviceEfficiencySummary();
        }
        summary.setTotalDeviceCount(defaultInteger(summary.getTotalDeviceCount()));
        summary.setOnlineDeviceCount(defaultInteger(summary.getOnlineDeviceCount()));
        summary.setActiveDeviceCount(defaultInteger(summary.getActiveDeviceCount()));
        summary.setFaultDeviceCount(defaultInteger(summary.getFaultDeviceCount()));
        summary.setTotalUsageMinutes(defaultInteger(summary.getTotalUsageMinutes()));
        summary.setAverageUtilizationRate(defaultDouble(summary.getAverageUtilizationRate()));
        summary.setActiveTrainingCount(defaultInteger(summary.getActiveTrainingCount()));
        return summary;
    }

    private List<AdminDeviceHeatRank> withRankNumbers(List<AdminDeviceHeatRank> ranking) {
        if (ranking == null) {
            return ranking;
        }
        int rank = 1;
        for (AdminDeviceHeatRank item : ranking) {
            item.setRankNo(Integer.valueOf(rank++));
            item.setUsageMinutes(defaultInteger(item.getUsageMinutes()));
            item.setUsageCount(defaultInteger(item.getUsageCount()));
            item.setUtilizationRate(defaultDouble(item.getUtilizationRate()));
        }
        return ranking;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }

    private Double defaultDouble(Double value) {
        return value == null ? Double.valueOf(0) : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    private String upper(String value) {
        return value == null ? null : value.toUpperCase();
    }
}
