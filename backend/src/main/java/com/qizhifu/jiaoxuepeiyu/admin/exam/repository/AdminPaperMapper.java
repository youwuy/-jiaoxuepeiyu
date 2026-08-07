package com.qizhifu.jiaoxuepeiyu.admin.exam.repository;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaper;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionOption;
import java.util.List;
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
public interface AdminPaperMapper {

    @Select("<script>"
            + "SELECT p.id AS paper_id, p.paper_name, p.course_name, p.compose_mode, p.total_score, p.question_count, "
            + "p.publish_status, p.creator_id, u.real_name AS creator_name, p.created_at, p.updated_at "
            + "FROM exam_paper p "
            + "LEFT JOIN sys_user u ON u.id = p.creator_id "
            + "WHERE p.deleted_flag = 0 "
            + "<if test='keyword != null'>AND (p.paper_name LIKE #{keyword} OR p.course_name LIKE #{keyword})</if> "
            + "<if test='composeMode != null'>AND p.compose_mode = #{composeMode}</if> "
            + "<if test='publishStatus != null'>AND p.publish_status = #{publishStatus}</if> "
            + "<if test='creatorId != null'>AND p.creator_id = #{creatorId}</if> "
            + "ORDER BY p.updated_at DESC, p.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    @Results(id = "paperMap", value = {
            @Result(column = "paper_id", property = "paperId", id = true),
            @Result(column = "paper_name", property = "paperName"),
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "compose_mode", property = "composeMode"),
            @Result(column = "total_score", property = "totalScore"),
            @Result(column = "question_count", property = "questionCount"),
            @Result(column = "publish_status", property = "publishStatus"),
            @Result(column = "creator_id", property = "creatorId"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "paper_id", property = "questions", many = @Many(select = "findPaperQuestions"))
    })
    List<AdminPaper> findPapers(AdminPaperQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM exam_paper p WHERE p.deleted_flag = 0 "
            + "<if test='keyword != null'>AND (p.paper_name LIKE #{keyword} OR p.course_name LIKE #{keyword})</if> "
            + "<if test='composeMode != null'>AND p.compose_mode = #{composeMode}</if> "
            + "<if test='publishStatus != null'>AND p.publish_status = #{publishStatus}</if> "
            + "<if test='creatorId != null'>AND p.creator_id = #{creatorId}</if> "
            + "</script>")
    long countPapers(AdminPaperQuery query);

    @Select("SELECT p.id AS paper_id, p.paper_name, p.course_name, p.compose_mode, p.total_score, p.question_count, "
            + "p.publish_status, p.creator_id, u.real_name AS creator_name, p.created_at, p.updated_at "
            + "FROM exam_paper p "
            + "LEFT JOIN sys_user u ON u.id = p.creator_id "
            + "WHERE p.id = #{paperId} AND p.deleted_flag = 0 LIMIT 1")
    @Results(id = "paperDetailMap", value = {
            @Result(column = "paper_id", property = "paperId", id = true),
            @Result(column = "paper_name", property = "paperName"),
            @Result(column = "course_name", property = "courseName"),
            @Result(column = "compose_mode", property = "composeMode"),
            @Result(column = "total_score", property = "totalScore"),
            @Result(column = "question_count", property = "questionCount"),
            @Result(column = "publish_status", property = "publishStatus"),
            @Result(column = "creator_id", property = "creatorId"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "paper_id", property = "questions", many = @Many(select = "findPaperQuestions"))
    })
    AdminPaper findPaper(@Param("paperId") Long paperId);

    @Select("SELECT id AS paper_question_id, paper_id, question_id, question_type, title, options_json, "
            + "standard_answer, score, sort_order "
            + "FROM exam_paper_question WHERE paper_id = #{paperId} ORDER BY sort_order ASC, id ASC")
    @Results(id = "paperQuestionMap", value = {
            @Result(column = "question_id", property = "questionId"),
            @Result(column = "question_type", property = "questionType"),
            @Result(column = "title", property = "title"),
            @Result(column = "options_json", property = "optionsJson"),
            @Result(column = "standard_answer", property = "standardAnswer"),
            @Result(column = "score", property = "score"),
            @Result(column = "sort_order", property = "sortOrder")
    })
    List<AdminPaperQuestion> findPaperQuestions(@Param("paperId") Long paperId);

    @Select("<script>"
            + "SELECT q.id AS question_id, q.question_type, q.title, q.standard_answer, q.score, "
            + "q.enabled_flag AS enabled, q.creator_id, q.created_at, q.updated_at "
            + "FROM exam_question q "
            + "WHERE q.deleted_flag = 0 AND q.id IN "
            + "<foreach collection='questionIds' item='questionId' open='(' separator=',' close=')'>#{questionId}</foreach>"
            + "</script>")
    @Results(id = "paperQuestionSourceMap", value = {
            @Result(column = "question_id", property = "questionId", id = true),
            @Result(column = "question_type", property = "questionType"),
            @Result(column = "title", property = "title"),
            @Result(column = "standard_answer", property = "standardAnswer"),
            @Result(column = "score", property = "score"),
            @Result(column = "enabled", property = "enabled"),
            @Result(column = "creator_id", property = "creatorId"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "question_id", property = "options", many = @Many(select = "findQuestionOptions"))
    })
    List<AdminQuestion> findQuestionsByIds(@Param("questionIds") List<Long> questionIds);

    @Select("SELECT q.id AS question_id, q.question_type, q.title, q.standard_answer, q.score, "
            + "q.enabled_flag AS enabled "
            + "FROM exam_question q "
            + "WHERE q.deleted_flag = 0 AND q.enabled_flag = 1 AND q.question_type = #{questionType} "
            + "ORDER BY RAND() LIMIT #{limit}")
    @Results(id = "paperAutoQuestionMap", value = {
            @Result(column = "question_id", property = "questionId", id = true),
            @Result(column = "question_type", property = "questionType"),
            @Result(column = "title", property = "title"),
            @Result(column = "standard_answer", property = "standardAnswer"),
            @Result(column = "score", property = "score"),
            @Result(column = "enabled", property = "enabled")
    })
    List<AdminQuestion> findEnabledQuestionsByType(@Param("questionType") String questionType, @Param("limit") int limit);

    @Select("SELECT option_key, option_text, correct_flag AS correct, sort_order "
            + "FROM exam_question_option WHERE question_id = #{questionId} ORDER BY sort_order ASC, id ASC")
    List<AdminQuestionOption> findQuestionOptions(@Param("questionId") Long questionId);

    @Insert("INSERT INTO exam_paper "
            + "(paper_name, course_name, compose_mode, total_score, question_count, publish_status, creator_id, deleted_flag, created_at, updated_at) "
            + "VALUES (#{paperName}, #{courseName}, #{composeMode}, #{totalScore}, #{questionCount}, #{publishStatus}, "
            + "#{creatorId}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "paperId")
    void insertPaper(AdminPaper paper);

    @Update("UPDATE exam_paper SET paper_name = #{paperName}, course_name = #{courseName}, compose_mode = #{composeMode}, "
            + "total_score = #{totalScore}, question_count = #{questionCount}, updated_at = NOW() "
            + "WHERE id = #{paperId} AND deleted_flag = 0")
    void updatePaper(AdminPaper paper);

    @Delete("DELETE FROM exam_paper_question WHERE paper_id = #{paperId}")
    void deletePaperQuestions(@Param("paperId") Long paperId);

    @Insert("INSERT INTO exam_paper_question "
            + "(paper_id, question_id, question_type, title, options_json, standard_answer, score, sort_order, created_at) "
            + "VALUES (#{paperId}, #{question.questionId}, #{question.questionType}, #{question.title}, "
            + "#{question.optionsJson}, #{question.standardAnswer}, #{question.score}, #{question.sortOrder}, NOW())")
    void insertPaperQuestion(@Param("paperId") Long paperId, @Param("question") AdminPaperQuestion question);

    @Update("UPDATE exam_paper SET publish_status = #{publishStatus}, updated_at = NOW() "
            + "WHERE id = #{paperId} AND deleted_flag = 0")
    void updatePaperPublishStatus(@Param("paperId") Long paperId, @Param("publishStatus") String publishStatus);

    @Insert("INSERT INTO exam_paper_log (paper_id, operator_id, action, content, created_at) "
            + "VALUES (#{paperId}, #{operatorId}, #{action}, #{content}, NOW())")
    void insertPaperLog(@Param("paperId") Long paperId,
                        @Param("operatorId") Long operatorId,
                        @Param("action") String action,
                        @Param("content") String content);

    @Select("SELECT l.id AS log_id, l.paper_id, l.operator_id, u.real_name AS operator_name, "
            + "l.action, l.content, l.created_at "
            + "FROM exam_paper_log l "
            + "LEFT JOIN sys_user u ON u.id = l.operator_id "
            + "WHERE l.paper_id = #{paperId} ORDER BY l.created_at DESC, l.id DESC")
    List<AdminPaperLog> findPaperLogs(@Param("paperId") Long paperId);
}
