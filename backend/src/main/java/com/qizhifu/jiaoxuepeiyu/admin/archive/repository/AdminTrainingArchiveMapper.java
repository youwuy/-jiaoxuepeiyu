package com.qizhifu.jiaoxuepeiyu.admin.archive.repository;

import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveQuery;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStep;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminTrainingArchiveMapper {

    @Select("<script>"
            + "SELECT ta.id AS archive_id, ta.training_id, ta.training_name, ta.training_mode, "
            + "ta.student_id, u.real_name AS student_name, u.username AS student_no, u.class_id, c.class_name, "
            + "ta.role_name, ta.submitted_at, ta.submit_type, ta.duration_seconds, ta.personal_score, ta.team_score "
            + "FROM training_attempt ta "
            + "JOIN sys_user u ON u.id = ta.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE 1 = 1 "
            + "<if test='trainingId != null'>AND ta.training_id = #{trainingId}</if> "
            + "<if test='studentId != null'>AND ta.student_id = #{studentId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='trainingMode != null'>AND ta.training_mode = #{trainingMode}</if> "
            + "<if test='submitType != null'>AND ta.submit_type = #{submitType}</if> "
            + "<if test='submittedStartTime != null'>AND ta.submitted_at &gt;= #{submittedStartTime}</if> "
            + "<if test='submittedEndExclusiveTime != null'>AND ta.submitted_at &lt; #{submittedEndExclusiveTime}</if> "
            + "<if test='keyword != null'>AND (ta.training_name LIKE #{keyword} OR u.real_name LIKE #{keyword} OR u.username LIKE #{keyword})</if> "
            + "ORDER BY ta.submitted_at DESC, ta.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    List<AdminTrainingArchive> findArchives(AdminTrainingArchiveQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM training_attempt ta "
            + "JOIN sys_user u ON u.id = ta.student_id "
            + "WHERE 1 = 1 "
            + "<if test='trainingId != null'>AND ta.training_id = #{trainingId}</if> "
            + "<if test='studentId != null'>AND ta.student_id = #{studentId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='trainingMode != null'>AND ta.training_mode = #{trainingMode}</if> "
            + "<if test='submitType != null'>AND ta.submit_type = #{submitType}</if> "
            + "<if test='submittedStartTime != null'>AND ta.submitted_at &gt;= #{submittedStartTime}</if> "
            + "<if test='submittedEndExclusiveTime != null'>AND ta.submitted_at &lt; #{submittedEndExclusiveTime}</if> "
            + "<if test='keyword != null'>AND (ta.training_name LIKE #{keyword} OR u.real_name LIKE #{keyword} OR u.username LIKE #{keyword})</if> "
            + "</script>")
    long countArchives(AdminTrainingArchiveQuery query);

    @Select("SELECT ta.id AS archive_id, ta.training_id, ta.training_name, ta.training_mode, "
            + "ta.student_id, u.real_name AS student_name, u.username AS student_no, c.class_name, "
            + "ta.role_name, ta.submitted_at, ta.submit_type, ta.duration_seconds, "
            + "ta.personal_score, ta.team_score, ta.recording_url "
            + "FROM training_attempt ta "
            + "JOIN sys_user u ON u.id = ta.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE ta.id = #{archiveId} LIMIT 1")
    AdminTrainingArchiveDetail findArchiveDetail(@Param("archiveId") Long archiveId);

    @Select("SELECT id AS step_id, step_name, standard_operation, actual_operation, "
            + "score, max_score, duration_seconds, video_start_second "
            + "FROM training_attempt_step "
            + "WHERE attempt_id = #{archiveId} ORDER BY sort_order ASC, id ASC")
    List<AdminTrainingArchiveStep> findArchiveSteps(@Param("archiveId") Long archiveId);

    @Select("<script>"
            + "SELECT COUNT(*) AS archive_count, "
            + "SUM(CASE WHEN ta.submit_type = 'NORMAL' THEN 1 ELSE 0 END) AS normal_submit_count, "
            + "SUM(CASE WHEN ta.submit_type = 'ABNORMAL_EXIT' THEN 1 ELSE 0 END) AS abnormal_submit_count, "
            + "SUM(CASE WHEN ta.submit_type = 'ROOM_DISSOLVED' THEN 1 ELSE 0 END) AS room_dissolved_count, "
            + "AVG(ta.personal_score) AS average_personal_score, AVG(ta.duration_seconds) AS average_duration_seconds "
            + "FROM training_attempt ta "
            + "JOIN sys_user u ON u.id = ta.student_id "
            + "WHERE 1 = 1 "
            + "<if test='trainingId != null'>AND ta.training_id = #{trainingId}</if> "
            + "<if test='studentId != null'>AND ta.student_id = #{studentId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='trainingMode != null'>AND ta.training_mode = #{trainingMode}</if> "
            + "<if test='submitType != null'>AND ta.submit_type = #{submitType}</if> "
            + "<if test='submittedStartTime != null'>AND ta.submitted_at &gt;= #{submittedStartTime}</if> "
            + "<if test='submittedEndExclusiveTime != null'>AND ta.submitted_at &lt; #{submittedEndExclusiveTime}</if> "
            + "<if test='keyword != null'>AND (ta.training_name LIKE #{keyword} OR u.real_name LIKE #{keyword} OR u.username LIKE #{keyword})</if> "
            + "</script>")
    AdminTrainingArchiveStatistics calculateStatistics(AdminTrainingArchiveQuery query);
}
