package com.qizhifu.jiaoxuepeiyu.student.assignment.repository;

import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentAnswerCommand;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentQuestionRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentAssignmentMapper {

    @Select("SELECT a.id AS assignment_id, a.course_id, a.assignment_title, a.assignment_type, "
            + "a.deadline, a.answer_start_time, a.answer_end_time, a.completion_rule, "
            + "a.pass_score, a.publish_mode, a.total_score, COALESCE(t.status, 'NOT_STARTED') AS status, "
            + "t.score, t.review_comment, t.submitted_at "
            + "FROM course_assignment a "
            + "JOIN course_content content ON content.id = a.content_id AND content.deleted_flag = 0 "
            + "JOIN course c ON c.id = a.course_id "
            + "LEFT JOIN course_class cc ON cc.course_id = c.id "
            + "JOIN sys_user u ON u.class_id = COALESCE(cc.class_id, c.class_id) "
            + "LEFT JOIN assignment_attempt t ON t.assignment_id = a.id AND t.student_id = u.id "
            + "WHERE u.id = #{studentId} AND a.id = #{assignmentId} "
            + "AND c.publish_status = 'PUBLISHED' AND a.publish_status = 'PUBLISHED' LIMIT 1")
    StudentAssignmentRecord findVisibleAssignment(@Param("studentId") Long studentId,
                                                  @Param("assignmentId") Long assignmentId);

    @Select("SELECT q.id AS question_id, q.question_type, q.title, q.options_json AS options, "
            + "q.standard_answer, q.score, ans.answer_content, COALESCE(ans.score, 0) AS awarded_score "
            + "FROM assignment_question q "
            + "LEFT JOIN assignment_attempt t ON t.assignment_id = q.assignment_id "
            + "AND t.student_id = #{studentId} "
            + "LEFT JOIN assignment_answer ans ON ans.attempt_id = t.id AND ans.question_id = q.id "
            + "WHERE q.assignment_id = #{assignmentId} "
            + "ORDER BY q.sort_order ASC, q.id ASC")
    List<AssignmentQuestionRecord> findQuestionsWithAnswers(@Param("studentId") Long studentId,
                                                            @Param("assignmentId") Long assignmentId);

    @Insert("INSERT INTO assignment_attempt "
            + "(assignment_id, student_id, status, created_at, updated_at) "
            + "VALUES (#{assignmentId}, #{studentId}, #{status}, NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE "
            + "status = IF(status = 'NOT_STARTED', VALUES(status), status), updated_at = NOW()")
    void ensureAttempt(@Param("studentId") Long studentId,
                       @Param("assignmentId") Long assignmentId,
                       @Param("status") String status);

    @Select("SELECT id FROM assignment_attempt "
            + "WHERE student_id = #{studentId} AND assignment_id = #{assignmentId} LIMIT 1")
    Long findAttemptId(@Param("studentId") Long studentId, @Param("assignmentId") Long assignmentId);

    @Delete("DELETE FROM assignment_answer WHERE attempt_id = #{attemptId}")
    void deleteAnswers(@Param("attemptId") Long attemptId);

    @Update("UPDATE assignment_attempt SET status = 'SAVED', score = NULL, review_comment = NULL, "
            + "reviewer_id = NULL, reviewed_at = NULL, submitted_at = NULL, updated_at = NOW() "
            + "WHERE id = #{attemptId}")
    void resetAttempt(@Param("attemptId") Long attemptId);

    @Insert("<script>"
            + "INSERT INTO assignment_answer "
            + "(attempt_id, question_id, answer_content, score, created_at, updated_at) VALUES "
            + "<foreach collection='answers' item='answer' separator=','>"
            + "(#{attemptId}, #{answer.questionId}, #{answer.answerContent}, NULL, NOW(), NOW())"
            + "</foreach>"
            + "</script>")
    void insertAnswers(@Param("attemptId") Long attemptId,
                       @Param("answers") List<AssignmentAnswerCommand.AnswerItem> answers);

    @Insert("INSERT INTO assignment_answer "
            + "(attempt_id, question_id, answer_content, score, created_at, updated_at) "
            + "VALUES (#{attemptId}, #{questionId}, #{answerContent}, #{score}, NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE "
            + "answer_content = VALUES(answer_content), score = VALUES(score), updated_at = NOW()")
    void upsertScoredAnswer(@Param("attemptId") Long attemptId,
                            @Param("questionId") Long questionId,
                            @Param("answerContent") String answerContent,
                            @Param("score") int score);

    @Update("UPDATE assignment_attempt SET status = 'SUBMITTED', score = #{score}, "
            + "submitted_at = #{submittedAt}, updated_at = NOW() WHERE id = #{attemptId}")
    void markSubmitted(@Param("attemptId") Long attemptId,
                       @Param("score") int score,
                       @Param("submittedAt") LocalDateTime submittedAt);

    @Insert("INSERT INTO course_learning_progress (course_id, student_id, completed_items, updated_at) "
            + "SELECT ca.course_id, #{studentId}, COUNT(*), NOW() "
            + "FROM course_assignment ca "
            + "JOIN course_content ct ON ct.course_id = ca.course_id "
            + "LEFT JOIN course_content_learning_progress cp "
            + "ON cp.content_id = ct.id AND cp.student_id = #{studentId} "
            + "LEFT JOIN course_assignment a ON a.content_id = ct.id "
            + "LEFT JOIN assignment_attempt aa "
            + "ON aa.assignment_id = a.id AND aa.student_id = #{studentId} "
            + "AND aa.status IN ('SUBMITTED', 'REVIEWED') "
            + "WHERE ca.id = #{assignmentId} "
            + "AND ct.deleted_flag = 0 "
            + "AND ((ct.item_type = 'COURSEWARE' AND cp.completed = 1) "
            + "OR (ct.item_type = 'ASSIGNMENT' AND aa.id IS NOT NULL "
            + "AND (a.completion_rule = 'SUBMIT' "
            + "OR (a.completion_rule = 'PASS_SCORE' AND aa.score IS NOT NULL AND aa.score >= COALESCE(a.pass_score, 0))))) "
            + "GROUP BY ca.course_id "
            + "ON DUPLICATE KEY UPDATE completed_items = VALUES(completed_items), updated_at = NOW()")
    void refreshCourseProgressByAssignment(@Param("studentId") Long studentId,
                                           @Param("assignmentId") Long assignmentId);
}
