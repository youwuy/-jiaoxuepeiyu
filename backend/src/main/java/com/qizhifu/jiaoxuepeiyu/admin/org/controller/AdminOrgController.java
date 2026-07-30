package com.qizhifu.jiaoxuepeiyu.admin.org.controller;

import com.qizhifu.jiaoxuepeiyu.admin.org.AdminOrgService;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @Operation(summary = "Get organization tree", description = "Returns the sorted organization tree with children nested under their parent nodes.")
    public ApiResponse<List<AdminOrg>> tree() {
        return ApiResponse.ok(service.getTree());
    }

    @PostMapping
    @Operation(summary = "Create organization", description = "Creates an organization node and returns the new organization id.")
    public ApiResponse<Long> create(@Valid @RequestBody AdminOrgCommand body) {
        return ApiResponse.ok(service.create(body));
    }

    @PutMapping("/{orgId}")
    @Operation(summary = "Update organization", description = "Updates an organization node and rejects self-parenting.")
    public ApiResponse<Void> update(@PathVariable Long orgId, @Valid @RequestBody AdminOrgCommand body) {
        service.update(orgId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orgId}/enable")
    @Operation(summary = "Enable organization", description = "Enables an organization node.")
    public ApiResponse<Void> enable(@PathVariable Long orgId) {
        service.enable(orgId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orgId}/disable")
    @Operation(summary = "Disable organization", description = "Disables an organization node.")
    public ApiResponse<Void> disable(@PathVariable Long orgId) {
        service.disable(orgId);
        return ApiResponse.ok(null);
    }
}
