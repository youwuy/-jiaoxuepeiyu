package com.qizhifu.jiaoxuepeiyu.admin.resource.repository;

import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicApplication;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResource;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceBatchCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceLog;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceQuery;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminResourceMapper {

    @Select("<script>"
            + "SELECT r.id AS resource_id, r.resource_name, r.resource_type, r.cover_url, r.file_url, "
            + "r.preview_url, r.file_name, r.file_size, r.major_id, m.major_name, r.course_name, "
            + "r.uploader_id, u.real_name AS uploader_name, r.public_status, r.current_version, "
            + "r.public_version, r.created_at, r.updated_at "
            + "FROM res_resource r "
            + "LEFT JOIN edu_major m ON m.id = r.major_id "
            + "LEFT JOIN sys_user u ON u.id = r.uploader_id "
            + "WHERE r.deleted_flag = 0 "
            + "<if test='keyword != null'>AND r.resource_name LIKE #{keyword}</if> "
            + "<if test='resourceType != null'>AND r.resource_type = #{resourceType}</if> "
            + "<if test='majorId != null'>AND r.major_id = #{majorId}</if> "
            + "<if test='courseName != null'>AND r.course_name LIKE #{courseName}</if> "
            + "<if test='uploaderId != null'>AND r.uploader_id = #{uploaderId}</if> "
            + "<if test='publicStatus != null'>AND r.public_status = #{publicStatus}</if> "
            + "<if test='uploadStartDate != null'>AND r.created_at &gt;= CONCAT(#{uploadStartDate}, ' 00:00:00')</if> "
            + "<if test='uploadEndDate != null'>AND r.created_at &lt;= CONCAT(#{uploadEndDate}, ' 23:59:59')</if> "
            + "ORDER BY r.updated_at DESC, r.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    List<AdminResource> findResources(AdminResourceQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM res_resource r WHERE r.deleted_flag = 0 "
            + "<if test='keyword != null'>AND r.resource_name LIKE #{keyword}</if> "
            + "<if test='resourceType != null'>AND r.resource_type = #{resourceType}</if> "
            + "<if test='majorId != null'>AND r.major_id = #{majorId}</if> "
            + "<if test='courseName != null'>AND r.course_name LIKE #{courseName}</if> "
            + "<if test='uploaderId != null'>AND r.uploader_id = #{uploaderId}</if> "
            + "<if test='publicStatus != null'>AND r.public_status = #{publicStatus}</if> "
            + "<if test='uploadStartDate != null'>AND r.created_at &gt;= CONCAT(#{uploadStartDate}, ' 00:00:00')</if> "
            + "<if test='uploadEndDate != null'>AND r.created_at &lt;= CONCAT(#{uploadEndDate}, ' 23:59:59')</if> "
            + "</script>")
    long countResources(AdminResourceQuery query);

    @Select("SELECT r.id AS resource_id, r.resource_name, r.resource_type, r.cover_url, r.file_url, "
            + "r.preview_url, r.file_name, r.file_size, r.major_id, m.major_name, r.course_name, "
            + "r.uploader_id, u.real_name AS uploader_name, r.public_status, r.current_version, "
            + "r.public_version, r.created_at, r.updated_at "
            + "FROM res_resource r "
            + "LEFT JOIN edu_major m ON m.id = r.major_id "
            + "LEFT JOIN sys_user u ON u.id = r.uploader_id "
            + "WHERE r.id = #{resourceId} AND r.deleted_flag = 0 LIMIT 1")
    AdminResource findResource(@Param("resourceId") Long resourceId);

    @Insert("INSERT INTO res_resource "
            + "(resource_name, resource_type, cover_url, file_url, preview_url, file_name, file_size, "
            + "major_id, course_name, uploader_id, public_status, current_version, public_version, "
            + "deleted_flag, created_at, updated_at) "
            + "VALUES (#{resourceName}, #{resourceType}, #{coverUrl}, #{fileUrl}, #{previewUrl}, "
            + "#{fileName}, #{fileSize}, #{majorId}, #{courseName}, #{uploaderId}, #{publicStatus}, "
            + "#{currentVersion}, #{publicVersion}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "resourceId")
    void insertResource(AdminResource resource);

    @Update("UPDATE res_resource SET resource_name = #{resourceName}, resource_type = #{resourceType}, "
            + "cover_url = #{coverUrl}, file_url = #{fileUrl}, preview_url = #{previewUrl}, "
            + "file_name = #{fileName}, file_size = #{fileSize}, major_id = #{majorId}, "
            + "course_name = #{courseName}, public_status = #{publicStatus}, current_version = #{currentVersion}, "
            + "public_version = #{publicVersion}, updated_at = NOW() WHERE id = #{resourceId} AND deleted_flag = 0")
    void updateResource(AdminResource resource);

    @Insert("INSERT INTO res_resource_version "
            + "(resource_id, resource_version, resource_name, resource_type, cover_url, file_url, preview_url, "
            + "file_name, file_size, major_id, course_name, created_by, created_at) "
            + "VALUES (#{resource.resourceId}, #{resource.currentVersion}, #{resource.resourceName}, "
            + "#{resource.resourceType}, #{resource.coverUrl}, #{resource.fileUrl}, #{resource.previewUrl}, "
            + "#{resource.fileName}, #{resource.fileSize}, #{resource.majorId}, #{resource.courseName}, "
            + "#{createdBy}, NOW())")
    void insertResourceVersion(@Param("resource") AdminResource resource, @Param("createdBy") Long createdBy);

    @Update("<script>"
            + "UPDATE res_resource "
            + "<set>"
            + "<if test='coverUrl != null'>cover_url = #{coverUrl},</if>"
            + "<if test='majorId != null'>major_id = #{majorId},</if>"
            + "<if test='courseName != null'>course_name = #{courseName},</if>"
            + "updated_at = NOW()"
            + "</set>"
            + "WHERE deleted_flag = 0 AND id IN "
            + "<foreach collection='resourceIds' item='resourceId' open='(' separator=',' close=')'>#{resourceId}</foreach>"
            + "</script>")
    void batchUpdate(AdminResourceBatchCommand command);

    @Select("<script>"
            + "SELECT DISTINCT c.course_name FROM course_content cc "
            + "JOIN course c ON c.id = cc.course_id "
            + "WHERE cc.resource_id IN "
            + "<foreach collection='resourceIds' item='resourceId' open='(' separator=',' close=')'>#{resourceId}</foreach>"
            + " ORDER BY c.course_name ASC"
            + "</script>")
    List<String> findCourseNamesUsingResources(@Param("resourceIds") List<Long> resourceIds);

    @Update("<script>"
            + "UPDATE res_resource SET deleted_flag = 1, updated_at = NOW() WHERE id IN "
            + "<foreach collection='resourceIds' item='resourceId' open='(' separator=',' close=')'>#{resourceId}</foreach>"
            + "</script>")
    void deleteResources(@Param("resourceIds") List<Long> resourceIds);

    @Select("SELECT id FROM res_public_application "
            + "WHERE resource_id = #{resourceId} AND public_status = 'PENDING' LIMIT 1")
    Long findPendingApplicationId(@Param("resourceId") Long resourceId);

    @Insert("INSERT INTO res_public_application "
            + "(resource_id, resource_version, applicant_id, public_status, applied_at, created_at, updated_at) "
            + "VALUES (#{resourceId}, #{resourceVersion}, #{applicantId}, 'PENDING', NOW(), NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "applicationId")
    void insertPublicApplication(AdminPublicApplication application);

    @Select("<script>"
            + "SELECT a.id AS application_id, a.resource_id, a.resource_version, v.resource_name, "
            + "v.resource_type, v.cover_url, v.file_url, v.preview_url, v.file_name, v.file_size, "
            + "v.major_id, m.major_name, v.course_name, a.applicant_id, applicant.real_name AS applicant_name, "
            + "a.reviewer_id, reviewer.real_name AS reviewer_name, a.public_status, a.review_comment, "
            + "a.applied_at, a.reviewed_at "
            + "FROM res_public_application a "
            + "JOIN res_resource_version v ON v.resource_id = a.resource_id AND v.resource_version = a.resource_version "
            + "LEFT JOIN edu_major m ON m.id = v.major_id "
            + "LEFT JOIN sys_user applicant ON applicant.id = a.applicant_id "
            + "LEFT JOIN sys_user reviewer ON reviewer.id = a.reviewer_id "
            + "WHERE 1 = 1 "
            + "<if test='keyword != null'>AND v.resource_name LIKE #{keyword}</if> "
            + "<if test='resourceType != null'>AND v.resource_type = #{resourceType}</if> "
            + "<if test='majorId != null'>AND v.major_id = #{majorId}</if> "
            + "<if test='courseName != null'>AND v.course_name LIKE #{courseName}</if> "
            + "<if test='uploaderId != null'>AND a.applicant_id = #{uploaderId}</if> "
            + "<if test='publicStatus != null'>AND a.public_status = #{publicStatus}</if> "
            + "ORDER BY a.applied_at DESC, a.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    List<AdminPublicApplication> findPublicApplications(AdminResourceQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM res_public_application a "
            + "JOIN res_resource_version v ON v.resource_id = a.resource_id AND v.resource_version = a.resource_version "
            + "WHERE 1 = 1 "
            + "<if test='keyword != null'>AND v.resource_name LIKE #{keyword}</if> "
            + "<if test='resourceType != null'>AND v.resource_type = #{resourceType}</if> "
            + "<if test='majorId != null'>AND v.major_id = #{majorId}</if> "
            + "<if test='courseName != null'>AND v.course_name LIKE #{courseName}</if> "
            + "<if test='uploaderId != null'>AND a.applicant_id = #{uploaderId}</if> "
            + "<if test='publicStatus != null'>AND a.public_status = #{publicStatus}</if> "
            + "</script>")
    long countPublicApplications(AdminResourceQuery query);

    @Select("SELECT a.id AS application_id, a.resource_id, a.resource_version, v.resource_name, "
            + "v.resource_type, v.cover_url, v.file_url, v.preview_url, v.file_name, v.file_size, "
            + "v.major_id, m.major_name, v.course_name, a.applicant_id, applicant.real_name AS applicant_name, "
            + "a.reviewer_id, reviewer.real_name AS reviewer_name, a.public_status, a.review_comment, "
            + "a.applied_at, a.reviewed_at "
            + "FROM res_public_application a "
            + "JOIN res_resource_version v ON v.resource_id = a.resource_id AND v.resource_version = a.resource_version "
            + "LEFT JOIN edu_major m ON m.id = v.major_id "
            + "LEFT JOIN sys_user applicant ON applicant.id = a.applicant_id "
            + "LEFT JOIN sys_user reviewer ON reviewer.id = a.reviewer_id "
            + "WHERE a.id = #{applicationId} LIMIT 1")
    AdminPublicApplication findPublicApplication(@Param("applicationId") Long applicationId);

    @Update("UPDATE res_public_application SET public_status = #{status}, review_comment = #{reviewComment}, "
            + "reviewer_id = #{reviewerId}, reviewed_at = NOW(), updated_at = NOW() WHERE id = #{applicationId}")
    void reviewPublicApplication(@Param("applicationId") Long applicationId,
                                 @Param("status") String status,
                                 @Param("reviewComment") String reviewComment,
                                 @Param("reviewerId") Long reviewerId);

    @Select("SELECT id FROM res_public_resource WHERE source_resource_id = #{resourceId} LIMIT 1")
    Long findPublicResourceId(@Param("resourceId") Long resourceId);

    @Insert("INSERT INTO res_public_resource "
            + "(source_resource_id, resource_version, resource_name, resource_type, cover_url, preview_url, "
            + "file_url, file_name, file_size, major_id, course_name, uploader_id, public_status, created_at, updated_at) "
            + "VALUES (#{resourceId}, #{resourceVersion}, #{resourceName}, #{resourceType}, #{coverUrl}, "
            + "#{previewUrl}, #{fileUrl}, #{fileName}, #{fileSize}, #{majorId}, #{courseName}, "
            + "#{applicantId}, 'PUBLIC', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "publicResourceId")
    void insertPublicResource(AdminPublicApplication application);

    @Update("UPDATE res_public_resource SET resource_version = #{application.resourceVersion}, "
            + "resource_name = #{application.resourceName}, resource_type = #{application.resourceType}, "
            + "cover_url = #{application.coverUrl}, preview_url = #{application.previewUrl}, "
            + "file_url = #{application.fileUrl}, file_name = #{application.fileName}, "
            + "file_size = #{application.fileSize}, major_id = #{application.majorId}, "
            + "course_name = #{application.courseName}, uploader_id = #{application.applicantId}, "
            + "public_status = 'PUBLIC', updated_at = NOW() WHERE id = #{publicResourceId}")
    void updatePublicResource(@Param("publicResourceId") Long publicResourceId,
                              @Param("application") AdminPublicApplication application);

    @Update("UPDATE res_resource SET public_status = #{publicStatus}, public_version = #{publicVersion}, "
            + "updated_at = NOW() WHERE id = #{resourceId} AND deleted_flag = 0")
    void updateResourcePublicState(@Param("resourceId") Long resourceId,
                                   @Param("publicStatus") String publicStatus,
                                   @Param("publicVersion") Integer publicVersion);

    @Select("<script>"
            + "SELECT p.id AS resource_id, p.source_resource_id, p.resource_name, p.resource_type, "
            + "p.cover_url, p.file_url, p.preview_url, p.file_name, p.file_size, p.major_id, "
            + "m.major_name, p.course_name, p.uploader_id, u.real_name AS uploader_name, "
            + "p.public_status, p.resource_version AS public_version, p.created_at, p.updated_at "
            + "FROM res_public_resource p "
            + "LEFT JOIN edu_major m ON m.id = p.major_id "
            + "LEFT JOIN sys_user u ON u.id = p.uploader_id "
            + "WHERE p.public_status = 'PUBLIC' "
            + "<if test='keyword != null'>AND p.resource_name LIKE #{keyword}</if> "
            + "<if test='resourceType != null'>AND p.resource_type = #{resourceType}</if> "
            + "<if test='majorId != null'>AND p.major_id = #{majorId}</if> "
            + "<if test='courseName != null'>AND p.course_name LIKE #{courseName}</if> "
            + "<if test='uploaderId != null'>AND p.uploader_id = #{uploaderId}</if> "
            + "ORDER BY p.updated_at DESC, p.id DESC LIMIT #{pageSize} OFFSET #{offset} "
            + "</script>")
    List<AdminResource> findPublicResources(AdminResourceQuery query);

    @Select("<script>"
            + "SELECT COUNT(*) FROM res_public_resource p WHERE p.public_status = 'PUBLIC' "
            + "<if test='keyword != null'>AND p.resource_name LIKE #{keyword}</if> "
            + "<if test='resourceType != null'>AND p.resource_type = #{resourceType}</if> "
            + "<if test='majorId != null'>AND p.major_id = #{majorId}</if> "
            + "<if test='courseName != null'>AND p.course_name LIKE #{courseName}</if> "
            + "<if test='uploaderId != null'>AND p.uploader_id = #{uploaderId}</if> "
            + "</script>")
    long countPublicResources(AdminResourceQuery query);

    @Insert("INSERT INTO msg_notification (message_type, title, content, source_id, created_at) "
            + "VALUES ('RESOURCE', #{title}, #{content}, #{sourceId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "notificationId")
    void insertNotification(AdminResourceNotification notification);

    @Insert("INSERT IGNORE INTO msg_user_notification "
            + "(notification_id, user_id, read_flag, created_at) "
            + "SELECT #{notificationId}, id, 0, NOW() FROM sys_user "
            + "WHERE user_type = 'student' AND status = 1")
    void notifyAllStudents(@Param("notificationId") Long notificationId);

    @Insert("INSERT INTO res_resource_log (resource_id, operator_id, action, content, created_at) "
            + "VALUES (#{resourceId}, #{operatorId}, #{action}, #{content}, NOW())")
    void insertResourceLog(@Param("resourceId") Long resourceId,
                           @Param("operatorId") Long operatorId,
                           @Param("action") String action,
                           @Param("content") String content);

    @Select("SELECT l.id AS log_id, l.resource_id, l.operator_id, u.real_name AS operator_name, "
            + "l.action, l.content, l.created_at "
            + "FROM res_resource_log l "
            + "LEFT JOIN sys_user u ON u.id = l.operator_id "
            + "WHERE l.resource_id = #{resourceId} ORDER BY l.created_at DESC, l.id DESC")
    List<AdminResourceLog> findResourceLogs(@Param("resourceId") Long resourceId);
}
