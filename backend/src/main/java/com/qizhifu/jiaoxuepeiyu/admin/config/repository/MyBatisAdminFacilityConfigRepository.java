package com.qizhifu.jiaoxuepeiyu.admin.config.repository;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminCamera;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminCameraCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroomCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminFacilityConfigRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminFacilityConfigRepository implements AdminFacilityConfigRepository {

    private final AdminFacilityConfigMapper mapper;

    public MyBatisAdminFacilityConfigRepository(AdminFacilityConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminClassroom> findClassrooms() {
        List<AdminClassroom> classrooms = mapper.findClassrooms();
        for (AdminClassroom classroom : classrooms) {
            classroom.setCameras(mapper.findCameras(classroom.getClassroomId()));
        }
        return classrooms;
    }

    @Override
    public AdminClassroom findClassroom(Long classroomId) {
        AdminClassroom classroom = mapper.findClassroom(classroomId);
        if (classroom != null) {
            classroom.setCameras(mapper.findCameras(classroomId));
        }
        return classroom;
    }

    @Override
    public Long createClassroom(AdminClassroomCommand command) {
        AdminClassroom classroom = toClassroom(null, command);
        mapper.insertClassroom(classroom);
        mapper.insertCameras(toCameras(classroom.getClassroomId(), command));
        return classroom.getClassroomId();
    }

    @Override
    public void updateClassroom(Long classroomId, AdminClassroomCommand command) {
        mapper.updateClassroom(toClassroom(classroomId, command));
        mapper.deleteCameras(classroomId);
        mapper.insertCameras(toCameras(classroomId, command));
    }

    @Override
    public void deleteClassroom(Long classroomId) {
        mapper.deleteCameras(classroomId);
        mapper.deleteClassroom(classroomId);
    }

    @Override
    public boolean isClassroomReferenced(Long classroomId) {
        return mapper.countClassroomReferences(classroomId) > 0;
    }

    private AdminClassroom toClassroom(Long classroomId, AdminClassroomCommand command) {
        AdminClassroom classroom = new AdminClassroom();
        classroom.setClassroomId(classroomId);
        classroom.setRoomName(command.getRoomName());
        classroom.setCameraCount(command.getCameras().size());
        return classroom;
    }

    private List<AdminCamera> toCameras(Long classroomId, AdminClassroomCommand command) {
        List<AdminCamera> cameras = new ArrayList<AdminCamera>();
        int sortOrder = 1;
        for (AdminCameraCommand source : command.getCameras()) {
            AdminCamera camera = new AdminCamera();
            camera.setClassroomId(classroomId);
            camera.setNvrHost(source.getNvrHost());
            camera.setNvrPort(source.getNvrPort());
            camera.setAdminUsername(source.getAdminUsername());
            camera.setAdminPassword(source.getAdminPassword());
            camera.setNvrChannel(source.getNvrChannel());
            camera.setStreamUrl(source.getStreamUrl());
            camera.setSortOrder(sortOrder++);
            cameras.add(camera);
        }
        return cameras;
    }
}
