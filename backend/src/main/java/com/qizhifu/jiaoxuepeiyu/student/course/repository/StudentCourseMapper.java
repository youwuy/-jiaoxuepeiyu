package com.qizhifu.jiaoxuepeiyu.student.course.repository;

import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseContentRecord;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseRecord;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentCourseMapper {

    @Select("<script>"
            + "SELECT DISTINCT c.id AS course_id, c.course_name, c.academic_term, "
            + "c.courseware_count, c.assignment_count, "
            + "COALESCE(p.completed_items, 0) AS completed_items, "
            + "(c.courseware_count + c.assignment_count) AS total_items, "
            + "c.teacher_names, c.open_start_time, c.open_end_time "
            + "FROM course c "
            + "LEFT JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = COALESCE(cc.class_id, c.class_id) "
            + "LEFT JOIN course_learning_progress p ON p.course_id = c.id AND p.student_id = u.id "
            + "WHERE u.id = #{studentId} AND c.publish_status = 'PUBLISHED' "
            + "<if test='keyword != null'>AND c.course_name LIKE #{keyword}</if> "
            + "</script>")
    List<StudentCourseRecord> findPublishedCourses(@Param("studentId") Long studentId,
                                                   @Param("keyword") String keyword);

    @Select("SELECT c.id AS course_id, c.course_name, c.academic_term, "
            + "c.courseware_count, c.assignment_count, "
            + "COALESCE(p.completed_items, 0) AS completed_items, "
            + "(c.courseware_count + c.assignment_count) AS total_items, "
            + "c.teacher_names, c.open_start_time, c.open_end_time "
            + "FROM course c "
            + "LEFT JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = COALESCE(cc.class_id, c.class_id) "
            + "LEFT JOIN course_learning_progress p ON p.course_id = c.id AND p.student_id = u.id "
            + "WHERE u.id = #{studentId} AND c.id = #{courseId} AND c.publish_status = 'PUBLISHED' LIMIT 1")
    StudentCourseRecord findPublishedCourse(@Param("studentId") Long studentId,
                                            @Param("courseId") Long courseId);

    @Select("SELECT ch.id AS chapter_id, ch.chapter_title, ch.sort_order AS chapter_sort_order, "
            + "ct.id AS content_id, ct.item_type, ct.title, ct.assignment_id, ct.resource_id, "
            + "r.resource_type, r.file_name, r.file_url, r.preview_url, "
            + "ct.required_duration_seconds, ct.learning_start_time, ct.learning_end_time, "
            + "COALESCE(cp.studied_seconds, 0) AS studied_seconds, "
            + "CASE WHEN cp.completed = 1 THEN TRUE ELSE FALSE END AS completed, ct.sort_order "
            + "FROM course_content ct "
            + "JOIN course_chapter ch ON ch.id = ct.chapter_id "
            + "LEFT JOIN course_chapter parent_ch ON parent_ch.id = ch.parent_chapter_id "
            + "LEFT JOIN course_chapter root_ch ON root_ch.id = parent_ch.parent_chapter_id "
            + "JOIN course c ON c.id = ct.course_id "
            + "LEFT JOIN res_resource r ON r.id = ct.resource_id AND r.deleted_flag = 0 "
            + "LEFT JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = COALESCE(cc.class_id, c.class_id) "
            + "LEFT JOIN course_content_learning_progress cp "
            + "ON cp.content_id = ct.id AND cp.student_id = u.id "
            + "WHERE u.id = #{studentId} AND c.id = #{courseId} AND c.publish_status = 'PUBLISHED' "
            + "ORDER BY COALESCE(root_ch.sort_order, parent_ch.sort_order, ch.sort_order) ASC, "
            + "COALESCE(root_ch.id, parent_ch.id, ch.id) ASC, "
            + "CASE WHEN root_ch.id IS NULL AND parent_ch.id IS NULL THEN 0 "
            + "WHEN root_ch.id IS NULL THEN 1 ELSE 2 END ASC, "
            + "COALESCE(parent_ch.sort_order, ch.sort_order) ASC, COALESCE(parent_ch.id, ch.id) ASC, "
            + "ch.sort_order ASC, ch.id ASC, ct.sort_order ASC, ct.id ASC")
    List<StudentCourseContentRecord> findCourseContents(@Param("studentId") Long studentId,
                                                        @Param("courseId") Long courseId);

    @Select("SELECT ch.id AS chapter_id, ch.chapter_title, ch.sort_order AS chapter_sort_order, "
            + "ct.id AS content_id, ct.item_type, ct.title, ct.assignment_id, ct.resource_id, "
            + "r.resource_type, r.file_name, r.file_url, r.preview_url, "
            + "ct.required_duration_seconds, ct.learning_start_time, ct.learning_end_time, "
            + "COALESCE(cp.studied_seconds, 0) AS studied_seconds, "
            + "CASE WHEN cp.completed = 1 THEN TRUE ELSE FALSE END AS completed, ct.sort_order "
            + "FROM course_content ct "
            + "JOIN course_chapter ch ON ch.id = ct.chapter_id "
            + "JOIN course c ON c.id = ct.course_id "
            + "LEFT JOIN res_resource r ON r.id = ct.resource_id AND r.deleted_flag = 0 "
            + "LEFT JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = COALESCE(cc.class_id, c.class_id) "
            + "LEFT JOIN course_content_learning_progress cp "
            + "ON cp.content_id = ct.id AND cp.student_id = u.id "
            + "WHERE u.id = #{studentId} AND c.id = #{courseId} AND ct.id = #{contentId} "
            + "AND ct.item_type = 'COURSEWARE' AND c.publish_status = 'PUBLISHED' LIMIT 1")
    StudentCourseContentRecord findCoursewareContent(@Param("studentId") Long studentId,
                                                     @Param("courseId") Long courseId,
                                                     @Param("contentId") Long contentId);

    @Select("SELECT content_id FROM course_content_learning_progress "
            + "WHERE student_id = #{studentId} AND course_id = #{courseId} "
            + "ORDER BY updated_at DESC, id DESC LIMIT 1")
    Long findLastContentId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Insert("INSERT INTO course_content_learning_progress "
            + "(course_id, content_id, student_id, studied_seconds, completed, updated_at) "
            + "VALUES (#{courseId}, #{contentId}, #{studentId}, #{studiedSeconds}, #{completed}, NOW()) "
            + "ON DUPLICATE KEY UPDATE "
            + "studied_seconds = GREATEST(studied_seconds, VALUES(studied_seconds)), "
            + "completed = GREATEST(completed, VALUES(completed)), updated_at = NOW()")
    void saveCoursewareProgress(@Param("studentId") Long studentId,
                                @Param("courseId") Long courseId,
                                @Param("contentId") Long contentId,
                                @Param("studiedSeconds") int studiedSeconds,
                                @Param("completed") int completed);

    @Insert("INSERT INTO course_learning_progress (course_id, student_id, completed_items, updated_at) "
            + "SELECT #{courseId}, #{studentId}, COUNT(*), NOW() "
            + "FROM course_content ct "
            + "LEFT JOIN course_content_learning_progress cp "
            + "ON cp.content_id = ct.id AND cp.student_id = #{studentId} "
            + "LEFT JOIN course_assignment a ON a.content_id = ct.id "
            + "LEFT JOIN assignment_attempt aa "
            + "ON aa.assignment_id = a.id AND aa.student_id = #{studentId} "
            + "AND aa.status IN ('SUBMITTED', 'REVIEWED') "
            + "WHERE ct.course_id = #{courseId} "
            + "AND ((ct.item_type = 'COURSEWARE' AND cp.completed = 1) "
            + "OR (ct.item_type = 'ASSIGNMENT' AND aa.id IS NOT NULL "
            + "AND (a.completion_rule = 'SUBMIT' "
            + "OR (a.completion_rule = 'PASS_SCORE' AND aa.score IS NOT NULL AND aa.score >= COALESCE(a.pass_score, 0))))) "
            + "ON DUPLICATE KEY UPDATE completed_items = VALUES(completed_items), updated_at = NOW()")
    void refreshCourseProgress(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
