package com.qizhifu.jiaoxuepeiyu.domain.score;

import java.math.BigDecimal;

public class ComprehensiveScoreInput {

    private final BigDecimal coursewareLearningScore;
    private final BigDecimal trainingPracticeScore;
    private final BigDecimal courseAssignmentScore;
    private final BigDecimal examScore;
    private final int coursewareWeight;
    private final int trainingPracticeWeight;
    private final int assignmentWeight;
    private final int examWeight;

    public ComprehensiveScoreInput(BigDecimal coursewareLearningScore,
                                   BigDecimal trainingPracticeScore,
                                   BigDecimal courseAssignmentScore,
                                   BigDecimal examScore,
                                   int coursewareWeight,
                                   int trainingPracticeWeight,
                                   int assignmentWeight,
                                   int examWeight) {
        this.coursewareLearningScore = coursewareLearningScore;
        this.trainingPracticeScore = trainingPracticeScore;
        this.courseAssignmentScore = courseAssignmentScore;
        this.examScore = examScore;
        this.coursewareWeight = coursewareWeight;
        this.trainingPracticeWeight = trainingPracticeWeight;
        this.assignmentWeight = assignmentWeight;
        this.examWeight = examWeight;
    }

    public BigDecimal getCoursewareLearningScore() {
        return coursewareLearningScore;
    }

    public BigDecimal getTrainingPracticeScore() {
        return trainingPracticeScore;
    }

    public BigDecimal getCourseAssignmentScore() {
        return courseAssignmentScore;
    }

    public BigDecimal getExamScore() {
        return examScore;
    }

    public int getCoursewareWeight() {
        return coursewareWeight;
    }

    public int getTrainingPracticeWeight() {
        return trainingPracticeWeight;
    }

    public int getAssignmentWeight() {
        return assignmentWeight;
    }

    public int getExamWeight() {
        return examWeight;
    }
}
