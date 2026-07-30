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

    @Select("SELECT id AS org_id, parent_id, org_name, sort_order, "
            + "CASE WHEN status = 1 THEN TRUE ELSE FALSE END AS enabled "
            + "FROM sys_org ORDER BY sort_order ASC, id ASC")
    List<AdminOrg> findAll();

    @Insert("INSERT INTO sys_org (parent_id, org_name, sort_order, status, created_at, updated_at) "
            + "VALUES (#{parentId}, #{orgName}, #{sortOrder}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "orgId")
    void insert(AdminOrg org);

    @Update("UPDATE sys_org SET parent_id = #{command.parentId}, org_name = #{command.orgName}, "
            + "sort_order = #{command.sortOrder}, updated_at = NOW() WHERE id = #{orgId}")
    void update(@Param("orgId") Long orgId, @Param("command") AdminOrgCommand command);

    @Update("UPDATE sys_org SET status = #{status}, updated_at = NOW() WHERE id = #{orgId}")
    void updateStatus(@Param("orgId") Long orgId, @Param("status") int status);
}
