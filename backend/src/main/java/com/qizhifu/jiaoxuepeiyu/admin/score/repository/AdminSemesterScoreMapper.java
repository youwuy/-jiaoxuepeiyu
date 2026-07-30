package com.qizhifu.jiaoxuepeiyu.admin.score.repository;

import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminSemesterScoreMapper {

    String SCORE_EXPR = "COALESCE(ss.comprehensive_score, ROUND("
            + "COALESCE(ss.courseware_learning_score, 0) * ss.courseware_weight / 100 + "
            + "COALESCE(ss.training_practice_score, 0) * ss.training_practice_weight / 100 + "
            + "COALESCE(ss.course_assignment_score, 0) * ss.assignment_weight / 100 + "
            + "COALESCE(ss.exam_score, 0) * ss.exam_weight / 100, 1))";

    @Select("<script>"
            + "SELECT ss.id AS score_id, ss.student_id, u.real_name AS student_name, u.username AS student_no, "
            + "u.class_id, c.class_name, c.major_id, m.major_name, ss.semester_id, "
            + "CONCAT(y.year_name, ' ', s.semester_name) AS academic_term, "
            + "ss.courseware_learning_score, ss.training_practice_score, ss.course_assignment_score, ss.exam_score, "
            + "ss.courseware_weight, ss.training_practice_weight, ss.assignment_weight, ss.exam_weight, "
            + SCORE_EXPR + " AS comprehensive_score, ss.published_at "
            + "FROM score_semester_summary ss "
            + "JOIN sys_user u ON u.id = ss.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "LEFT JOIN edu_major m ON m.id = c.major_id "
            + "JOIN edu_semester s ON s.id = ss.semester_id "
            + "JOIN edu_academic_year y ON y.id = s.academic_year_id "
            + "WHERE u.user_type = 'student' "
            + "<if test='semesterId != null'>AND ss.semester_id = #{semesterId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='majorId != null'>AND c.major_id = #{majorId}</if> "
            + "<if test='studentId != null'>AND ss.student_id = #{studentId}</if> "
            + "<if test='keyword != null'>AND (u.real_name LIKE #{keyword} OR u.username LIKE #{keyword})</if> "
            + "ORDER BY ss.semester_id DESC, " + SCORE_EXPR + " DESC, ss.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    List<AdminSemesterScore> findScores(AdminSemesterScoreQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM score_semester_summary ss "
            + "JOIN sys_user u ON u.id = ss.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE u.user_type = 'student' "
            + "<if test='semesterId != null'>AND ss.semester_id = #{semesterId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='majorId != null'>AND c.major_id = #{majorId}</if> "
            + "<if test='studentId != null'>AND ss.student_id = #{studentId}</if> "
            + "<if test='keyword != null'>AND (u.real_name LIKE #{keyword} OR u.username LIKE #{keyword})</if> "
            + "</script>")
    long countScores(AdminSemesterScoreQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) AS student_count, AVG(" + SCORE_EXPR + ") AS average_score, "
            + "MAX(" + SCORE_EXPR + ") AS max_score, MIN(" + SCORE_EXPR + ") AS min_score, "
            + "SUM(CASE WHEN " + SCORE_EXPR + " &gt;= 90 THEN 1 ELSE 0 END) AS excellent_count, "
            + "SUM(CASE WHEN " + SCORE_EXPR + " &gt;= 60 THEN 1 ELSE 0 END) AS pass_count "
            + "FROM score_semester_summary ss "
            + "JOIN sys_user u ON u.id = ss.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE u.user_type = 'student' "
            + "<if test='semesterId != null'>AND ss.semester_id = #{semesterId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='majorId != null'>AND c.major_id = #{majorId}</if> "
            + "<if test='studentId != null'>AND ss.student_id = #{studentId}</if> "
            + "<if test='keyword != null'>AND (u.real_name LIKE #{keyword} OR u.username LIKE #{keyword})</if> "
            + "</script>")
    AdminSemesterScoreStatistics calculateStatistics(AdminSemesterScoreQuery query);

    @Select("<script>"
            + "SELECT ss.id AS score_id, ss.student_id, u.real_name AS student_name, u.username AS student_no, "
            + "u.class_id, c.class_name, c.major_id, m.major_name, ss.semester_id, "
            + "CONCAT(y.year_name, ' ', s.semester_name) AS academic_term, "
            + "ss.courseware_learning_score, ss.training_practice_score, ss.course_assignment_score, ss.exam_score, "
            + "ss.courseware_weight, ss.training_practice_weight, ss.assignment_weight, ss.exam_weight, "
            + SCORE_EXPR + " AS comprehensive_score, ss.published_at "
            + "FROM score_semester_summary ss "
            + "JOIN sys_user u ON u.id = ss.student_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "LEFT JOIN edu_major m ON m.id = c.major_id "
            + "JOIN edu_semester s ON s.id = ss.semester_id "
            + "JOIN edu_academic_year y ON y.id = s.academic_year_id "
            + "WHERE u.user_type = 'student' "
            + "<if test='semesterId != null'>AND ss.semester_id = #{semesterId}</if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='majorId != null'>AND c.major_id = #{majorId}</if> "
            + "<if test='studentId != null'>AND ss.student_id = #{studentId}</if> "
            + "<if test='keyword != null'>AND (u.real_name LIKE #{keyword} OR u.username LIKE #{keyword})</if> "
            + "ORDER BY " + SCORE_EXPR + " DESC, ss.id ASC LIMIT #{pageSize} "
            + "</script>")
    List<AdminSemesterScore> findRanking(AdminSemesterScoreQuery query);
}
