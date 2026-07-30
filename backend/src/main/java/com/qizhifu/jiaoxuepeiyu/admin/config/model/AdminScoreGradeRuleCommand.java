package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import java.math.BigDecimal;

public class AdminScoreGradeRuleCommand {

    private String gradeName;
    private BigDecimal minScore;
    private BigDecimal maxScore;

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
}
