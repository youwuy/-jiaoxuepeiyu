package com.qizhifu.jiaoxuepeiyu.student.course.port;

import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import java.util.List;

public interface StudentCourseRepository {

    List<StudentCourseRecord> findPublishedCourses(Long studentId, String keyword);
}
