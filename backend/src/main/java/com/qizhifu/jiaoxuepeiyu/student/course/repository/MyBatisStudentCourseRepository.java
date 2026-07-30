package com.qizhifu.jiaoxuepeiyu.student.course.repository;

import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseContentRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.port.StudentCourseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentCourseRepository implements StudentCourseRepository {

    private final StudentCourseMapper mapper;

    public MyBatisStudentCourseRepository(StudentCourseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<StudentCourseRecord> findPublishedCourses(Long studentId, String keyword) {
        String normalizedKeyword = keyword == null || keyword.trim().length() == 0 ? null : "%" + keyword.trim() + "%";
        return mapper.findPublishedCourses(studentId, normalizedKeyword);
    }

    @Override
    public Optional<StudentCourseRecord> findPublishedCourse(Long studentId, Long courseId) {
        return Optional.ofNullable(mapper.findPublishedCourse(studentId, courseId));
    }

    @Override
    public List<StudentCourseContentRecord> findCourseContents(Long studentId, Long courseId) {
        return mapper.findCourseContents(studentId, courseId);
    }

    @Override
    public Optional<StudentCourseContentRecord> findCoursewareContent(Long studentId, Long courseId, Long contentId) {
        return Optional.ofNullable(mapper.findCoursewareContent(studentId, courseId, contentId));
    }

    @Override
    public Optional<Long> findLastContentId(Long studentId, Long courseId) {
        return Optional.ofNullable(mapper.findLastContentId(studentId, courseId));
    }

    @Override
    public void saveCoursewareProgress(Long studentId,
                                       Long courseId,
                                       Long contentId,
                                       int studiedSeconds,
                                       boolean completed) {
        mapper.saveCoursewareProgress(studentId, courseId, contentId, studiedSeconds, completed ? 1 : 0);
        mapper.refreshCourseProgress(studentId, courseId);
    }
}
