package com.qizhifu.jiaoxuepeiyu.admin.course.port;

import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourse;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseLog;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStatistics;
import java.util.Collections;
import java.util.List;

public interface AdminCourseRepository {

    List<AdminCourse> findCourses(AdminCourseQuery query);

    long countCourses(AdminCourseQuery query);

    AdminCourse findCourse(Long courseId);

    Long createCourse(AdminCourseCommand command, Long creatorId);

    void updateCourse(Long courseId, AdminCourseCommand command);

    void updatePublishStatus(Long courseId, String publishStatus);

    void deleteCourse(Long courseId);

    Long copyCourse(Long sourceCourseId, Long creatorId);

    void notifyBoundStudents(Long courseId, String title, String content);

    AdminCourseStatistics calculateStatistics(Long courseId);

    void appendCourseLog(Long courseId, Long operatorId, String action, String content);

    default List<AdminCourseLog> findCourseLogs(Long courseId) {
        return Collections.emptyList();
    }
}
