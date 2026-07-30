package com.qizhifu.jiaoxuepeiyu.domain.score;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ScoreCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private ScoreCalculator() {
    }

    public static BigDecimal calculate(ComprehensiveScoreInput input) {
        int totalWeight = input.getCoursewareWeight()
                + input.getTrainingPracticeWeight()
                + input.getAssignmentWeight()
                + input.getExamWeight();
        if (totalWeight != 100) {
            throw new IllegalArgumentException("Score weights must add up to 100");
        }

        BigDecimal total = weighted(input.getCoursewareLearningScore(), input.getCoursewareWeight())
                .add(weighted(input.getTrainingPracticeScore(), input.getTrainingPracticeWeight()))
                .add(weighted(input.getCourseAssignmentScore(), input.getAssignmentWeight()))
                .add(weighted(input.getExamScore(), input.getExamWeight()));

        return total.setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal weighted(BigDecimal score, int weight) {
        BigDecimal safeScore = score == null ? BigDecimal.ZERO : score;
        return safeScore.multiply(new BigDecimal(weight)).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
    }
}
