package com.qizhifu.jiaoxuepeiyu.student.message.repository;

import com.qizhifu.jiaoxuepeiyu.student.message.model.StudentMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentMessageMapper {

    @Select("SELECT un.id, n.message_type, n.title, n.content, "
            + "CASE WHEN un.read_flag = 1 THEN TRUE ELSE FALSE END AS read, n.created_at "
            + "FROM msg_user_notification un JOIN msg_notification n ON n.id = un.notification_id "
            + "WHERE un.user_id = #{studentId} "
            + "ORDER BY n.created_at DESC, un.id DESC")
    List<StudentMessage> findMessages(@Param("studentId") Long studentId);

    @Update("UPDATE msg_user_notification SET read_flag = 1, read_at = NOW() "
            + "WHERE user_id = #{studentId} AND id = #{messageId}")
    void markRead(@Param("studentId") Long studentId, @Param("messageId") Long messageId);

    @Update("UPDATE msg_user_notification SET read_flag = 1, read_at = NOW() "
            + "WHERE user_id = #{studentId} AND read_flag = 0")
    void markAllRead(@Param("studentId") Long studentId);
}
