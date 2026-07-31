package com.qizhifu.jiaoxuepeiyu.admin.assignment.repository;

import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttempt;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttemptQuery;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewLog;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminAssignmentReviewMapper {

    @Select("<script>"
            + "SELECT t.id AS attempt_id, t.assignment_id, a.assignment_title, a.assignment_type, "
            + "a.course_id, c.course_name, t.student_id, u.real_name AS student_name, "
            + "u.account_no AS student_no, u.class_id, cl.class_name, a.total_score, t.status, "
            + "t.score, t.review_comment, t.reviewer_id, reviewer.real_name AS reviewer_name, "
            + "t.submitted_at, t.reviewed_at "
            + "FROM assignment_attempt t "
            + "JOIN course_assignment a ON a.id = t.assignment_id "
            + "JOIN course c ON c.id = a.course_id "
            + "JOIN sys_user u ON u.id = t.student_id "
            + "LEFT JOIN edu_class cl ON cl.id = u.class_id "
            + "LEFT JOIN sys_user reviewer ON reviewer.id = t.reviewer_id "
            + "WHERE 1 = 1 "
            + "<if test='courseId != null'>AND a.course_id = #{courseId}</if> "
            + "<if test='assignmentId != null'>AND t.assignment_id = #{assignmentId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='studentId != null'>AND t.student_id = #{studentId}</if> "
            + "<if test='status != null'>AND t.status = #{status}</if> "
            + "<if test='keyword != null'>AND (u.real_name LIKE #{keyword} OR u.account_no LIKE #{keyword} OR a.assignment_title LIKE #{keyword})</if> "
            + "ORDER BY t.updated_at DESC, t.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    @Results(id = "attemptMap", value = {
            @Result(column = "attempt_id", property = "attemptId", id = true),
            @Result(column = "assignment_id", property = "assignmentId"),
            @Result(column = "assignment_title", property = "assignmentTitle"),
            @Result(column = "assignment_type", property = "assignmentType"),
            @Result(column = "course_id", property = "courseId"),
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "student_name", property = "studentName"),
            @Result(column = "student_no", property = "studentNo"),
            @Result(column = "class_id", property = "classId"),
            @Result(column = "class_name", property = "className"),
            @Result(column = "total_score", property = "totalScore"),
            @Result(column = "status", property = "status"),
            @Result(column = "score", property = "score"),
            @Result(column = "review_comment", property = "reviewComment"),
            @Result(column = "reviewer_id", property = "reviewerId"),
            @Result(column = "reviewer_name", property = "reviewerName"),
            @Result(column = "submitted_at", property = "submittedAt"),
            @Result(column = "reviewed_at", property = "reviewedAt"),
            @Result(column = "attempt_id", property = "answers", many = @Many(select = "findAttemptAnswers"))
    })
    List<AdminAssignmentAttempt> findAttempts(AdminAssignmentAttemptQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM assignment_attempt t "
            + "JOIN course_assignment a ON a.id = t.assignment_id "
            + "JOIN sys_user u ON u.id = t.student_id "
            + "WHERE 1 = 1 "
            + "<if test='courseId != null'>AND a.course_id = #{courseId}</if> "
            + "<if test='assignmentId != null'>AND t.assignment_id = #{assignmentId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='studentId != null'>AND t.student_id = #{studentId}</if> "
            + "<if test='status != null'>AND t.status = #{status}</if> "
            + "<if test='keyword != null'>AND (u.real_name LIKE #{keyword} OR u.account_no LIKE #{keyword} OR a.assignment_title LIKE #{keyword})</if> "
            + "</script>")
    long countAttempts(AdminAssignmentAttemptQuery query);

    @Select("SELECT t.id AS attempt_id, t.assignment_id, a.assignment_title, a.assignment_type, "
            + "a.course_id, c.course_name, t.student_id, u.real_name AS student_name, "
            + "u.account_no AS student_no, u.class_id, cl.class_name, a.total_score, t.status, "
            + "t.score, t.review_comment, t.reviewer_id, reviewer.real_name AS reviewer_name, "
            + "t.submitted_at, t.reviewed_at "
            + "FROM assignment_attempt t "
            + "JOIN course_assignment a ON a.id = t.assignment_id "
            + "JOIN course c ON c.id = a.course_id "
            + "JOIN sys_user u ON u.id = t.student_id "
            + "LEFT JOIN edu_class cl ON cl.id = u.class_id "
            + "LEFT JOIN sys_user reviewer ON reviewer.id = t.reviewer_id "
            + "WHERE t.id = #{attemptId} LIMIT 1")
    @Results(id = "attemptDetailMap", value = {
            @Result(column = "attempt_id", property = "attemptId", id = true),
            @Result(column = "assignment_id", property = "assignmentId"),
            @Result(column = "assignment_title", property = "assignmentTitle"),
            @Result(column = "assignment_type", property = "assignmentType"),
            @Result(column = "course_id", property = "courseId"),
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "student_name", property = "studentName"),
            @Result(column = "student_no", property = "studentNo"),
            @Result(column = "class_id", property = "classId"),
            @Result(column = "class_name", property = "className"),
            @Result(column = "total_score", property = "totalScore"),
            @Result(column = "status", property = "status"),
            @Result(column = "score", property = "score"),
            @Result(column = "review_comment", property = "reviewComment"),
            @Result(column = "reviewer_id", property = "reviewerId"),
            @Result(column = "reviewer_name", property = "reviewerName"),
            @Result(column = "submitted_at", property = "submittedAt"),
            @Result(column = "reviewed_at", property = "reviewedAt"),
            @Result(column = "attempt_id", property = "answers", many = @Many(select = "findAttemptAnswers"))
    })
    AdminAssignmentAttempt findAttempt(@Param("attemptId") Long attemptId);

    @Select("SELECT q.id AS question_id, q.question_type, q.title, q.standard_answer, ans.answer_content, "
            + "q.score AS question_score, COALESCE(ans.score, 0) AS score, ans.review_comment "
            + "FROM assignment_attempt t "
            + "JOIN assignment_question q ON q.assignment_id = t.assignment_id "
            + "LEFT JOIN assignment_answer ans ON ans.attempt_id = t.id AND ans.question_id = q.id "
            + "WHERE t.id = #{attemptId} ORDER BY q.sort_order ASC, q.id ASC")
    List<AdminAssignmentAttempt.Answer> findAttemptAnswers(@Param("attemptId") Long attemptId);

    @Insert("INSERT INTO assignment_answer "
            + "(attempt_id, question_id, answer_content, score, review_comment, created_at, updated_at) "
            + "VALUES (#{attemptId}, #{questionId}, NULL, #{score}, #{comment}, NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE score = VALUES(score), review_comment = VALUES(review_comment), updated_at = NOW()")
    void updateAnswerScore(@Param("attemptId") Long attemptId,
                           @Param("questionId") Long questionId,
                           @Param("score") Integer score,
                           @Param("comment") String comment);

    @Update("UPDATE assignment_attempt SET status = 'REVIEWED', score = #{score}, review_comment = #{reviewComment}, "
            + "reviewer_id = #{reviewerId}, reviewed_at = NOW(), updated_at = NOW() WHERE id = #{attemptId}")
    void markReviewed(@Param("attemptId") Long attemptId,
                      @Param("score") Integer score,
                      @Param("reviewComment") String reviewComment,
                      @Param("reviewerId") Long reviewerId);

    @Insert("INSERT INTO course_learning_progress (course_id, student_id, completed_items, updated_at) "
            + "SELECT ca.course_id, aa.student_id, COUNT(*), NOW() "
            + "FROM assignment_attempt aa "
            + "JOIN course_assignment ca ON ca.id = aa.assignment_id "
            + "JOIN course_content ct ON ct.course_id = ca.course_id "
            + "LEFT JOIN course_content_learning_progress cp "
            + "ON cp.content_id = ct.id AND cp.student_id = aa.student_id "
            + "LEFT JOIN course_assignment a ON a.content_id = ct.id "
            + "LEFT JOIN assignment_attempt completed_attempt "
            + "ON completed_attempt.assignment_id = a.id AND completed_attempt.student_id = aa.student_id "
            + "AND completed_attempt.status IN ('SUBMITTED', 'REVIEWED') "
            + "WHERE aa.id = #{attemptId} "
            + "AND ((ct.item_type = 'COURSEWARE' AND cp.completed = 1) "
            + "OR (ct.item_type = 'ASSIGNMENT' AND completed_attempt.id IS NOT NULL "
            + "AND (a.completion_rule = 'SUBMIT' "
            + "OR (a.completion_rule = 'PASS_SCORE' AND completed_attempt.score IS NOT NULL "
            + "AND completed_attempt.score >= COALESCE(a.pass_score, 0))))) "
            + "GROUP BY ca.course_id, aa.student_id "
            + "ON DUPLICATE KEY UPDATE completed_items = VALUES(completed_items), updated_at = NOW()")
    void refreshCourseProgress(@Param("attemptId") Long attemptId);

    @Insert("INSERT INTO assignment_review_log (attempt_id, reviewer_id, action, content, created_at) "
            + "VALUES (#{attemptId}, #{reviewerId}, #{action}, #{content}, NOW())")
    void insertReviewLog(@Param("attemptId") Long attemptId,
                         @Param("reviewerId") Long reviewerId,
                         @Param("action") String action,
                         @Param("content") String content);

    @Select("SELECT l.id AS log_id, l.attempt_id, l.reviewer_id, u.real_name AS reviewer_name, "
            + "l.action, l.content, l.created_at "
            + "FROM assignment_review_log l "
            + "LEFT JOIN sys_user u ON u.id = l.reviewer_id "
            + "WHERE l.attempt_id = #{attemptId} ORDER BY l.created_at DESC, l.id DESC")
    List<AdminAssignmentReviewLog> findReviewLogs(@Param("attemptId") Long attemptId);
}
