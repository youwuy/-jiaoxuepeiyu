package com.qizhifu.jiaoxuepeiyu.auth.repository;

import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import java.time.Instant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SessionMapper {

    @Select("SELECT COUNT(*) FROM sys_user_session "
            + "WHERE user_id = #{userId} AND invalidated_at IS NULL AND expires_at > #{now}")
    int countActiveSessions(@Param("userId") Long userId, @Param("now") Instant now);

    @Update("UPDATE sys_user_session SET invalidated_at = NOW() "
            + "WHERE user_id = #{userId} AND invalidated_at IS NULL AND expires_at > NOW()")
    void invalidateActiveSessions(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_session(user_id, token_hash, portal, login_ip, expires_at) "
            + "VALUES(#{userId}, #{tokenHash}, #{portal}, #{loginIp}, #{expiresAt})")
    void createSession(@Param("userId") Long userId,
                       @Param("tokenHash") String tokenHash,
                       @Param("portal") String portal,
                       @Param("loginIp") String loginIp,
                       @Param("expiresAt") Instant expiresAt);

    @Select("SELECT u.id, u.username, u.real_name, u.user_type "
            + "FROM sys_user_session s JOIN sys_user u ON u.id = s.user_id "
            + "WHERE s.token_hash = #{tokenHash} "
            + "AND s.invalidated_at IS NULL "
            + "AND s.expires_at > #{now} "
            + "AND u.status = 1 "
            + "LIMIT 1")
    AuthenticatedUser findActiveUserByToken(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    @Update("UPDATE sys_user_session SET invalidated_at = NOW() "
            + "WHERE token_hash = #{tokenHash} AND invalidated_at IS NULL AND expires_at > NOW()")
    void invalidateToken(@Param("tokenHash") String tokenHash);
}
