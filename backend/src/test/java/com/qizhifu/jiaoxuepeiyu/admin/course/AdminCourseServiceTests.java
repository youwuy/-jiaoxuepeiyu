package com.qizhifu.jiaoxuepeiyu.admin.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourse;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseChapterCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseContentCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatisticsQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.port.AdminCourseRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminCourseServiceTests {

    @Test
    void createsCourseWithCountsAndDraftStatus() {
        FakeCourses repository = new FakeCourses();
        AdminCourseService service = new AdminCourseService(repository);

        Long courseId = service.createCourse(courseCommand(), 9L);

        assertEquals(31L, courseId.longValue());
        assertEquals("DRAFT", repository.savedCommand.getPublishStatus());
        assertEquals(1, repository.savedCommand.getCoursewareCount().intValue());
        assertEquals(1, repository.savedCommand.getAssignmentCount().intValue());
        assertEquals("CREATE", repository.lastLogAction);
    }

    @Test
    void rejectsCourseWithoutTeachingClass() {
        AdminCourseService service = new AdminCourseService(new FakeCourses());
        AdminCourseCommand command = courseCommand();
        command.setClassIds(new ArrayList<Long>());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createCourse(command, 9L);
        });

        assertEquals("Course teaching classes are required", exception.getMessage());
    }

    @Test
    void rejectsInvalidOpenTimeRange() {
        AdminCourseService service = new AdminCourseService(new FakeCourses());
        AdminCourseCommand command = courseCommand();
        command.setOpenStartTime(LocalDateTime.of(2026, 9, 2, 0, 0));
        command.setOpenEndTime(LocalDateTime.of(2026, 9, 1, 0, 0));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createCourse(command, 9L);
        });

        assertEquals("Course open end time must be after start time", exception.getMessage());
    }

    @Test
    void rejectsCourseNameLongerThanTwentyCharacters() {
        AdminCourseService service = new AdminCourseService(new FakeCourses());
        AdminCourseCommand command = courseCommand();
        command.setCourseName("123456789012345678901");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createCourse(command, 9L);
        });

        assertEquals("Course name cannot exceed 20 characters", exception.getMessage());
    }

    @Test
    void rejectsCoursewareLearningEndBeforeStart() {
        AdminCourseService service = new AdminCourseService(new FakeCourses());
        AdminCourseCommand command = courseCommand();
        AdminCourseContentCommand content = command.getChapters().get(0).getContents().get(0);
        content.setLearningStartTime(LocalDateTime.of(2026, 9, 2, 0, 0));
        content.setLearningEndTime(LocalDateTime.of(2026, 9, 1, 0, 0));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createCourse(command, 9L);
        });

        assertEquals("Courseware learning end time must be after start time", exception.getMessage());
    }

    @Test
    void rejectsAssignmentPassScoreOutsideTotalScore() {
        AdminCourseService service = new AdminCourseService(new FakeCourses());
        AdminCourseCommand command = courseCommand();
        AdminCourseContentCommand content = command.getChapters().get(0).getContents().get(1);
        content.setAssignmentCompletionRule("PASS_SCORE");
        content.setPassScore(101);
        content.setAssignmentTotalScore(100);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createCourse(command, 9L);
        });

        assertEquals("Assignment pass score must be between 0 and total score", exception.getMessage());
    }

    @Test
    void normalizesCourseListTeachingTimeOverlapAndUnpublishedStatus() {
        FakeCourses repository = new FakeCourses();
        AdminCourseService service = new AdminCourseService(repository);
        AdminCourseQuery query = new AdminCourseQuery();
        query.setTeachingStartTime(LocalDateTime.of(2026, 9, 1, 0, 0));
        query.setTeachingEndTime(LocalDateTime.of(2026, 9, 30, 23, 59));
        query.setPublishStatus("UNPUBLISHED");

        service.listCourses(query);

        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0), repository.lastQuery.getTeachingStartTime());
        assertEquals(LocalDateTime.of(2026, 9, 30, 23, 59), repository.lastQuery.getTeachingEndTime());
        assertEquals("NOT_PUBLISHED", repository.lastQuery.getPublishStatus());
    }

    @Test
    void keepsNestedChaptersAndCountsNestedContent() {
        FakeCourses repository = new FakeCourses();
        AdminCourseService service = new AdminCourseService(repository);
        AdminCourseCommand command = courseCommand();
        AdminCourseChapterCommand child = new AdminCourseChapterCommand();
        child.setChapterTitle("Section 1");
        child.setContents(Arrays.asList(content("COURSEWARE")));
        command.getChapters().get(0).setChildren(Arrays.asList(child));

        service.createCourse(command, 9L);

        assertEquals(2, repository.savedCommand.getCoursewareCount().intValue());
        assertEquals(1, repository.savedCommand.getChapters().get(0).getChildren().size());
    }

    @Test
    void rejectsChapterDeeperThanThreeLevels() {
        AdminCourseService service = new AdminCourseService(new FakeCourses());
        AdminCourseCommand command = courseCommand();
        AdminCourseChapterCommand level2 = new AdminCourseChapterCommand();
        level2.setChapterTitle("Level 2");
        AdminCourseChapterCommand level3 = new AdminCourseChapterCommand();
        level3.setChapterTitle("Level 3");
        AdminCourseChapterCommand level4 = new AdminCourseChapterCommand();
        level4.setChapterTitle("Level 4");
        level3.setChildren(Arrays.asList(level4));
        level2.setChildren(Arrays.asList(level3));
        command.getChapters().get(0).setChildren(Arrays.asList(level2));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createCourse(command, 9L);
        });

        assertEquals("Course chapters cannot exceed 3 levels", exception.getMessage());
    }

    @Test
    void publishesCourseAndNotifiesBoundStudents() {
        FakeCourses repository = new FakeCourses();
        repository.course = existingCourse(31L, 2);
        AdminCourseService service = new AdminCourseService(repository);

        service.publishCourse(31L, 9L);

        assertEquals("PUBLISHED", repository.publishStatus);
        assertEquals(31L, repository.notificationCourseId.longValue());
        assertEquals("PUBLISH", repository.lastLogAction);
    }

    @Test
    void rejectsPublishingCourseWithoutContent() {
        FakeCourses repository = new FakeCourses();
        repository.course = existingCourse(31L, 0);
        AdminCourseService service = new AdminCourseService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.publishCourse(31L, 9L);
        });

        assertEquals("Course must contain content before publishing", exception.getMessage());
    }

    @Test
    void copiesCourseAsDraftWithOriginalBindings() {
        FakeCourses repository = new FakeCourses();
        repository.course = existingCourse(31L, 2);
        AdminCourseService service = new AdminCourseService(repository);

        Long copiedId = service.copyCourse(31L, 9L);

        assertEquals(32L, copiedId.longValue());
        assertEquals(31L, repository.copiedSourceCourseId.longValue());
        assertEquals(9L, repository.copiedCreatorId.longValue());
        assertEquals("COPY", repository.lastLogAction);
    }

    @Test
    void returnsCourseStatistics() {
        FakeCourses repository = new FakeCourses();
        repository.course = existingCourse(31L, 2);
        repository.statistics = new AdminCourseStatistics();
        repository.statistics.setCourseId(31L);
        repository.statistics.setStudentCount(40);
        repository.statistics.setCompletedCount(12);
        AdminCourseService service = new AdminCourseService(repository);

        AdminCourseStatistics statistics = service.getStatistics(31L);

        assertEquals(40, statistics.getStudentCount().intValue());
        assertEquals(12, statistics.getCompletedCount().intValue());
    }

    private AdminCourseCommand courseCommand() {
        AdminCourseCommand command = new AdminCourseCommand();
        command.setCourseName("Safety Course");
        command.setAcademicYearId(1L);
        command.setSemesterId(2L);
        command.setMajorId(3L);
        command.setCoverUrl("https://cdn.example/course.png");
        command.setOpenStartTime(LocalDateTime.of(2026, 9, 1, 0, 0));
        command.setOpenEndTime(LocalDateTime.of(2026, 12, 31, 23, 59));
        command.setTeacherIds(Arrays.asList(9L));
        command.setClassIds(Arrays.asList(10L, 11L));
        command.setLearningMode("SELF_PACED");
        command.setAssignmentCompletionRule("SUBMIT");
        command.setCoursewareScoreCap(100);
        command.setChapters(Arrays.asList(chapter()));
        return command;
    }

    private AdminCourseChapterCommand chapter() {
        AdminCourseChapterCommand chapter = new AdminCourseChapterCommand();
        chapter.setChapterTitle("Chapter 1");
        chapter.setSortOrder(1);
        chapter.setContents(Arrays.asList(content("COURSEWARE"), content("ASSIGNMENT")));
        return chapter;
    }

    private AdminCourseContentCommand content(String itemType) {
        AdminCourseContentCommand content = new AdminCourseContentCommand();
        content.setItemType(itemType);
        content.setTitle(itemType + " item");
        content.setResourceId("COURSEWARE".equals(itemType) ? 1L : null);
        content.setAssignmentId("ASSIGNMENT".equals(itemType) ? 2L : null);
        content.setRequiredDurationSeconds("COURSEWARE".equals(itemType) ? 60 : 0);
        content.setLearningStartTime("COURSEWARE".equals(itemType) ? LocalDateTime.of(2026, 9, 1, 0, 0) : null);
        content.setLearningEndTime("COURSEWARE".equals(itemType) ? LocalDateTime.of(2026, 12, 31, 23, 59) : null);
        content.setAssignmentCompletionRule("ASSIGNMENT".equals(itemType) ? "SUBMIT" : null);
        content.setAssignmentPublishMode("ASSIGNMENT".equals(itemType) ? "PRACTICE" : null);
        content.setAnswerStartTime("ASSIGNMENT".equals(itemType) ? LocalDateTime.of(2026, 9, 1, 0, 0) : null);
        content.setAnswerEndTime("ASSIGNMENT".equals(itemType) ? LocalDateTime.of(2026, 12, 31, 23, 59) : null);
        content.setAssignmentTotalScore("ASSIGNMENT".equals(itemType) ? 100 : null);
        content.setSortOrder("COURSEWARE".equals(itemType) ? 1 : 2);
        return content;
    }

    private AdminCourse existingCourse(Long courseId, int contentCount) {
        AdminCourse course = new AdminCourse();
        course.setCourseId(courseId);
        course.setCourseName("Safety Course");
        course.setPublishStatus("DRAFT");
        course.setCoursewareCount(contentCount > 0 ? 1 : 0);
        course.setAssignmentCount(contentCount > 1 ? 1 : 0);
        return course;
    }

    private static class FakeCourses implements AdminCourseRepository {
        private AdminCourseCommand savedCommand;
        private AdminCourse course;
        private AdminCourseStatistics statistics;
        private String publishStatus;
        private Long notificationCourseId;
        private Long copiedSourceCourseId;
        private Long copiedCreatorId;
        private String lastLogAction;
        private AdminCourseQuery lastQuery;

        @Override
        public List<AdminCourse> findCourses(AdminCourseQuery query) {
            this.lastQuery = query;
            return new ArrayList<AdminCourse>();
        }

        @Override
        public long countCourses(AdminCourseQuery query) {
            return 0;
        }

        @Override
        public AdminCourse findCourse(Long courseId) {
            return course;
        }

        @Override
        public Long createCourse(AdminCourseCommand command, Long creatorId) {
            this.savedCommand = command;
            return 31L;
        }

        @Override
        public void updateCourse(Long courseId, AdminCourseCommand command) {
            this.savedCommand = command;
        }

        @Override
        public void updatePublishStatus(Long courseId, String publishStatus) {
            this.publishStatus = publishStatus;
        }

        @Override
        public void deleteCourse(Long courseId) {
        }

        @Override
        public Long copyCourse(Long sourceCourseId, Long creatorId) {
            this.copiedSourceCourseId = sourceCourseId;
            this.copiedCreatorId = creatorId;
            return 32L;
        }

        @Override
        public void notifyBoundStudents(Long courseId, String title, String content) {
            this.notificationCourseId = courseId;
        }

        @Override
        public AdminCourseStatistics calculateStatistics(Long courseId) {
            return statistics;
        }

        @Override
        public List<AdminCourseStudentStatistics> findStudentStatistics(Long courseId, AdminCourseStudentStatisticsQuery query) {
            return new ArrayList<AdminCourseStudentStatistics>();
        }

        @Override
        public long countStudentStatistics(Long courseId, AdminCourseStudentStatisticsQuery query) {
            return 0;
        }

        @Override
        public void appendCourseLog(Long courseId, Long operatorId, String action, String content) {
            this.lastLogAction = action;
        }
    }
}
