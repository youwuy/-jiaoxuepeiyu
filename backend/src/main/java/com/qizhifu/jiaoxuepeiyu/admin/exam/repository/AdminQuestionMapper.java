package com.qizhifu.jiaoxuepeiyu.admin.exam.repository;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionOption;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionQuery;
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
public interface AdminQuestionMapper {

    @Select("<script>"
            + "SELECT q.id AS question_id, q.question_type, q.title, q.standard_answer, q.explanation, q.score, "
            + "q.enabled_flag AS enabled, q.creator_id, u.real_name AS creator_name, q.created_at, q.updated_at "
            + "FROM exam_question q "
            + "LEFT JOIN sys_user u ON u.id = q.creator_id "
            + "WHERE q.deleted_flag = 0 "
            + "<if test='keyword != null'>AND q.title LIKE #{keyword}</if> "
            + "<if test='questionType != null'>AND q.question_type = #{questionType}</if> "
            + "<if test='enabled != null'>AND q.enabled_flag = #{enabled}</if> "
            + "<if test='creatorId != null'>AND q.creator_id = #{creatorId}</if> "
            + "ORDER BY q.updated_at DESC, q.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    @Results(id = "questionMap", value = {
            @Result(column = "question_id", property = "questionId", id = true),
            @Result(column = "question_type", property = "questionType"),
            @Result(column = "title", property = "title"),
            @Result(column = "standard_answer", property = "standardAnswer"),
            @Result(column = "explanation", property = "explanation"),
            @Result(column = "score", property = "score"),
            @Result(column = "enabled", property = "enabled"),
            @Result(column = "creator_id", property = "creatorId"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "question_id", property = "options", many = @Many(select = "findQuestionOptions"))
    })
    List<AdminQuestion> findQuestions(AdminQuestionQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM exam_question q WHERE q.deleted_flag = 0 "
            + "<if test='keyword != null'>AND q.title LIKE #{keyword}</if> "
            + "<if test='questionType != null'>AND q.question_type = #{questionType}</if> "
            + "<if test='enabled != null'>AND q.enabled_flag = #{enabled}</if> "
            + "<if test='creatorId != null'>AND q.creator_id = #{creatorId}</if> "
            + "</script>")
    long countQuestions(AdminQuestionQuery query);

    @Select("SELECT q.id AS question_id, q.question_type, q.title, q.standard_answer, q.explanation, q.score, "
            + "q.enabled_flag AS enabled, q.creator_id, u.real_name AS creator_name, q.created_at, q.updated_at "
            + "FROM exam_question q "
            + "LEFT JOIN sys_user u ON u.id = q.creator_id "
            + "WHERE q.id = #{questionId} AND q.deleted_flag = 0 LIMIT 1")
    @Results(id = "questionDetailMap", value = {
            @Result(column = "question_id", property = "questionId", id = true),
            @Result(column = "question_type", property = "questionType"),
            @Result(column = "title", property = "title"),
            @Result(column = "standard_answer", property = "standardAnswer"),
            @Result(column = "explanation", property = "explanation"),
            @Result(column = "score", property = "score"),
            @Result(column = "enabled", property = "enabled"),
            @Result(column = "creator_id", property = "creatorId"),
            @Result(column = "creator_name", property = "creatorName"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "question_id", property = "options", many = @Many(select = "findQuestionOptions"))
    })
    AdminQuestion findQuestion(@Param("questionId") Long questionId);

    @Select("<script>"
            + "SELECT q.id AS question_id, q.question_type, q.title, q.standard_answer, q.explanation, q.score, "
            + "q.enabled_flag AS enabled, q.creator_id, q.created_at, q.updated_at "
            + "FROM exam_question q "
            + "WHERE q.deleted_flag = 0 AND q.id IN "
            + "<foreach collection='questionIds' item='questionId' open='(' separator=',' close=')'>#{questionId}</foreach>"
            + "</script>")
    @Results(id = "questionByIdsMap", value = {
            @Result(column = "question_id", property = "questionId", id = true),
            @Result(column = "question_type", property = "questionType"),
            @Result(column = "title", property = "title"),
            @Result(column = "standard_answer", property = "standardAnswer"),
            @Result(column = "explanation", property = "explanation"),
            @Result(column = "score", property = "score"),
            @Result(column = "enabled", property = "enabled"),
            @Result(column = "creator_id", property = "creatorId"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "question_id", property = "options", many = @Many(select = "findQuestionOptions"))
    })
    List<AdminQuestion> findQuestionsByIds(@Param("questionIds") List<Long> questionIds);

    @Select("SELECT q.id AS question_id, q.question_type, q.title, q.standard_answer, q.explanation, q.score, "
            + "q.enabled_flag AS enabled, q.creator_id, q.created_at, q.updated_at "
            + "FROM exam_question q "
            + "WHERE q.deleted_flag = 0 AND q.enabled_flag = 1 AND q.question_type = #{questionType} "
            + "ORDER BY RAND() LIMIT #{limit}")
    @Results(id = "enabledQuestionMap", value = {
            @Result(column = "question_id", property = "questionId", id = true),
            @Result(column = "question_type", property = "questionType"),
            @Result(column = "title", property = "title"),
            @Result(column = "standard_answer", property = "standardAnswer"),
            @Result(column = "explanation", property = "explanation"),
            @Result(column = "score", property = "score"),
            @Result(column = "enabled", property = "enabled")
    })
    List<AdminQuestion> findEnabledQuestionsByType(@Param("questionType") String questionType, @Param("limit") int limit);

    @Select("SELECT option_key, option_text, correct_flag AS correct, sort_order "
            + "FROM exam_question_option WHERE question_id = #{questionId} ORDER BY sort_order ASC, id ASC")
    List<AdminQuestionOption> findQuestionOptions(@Param("questionId") Long questionId);

    @Insert("INSERT INTO exam_question "
            + "(question_type, title, standard_answer, explanation, score, enabled_flag, creator_id, deleted_flag, created_at, updated_at) "
            + "VALUES (#{questionType}, #{title}, #{standardAnswer}, #{explanation}, #{score}, #{enabled}, #{creatorId}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "questionId")
    void insertQuestion(AdminQuestion question);

    @Update("UPDATE exam_question SET question_type = #{questionType}, title = #{title}, "
            + "standard_answer = #{standardAnswer}, explanation = #{explanation}, score = #{score}, updated_at = NOW() "
            + "WHERE id = #{questionId} AND deleted_flag = 0")
    void updateQuestion(AdminQuestion question);

    @Delete("DELETE FROM exam_question_option WHERE question_id = #{questionId}")
    void deleteQuestionOptions(@Param("questionId") Long questionId);

    @Insert("INSERT INTO exam_question_option "
            + "(question_id, option_key, option_text, correct_flag, sort_order, created_at) "
            + "VALUES (#{questionId}, #{option.optionKey}, #{option.optionText}, #{option.correct}, "
            + "#{option.sortOrder}, NOW())")
    void insertQuestionOption(@Param("questionId") Long questionId, @Param("option") AdminQuestionOption option);

    @Update("UPDATE exam_question SET enabled_flag = #{enabled}, updated_at = NOW() "
            + "WHERE id = #{questionId} AND deleted_flag = 0")
    void updateQuestionStatus(@Param("questionId") Long questionId, @Param("enabled") boolean enabled);

    @Insert("INSERT INTO exam_question_log (question_id, operator_id, action, content, created_at) "
            + "VALUES (#{questionId}, #{operatorId}, #{action}, #{content}, NOW())")
    void insertQuestionLog(@Param("questionId") Long questionId,
                           @Param("operatorId") Long operatorId,
                           @Param("action") String action,
                           @Param("content") String content);

    @Select("SELECT l.id AS log_id, l.question_id, l.operator_id, u.real_name AS operator_name, "
            + "l.action, l.content, l.created_at "
            + "FROM exam_question_log l "
            + "LEFT JOIN sys_user u ON u.id = l.operator_id "
            + "WHERE l.question_id = #{questionId} ORDER BY l.created_at DESC, l.id DESC")
    List<AdminQuestionLog> findQuestionLogs(@Param("questionId") Long questionId);
}
