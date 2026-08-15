package com.qizhifu.jiaoxuepeiyu.admin.course.repository;

import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourse;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseChapter;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseContent;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseLog;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentContentStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStudentStatisticsQuery;
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
            + "JOIN course_content ct_pending ON ct_pending.id = ca_pending.content_id AND ct_pending.deleted_flag = 0 "
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
            + "AND ch.deleted_flag = 0 AND ch.parent_chapter_id IS NULL ORDER BY ch.sort_order ASC, ch.id ASC")
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
            + "AND ch.deleted_flag = 0 ORDER BY ch.sort_order ASC, ch.id ASC")
    @ResultMap("courseChapterMap")
    List<AdminCourseChapter> findChildChapters(@Param("parentChapterId") Long parentChapterId);

    @Select("SELECT ct.id AS content_id, ct.chapter_id, ct.item_type, ct.title, ct.resource_id, ct.assignment_id, "
            + "a.assignment_type, ct.required_duration_seconds, ct.learning_start_time, ct.learning_end_time, "
            + "a.completion_rule AS assignment_completion_rule, a.pass_score, "
            + "a.publish_mode AS assignment_publish_mode, a.answer_start_time, a.answer_end_time, "
            + "a.total_score AS assignment_total_score, ct.sort_order "
            + "FROM course_content ct "
            + "LEFT JOIN course_assignment a ON a.id = ct.assignment_id "
            + "WHERE ct.chapter_id = #{chapterId} AND ct.deleted_flag = 0 ORDER BY ct.sort_order ASC, ct.id ASC")
    @Results(id = "courseContentMap", value = {
            @Result(column = "content_id", property = "contentId", id = true),
            @Result(column = "assignment_id", property = "assignmentId"),
            @Result(column = "assignment_id", property = "questionIds", many = @Many(select = "findAssignmentQuestionIds")),
            @Result(column = "assignment_id", property = "trainingIds", many = @Many(select = "findAssignmentTrainingIds"))
    })
    List<AdminCourseContent> findContents(@Param("chapterId") Long chapterId);

    @Select("SELECT source_question_id FROM assignment_question "
            + "WHERE assignment_id = #{assignmentId} AND source_question_id IS NOT NULL "
            + "ORDER BY sort_order ASC, id ASC")
    List<Long> findAssignmentQuestionIds(@Param("assignmentId") Long assignmentId);

    @Select("SELECT training_id FROM assignment_training WHERE assignment_id = #{assignmentId} "
            + "ORDER BY sort_order ASC, id ASC")
    List<Long> findAssignmentTrainingIds(@Param("assignmentId") Long assignmentId);

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

    @Update("<script>UPDATE course_assignment a JOIN course_content ct ON ct.id = a.content_id "
            + "SET a.publish_status = 'OFFLINE', a.updated_at = NOW() "
            + "WHERE ct.course_id = #{courseId} AND ct.deleted_flag = 0 "
            + "<if test='retainedContentIds != null and retainedContentIds.size() > 0'>"
            + "AND ct.id NOT IN <foreach collection='retainedContentIds' item='contentId' open='(' separator=',' close=')'>#{contentId}</foreach>"
            + "</if></script>")
    void offlineRemovedAssignments(@Param("courseId") Long courseId,
                                   @Param("retainedContentIds") List<Long> retainedContentIds);

    @Update("<script>UPDATE course_content SET deleted_flag = 1, updated_at = NOW() "
            + "WHERE course_id = #{courseId} AND deleted_flag = 0 "
            + "<if test='retainedContentIds != null and retainedContentIds.size() > 0'>"
            + "AND id NOT IN <foreach collection='retainedContentIds' item='contentId' open='(' separator=',' close=')'>#{contentId}</foreach>"
            + "</if></script>")
    void softDeleteContentsExcept(@Param("courseId") Long courseId,
                                  @Param("retainedContentIds") List<Long> retainedContentIds);

    @Update("<script>UPDATE course_chapter SET deleted_flag = 1, updated_at = NOW() "
            + "WHERE course_id = #{courseId} AND deleted_flag = 0 "
            + "<if test='retainedChapterIds != null and retainedChapterIds.size() > 0'>"
            + "AND id NOT IN <foreach collection='retainedChapterIds' item='chapterId' open='(' separator=',' close=')'>#{chapterId}</foreach>"
            + "</if></script>")
    void softDeleteChaptersExcept(@Param("courseId") Long courseId,
                                  @Param("retainedChapterIds") List<Long> retainedChapterIds);

    @Insert("INSERT INTO course_chapter (course_id, parent_chapter_id, chapter_title, sort_order, created_at, updated_at) "
            + "VALUES (#{courseId}, #{chapter.parentChapterId}, #{chapter.chapterTitle}, #{chapter.sortOrder}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "chapter.chapterId")
    void insertChapter(@Param("courseId") Long courseId, @Param("chapter") AdminCourseChapter chapter);

    @Update("UPDATE course_chapter SET parent_chapter_id = #{chapter.parentChapterId}, "
            + "chapter_title = #{chapter.chapterTitle}, sort_order = #{chapter.sortOrder}, updated_at = NOW() "
            + "WHERE id = #{chapter.chapterId} AND course_id = #{courseId} AND deleted_flag = 0")
    int updateChapter(@Param("courseId") Long courseId, @Param("chapter") AdminCourseChapter chapter);

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

    @Update("UPDATE course_content SET chapter_id = #{chapterId}, item_type = #{content.itemType}, "
            + "title = #{content.title}, resource_id = #{content.resourceId}, assignment_id = #{content.assignmentId}, "
            + "required_duration_seconds = #{content.requiredDurationSeconds}, "
            + "learning_start_time = #{content.learningStartTime}, learning_end_time = #{content.learningEndTime}, "
            + "sort_order = #{content.sortOrder}, updated_at = NOW() "
            + "WHERE id = #{content.contentId} AND course_id = #{courseId} AND deleted_flag = 0")
    int updateContent(@Param("courseId") Long courseId,
                      @Param("chapterId") Long chapterId,
                      @Param("content") AdminCourseContent content);

    @Insert("INSERT INTO course_assignment "
            + "(course_id, content_id, assignment_title, assignment_type, deadline, answer_start_time, answer_end_time, "
            + "completion_rule, pass_score, publish_mode, total_score, publish_status, created_at, updated_at) "
            + "VALUES (#{courseId}, #{contentId}, #{content.title}, #{content.assignmentType}, #{content.answerEndTime}, "
            + "#{content.answerStartTime}, #{content.answerEndTime}, #{content.assignmentCompletionRule}, "
            + "#{content.passScore}, #{content.assignmentPublishMode}, #{content.assignmentTotalScore}, "
            + "'DRAFT', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "content.assignmentId")
    void insertAssignment(@Param("courseId") Long courseId,
                          @Param("contentId") Long contentId,
                          @Param("content") AdminCourseContent content);

    @Update("UPDATE course_content SET assignment_id = #{assignmentId}, updated_at = NOW() WHERE id = #{contentId}")
    void updateContentAssignmentId(@Param("contentId") Long contentId,
                                   @Param("assignmentId") Long assignmentId);

    @Update("UPDATE course_assignment SET course_id = #{courseId}, content_id = #{contentId}, "
            + "assignment_type = #{content.assignmentType}, completion_rule = #{content.assignmentCompletionRule}, pass_score = #{content.passScore}, "
            + "publish_mode = #{content.assignmentPublishMode}, "
            + "answer_start_time = #{content.answerStartTime}, answer_end_time = #{content.answerEndTime}, "
            + "deadline = #{content.answerEndTime}, updated_at = NOW() "
            + "WHERE id = #{assignmentId}")
    void updateAssignmentContent(@Param("assignmentId") Long assignmentId,
                                 @Param("courseId") Long courseId,
                                 @Param("contentId") Long contentId,
                                 @Param("content") AdminCourseContent content);

    @Delete("DELETE FROM assignment_question WHERE assignment_id = #{assignmentId}")
    void deleteAssignmentQuestions(@Param("assignmentId") Long assignmentId);

    @Delete("DELETE FROM assignment_training WHERE assignment_id = #{assignmentId}")
    void deleteAssignmentTrainings(@Param("assignmentId") Long assignmentId);

    @Insert("INSERT INTO assignment_training (assignment_id, training_id, sort_order, created_at, updated_at) "
            + "SELECT #{assignmentId}, id, #{sortOrder}, NOW(), NOW() FROM training_course "
            + "WHERE id = #{trainingId} AND deleted_flag = 0")
    int insertAssignmentTraining(@Param("assignmentId") Long assignmentId,
                                 @Param("trainingId") Long trainingId,
                                 @Param("sortOrder") int sortOrder);

    @Insert("INSERT INTO assignment_question "
            + "(assignment_id, source_question_id, question_type, title, options_json, standard_answer, score, sort_order, created_at, updated_at) "
            + "SELECT #{assignmentId}, q.id, q.question_type, q.title, "
            + "(SELECT CONCAT('[', GROUP_CONCAT(JSON_OBJECT('optionKey', o.option_key, 'optionText', o.option_text) "
            + "ORDER BY o.sort_order ASC, o.id ASC SEPARATOR ','), ']') "
            + "FROM exam_question_option o WHERE o.question_id = q.id), "
            + "q.standard_answer, q.score, #{sortOrder}, NOW(), NOW() "
            + "FROM exam_question q WHERE q.id = #{questionId} AND q.deleted_flag = 0 AND q.enabled_flag = 1")
    int insertAssignmentQuestionFromBank(@Param("assignmentId") Long assignmentId,
                                         @Param("questionId") Long questionId,
                                         @Param("sortOrder") int sortOrder);

    @Update("UPDATE course_assignment SET total_score = "
            + "(SELECT COALESCE(SUM(score), 0) FROM assignment_question WHERE assignment_id = #{assignmentId}), "
            + "updated_at = NOW() WHERE id = #{assignmentId}")
    void refreshAssignmentTotalScore(@Param("assignmentId") Long assignmentId);

    @Update("UPDATE course_assignment a JOIN course_content ct ON ct.id = a.content_id "
            + "SET a.publish_status = #{publishStatus}, a.updated_at = NOW() "
            + "WHERE a.course_id = #{courseId} AND ct.deleted_flag = 0")
    void updateAssignmentPublishStatus(@Param("courseId") Long courseId,
                                       @Param("publishStatus") String publishStatus);

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
            + "JOIN course_content ct_active ON ct_active.id = ca.content_id AND ct_active.deleted_flag = 0 "
            + "WHERE ca.course_id = #{courseId} AND aa.status = 'SUBMITTED') AS pending_review_count, "
            + "(SELECT AVG(aa.score) FROM assignment_attempt aa "
            + "JOIN course_assignment ca ON ca.id = aa.assignment_id "
            + "JOIN course_content ct_active ON ct_active.id = ca.content_id AND ct_active.deleted_flag = 0 "
            + "WHERE ca.course_id = #{courseId} AND aa.score IS NOT NULL) AS average_score "
            + "FROM course c "
            + "JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = cc.class_id AND u.user_type = 'student' AND u.status = 1 "
            + "LEFT JOIN course_learning_progress p ON p.course_id = c.id AND p.student_id = u.id "
            + "WHERE c.id = #{courseId}")
    AdminCourseStatistics calculateStatistics(@Param("courseId") Long courseId);

    @Select("<script>"
            + "SELECT u.id AS student_id, u.real_name AS student_name, u.username AS student_no, "
            + "u.class_id, ec.class_name, "
            + "ROUND(CASE WHEN c.courseware_count > 0 "
            + "THEN COALESCE(p.completed_courseware, 0) * 100 / c.courseware_count ELSE 0 END, 1) AS progress_percent, "
            + "ROUND(CASE WHEN c.courseware_count > 0 "
            + "THEN COALESCE(p.completed_courseware, 0) * c.courseware_score_cap / c.courseware_count ELSE 0 END, 1) AS progress_score, "
            + "COALESCE(a.assignment_count, 0) AS assignment_count, COALESCE(a.assignment_score, 0) AS assignment_score "
            + "FROM course c "
            + "JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = cc.class_id AND u.user_type = 'student' AND u.status = 1 "
            + "LEFT JOIN edu_class ec ON ec.id = u.class_id "
            + "LEFT JOIN (SELECT cp.student_id, COUNT(DISTINCT cp.content_id) AS completed_courseware "
            + "FROM course_content_learning_progress cp "
            + "JOIN course_content progress_content ON progress_content.id = cp.content_id "
            + "AND progress_content.course_id = cp.course_id AND progress_content.item_type = 'COURSEWARE' "
            + "AND progress_content.deleted_flag = 0 "
            + "WHERE cp.course_id = #{courseId} AND cp.completed = 1 GROUP BY cp.student_id) p ON p.student_id = u.id "
            + "LEFT JOIN ("
            + "SELECT attempts.student_id, COUNT(*) AS assignment_count, SUM(COALESCE(attempts.assignment_score, 0)) AS assignment_score "
            + "FROM (SELECT aa.student_id, aa.assignment_id, MAX(aa.score) AS assignment_score "
            + "FROM assignment_attempt aa JOIN course_assignment ca ON ca.id = aa.assignment_id "
            + "JOIN course_content active_content ON active_content.id = ca.content_id AND active_content.deleted_flag = 0 "
            + "WHERE ca.course_id = #{courseId} AND aa.status IN ('SUBMITTED', 'REVIEWED') "
            + "GROUP BY aa.student_id, aa.assignment_id) attempts GROUP BY attempts.student_id"
            + ") a ON a.student_id = u.id "
            + "WHERE c.id = #{courseId} "
            + "<if test='query.studentName != null'>AND u.real_name LIKE #{query.studentName}</if> "
            + "<if test='query.studentNo != null'>AND u.username LIKE #{query.studentNo}</if> "
            + "<if test='query.className != null'>AND ec.class_name LIKE #{query.className}</if> "
            + "ORDER BY ec.class_name ASC, u.username ASC LIMIT #{query.pageSize} OFFSET #{query.offset} "
            + "</script>")
    List<AdminCourseStudentStatistics> findStudentStatistics(@Param("courseId") Long courseId,
                                                             @Param("query") AdminCourseStudentStatisticsQuery query);

    @Select("<script>"
            + "SELECT COUNT(DISTINCT u.id) "
            + "FROM course c "
            + "JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = cc.class_id AND u.user_type = 'student' AND u.status = 1 "
            + "LEFT JOIN edu_class ec ON ec.id = u.class_id "
            + "WHERE c.id = #{courseId} "
            + "<if test='query.studentName != null'>AND u.real_name LIKE #{query.studentName}</if> "
            + "<if test='query.studentNo != null'>AND u.username LIKE #{query.studentNo}</if> "
            + "<if test='query.className != null'>AND ec.class_name LIKE #{query.className}</if> "
            + "</script>")
    long countStudentStatistics(@Param("courseId") Long courseId,
                                @Param("query") AdminCourseStudentStatisticsQuery query);

    @Select("SELECT ch.id AS chapter_id, ch.parent_chapter_id, ch.chapter_title, "
            + "ch.sort_order AS chapter_sort_order, ct.id AS content_id, ct.item_type, "
            + "ct.title AS content_title, ct.sort_order AS content_sort_order, "
            + "CASE WHEN ct.id IS NULL THEN NULL "
            + "WHEN ct.item_type = 'COURSEWARE' THEN CASE "
            + "WHEN COALESCE(cp.completed, 0) = 1 THEN 'COMPLETED' "
            + "WHEN COALESCE(cp.studied_seconds, 0) > 0 THEN 'IN_PROGRESS' ELSE 'NOT_COMPLETED' END "
            + "WHEN COALESCE(attempts.has_submitted, 0) = 1 AND (ca.completion_rule = 'SUBMIT' "
            + "OR COALESCE(attempts.best_score, 0) >= COALESCE(ca.pass_score, 0)) THEN 'COMPLETED' "
            + "WHEN COALESCE(attempts.has_attempt, 0) = 1 THEN 'IN_PROGRESS' ELSE 'NOT_COMPLETED' END AS completion_status, "
            + "CASE WHEN ct.item_type = 'ASSIGNMENT' THEN attempts.best_score ELSE NULL END AS score "
            + "FROM course_chapter ch JOIN course c ON c.id = ch.course_id AND c.deleted_flag = 0 "
            + "JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = cc.class_id AND u.id = #{studentId} "
            + "AND u.user_type = 'student' AND u.status = 1 "
            + "LEFT JOIN course_content ct ON ct.chapter_id = ch.id AND ct.course_id = c.id AND ct.deleted_flag = 0 "
            + "LEFT JOIN course_assignment ca ON ca.id = ct.assignment_id "
            + "LEFT JOIN course_content_learning_progress cp ON cp.course_id = c.id "
            + "AND cp.content_id = ct.id AND cp.student_id = u.id "
            + "LEFT JOIN (SELECT aa.assignment_id, 1 AS has_attempt, "
            + "MAX(CASE WHEN aa.status IN ('SUBMITTED', 'REVIEWED') THEN 1 ELSE 0 END) AS has_submitted, "
            + "MAX(CASE WHEN aa.status IN ('SUBMITTED', 'REVIEWED') THEN aa.score ELSE NULL END) AS best_score "
            + "FROM assignment_attempt aa WHERE aa.student_id = #{studentId} GROUP BY aa.assignment_id) attempts "
            + "ON attempts.assignment_id = ca.id WHERE c.id = #{courseId} AND ch.deleted_flag = 0 "
            + "ORDER BY ch.sort_order ASC, ch.id ASC, ct.sort_order ASC, ct.id ASC")
    List<AdminCourseStudentContentStatistics> findStudentContentStatistics(
            @Param("courseId") Long courseId,
            @Param("studentId") Long studentId);

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
