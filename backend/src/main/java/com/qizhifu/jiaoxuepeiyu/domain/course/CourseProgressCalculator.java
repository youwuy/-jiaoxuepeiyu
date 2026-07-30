package com.qizhifu.jiaoxuepeiyu.domain.course;

public final class CourseProgressCalculator {

    private CourseProgressCalculator() {
    }

    public static int calculatePercent(int completedItems, int totalItems) {
        if (totalItems <= 0) {
            return 0;
        }

        int normalizedCompleted = Math.max(0, Math.min(completedItems, totalItems));
        return (int) Math.floor(normalizedCompleted * 100.0 / totalItems);
    }
}
