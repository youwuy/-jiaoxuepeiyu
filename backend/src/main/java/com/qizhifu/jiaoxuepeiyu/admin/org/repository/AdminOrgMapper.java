package com.qizhifu.jiaoxuepeiyu.admin.org.repository;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminOrgMapper {

    @Select("SELECT o.id AS org_id, o.parent_id, o.org_name, o.sort_order, "
            + "CASE WHEN o.status = 1 THEN TRUE ELSE FALSE END AS enabled, "
            + "o.created_by, creator.real_name AS created_name, "
            + "o.updated_by, updater.real_name AS updated_name, "
            + "o.created_at, o.updated_at "
            + "FROM sys_org o "
            + "LEFT JOIN sys_user creator ON creator.id = o.created_by "
            + "LEFT JOIN sys_user updater ON updater.id = o.updated_by "
            + "ORDER BY o.sort_order ASC, o.id ASC")
    List<AdminOrg> findAll();

    @Insert("INSERT INTO sys_org (parent_id, org_name, sort_order, status, created_by, updated_by, created_at, updated_at) "
            + "VALUES (#{parentId}, #{orgName}, #{sortOrder}, 1, #{createdBy}, #{updatedBy}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "orgId")
    void insert(AdminOrg org);

    @Update("UPDATE sys_org SET parent_id = #{command.parentId}, org_name = #{command.orgName}, "
            + "sort_order = #{command.sortOrder}, updated_by = #{operatorId}, updated_at = NOW() WHERE id = #{orgId}")
    void update(@Param("orgId") Long orgId, @Param("command") AdminOrgCommand command, @Param("operatorId") Long operatorId);

    @Update("UPDATE sys_org SET status = #{status}, updated_by = #{operatorId}, updated_at = NOW() WHERE id = #{orgId}")
    void updateStatus(@Param("orgId") Long orgId, @Param("status") int status, @Param("operatorId") Long operatorId);

    @Update("UPDATE sys_org SET sort_order = #{sortOrder}, updated_by = #{operatorId}, updated_at = NOW() WHERE id = #{orgId}")
    void updateSort(@Param("orgId") Long orgId,
                    @Param("sortOrder") int sortOrder,
                    @Param("operatorId") Long operatorId);
}
