package com.qizhifu.jiaoxuepeiyu.admin.org.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.org.AdminOrgService;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgSortCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/org")
@Tag(name = "Admin Organization", description = "Organization tree management APIs.")
public class AdminOrgController {

    private final AdminOrgService service;

    public AdminOrgController(AdminOrgService service) {
        this.service = service;
    }

    @GetMapping("/tree")
    @Operation(summary = "Get organization tree", description = "Returns the sorted organization tree with optional fuzzy name search. Set enabledOnly=true for parent organization picker options.")
    public ApiResponse<List<AdminOrg>> tree(@Parameter(description = "Optional fuzzy organization name keyword.")
                                            @RequestParam(value = "keyword", required = false) String keyword,
                                            @Parameter(description = "When true, returns enabled organizations only.")
                                            @RequestParam(value = "enabledOnly", required = false, defaultValue = "false") boolean enabledOnly) {
        return ApiResponse.ok(service.getTree(keyword, enabledOnly));
    }

    @PostMapping
    @Operation(summary = "Create organization", description = "Creates an organization node and returns the new organization id.")
    public ApiResponse<Long> create(@Valid @RequestBody AdminOrgCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.create(body, AdminContext.requireAdminId(request)));
    }

    @PutMapping("/{orgId}")
    @Operation(summary = "Update organization", description = "Updates organization name and sort order while preserving its parent node.")
    public ApiResponse<Void> update(@PathVariable Long orgId,
                                    @Valid @RequestBody AdminOrgCommand body,
                                    HttpServletRequest request) {
        service.update(orgId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PutMapping("/sort")
    @Operation(summary = "Update organization sort order", description = "Persists drag-and-drop sort weights for organization nodes.")
    public ApiResponse<Void> updateSort(@RequestBody List<AdminOrgSortCommand> body, HttpServletRequest request) {
        service.updateSort(body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orgId}/enable")
    @Operation(summary = "Enable organization", description = "Enables the selected organization node only.")
    public ApiResponse<Void> enable(@PathVariable Long orgId, HttpServletRequest request) {
        service.enable(orgId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orgId}/disable")
    @Operation(summary = "Disable organization", description = "Disables the selected organization node and all descendant nodes.")
    public ApiResponse<Void> disable(@PathVariable Long orgId, HttpServletRequest request) {
        service.disable(orgId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }
}
