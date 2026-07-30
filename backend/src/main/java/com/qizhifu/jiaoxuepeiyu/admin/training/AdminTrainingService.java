package com.qizhifu.jiaoxuepeiyu.admin.training;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingLog;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingMonitorSnapshot;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingQuery;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.training.port.AdminTrainingRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminTrainingService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> TRAINING_TYPES = new HashSet<String>(Arrays.asList("PRACTICE", "EXAM"));
    private static final Set<String> TRAINING_MODES = new HashSet<String>(Arrays.asList("SINGLE", "TEAM"));
    private static final Set<String> PAPER_MODES = new HashSet<String>(Arrays.asList("MANUAL", "AUTO"));

    private final AdminTrainingRepository repository;
    private final Clock clock;

    public AdminTrainingService(AdminTrainingRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    AdminTrainingService(AdminTrainingRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public PageResponse<AdminTraining> listTrainings(AdminTrainingQuery query) {
        AdminTrainingQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminTraining>(
                repository.findTrainings(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countTrainings(normalized));
    }

    public AdminTraining getTraining(Long trainingId) {
        AdminTraining training = repository.findTraining(trainingId);
        if (training == null) {
            throw new BusinessException(404, "Training not found");
        }
        return training;
    }

    @Transactional
    public Long createTraining(AdminTrainingCommand command, Long creatorId) {
        requireOperator(creatorId);
        AdminTrainingCommand normalized = normalizedTraining(command);
        Long trainingId = repository.createTraining(normalized, creatorId);
        repository.appendTrainingLog(trainingId, creatorId, "CREATE", "Create training");
        return trainingId;
    }

    @Transactional
    public void updateTraining(Long trainingId, AdminTrainingCommand command, Long operatorId) {
        requireOperator(operatorId);
        getTraining(trainingId);
        repository.updateTraining(trainingId, normalizedTraining(command));
        repository.appendTrainingLog(trainingId, operatorId, "UPDATE", "Update training");
    }

    @Transactional
    public void publishTraining(Long trainingId, Long operatorId) {
        requireOperator(operatorId);
        AdminTraining training = getTraining(trainingId);
        validatePublishable(training);
        int studentCount = repository.countEnabledStudentsByTrainingClasses(trainingId);
        if (studentCount <= 0) {
            throw new BusinessException(400, "Training must have enabled students before publishing");
        }
        repository.syncParticipants(trainingId);
        repository.updatePublishStatus(trainingId, "PUBLISHED");
        repository.notifyParticipants(trainingId,
                "New training published",
                "Training \"" + training.getTrainingName() + "\" is now available.");
        repository.appendTrainingLog(trainingId, operatorId, "PUBLISH", "Publish training");
    }

    @Transactional
    public void cancelPublishTraining(Long trainingId, Long operatorId) {
        requireOperator(operatorId);
        getTraining(trainingId);
        repository.updatePublishStatus(trainingId, "OFFLINE");
        repository.appendTrainingLog(trainingId, operatorId, "CANCEL_PUBLISH", "Cancel training publish");
    }

    @Transactional
    public void deleteTraining(Long trainingId, Long operatorId) {
        requireOperator(operatorId);
        getTraining(trainingId);
        repository.deleteTraining(trainingId);
        repository.appendTrainingLog(trainingId, operatorId, "DELETE", "Delete training");
    }

    public AdminTrainingStatistics getStatistics(Long trainingId) {
        getTraining(trainingId);
        AdminTrainingStatistics statistics = repository.calculateStatistics(trainingId);
        if (statistics == null) {
            statistics = new AdminTrainingStatistics();
        }
        return normalizedStatistics(trainingId, statistics);
    }

    public AdminTrainingMonitorSnapshot getMonitorSnapshot(Long trainingId) {
        getTraining(trainingId);
        AdminTrainingMonitorSnapshot snapshot = repository.getMonitorSnapshot(trainingId);
        if (snapshot == null) {
            snapshot = new AdminTrainingMonitorSnapshot();
        }
        snapshot.setTrainingId(trainingId);
        if (snapshot.getGeneratedAt() == null) {
            snapshot.setGeneratedAt(LocalDateTime.now(clock));
        }
        snapshot.setStatistics(normalizedStatistics(trainingId, snapshot.getStatistics()));
        return snapshot;
    }

    public List<AdminTrainingLog> listTrainingLogs(Long trainingId) {
        getTraining(trainingId);
        return repository.findTrainingLogs(trainingId);
    }

    private AdminTrainingCommand normalizedTraining(AdminTrainingCommand command) {
        if (command == null) {
            throw new BusinessException(400, "Training data is required");
        }
        String trainingName = trimToNull(command.getTrainingName());
        if (trainingName == null) {
            throw new BusinessException(400, "Training name is required");
        }
        if (trainingName.length() > 128) {
            throw new BusinessException(400, "Training name cannot exceed 128 characters");
        }
        if (command.getAcademicYearId() == null || command.getSemesterId() == null) {
            throw new BusinessException(400, "Training academic year and semester are required");
        }
        if (command.getMajorId() == null) {
            throw new BusinessException(400, "Training major is required");
        }
        String coverUrl = trimToNull(command.getCoverUrl());
        if (coverUrl == null) {
            throw new BusinessException(400, "Training cover is required");
        }
        validateOpenTime(command.getOpenStartTime(), command.getOpenEndTime());

        AdminTrainingCommand normalized = new AdminTrainingCommand();
        normalized.setTrainingName(trainingName);
        normalized.setAcademicYearId(command.getAcademicYearId());
        normalized.setSemesterId(command.getSemesterId());
        normalized.setMajorId(command.getMajorId());
        normalized.setCoverUrl(coverUrl);
        normalized.setTrainingType(normalizedEnum(command.getTrainingType(), "PRACTICE", TRAINING_TYPES,
                "Training type is invalid"));
        normalized.setTrainingMode(normalizedEnum(command.getTrainingMode(), "SINGLE", TRAINING_MODES,
                "Training mode is invalid"));
        normalized.setPaperMode(normalizedEnum(command.getPaperMode(), "MANUAL", PAPER_MODES,
                "Training paper mode is invalid"));
        normalized.setPaperId(command.getPaperId());
        normalized.setOpenStartTime(command.getOpenStartTime());
        normalized.setOpenEndTime(command.getOpenEndTime());
        normalized.setAppRequired(command.getAppRequired() == null ? Boolean.TRUE : command.getAppRequired());
        normalized.setClassIds(normalizedIds(command.getClassIds(), "Training classes are required"));
        normalized.setRoles(normalizedRoles(command.getRoles()));
        normalized.setTeamSize(normalizedTeamSize(normalized.getTrainingMode(), command.getTeamSize()));
        validatePaper(normalized);
        validateRoles(normalized);
        normalized.setPublishStatus("DRAFT");
        return normalized;
    }

    private void validatePublishable(AdminTraining training) {
        if ("MANUAL".equals(training.getPaperMode()) && training.getPaperId() == null) {
            throw new BusinessException(400, "Manual training paper is required");
        }
        if ("EXAM".equals(training.getTrainingType()) && training.getPaperId() == null) {
            throw new BusinessException(400, "Training exam paper is required");
        }
        if ("TEAM".equals(training.getTrainingMode())) {
            int roleCount = training.getRoles() == null ? 0 : training.getRoles().size();
            int teamSize = training.getTeamSize() == null ? 0 : training.getTeamSize().intValue();
            if (teamSize <= 1 || roleCount != teamSize) {
                throw new BusinessException(400, "Team training roles must match team size");
            }
        }
    }

    private void validatePaper(AdminTrainingCommand command) {
        if ("EXAM".equals(command.getTrainingType()) && command.getPaperId() == null) {
            throw new BusinessException(400, "Training exam paper is required");
        }
        if ("MANUAL".equals(command.getPaperMode()) && command.getPaperId() == null) {
            throw new BusinessException(400, "Manual training paper is required");
        }
    }

    private void validateRoles(AdminTrainingCommand command) {
        if ("SINGLE".equals(command.getTrainingMode())) {
            if (command.getRoles() == null || command.getRoles().isEmpty()) {
                return;
            }
            throw new BusinessException(400, "Single training cannot configure team roles");
        }
        int roleCount = command.getRoles() == null ? 0 : command.getRoles().size();
        if (roleCount != command.getTeamSize().intValue()) {
            throw new BusinessException(400, "Team training roles must match team size");
        }
    }

    private List<AdminTrainingRoleCommand> normalizedRoles(List<AdminTrainingRoleCommand> roles) {
        List<AdminTrainingRoleCommand> normalized = new ArrayList<AdminTrainingRoleCommand>();
        if (roles == null) {
            return normalized;
        }
        int defaultSort = 1;
        for (AdminTrainingRoleCommand role : roles) {
            String roleName = trimToNull(role == null ? null : role.getRoleName());
            if (roleName == null) {
                throw new BusinessException(400, "Training role name is required");
            }
            AdminTrainingRoleCommand normalizedRole = new AdminTrainingRoleCommand();
            normalizedRole.setRoleName(roleName);
            normalizedRole.setSortOrder(role.getSortOrder() == null ? Integer.valueOf(defaultSort) : role.getSortOrder());
            normalized.add(normalizedRole);
            defaultSort++;
        }
        return normalized;
    }

    private Integer normalizedTeamSize(String trainingMode, Integer teamSize) {
        if ("SINGLE".equals(trainingMode)) {
            return Integer.valueOf(1);
        }
        int value = teamSize == null ? 0 : teamSize.intValue();
        if (value <= 1) {
            throw new BusinessException(400, "Team training size must be greater than 1");
        }
        if (value > 20) {
            throw new BusinessException(400, "Team training size cannot exceed 20");
        }
        return Integer.valueOf(value);
    }

    private AdminTrainingQuery normalizedQuery(AdminTrainingQuery query) {
        AdminTrainingQuery normalized = new AdminTrainingQuery();
        if (query != null) {
            normalized.setKeyword(trimToNull(query.getKeyword()));
            normalized.setAcademicYearId(query.getAcademicYearId());
            normalized.setSemesterId(query.getSemesterId());
            normalized.setMajorId(query.getMajorId());
            normalized.setClassId(query.getClassId());
            normalized.setTrainingType(upper(trimToNull(query.getTrainingType())));
            normalized.setTrainingMode(upper(trimToNull(query.getTrainingMode())));
            normalized.setPublishStatus(upper(trimToNull(query.getPublishStatus())));
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
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

    private AdminTrainingStatistics normalizedStatistics(Long trainingId, AdminTrainingStatistics statistics) {
        if (statistics == null) {
            statistics = new AdminTrainingStatistics();
        }
        statistics.setTrainingId(statistics.getTrainingId() == null ? trainingId : statistics.getTrainingId());
        statistics.setParticipantCount(defaultInteger(statistics.getParticipantCount()));
        statistics.setWaitingRoomCount(defaultInteger(statistics.getWaitingRoomCount()));
        statistics.setStartedRoomCount(defaultInteger(statistics.getStartedRoomCount()));
        statistics.setDissolvedRoomCount(defaultInteger(statistics.getDissolvedRoomCount()));
        statistics.setSubmittedAttemptCount(defaultInteger(statistics.getSubmittedAttemptCount()));
        statistics.setAverageScore(defaultDouble(statistics.getAverageScore()));
        statistics.setMaxScore(defaultDouble(statistics.getMaxScore()));
        statistics.setMinScore(defaultDouble(statistics.getMinScore()));
        return statistics;
    }

    private List<Long> normalizedIds(List<Long> ids, String message) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, message);
        }
        List<Long> normalized = new ArrayList<Long>();
        for (Long id : ids) {
            if (id != null && id.longValue() > 0 && !normalized.contains(id)) {
                normalized.add(id);
            }
        }
        if (normalized.isEmpty()) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    private void validateOpenTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(400, "Training open time range is required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(400, "Training open end time must be after start time");
        }
    }

    private String normalizedEnum(String value, String defaultValue, Set<String> allowed, String message) {
        String normalized = upper(trimToNull(value));
        if (normalized == null) {
            normalized = defaultValue;
        }
        if (!allowed.contains(normalized)) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }

    private Double defaultDouble(Double value) {
        return value == null ? Double.valueOf(0) : value;
    }

    private void requireOperator(Long operatorId) {
        if (operatorId == null || operatorId.longValue() <= 0) {
            throw new BusinessException(401, "Missing admin identity");
        }
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
