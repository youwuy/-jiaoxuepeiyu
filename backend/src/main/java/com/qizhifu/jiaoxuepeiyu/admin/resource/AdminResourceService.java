package com.qizhifu.jiaoxuepeiyu.admin.resource;

import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicApplication;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicReviewCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResource;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceBatchCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceLog;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceQuery;
import com.qizhifu.jiaoxuepeiyu.admin.resource.port.AdminResourceRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminResourceService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final long MAX_RESOURCE_FILE_BYTES = 200L * 1024L * 1024L;
    private static final Set<String> DOCUMENT_EXTENSIONS = new HashSet<String>(Arrays.asList("pdf", "doc", "docx", "xls", "xlsx"));
    private static final Set<String> PRESENTATION_EXTENSIONS = new HashSet<String>(Arrays.asList("ppt", "pptx"));
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<String>(Arrays.asList("jpg", "jpeg", "png", "gif"));
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<String>(Arrays.asList("mp4", "mov", "avi", "flv", "wmv"));
    private static final Set<String> AUDIO_EXTENSIONS = new HashSet<String>(Arrays.asList("mp3", "wav", "wma"));

    private final AdminResourceRepository repository;

    public AdminResourceService(AdminResourceRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdminResource> listResources(AdminResourceQuery query) {
        AdminResourceQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminResource>(
                repository.findResources(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countResources(normalized));
    }

    public AdminResource getResource(Long resourceId) {
        AdminResource resource = repository.findResource(resourceId);
        if (resource == null) {
            throw new BusinessException(404, "Resource not found");
        }
        return resource;
    }

    @Transactional
    public Long createResource(AdminResourceCommand command, Long uploaderId) {
        requireOperator(uploaderId);
        AdminResourceCommand normalized = normalizedResource(command, true);
        String resourceType = detectResourceType(normalized.getFileName(), normalized.getFileUrl());
        Long resourceId = repository.createResource(normalized, uploaderId, resourceType, "NOT_APPLIED", 1);
        repository.appendResourceLog(resourceId, uploaderId, "CREATE", "Create resource");
        return resourceId;
    }

    @Transactional
    public void updateResource(Long resourceId, AdminResourceCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminResource existing = getResource(resourceId);
        AdminResourceCommand normalized = normalizedResource(command, true);
        String resourceType = detectResourceType(normalized.getFileName(), normalized.getFileUrl());
        int nextVersion = nullToOne(existing.getCurrentVersion()) + 1;
        String nextPublicStatus = existing.getPublicVersion() == null ? "NOT_APPLIED" : "PUBLIC";
        repository.updateResource(resourceId, normalized, resourceType, nextVersion);
        repository.updateResourcePublicState(resourceId, nextPublicStatus, existing.getPublicVersion());
        repository.appendResourceLog(resourceId, operatorId, "UPDATE", "Update resource");
    }

    @Transactional
    public void batchUpdate(AdminResourceBatchCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminResourceBatchCommand normalized = normalizedBatch(command);
        repository.batchUpdate(normalized);
        for (Long resourceId : normalized.getResourceIds()) {
            repository.appendResourceLog(resourceId, operatorId, "BATCH_UPDATE", "Batch update resource");
        }
    }

    @Transactional
    public void deleteResources(List<Long> resourceIds, Long operatorId) {
        requireOperator(operatorId);
        List<Long> ids = normalizedIds(resourceIds);
        List<String> courseNames = repository.findCourseNamesUsingResources(ids);
        if (courseNames != null && !courseNames.isEmpty()) {
            throw new BusinessException(400, "Selected resources are used by courses: " + join(courseNames));
        }
        repository.deleteResources(ids);
        for (Long resourceId : ids) {
            repository.appendResourceLog(resourceId, operatorId, "DELETE", "Delete resource");
        }
    }

    @Transactional
    public Long submitPublicApplication(Long resourceId, Long applicantId) {
        requireOperator(applicantId);
        AdminResource resource = getResource(resourceId);
        if (resource.getPublicVersion() != null
                && resource.getCurrentVersion() != null
                && resource.getCurrentVersion().equals(resource.getPublicVersion())) {
            throw new BusinessException(400, "Resource current version is already public");
        }
        Long pendingId = repository.findPendingApplicationId(resourceId);
        if (pendingId != null) {
            throw new BusinessException(400, "Resource already has a pending public application");
        }
        int version = nullToOne(resource.getCurrentVersion());
        Long applicationId = repository.createPublicApplication(resourceId, version, applicantId);
        repository.updateResourcePublicState(resourceId, "PENDING", resource.getPublicVersion());
        repository.appendResourceLog(resourceId, applicantId,
                isPublicVersionChanged(resource) ? "APPLY_PUBLIC_LATEST" : "APPLY_PUBLIC",
                "Submit public application");
        return applicationId;
    }

    public PageResponse<AdminPublicApplication> listPublicApplications(AdminResourceQuery query) {
        AdminResourceQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminPublicApplication>(
                repository.findPublicApplications(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countPublicApplications(normalized));
    }

    public AdminPublicApplication getPublicApplication(Long applicationId) {
        AdminPublicApplication application = repository.findPublicApplication(applicationId);
        if (application == null) {
            throw new BusinessException(404, "Public application not found");
        }
        return application;
    }

    public PageResponse<AdminResource> listPublicResources(AdminResourceQuery query) {
        AdminResourceQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminResource>(
                repository.findPublicResources(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countPublicResources(normalized));
    }

    public List<AdminResourceLog> listResourceLogs(Long resourceId) {
        getResource(resourceId);
        return repository.findResourceLogs(resourceId);
    }

    @Transactional
    public void approveApplication(Long applicationId, AdminPublicReviewCommand command, Long reviewerId) {
        requireOperator(reviewerId);
        AdminPublicApplication application = requirePendingApplication(applicationId);
        String reviewComment = trimToNull(command == null ? null : command.getReviewComment());
        repository.reviewPublicApplication(applicationId, "APPROVED", reviewComment, reviewerId);
        Long publicResourceId = repository.publishResource(application);
        repository.updateResourcePublicState(application.getResourceId(), "PUBLIC", application.getResourceVersion());
        Long notificationId = repository.createNotification("New public resource",
                "Resource \"" + application.getResourceName() + "\" is now public.", publicResourceId);
        repository.notifyAllStudents(notificationId);
        repository.appendResourceLog(application.getResourceId(), reviewerId, "AUDIT_APPROVE", "Approve public application");
    }

    @Transactional
    public void rejectApplication(Long applicationId, AdminPublicReviewCommand command, Long reviewerId) {
        requireOperator(reviewerId);
        AdminPublicApplication application = requirePendingApplication(applicationId);
        String reviewComment = trimToNull(command == null ? null : command.getReviewComment());
        if (reviewComment == null) {
            throw new BusinessException(400, "Rejecting a resource requires a review comment");
        }
        AdminResource resource = getResource(application.getResourceId());
        repository.reviewPublicApplication(applicationId, "REJECTED", reviewComment, reviewerId);
        repository.updateResourcePublicState(application.getResourceId(), "REJECTED", resource.getPublicVersion());
        repository.appendResourceLog(application.getResourceId(), reviewerId, "AUDIT_REJECT", "Reject public application");
    }

    private AdminPublicApplication requirePendingApplication(Long applicationId) {
        AdminPublicApplication application = getPublicApplication(applicationId);
        if (!"PENDING".equals(application.getPublicStatus())) {
            throw new BusinessException(400, "Only pending applications can be reviewed");
        }
        return application;
    }

    private AdminResourceCommand normalizedResource(AdminResourceCommand command, boolean requireFile) {
        if (command == null || !InputValidator.hasText(command.getResourceName())) {
            throw new BusinessException(400, "Resource name is required");
        }
        String resourceName = command.getResourceName().trim();
        if (resourceName.length() > 20) {
            throw new BusinessException(400, "Resource name cannot exceed 20 characters");
        }
        if (!InputValidator.hasText(command.getCoverUrl())) {
            throw new BusinessException(400, "Cover image is required");
        }
        if (requireFile && (!InputValidator.hasText(command.getFileUrl()) || !InputValidator.hasText(command.getFileName()))) {
            throw new BusinessException(400, "Resource file is required");
        }
        if (command.getFileSize() == null || command.getFileSize().longValue() <= 0) {
            throw new BusinessException(400, "Resource file size is required");
        }
        if (command.getFileSize().longValue() > MAX_RESOURCE_FILE_BYTES) {
            throw new BusinessException(400, "Resource file cannot exceed 200MB");
        }
        if (command.getMajorId() == null) {
            throw new BusinessException(400, "Major is required");
        }
        String courseName = trimToNull(command.getCourseName());
        if (courseName != null && courseName.length() > 30) {
            throw new BusinessException(400, "Course name cannot exceed 30 characters");
        }
        AdminResourceCommand normalized = new AdminResourceCommand();
        normalized.setResourceName(resourceName);
        normalized.setCoverUrl(command.getCoverUrl().trim());
        normalized.setFileUrl(command.getFileUrl().trim());
        normalized.setPreviewUrl(trimToNull(command.getPreviewUrl()));
        normalized.setFileName(command.getFileName().trim());
        normalized.setFileSize(command.getFileSize());
        normalized.setMajorId(command.getMajorId());
        normalized.setCourseName(courseName);
        return normalized;
    }

    private AdminResourceBatchCommand normalizedBatch(AdminResourceBatchCommand command) {
        if (command == null) {
            throw new BusinessException(400, "Batch update data is required");
        }
        List<Long> ids = normalizedIds(command.getResourceIds());
        String coverUrl = trimToNull(command.getCoverUrl());
        String courseName = trimToNull(command.getCourseName());
        if (courseName != null && courseName.length() > 30) {
            throw new BusinessException(400, "Course name cannot exceed 30 characters");
        }
        if (coverUrl == null && command.getMajorId() == null && courseName == null) {
            throw new BusinessException(400, "At least one batch field is required");
        }
        AdminResourceBatchCommand normalized = new AdminResourceBatchCommand();
        normalized.setResourceIds(ids);
        normalized.setCoverUrl(coverUrl);
        normalized.setMajorId(command.getMajorId());
        normalized.setCourseName(courseName);
        return normalized;
    }

    private AdminResourceQuery normalizedQuery(AdminResourceQuery query) {
        AdminResourceQuery normalized = new AdminResourceQuery();
        if (query != null) {
            normalized.setKeyword(trimToNull(query.getKeyword()));
            normalized.setResourceType(trimToNull(query.getResourceType()));
            normalized.setMajorId(query.getMajorId());
            normalized.setCourseName(trimToNull(query.getCourseName()));
            normalized.setUploaderId(query.getUploaderId());
            normalized.setPublicStatus(trimToNull(query.getPublicStatus()));
            normalized.setUploadStartDate(trimToNull(query.getUploadStartDate()));
            normalized.setUploadEndDate(trimToNull(query.getUploadEndDate()));
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(MAX_PAGE_SIZE);
        }
        return normalized;
    }

    private List<Long> normalizedIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "Resource ids are required");
        }
        List<Long> normalized = new ArrayList<Long>();
        for (Long id : ids) {
            if (id != null && id.longValue() > 0 && !normalized.contains(id)) {
                normalized.add(id);
            }
        }
        if (normalized.isEmpty()) {
            throw new BusinessException(400, "Resource ids are required");
        }
        return normalized;
    }

    private String detectResourceType(String fileName, String fileUrl) {
        String source = InputValidator.hasText(fileName) ? fileName : fileUrl;
        int index = source.lastIndexOf('.');
        if (index < 0 || index == source.length() - 1) {
            throw new BusinessException(400, "Resource file type is not supported");
        }
        String extension = source.substring(index + 1).toLowerCase(Locale.ENGLISH);
        if (DOCUMENT_EXTENSIONS.contains(extension)) {
            return "DOCUMENT";
        }
        if (PRESENTATION_EXTENSIONS.contains(extension)) {
            return "PRESENTATION";
        }
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return "IMAGE";
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return "VIDEO";
        }
        if (AUDIO_EXTENSIONS.contains(extension)) {
            return "AUDIO";
        }
        throw new BusinessException(400, "Resource file type is not supported");
    }

    private void requireOperator(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(401, "Missing admin identity");
        }
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }

    private int nullToOne(Integer value) {
        return value == null || value.intValue() < 1 ? 1 : value.intValue();
    }

    private boolean isPublicVersionChanged(AdminResource resource) {
        return resource.getPublicVersion() != null
                && resource.getCurrentVersion() != null
                && !resource.getCurrentVersion().equals(resource.getPublicVersion());
    }

    private String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
