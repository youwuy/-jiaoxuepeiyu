package com.qizhifu.jiaoxuepeiyu.student.course;

import com.qizhifu.jiaoxuepeiyu.domain.course.CourseProgressCalculator;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseCard;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.port.StudentCourseRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudentCourseService {

    private final StudentCourseRepository repository;
    private final Clock clock;

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

    private String status(StudentCourseRecord record, LocalDateTime now) {
        if (record.getOpenStartTime() != null && now.isBefore(record.getOpenStartTime())) {
            return "NOT_STARTED";
        }
        if (record.getOpenEndTime() != null && now.isAfter(record.getOpenEndTime())) {
            return "FINISHED";
        }
        return "STUDYING";
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
