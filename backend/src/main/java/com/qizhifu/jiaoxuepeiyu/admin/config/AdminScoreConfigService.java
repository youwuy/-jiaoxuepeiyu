package com.qizhifu.jiaoxuepeiyu.admin.config;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRule;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleLog;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeight;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeightCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminScoreConfigRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminScoreConfigService {

    private static final BigDecimal ZERO = new BigDecimal("0");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AdminScoreConfigRepository repository;

    public AdminScoreConfigService(AdminScoreConfigRepository repository) {
        this.repository = repository;
    }

    public List<AdminScoreWeight> listScoreWeights(Long semesterId) {
        return repository.findScoreWeights(semesterId);
    }

    public Long createScoreWeight(AdminScoreWeightCommand command, Long createdBy) {
        AdminScoreWeightCommand normalized = normalizedWeight(command);
        if (createdBy == null) {
            throw new BusinessException(401, "Missing admin identity");
        }
        return repository.createScoreWeight(normalized, createdBy);
    }

    public List<AdminScoreGradeRule> listGradeRules() {
        return repository.findGradeRules();
    }

    public List<AdminScoreGradeRuleLog> listGradeRuleLogs() {
        return repository.findGradeRuleLogs();
    }

    @Transactional
    public void replaceGradeRules(List<AdminScoreGradeRuleCommand> rules, Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(401, "Missing admin identity");
        }
        List<AdminScoreGradeRuleCommand> normalized = normalizedRules(rules);
        String beforeContent = formatExistingRules(repository.findGradeRules());
        String afterContent = formatRuleCommands(normalized);
        repository.replaceGradeRules(normalized);
        repository.createGradeRuleLog(beforeContent, afterContent, operatorId);
    }

    private String formatExistingRules(List<AdminScoreGradeRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return "未配置";
        }
        List<String> values = new ArrayList<String>();
        for (AdminScoreGradeRule rule : rules) {
            values.add(formatRule(rule.getGradeName(), rule.getMinScore(), rule.getMaxScore()));
        }
        return join(values);
    }

    private String formatRuleCommands(List<AdminScoreGradeRuleCommand> rules) {
        List<String> values = new ArrayList<String>();
        for (AdminScoreGradeRuleCommand rule : rules) {
            values.add(formatRule(rule.getGradeName(), rule.getMinScore(), rule.getMaxScore()));
        }
        return join(values);
    }

    private String formatRule(String name, BigDecimal minScore, BigDecimal maxScore) {
        return name + "（" + minScore.stripTrailingZeros().toPlainString() + "%-"
                + maxScore.stripTrailingZeros().toPlainString() + "%）";
    }

    private String join(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append("、");
            }
            result.append(value);
        }
        return result.toString();
    }

    private AdminScoreWeightCommand normalizedWeight(AdminScoreWeightCommand command) {
        if (command == null || command.getSemesterId() == null) {
            throw new BusinessException(400, "Semester is required");
        }
        int total = command.getCoursewareWeight()
                + command.getTrainingPracticeWeight()
                + command.getAssignmentWeight()
                + command.getExamWeight();
        if (total != 100) {
            throw new BusinessException(400, "Score weights must add up to 100");
        }
        if (command.getCoursewareWeight() < 0
                || command.getTrainingPracticeWeight() < 0
                || command.getAssignmentWeight() < 0
                || command.getExamWeight() < 0) {
            throw new BusinessException(400, "Score weights cannot be negative");
        }
        return command;
    }

    private List<AdminScoreGradeRuleCommand> normalizedRules(List<AdminScoreGradeRuleCommand> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new BusinessException(400, "Score grade rules are required");
        }
        List<AdminScoreGradeRuleCommand> normalized = new ArrayList<AdminScoreGradeRuleCommand>();
        for (AdminScoreGradeRuleCommand rule : rules) {
            normalized.add(normalizedRule(rule));
        }
        Collections.sort(normalized, new GradeRuleComparator());
        for (int i = 1; i < normalized.size(); i++) {
            AdminScoreGradeRuleCommand previous = normalized.get(i - 1);
            AdminScoreGradeRuleCommand current = normalized.get(i);
            if (current.getMaxScore().compareTo(previous.getMinScore()) > 0) {
                throw new BusinessException(400, "Score grade rules cannot overlap");
            }
        }
        return normalized;
    }

    private AdminScoreGradeRuleCommand normalizedRule(AdminScoreGradeRuleCommand rule) {
        if (rule == null || !InputValidator.hasText(rule.getGradeName())) {
            throw new BusinessException(400, "Grade name is required");
        }
        if (rule.getMinScore() == null || rule.getMaxScore() == null) {
            throw new BusinessException(400, "Grade score range is required");
        }
        if (rule.getMinScore().compareTo(ZERO) < 0 || rule.getMaxScore().compareTo(ONE_HUNDRED) > 0) {
            throw new BusinessException(400, "Grade score range must be between 0 and 100");
        }
        if (rule.getMinScore().compareTo(rule.getMaxScore()) > 0) {
            throw new BusinessException(400, "Grade min score cannot exceed max score");
        }
        AdminScoreGradeRuleCommand normalized = new AdminScoreGradeRuleCommand();
        normalized.setGradeName(rule.getGradeName().trim());
        normalized.setMinScore(rule.getMinScore());
        normalized.setMaxScore(rule.getMaxScore());
        return normalized;
    }

    private static class GradeRuleComparator implements Comparator<AdminScoreGradeRuleCommand> {
        @Override
        public int compare(AdminScoreGradeRuleCommand left, AdminScoreGradeRuleCommand right) {
            return right.getMinScore().compareTo(left.getMinScore());
        }
    }
}
