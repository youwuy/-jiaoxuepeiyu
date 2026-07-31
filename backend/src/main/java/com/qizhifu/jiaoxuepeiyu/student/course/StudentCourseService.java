package com.qizhifu.jiaoxuepeiyu.student.course;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.domain.course.CourseProgressCalculator;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseChapter;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseCard;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseContentRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseDetail;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseItem;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.port.StudentCourseRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentCourseService {

    private final StudentCourseRepository repository;
    private final Clock clock;

    @Autowired
    public StudentCourseService(StudentCourseRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    StudentCourseService(StudentCourseRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<StudentCourseCard> listCourses(Long studentId, String keyword) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<StudentCourseCard> cards = new ArrayList<StudentCourseCard>();
        for (StudentCourseRecord record : repository.findPublishedCourses(studentId, keyword)) {
            StudentCourseCard card = toCard(record, now);
            cards.add(card);
        }
        Collections.sort(cards, new StudentCourseCardComparator());
        return cards;
    }

    public StudentCourseDetail getCourseDetail(Long studentId, Long courseId) {
        LocalDateTime now = LocalDateTime.now(clock);
        StudentCourseRecord course = repository.findPublishedCourse(studentId, courseId)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));

        StudentCourseDetail detail = new StudentCourseDetail();
        detail.setCourseId(course.getCourseId());
        detail.setCourseName(course.getCourseName());
        detail.setAcademicTerm(course.getAcademicTerm());
        detail.setTeacherNames(course.getTeacherNames());
        detail.setOpenStartTime(course.getOpenStartTime());
        detail.setOpenEndTime(course.getOpenEndTime());
        detail.setProgressPercent(CourseProgressCalculator.calculatePercent(
                course.getCompletedItems(), course.getTotalItems()));
        detail.setStatus(status(course, now));
        detail.setLastContentId(repository.findLastContentId(studentId, courseId).orElse(null));
        detail.setChapters(toChapters(repository.findCourseContents(studentId, courseId)));
        return detail;
    }

    public void updateCoursewareProgress(Long studentId,
                                         Long courseId,
                                         Long contentId,
                                         int studiedSeconds,
                                         boolean completed) {
        if (studiedSeconds < 0) {
            throw new BusinessException(400, "Studied seconds cannot be negative");
        }
        StudentCourseRecord course = repository.findPublishedCourse(studentId, courseId)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));
        StudentCourseContentRecord content = repository.findCoursewareContent(studentId, courseId, contentId)
                .orElseThrow(() -> new BusinessException(404, "Courseware content not found"));
        LocalDateTime now = LocalDateTime.now(clock);
        if (!isOpen(now, course.getOpenStartTime(), course.getOpenEndTime())
                || !isOpen(now, content.getLearningStartTime(), content.getLearningEndTime())) {
            throw new BusinessException(400, "Courseware is not open for learning");
        }
        boolean effectiveCompleted = completed
                || content.getRequiredDurationSeconds() > 0
                && studiedSeconds >= content.getRequiredDurationSeconds();
        repository.saveCoursewareProgress(studentId, courseId, contentId, studiedSeconds, effectiveCompleted);
    }

    private StudentCourseCard toCard(StudentCourseRecord record, LocalDateTime now) {
        StudentCourseCard card = new StudentCourseCard();
        card.setCourseId(record.getCourseId());
        card.setCourseName(record.getCourseName());
        card.setAcademicTerm(record.getAcademicTerm());
        card.setTeacherNames(record.getTeacherNames());
        card.setCoursewareCount(record.getCoursewareCount());
        card.setAssignmentCount(record.getAssignmentCount());
        card.setOpenStartTime(record.getOpenStartTime());
        card.setOpenEndTime(record.getOpenEndTime());
        card.setProgressPercent(CourseProgressCalculator.calculatePercent(
                record.getCompletedItems(), record.getTotalItems()));
        card.setStatus(status(record, now));
        return card;
    }

    private List<StudentCourseChapter> toChapters(List<StudentCourseContentRecord> records) {
        List<StudentCourseChapter> chapters = new ArrayList<StudentCourseChapter>();
        Long currentChapterId = null;
        StudentCourseChapter currentChapter = null;
        for (StudentCourseContentRecord record : records) {
            if (!record.getChapterId().equals(currentChapterId)) {
                currentChapter = new StudentCourseChapter();
                currentChapter.setChapterId(record.getChapterId());
                currentChapter.setChapterTitle(record.getChapterTitle());
                currentChapter.setSortOrder(record.getChapterSortOrder());
                chapters.add(currentChapter);
                currentChapterId = record.getChapterId();
            }
            currentChapter.getItems().add(toItem(record));
        }
        return chapters;
    }

    private StudentCourseItem toItem(StudentCourseContentRecord record) {
        StudentCourseItem item = new StudentCourseItem();
        item.setContentId(record.getContentId());
        item.setItemType(record.getItemType());
        item.setTitle(record.getTitle());
        item.setResourceId(record.getResourceId());
        item.setAssignmentId(record.getAssignmentId());
        item.setRequiredDurationSeconds(record.getRequiredDurationSeconds());
        item.setLearningStartTime(record.getLearningStartTime());
        item.setLearningEndTime(record.getLearningEndTime());
        item.setStudiedSeconds(record.getStudiedSeconds());
        item.setCompleted(record.isCompleted());
        item.setSortOrder(record.getSortOrder());
        return item;
    }

    private String status(StudentCourseRecord record, LocalDateTime now) {
        if (record.getOpenStartTime() != null && now.isBefore(record.getOpenStartTime())) {
            return "NOT_STARTED";
        }
        if (record.getOpenEndTime() != null && now.isAfter(record.getOpenEndTime())) {
            return "FINISHED";
        }
        return "STUDYING";
    }

    private boolean isOpen(LocalDateTime now, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && now.isBefore(startTime)) {
            return false;
        }
        return endTime == null || !now.isAfter(endTime);
    }

    private static class StudentCourseCardComparator implements Comparator<StudentCourseCard> {
        @Override
        public int compare(StudentCourseCard left, StudentCourseCard right) {
            int status = Integer.compare(statusRank(left.getStatus()), statusRank(right.getStatus()));
            if (status != 0) {
                return status;
            }
            LocalDateTime leftTime = "FINISHED".equals(left.getStatus())
                    ? left.getOpenEndTime()
                    : left.getOpenStartTime();
            LocalDateTime rightTime = "FINISHED".equals(right.getStatus())
                    ? right.getOpenEndTime()
                    : right.getOpenStartTime();
            int time = compareTime(leftTime, rightTime, "FINISHED".equals(left.getStatus()));
            if (time != 0) {
                return time;
            }
            return right.getCourseId().compareTo(left.getCourseId());
        }

        private static int statusRank(String status) {
            if ("STUDYING".equals(status)) {
                return 0;
            }
            if ("NOT_STARTED".equals(status)) {
                return 1;
            }
            return 2;
        }

        private static int compareTime(LocalDateTime left, LocalDateTime right, boolean asc) {
            if (left == null && right == null) {
                return 0;
            }
            if (left == null) {
                return 1;
            }
            if (right == null) {
                return -1;
            }
            return asc ? left.compareTo(right) : right.compareTo(left);
        }
    }
}
