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
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingWeakStep;
import com.qizhifu.jiaoxuepeiyu.admin.training.port.AdminTrainingRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    void acceptsTeamTrainingWhenCourseContainsRolesForMultipleTopics() {
        FakeTrainings repository = new FakeTrainings();
        AdminTrainingService service = new AdminTrainingService(new FakeTrainings());
        AdminTrainingCommand command = trainingCommand();
        command.setTeamSize(3);

        service = new AdminTrainingService(repository);
        service.createTraining(command, 9L);

        assertEquals(2, repository.savedCommand.getRoles().size());
    }

    @Test
    void acceptsExamTrainingWithoutTheoryPaperWhenTopicsAreBound() {
        FakeTrainings repository = new FakeTrainings();
        AdminTrainingService service = new AdminTrainingService(repository);
        AdminTrainingCommand command = trainingCommand();
        command.setTrainingType("EXAM");
        command.setPaperMode("NONE");
        command.setPaperId(null);

        service.createTraining(command, 9L);

        assertEquals(Arrays.asList(21L), repository.savedCommand.getTopicIds());
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
    void startsPublishedTeamExamOnce() {
        FakeTrainings repository = new FakeTrainings();
        repository.training = existingTraining(71L);
        repository.training.setTrainingType("EXAM");
        repository.training.setTrainingMode("TEAM");
        repository.training.setPublishStatus("PUBLISHED");
        AdminTrainingService service = new AdminTrainingService(repository);

        service.startExam(71L, 9L);

        assertEquals(71L, repository.startedTrainingId.longValue());
        assertEquals("START_EXAM", repository.lastLogAction);
    }

    @Test
    void rejectsStartingExamTwice() {
        FakeTrainings repository = new FakeTrainings();
        repository.training = existingTraining(71L);
        repository.training.setTrainingType("EXAM");
        repository.training.setTrainingMode("TEAM");
        repository.training.setPublishStatus("PUBLISHED");
        repository.training.setExamStartedAt(LocalDateTime.of(2026, 9, 1, 8, 0));
        AdminTrainingService service = new AdminTrainingService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.startExam(71L, 9L));

        assertEquals("Training exam has already started", exception.getMessage());
    }

    @Test
    void preservesTrainingOverlapRangeInListQuery() {
        FakeTrainings repository = new FakeTrainings();
        AdminTrainingService service = new AdminTrainingService(repository);
        AdminTrainingQuery query = new AdminTrainingQuery();
        LocalDateTime rangeStart = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2026, 9, 30, 23, 59);
        query.setRangeStart(rangeStart);
        query.setRangeEnd(rangeEnd);

        service.listTrainings(query);

        assertEquals(rangeStart, repository.lastQuery.getRangeStart());
        assertEquals(rangeEnd, repository.lastQuery.getRangeEnd());
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
        command.setClassroomId(8L);
        command.setTeacherIds(Arrays.asList(9L));
        command.setScoreBasis("HIGHEST");
        command.setTopicIds(Arrays.asList(21L));
        command.setClassIds(Arrays.asList(10L, 11L));
        command.setRoles(Arrays.asList(role(21L, "Driver", 1), role(21L, "Dispatcher", 2)));
        return command;
    }

    private AdminTrainingRoleCommand role(Long topicId, String roleName, int sortOrder) {
        AdminTrainingRoleCommand role = new AdminTrainingRoleCommand();
        role.setTopicId(topicId);
        role.setRoleName(roleName);
        role.setAiFillEnabled(Boolean.FALSE);
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
        training.setRoles(Arrays.asList(role(21L, "Driver", 1).toRole(), role(21L, "Dispatcher", 2).toRole()));
        return training;
    }

    private static class FakeTrainings implements AdminTrainingRepository {
        private AdminTrainingCommand savedCommand;
        private AdminTraining training;
        private int boundStudentCount = 28;
        private Long syncedTrainingId;
        private String publishStatus;
        private Long notificationTrainingId;
        private Long startedTrainingId;
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
        public boolean roleBelongsToTopic(Long topicId, String roleName) {
            return Long.valueOf(21L).equals(topicId)
                    && ("Driver".equals(roleName) || "Dispatcher".equals(roleName));
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
        public void markExamStarted(Long trainingId) {
            this.startedTrainingId = trainingId;
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
        public List<AdminTrainingWeakStep> findWeakSteps(Long trainingId, String className) {
            return new ArrayList<AdminTrainingWeakStep>();
        }

        @Override
        public AdminTrainingMonitorSnapshot getMonitorSnapshot(Long trainingId) {
            return new AdminTrainingMonitorSnapshot();
        }

        @Override
        public boolean dissolveRoom(Long trainingId, Long roomId) {
            return false;
        }

        @Override
        public List<Map<String, Object>> findReviewRows(Long trainingId) {
            return new ArrayList<Map<String, Object>>();
        }

        @Override
        public List<Map<String, Object>> findReviewAttempts(Long trainingId, Long studentId, Long topicId) {
            return new ArrayList<Map<String, Object>>();
        }

        @Override
        public boolean reviewAttempt(Long trainingId, Long attemptId, Double manualScore, String comment, Long reviewerId) {
            return false;
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
