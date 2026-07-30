package com.qizhifu.jiaoxuepeiyu.student.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTraining;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingRecord;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingAppInstallation;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoom;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoomMember;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoomRole;
import com.qizhifu.jiaoxuepeiyu.student.training.port.StudentTrainingRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StudentTrainingServiceTests {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-30T08:00:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Test
    void listsVisibleTrainingsWithStatusAndActiveRoom() {
        StudentTrainingService service = new StudentTrainingService(new FakeTrainings(), CLOCK);

        List<StudentTraining> trainings = service.listTrainings(7L, " TEAM ", " dispatch ");

        assertEquals(1, trainings.size());
        assertEquals("RUNNING", trainings.get(0).getStatus());
        assertEquals(true, trainings.get(0).isAppInstalled());
        assertEquals(99L, trainings.get(0).getActiveRoomId().longValue());
    }

    @Test
    void rejectsCreatingRoomWhenStudentAlreadyInActiveRoom() {
        FakeTrainings repository = new FakeTrainings();
        repository.activeRoomId = 99L;
        StudentTrainingService service = new StudentTrainingService(repository, CLOCK);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createRoom(7L, 21L);
        });

        assertEquals("Student already has an active room", exception.getMessage());
    }

    @Test
    void createsTeamRoomAndAddsOwnerMember() {
        FakeTrainings repository = new FakeTrainings();
        repository.activeRoomId = null;
        StudentTrainingService service = new StudentTrainingService(repository, CLOCK);

        TrainingRoom room = service.createRoom(7L, 21L);

        assertEquals(101L, room.getRoomId().longValue());
        assertEquals(7L, repository.addedMemberStudentId.longValue());
    }

    @Test
    void rejectsJoiningFullRoom() {
        FakeTrainings repository = new FakeTrainings();
        repository.activeRoomId = null;
        repository.room.getMembers().add(member(8L, null));
        repository.room.getMembers().add(member(9L, null));
        StudentTrainingService service = new StudentTrainingService(repository, CLOCK);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.joinRoom(7L, 101L);
        });

        assertEquals("Training room is full", exception.getMessage());
    }

    @Test
    void rejectsClaimingRoleAlreadyClaimedByAnotherMember() {
        FakeTrainings repository = new FakeTrainings();
        repository.room.getRoles().get(0).setClaimed(true);
        repository.room.getRoles().get(0).setClaimedByStudentId(8L);
        StudentTrainingService service = new StudentTrainingService(repository, CLOCK);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.claimRole(7L, 101L, 501L);
        });

        assertEquals("Training role has been claimed", exception.getMessage());
    }

    @Test
    void startsRoomWhenOwnerHasFullTeamAndRoles() {
        FakeTrainings repository = new FakeTrainings();
        repository.room.getMembers().clear();
        repository.room.getMembers().add(member(7L, 501L));
        repository.room.getMembers().add(member(8L, 502L));
        repository.room.getRoles().get(0).setClaimed(true);
        repository.room.getRoles().get(0).setClaimedByStudentId(7L);
        repository.room.getRoles().get(1).setClaimed(true);
        repository.room.getRoles().get(1).setClaimedByStudentId(8L);
        StudentTrainingService service = new StudentTrainingService(repository, CLOCK);

        TrainingRoom room = service.startRoom(7L, 101L);

        assertEquals("STARTED", room.getRoomStatus());
        assertEquals(101L, repository.startedRoomId.longValue());
    }

    private static class FakeTrainings implements StudentTrainingRepository {
        private Long activeRoomId = 99L;
        private Long addedMemberStudentId;
        private Long startedRoomId;
        private TrainingRoom room = room();

        @Override
        public List<StudentTrainingRecord> findTrainings(Long studentId, String mode, String keyword) {
            return Arrays.asList(training());
        }

        @Override
        public Optional<StudentTrainingRecord> findTraining(Long studentId, Long trainingId) {
            return Optional.of(training());
        }

        @Override
        public TrainingAppInstallation findAppInstallation(Long studentId) {
            TrainingAppInstallation installation = new TrainingAppInstallation();
            installation.setInstalled(true);
            installation.setVersion("1.0.0");
            return installation;
        }

        @Override
        public Optional<Long> findActiveRoomId(Long studentId) {
            return Optional.ofNullable(activeRoomId);
        }

        @Override
        public TrainingRoom createRoom(Long studentId, Long trainingId) {
            return room;
        }

        @Override
        public void addMember(Long roomId, Long studentId) {
            addedMemberStudentId = studentId;
            room.getMembers().add(member(studentId, null));
        }

        @Override
        public Optional<TrainingRoom> findRoom(Long roomId) {
            return Optional.of(room);
        }

        @Override
        public void claimRole(Long roomId, Long studentId, Long roleId) {
            room.getMembers().get(0).setRoleId(roleId);
            room.getRoles().get(0).setClaimed(true);
            room.getRoles().get(0).setClaimedByStudentId(studentId);
        }

        @Override
        public void leaveRoom(Long roomId, Long studentId) {
        }

        @Override
        public void dissolveRoom(Long roomId) {
            room.setRoomStatus("DISSOLVED");
        }

        @Override
        public void startRoom(Long roomId) {
            startedRoomId = roomId;
            room.setRoomStatus("STARTED");
        }

        private StudentTrainingRecord training() {
            StudentTrainingRecord record = new StudentTrainingRecord();
            record.setTrainingId(21L);
            record.setTrainingName("dispatch practice");
            record.setTrainingMode("TEAM");
            record.setOpenStartTime(LocalDateTime.parse("2026-07-01T00:00:00"));
            record.setOpenEndTime(LocalDateTime.parse("2026-08-31T23:59:59"));
            record.setTeamSize(2);
            record.setRoleCount(2);
            record.setAppRequired(true);
            record.setAppInstalled(true);
            return record;
        }

        private TrainingRoom room() {
            TrainingRoom room = new TrainingRoom();
            room.setRoomId(101L);
            room.setTrainingId(21L);
            room.setTrainingName("dispatch practice");
            room.setRoomCode("R101");
            room.setRoomStatus("WAITING");
            room.setOwnerStudentId(7L);
            room.setTeamSize(2);
            room.setMembers(new ArrayList<TrainingRoomMember>(Collections.singletonList(member(7L, null))));
            room.setRoles(new ArrayList<TrainingRoomRole>(Arrays.asList(role(501L, "Dispatcher"), role(502L, "Operator"))));
            return room;
        }
    }

    private static TrainingRoomMember member(Long studentId, Long roleId) {
        TrainingRoomMember member = new TrainingRoomMember();
        member.setStudentId(studentId);
        member.setStudentName("Student " + studentId);
        member.setRoleId(roleId);
        member.setOwner(7L.equals(studentId));
        return member;
    }

    private static TrainingRoomRole role(Long roleId, String roleName) {
        TrainingRoomRole role = new TrainingRoomRole();
        role.setRoleId(roleId);
        role.setRoleName(roleName);
        return role;
    }
}
