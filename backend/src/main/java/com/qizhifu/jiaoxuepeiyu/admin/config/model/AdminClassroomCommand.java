package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import java.util.ArrayList;
import java.util.List;

public class AdminClassroomCommand {

    private String roomName;
    private Integer fixedDeviceCount;
    private List<AdminCameraCommand> cameras = new ArrayList<AdminCameraCommand>();

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getFixedDeviceCount() {
        return fixedDeviceCount;
    }

    public void setFixedDeviceCount(Integer fixedDeviceCount) {
        this.fixedDeviceCount = fixedDeviceCount;
    }

    public List<AdminCameraCommand> getCameras() {
        return cameras;
    }

    public void setCameras(List<AdminCameraCommand> cameras) {
        this.cameras = cameras == null ? new ArrayList<AdminCameraCommand>() : cameras;
    }
}
