package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminClassroom {

    private Long classroomId;
    private String roomName;
    private int cameraCount;
    private LocalDateTime createdAt;
    private List<AdminCamera> cameras = new ArrayList<AdminCamera>();

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCameraCount() {
        return cameraCount;
    }

    public void setCameraCount(int cameraCount) {
        this.cameraCount = cameraCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AdminCamera> getCameras() {
        return cameras;
    }

    public void setCameras(List<AdminCamera> cameras) {
        this.cameras = cameras == null ? new ArrayList<AdminCamera>() : cameras;
    }
}
