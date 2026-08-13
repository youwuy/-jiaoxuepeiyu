package com.qizhifu.jiaoxuepeiyu.admin.training.model;

public class AdminTrainingStudentState {

    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long classId;
    private String className;
    private String clientIp;
    private String deskStatus;
    private String progressStatus;
    private String currentTopicName;
    private String trainingMode;
    private Integer submittedTopicCount;
    private Integer totalTopicCount;
    private Double score;
    private Double teamScore;
    private Long roomId;
    private String roomCode;
    private String roomStatus;
    private String roleName;
    private String teammateNames;
    private String desktopStreamUrl;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDeskStatus() {
        return deskStatus;
    }

    public void setDeskStatus(String deskStatus) {
        this.deskStatus = deskStatus;
    }

    public String getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(String progressStatus) {
        this.progressStatus = progressStatus;
    }

    public String getCurrentTopicName() { return currentTopicName; }
    public void setCurrentTopicName(String currentTopicName) { this.currentTopicName = currentTopicName; }
    public String getTrainingMode() { return trainingMode; }
    public void setTrainingMode(String trainingMode) { this.trainingMode = trainingMode; }
    public Integer getSubmittedTopicCount() { return submittedTopicCount; }
    public void setSubmittedTopicCount(Integer submittedTopicCount) { this.submittedTopicCount = submittedTopicCount; }
    public Integer getTotalTopicCount() { return totalTopicCount; }
    public void setTotalTopicCount(Integer totalTopicCount) { this.totalTopicCount = totalTopicCount; }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getTeamScore() { return teamScore; }
    public void setTeamScore(Double teamScore) { this.teamScore = teamScore; }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getTeammateNames() { return teammateNames; }
    public void setTeammateNames(String teammateNames) { this.teammateNames = teammateNames; }
    public String getDesktopStreamUrl() { return desktopStreamUrl; }
    public void setDesktopStreamUrl(String desktopStreamUrl) { this.desktopStreamUrl = desktopStreamUrl; }
}
