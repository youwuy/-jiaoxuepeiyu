package com.qizhifu.jiaoxuepeiyu.admin.config.repository;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRule;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeight;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminScoreConfigMapper {

    @Select("<script>"
            + "SELECT w.id AS weight_id, w.semester_id, w.courseware_weight, w.training_practice_weight, "
            + "w.assignment_weight, w.exam_weight, w.effective_from, w.created_by, "
            + "u.real_name AS operator_name, w.created_at "
            + "FROM edu_score_weight w LEFT JOIN sys_user u ON u.id = w.created_by WHERE 1 = 1 "
            + "<if test='semesterId != null'>AND semester_id = #{semesterId}</if> "
            + "ORDER BY effective_from DESC, id DESC"
            + "</script>")
    List<AdminScoreWeight> findScoreWeights(@Param("semesterId") Long semesterId);

    @Insert("INSERT INTO edu_score_weight "
            + "(semester_id, courseware_weight, training_practice_weight, assignment_weight, "
            + "exam_weight, effective_from, created_by, created_at) "
            + "VALUES (#{semesterId}, #{coursewareWeight}, #{trainingPracticeWeight}, "
            + "#{assignmentWeight}, #{examWeight}, NOW(), #{createdBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "weightId")
    void insertScoreWeight(AdminScoreWeight weight);

    @Select("SELECT id AS rule_id, grade_name, min_score, max_score, sort_order "
            + "FROM edu_score_grade_rule ORDER BY sort_order ASC, min_score DESC, id ASC")
    List<AdminScoreGradeRule> findGradeRules();

    @Delete("DELETE FROM edu_score_grade_rule")
    void deleteGradeRules();

    @Insert("<script>"
            + "INSERT INTO edu_score_grade_rule (grade_name, min_score, max_score, sort_order, created_at) VALUES "
            + "<foreach collection='rules' item='rule' index='index' separator=','>"
            + "(#{rule.gradeName}, #{rule.minScore}, #{rule.maxScore}, #{index}, NOW())"
            + "</foreach>"
            + "</script>")
    void insertGradeRules(@Param("rules") List<AdminScoreGradeRuleCommand> rules);
}
