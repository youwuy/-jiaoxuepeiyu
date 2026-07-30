package com.qizhifu.jiaoxuepeiyu.admin.archive;

import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveQuery;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.archive.port.AdminTrainingArchiveRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminTrainingArchiveService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminTrainingArchiveRepository repository;

    public AdminTrainingArchiveService(AdminTrainingArchiveRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdminTrainingArchive> listArchives(AdminTrainingArchiveQuery query) {
        AdminTrainingArchiveQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminTrainingArchive>(
                repository.findArchives(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countArchives(normalized));
    }

    public AdminTrainingArchiveDetail getArchiveDetail(Long archiveId) {
        AdminTrainingArchiveDetail detail = repository.findArchiveDetail(archiveId);
        if (detail == null) {
            throw new BusinessException(404, "Training archive not found");
        }
        detail.setSteps(repository.findArchiveSteps(archiveId));
        return detail;
    }

    public AdminTrainingArchiveStatistics getStatistics(AdminTrainingArchiveQuery query) {
        AdminTrainingArchiveStatistics statistics = repository.calculateStatistics(normalizedQuery(query));
        if (statistics == null) {
            statistics = new AdminTrainingArchiveStatistics();
        }
        statistics.setArchiveCount(defaultInteger(statistics.getArchiveCount()));
        statistics.setNormalSubmitCount(defaultInteger(statistics.getNormalSubmitCount()));
        statistics.setAbnormalSubmitCount(defaultInteger(statistics.getAbnormalSubmitCount()));
        statistics.setRoomDissolvedCount(defaultInteger(statistics.getRoomDissolvedCount()));
        statistics.setAveragePersonalScore(defaultDouble(statistics.getAveragePersonalScore()));
        statistics.setAverageDurationSeconds(defaultDouble(statistics.getAverageDurationSeconds()));
        return statistics;
    }

    public List<AdminTrainingArchive> exportArchives(AdminTrainingArchiveQuery query) {
        AdminTrainingArchiveQuery normalized = normalizedQuery(query);
        normalized.setPage(1);
        normalized.setPageSize(MAX_PAGE_SIZE);
        return repository.findArchives(normalized);
    }

    private AdminTrainingArchiveQuery normalizedQuery(AdminTrainingArchiveQuery query) {
        AdminTrainingArchiveQuery normalized = new AdminTrainingArchiveQuery();
        if (query != null) {
            normalized.setTrainingId(query.getTrainingId());
            normalized.setStudentId(query.getStudentId());
            normalized.setClassId(query.getClassId());
            normalized.setTrainingMode(upper(trimToNull(query.getTrainingMode())));
            normalized.setSubmitType(upper(trimToNull(query.getSubmitType())));
            normalized.setKeyword(trimToNull(query.getKeyword()));
            normalized.setSubmittedStartDate(query.getSubmittedStartDate());
            normalized.setSubmittedEndDate(query.getSubmittedEndDate());
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
        LocalDate startDate = normalized.getSubmittedStartDate();
        LocalDate endDate = normalized.getSubmittedEndDate();
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(400, "Archive submitted end date must not be before start date");
        }
        normalized.setSubmittedStartTime(startDate == null ? null : startDate.atStartOfDay());
        normalized.setSubmittedEndExclusiveTime(endDate == null ? null : endDate.plusDays(1).atStartOfDay());
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(MAX_PAGE_SIZE);
        }
        return normalized;
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
