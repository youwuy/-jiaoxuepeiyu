package com.qizhifu.jiaoxuepeiyu.admin.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicApplication;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicReviewCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResource;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceBatchCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceQuery;
import com.qizhifu.jiaoxuepeiyu.admin.resource.port.AdminResourceRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminResourceServiceTests {

    @Test
    void createsResourceWithDetectedTypeAndDraftStatus() {
        FakeResources repository = new FakeResources();
        AdminResourceService service = new AdminResourceService(repository);

        Long resourceId = service.createResource(command("Safety Training", "intro.mp4"), 9L);

        assertEquals(30L, resourceId.longValue());
        assertEquals("Safety Training", repository.savedCommand.getResourceName());
        assertEquals("VIDEO", repository.savedType);
        assertEquals("NOT_APPLIED", repository.savedPublicStatus);
        assertEquals(1, repository.savedVersion);
        assertEquals("CREATE", repository.lastLogAction);
    }

    @Test
    void rejectsUnsupportedResourceFileType() {
        AdminResourceService service = new AdminResourceService(new FakeResources());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createResource(command("Bad", "intro.exe"), 9L);
        });

        assertEquals("Resource file type is not supported", exception.getMessage());
    }

    @Test
    void batchUpdatesOnlyProvidedFields() {
        FakeResources repository = new FakeResources();
        AdminResourceService service = new AdminResourceService(repository);
        AdminResourceBatchCommand command = new AdminResourceBatchCommand();
        command.setResourceIds(Arrays.asList(1L, 2L));
        command.setCoverUrl(" https://cdn.example/cover.png ");
        command.setMajorId(3L);

        service.batchUpdate(command, 9L);

        assertEquals(Arrays.asList(1L, 2L), repository.batchCommand.getResourceIds());
        assertEquals("https://cdn.example/cover.png", repository.batchCommand.getCoverUrl());
        assertEquals(3L, repository.batchCommand.getMajorId().longValue());
        assertEquals(null, repository.batchCommand.getCourseName());
        assertEquals("BATCH_UPDATE", repository.lastLogAction);
    }

    @Test
    void rejectsDeletingResourcesUsedByCourses() {
        FakeResources repository = new FakeResources();
        repository.usedCourseNames = Arrays.asList("Rail Safety");
        AdminResourceService service = new AdminResourceService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.deleteResources(Arrays.asList(1L), 9L);
        });

        assertEquals("Selected resources are used by courses: Rail Safety", exception.getMessage());
    }

    @Test
    void submitsCurrentVersionForPublicReview() {
        FakeResources repository = new FakeResources();
        repository.resource = resource(40L, "NOT_APPLIED", 2, null);
        AdminResourceService service = new AdminResourceService(repository);

        Long applicationId = service.submitPublicApplication(40L, 9L);

        assertEquals(50L, applicationId.longValue());
        assertEquals(40L, repository.applicationResourceId.longValue());
        assertEquals(2, repository.applicationVersion.intValue());
        assertEquals("PENDING", repository.updatedPublicStatus);
        assertEquals("APPLY_PUBLIC", repository.lastLogAction);
    }

    @Test
    void rejectsDuplicatePendingPublicApplication() {
        FakeResources repository = new FakeResources();
        repository.resource = resource(40L, "PENDING", 2, null);
        repository.pendingApplicationId = 51L;
        AdminResourceService service = new AdminResourceService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.submitPublicApplication(40L, 9L);
        });

        assertEquals("Resource already has a pending public application", exception.getMessage());
    }

    @Test
    void rejectsPublicApplicationWhenCurrentVersionAlreadyPublic() {
        FakeResources repository = new FakeResources();
        repository.resource = resource(40L, "PUBLIC", 2, 2);
        AdminResourceService service = new AdminResourceService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.submitPublicApplication(40L, 9L);
        });

        assertEquals("Resource current version is already public", exception.getMessage());
    }

    @Test
    void approvesApplicationAndPublishesLatestVersion() {
        FakeResources repository = new FakeResources();
        repository.application = application(50L, 40L, 2, "PENDING");
        AdminResourceService service = new AdminResourceService(repository);
        AdminPublicReviewCommand command = new AdminPublicReviewCommand();
        command.setReviewComment("OK");

        service.approveApplication(50L, command, 7L);

        assertEquals("APPROVED", repository.reviewStatus);
        assertEquals("PUBLIC", repository.updatedPublicStatus);
        assertEquals(2, repository.updatedPublicVersion.intValue());
        assertEquals(101L, repository.notificationSourceId.longValue());
        assertEquals("AUDIT_APPROVE", repository.lastLogAction);
    }

    @Test
    void rejectsApplicationWithoutReviewComment() {
        FakeResources repository = new FakeResources();
        repository.application = application(50L, 40L, 2, "PENDING");
        AdminResourceService service = new AdminResourceService(repository);
        AdminPublicReviewCommand command = new AdminPublicReviewCommand();
        command.setReviewComment(" ");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.rejectApplication(50L, command, 7L);
        });

        assertEquals("Rejecting a resource requires a review comment", exception.getMessage());
    }

    private AdminResourceCommand command(String name, String fileName) {
        AdminResourceCommand command = new AdminResourceCommand();
        command.setResourceName(" " + name + " ");
        command.setCoverUrl("https://cdn.example/cover.png");
        command.setFileUrl("https://cdn.example/" + fileName);
        command.setPreviewUrl("https://cdn.example/preview/" + fileName);
        command.setFileName(fileName);
        command.setFileSize(1024L);
        command.setMajorId(3L);
        command.setCourseName(" Train Ops ");
        return command;
    }

    private AdminResource resource(Long resourceId, String publicStatus, Integer currentVersion, Integer publicVersion) {
        AdminResource resource = new AdminResource();
        resource.setResourceId(resourceId);
        resource.setResourceName("Safety Training");
        resource.setResourceType("VIDEO");
        resource.setCoverUrl("cover");
        resource.setFileUrl("file");
        resource.setPreviewUrl("preview");
        resource.setFileName("intro.mp4");
        resource.setMajorId(3L);
        resource.setUploaderId(9L);
        resource.setPublicStatus(publicStatus);
        resource.setCurrentVersion(currentVersion);
        resource.setPublicVersion(publicVersion);
        return resource;
    }

    private AdminPublicApplication application(Long applicationId, Long resourceId, Integer version, String status) {
        AdminPublicApplication application = new AdminPublicApplication();
        application.setApplicationId(applicationId);
        application.setResourceId(resourceId);
        application.setResourceVersion(version);
        application.setResourceName("Safety Training");
        application.setResourceType("VIDEO");
        application.setPublicStatus(status);
        return application;
    }

    private static class FakeResources implements AdminResourceRepository {
        private AdminResourceCommand savedCommand;
        private String savedType;
        private String savedPublicStatus;
        private Integer savedVersion;
        private AdminResourceBatchCommand batchCommand;
        private List<String> usedCourseNames = new ArrayList<String>();
        private AdminResource resource;
        private Long pendingApplicationId;
        private Long applicationResourceId;
        private Integer applicationVersion;
        private AdminPublicApplication application;
        private String reviewStatus;
        private String updatedPublicStatus;
        private Integer updatedPublicVersion;
        private Long notificationSourceId;
        private String lastLogAction;

        @Override
        public List<AdminResource> findResources(AdminResourceQuery query) {
            return new ArrayList<AdminResource>();
        }

        @Override
        public long countResources(AdminResourceQuery query) {
            return 0;
        }

        @Override
        public AdminResource findResource(Long resourceId) {
            return resource;
        }

        @Override
        public Long createResource(AdminResourceCommand command, Long uploaderId, String resourceType,
                                   String publicStatus, Integer currentVersion) {
            this.savedCommand = command;
            this.savedType = resourceType;
            this.savedPublicStatus = publicStatus;
            this.savedVersion = currentVersion;
            return 30L;
        }

        @Override
        public void updateResource(Long resourceId, AdminResourceCommand command, String resourceType,
                                   Integer nextVersion) {
        }

        @Override
        public void batchUpdate(AdminResourceBatchCommand command) {
            this.batchCommand = command;
        }

        @Override
        public List<String> findCourseNamesUsingResources(List<Long> resourceIds) {
            return usedCourseNames;
        }

        @Override
        public void deleteResources(List<Long> resourceIds) {
        }

        @Override
        public Long findPendingApplicationId(Long resourceId) {
            return pendingApplicationId;
        }

        @Override
        public Long createPublicApplication(Long resourceId, Integer resourceVersion, Long applicantId) {
            this.applicationResourceId = resourceId;
            this.applicationVersion = resourceVersion;
            return 50L;
        }

        @Override
        public List<AdminPublicApplication> findPublicApplications(AdminResourceQuery query) {
            return new ArrayList<AdminPublicApplication>();
        }

        @Override
        public long countPublicApplications(AdminResourceQuery query) {
            return 0;
        }

        @Override
        public AdminPublicApplication findPublicApplication(Long applicationId) {
            return application;
        }

        @Override
        public List<AdminResource> findPublicResources(AdminResourceQuery query) {
            return new ArrayList<AdminResource>();
        }

        @Override
        public long countPublicResources(AdminResourceQuery query) {
            return 0;
        }

        @Override
        public void reviewPublicApplication(Long applicationId, String status, String reviewComment, Long reviewerId) {
            this.reviewStatus = status;
        }

        @Override
        public Long publishResource(AdminPublicApplication application) {
            return 101L;
        }

        @Override
        public void updateResourcePublicState(Long resourceId, String publicStatus, Integer publicVersion) {
            this.updatedPublicStatus = publicStatus;
            this.updatedPublicVersion = publicVersion;
        }

        @Override
        public Long createNotification(String title, String content, Long sourceId) {
            this.notificationSourceId = sourceId;
            return 88L;
        }

        @Override
        public void notifyAllStudents(Long notificationId) {
        }

        @Override
        public void appendResourceLog(Long resourceId, Long operatorId, String action, String content) {
            this.lastLogAction = action;
        }

        @Override
        public List<com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceLog> findResourceLogs(Long resourceId) {
            return new ArrayList<com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceLog>();
        }
    }
}
