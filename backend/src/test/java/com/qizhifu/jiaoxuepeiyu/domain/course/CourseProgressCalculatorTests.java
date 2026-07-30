package com.qizhifu.jiaoxuepeiyu.domain.course;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CourseProgressCalculatorTests {

    @Test
    void floorsCompletedItemsPercentage() {
        assertEquals(77, CourseProgressCalculator.calculatePercent(7, 9));
    }

    @Test
    void returnsZeroWhenCourseHasNoItems() {
        assertEquals(0, CourseProgressCalculator.calculatePercent(0, 0));
    }

    @Test
    void capsInvalidCompletedCountAtTotal() {
        assertEquals(100, CourseProgressCalculator.calculatePercent(12, 9));
    }
}
