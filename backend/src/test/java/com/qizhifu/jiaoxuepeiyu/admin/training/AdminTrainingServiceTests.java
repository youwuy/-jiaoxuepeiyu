package com.qizhifu.jiaoxuepeiyu.admin.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingLog;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingMonitorSnapshot;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingQuery;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.training.port.AdminTrainingRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminTrainingServiceTests {

    @Test
    void createsDraftTrainingWithNormalizedRoles() {
        FakeTrainings repository = new FakeTrainings();
        AdminTrainingService service = new AdminTrainingService(repository);

        Long trainingId = service.createTraining(trainingCommand(), 9L);

        assertEquals(71L, trainingId.longValue());
        assertEquals("DRAFT", repository.savedCommand.getPublishStatus());
        assertEquals("PRACTICE", repository.savedCommand.getTrainingType());
        assertEquals("TEAM", repository.savedCommand.getTrainingMode());
        assertEquals(2, repository.savedCommand.getRoles().size());
        assertEquals("CREATE", repository.lastLogAction);
    }

    @Test
    void rejectsTrainingWithoutBoundClass() {
        AdminTrainingService service = new AdminTrainingService(new FakeTrainings());
        AdminTrainingCommand command = trainingCommand();
        command.setClassIds(new ArrayList<Long>());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createTraining(command, 9L);
        });

        assertEquals("Training classes are required", exception.getMessage());
    }

    @Test
    void acceptsPracticeTrainingWithoutTheoryPaper() {
        FakeTrainings repository = new FakeTrainings();
        AdminTrainingService service = new AdminTrainingService(repository);
        AdminTrainingCommand command = trainingCommand();
        command.setPaperMode("NONE");
        command.setPaperId(null);

        service.createTraining(command, 9L);

        assertEquals("NONE", repository.savedCommand.getPaperMode());
    }

    @Test
    void rejectsTeamTrainingWhenRoleCountDoesNotMatchTeamSize() {
        AdminTrainingService service = new AdminTrainingService(new FakeTrainings());
        AdminTrainingCommand command = trainingCommand();
        command.setTeamSize(3);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createTraining(command, 9L);
        });

        assertEquals("Team training roles must match team size", exception.getMessage());
    }

    @Test
    void rejectsExamTrainingWithoutPaper() {
        AdminTrainingService service = new AdminTrainingService(new FakeTrainings());
        AdminTrainingCommand command = trainingCommand();
        command.setTrainingType("EXAM");
        command.setPaperId(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createTraining(command, 9L);
        });

        assertEquals("Training exam paper is required", exception.getMessage());
    }

    @Test
    void publishesTrainingAndSyncsParticipantsAndNotifications() {
        FakeTrainings repository = new FakeTrainings();
        repository.training = existingTraining(71L);
        repository.boundStudentCount = 28;
        AdminTrainingService service = new AdminTrainingService(repository);

        service.publishTraining(71L, 9L);

        assertEquals(71L, repository.syncedTrainingId.longValue());
        assertEquals("PUBLISHED", repository.publishStatus);
        assertEquals(71L, repository.notificationTrainingId.longValue());
        assertEquals("PUBLISH", repository.lastLogAction);
    }

    @Test
    void rejectsPublishingTrainingWithoutStudents() {
        FakeTrainings repository = new FakeTrainings();
        repository.training = existingTraining(71L);
        repository.boundStudentCount = 0;
        AdminTrainingService service = new AdminTrainingService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.publishTraining(71L, 9L);
        });

        assertEquals("Training must have enabled students before publishing", exception.getMessage());
    }

    @Test
    void returnsStatisticsWithDefaultNumbers() {
        FakeTrainings repository = new FakeTrainings();
        repository.training = existingTraining(71L);
        AdminTrainingService service = new AdminTrainingService(repository);

        AdminTrainingStatistics statistics = service.getStatistics(71L);

        assertEquals(71L, statistics.getTrainingId().longValue());
        assertEquals(0, statistics.getParticipantCount().intValue());
        assertEquals(0, statistics.getAverageScore().doubleValue(), 0.001);
    }

    @Test
    void exportsTrainingsWithFilterAndMaximumPageSize() {
        FakeTrainings repository = new FakeTrainings();
        repository.trainings = Arrays.asList(existingTraining(71L));
        AdminTrainingService service = new AdminTrainingService(repository);
        AdminTrainingQuery query = new AdminTrainingQuery();
        query.setKeyword("Door");
        query.setPage(3);
        query.setPageSize(10);

        List<AdminTraining> trainings = service.exportTrainings(query);

        assertEquals(1, trainings.size());
        assertEquals(1, repository.findTrainingsCalls);
        assertEquals(0, repository.countTrainingsCalls);
        assertEquals(1, repository.lastQuery.getPage());
        assertEquals(100, repository.lastQuery.getPageSize());
        assertEquals("Door", repository.lastQuery.getKeyword());
    }

    private AdminTrainingCommand trainingCommand() {
        AdminTrainingCommand command = new AdminTrainingCommand();
        command.setTrainingName("Door Operation Drill");
        command.setAcademicYearId(1L);
        command.setSemesterId(2L);
        command.setMajorId(3L);
        command.setCoverUrl("https://cdn.example/training.png");
        command.setTrainingType("PRACTICE");
        command.setTrainingMode("TEAM");
        command.setPaperMode("MANUAL");
        command.setPaperId(5L);
        command.setOpenStartTime(LocalDateTime.of(2026, 9, 1, 0, 0));
        command.setOpenEndTime(LocalDateTime.of(2026, 12, 31, 23, 59));
        command.setTeamSize(2);
        command.setAppRequired(Boolean.TRUE);
        command.setClassIds(Arrays.asList(10L, 11L));
        command.setRoles(Arrays.asList(role("Driver", 1), role("Dispatcher", 2)));
        return command;
    }

    private AdminTrainingRoleCommand role(String roleName, int sortOrder) {
        AdminTrainingRoleCommand role = new AdminTrainingRoleCommand();
        role.setRoleName(roleName);
        role.setSortOrder(sortOrder);
        return role;
    }

    private AdminTraining existingTraining(Long trainingId) {
        AdminTraining training = new AdminTraining();
        training.setTrainingId(trainingId);
        training.setTrainingName("Door Operation Drill");
        training.setTrainingType("PRACTICE");
        training.setTrainingMode("TEAM");
        training.setPaperId(5L);
        training.setTeamSize(2);
        training.setClassIds(Arrays.asList(10L, 11L));
        training.setRoles(Arrays.asList(role("Driver", 1).toRole(), role("Dispatcher", 2).toRole()));
        return training;
    }

    private static class FakeTrainings implements AdminTrainingRepository {
        private AdminTrainingCommand savedCommand;
        private AdminTraining training;
        private int boundStudentCount = 28;
        private Long syncedTrainingId;
        private String publishStatus;
        private Long notificationTrainingId;
        private String lastLogAction;
        private List<AdminTraining> trainings = new ArrayList<AdminTraining>();
        private AdminTrainingQuery lastQuery;
        private int findTrainingsCalls;
        private int countTrainingsCalls;

        @Override
        public List<AdminTraining> findTrainings(AdminTrainingQuery query) {
            this.lastQuery = query;
            this.findTrainingsCalls++;
            return trainings;
        }

        @Override
        public long countTrainings(AdminTrainingQuery query) {
            this.lastQuery = query;
            this.countTrainingsCalls++;
            return 0;
        }

        @Override
        public AdminTraining findTraining(Long trainingId) {
            return training;
        }

        @Override
        public Long createTraining(AdminTrainingCommand command, Long creatorId) {
            this.savedCommand = command;
            return 71L;
        }

        @Override
        public void updateTraining(Long trainingId, AdminTrainingCommand command) {
            this.savedCommand = command;
        }

        @Override
        public int countEnabledStudentsByTrainingClasses(Long trainingId) {
            return boundStudentCount;
        }

        @Override
        public void syncParticipants(Long trainingId) {
            this.syncedTrainingId = trainingId;
        }

        @Override
        public void updatePublishStatus(Long trainingId, String publishStatus) {
            this.publishStatus = publishStatus;
        }

        @Override
        public void deleteTraining(Long trainingId) {
        }

        @Override
        public void notifyParticipants(Long trainingId, String title, String content) {
            this.notificationTrainingId = trainingId;
        }

        @Override
        public AdminTrainingStatistics calculateStatistics(Long trainingId) {
            return null;
        }

        @Override
        public AdminTrainingMonitorSnapshot getMonitorSnapshot(Long trainingId) {
            return new AdminTrainingMonitorSnapshot();
        }

        @Override
        public void appendTrainingLog(Long trainingId, Long operatorId, String action, String content) {
            this.lastLogAction = action;
        }

        @Override
        public List<AdminTrainingLog> findTrainingLogs(Long trainingId) {
            return new ArrayList<AdminTrainingLog>();
        }
    }
}
