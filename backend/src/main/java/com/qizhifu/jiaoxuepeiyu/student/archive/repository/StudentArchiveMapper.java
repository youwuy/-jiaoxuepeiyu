package com.qizhifu.jiaoxuepeiyu.student.archive.repository;

import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveStep;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentArchiveMapper {

    @Select("<script>"
            + "SELECT ta.id AS archive_id, ta.training_name, ta.training_mode, ta.role_name, "
            + "ta.submitted_at, ta.submit_type, ta.duration_seconds, ta.personal_score, ta.team_score "
            + "FROM training_attempt ta "
            + "WHERE ta.student_id = #{studentId} "
            + "<if test='mode != null'>AND ta.training_mode = #{mode}</if> "
            + "<if test='keyword != null'>AND ta.training_name LIKE #{keyword}</if> "
            + "ORDER BY ta.submitted_at DESC, ta.id DESC "
            + "</script>")
    List<StudentTrainingArchive> findArchives(@Param("studentId") Long studentId,
                                              @Param("mode") String mode,
                                              @Param("keyword") String keyword);

    @Select("SELECT ta.id AS archive_id, ta.training_name, ta.training_mode, ta.role_name, "
            + "u.real_name AS student_name, u.username AS student_no, c.class_name, "
            + "ta.submitted_at, ta.submit_type, ta.duration_seconds, "
            + "ta.personal_score, ta.team_score, ta.recording_url "
            + "FROM training_attempt ta "
            + "JOIN sys_user u ON u.id = ta.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE ta.student_id = #{studentId} AND ta.id = #{archiveId} LIMIT 1")
    StudentTrainingArchiveDetail findArchiveDetail(@Param("studentId") Long studentId,
                                                   @Param("archiveId") Long archiveId);

    @Select("SELECT id AS step_id, step_name, standard_operation, actual_operation, "
            + "score, max_score, duration_seconds, video_start_second "
            + "FROM training_attempt_step "
            + "WHERE attempt_id = #{archiveId} "
            + "ORDER BY sort_order ASC, id ASC")
    List<StudentTrainingArchiveStep> findArchiveSteps(@Param("archiveId") Long archiveId);
}
