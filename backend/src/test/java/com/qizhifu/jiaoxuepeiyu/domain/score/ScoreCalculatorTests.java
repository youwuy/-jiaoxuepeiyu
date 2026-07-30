package com.qizhifu.jiaoxuepeiyu.domain.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ScoreCalculatorTests {

    @Test
    void calculatesWeightedSemesterScoreWithOneDecimalPlace() {
        ComprehensiveScoreInput input = new ComprehensiveScoreInput(
                new BigDecimal("80"),
                new BigDecimal("90"),
                new BigDecimal("70"),
                new BigDecimal("100"),
                20,
                30,
                20,
                30);

        assertEquals(new BigDecimal("88.0"), ScoreCalculator.calculate(input));
    }

    @Test
    void handlesDecimalScoresAndRoundsHalfUp() {
        ComprehensiveScoreInput input = new ComprehensiveScoreInput(
                new BigDecimal("88.8"),
                new BigDecimal("91.6"),
                new BigDecimal("77.3"),
                new BigDecimal("85.5"),
                25,
                25,
                25,
                25);

        assertEquals(new BigDecimal("85.8"), ScoreCalculator.calculate(input));
    }

    @Test
    void rejectsWeightsThatDoNotAddToOneHundred() {
        ComprehensiveScoreInput input = new ComprehensiveScoreInput(
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.TEN,
                20,
                20,
                20,
                20);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ScoreCalculator.calculate(input);
        });
        assertEquals("Score weights must add up to 100", ex.getMessage());
    }
}
