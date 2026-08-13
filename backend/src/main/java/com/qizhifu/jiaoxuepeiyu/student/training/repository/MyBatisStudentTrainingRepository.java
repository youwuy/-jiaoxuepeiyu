package com.qizhifu.jiaoxuepeiyu.student.training.repository;

import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingRecord;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingAppInstallation;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoom;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingTopic;
import com.qizhifu.jiaoxuepeiyu.student.training.port.StudentTrainingRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentTrainingRepository implements StudentTrainingRepository {

    private final StudentTrainingMapper mapper;

    public MyBatisStudentTrainingRepository(StudentTrainingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<StudentTrainingRecord> findTrainings(Long studentId, String mode, String keyword) {
        String keywordLike = keyword == null ? null : "%" + keyword + "%";
        return mapper.findTrainings(studentId, mode, keywordLike);
    }

    @Override
    public Optional<StudentTrainingRecord> findTraining(Long studentId, Long trainingId) {
        return Optional.ofNullable(mapper.findTraining(studentId, trainingId));
    }

    @Override
    public Optional<Long> findLatestAttemptId(Long studentId, Long trainingId) {
        return Optional.ofNullable(mapper.findLatestAttemptId(studentId, trainingId));
    }

    @Override
    public TrainingAppInstallation findAppInstallation(Long studentId) {
        TrainingAppInstallation installation = mapper.findAppInstallation(studentId);
        if (installation == null) {
            installation = new TrainingAppInstallation();
            installation.setInstalled(false);
            installation.setMessage("Training application is not installed");
        }
        return installation;
    }

    @Override
    public Optional<Long> findActiveRoomId(Long studentId) {
        return Optional.ofNullable(mapper.findActiveRoomId(studentId));
    }

    @Override
    public List<StudentTrainingTopic> findTopics(Long trainingId) { return mapper.findTopics(trainingId); }

    @Override
    public List<TrainingRoom> findWaitingRooms(Long trainingId) {
        List<TrainingRoom> rooms = mapper.findWaitingRooms(trainingId);
        for (TrainingRoom room : rooms) {
            room.setMembers(mapper.findMembers(room.getRoomId()));
            room.setRoles(mapper.findRoles(room.getRoomId()));
        }
        return rooms;
    }

    @Override
    public TrainingRoom createRoom(Long studentId, Long trainingId) {
        TrainingRoom room = new TrainingRoom();
        room.setTrainingId(trainingId);
        room.setOwnerStudentId(studentId);
        room.setRoomCode("R" + trainingId + studentId + System.currentTimeMillis());
        mapper.insertRoom(room);
        mapper.insertRoomRoles(room.getRoomId(), trainingId);
        return findRoom(room.getRoomId()).orElse(room);
    }

    @Override
    public void addMember(Long roomId, Long studentId) {
        mapper.addMember(roomId, studentId);
    }

    @Override
    public Optional<TrainingRoom> findRoom(Long roomId) {
        TrainingRoom room = mapper.findRoom(roomId);
        if (room == null) {
            return Optional.empty();
        }
        room.setMembers(mapper.findMembers(roomId));
        room.setRoles(mapper.findRoles(roomId));
        return Optional.of(room);
    }

    @Override
    public void claimRole(Long roomId, Long studentId, Long roleId) {
        mapper.claimRole(roomId, studentId, roleId);
    }

    @Override
    public void releaseRole(Long roomId, Long studentId, Long roleId) {
        mapper.releaseRole(roomId, studentId, roleId);
    }

    @Override
    public void leaveRoom(Long roomId, Long studentId) {
        mapper.leaveRoom(roomId, studentId);
    }

    @Override
    public void dissolveRoom(Long roomId) {
        mapper.dissolveRoom(roomId);
    }

    @Override
    public void startRoom(Long roomId) {
        mapper.startRoom(roomId);
    }
}
