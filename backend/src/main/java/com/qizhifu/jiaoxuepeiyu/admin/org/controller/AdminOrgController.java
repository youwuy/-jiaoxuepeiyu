package com.qizhifu.jiaoxuepeiyu.admin.org.controller;

import com.qizhifu.jiaoxuepeiyu.admin.org.AdminOrgService;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
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
public class AdminOrgController {

    private final AdminOrgService service;

    public AdminOrgController(AdminOrgService service) {
        this.service = service;
    }

    @GetMapping("/tree")
    public ApiResponse<List<AdminOrg>> tree() {
        return ApiResponse.ok(service.getTree());
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody AdminOrgCommand body) {
        return ApiResponse.ok(service.create(body));
    }

    @PutMapping("/{orgId}")
    public ApiResponse<Void> update(@PathVariable Long orgId, @Valid @RequestBody AdminOrgCommand body) {
        service.update(orgId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orgId}/enable")
    public ApiResponse<Void> enable(@PathVariable Long orgId) {
        service.enable(orgId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orgId}/disable")
    public ApiResponse<Void> disable(@PathVariable Long orgId) {
        service.disable(orgId);
        return ApiResponse.ok(null);
    }
}
