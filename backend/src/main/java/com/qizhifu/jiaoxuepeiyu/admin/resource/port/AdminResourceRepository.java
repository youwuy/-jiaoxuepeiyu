package com.qizhifu.jiaoxuepeiyu.admin.resource.port;

import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicApplication;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResource;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceBatchCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceLog;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceQuery;
import java.util.List;

public interface AdminResourceRepository {

    List<AdminResource> findResources(AdminResourceQuery query);

    long countResources(AdminResourceQuery query);

    AdminResource findResource(Long resourceId);

    Long createResource(AdminResourceCommand command, Long uploaderId, String resourceType,
                        String publicStatus, Integer currentVersion);

    void updateResource(Long resourceId, AdminResourceCommand command, String resourceType, Integer nextVersion);

    void batchUpdate(AdminResourceBatchCommand command);

    List<String> findCourseNamesUsingResources(List<Long> resourceIds);

    void deleteResources(List<Long> resourceIds);

    Long findPendingApplicationId(Long resourceId);

    Long createPublicApplication(Long resourceId, Integer resourceVersion, Long applicantId);

    List<AdminPublicApplication> findPublicApplications(AdminResourceQuery query);

    long countPublicApplications(AdminResourceQuery query);

    AdminPublicApplication findPublicApplication(Long applicationId);

    List<AdminResource> findPublicResources(AdminResourceQuery query);

    long countPublicResources(AdminResourceQuery query);

    void reviewPublicApplication(Long applicationId, String status, String reviewComment, Long reviewerId);

    Long publishResource(AdminPublicApplication application);

    void updateResourcePublicState(Long resourceId, String publicStatus, Integer publicVersion);

    Long createNotification(String title, String content, Long sourceId);

    void notifyAllStudents(Long notificationId);

    void appendResourceLog(Long resourceId, Long operatorId, String action, String content);

    List<AdminResourceLog> findResourceLogs(Long resourceId);
}
