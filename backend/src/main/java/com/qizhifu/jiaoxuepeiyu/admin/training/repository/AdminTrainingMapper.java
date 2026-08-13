package com.qizhifu.jiaoxuepeiyu.admin.training.repository;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCameraState;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingLog;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingQuery;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingRole;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStudentState;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminTrainingMapper {

    @Select("<script>"
            + "SELECT DISTINCT t.id AS training_id, t.training_name, t.academic_year_id, "
            + "ay.year_name AS academic_year_name, t.semester_id, s.semester_name, "
            + "t.major_id, m.major_name, t.cover_url, t.training_type, t.training_mode, "
            + "t.paper_mode, t.paper_id, p.paper_name, t.publish_status, "
            + "t.open_start_time, t.open_end_time, t.exam_started_at, t.team_size, "
            + "CASE WHEN t.app_required = 1 THEN TRUE ELSE FALSE END AS app_required, "
            + "t.class_names, t.classroom_id, t.score_basis, "
            + "(SELECT GROUP_CONCAT(u2.real_name ORDER BY tt.sort_order SEPARATOR ', ') FROM training_teacher tt JOIN sys_user u2 ON u2.id = tt.teacher_id WHERE tt.training_id = t.id) AS teacher_names, "
            + "(SELECT room_name FROM training_room WHERE id = t.classroom_id) AS classroom_name, "
            + "(SELECT COUNT(*) FROM training_topic_binding tb WHERE tb.training_id = t.id) AS topic_count, "
            + "(SELECT COUNT(*) FROM training_participant tp WHERE tp.training_id = t.id) AS participant_count, "
            + "(SELECT COUNT(*) FROM training_team_room tr WHERE tr.training_id = t.id) AS room_count, "
            + "(SELECT COUNT(*) FROM training_team_room tr WHERE tr.training_id = t.id AND tr.room_status = 'STARTED') AS started_room_count, "
            + "(SELECT AVG(ms.score) FROM training_monitor_snapshot ms WHERE ms.training_id = t.id AND ms.score IS NOT NULL) AS average_score, "
            + "t.created_by, u.real_name AS creator_name, t.created_at, t.updated_at "
            + "FROM training_course t "
            + "LEFT JOIN edu_academic_year ay ON ay.id = t.academic_year_id "
            + "LEFT JOIN edu_semester s ON s.id = t.semester_id "
            + "LEFT JOIN edu_major m ON m.id = t.major_id "
            + "LEFT JOIN exam_paper p ON p.id = t.paper_id "
            + "LEFT JOIN sys_user u ON u.id = t.created_by "
            + "<if test='classId != null'>JOIN training_class tc_filter ON tc_filter.training_id = t.id AND tc_filter.class_id = #{classId}</if> "
            + "WHERE t.deleted_flag = 0 "
            + "<if test='keyword != null'>AND t.training_name LIKE #{keyword}</if> "
            + "<if test='academicYearId != null'>AND t.academic_year_id = #{academicYearId}</if> "
            + "<if test='semesterId != null'>AND t.semester_id = #{semesterId}</if> "
            + "<if test='majorId != null'>AND t.major_id = #{majorId}</if> "
            + "<if test='trainingType != null'>AND t.training_type = #{trainingType}</if> "
            + "<if test='trainingMode != null'>AND t.training_mode = #{trainingMode}</if> "
            + "<if test='publishStatus != null'>AND t.publish_status = #{publishStatus}</if> "
            + "<if test='rangeStart != null'>AND t.open_end_time &gt;= #{rangeStart}</if> "
            + "<if test='rangeEnd != null'>AND t.open_start_time &lt;= #{rangeEnd}</if> "
            + "ORDER BY t.updated_at DESC, t.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    @Results(id = "trainingMap", value = {
            @Result(column = "training_id", property = "trainingId", id = true),
            @Result(column = "training_name", property = "trainingName"),
            @Result(column = "academic_year_id", property = "academicYearId"),
            @Result(column = "academic_year_name", property = "academicYearName"),
            @Result(column = "semester_id", property = "semesterId"),
            @Result(column = "semester_name", property = "semesterName"),
            @Result(column = "major_id", property = "majorId"),
            @Result(column = "major_name", property = "majorName"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "training_type", property = "trainingType"),
            @Result(column = "training_mode", property = "trainingMode"),
            @Result(column = "paper_mode", property = "paperMode"),
            @Result(column = "paper_id", property = "paperId"),
            @Result(column = "paper_name", property = "paperName"),
            @Result(column = "publish_status", property = "publishStatus"),
            @Result(column = "open_start_time", property = "openStartTime"),
            @Result(column = "open_end_time", property = "openEndTime"),
            @Result(column = "exam_started_at", property = "examStartedAt"),
            @Result(column = "team_size", property = "teamSize"),
            @Result(column = "app_required", property = "appRequired"),
            @Result(column = "class_names", property = "classNames"),
            @Result(column = "classroom_id", property = "classroomId"),
            @Result(column = "score_basis", property = "scoreBasis"),
            @Result(column = "teacher_names", property = "teacherNames"),
            @Result(column = "classroom_name", property = "classroomName"),
            @Result(column = "topic_count", property = "topicCount"),
            @Result(column = "participant_count", property = "participantCount"),
            @Result(column = "room_count", property = "roomCount"),
            @Result(column = "started_room_count", property = "startedRoomCount"),
            @Result(column = "average_score", property = "averageScore"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "training_id", property = "classIds", many = @Many(select = "findClassIds")),
            @Result(column = "training_id", property = "roles", many = @Many(select = "findRoles")),
            @Result(column = "training_id", property = "teacherIds", many = @Many(select = "findTeacherIds")),
            @Result(column = "training_id", property = "topicIds", many = @Many(select = "findTopicIds"))
    })
    List<AdminTraining> findTrainings(AdminTrainingQuery query);

    @Select("<script>"
            + "SELECT COUNT(DISTINCT t.id) FROM training_course t "
            + "<if test='classId != null'>JOIN training_class tc_filter ON tc_filter.training_id = t.id AND tc_filter.class_id = #{classId}</if> "
            + "WHERE t.deleted_flag = 0 "
            + "<if test='keyword != null'>AND t.training_name LIKE #{keyword}</if> "
            + "<if test='academicYearId != null'>AND t.academic_year_id = #{academicYearId}</if> "
            + "<if test='semesterId != null'>AND t.semester_id = #{semesterId}</if> "
            + "<if test='majorId != null'>AND t.major_id = #{majorId}</if> "
            + "<if test='trainingType != null'>AND t.training_type = #{trainingType}</if> "
            + "<if test='trainingMode != null'>AND t.training_mode = #{trainingMode}</if> "
            + "<if test='publishStatus != null'>AND t.publish_status = #{publishStatus}</if> "
            + "<if test='rangeStart != null'>AND t.open_end_time &gt;= #{rangeStart}</if> "
            + "<if test='rangeEnd != null'>AND t.open_start_time &lt;= #{rangeEnd}</if> "
            + "</script>")
    long countTrainings(AdminTrainingQuery query);

    @Select("SELECT t.id AS training_id, t.training_name, t.academic_year_id, ay.year_name AS academic_year_name, "
            + "t.semester_id, s.semester_name, t.major_id, m.major_name, t.cover_url, "
            + "t.training_type, t.training_mode, t.paper_mode, t.paper_id, p.paper_name, "
            + "t.publish_status, t.open_start_time, t.open_end_time, t.exam_started_at, t.team_size, "
            + "CASE WHEN t.app_required = 1 THEN TRUE ELSE FALSE END AS app_required, "
            + "t.class_names, t.classroom_id, t.score_basis, t.created_by, u.real_name AS creator_name, t.created_at, t.updated_at "
            + "FROM training_course t "
            + "LEFT JOIN edu_academic_year ay ON ay.id = t.academic_year_id "
            + "LEFT JOIN edu_semester s ON s.id = t.semester_id "
            + "LEFT JOIN edu_major m ON m.id = t.major_id "
            + "LEFT JOIN exam_paper p ON p.id = t.paper_id "
            + "LEFT JOIN sys_user u ON u.id = t.created_by "
            + "WHERE t.id = #{trainingId} AND t.deleted_flag = 0 LIMIT 1")
    @Results(id = "trainingDetailMap", value = {
            @Result(column = "training_id", property = "trainingId", id = true),
            @Result(column = "training_name", property = "trainingName"),
            @Result(column = "academic_year_id", property = "academicYearId"),
            @Result(column = "academic_year_name", property = "academicYearName"),
            @Result(column = "semester_id", property = "semesterId"),
            @Result(column = "semester_name", property = "semesterName"),
            @Result(column = "major_id", property = "majorId"),
            @Result(column = "major_name", property = "majorName"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "training_type", property = "trainingType"),
            @Result(column = "training_mode", property = "trainingMode"),
            @Result(column = "paper_mode", property = "paperMode"),
            @Result(column = "paper_id", property = "paperId"),
            @Result(column = "paper_name", property = "paperName"),
            @Result(column = "publish_status", property = "publishStatus"),
            @Result(column = "open_start_time", property = "openStartTime"),
            @Result(column = "open_end_time", property = "openEndTime"),
            @Result(column = "exam_started_at", property = "examStartedAt"),
            @Result(column = "team_size", property = "teamSize"),
            @Result(column = "app_required", property = "appRequired"),
            @Result(column = "class_names", property = "classNames"),
            @Result(column = "classroom_id", property = "classroomId"),
            @Result(column = "score_basis", property = "scoreBasis"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "training_id", property = "classIds", many = @Many(select = "findClassIds")),
            @Result(column = "training_id", property = "roles", many = @Many(select = "findRoles")),
            @Result(column = "training_id", property = "teacherIds", many = @Many(select = "findTeacherIds")),
            @Result(column = "training_id", property = "topicIds", many = @Many(select = "findTopicIds"))
    })
    AdminTraining findTraining(@Param("trainingId") Long trainingId);

    @Select("SELECT class_id FROM training_class WHERE training_id = #{trainingId} ORDER BY sort_order ASC, id ASC")
    List<Long> findClassIds(@Param("trainingId") Long trainingId);

    @Select("SELECT teacher_id FROM training_teacher WHERE training_id = #{trainingId} ORDER BY sort_order ASC, id ASC")
    List<Long> findTeacherIds(@Param("trainingId") Long trainingId);

    @Select("SELECT topic_id FROM training_topic_binding WHERE training_id = #{trainingId} ORDER BY sort_order ASC, id ASC")
    List<Long> findTopicIds(@Param("trainingId") Long trainingId);

    @Select("SELECT id AS role_id, training_id, role_name, sort_order "
            + "FROM training_role WHERE training_id = #{trainingId} ORDER BY sort_order ASC, id ASC")
    List<AdminTrainingRole> findRoles(@Param("trainingId") Long trainingId);

    @Insert("INSERT INTO training_course "
            + "(training_name, academic_year_id, semester_id, major_id, cover_url, training_type, training_mode, "
            + "paper_mode, paper_id, publish_status, open_start_time, open_end_time, team_size, app_required, classroom_id, score_basis, "
            + "class_names, created_by, deleted_flag, created_at, updated_at) "
            + "VALUES (#{trainingName}, #{academicYearId}, #{semesterId}, #{majorId}, #{coverUrl}, "
            + "#{trainingType}, #{trainingMode}, #{paperMode}, #{paperId}, #{publishStatus}, "
            + "#{openStartTime}, #{openEndTime}, #{teamSize}, #{appRequired}, #{classroomId}, #{scoreBasis}, #{classNames}, #{createdBy}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "trainingId")
    void insertTraining(AdminTraining training);

    @Update("UPDATE training_course SET training_name = #{trainingName}, academic_year_id = #{academicYearId}, "
            + "semester_id = #{semesterId}, major_id = #{majorId}, cover_url = #{coverUrl}, "
            + "training_type = #{trainingType}, training_mode = #{trainingMode}, paper_mode = #{paperMode}, "
            + "paper_id = #{paperId}, open_start_time = #{openStartTime}, open_end_time = #{openEndTime}, "
            + "team_size = #{teamSize}, app_required = #{appRequired}, classroom_id = #{classroomId}, score_basis = #{scoreBasis}, class_names = #{classNames}, updated_at = NOW() "
            + "WHERE id = #{trainingId} AND deleted_flag = 0")
    void updateTraining(AdminTraining training);

    @Delete("DELETE FROM training_class WHERE training_id = #{trainingId}")
    void deleteClasses(@Param("trainingId") Long trainingId);

    @Insert("INSERT INTO training_class (training_id, class_id, sort_order, created_at) "
            + "VALUES (#{trainingId}, #{classId}, #{sortOrder}, NOW())")
    void insertClass(@Param("trainingId") Long trainingId, @Param("classId") Long classId, @Param("sortOrder") int sortOrder);

    @Delete("DELETE FROM training_role WHERE training_id = #{trainingId}")
    void deleteRoles(@Param("trainingId") Long trainingId);

    @Insert("INSERT INTO training_role (training_id, role_name, sort_order, created_at) "
            + "VALUES (#{trainingId}, #{role.roleName}, #{role.sortOrder}, NOW())")
    void insertRole(@Param("trainingId") Long trainingId, @Param("role") AdminTrainingRole role);

    @Delete("DELETE FROM training_teacher WHERE training_id = #{trainingId}")
    void deleteTeachers(@Param("trainingId") Long trainingId);

    @Insert("INSERT INTO training_teacher (training_id, teacher_id, sort_order) VALUES (#{trainingId}, #{teacherId}, #{sortOrder})")
    void insertTeacher(@Param("trainingId") Long trainingId, @Param("teacherId") Long teacherId, @Param("sortOrder") int sortOrder);

    @Delete("DELETE FROM training_topic_binding WHERE training_id = #{trainingId}")
    void deleteTopics(@Param("trainingId") Long trainingId);

    @Insert("INSERT INTO training_topic_binding (training_id, topic_id, sort_order) VALUES (#{trainingId}, #{topicId}, #{sortOrder})")
    void insertTopic(@Param("trainingId") Long trainingId, @Param("topicId") Long topicId, @Param("sortOrder") int sortOrder);

    @Select("<script>"
            + "SELECT GROUP_CONCAT(class_name ORDER BY id SEPARATOR ', ') FROM edu_class WHERE id IN "
            + "<foreach collection='classIds' item='classId' open='(' separator=',' close=')'>#{classId}</foreach>"
            + "</script>")
    String findClassNamesByIds(@Param("classIds") List<Long> classIds);

    @Select("SELECT COUNT(DISTINCT u.id) FROM sys_user u "
            + "JOIN training_class tc ON tc.class_id = u.class_id "
            + "WHERE tc.training_id = #{trainingId} AND u.user_type = 'student' AND u.status = 1")
    int countEnabledStudentsByTrainingClasses(@Param("trainingId") Long trainingId);

    @Delete("DELETE FROM training_participant WHERE training_id = #{trainingId}")
    void deleteParticipants(@Param("trainingId") Long trainingId);

    @Insert("INSERT IGNORE INTO training_participant (training_id, student_id, created_at) "
            + "SELECT #{trainingId}, u.id, NOW() FROM sys_user u "
            + "JOIN training_class tc ON tc.class_id = u.class_id "
            + "WHERE tc.training_id = #{trainingId} AND u.user_type = 'student' AND u.status = 1")
    void insertParticipantsFromClasses(@Param("trainingId") Long trainingId);

    @Update("UPDATE training_course SET publish_status = #{publishStatus}, updated_at = NOW() "
            + "WHERE id = #{trainingId} AND deleted_flag = 0")
    void updatePublishStatus(@Param("trainingId") Long trainingId, @Param("publishStatus") String publishStatus);

    @Update("UPDATE training_course SET exam_started_at = NOW(), updated_at = NOW() WHERE id = #{trainingId} AND deleted_flag = 0")
    void markExamStarted(@Param("trainingId") Long trainingId);

    @Update("UPDATE training_course SET deleted_flag = 1, publish_status = 'OFFLINE', updated_at = NOW() "
            + "WHERE id = #{trainingId} AND deleted_flag = 0")
    void deleteTraining(@Param("trainingId") Long trainingId);

    @Insert("INSERT INTO msg_notification (message_type, title, content, source_id, created_at) "
            + "VALUES ('TRAINING', #{title}, #{content}, #{sourceId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "notificationId")
    void insertNotification(AdminTrainingNotification notification);

    @Insert("INSERT IGNORE INTO msg_user_notification (notification_id, user_id, read_flag, created_at) "
            + "SELECT #{notificationId}, student_id, 0, NOW() FROM training_participant "
            + "WHERE training_id = #{trainingId}")
    void notifyParticipants(@Param("trainingId") Long trainingId, @Param("notificationId") Long notificationId);

    @Select("SELECT #{trainingId} AS training_id, "
            + "(SELECT COUNT(*) FROM training_participant WHERE training_id = #{trainingId}) AS participant_count, "
            + "(SELECT COUNT(*) FROM training_team_room WHERE training_id = #{trainingId} AND room_status = 'WAITING') AS waiting_room_count, "
            + "(SELECT COUNT(*) FROM training_team_room WHERE training_id = #{trainingId} AND room_status = 'STARTED') AS started_room_count, "
            + "(SELECT COUNT(*) FROM training_team_room WHERE training_id = #{trainingId} AND room_status = 'DISSOLVED') AS dissolved_room_count, "
            + "(SELECT COUNT(DISTINCT ta.student_id) FROM training_attempt ta JOIN training_course tc ON tc.id = ta.training_id "
            + "WHERE ta.training_id = #{trainingId} AND ta.submitted_at IS NOT NULL) AS submitted_attempt_count, "
            + "(SELECT AVG(final_score) FROM (SELECT tp.student_id, "
            + "(SELECT COALESCE(ta.manual_score, ta.personal_score) FROM training_attempt ta "
            + "WHERE ta.training_id = tp.training_id AND ta.student_id = tp.student_id AND ta.submitted_at IS NOT NULL "
            + "ORDER BY CASE WHEN tc.score_basis = 'HIGHEST' THEN COALESCE(ta.manual_score, ta.personal_score) END DESC, "
            + "ta.submitted_at DESC, ta.id DESC LIMIT 1) AS final_score "
            + "FROM training_participant tp JOIN training_course tc ON tc.id = tp.training_id "
            + "WHERE tp.training_id = #{trainingId}) final_scores WHERE final_score IS NOT NULL) AS average_score, "
            + "(SELECT MAX(final_score) FROM (SELECT tp.student_id, "
            + "(SELECT COALESCE(ta.manual_score, ta.personal_score) FROM training_attempt ta WHERE ta.training_id = tp.training_id "
            + "AND ta.student_id = tp.student_id AND ta.submitted_at IS NOT NULL ORDER BY CASE WHEN tc.score_basis = 'HIGHEST' THEN COALESCE(ta.manual_score, ta.personal_score) END DESC, ta.submitted_at DESC, ta.id DESC LIMIT 1) AS final_score "
            + "FROM training_participant tp JOIN training_course tc ON tc.id = tp.training_id WHERE tp.training_id = #{trainingId}) final_scores WHERE final_score IS NOT NULL) AS max_score, "
            + "(SELECT MIN(final_score) FROM (SELECT tp.student_id, "
            + "(SELECT COALESCE(ta.manual_score, ta.personal_score) FROM training_attempt ta WHERE ta.training_id = tp.training_id "
            + "AND ta.student_id = tp.student_id AND ta.submitted_at IS NOT NULL ORDER BY CASE WHEN tc.score_basis = 'HIGHEST' THEN COALESCE(ta.manual_score, ta.personal_score) END DESC, ta.submitted_at DESC, ta.id DESC LIMIT 1) AS final_score "
            + "FROM training_participant tp JOIN training_course tc ON tc.id = tp.training_id WHERE tp.training_id = #{trainingId}) final_scores WHERE final_score IS NOT NULL) AS min_score")
    AdminTrainingStatistics calculateStatistics(@Param("trainingId") Long trainingId);

    @Select("SELECT rc.id AS camera_id, tr.id AS classroom_id, tr.room_name AS classroom_name, "
            + "rc.camera_name, CAST(rc.nvr_channel AS CHAR) AS nvr_channel, rc.rtsp_url AS stream_url, "
            + "CASE WHEN rc.status = 1 THEN 'ONLINE' ELSE 'OFFLINE' END AS camera_status "
            + "FROM training_course tc "
            + "JOIN training_room tr ON tr.id = tc.classroom_id "
            + "JOIN room_camera rc ON rc.room_id = tr.id "
            + "WHERE tc.id = #{trainingId} AND tc.deleted_flag = 0 "
            + "ORDER BY tr.id ASC, rc.sort_order ASC, rc.id ASC")
    List<AdminTrainingCameraState> findMonitorCameras(@Param("trainingId") Long trainingId);

    @Select("SELECT u.id AS student_id, u.real_name AS student_name, u.username AS student_no, tc.training_mode, "
            + "u.class_id, c.class_name, u.last_login_ip AS client_ip, COALESCE(ms.desk_status, 'OFFLINE') AS desk_status, "
            + "COALESCE(ms.progress_status, 'NOT_STARTED') AS progress_status, ms.current_topic_name, "
            + "COALESCE(ms.submitted_topic_count, 0) AS submitted_topic_count, "
            + "(SELECT COUNT(*) FROM training_topic_binding tb WHERE tb.training_id = tp.training_id) AS total_topic_count, "
            + "ms.score, ms.team_score, ms.desktop_stream_url, "
            + "r.id AS room_id, r.room_code, r.room_status, rr.role_name, "
            + "(SELECT GROUP_CONCAT(u2.real_name ORDER BY u2.real_name SEPARATOR '、') "
            + "FROM training_team_room_member rm2 JOIN sys_user u2 ON u2.id = rm2.student_id "
            + "WHERE rm2.room_id = r.id AND rm2.member_status = 'ACTIVE') AS teammate_names "
            + "FROM training_participant tp "
            + "JOIN training_course tc ON tc.id = tp.training_id "
            + "JOIN sys_user u ON u.id = tp.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "LEFT JOIN training_monitor_snapshot ms ON ms.training_id = tp.training_id AND ms.student_id = tp.student_id "
            + "LEFT JOIN training_team_room_member rm ON rm.student_id = tp.student_id AND rm.member_status = 'ACTIVE' "
            + "LEFT JOIN training_team_room r ON r.id = rm.room_id AND r.training_id = tp.training_id "
            + "LEFT JOIN training_team_room_role rr ON rr.room_id = r.id AND rr.role_id = rm.role_id "
            + "WHERE tp.training_id = #{trainingId} ORDER BY c.class_name ASC, u.username ASC, u.id ASC")
    List<AdminTrainingStudentState> findMonitorStudents(@Param("trainingId") Long trainingId);

    @Update("UPDATE training_team_room SET room_status = 'DISSOLVED', updated_at = NOW() "
            + "WHERE id = #{roomId} AND training_id = #{trainingId} AND room_status IN ('WAITING', 'STARTED')")
    int dissolveRoom(@Param("trainingId") Long trainingId, @Param("roomId") Long roomId);

    @Update("UPDATE training_team_room_member SET member_status = 'LEFT', role_id = NULL, left_at = NOW(), updated_at = NOW() "
            + "WHERE room_id = #{roomId} AND member_status = 'ACTIVE'")
    void removeActiveRoomMembers(@Param("roomId") Long roomId);

    @Update("UPDATE training_monitor_snapshot ms JOIN training_team_room_member rm ON rm.student_id = ms.student_id "
            + "SET ms.progress_status = 'NOT_STARTED', ms.current_topic_name = NULL, ms.score = NULL, ms.team_score = NULL, ms.updated_at = NOW() "
            + "WHERE ms.training_id = #{trainingId} AND rm.room_id = #{roomId}")
    void clearRoomMonitorState(@Param("trainingId") Long trainingId, @Param("roomId") Long roomId);

    @Select("SELECT tp.student_id AS studentId, u.real_name AS studentName, u.username AS studentNo, "
            + "u.class_id AS classId, c.class_name AS className, tt.id AS topicId, tt.topic_name AS topicName, "
            + "tt.training_mode AS trainingMode, ta.id AS attemptId, ta.submitted_at AS submittedAt, "
            + "ta.personal_score AS systemScore, ta.team_score AS teamScore, ta.manual_score AS manualScore, "
            + "ta.review_comment AS reviewComment, ta.reviewed_at AS reviewedAt, ta.role_name AS roleName, "
            + "(SELECT COUNT(*) FROM training_attempt tx WHERE tx.training_id = tp.training_id "
            + "AND tx.student_id = tp.student_id AND (tx.topic_id = tt.id OR (tx.topic_id IS NULL AND "
            + "(SELECT COUNT(*) FROM training_topic_binding one_tb WHERE one_tb.training_id = tp.training_id) = 1))) AS submitCount, "
            + "(SELECT GROUP_CONCAT(CONCAT(u2.real_name, ' ', COALESCE(a2.manual_score, a2.personal_score, 0)) "
            + "ORDER BY u2.real_name SEPARATOR '、') FROM training_team_room_member rm1 "
            + "JOIN training_team_room_member rm2 ON rm2.room_id = rm1.room_id AND rm2.member_status = 'ACTIVE' "
            + "JOIN sys_user u2 ON u2.id = rm2.student_id "
            + "LEFT JOIN training_attempt a2 ON a2.student_id = rm2.student_id AND a2.training_id = tp.training_id "
            + "WHERE rm1.student_id = tp.student_id AND rm1.member_status = 'ACTIVE') AS teammateScores "
            + "FROM training_participant tp JOIN sys_user u ON u.id = tp.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "JOIN training_topic_binding tb ON tb.training_id = tp.training_id "
            + "JOIN training_topic tt ON tt.id = tb.topic_id "
            + "LEFT JOIN training_attempt ta ON ta.id = (SELECT tx.id FROM training_attempt tx "
            + "WHERE tx.training_id = tp.training_id AND tx.student_id = tp.student_id "
            + "AND (tx.topic_id = tt.id OR (tx.topic_id IS NULL AND "
            + "(SELECT COUNT(*) FROM training_topic_binding one_tb WHERE one_tb.training_id = tp.training_id) = 1)) "
            + "ORDER BY tx.submitted_at DESC, tx.id DESC LIMIT 1) "
            + "WHERE tp.training_id = #{trainingId} "
            + "ORDER BY tb.sort_order ASC, c.class_name ASC, u.username ASC, u.id ASC")
    List<Map<String, Object>> findReviewRows(@Param("trainingId") Long trainingId);

    @Select("SELECT ta.id AS attemptId, ta.submitted_at AS submittedAt, ta.personal_score AS systemScore, "
            + "ta.manual_score AS manualScore, ta.team_score AS teamScore, ta.review_comment AS reviewComment, "
            + "ta.reviewed_at AS reviewedAt, ta.role_name AS roleName, ta.duration_seconds AS durationSeconds "
            + "FROM training_attempt ta WHERE ta.training_id = #{trainingId} AND ta.student_id = #{studentId} "
            + "AND (ta.topic_id = #{topicId} OR (ta.topic_id IS NULL AND "
            + "(SELECT COUNT(*) FROM training_topic_binding tb WHERE tb.training_id = #{trainingId}) = 1)) "
            + "ORDER BY ta.submitted_at DESC, ta.id DESC")
    List<Map<String, Object>> findReviewAttempts(@Param("trainingId") Long trainingId,
                                                  @Param("studentId") Long studentId,
                                                  @Param("topicId") Long topicId);

    @Update("UPDATE training_attempt SET manual_score = #{manualScore}, review_comment = #{comment}, "
            + "reviewer_id = #{reviewerId}, reviewed_at = NOW(), updated_at = NOW() "
            + "WHERE id = #{attemptId} AND training_id = #{trainingId}")
    int reviewAttempt(@Param("trainingId") Long trainingId,
                      @Param("attemptId") Long attemptId,
                      @Param("manualScore") Double manualScore,
                      @Param("comment") String comment,
                      @Param("reviewerId") Long reviewerId);

    @Insert("INSERT INTO training_course_log (training_id, operator_id, action, content, created_at) "
            + "VALUES (#{trainingId}, #{operatorId}, #{action}, #{content}, NOW())")
    void insertTrainingLog(@Param("trainingId") Long trainingId,
                           @Param("operatorId") Long operatorId,
                           @Param("action") String action,
                           @Param("content") String content);

    @Select("SELECT l.id AS log_id, l.training_id, l.operator_id, u.real_name AS operator_name, "
            + "l.action, l.content, l.created_at "
            + "FROM training_course_log l LEFT JOIN sys_user u ON u.id = l.operator_id "
            + "WHERE l.training_id = #{trainingId} ORDER BY l.created_at DESC, l.id DESC")
    List<AdminTrainingLog> findTrainingLogs(@Param("trainingId") Long trainingId);
}
