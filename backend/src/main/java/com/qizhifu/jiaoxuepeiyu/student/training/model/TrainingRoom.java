package com.qizhifu.jiaoxuepeiyu.student.training.model;

import java.util.ArrayList;
import java.util.List;

public class TrainingRoom {

    private Long roomId;
    private Long trainingId;
    private Long topicId;
    private String topicName;
    private String trainingName;
    private String roomCode;
    private String roomStatus;
    private Long ownerStudentId;
    private int teamSize;
    private List<TrainingRoomMember> members = new ArrayList<TrainingRoomMember>();
    private List<TrainingRoomRole> roles = new ArrayList<TrainingRoomRole>();

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public Long getOwnerStudentId() {
        return ownerStudentId;
    }

    public void setOwnerStudentId(Long ownerStudentId) {
        this.ownerStudentId = ownerStudentId;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public List<TrainingRoomMember> getMembers() {
        return members;
    }

    public void setMembers(List<TrainingRoomMember> members) {
        this.members = members;
    }

    public List<TrainingRoomRole> getRoles() {
        return roles;
    }

    public void setRoles(List<TrainingRoomRole> roles) {
        this.roles = roles;
    }
}
