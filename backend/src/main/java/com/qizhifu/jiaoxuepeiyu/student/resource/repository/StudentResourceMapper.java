package com.qizhifu.jiaoxuepeiyu.student.resource.repository;

import com.qizhifu.jiaoxuepeiyu.student.resource.model.PublicResourceCard;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentResourceMapper {

    @Select("<script>"
            + "SELECT r.id AS resource_id, r.resource_name, r.resource_type, r.cover_url, r.preview_url, "
            + "r.file_url, r.file_name, r.file_size, "
            + "r.major_id, m.major_name, u.real_name AS uploader_name, r.updated_at "
            + "FROM res_public_resource r "
            + "LEFT JOIN edu_major m ON m.id = r.major_id "
            + "LEFT JOIN sys_user u ON u.id = r.uploader_id "
            + "WHERE r.public_status = 'PUBLIC' "
            + "<if test='keyword != null'>AND r.resource_name LIKE #{keyword}</if> "
            + "<if test='resourceType != null'>AND r.resource_type = #{resourceType}</if> "
            + "<if test='majorId != null'>AND r.major_id = #{majorId}</if> "
            + "ORDER BY r.updated_at DESC, r.id DESC "
            + "</script>")
    List<PublicResourceCard> findPublicResources(@Param("keyword") String keyword,
                                                 @Param("resourceType") String resourceType,
                                                 @Param("majorId") Long majorId);
}
