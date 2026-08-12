package com.qizhifu.jiaoxuepeiyu.admin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminCameraCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroomCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminFacilityConfigRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminFacilityConfigServiceTests {

    @Test
    void createsClassroomWithCameras() {
        FakeFacilities repository = new FakeFacilities();
        AdminFacilityConfigService service = new AdminFacilityConfigService(repository);

        Long classroomId = service.createClassroom(command(camera("10.0.0.1", 554, "CH01")));

        assertEquals(20L, classroomId.longValue());
        assertEquals("Room A", repository.createdCommand.getRoomName());
        assertEquals(1, repository.createdCommand.getCameras().size());
    }

    @Test
    void rejectsClassroomWithoutCamera() {
        AdminFacilityConfigService service = new AdminFacilityConfigService(new FakeFacilities());
        AdminClassroomCommand command = command();

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createClassroom(command));

        assertEquals("At least one camera is required", exception.getMessage());
    }

    @Test
    void rejectsInvalidNvrPort() {
        AdminFacilityConfigService service = new AdminFacilityConfigService(new FakeFacilities());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createClassroom(command(camera("10.0.0.1", 70000, "CH01")));
        });

        assertEquals("NVR port is invalid", exception.getMessage());
    }

    @Test
    void rejectsDuplicateNvrChannelInsideClassroom() {
        AdminFacilityConfigService service = new AdminFacilityConfigService(new FakeFacilities());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createClassroom(command(
                    camera("10.0.0.1", 554, "CH01"),
                    camera("10.0.0.1", 8554, "CH01")));
        });

        assertEquals("NVR host and channel cannot repeat in one classroom", exception.getMessage());
    }

    private AdminClassroomCommand command(AdminCameraCommand... cameras) {
        AdminClassroomCommand command = new AdminClassroomCommand();
        command.setRoomName(" Room A ");
        command.setCameras(Arrays.asList(cameras));
        return command;
    }

    private AdminCameraCommand camera(String host, int port, String channel) {
        AdminCameraCommand camera = new AdminCameraCommand();
        camera.setNvrHost(host);
        camera.setNvrPort(port);
        camera.setAdminUsername("admin");
        camera.setAdminPassword("secret");
        camera.setNvrChannel(channel);
        camera.setStreamUrl("rtsp://example");
        return camera;
    }

    private static class FakeFacilities implements AdminFacilityConfigRepository {
        private AdminClassroomCommand createdCommand;

        @Override
        public List<com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom> findClassrooms() {
            return new ArrayList<com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom>();
        }

        @Override
        public com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom findClassroom(Long classroomId) {
            return null;
        }

        @Override
        public Long createClassroom(AdminClassroomCommand command) {
            this.createdCommand = command;
            return 20L;
        }

        @Override
        public void updateClassroom(Long classroomId, AdminClassroomCommand command) {
        }

        @Override
        public void deleteClassroom(Long classroomId) {
        }

        @Override
        public boolean isClassroomReferenced(Long classroomId) {
            return false;
        }
    }
}
