package com.qizhifu.jiaoxuepeiyu.ue.repository;

import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptStepCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptSubmission;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingMonitorSnapshotCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.UeScoreWeight;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UeTrainingCallbackMapper {

    @Select("SELECT t.id AS training_id, t.training_name, t.training_type, t.training_mode, "
            + "tt.id AS topic_id, tt.topic_name, t.paper_id, t.open_start_time, t.open_end_time, "
            + "u.id AS student_id, u.real_name AS student_name, "
            + "r.id AS room_id, r.room_code, r.room_status, rm.role_id, rr.role_name, t.team_size "
            + "FROM training_course t "
            + "JOIN training_participant tp ON tp.training_id = t.id "
            + "JOIN sys_user u ON u.id = tp.student_id "
            + "JOIN training_topic_binding tb ON tb.training_id = t.id AND tb.topic_id = #{topicId} "
            + "JOIN training_topic tt ON tt.id = tb.topic_id "
            + "LEFT JOIN training_team_room_member rm ON rm.student_id = tp.student_id AND rm.member_status = 'ACTIVE' "
            + "LEFT JOIN training_team_room r ON r.id = rm.room_id AND r.training_id = t.id AND r.topic_id = tt.id "
            + "LEFT JOIN training_team_room_role rr ON rr.room_id = r.id AND rr.role_id = rm.role_id "
            + "WHERE t.id = #{trainingId} AND tp.student_id = #{studentId} "
            + "AND t.publish_status = 'PUBLISHED' AND t.deleted_flag = 0 LIMIT 1")
    TrainingLaunchTask findTask(@Param("trainingId") Long trainingId,
                                @Param("studentId") Long studentId,
                                @Param("topicId") Long topicId);

    @Select("SELECT id FROM training_attempt WHERE student_id = #{studentId} "
            + "AND training_id = #{trainingId} AND client_attempt_id = #{clientAttemptId} LIMIT 1")
    Long findAttemptId(@Param("studentId") Long studentId,
                       @Param("trainingId") Long trainingId,
                       @Param("clientAttemptId") String clientAttemptId);

    @Select("SELECT COUNT(*) FROM training_topic_binding WHERE training_id = #{trainingId} AND topic_id = #{topicId}")
    int countTrainingTopic(@Param("trainingId") Long trainingId, @Param("topicId") Long topicId);

    @Select("SELECT rr.role_name FROM training_team_room_role rr "
            + "LEFT JOIN training_team_room_member m "
            + "ON m.room_id = rr.room_id AND m.role_id = rr.role_id AND m.member_status = 'ACTIVE' "
            + "WHERE rr.room_id = #{roomId} AND (rr.ai_fill_enabled = 1 OR m.student_id IS NULL) "
            + "ORDER BY rr.sort_order ASC, rr.id ASC")
    List<String> findUnclaimedRoleNames(@Param("roomId") Long roomId);

    @Insert("INSERT INTO training_monitor_snapshot "
            + "(training_id, student_id, classroom_id, desk_status, progress_status, current_topic_name, submitted_topic_count, desktop_stream_url, score, team_score, last_event_at, updated_at) "
            + "VALUES (#{trainingId}, #{studentId}, #{classroomId}, #{deskStatus}, #{progressStatus}, #{currentTopicName}, #{submittedTopicCount}, #{desktopStreamUrl}, "
            + "#{score}, #{teamScore}, #{lastEventAt}, NOW()) "
            + "ON DUPLICATE KEY UPDATE classroom_id = COALESCE(VALUES(classroom_id), classroom_id), "
            + "desk_status = VALUES(desk_status), progress_status = VALUES(progress_status), current_topic_name = VALUES(current_topic_name), "
            + "submitted_topic_count = COALESCE(VALUES(submitted_topic_count), submitted_topic_count), desktop_stream_url = VALUES(desktop_stream_url), "
            + "score = VALUES(score), team_score = VALUES(team_score), "
            + "last_event_at = VALUES(last_event_at), updated_at = NOW()")
    void upsertMonitorSnapshot(TrainingMonitorSnapshotCommand command);

    @Insert("INSERT INTO training_attempt "
            + "(student_id, training_id, topic_id, client_attempt_id, training_name, training_mode, role_name, submitted_at, submit_type, "
            + "duration_seconds, personal_score, team_score, recording_url, created_at, updated_at) "
            + "VALUES (#{studentId}, #{trainingId}, #{topicId}, #{clientAttemptId}, #{trainingName}, #{trainingMode}, #{roleName}, #{submittedAt}, "
            + "#{submitType}, #{durationSeconds}, #{personalScore}, #{teamScore}, #{recordingUrl}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "attemptId")
    void insertAttempt(TrainingAttemptSubmission submission);

    @Insert("INSERT INTO training_attempt_step "
            + "(attempt_id, step_name, standard_operation, actual_operation, score, max_score, duration_seconds, "
            + "video_start_second, sort_order, created_at) "
            + "VALUES (#{attemptId}, #{step.stepName}, #{step.standardOperation}, #{step.actualOperation}, "
            + "#{step.score}, #{step.maxScore}, #{durationSeconds}, #{videoStartSecond}, #{sortOrder}, NOW())")
    void insertAttemptStep(@Param("attemptId") Long attemptId,
                           @Param("step") TrainingAttemptStepCommand step,
                           @Param("durationSeconds") int durationSeconds,
                           @Param("videoStartSecond") int videoStartSecond,
                           @Param("sortOrder") int sortOrder);

    @Select("SELECT id FROM edu_semester ORDER BY current_flag DESC, id DESC LIMIT 1")
    Long findCurrentSemesterId();

    @Select("SELECT courseware_weight, training_practice_weight, assignment_weight, exam_weight "
            + "FROM edu_score_weight WHERE semester_id = #{semesterId} "
            + "ORDER BY effective_from DESC, id DESC LIMIT 1")
    UeScoreWeight findLatestScoreWeight(@Param("semesterId") Long semesterId);

    @Insert("INSERT INTO score_semester_summary "
            + "(student_id, semester_id, courseware_learning_score, training_practice_score, "
            + "course_assignment_score, exam_score, courseware_weight, training_practice_weight, "
            + "assignment_weight, exam_weight, comprehensive_score, published_at, created_at, updated_at) "
            + "VALUES (#{studentId}, #{semesterId}, 0, #{trainingPracticeScore}, 0, 0, "
            + "#{weight.coursewareWeight}, #{weight.trainingPracticeWeight}, #{weight.assignmentWeight}, #{weight.examWeight}, "
            + "ROUND(#{trainingPracticeScore} * #{weight.trainingPracticeWeight} / 100, 1), NOW(), NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE "
            + "training_practice_score = VALUES(training_practice_score), "
            + "courseware_weight = VALUES(courseware_weight), "
            + "training_practice_weight = VALUES(training_practice_weight), "
            + "assignment_weight = VALUES(assignment_weight), "
            + "exam_weight = VALUES(exam_weight), "
            + "comprehensive_score = ROUND("
            + "COALESCE(courseware_learning_score, 0) * VALUES(courseware_weight) / 100 + "
            + "VALUES(training_practice_score) * VALUES(training_practice_weight) / 100 + "
            + "COALESCE(course_assignment_score, 0) * VALUES(assignment_weight) / 100 + "
            + "COALESCE(exam_score, 0) * VALUES(exam_weight) / 100, 1), "
            + "published_at = NOW(), updated_at = NOW()")
    void upsertTrainingPracticeScore(@Param("studentId") Long studentId,
                                     @Param("semesterId") Long semesterId,
                                     @Param("trainingPracticeScore") BigDecimal trainingPracticeScore,
                                     @Param("weight") UeScoreWeight weight);
}
