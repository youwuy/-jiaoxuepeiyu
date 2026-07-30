package com.qizhifu.jiaoxuepeiyu.student.training.repository;

import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTrainingRecord;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingAppInstallation;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoom;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoomMember;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoomRole;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentTrainingMapper {

    @Select("<script>"
            + "SELECT t.id AS training_id, t.training_name, t.training_mode, "
            + "t.open_start_time, t.open_end_time, t.team_size, "
            + "(SELECT COUNT(*) FROM training_role tr WHERE tr.training_id = t.id) AS role_count, "
            + "t.app_required, CASE WHEN ai.installed = 1 THEN TRUE ELSE FALSE END AS app_installed "
            + "FROM training_course t "
            + "JOIN training_participant p ON p.training_id = t.id "
            + "LEFT JOIN training_app_installation ai ON ai.student_id = #{studentId} "
            + "WHERE p.student_id = #{studentId} AND t.publish_status = 'PUBLISHED' "
            + "<if test='mode != null'>AND t.training_mode = #{mode}</if> "
            + "<if test='keyword != null'>AND t.training_name LIKE #{keyword}</if> "
            + "ORDER BY t.open_start_time DESC, t.id DESC "
            + "</script>")
    List<StudentTrainingRecord> findTrainings(@Param("studentId") Long studentId,
                                              @Param("mode") String mode,
                                              @Param("keyword") String keyword);

    @Select("SELECT t.id AS training_id, t.training_name, t.training_mode, "
            + "t.open_start_time, t.open_end_time, t.team_size, "
            + "(SELECT COUNT(*) FROM training_role tr WHERE tr.training_id = t.id) AS role_count, "
            + "t.app_required, CASE WHEN ai.installed = 1 THEN TRUE ELSE FALSE END AS app_installed "
            + "FROM training_course t "
            + "JOIN training_participant p ON p.training_id = t.id "
            + "LEFT JOIN training_app_installation ai ON ai.student_id = #{studentId} "
            + "WHERE p.student_id = #{studentId} AND t.id = #{trainingId} "
            + "AND t.publish_status = 'PUBLISHED' LIMIT 1")
    StudentTrainingRecord findTraining(@Param("studentId") Long studentId,
                                       @Param("trainingId") Long trainingId);

    @Select("SELECT CASE WHEN installed = 1 THEN TRUE ELSE FALSE END AS installed, "
            + "app_version AS version, download_url, install_message AS message "
            + "FROM training_app_installation WHERE student_id = #{studentId} LIMIT 1")
    TrainingAppInstallation findAppInstallation(@Param("studentId") Long studentId);

    @Select("SELECT r.id FROM training_team_room r "
            + "JOIN training_team_room_member m ON m.room_id = r.id "
            + "WHERE m.student_id = #{studentId} AND m.member_status = 'ACTIVE' "
            + "AND r.room_status IN ('WAITING', 'STARTED') "
            + "ORDER BY r.created_at DESC, r.id DESC LIMIT 1")
    Long findActiveRoomId(@Param("studentId") Long studentId);

    @Insert("INSERT INTO training_team_room "
            + "(training_id, owner_student_id, room_code, room_status, created_at, updated_at) "
            + "VALUES (#{trainingId}, #{ownerStudentId}, #{roomCode}, 'WAITING', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "roomId")
    void insertRoom(TrainingRoom room);

    @Insert("INSERT INTO training_team_room_role (room_id, role_id, role_name, sort_order, created_at) "
            + "SELECT #{roomId}, id, role_name, sort_order, NOW() "
            + "FROM training_role WHERE training_id = #{trainingId} ORDER BY sort_order ASC, id ASC")
    void insertRoomRoles(@Param("roomId") Long roomId, @Param("trainingId") Long trainingId);

    @Insert("INSERT INTO training_team_room_member "
            + "(room_id, student_id, member_status, joined_at, created_at, updated_at) "
            + "VALUES (#{roomId}, #{studentId}, 'ACTIVE', NOW(), NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE member_status = 'ACTIVE', role_id = NULL, left_at = NULL, updated_at = NOW()")
    void addMember(@Param("roomId") Long roomId, @Param("studentId") Long studentId);

    @Select("SELECT r.id AS room_id, r.training_id, t.training_name, r.room_code, r.room_status, "
            + "r.owner_student_id, t.team_size "
            + "FROM training_team_room r JOIN training_course t ON t.id = r.training_id "
            + "WHERE r.id = #{roomId} LIMIT 1 FOR UPDATE")
    TrainingRoom findRoom(@Param("roomId") Long roomId);

    @Select("SELECT m.student_id, u.real_name AS student_name, m.role_id, rr.role_name, "
            + "CASE WHEN m.student_id = r.owner_student_id THEN TRUE ELSE FALSE END AS owner "
            + "FROM training_team_room_member m "
            + "JOIN training_team_room r ON r.id = m.room_id "
            + "JOIN sys_user u ON u.id = m.student_id "
            + "LEFT JOIN training_team_room_role rr ON rr.room_id = m.room_id AND rr.role_id = m.role_id "
            + "WHERE m.room_id = #{roomId} AND m.member_status = 'ACTIVE' "
            + "ORDER BY m.joined_at ASC, m.id ASC")
    List<TrainingRoomMember> findMembers(@Param("roomId") Long roomId);

    @Select("SELECT rr.role_id, rr.role_name, "
            + "CASE WHEN m.student_id IS NULL THEN FALSE ELSE TRUE END AS claimed, "
            + "m.student_id AS claimed_by_student_id "
            + "FROM training_team_room_role rr "
            + "LEFT JOIN training_team_room_member m "
            + "ON m.room_id = rr.room_id AND m.role_id = rr.role_id AND m.member_status = 'ACTIVE' "
            + "WHERE rr.room_id = #{roomId} ORDER BY rr.sort_order ASC, rr.id ASC")
    List<TrainingRoomRole> findRoles(@Param("roomId") Long roomId);

    @Update("UPDATE training_team_room_member SET role_id = #{roleId}, updated_at = NOW() "
            + "WHERE room_id = #{roomId} AND student_id = #{studentId} AND member_status = 'ACTIVE'")
    void claimRole(@Param("roomId") Long roomId,
                   @Param("studentId") Long studentId,
                   @Param("roleId") Long roleId);

    @Update("UPDATE training_team_room_member SET member_status = 'LEFT', role_id = NULL, "
            + "left_at = NOW(), updated_at = NOW() "
            + "WHERE room_id = #{roomId} AND student_id = #{studentId} AND member_status = 'ACTIVE'")
    void leaveRoom(@Param("roomId") Long roomId, @Param("studentId") Long studentId);

    @Update("UPDATE training_team_room SET room_status = 'DISSOLVED', updated_at = NOW() "
            + "WHERE id = #{roomId} AND room_status = 'WAITING'")
    void dissolveRoom(@Param("roomId") Long roomId);

    @Update("UPDATE training_team_room SET room_status = 'STARTED', started_at = NOW(), updated_at = NOW() "
            + "WHERE id = #{roomId} AND room_status = 'WAITING'")
    void startRoom(@Param("roomId") Long roomId);
}
