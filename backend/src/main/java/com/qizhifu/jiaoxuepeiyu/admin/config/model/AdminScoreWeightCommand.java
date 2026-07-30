package com.qizhifu.jiaoxuepeiyu.admin.config.model;

public class AdminScoreWeightCommand {

    private Long semesterId;
    private int coursewareWeight;
    private int trainingPracticeWeight;
    private int assignmentWeight;
    private int examWeight;

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
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
