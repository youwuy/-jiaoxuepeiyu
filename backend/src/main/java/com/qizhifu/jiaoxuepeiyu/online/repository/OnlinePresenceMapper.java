package com.qizhifu.jiaoxuepeiyu.online.repository;

import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUser;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUserQuery;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OnlinePresenceMapper {

    @Update("UPDATE sys_user SET last_login_ip = #{ipAddress}, last_heartbeat_time = #{heartbeatAt}, updated_at = NOW() "
            + "WHERE id = #{userId} AND status = 1")
    void updateHeartbeat(@Param("userId") Long userId,
                         @Param("ipAddress") String ipAddress,
                         @Param("heartbeatAt") LocalDateTime heartbeatAt);

    @Update("UPDATE sys_user SET last_heartbeat_time = NULL, updated_at = NOW() WHERE id = #{userId}")
    void markOffline(@Param("userId") Long userId);

    @Select("<script>"
            + "SELECT id AS user_id, username, real_name, user_type, last_login_ip, last_heartbeat_time, "
            + "CASE WHEN last_heartbeat_time IS NOT NULL AND last_heartbeat_time &gt;= #{onlineCutoff} THEN TRUE ELSE FALSE END AS online "
            + "FROM sys_user WHERE status = 1 "
            + "<if test='query.userType != null'>AND user_type = #{query.userType}</if> "
            + "<if test='query.keyword != null'>AND (username LIKE #{query.keyword} OR real_name LIKE #{query.keyword} OR phone LIKE #{query.keyword})</if> "
            + "<if test='query.onlineOnly != null and query.onlineOnly'>AND last_heartbeat_time IS NOT NULL AND last_heartbeat_time &gt;= #{onlineCutoff}</if> "
            + "ORDER BY online DESC, last_heartbeat_time DESC, id DESC LIMIT #{query.limit} "
            + "</script>")
    List<OnlineUser> findUsers(@Param("query") OnlineUserQuery query,
                               @Param("onlineCutoff") LocalDateTime onlineCutoff);

    @Select("<script>"
            + "SELECT COUNT(*) FROM sys_user WHERE status = 1 "
            + "<if test='query.userType != null'>AND user_type = #{query.userType}</if> "
            + "<if test='query.keyword != null'>AND (username LIKE #{query.keyword} OR real_name LIKE #{query.keyword} OR phone LIKE #{query.keyword})</if> "
            + "</script>")
    long countUsers(@Param("query") OnlineUserQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM sys_user WHERE status = 1 "
            + "AND last_heartbeat_time IS NOT NULL AND last_heartbeat_time &gt;= #{onlineCutoff} "
            + "<if test='query.userType != null'>AND user_type = #{query.userType}</if> "
            + "<if test='query.keyword != null'>AND (username LIKE #{query.keyword} OR real_name LIKE #{query.keyword} OR phone LIKE #{query.keyword})</if> "
            + "</script>")
    long countOnlineUsers(@Param("query") OnlineUserQuery query,
                          @Param("onlineCutoff") LocalDateTime onlineCutoff);
}
