package com.qizhifu.jiaoxuepeiyu.admin.config.repository;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRule;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeight;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeightCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminScoreConfigRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminScoreConfigRepository implements AdminScoreConfigRepository {

    private final AdminScoreConfigMapper mapper;

    public MyBatisAdminScoreConfigRepository(AdminScoreConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminScoreWeight> findScoreWeights(Long semesterId) {
        return mapper.findScoreWeights(semesterId);
    }

    @Override
    public Long createScoreWeight(AdminScoreWeightCommand command, Long createdBy) {
        AdminScoreWeight weight = new AdminScoreWeight();
        weight.setSemesterId(command.getSemesterId());
        weight.setCoursewareWeight(command.getCoursewareWeight());
        weight.setTrainingPracticeWeight(command.getTrainingPracticeWeight());
        weight.setAssignmentWeight(command.getAssignmentWeight());
        weight.setExamWeight(command.getExamWeight());
        weight.setCreatedBy(createdBy);
        mapper.insertScoreWeight(weight);
        return weight.getWeightId();
    }

    @Override
    public List<AdminScoreGradeRule> findGradeRules() {
        return mapper.findGradeRules();
    }

    @Override
    public void replaceGradeRules(List<AdminScoreGradeRuleCommand> rules) {
        mapper.deleteGradeRules();
        mapper.insertGradeRules(rules);
    }
}
