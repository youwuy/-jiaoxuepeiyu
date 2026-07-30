package com.qizhifu.jiaoxuepeiyu.student.course;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseCard;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.port.StudentCourseRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
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

    private static class FakeCourses implements StudentCourseRepository {
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
    }
}
