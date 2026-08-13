package com.qizhifu.jiaoxuepeiyu.admin.resource.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.resource.AdminResourceService;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicApplication;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminPublicReviewCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResource;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceBatchCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceIdsCommand;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceLog;
import com.qizhifu.jiaoxuepeiyu.admin.resource.model.AdminResourceQuery;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Resources", description = "Personal resources, public review applications, and public resource library APIs.")
public class AdminResourceController {

    private final AdminResourceService service;

    public AdminResourceController(AdminResourceService service) {
        this.service = service;
    }

    @GetMapping("/resources")
    @Operation(summary = "List personal resources", description = "Returns paged personal resources filtered by name, type, major, course, uploader, status, or upload date.")
    public ApiResponse<PageResponse<AdminResource>> listResources(@ModelAttribute AdminResourceQuery query,
                                                                   HttpServletRequest request) {
        return ApiResponse.ok(service.listPersonalResources(query, AdminContext.requireAdminId(request)));
    }

    @GetMapping("/resources/{resourceId}")
    @Operation(summary = "Get resource detail", description = "Returns one personal resource with file metadata and public version state.")
    public ApiResponse<AdminResource> getResource(@PathVariable Long resourceId, HttpServletRequest request) {
        return ApiResponse.ok(service.getPersonalResource(resourceId, AdminContext.requireAdminId(request)));
    }

    @PostMapping("/resources")
    @Operation(summary = "Create resource", description = "Creates resource metadata, detects type from file suffix, and starts with NOT_APPLIED public status.")
    public ApiResponse<Long> createResource(@RequestBody AdminResourceCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.createResource(body, AdminContext.requireAdminId(request)));
    }

    @PutMapping("/resources/{resourceId}")
    @Operation(summary = "Update resource", description = "Updates resource metadata, creates a new version, and preserves older public version state when applicable.")
    public ApiResponse<Void> updateResource(@PathVariable Long resourceId,
                                            @RequestBody AdminResourceCommand body,
                                            HttpServletRequest request) {
        service.updatePersonalResource(resourceId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PutMapping("/resources/batch")
    @Operation(summary = "Batch update resources", description = "Batch updates selected resources using only provided cover, major, or course fields.")
    public ApiResponse<Void> batchUpdate(@RequestBody AdminResourceBatchCommand body, HttpServletRequest request) {
        service.batchUpdatePersonalResources(body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/resources/batch/delete")
    @Operation(summary = "Batch delete resources", description = "Soft deletes resources after checking that selected resources are not bound to courses.")
    public ApiResponse<Void> deleteResources(@RequestBody AdminResourceIdsCommand body, HttpServletRequest request) {
        service.deletePersonalResources(body == null ? null : body.getResourceIds(), AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/resources/{resourceId}/public-applications")
    @Operation(summary = "Submit public application", description = "Submits the resource current version for public review and marks the resource as PENDING.")
    public ApiResponse<Long> submitPublicApplication(@PathVariable Long resourceId, HttpServletRequest request) {
        return ApiResponse.ok(service.submitPersonalPublicApplication(resourceId, AdminContext.requireAdminId(request)));
    }

    @GetMapping("/resources/{resourceId}/logs")
    @Operation(summary = "List resource operation logs", description = "Returns operation logs for one resource sorted by newest first.")
    public ApiResponse<List<AdminResourceLog>> listResourceLogs(@PathVariable Long resourceId,
                                                                 HttpServletRequest request) {
        return ApiResponse.ok(service.listPersonalResourceLogs(resourceId, AdminContext.requireAdminId(request)));
    }

    @GetMapping("/public-applications")
    @Operation(summary = "List public applications", description = "Returns paged resource public applications filtered by resource fields or review status.")
    public ApiResponse<PageResponse<AdminPublicApplication>> listPublicApplications(@ModelAttribute AdminResourceQuery query) {
        return ApiResponse.ok(service.listPublicApplications(query));
    }

    @GetMapping("/public-applications/{applicationId}")
    @Operation(summary = "Get public application detail", description = "Returns one public review application with the submitted resource version snapshot.")
    public ApiResponse<AdminPublicApplication> getPublicApplication(@PathVariable Long applicationId) {
        return ApiResponse.ok(service.getPublicApplication(applicationId));
    }

    @PostMapping("/public-applications/{applicationId}/approve")
    @Operation(summary = "Approve public application", description = "Approves a pending application, publishes the submitted version, and notifies all enabled students.")
    public ApiResponse<Void> approvePublicApplication(@PathVariable Long applicationId,
                                                      @RequestBody AdminPublicReviewCommand body,
                                                      HttpServletRequest request) {
        service.approveApplication(applicationId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/public-applications/{applicationId}/reject")
    @Operation(summary = "Reject public application", description = "Rejects a pending application; review comment is required for rejection.")
    public ApiResponse<Void> rejectPublicApplication(@PathVariable Long applicationId,
                                                     @RequestBody AdminPublicReviewCommand body,
                                                     HttpServletRequest request) {
        service.rejectApplication(applicationId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @GetMapping("/public-resources")
    @Operation(summary = "List public resources", description = "Returns paged approved public resources; only the latest approved version per source resource is shown.")
    public ApiResponse<PageResponse<AdminResource>> listPublicResources(@ModelAttribute AdminResourceQuery query) {
        return ApiResponse.ok(service.listPublicResources(query));
    }
}
