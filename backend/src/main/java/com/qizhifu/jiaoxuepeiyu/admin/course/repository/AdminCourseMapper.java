package com.qizhifu.jiaoxuepeiyu.admin.course.repository;

import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourse;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseChapter;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseContent;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseLog;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStatistics;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminCourseMapper {

    @Select("<script>"
            + "SELECT DISTINCT c.id AS course_id, c.course_name, c.academic_year_id, ay.year_name AS academic_year_name, "
            + "c.semester_id, s.semester_name, c.academic_term, c.major_id, m.major_name, c.class_id, "
            + "c.cover_url, c.teacher_names, c.class_names, c.learning_mode, c.assignment_completion_rule, "
            + "c.courseware_score_cap, c.courseware_count, c.assignment_count, "
            + "(SELECT COUNT(*) FROM assignment_attempt aa "
            + "JOIN course_assignment ca_pending ON ca_pending.id = aa.assignment_id "
            + "WHERE ca_pending.course_id = c.id AND aa.status = 'SUBMITTED') AS pending_review_count, "
            + "c.publish_status, "
            + "c.open_start_time, c.open_end_time, c.created_by, u.real_name AS creator_name, c.created_at, c.updated_at "
            + "FROM course c "
            + "LEFT JOIN edu_academic_year ay ON ay.id = c.academic_year_id "
            + "LEFT JOIN edu_semester s ON s.id = c.semester_id "
            + "LEFT JOIN edu_major m ON m.id = c.major_id "
            + "LEFT JOIN sys_user u ON u.id = c.created_by "
            + "<if test='classId != null'>JOIN course_class cc_filter ON cc_filter.course_id = c.id AND cc_filter.class_id = #{classId}</if> "
            + "<if test='teacherId != null'>JOIN course_teacher ct_filter ON ct_filter.course_id = c.id AND ct_filter.teacher_id = #{teacherId}</if> "
            + "WHERE c.deleted_flag = 0 "
            + "<if test='keyword != null'>AND c.course_name LIKE #{keyword}</if> "
            + "<if test='academicYearId != null'>AND c.academic_year_id = #{academicYearId}</if> "
            + "<if test='semesterId != null'>AND c.semester_id = #{semesterId}</if> "
            + "<if test='majorId != null'>AND c.major_id = #{majorId}</if> "
            + "<if test='teachingStartTime != null'>AND c.open_end_time &gt;= #{teachingStartTime}</if> "
            + "<if test='teachingEndTime != null'>AND c.open_start_time &lt;= #{teachingEndTime}</if> "
            + "<if test='publishStatus != null and publishStatus == \"PUBLISHED\"'>AND c.publish_status = 'PUBLISHED'</if> "
            + "<if test='publishStatus != null and publishStatus == \"NOT_PUBLISHED\"'>AND c.publish_status &lt;&gt; 'PUBLISHED'</if> "
            + "<if test='publishStatus != null and publishStatus != \"PUBLISHED\" and publishStatus != \"NOT_PUBLISHED\"'>AND c.publish_status = #{publishStatus}</if> "
            + "ORDER BY c.updated_at DESC, c.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    @Results(id = "courseMap", value = {
            @Result(column = "course_id", property = "courseId", id = true),
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "academic_year_id", property = "academicYearId"),
            @Result(column = "academic_year_name", property = "academicYearName"),
            @Result(column = "semester_id", property = "semesterId"),
            @Result(column = "semester_name", property = "semesterName"),
            @Result(column = "academic_term", property = "academicTerm"),
            @Result(column = "major_id", property = "majorId"),
            @Result(column = "major_name", property = "majorName"),
            @Result(column = "class_id", property = "classId"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "teacher_names", property = "teacherNames"),
            @Result(column = "class_names", property = "classNames"),
            @Result(column = "learning_mode", property = "learningMode"),
            @Result(column = "assignment_completion_rule", property = "assignmentCompletionRule"),
            @Result(column = "courseware_score_cap", property = "coursewareScoreCap"),
            @Result(column = "courseware_count", property = "coursewareCount"),
            @Result(column = "assignment_count", property = "assignmentCount"),
            @Result(column = "pending_review_count", property = "pendingReviewCount"),
            @Result(column = "publish_status", property = "publishStatus"),
            @Result(column = "open_start_time", property = "openStartTime"),
            @Result(column = "open_end_time", property = "openEndTime"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "course_id", property = "teacherIds", many = @Many(select = "findTeacherIds")),
            @Result(column = "course_id", property = "classIds", many = @Many(select = "findClassIds")),
            @Result(column = "course_id", property = "chapters", many = @Many(select = "findChapters"))
    })
    List<AdminCourse> findCourses(AdminCourseQuery query);

    @Select("<script>"
            + "SELECT COUNT(DISTINCT c.id) FROM course c "
            + "<if test='classId != null'>JOIN course_class cc_filter ON cc_filter.course_id = c.id AND cc_filter.class_id = #{classId}</if> "
            + "<if test='teacherId != null'>JOIN course_teacher ct_filter ON ct_filter.course_id = c.id AND ct_filter.teacher_id = #{teacherId}</if> "
            + "WHERE c.deleted_flag = 0 "
            + "<if test='keyword != null'>AND c.course_name LIKE #{keyword}</if> "
            + "<if test='academicYearId != null'>AND c.academic_year_id = #{academicYearId}</if> "
            + "<if test='semesterId != null'>AND c.semester_id = #{semesterId}</if> "
            + "<if test='majorId != null'>AND c.major_id = #{majorId}</if> "
            + "<if test='teachingStartTime != null'>AND c.open_end_time &gt;= #{teachingStartTime}</if> "
            + "<if test='teachingEndTime != null'>AND c.open_start_time &lt;= #{teachingEndTime}</if> "
            + "<if test='publishStatus != null and publishStatus == \"PUBLISHED\"'>AND c.publish_status = 'PUBLISHED'</if> "
            + "<if test='publishStatus != null and publishStatus == \"NOT_PUBLISHED\"'>AND c.publish_status &lt;&gt; 'PUBLISHED'</if> "
            + "<if test='publishStatus != null and publishStatus != \"PUBLISHED\" and publishStatus != \"NOT_PUBLISHED\"'>AND c.publish_status = #{publishStatus}</if> "
            + "</script>")
    long countCourses(AdminCourseQuery query);

    @Select("SELECT c.id AS course_id, c.course_name, c.academic_year_id, ay.year_name AS academic_year_name, "
            + "c.semester_id, s.semester_name, c.academic_term, c.major_id, m.major_name, c.class_id, "
            + "c.cover_url, c.teacher_names, c.class_names, c.learning_mode, c.assignment_completion_rule, "
            + "c.courseware_score_cap, c.courseware_count, c.assignment_count, c.publish_status, "
            + "c.open_start_time, c.open_end_time, c.created_by, u.real_name AS creator_name, c.created_at, c.updated_at "
            + "FROM course c "
            + "LEFT JOIN edu_academic_year ay ON ay.id = c.academic_year_id "
            + "LEFT JOIN edu_semester s ON s.id = c.semester_id "
            + "LEFT JOIN edu_major m ON m.id = c.major_id "
            + "LEFT JOIN sys_user u ON u.id = c.created_by "
            + "WHERE c.id = #{courseId} AND c.deleted_flag = 0 LIMIT 1")
    @Results(id = "courseDetailMap", value = {
            @Result(column = "course_id", property = "courseId", id = true),
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "academic_year_id", property = "academicYearId"),
            @Result(column = "academic_year_name", property = "academicYearName"),
            @Result(column = "semester_id", property = "semesterId"),
            @Result(column = "semester_name", property = "semesterName"),
            @Result(column = "academic_term", property = "academicTerm"),
            @Result(column = "major_id", property = "majorId"),
            @Result(column = "major_name", property = "majorName"),
            @Result(column = "class_id", property = "classId"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "teacher_names", property = "teacherNames"),
            @Result(column = "class_names", property = "classNames"),
            @Result(column = "learning_mode", property = "learningMode"),
            @Result(column = "assignment_completion_rule", property = "assignmentCompletionRule"),
            @Result(column = "courseware_score_cap", property = "coursewareScoreCap"),
            @Result(column = "courseware_count", property = "coursewareCount"),
            @Result(column = "assignment_count", property = "assignmentCount"),
            @Result(column = "publish_status", property = "publishStatus"),
            @Result(column = "open_start_time", property = "openStartTime"),
            @Result(column = "open_end_time", property = "openEndTime"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "course_id", property = "teacherIds", many = @Many(select = "findTeacherIds")),
            @Result(column = "course_id", property = "classIds", many = @Many(select = "findClassIds")),
            @Result(column = "course_id", property = "chapters", many = @Many(select = "findChapters"))
    })
    AdminCourse findCourse(@Param("courseId") Long courseId);

    @Select("SELECT teacher_id FROM course_teacher WHERE course_id = #{courseId} ORDER BY sort_order ASC, id ASC")
    List<Long> findTeacherIds(@Param("courseId") Long courseId);

    @Select("SELECT class_id FROM course_class WHERE course_id = #{courseId} ORDER BY sort_order ASC, id ASC")
    List<Long> findClassIds(@Param("courseId") Long courseId);

    @Select("SELECT ch.id AS chapter_id, ch.course_id, ch.parent_chapter_id, ch.chapter_title, ch.sort_order "
            + "FROM course_chapter ch WHERE ch.course_id = #{courseId} "
            + "AND ch.parent_chapter_id IS NULL ORDER BY ch.sort_order ASC, ch.id ASC")
    @Results(id = "courseChapterMap", value = {
            @Result(column = "chapter_id", property = "chapterId", id = true),
            @Result(column = "course_id", property = "courseId"),
            @Result(column = "parent_chapter_id", property = "parentChapterId"),
            @Result(column = "chapter_title", property = "chapterTitle"),
            @Result(column = "sort_order", property = "sortOrder"),
            @Result(column = "chapter_id", property = "contents", many = @Many(select = "findContents")),
            @Result(column = "chapter_id", property = "children", many = @Many(select = "findChildChapters"))
    })
    List<AdminCourseChapter> findChapters(@Param("courseId") Long courseId);

    @Select("SELECT ch.id AS chapter_id, ch.course_id, ch.parent_chapter_id, ch.chapter_title, ch.sort_order "
            + "FROM course_chapter ch WHERE ch.parent_chapter_id = #{parentChapterId} "
            + "ORDER BY ch.sort_order ASC, ch.id ASC")
    @ResultMap("courseChapterMap")
    List<AdminCourseChapter> findChildChapters(@Param("parentChapterId") Long parentChapterId);

    @Select("SELECT ct.id AS content_id, ct.chapter_id, ct.item_type, ct.title, ct.resource_id, ct.assignment_id, "
            + "ct.required_duration_seconds, ct.learning_start_time, ct.learning_end_time, "
            + "a.completion_rule AS assignment_completion_rule, a.pass_score, "
            + "a.publish_mode AS assignment_publish_mode, a.answer_start_time, a.answer_end_time, "
            + "a.total_score AS assignment_total_score, ct.sort_order "
            + "FROM course_content ct "
            + "LEFT JOIN course_assignment a ON a.id = ct.assignment_id "
            + "WHERE ct.chapter_id = #{chapterId} ORDER BY ct.sort_order ASC, ct.id ASC")
    List<AdminCourseContent> findContents(@Param("chapterId") Long chapterId);

    @Insert("INSERT INTO course "
            + "(class_id, course_name, academic_year_id, semester_id, academic_term, major_id, cover_url, "
            + "teacher_names, class_names, learning_mode, assignment_completion_rule, courseware_score_cap, "
            + "courseware_count, assignment_count, publish_status, open_start_time, open_end_time, "
            + "created_by, deleted_flag, created_at, updated_at) "
            + "VALUES (#{classId}, #{courseName}, #{academicYearId}, #{semesterId}, #{academicTerm}, "
            + "#{majorId}, #{coverUrl}, #{teacherNames}, #{classNames}, #{learningMode}, "
            + "#{assignmentCompletionRule}, #{coursewareScoreCap}, #{coursewareCount}, #{assignmentCount}, "
            + "#{publishStatus}, #{openStartTime}, #{openEndTime}, #{createdBy}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "courseId")
    void insertCourse(AdminCourse course);

    @Update("UPDATE course SET class_id = #{classId}, course_name = #{courseName}, "
            + "academic_year_id = #{academicYearId}, semester_id = #{semesterId}, academic_term = #{academicTerm}, "
            + "major_id = #{majorId}, cover_url = #{coverUrl}, teacher_names = #{teacherNames}, "
            + "class_names = #{classNames}, learning_mode = #{learningMode}, "
            + "assignment_completion_rule = #{assignmentCompletionRule}, courseware_score_cap = #{coursewareScoreCap}, "
            + "courseware_count = #{coursewareCount}, assignment_count = #{assignmentCount}, "
            + "open_start_time = #{openStartTime}, open_end_time = #{openEndTime}, updated_at = NOW() "
            + "WHERE id = #{courseId} AND deleted_flag = 0")
    void updateCourse(AdminCourse course);

    @Delete("DELETE FROM course_teacher WHERE course_id = #{courseId}")
    void deleteTeachers(@Param("courseId") Long courseId);

    @Insert("INSERT INTO course_teacher (course_id, teacher_id, sort_order, created_at) "
            + "VALUES (#{courseId}, #{teacherId}, #{sortOrder}, NOW())")
    void insertTeacher(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId, @Param("sortOrder") int sortOrder);

    @Delete("DELETE FROM course_class WHERE course_id = #{courseId}")
    void deleteClasses(@Param("courseId") Long courseId);

    @Insert("INSERT INTO course_class (course_id, class_id, sort_order, created_at) "
            + "VALUES (#{courseId}, #{classId}, #{sortOrder}, NOW())")
    void insertClass(@Param("courseId") Long courseId, @Param("classId") Long classId, @Param("sortOrder") int sortOrder);

    @Delete("DELETE FROM course_content WHERE course_id = #{courseId}")
    void deleteContents(@Param("courseId") Long courseId);

    @Delete("DELETE FROM course_chapter WHERE course_id = #{courseId}")
    void deleteChapters(@Param("courseId") Long courseId);

    @Insert("INSERT INTO course_chapter (course_id, parent_chapter_id, chapter_title, sort_order, created_at, updated_at) "
            + "VALUES (#{courseId}, #{chapter.parentChapterId}, #{chapter.chapterTitle}, #{chapter.sortOrder}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "chapter.chapterId")
    void insertChapter(@Param("courseId") Long courseId, @Param("chapter") AdminCourseChapter chapter);

    @Insert("INSERT INTO course_content "
            + "(course_id, chapter_id, item_type, title, resource_id, assignment_id, required_duration_seconds, "
            + "learning_start_time, learning_end_time, sort_order, created_at, updated_at) "
            + "VALUES (#{courseId}, #{chapterId}, #{content.itemType}, #{content.title}, #{content.resourceId}, "
            + "#{content.assignmentId}, #{content.requiredDurationSeconds}, #{content.learningStartTime}, "
            + "#{content.learningEndTime}, #{content.sortOrder}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "content.contentId")
    void insertContent(@Param("courseId") Long courseId,
                       @Param("chapterId") Long chapterId,
                       @Param("content") AdminCourseContent content);

    @Update("UPDATE course_assignment SET course_id = #{courseId}, content_id = #{contentId}, "
            + "completion_rule = #{content.assignmentCompletionRule}, pass_score = #{content.passScore}, "
            + "publish_mode = #{content.assignmentPublishMode}, "
            + "answer_start_time = #{content.answerStartTime}, answer_end_time = #{content.answerEndTime}, "
            + "deadline = #{content.answerEndTime}, updated_at = NOW() "
            + "WHERE id = #{assignmentId}")
    void updateAssignmentContent(@Param("assignmentId") Long assignmentId,
                                 @Param("courseId") Long courseId,
                                 @Param("contentId") Long contentId,
                                 @Param("content") AdminCourseContent content);

    @Update("UPDATE course SET publish_status = #{publishStatus}, updated_at = NOW() "
            + "WHERE id = #{courseId} AND deleted_flag = 0")
    void updatePublishStatus(@Param("courseId") Long courseId, @Param("publishStatus") String publishStatus);

    @Update("UPDATE course SET deleted_flag = 1, publish_status = 'OFFLINE', updated_at = NOW() "
            + "WHERE id = #{courseId} AND deleted_flag = 0")
    void deleteCourse(@Param("courseId") Long courseId);

    @Select("SELECT GROUP_CONCAT(real_name ORDER BY id SEPARATOR ', ') FROM sys_user WHERE id IN "
            + "(SELECT teacher_id FROM course_teacher WHERE course_id = #{courseId})")
    String findTeacherNames(@Param("courseId") Long courseId);

    @Select("SELECT GROUP_CONCAT(class_name ORDER BY id SEPARATOR ', ') FROM edu_class WHERE id IN "
            + "(SELECT class_id FROM course_class WHERE course_id = #{courseId})")
    String findClassNames(@Param("courseId") Long courseId);

    @Select("SELECT CONCAT(ay.year_name, ' ', s.semester_name) FROM edu_semester s "
            + "JOIN edu_academic_year ay ON ay.id = s.academic_year_id WHERE s.id = #{semesterId} LIMIT 1")
    String findAcademicTerm(@Param("semesterId") Long semesterId);

    @Select("<script>"
            + "SELECT GROUP_CONCAT(real_name ORDER BY id SEPARATOR ', ') FROM sys_user WHERE id IN "
            + "<foreach collection='teacherIds' item='teacherId' open='(' separator=',' close=')'>#{teacherId}</foreach>"
            + "</script>")
    String findTeacherNamesByIds(@Param("teacherIds") List<Long> teacherIds);

    @Select("<script>"
            + "SELECT GROUP_CONCAT(class_name ORDER BY id SEPARATOR ', ') FROM edu_class WHERE id IN "
            + "<foreach collection='classIds' item='classId' open='(' separator=',' close=')'>#{classId}</foreach>"
            + "</script>")
    String findClassNamesByIds(@Param("classIds") List<Long> classIds);

    @Insert("INSERT INTO msg_notification (message_type, title, content, source_id, created_at) "
            + "VALUES ('COURSE', #{title}, #{content}, #{sourceId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "notificationId")
    void insertNotification(AdminCourseNotification notification);

    @Insert("INSERT IGNORE INTO msg_user_notification (notification_id, user_id, read_flag, created_at) "
            + "SELECT #{notificationId}, u.id, 0, NOW() FROM sys_user u "
            + "JOIN course_class cc ON cc.class_id = u.class_id "
            + "WHERE cc.course_id = #{courseId} AND u.user_type = 'student' AND u.status = 1")
    void notifyBoundStudents(@Param("courseId") Long courseId, @Param("notificationId") Long notificationId);

    @Select("SELECT #{courseId} AS course_id, COUNT(DISTINCT u.id) AS student_count, "
            + "SUM(CASE WHEN p.completed_items >= (c.courseware_count + c.assignment_count) "
            + "AND (c.courseware_count + c.assignment_count) > 0 THEN 1 ELSE 0 END) AS completed_count, "
            + "SUM(CASE WHEN COALESCE(p.completed_items, 0) > 0 "
            + "AND COALESCE(p.completed_items, 0) < (c.courseware_count + c.assignment_count) THEN 1 ELSE 0 END) AS studying_count, "
            + "SUM(CASE WHEN COALESCE(p.completed_items, 0) = 0 THEN 1 ELSE 0 END) AS not_started_count, "
            + "(SELECT COUNT(*) FROM assignment_attempt aa "
            + "JOIN course_assignment ca ON ca.id = aa.assignment_id "
            + "WHERE ca.course_id = #{courseId} AND aa.status = 'SUBMITTED') AS pending_review_count, "
            + "(SELECT AVG(aa.score) FROM assignment_attempt aa "
            + "JOIN course_assignment ca ON ca.id = aa.assignment_id "
            + "WHERE ca.course_id = #{courseId} AND aa.score IS NOT NULL) AS average_score "
            + "FROM course c "
            + "JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = cc.class_id AND u.user_type = 'student' AND u.status = 1 "
            + "LEFT JOIN course_learning_progress p ON p.course_id = c.id AND p.student_id = u.id "
            + "WHERE c.id = #{courseId}")
    AdminCourseStatistics calculateStatistics(@Param("courseId") Long courseId);

    @Insert("INSERT INTO course_log (course_id, operator_id, action, content, created_at) "
            + "VALUES (#{courseId}, #{operatorId}, #{action}, #{content}, NOW())")
    void insertCourseLog(@Param("courseId") Long courseId,
                         @Param("operatorId") Long operatorId,
                         @Param("action") String action,
                         @Param("content") String content);

    @Select("SELECT l.id AS log_id, l.course_id, l.operator_id, u.real_name AS operator_name, "
            + "l.action, l.content, l.created_at "
            + "FROM course_log l LEFT JOIN sys_user u ON u.id = l.operator_id "
            + "WHERE l.course_id = #{courseId} ORDER BY l.created_at DESC, l.id DESC")
    List<AdminCourseLog> findCourseLogs(@Param("courseId") Long courseId);
}
