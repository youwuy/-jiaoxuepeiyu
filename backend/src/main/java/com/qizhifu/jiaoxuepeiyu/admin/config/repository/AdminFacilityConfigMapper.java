package com.qizhifu.jiaoxuepeiyu.admin.config.repository;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminCamera;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminFacilityConfigMapper {

    @Select("SELECT r.id AS classroom_id, r.room_name, "
            + "(SELECT COUNT(*) FROM room_camera c WHERE c.room_id = r.id) AS camera_count, "
            + "r.created_at FROM training_room r ORDER BY r.created_at DESC, r.id DESC")
    List<AdminClassroom> findClassrooms();

    @Select("SELECT id AS classroom_id, room_name, fixed_device_count AS camera_count, created_at "
            + "FROM training_room WHERE id = #{classroomId} LIMIT 1")
    AdminClassroom findClassroom(@Param("classroomId") Long classroomId);

    @Select("SELECT id AS camera_id, room_id AS classroom_id, nvr_host, nvr_port, "
            + "username AS admin_username, password AS admin_password, nvr_channel, "
            + "rtsp_url AS stream_url, sort_order "
            + "FROM room_camera WHERE room_id = #{classroomId} ORDER BY sort_order ASC, id ASC")
    List<AdminCamera> findCameras(@Param("classroomId") Long classroomId);

    @Insert("INSERT INTO training_room (room_name, fixed_device_count, created_at, updated_at) "
            + "VALUES (#{roomName}, #{cameraCount}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "classroomId")
    void insertClassroom(AdminClassroom classroom);

    @Update("UPDATE training_room SET room_name = #{roomName}, fixed_device_count = #{cameraCount}, "
            + "updated_at = NOW() WHERE id = #{classroomId}")
    void updateClassroom(AdminClassroom classroom);

    @Delete("DELETE FROM training_room WHERE id = #{classroomId}")
    void deleteClassroom(@Param("classroomId") Long classroomId);

    @Delete("DELETE FROM room_camera WHERE room_id = #{classroomId}")
    void deleteCameras(@Param("classroomId") Long classroomId);

    @Insert("<script>"
            + "INSERT INTO room_camera "
            + "(room_id, camera_name, nvr_host, nvr_port, username, password, nvr_channel, "
            + "rtsp_url, sort_order, status, created_at, updated_at) VALUES "
            + "<foreach collection='cameras' item='camera' separator=','>"
            + "(#{camera.classroomId}, #{camera.nvrChannel}, #{camera.nvrHost}, #{camera.nvrPort}, "
            + "#{camera.adminUsername}, #{camera.adminPassword}, #{camera.nvrChannel}, "
            + "#{camera.streamUrl}, #{camera.sortOrder}, 1, NOW(), NOW())"
            + "</foreach>"
            + "</script>")
    void insertCameras(@Param("cameras") List<AdminCamera> cameras);
}
