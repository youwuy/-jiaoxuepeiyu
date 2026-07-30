package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingStatistics {

    private Long trainingId;
    private Integer participantCount;
    private Integer waitingRoomCount;
    private Integer startedRoomCount;
    private Integer dissolvedRoomCount;
    private Integer submittedAttemptCount;
    private Double averageScore;
    private Double maxScore;
    private Double minScore;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public Integer getWaitingRoomCount() {
        return waitingRoomCount;
    }

    public void setWaitingRoomCount(Integer waitingRoomCount) {
        this.waitingRoomCount = waitingRoomCount;
    }

    public Integer getStartedRoomCount() {
        return startedRoomCount;
    }

    public void setStartedRoomCount(Integer startedRoomCount) {
        this.startedRoomCount = startedRoomCount;
    }

    public Integer getDissolvedRoomCount() {
        return dissolvedRoomCount;
    }

    public void setDissolvedRoomCount(Integer dissolvedRoomCount) {
        this.dissolvedRoomCount = dissolvedRoomCount;
    }

    public Integer getSubmittedAttemptCount() {
        return submittedAttemptCount;
    }

    public void setSubmittedAttemptCount(Integer submittedAttemptCount) {
        this.submittedAttemptCount = submittedAttemptCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }
}
