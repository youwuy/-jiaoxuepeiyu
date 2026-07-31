package com.qizhifu.jiaoxuepeiyu.student.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseContentRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseDetail;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseCard;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.port.StudentCourseRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StudentCourseServiceTests {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-30T08:00:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Test
    void listsOnlyStudentPublishedCoursesWithStatusProgressAndRequiredSort() {
        StudentCourseService service = new StudentCourseService(new FakeCourses(), CLOCK);

        List<StudentCourseCard> cards = service.listCourses(7L, "");

        assertEquals(3, cards.size());
        assertEquals("Running Course", cards.get(0).getCourseName());
        assertEquals("STUDYING", cards.get(0).getStatus());
        assertEquals(50, cards.get(0).getProgressPercent());
        assertEquals("Upcoming Course", cards.get(1).getCourseName());
        assertEquals("NOT_STARTED", cards.get(1).getStatus());
        assertEquals("Finished Course", cards.get(2).getCourseName());
        assertEquals("FINISHED", cards.get(2).getStatus());
    }

    @Test
    void filtersCourseNameByKeyword() {
        StudentCourseService service = new StudentCourseService(new FakeCourses(), CLOCK);

        List<StudentCourseCard> cards = service.listCourses(7L, "Running");

        assertEquals(1, cards.size());
        assertEquals("Running Course", cards.get(0).getCourseName());
    }

    @Test
    void returnsCourseDetailWithChapterItemsAndLastContent() {
        StudentCourseService service = new StudentCourseService(new FakeCourses(), CLOCK);

        StudentCourseDetail detail = service.getCourseDetail(7L, 3L);

        assertEquals("Running Course", detail.getCourseName());
        assertEquals("STUDYING", detail.getStatus());
        assertEquals(50, detail.getProgressPercent());
        assertEquals(100L, detail.getLastContentId().longValue());
        assertEquals(1, detail.getChapters().size());
        assertEquals("Chapter One", detail.getChapters().get(0).getChapterTitle());
        assertEquals(2, detail.getChapters().get(0).getItems().size());
        assertEquals("COURSEWARE", detail.getChapters().get(0).getItems().get(0).getItemType());
    }

    @Test
    void completesCoursewareProgressWhenStudiedDurationReachesRequirement() {
        FakeCourses repository = new FakeCourses();
        StudentCourseService service = new StudentCourseService(repository, CLOCK);

        service.updateCoursewareProgress(7L, 3L, 100L, 600, false);

        assertEquals(7L, repository.progressStudentId.longValue());
        assertEquals(3L, repository.progressCourseId.longValue());
        assertEquals(100L, repository.progressContentId.longValue());
        assertEquals(600, repository.progressStudiedSeconds);
        assertEquals(true, repository.progressCompleted);
    }

    @Test
    void rejectsCoursewareProgressOutsideContentLearningWindow() {
        FakeCourses repository = new FakeCourses();
        repository.coursewareLearningStartTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        repository.coursewareLearningEndTime = LocalDateTime.of(2026, 8, 31, 23, 59);
        StudentCourseService service = new StudentCourseService(repository, CLOCK);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.updateCoursewareProgress(7L, 3L, 100L, 600, false);
        });

        assertEquals("Courseware is not open for learning", exception.getMessage());
    }

    private static class FakeCourses implements StudentCourseRepository {
        private Long progressStudentId;
        private Long progressCourseId;
        private Long progressContentId;
        private int progressStudiedSeconds;
        private boolean progressCompleted;
        private LocalDateTime coursewareLearningStartTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        private LocalDateTime coursewareLearningEndTime = LocalDateTime.of(2026, 8, 31, 23, 59);

        @Override
        public List<StudentCourseRecord> findPublishedCourses(Long studentId, String keyword) {
            List<StudentCourseRecord> all = Arrays.asList(
                    course(1L, "Finished Course", "2026-01-01T00:00:00", "2026-02-01T00:00:00", 5, 5),
                    course(2L, "Upcoming Course", "2026-08-01T00:00:00", "2026-09-01T00:00:00", 0, 4),
                    course(3L, "Running Course", "2026-07-01T00:00:00", "2026-08-31T00:00:00", 3, 6));
            if (keyword == null || keyword.trim().length() == 0) {
                return all;
            }
            return Arrays.asList(all.get(2));
        }

        @Override
        public Optional<StudentCourseRecord> findPublishedCourse(Long studentId, Long courseId) {
            return Optional.of(course(3L, "Running Course", "2026-07-01T00:00:00", "2026-08-31T00:00:00", 3, 6));
        }

        @Override
        public List<StudentCourseContentRecord> findCourseContents(Long studentId, Long courseId) {
            StudentCourseContentRecord courseware = content(10L, "Chapter One", 1, 100L,
                    "COURSEWARE", "Signal Basics", null, 8L, 300, 300, true, 1);
            StudentCourseContentRecord assignment = content(10L, "Chapter One", 1, 101L,
                    "ASSIGNMENT", "Theory Homework", 12L, null, 0, 0, false, 2);
            return Arrays.asList(courseware, assignment);
        }

        @Override
        public Optional<StudentCourseContentRecord> findCoursewareContent(Long studentId, Long courseId, Long contentId) {
            return Optional.of(content(10L, "Chapter One", 1, contentId,
                    "COURSEWARE", "Signal Basics", null, 8L, 300, 0, false, 1));
        }

        @Override
        public Optional<Long> findLastContentId(Long studentId, Long courseId) {
            return Optional.of(100L);
        }

        @Override
        public void saveCoursewareProgress(Long studentId,
                                           Long courseId,
                                           Long contentId,
                                           int studiedSeconds,
                                           boolean completed) {
            this.progressStudentId = studentId;
            this.progressCourseId = courseId;
            this.progressContentId = contentId;
            this.progressStudiedSeconds = studiedSeconds;
            this.progressCompleted = completed;
        }

        private StudentCourseRecord course(Long id,
                                           String name,
                                           String openStart,
                                           String openEnd,
                                           int completedItems,
                                           int totalItems) {
            StudentCourseRecord record = new StudentCourseRecord();
            record.setCourseId(id);
            record.setCourseName(name);
            record.setOpenStartTime(LocalDateTime.parse(openStart));
            record.setOpenEndTime(LocalDateTime.parse(openEnd));
            record.setCompletedItems(completedItems);
            record.setTotalItems(totalItems);
            return record;
        }

        private StudentCourseContentRecord content(Long chapterId,
                                                   String chapterTitle,
                                                   int chapterSortOrder,
                                                   Long contentId,
                                                   String itemType,
                                                   String title,
                                                   Long assignmentId,
                                                   Long resourceId,
                                                   int requiredDurationSeconds,
                                                   int studiedSeconds,
                                                   boolean completed,
                                                   int sortOrder) {
            StudentCourseContentRecord record = new StudentCourseContentRecord();
            record.setChapterId(chapterId);
            record.setChapterTitle(chapterTitle);
            record.setChapterSortOrder(chapterSortOrder);
            record.setContentId(contentId);
            record.setItemType(itemType);
            record.setTitle(title);
            record.setAssignmentId(assignmentId);
            record.setResourceId(resourceId);
            record.setRequiredDurationSeconds(requiredDurationSeconds);
            record.setLearningStartTime(coursewareLearningStartTime);
            record.setLearningEndTime(coursewareLearningEndTime);
            record.setStudiedSeconds(studiedSeconds);
            record.setCompleted(completed);
            record.setSortOrder(sortOrder);
            return record;
        }
    }
}
