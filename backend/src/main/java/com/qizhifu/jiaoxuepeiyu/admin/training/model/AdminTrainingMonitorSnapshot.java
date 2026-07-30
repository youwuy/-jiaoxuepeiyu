package com.qizhifu.jiaoxuepeiyu.admin.training.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class AdminTrainingMonitorSnapshot {

    private Long trainingId;
    private LocalDateTime generatedAt;
    private List<AdminTrainingCameraState> cameras = Collections.emptyList();
    private List<AdminTrainingStudentState> students = Collections.emptyList();
    private AdminTrainingStatistics statistics;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<AdminTrainingCameraState> getCameras() {
        return cameras;
    }

    public void setCameras(List<AdminTrainingCameraState> cameras) {
        this.cameras = cameras == null ? Collections.<AdminTrainingCameraState>emptyList() : cameras;
    }

    public List<AdminTrainingStudentState> getStudents() {
        return students;
    }

    public void setStudents(List<AdminTrainingStudentState> students) {
        this.students = students == null ? Collections.<AdminTrainingStudentState>emptyList() : students;
    }

    public AdminTrainingStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(AdminTrainingStatistics statistics) {
        this.statistics = statistics;
    }
}
