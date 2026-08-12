package com.qizhifu.jiaoxuepeiyu.admin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRule;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleLog;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeightCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminScoreConfigRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminScoreConfigServiceTests {

    @Test
    void createsScoreWeightWhenWeightsAddToOneHundred() {
        FakeScores repository = new FakeScores();
        AdminScoreConfigService service = new AdminScoreConfigService(repository);

        Long weightId = service.createScoreWeight(weight(), 9L);

        assertEquals(30L, weightId.longValue());
        assertEquals(9L, repository.createdBy.longValue());
    }

    @Test
    void rejectsScoreWeightThatDoesNotAddToOneHundred() {
        AdminScoreConfigService service = new AdminScoreConfigService(new FakeScores());
        AdminScoreWeightCommand command = weight();
        command.setExamWeight(10);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createScoreWeight(command, 9L);
        });

        assertEquals("Score weights must add up to 100", exception.getMessage());
    }

    @Test
    void rejectsOverlappingGradeRules() {
        AdminScoreConfigService service = new AdminScoreConfigService(new FakeScores());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.replaceGradeRules(Arrays.asList(
                    grade("A", "90", "100"),
                    grade("B", "80", "95")), 9L);
        });

        assertEquals("Score grade rules cannot overlap", exception.getMessage());
    }

    @Test
    void replacesSortedGradeRules() {
        FakeScores repository = new FakeScores();
        AdminScoreConfigService service = new AdminScoreConfigService(repository);

        service.replaceGradeRules(Arrays.asList(
                grade("B", "0", "89"),
                grade("A", "90", "100")), 9L);

        assertEquals(true, repository.replacedGrades);
        assertEquals("A", repository.gradeRules.get(0).getGradeName());
        assertEquals("未配置", repository.beforeContent);
        assertEquals("A（90%-100%）、B（0%-89%）", repository.afterContent);
        assertEquals(9L, repository.logOperatorId.longValue());
    }

    private AdminScoreWeightCommand weight() {
        AdminScoreWeightCommand command = new AdminScoreWeightCommand();
        command.setSemesterId(1L);
        command.setCoursewareWeight(20);
        command.setTrainingPracticeWeight(30);
        command.setAssignmentWeight(20);
        command.setExamWeight(30);
        return command;
    }

    private AdminScoreGradeRuleCommand grade(String name, String min, String max) {
        AdminScoreGradeRuleCommand command = new AdminScoreGradeRuleCommand();
        command.setGradeName(name);
        command.setMinScore(new BigDecimal(min));
        command.setMaxScore(new BigDecimal(max));
        return command;
    }

    private static class FakeScores implements AdminScoreConfigRepository {
        private Long createdBy;
        private boolean replacedGrades;
        private List<AdminScoreGradeRuleCommand> gradeRules;
        private String beforeContent;
        private String afterContent;
        private Long logOperatorId;

        @Override
        public List<com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeight> findScoreWeights(Long semesterId) {
            return new ArrayList<com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeight>();
        }

        @Override
        public Long createScoreWeight(AdminScoreWeightCommand command, Long createdBy) {
            this.createdBy = createdBy;
            return 30L;
        }

        @Override
        public List<AdminScoreGradeRule> findGradeRules() {
            return new ArrayList<AdminScoreGradeRule>();
        }

        @Override
        public void replaceGradeRules(List<AdminScoreGradeRuleCommand> rules) {
            this.replacedGrades = true;
            this.gradeRules = rules;
        }

        @Override
        public void createGradeRuleLog(String beforeContent, String afterContent, Long operatorId) {
            this.beforeContent = beforeContent;
            this.afterContent = afterContent;
            this.logOperatorId = operatorId;
        }

        @Override
        public List<AdminScoreGradeRuleLog> findGradeRuleLogs() {
            return new ArrayList<AdminScoreGradeRuleLog>();
        }
    }
}
