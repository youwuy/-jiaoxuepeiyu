package com.qizhifu.jiaoxuepeiyu.ue.model;

public class UeScoreWeight {

    private int coursewareWeight;
    private int trainingPracticeWeight;
    private int assignmentWeight;
    private int examWeight;

    public UeScoreWeight() {
    }

    public UeScoreWeight(int coursewareWeight, int trainingPracticeWeight, int assignmentWeight, int examWeight) {
        this.coursewareWeight = coursewareWeight;
        this.trainingPracticeWeight = trainingPracticeWeight;
        this.assignmentWeight = assignmentWeight;
        this.examWeight = examWeight;
    }

    public int getCoursewareWeight() {
        return coursewareWeight;
    }

    public void setCoursewareWeight(int coursewareWeight) {
        this.coursewareWeight = coursewareWeight;
    }

    public int getTrainingPracticeWeight() {
        return trainingPracticeWeight;
    }

    public void setTrainingPracticeWeight(int trainingPracticeWeight) {
        this.trainingPracticeWeight = trainingPracticeWeight;
    }

    public int getAssignmentWeight() {
        return assignmentWeight;
    }

    public void setAssignmentWeight(int assignmentWeight) {
        this.assignmentWeight = assignmentWeight;
    }

    public int getExamWeight() {
        return examWeight;
    }

    public void setExamWeight(int examWeight) {
        this.examWeight = examWeight;
    }
}
