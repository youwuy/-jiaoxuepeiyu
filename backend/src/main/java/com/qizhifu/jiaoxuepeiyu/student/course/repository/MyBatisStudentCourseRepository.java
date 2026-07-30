package com.qizhifu.jiaoxuepeiyu.student.course.repository;

import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.port.StudentCourseRepository;
import java.util.List;
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
}
