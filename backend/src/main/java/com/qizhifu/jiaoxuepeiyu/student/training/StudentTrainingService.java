package com.qizhifu.jiaoxuepeiyu.student.training;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTraining;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingRecord;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingAppInstallation;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoom;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoomMember;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoomRole;
import com.qizhifu.jiaoxuepeiyu.student.training.port.StudentTrainingRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentTrainingService {

    private final StudentTrainingRepository repository;
    private final Clock clock;

    @Autowired
    public StudentTrainingService(StudentTrainingRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    StudentTrainingService(StudentTrainingRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<StudentTraining> listTrainings(Long studentId, String mode, String keyword) {
        Long activeRoomId = repository.findActiveRoomId(studentId).orElse(null);
        LocalDateTime now = LocalDateTime.now(clock);
        List<StudentTraining> trainings = new ArrayList<StudentTraining>();
        for (StudentTrainingRecord record : repository.findTrainings(studentId, normalize(mode), normalize(keyword))) {
            StudentTraining training = new StudentTraining();
            training.setTrainingId(record.getTrainingId());
            training.setTrainingName(record.getTrainingName());
            training.setTrainingType(record.getTrainingType());
            training.setTrainingMode(record.getTrainingMode());
            training.setOpenStartTime(record.getOpenStartTime());
            training.setOpenEndTime(record.getOpenEndTime());
            training.setTeamSize(record.getTeamSize());
            training.setRoleCount(record.getRoleCount());
            training.setAppRequired(record.isAppRequired());
            training.setAppInstalled(record.isAppInstalled());
            training.setActiveRoomId(activeRoomId);
            training.setLatestAttemptId(record.getLatestAttemptId());
            training.setTopics(repository.findTopics(record.getTrainingId()));
            training.setStatus(status(record, now));
            trainings.add(training);
        }
        return trainings;
    }

    public TrainingAppInstallation getAppInstallation(Long studentId) {
        return repository.findAppInstallation(studentId);
    }

    public List<TrainingRoom> listWaitingRooms(Long studentId, Long trainingId, Long topicId) {
        requireTraining(studentId, trainingId);
        requireTeamTopic(trainingId, topicId);
        return repository.findWaitingRooms(trainingId, topicId);
    }

    @Transactional
    public TrainingRoom createRoom(Long studentId, Long trainingId, Long topicId) {
        StudentTrainingRecord training = requireTraining(studentId, trainingId);
        assertExamNotStarted(trainingId);
        if (!"TEAM".equals(training.getTrainingMode())) {
            throw new BusinessException(400, "Only team training can create rooms");
        }
        requireTeamTopic(trainingId, topicId);
        assertNoActiveRoom(studentId);
        TrainingRoom room = repository.createRoom(studentId, trainingId, topicId);
        repository.addMember(room.getRoomId(), studentId);
        assignExamRole(training, room.getRoomId(), studentId);
        return requireRoom(room.getRoomId());
    }

    @Transactional
    public TrainingRoom joinRoom(Long studentId, Long roomId) {
        assertNoActiveRoom(studentId);
        TrainingRoom room = requireRoom(roomId);
        assertExamNotStarted(room.getTrainingId());
        StudentTrainingRecord training = requireTraining(studentId, room.getTrainingId());
        assertWaiting(room);
        if (room.getMembers().size() >= room.getTeamSize()) {
            throw new BusinessException(400, "Training room is full");
        }
        repository.addMember(roomId, studentId);
        assignExamRole(training, roomId, studentId);
        return requireRoom(roomId);
    }

    @Transactional
    public TrainingRoom leaveRoom(Long studentId, Long roomId) {
        TrainingRoom room = requireRoom(roomId);
        assertMember(room, studentId);
        repository.leaveRoom(roomId, studentId);
        if (studentId.equals(room.getOwnerStudentId()) && "WAITING".equals(room.getRoomStatus())) {
            repository.dissolveRoom(roomId);
        }
        return requireRoom(roomId);
    }

    @Transactional
    public TrainingRoom claimRole(Long studentId, Long roomId, Long roleId) {
        TrainingRoom room = requireRoom(roomId);
        assertWaiting(room);
        assertMember(room, studentId);
        TrainingRoomMember member = findMember(room, studentId);
        if (member.getRoleId() != null && !member.getRoleId().equals(roleId)) {
            throw new BusinessException(400, "Training role cannot be changed after selection");
        }
        TrainingRoomRole role = findRole(room, roleId);
        if (role.isAiFillEnabled()) {
            throw new BusinessException(400, "Training role is reserved for AI");
        }
        if (role.isClaimed() && !studentId.equals(role.getClaimedByStudentId())) {
            throw new BusinessException(400, "Training role has been claimed");
        }
        repository.claimRole(roomId, studentId, roleId);
        return requireRoom(roomId);
    }

    @Transactional
    public TrainingRoom releaseRole(Long studentId, Long roomId, Long roleId) {
        TrainingRoom room = requireRoom(roomId);
        assertWaiting(room);
        assertMember(room, studentId);
        TrainingRoomRole role = findRole(room, roleId);
        if (role.isClaimed() && !studentId.equals(role.getClaimedByStudentId())) {
            throw new BusinessException(400, "Training role is claimed by another member");
        }
        if (role.isClaimed()) {
            repository.releaseRole(roomId, studentId, roleId);
        }
        return requireRoom(roomId);
    }

    @Transactional
    public TrainingRoom startRoom(Long studentId, Long roomId) {
        TrainingRoom room = requireRoom(roomId);
        assertWaiting(room);
        StudentTrainingRecord training = requireTraining(studentId, room.getTrainingId());
        if ("EXAM".equals(training.getTrainingType())) {
            throw new BusinessException(403, "Training exams are started by the teacher");
        }
        if (!studentId.equals(room.getOwnerStudentId())) {
            throw new BusinessException(403, "Only room owner can start training");
        }
        room = requireRoom(roomId);
        repository.startRoom(roomId);
        return requireRoom(roomId);
    }

    public TrainingRoom getRoom(Long studentId, Long roomId) {
        TrainingRoom room = requireRoom(roomId);
        assertMember(room, studentId);
        return room;
    }

    private StudentTrainingRecord requireTraining(Long studentId, Long trainingId) {
        return repository.findTraining(studentId, trainingId)
                .orElseThrow(() -> new BusinessException(404, "Training not found"));
    }

    private TrainingRoom requireRoom(Long roomId) {
        return repository.findRoom(roomId)
                .orElseThrow(() -> new BusinessException(404, "Training room not found"));
    }

    private void requireTeamTopic(Long trainingId, Long topicId) {
        if (topicId == null || topicId.longValue() <= 0L) {
            throw new BusinessException(400, "Training topic is required");
        }
        for (com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingTopic topic : repository.findTopics(trainingId)) {
            if (topicId.equals(topic.getTopicId()) && "TEAM".equals(topic.getTrainingMode())) return;
        }
        throw new BusinessException(404, "Team training topic not found");
    }

    private void assignExamRole(StudentTrainingRecord training, Long roomId, Long studentId) {
        if (!"EXAM".equals(training.getTrainingType())) {
            return;
        }
        List<TrainingRoomRole> availableRoles = new ArrayList<TrainingRoomRole>();
        for (TrainingRoomRole role : requireRoom(roomId).getRoles()) {
            if (!role.isAiFillEnabled() && !role.isClaimed()) {
                availableRoles.add(role);
            }
        }
        if (!availableRoles.isEmpty()) {
            TrainingRoomRole assigned = availableRoles.get(ThreadLocalRandom.current().nextInt(availableRoles.size()));
            repository.claimRole(roomId, studentId, assigned.getRoleId());
        }
    }

    private void assertNoActiveRoom(Long studentId) {
        if (repository.findActiveRoomId(studentId).isPresent()) {
            throw new BusinessException(400, "Student already has an active room");
        }
    }

    private void assertExamNotStarted(Long trainingId) {
        if (repository.isExamStarted(trainingId)) {
            throw new BusinessException(400, "Training exam has already started");
        }
    }

    private void assertWaiting(TrainingRoom room) {
        if (!"WAITING".equals(room.getRoomStatus())) {
            throw new BusinessException(400, "Training room is not waiting");
        }
    }

    private void assertMember(TrainingRoom room, Long studentId) {
        findMember(room, studentId);
    }

    private TrainingRoomMember findMember(TrainingRoom room, Long studentId) {
        for (TrainingRoomMember member : room.getMembers()) {
            if (studentId.equals(member.getStudentId())) {
                return member;
            }
        }
        throw new BusinessException(403, "Student is not in this room");
    }

    private TrainingRoomRole findRole(TrainingRoom room, Long roleId) {
        for (TrainingRoomRole role : room.getRoles()) {
            if (roleId.equals(role.getRoleId())) {
                return role;
            }
        }
        throw new BusinessException(404, "Training role not found");
    }

    private String status(StudentTrainingRecord record, LocalDateTime now) {
        if (record.getOpenStartTime() != null && now.isBefore(record.getOpenStartTime())) {
            return "NOT_STARTED";
        }
        if (record.getOpenEndTime() != null && now.isAfter(record.getOpenEndTime())) {
            return "FINISHED";
        }
        return "RUNNING";
    }

    private String normalize(String value) {
        return value == null || value.trim().length() == 0 ? null : value.trim();
    }
}
