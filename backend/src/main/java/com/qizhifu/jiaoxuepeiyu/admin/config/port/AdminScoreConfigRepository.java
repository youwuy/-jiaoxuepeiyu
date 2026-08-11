package com.qizhifu.jiaoxuepeiyu.admin.config.port;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRule;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleLog;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeight;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeightCommand;
import java.util.List;

public interface AdminScoreConfigRepository {

    List<AdminScoreWeight> findScoreWeights(Long semesterId);

    Long createScoreWeight(AdminScoreWeightCommand command, Long createdBy);

    List<AdminScoreGradeRule> findGradeRules();

    void replaceGradeRules(List<AdminScoreGradeRuleCommand> rules);

    void createGradeRuleLog(String beforeContent, String afterContent, Long operatorId);

    List<AdminScoreGradeRuleLog> findGradeRuleLogs();
}
