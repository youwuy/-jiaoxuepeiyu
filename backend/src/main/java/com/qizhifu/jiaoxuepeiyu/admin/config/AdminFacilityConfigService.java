package com.qizhifu.jiaoxuepeiyu.admin.config;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminCameraCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroomCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminFacilityConfigRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminFacilityConfigService {

    private final AdminFacilityConfigRepository repository;

    public AdminFacilityConfigService(AdminFacilityConfigRepository repository) {
        this.repository = repository;
    }

    public List<AdminClassroom> listClassrooms() {
        return repository.findClassrooms();
    }

    public AdminClassroom getClassroom(Long classroomId) {
        AdminClassroom classroom = repository.findClassroom(classroomId);
        if (classroom == null) {
            throw new BusinessException(404, "Classroom not found");
        }
        return classroom;
    }

    @Transactional
    public Long createClassroom(AdminClassroomCommand command) {
        return repository.createClassroom(normalized(command));
    }

    @Transactional
    public void updateClassroom(Long classroomId, AdminClassroomCommand command) {
        repository.updateClassroom(classroomId, normalized(command));
    }

    @Transactional
    public void deleteClassroom(Long classroomId) {
        getClassroom(classroomId);
        if (repository.isClassroomReferenced(classroomId)) {
            throw new BusinessException(400, "Classroom is referenced by training or device records");
        }
        repository.deleteClassroom(classroomId);
    }

    private AdminClassroomCommand normalized(AdminClassroomCommand command) {
        if (command == null || !InputValidator.hasText(command.getRoomName())) {
            throw new BusinessException(400, "Classroom name is required");
        }
        if (command.getFixedDeviceCount() == null || command.getFixedDeviceCount().intValue() <= 0) {
            throw new BusinessException(400, "Classroom fixed device count must be greater than zero");
        }
        if (command.getCameras() == null || command.getCameras().isEmpty()) {
            throw new BusinessException(400, "At least one camera is required");
        }
        Set<String> nvrPairs = new HashSet<String>();
        List<AdminCameraCommand> cameras = new ArrayList<AdminCameraCommand>();
        for (AdminCameraCommand camera : command.getCameras()) {
            AdminCameraCommand normalizedCamera = normalizedCamera(camera);
            String key = normalizedCamera.getNvrHost() + "|" + normalizedCamera.getNvrChannel();
            if (!nvrPairs.add(key)) {
                throw new BusinessException(400, "NVR host and channel cannot repeat in one classroom");
            }
            cameras.add(normalizedCamera);
        }
        AdminClassroomCommand normalized = new AdminClassroomCommand();
        normalized.setRoomName(command.getRoomName().trim());
        normalized.setFixedDeviceCount(command.getFixedDeviceCount());
        normalized.setCameras(cameras);
        return normalized;
    }

    private AdminCameraCommand normalizedCamera(AdminCameraCommand camera) {
        if (camera == null) {
            throw new BusinessException(400, "Camera information is required");
        }
        if (!isIpv4(camera.getNvrHost())) {
            throw new BusinessException(400, "NVR host IP is invalid");
        }
        if (camera.getNvrPort() < 1 || camera.getNvrPort() > 65535) {
            throw new BusinessException(400, "NVR port is invalid");
        }
        if (!InputValidator.hasText(camera.getAdminUsername())) {
            throw new BusinessException(400, "NVR admin username is required");
        }
        if (!InputValidator.hasText(camera.getAdminPassword())) {
            throw new BusinessException(400, "NVR admin password is required");
        }
        if (!InputValidator.hasText(camera.getNvrChannel())) {
            throw new BusinessException(400, "NVR channel is required");
        }
        AdminCameraCommand normalized = new AdminCameraCommand();
        normalized.setNvrHost(camera.getNvrHost().trim());
        normalized.setNvrPort(camera.getNvrPort());
        normalized.setAdminUsername(camera.getAdminUsername().trim());
        normalized.setAdminPassword(camera.getAdminPassword());
        normalized.setNvrChannel(camera.getNvrChannel().trim());
        normalized.setStreamUrl(trimToNull(camera.getStreamUrl()));
        return normalized;
    }

    private boolean isIpv4(String value) {
        if (!InputValidator.hasText(value)) {
            return false;
        }
        String[] parts = value.trim().split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int number = Integer.parseInt(part);
                if (number < 0 || number > 255) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }
}
