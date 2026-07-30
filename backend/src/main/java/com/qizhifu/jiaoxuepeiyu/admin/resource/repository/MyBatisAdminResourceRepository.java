package com.qizhifu.jiaoxuepeiyu.admin.resource.repository;

import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicApplication;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResource;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceBatchCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceLog;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceQuery;
import com.qizhifu.jiaoxuepeiyu.admin.resource.port.AdminResourceRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminResourceRepository implements AdminResourceRepository {

    private final AdminResourceMapper mapper;

    public MyBatisAdminResourceRepository(AdminResourceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminResource> findResources(AdminResourceQuery query) {
        return mapper.findResources(likeQuery(query));
    }

    @Override
    public long countResources(AdminResourceQuery query) {
        return mapper.countResources(likeQuery(query));
    }

    @Override
    public AdminResource findResource(Long resourceId) {
        return mapper.findResource(resourceId);
    }

    @Override
    public Long createResource(AdminResourceCommand command, Long uploaderId, String resourceType,
                               String publicStatus, Integer currentVersion) {
        AdminResource resource = toResource(null, command, uploaderId, resourceType, publicStatus, currentVersion, null);
        mapper.insertResource(resource);
        mapper.insertResourceVersion(resource, uploaderId);
        return resource.getResourceId();
    }

    @Override
    public void updateResource(Long resourceId, AdminResourceCommand command, String resourceType, Integer nextVersion) {
        AdminResource existing = mapper.findResource(resourceId);
        Long uploaderId = existing == null ? null : existing.getUploaderId();
        Integer publicVersion = existing == null ? null : existing.getPublicVersion();
        AdminResource resource = toResource(resourceId, command, uploaderId, resourceType, "NOT_APPLIED", nextVersion, publicVersion);
        mapper.updateResource(resource);
        mapper.insertResourceVersion(resource, uploaderId);
    }

    @Override
    public void batchUpdate(AdminResourceBatchCommand command) {
        mapper.batchUpdate(command);
    }

    @Override
    public List<String> findCourseNamesUsingResources(List<Long> resourceIds) {
        return mapper.findCourseNamesUsingResources(resourceIds);
    }

    @Override
    public void deleteResources(List<Long> resourceIds) {
        mapper.deleteResources(resourceIds);
    }

    @Override
    public Long findPendingApplicationId(Long resourceId) {
        return mapper.findPendingApplicationId(resourceId);
    }

    @Override
    public Long createPublicApplication(Long resourceId, Integer resourceVersion, Long applicantId) {
        AdminPublicApplication application = new AdminPublicApplication();
        application.setResourceId(resourceId);
        application.setResourceVersion(resourceVersion);
        application.setApplicantId(applicantId);
        mapper.insertPublicApplication(application);
        return application.getApplicationId();
    }

    @Override
    public List<AdminPublicApplication> findPublicApplications(AdminResourceQuery query) {
        return mapper.findPublicApplications(likeQuery(query));
    }

    @Override
    public long countPublicApplications(AdminResourceQuery query) {
        return mapper.countPublicApplications(likeQuery(query));
    }

    @Override
    public AdminPublicApplication findPublicApplication(Long applicationId) {
        return mapper.findPublicApplication(applicationId);
    }

    @Override
    public List<AdminResource> findPublicResources(AdminResourceQuery query) {
        return mapper.findPublicResources(likeQuery(query));
    }

    @Override
    public long countPublicResources(AdminResourceQuery query) {
        return mapper.countPublicResources(likeQuery(query));
    }

    @Override
    public void reviewPublicApplication(Long applicationId, String status, String reviewComment, Long reviewerId) {
        mapper.reviewPublicApplication(applicationId, status, reviewComment, reviewerId);
    }

    @Override
    public Long publishResource(AdminPublicApplication application) {
        Long existingId = mapper.findPublicResourceId(application.getResourceId());
        if (existingId == null) {
            mapper.insertPublicResource(application);
            return application.getPublicResourceId();
        }
        mapper.updatePublicResource(existingId, application);
        return existingId;
    }

    @Override
    public void updateResourcePublicState(Long resourceId, String publicStatus, Integer publicVersion) {
        mapper.updateResourcePublicState(resourceId, publicStatus, publicVersion);
    }

    @Override
    public Long createNotification(String title, String content, Long sourceId) {
        AdminResourceNotification notification = new AdminResourceNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSourceId(sourceId);
        mapper.insertNotification(notification);
        return notification.getNotificationId();
    }

    @Override
    public void notifyAllStudents(Long notificationId) {
        mapper.notifyAllStudents(notificationId);
    }

    @Override
    public void appendResourceLog(Long resourceId, Long operatorId, String action, String content) {
        mapper.insertResourceLog(resourceId, operatorId, action, content);
    }

    @Override
    public List<AdminResourceLog> findResourceLogs(Long resourceId) {
        return mapper.findResourceLogs(resourceId);
    }

    private AdminResource toResource(Long resourceId, AdminResourceCommand command, Long uploaderId, String resourceType,
                                     String publicStatus, Integer currentVersion, Integer publicVersion) {
        AdminResource resource = new AdminResource();
        resource.setResourceId(resourceId);
        resource.setResourceName(command.getResourceName());
        resource.setResourceType(resourceType);
        resource.setCoverUrl(command.getCoverUrl());
        resource.setFileUrl(command.getFileUrl());
        resource.setPreviewUrl(command.getPreviewUrl());
        resource.setFileName(command.getFileName());
        resource.setFileSize(command.getFileSize());
        resource.setMajorId(command.getMajorId());
        resource.setCourseName(command.getCourseName());
        resource.setUploaderId(uploaderId);
        resource.setPublicStatus(publicStatus);
        resource.setCurrentVersion(currentVersion);
        resource.setPublicVersion(publicVersion);
        return resource;
    }

    private AdminResourceQuery likeQuery(AdminResourceQuery source) {
        AdminResourceQuery query = new AdminResourceQuery();
        query.setKeyword(like(source.getKeyword()));
        query.setResourceType(source.getResourceType());
        query.setMajorId(source.getMajorId());
        query.setCourseName(like(source.getCourseName()));
        query.setUploaderId(source.getUploaderId());
        query.setPublicStatus(source.getPublicStatus());
        query.setUploadStartDate(source.getUploadStartDate());
        query.setUploadEndDate(source.getUploadEndDate());
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
