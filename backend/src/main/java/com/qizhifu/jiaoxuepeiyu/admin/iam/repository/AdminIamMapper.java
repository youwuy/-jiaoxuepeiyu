package com.qizhifu.jiaoxuepeiyu.admin.iam.repository;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRole;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleLog;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleQuery;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminIamMapper {

    @Select("SELECT id AS permission_id, parent_id, permission_name, permission_code, permission_type, "
            + "route_path, CASE WHEN visible = 1 THEN TRUE ELSE FALSE END AS visible, sort_order "
            + "FROM sys_permission ORDER BY sort_order ASC, id ASC")
    List<AdminPermission> findPermissions();

    @Select("<script>"
            + "SELECT r.id AS role_id, r.role_name, r.role_code, r.data_scope, r.remark, "
            + "CASE WHEN r.status = 1 THEN TRUE ELSE FALSE END AS enabled, "
            + "(SELECT COUNT(*) FROM sys_user_role ur WHERE ur.role_id = r.id) AS user_count, "
            + "r.created_at, r.updated_at "
            + "FROM sys_role r WHERE r.deleted_flag = 0 "
            + "<if test='keyword != null'>AND (r.role_name LIKE #{keyword} OR r.role_code LIKE #{keyword})</if> "
            + "<if test='enabled != null'>AND r.status = <choose><when test='enabled'>1</when><otherwise>0</otherwise></choose></if> "
            + "ORDER BY r.updated_at DESC, r.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    List<AdminRole> findRoles(AdminRoleQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM sys_role r WHERE r.deleted_flag = 0 "
            + "<if test='keyword != null'>AND (r.role_name LIKE #{keyword} OR r.role_code LIKE #{keyword})</if> "
            + "<if test='enabled != null'>AND r.status = <choose><when test='enabled'>1</when><otherwise>0</otherwise></choose></if> "
            + "</script>")
    long countRoles(AdminRoleQuery query);

    @Select("SELECT r.id AS role_id, r.role_name, r.role_code, r.data_scope, r.remark, "
            + "CASE WHEN r.status = 1 THEN TRUE ELSE FALSE END AS enabled, "
            + "(SELECT COUNT(*) FROM sys_user_role ur WHERE ur.role_id = r.id) AS user_count, "
            + "r.created_at, r.updated_at "
            + "FROM sys_role r WHERE r.id = #{roleId} AND r.deleted_flag = 0 LIMIT 1")
    AdminRole findRole(@Param("roleId") Long roleId);

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId} ORDER BY permission_id ASC")
    List<Long> findPermissionIds(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role (role_name, role_code, data_scope, remark, status, deleted_flag, created_at, updated_at) "
            + "VALUES (#{roleName}, #{roleCode}, #{dataScope}, #{remark}, 1, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "roleId")
    void insertRole(AdminRole role);

    @Update("UPDATE sys_role SET role_name = #{roleName}, role_code = #{roleCode}, "
            + "data_scope = #{dataScope}, remark = #{remark}, updated_at = NOW() "
            + "WHERE id = #{roleId} AND deleted_flag = 0")
    void updateRole(AdminRole role);

    @Update("UPDATE sys_role SET status = #{status}, updated_at = NOW() WHERE id = #{roleId} AND deleted_flag = 0")
    void updateStatus(@Param("roleId") Long roleId, @Param("status") int status);

    @Update("UPDATE sys_role SET deleted_flag = 1, status = 0, updated_at = NOW() WHERE id = #{roleId} AND deleted_flag = 0")
    void deleteRole(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deletePermissions(@Param("roleId") Long roleId);

    @Insert("<script>"
            + "INSERT INTO sys_role_permission (role_id, permission_id, data_scope, created_at) VALUES "
            + "<foreach collection='permissionIds' item='permissionId' separator=','>"
            + "(#{roleId}, #{permissionId}, #{dataScope}, NOW())"
            + "</foreach>"
            + "</script>")
    void insertPermissions(@Param("roleId") Long roleId,
                           @Param("permissionIds") List<Long> permissionIds,
                           @Param("dataScope") String dataScope);

    @Insert("INSERT INTO sys_role_log (role_id, operator_id, action, content, created_at) "
            + "VALUES (#{roleId}, #{operatorId}, #{action}, #{content}, NOW())")
    void insertRoleLog(@Param("roleId") Long roleId,
                       @Param("operatorId") Long operatorId,
                       @Param("action") String action,
                       @Param("content") String content);

    @Select("SELECT l.id AS log_id, l.role_id, l.operator_id, u.real_name AS operator_name, "
            + "l.action, l.content, l.created_at "
            + "FROM sys_role_log l LEFT JOIN sys_user u ON u.id = l.operator_id "
            + "WHERE l.role_id = #{roleId} ORDER BY l.created_at DESC, l.id DESC")
    List<AdminRoleLog> findRoleLogs(@Param("roleId") Long roleId);
}
