package com.qizhifu.jiaoxuepeiyu.admin.training.repository;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCameraState;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingLog;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingMonitorSnapshot;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingQuery;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingRole;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingWeakStep;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingWeakTopic;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStudentState;
import com.qizhifu.jiaoxuepeiyu.admin.training.port.AdminTrainingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminTrainingRepository implements AdminTrainingRepository {

    private final AdminTrainingMapper mapper;

    public MyBatisAdminTrainingRepository(AdminTrainingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminTraining> findTrainings(AdminTrainingQuery query) {
        return mapper.findTrainings(likeQuery(query));
    }

    @Override
    public long countTrainings(AdminTrainingQuery query) {
        return mapper.countTrainings(likeQuery(query));
    }

    @Override
    public AdminTraining findTraining(Long trainingId) {
        return mapper.findTraining(trainingId);
    }

    @Override
    public boolean roleBelongsToTopic(Long topicId, String roleName) {
        return mapper.countTopicRole(topicId, roleName) > 0;
    }

    @Override
    public Long createTraining(AdminTrainingCommand command, Long creatorId) {
        AdminTraining training = toTraining(null, command, creatorId);
        mapper.insertTraining(training);
        replaceBindings(training.getTrainingId(), command);
        return training.getTrainingId();
    }

    @Override
    public void updateTraining(Long trainingId, AdminTrainingCommand command) {
        AdminTraining existing = mapper.findTraining(trainingId);
        Long creatorId = existing == null ? null : existing.getCreatedBy();
        mapper.updateTraining(toTraining(trainingId, command, creatorId));
        replaceBindings(trainingId, command);
    }

    @Override
    public int countEnabledStudentsByTrainingClasses(Long trainingId) {
        return mapper.countEnabledStudentsByTrainingClasses(trainingId);
    }

    @Override
    public void syncParticipants(Long trainingId) {
        mapper.deleteParticipants(trainingId);
        mapper.insertParticipantsFromClasses(trainingId);
        mapper.insertParticipantsFromStudents(trainingId);
    }

    @Override
    public void updatePublishStatus(Long trainingId, String publishStatus) {
        mapper.updatePublishStatus(trainingId, publishStatus);
    }

    @Override
    public void markExamStarted(Long trainingId) {
        mapper.markExamStarted(trainingId);
        mapper.startWaitingExamRooms(trainingId);
    }

    @Override
    public void deleteTraining(Long trainingId) {
        mapper.deleteTraining(trainingId);
    }

    @Override
    public void notifyParticipants(Long trainingId, String title, String content) {
        AdminTrainingNotification notification = new AdminTrainingNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSourceId(trainingId);
        mapper.insertNotification(notification);
        mapper.notifyParticipants(trainingId, notification.getNotificationId());
    }

    @Override
    public AdminTrainingStatistics calculateStatistics(Long trainingId) {
        return mapper.calculateStatistics(trainingId);
    }

    @Override
    public List<AdminTrainingWeakStep> findWeakSteps(Long trainingId, String className) {
        return mapper.findWeakSteps(trainingId, className);
    }

    @Override
    public List<AdminTrainingWeakTopic> findWeakTopics(Long trainingId, String className) {
        return mapper.findWeakTopics(trainingId, className);
    }

    @Override
    public AdminTrainingMonitorSnapshot getMonitorSnapshot(Long trainingId) {
        AdminTrainingMonitorSnapshot snapshot = new AdminTrainingMonitorSnapshot();
        snapshot.setTrainingId(trainingId);
        snapshot.setGeneratedAt(LocalDateTime.now());
        List<AdminTrainingCameraState> cameras = mapper.findMonitorCameras(trainingId);
        List<AdminTrainingStudentState> students = mapper.findMonitorStudents(trainingId);
        snapshot.setCameras(cameras);
        snapshot.setStudents(students);
        snapshot.setStatistics(calculateStatistics(trainingId));
        return snapshot;
    }

    @Override
    public boolean dissolveRoom(Long trainingId, Long roomId) {
        int changed = mapper.dissolveRoom(trainingId, roomId);
        if (changed > 0) {
            mapper.removeActiveRoomMembers(roomId);
            mapper.clearRoomMonitorState(trainingId, roomId);
        }
        return changed > 0;
    }

    @Override
    public List<Map<String, Object>> findReviewRows(Long trainingId) {
        return mapper.findReviewRows(trainingId);
    }

    @Override
    public List<Map<String, Object>> findReviewAttempts(Long trainingId, Long studentId, Long topicId) {
        return mapper.findReviewAttempts(trainingId, studentId, topicId);
    }

    @Override
    public Double findAttemptMaxScore(Long trainingId, Long attemptId) {
        return mapper.findAttemptMaxScore(trainingId, attemptId);
    }

    @Override
    public boolean reviewAttempt(Long trainingId, Long attemptId, Double manualScore, String comment, Long reviewerId) {
        return mapper.reviewAttempt(trainingId, attemptId, manualScore, comment, reviewerId) > 0;
    }

    @Override
    public void appendTrainingLog(Long trainingId, Long operatorId, String action, String content) {
        mapper.insertTrainingLog(trainingId, operatorId, action, content);
    }

    @Override
    public List<AdminTrainingLog> findTrainingLogs(Long trainingId) {
        return mapper.findTrainingLogs(trainingId);
    }

    private void replaceBindings(Long trainingId, AdminTrainingCommand command) {
        mapper.deleteClasses(trainingId);
        int classSort = 1;
        for (Long classId : command.getClassIds()) {
            mapper.insertClass(trainingId, classId, classSort++);
        }
        mapper.deleteStudents(trainingId);
        int studentSort = 1;
        for (Long studentId : command.getStudentIds()) {
            mapper.insertStudent(trainingId, studentId, studentSort++);
        }
        mapper.deleteRoles(trainingId);
        for (AdminTrainingRoleCommand roleCommand : command.getRoles()) {
            mapper.insertRole(trainingId, toRole(roleCommand));
        }
        mapper.deleteTeachers(trainingId);
        int teacherSort = 1;
        for (Long teacherId : command.getTeacherIds()) {
            mapper.insertTeacher(trainingId, teacherId, teacherSort++);
        }
        mapper.deleteTopics(trainingId);
        int topicSort = 1;
        for (Long topicId : command.getTopicIds()) {
            mapper.insertTopic(trainingId, topicId, topicSort++);
        }
    }

    private AdminTraining toTraining(Long trainingId, AdminTrainingCommand command, Long creatorId) {
        AdminTraining training = new AdminTraining();
        training.setTrainingId(trainingId);
        training.setTrainingName(command.getTrainingName());
        training.setAcademicYearId(command.getAcademicYearId());
        training.setSemesterId(command.getSemesterId());
        training.setMajorId(command.getMajorId());
        training.setCoverUrl(command.getCoverUrl());
        training.setTrainingType(command.getTrainingType());
        training.setTrainingMode(command.getTrainingMode());
        training.setPaperMode(command.getPaperMode());
        training.setPaperId(command.getPaperId());
        training.setPublishStatus(command.getPublishStatus());
        training.setOpenStartTime(command.getOpenStartTime());
        training.setOpenEndTime(command.getOpenEndTime());
        training.setTeamSize(command.getTeamSize());
        training.setAppRequired(command.getAppRequired());
        training.setClassroomId(command.getClassroomId());
        training.setTeacherIds(command.getTeacherIds());
        training.setScoreBasis(command.getScoreBasis());
        training.setTopicIds(command.getTopicIds());
        String classNames = command.getClassIds().isEmpty() ? null : mapper.findClassNamesByIds(command.getClassIds());
        String studentNames = command.getStudentIds().isEmpty() ? null : mapper.findStudentNamesByIds(command.getStudentIds());
        training.setClassNames(joinTargetNames(classNames, studentNames));
        training.setCreatedBy(creatorId);
        return training;
    }

    private String joinTargetNames(String classNames, String studentNames) {
        if (classNames == null || classNames.trim().isEmpty()) return studentNames;
        if (studentNames == null || studentNames.trim().isEmpty()) return classNames;
        return classNames + ", " + studentNames;
    }

    private AdminTrainingRole toRole(AdminTrainingRoleCommand command) {
        AdminTrainingRole role = new AdminTrainingRole();
        role.setTopicId(command.getTopicId());
        role.setRoleName(command.getRoleName());
        role.setAiFillEnabled(command.getAiFillEnabled());
        role.setSortOrder(command.getSortOrder());
        return role;
    }

    private AdminTrainingQuery likeQuery(AdminTrainingQuery source) {
        AdminTrainingQuery query = new AdminTrainingQuery();
        query.setKeyword(like(source.getKeyword()));
        query.setAcademicYearId(source.getAcademicYearId());
        query.setSemesterId(source.getSemesterId());
        query.setMajorId(source.getMajorId());
        query.setClassId(source.getClassId());
        query.setTrainingType(source.getTrainingType());
        query.setTrainingMode(source.getTrainingMode());
        query.setPublishStatus(source.getPublishStatus());
        query.setRangeStart(source.getRangeStart());
        query.setRangeEnd(source.getRangeEnd());
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
