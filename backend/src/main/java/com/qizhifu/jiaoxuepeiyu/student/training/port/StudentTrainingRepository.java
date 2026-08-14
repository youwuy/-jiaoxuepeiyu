package com.qizhifu.jiaoxuepeiyu.student.training.port;

import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingRecord;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingAppInstallation;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoom;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingTopic;
import java.util.List;
import java.util.Optional;

public interface StudentTrainingRepository {

    List<StudentTrainingRecord> findTrainings(Long studentId, String mode, String keyword);

    Optional<StudentTrainingRecord> findTraining(Long studentId, Long trainingId);

    Optional<Long> findLatestAttemptId(Long studentId, Long trainingId);

    TrainingAppInstallation findAppInstallation(Long studentId);

    Optional<Long> findActiveRoomId(Long studentId);

    default boolean isExamStarted(Long trainingId) { return false; }

    default List<StudentTrainingTopic> findTopics(Long trainingId) { return java.util.Collections.emptyList(); }

    default List<TrainingRoom> findWaitingRooms(Long trainingId, Long topicId) { return java.util.Collections.emptyList(); }

    TrainingRoom createRoom(Long studentId, Long trainingId, Long topicId);

    void addMember(Long roomId, Long studentId);

    Optional<TrainingRoom> findRoom(Long roomId);

    void claimRole(Long roomId, Long studentId, Long roleId);

    void releaseRole(Long roomId, Long studentId, Long roleId);

    void leaveRoom(Long roomId, Long studentId);

    void dissolveRoom(Long roomId);

    void startRoom(Long roomId);
}
