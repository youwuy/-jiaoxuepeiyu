package com.qizhifu.jiaoxuepeiyu.student.course.port;

import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseContentRecord;
import java.util.List;
import java.util.Optional;

public interface StudentCourseRepository {

    List<StudentCourseRecord> findPublishedCourses(Long studentId, String keyword);

    Optional<StudentCourseRecord> findPublishedCourse(Long studentId, Long courseId);

    List<StudentCourseContentRecord> findCourseContents(Long studentId, Long courseId);

    Optional<StudentCourseContentRecord> findCoursewareContent(Long studentId, Long courseId, Long contentId);

    Optional<Long> findLastContentId(Long studentId, Long courseId);

    void saveCoursewareProgress(Long studentId,
                                Long courseId,
                                Long contentId,
                                int studiedSeconds,
                                boolean completed);
}
