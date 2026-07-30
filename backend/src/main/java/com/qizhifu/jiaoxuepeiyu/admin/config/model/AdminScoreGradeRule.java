package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import java.math.BigDecimal;

public class AdminScoreGradeRule {

    private Long ruleId;
    private String gradeName;
    private BigDecimal minScore;
    private BigDecimal maxScore;
    private int sortOrder;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public BigDecimal getMinScore() {
        return minScore;
    }

    public void setMinScore(BigDecimal minScore) {
        this.minScore = minScore;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
