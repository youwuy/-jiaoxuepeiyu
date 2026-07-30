package com.qizhifu.jiaoxuepeiyu.admin.course.model;

public class AdminCourseStatistics {

    private Long courseId;
    private Integer studentCount;
    private Integer completedCount;
    private Integer studyingCount;
    private Integer notStartedCount;
    private Integer pendingReviewCount;
    private Double averageScore;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getStudyingCount() {
        return studyingCount;
    }

    public void setStudyingCount(Integer studyingCount) {
        this.studyingCount = studyingCount;
    }

    public Integer getNotStartedCount() {
        return notStartedCount;
    }

    public void setNotStartedCount(Integer notStartedCount) {
        this.notStartedCount = notStartedCount;
    }

    public Integer getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(Integer pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
}
