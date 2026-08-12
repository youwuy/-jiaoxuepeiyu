package com.qizhifu.jiaoxuepeiyu.admin.account.repository;

import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminAccountMapper {

    @Select("<script>"
            + "SELECT u.id AS user_id, u.username AS account_no, u.real_name, u.phone, "
            + "u.id_card AS id_card, u.id_card AS masked_id_card, u.job_title, u.user_type, u.org_id, o.org_name, "
            + "u.class_id, c.class_name, CASE WHEN u.status = 1 THEN TRUE ELSE FALSE END AS enabled, "
            + "CASE WHEN u.face_file_id IS NULL THEN FALSE ELSE TRUE END AS face_recorded, "
            + "CASE WHEN u.fingerprint_file_id IS NULL THEN FALSE ELSE TRUE END AS fingerprint_recorded, "
            + "u.created_at "
            + "FROM sys_user u "
            + "LEFT JOIN sys_org o ON o.id = u.org_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE u.user_type = #{userType} "
            + "<if test=\"dataScope == 'SELF'\">AND u.id = #{currentUserId}</if> "
            + "<if test=\"dataScope == 'ORG_ONLY' and (orgIds == null or orgIds.isEmpty())\">AND 1 = 0</if> "
            + "<if test='orgIds != null and !orgIds.isEmpty()'>AND u.org_id IN "
            + "<foreach collection='orgIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='enabled != null'>AND u.status = <choose><when test='enabled'>1</when><otherwise>0</otherwise></choose></if> "
            + "<if test='realName != null'>AND u.real_name LIKE #{realName}</if> "
            + "<if test='accountNo != null'>AND u.username LIKE #{accountNo}</if> "
            + "<if test='phone != null'>AND u.phone LIKE #{phone}</if> "
            + "<if test='jobTitle != null'>AND u.job_title LIKE #{jobTitle}</if> "
            + "ORDER BY u.created_at DESC, u.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    List<AdminAccount> findAccounts(AdminAccountQuery query);

    @Select("<script>"
            + "SELECT u.id AS user_id, u.username AS account_no, u.real_name, u.phone, "
            + "u.id_card AS id_card, u.id_card AS masked_id_card, u.job_title, u.user_type, u.org_id, o.org_name, "
            + "u.class_id, c.class_name, CASE WHEN u.status = 1 THEN TRUE ELSE FALSE END AS enabled, "
            + "CASE WHEN u.face_file_id IS NULL THEN FALSE ELSE TRUE END AS face_recorded, "
            + "CASE WHEN u.fingerprint_file_id IS NULL THEN FALSE ELSE TRUE END AS fingerprint_recorded, "
            + "u.created_at "
            + "FROM sys_user u "
            + "LEFT JOIN sys_org o ON o.id = u.org_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE u.user_type = #{userType} "
            + "<if test=\"dataScope == 'SELF'\">AND u.id = #{currentUserId}</if> "
            + "<if test=\"dataScope == 'ORG_ONLY' and (orgIds == null or orgIds.isEmpty())\">AND 1 = 0</if> "
            + "<if test='orgIds != null and !orgIds.isEmpty()'>AND u.org_id IN "
            + "<foreach collection='orgIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='enabled != null'>AND u.status = <choose><when test='enabled'>1</when><otherwise>0</otherwise></choose></if> "
            + "<if test='realName != null'>AND u.real_name LIKE #{realName}</if> "
            + "<if test='accountNo != null'>AND u.username LIKE #{accountNo}</if> "
            + "<if test='phone != null'>AND u.phone LIKE #{phone}</if> "
            + "<if test='jobTitle != null'>AND u.job_title LIKE #{jobTitle}</if> "
            + "ORDER BY u.created_at DESC, u.id DESC "
            + "</script>")
    List<AdminAccount> findAccountsForExport(AdminAccountQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM sys_user u WHERE u.user_type = #{userType} "
            + "<if test=\"dataScope == 'SELF'\">AND u.id = #{currentUserId}</if> "
            + "<if test=\"dataScope == 'ORG_ONLY' and (orgIds == null or orgIds.isEmpty())\">AND 1 = 0</if> "
            + "<if test='orgIds != null and !orgIds.isEmpty()'>AND u.org_id IN "
            + "<foreach collection='orgIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> "
            + "<if test='classId != null'>AND u.class_id = #{classId}</if> "
            + "<if test='enabled != null'>AND u.status = <choose><when test='enabled'>1</when><otherwise>0</otherwise></choose></if> "
            + "<if test='realName != null'>AND u.real_name LIKE #{realName}</if> "
            + "<if test='accountNo != null'>AND u.username LIKE #{accountNo}</if> "
            + "<if test='phone != null'>AND u.phone LIKE #{phone}</if> "
            + "<if test='jobTitle != null'>AND u.job_title LIKE #{jobTitle}</if> "
            + "</script>")
    long countAccounts(AdminAccountQuery query);

    @Select("SELECT id FROM sys_org WHERE parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<Long> findChildOrgIds(@Param("parentId") Long parentId);

    @Select("SELECT u.id AS user_id, u.username AS account_no, u.real_name, u.phone, "
            + "u.id_card AS id_card, u.id_card AS masked_id_card, u.job_title, u.user_type, u.org_id, o.org_name, "
            + "u.class_id, c.class_name, CASE WHEN u.status = 1 THEN TRUE ELSE FALSE END AS enabled, "
            + "CASE WHEN u.face_file_id IS NULL THEN FALSE ELSE TRUE END AS face_recorded, "
            + "CASE WHEN u.fingerprint_file_id IS NULL THEN FALSE ELSE TRUE END AS fingerprint_recorded, "
            + "u.created_at "
            + "FROM sys_user u "
            + "LEFT JOIN sys_org o ON o.id = u.org_id "
            + "LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE u.id = #{userId} LIMIT 1")
    AdminAccount findById(@Param("userId") Long userId);

    @Select("<script>"
            + "SELECT username FROM sys_user WHERE username IN "
            + "<foreach collection='accountNos' item='accountNo' open='(' separator=',' close=')'>#{accountNo}</foreach>"
            + "</script>")
    List<String> findExistingAccountNos(@Param("accountNos") List<String> accountNos);

    @Insert("INSERT INTO sys_user "
            + "(username, real_name, phone, user_type, status, password_hash, org_id, class_id, "
            + "id_card, job_title, face_file_id, fingerprint_file_id, created_at, updated_at) "
            + "VALUES (#{command.accountNo}, #{command.realName}, #{command.phone}, #{command.userType}, "
            + "1, #{passwordHash}, #{command.orgId}, #{command.classId}, #{command.idCard}, "
            + "#{command.jobTitle}, #{command.faceFileId}, #{command.fingerprintFileId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "command.userId")
    void insert(@Param("command") AdminAccountCommand command, @Param("passwordHash") String passwordHash);

    @Update("UPDATE sys_user SET real_name = #{command.realName}, phone = #{command.phone}, "
            + "org_id = #{command.orgId}, class_id = #{command.classId}, id_card = #{command.idCard}, "
            + "job_title = #{command.jobTitle}, face_file_id = #{command.faceFileId}, "
            + "fingerprint_file_id = #{command.fingerprintFileId}, updated_at = NOW() "
            + "WHERE id = #{userId} AND user_type = #{command.userType}")
    void update(@Param("userId") Long userId, @Param("command") AdminAccountCommand command);

    @Update("UPDATE sys_user SET status = #{status}, updated_at = NOW() WHERE id = #{userId}")
    void updateStatus(@Param("userId") Long userId, @Param("status") int status);

    @Update("<script>"
            + "UPDATE sys_user SET password_hash = #{passwordHash}, updated_at = NOW() "
            + "WHERE id IN "
            + "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach>"
            + "</script>")
    void resetPasswords(@Param("userIds") List<Long> userIds, @Param("passwordHash") String passwordHash);

    @Update("<script>"
            + "UPDATE sys_user SET org_id = #{orgId}, updated_at = NOW() WHERE id IN "
            + "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach>"
            + "</script>")
    void updateOrg(@Param("userIds") List<Long> userIds, @Param("orgId") Long orgId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteRoles(@Param("userId") Long userId);

    @Insert("<script>"
            + "INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES "
            + "<foreach collection='roleIds' item='roleId' separator=','>(#{userId}, #{roleId}, NOW())</foreach>"
            + "</script>")
    void insertRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    @Delete("DELETE FROM sys_user_org_scope WHERE user_id = #{userId} AND scope_type = #{scopeType}")
    void deleteScopes(@Param("userId") Long userId, @Param("scopeType") String scopeType);

    @Insert("<script>"
            + "INSERT INTO sys_user_org_scope (user_id, org_id, scope_type, created_at) VALUES "
            + "<foreach collection='ids' item='id' separator=','>(#{userId}, #{id}, #{scopeType}, NOW())</foreach>"
            + "</script>")
    void insertScopes(@Param("userId") Long userId, @Param("scopeType") String scopeType, @Param("ids") List<Long> ids);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId} ORDER BY role_id ASC")
    List<Long> findRoleIds(@Param("userId") Long userId);

    @Select("SELECT r.role_name FROM sys_user_role ur JOIN sys_role r ON r.id = ur.role_id "
            + "WHERE ur.user_id = #{userId} ORDER BY r.role_name ASC")
    List<String> findRoleNames(@Param("userId") Long userId);

    @Select("SELECT org_id FROM sys_user_org_scope "
            + "WHERE user_id = #{userId} AND scope_type = #{scopeType} ORDER BY org_id ASC")
    List<Long> findScopeIds(@Param("userId") Long userId, @Param("scopeType") String scopeType);
}
