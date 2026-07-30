package com.qizhifu.jiaoxuepeiyu.student.course.repository;

import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentCourseMapper {

    @Select("<script>"
            + "SELECT c.id AS course_id, c.course_name, c.academic_term, "
            + "c.courseware_count, c.assignment_count, "
            + "COALESCE(p.completed_items, 0) AS completed_items, "
            + "(c.courseware_count + c.assignment_count) AS total_items, "
            + "c.teacher_names, c.open_start_time, c.open_end_time "
            + "FROM course c "
            + "JOIN sys_user u ON u.class_id = c.class_id "
            + "LEFT JOIN course_learning_progress p ON p.course_id = c.id AND p.student_id = u.id "
            + "WHERE u.id = #{studentId} AND c.publish_status = 'PUBLISHED' "
            + "<if test='keyword != null'>AND c.course_name LIKE #{keyword}</if> "
            + "</script>")
    List<StudentCourseRecord> findPublishedCourses(@Param("studentId") Long studentId,
                                                   @Param("keyword") String keyword);
}
