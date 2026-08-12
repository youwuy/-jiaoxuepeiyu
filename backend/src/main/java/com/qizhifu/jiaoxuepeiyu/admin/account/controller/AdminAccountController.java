package com.qizhifu.jiaoxuepeiyu.admin.account.controller;

import com.qizhifu.jiaoxuepeiyu.admin.account.AdminAccountService;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AccountIdsCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountExportRow;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountImportResult;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.BatchOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.RoleBindingCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.export.CsvExporter;
import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.iam.AdminDataScopeContext;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminDataScopeAccess;
import javax.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
@Tag(name = "Admin Account", description = "Teacher and student account management APIs.")
public class AdminAccountController {

    private final AdminAccountService service;

    public AdminAccountController(AdminAccountService service) {
        this.service = service;
    }

    @GetMapping("/teachers")
    @Operation(summary = "List teacher accounts", description = "Returns paged teacher accounts with masked sensitive fields.")
    public ApiResponse<PageResponse<AdminAccount>> listTeachers(@ModelAttribute AdminAccountQuery query, HttpServletRequest request) {
        applyDataScope(query, request);
        return ApiResponse.ok(service.listTeachers(query));
    }

    @GetMapping("/students")
    @Operation(summary = "List student accounts", description = "Returns paged student accounts with class, organization, and masked sensitive fields.")
    public ApiResponse<PageResponse<AdminAccount>> listStudents(@ModelAttribute AdminAccountQuery query, HttpServletRequest request) {
        applyDataScope(query, request);
        return ApiResponse.ok(service.listStudents(query));
    }

    @PostMapping("/teachers/import/preview")
    @Operation(summary = "Preview teacher account import", description = "Validates parsed teacher account rows before submission and returns row-level errors.")
    public ApiResponse<AdminAccountImportPreview> previewTeacherImport(@RequestBody AdminAccountImportCommand body) {
        return ApiResponse.ok(service.previewImport("teacher", body));
    }

    @PostMapping("/teachers/import")
    @Operation(summary = "Import teacher accounts", description = "Creates teacher accounts from validated parsed rows using the configured initial password hash.")
    public ApiResponse<AdminAccountImportResult> importTeachers(@RequestBody AdminAccountImportCommand body) {
        return ApiResponse.ok(service.importAccounts("teacher", body));
    }

    @GetMapping("/teachers/export")
    @Operation(summary = "Export teacher accounts", description = "Returns export-ready teacher account rows with masked sensitive fields.")
    public ApiResponse<List<AdminAccountExportRow>> exportTeachers(@ModelAttribute AdminAccountQuery query, HttpServletRequest request) {
        applyDataScope(query, request);
        return ApiResponse.ok(service.exportAccounts("teacher", query));
    }

    @GetMapping("/teachers/export/file")
    @Operation(summary = "Download teacher account CSV", description = "Downloads teacher account rows as an Excel-compatible CSV file with masked sensitive fields.")
    public ResponseEntity<byte[]> exportTeacherFile(@ModelAttribute AdminAccountQuery query) {
        return accountCsv("teacher-accounts.csv", service.exportAccounts("teacher", query));
    }

    @PostMapping("/students/import/preview")
    @Operation(summary = "Preview student account import", description = "Validates parsed student account rows before submission and returns row-level errors.")
    public ApiResponse<AdminAccountImportPreview> previewStudentImport(@RequestBody AdminAccountImportCommand body) {
        return ApiResponse.ok(service.previewImport("student", body));
    }

    @PostMapping("/students/import")
    @Operation(summary = "Import student accounts", description = "Creates student accounts from validated parsed rows using the configured initial password hash.")
    public ApiResponse<AdminAccountImportResult> importStudents(@RequestBody AdminAccountImportCommand body) {
        return ApiResponse.ok(service.importAccounts("student", body));
    }

    @GetMapping("/students/export")
    @Operation(summary = "Export student accounts", description = "Returns export-ready student account rows with masked sensitive fields.")
    public ApiResponse<List<AdminAccountExportRow>> exportStudents(@ModelAttribute AdminAccountQuery query, HttpServletRequest request) {
        applyDataScope(query, request);
        return ApiResponse.ok(service.exportAccounts("student", query));
    }

    @GetMapping("/students/export/file")
    @Operation(summary = "Download student account CSV", description = "Downloads student account rows as an Excel-compatible CSV file with masked sensitive fields.")
    public ResponseEntity<byte[]> exportStudentFile(@ModelAttribute AdminAccountQuery query) {
        return accountCsv("student-accounts.csv", service.exportAccounts("student", query));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get account detail", description = "Returns one account detail with phone and ID card masked.")
    public ApiResponse<AdminAccount> get(@PathVariable Long userId, HttpServletRequest request) {
        AdminDataScopeAccess access = AdminDataScopeContext.get(request);
        if (access == null) {
            return ApiResponse.ok(service.get(userId));
        }
        return ApiResponse.ok(service.get(userId, AdminContext.requireAdminId(request),
                access.getDataScope(), access.getManagedOrgIds()));
    }

    @PostMapping("/teachers")
    @Operation(summary = "Create teacher account", description = "Creates a teacher account with hashed configured initial password and returns the new user id.")
    public ApiResponse<Long> createTeacher(@RequestBody AdminAccountCommand body) {
        return ApiResponse.ok(service.createTeacher(body));
    }

    @PostMapping("/students")
    @Operation(summary = "Create student account", description = "Creates a student account bound to a class and returns the new user id.")
    public ApiResponse<Long> createStudent(@RequestBody AdminAccountCommand body) {
        return ApiResponse.ok(service.createStudent(body));
    }

    @PutMapping("/teachers/{userId}")
    @Operation(summary = "Update teacher account", description = "Updates editable teacher profile fields and replaces role, managed organization, and teaching class bindings.")
    public ApiResponse<Void> updateTeacher(@PathVariable Long userId, @RequestBody AdminAccountCommand body) {
        service.updateTeacher(userId, body);
        return ApiResponse.ok(null);
    }

    @PutMapping("/students/{userId}")
    @Operation(summary = "Update student account", description = "Updates editable student profile fields without changing the account number.")
    public ApiResponse<Void> updateStudent(@PathVariable Long userId, @RequestBody AdminAccountCommand body) {
        service.updateStudent(userId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/enable")
    @Operation(summary = "Enable account", description = "Enables login for the selected account.")
    public ApiResponse<Void> enable(@PathVariable Long userId) {
        service.enable(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/disable")
    @Operation(summary = "Disable account", description = "Disables login for the selected account.")
    public ApiResponse<Void> disable(@PathVariable Long userId) {
        service.disable(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/reset-password")
    @Operation(summary = "Batch reset passwords", description = "Resets selected accounts to the submitted password hash without returning plaintext passwords.")
    public ApiResponse<Void> resetPasswords(@RequestBody AccountIdsCommand body) {
        service.resetPasswords(body.getUserIds(), body.getPassword());
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/org")
    @Operation(summary = "Batch update organization", description = "Updates the belonging organization for selected accounts.")
    public ApiResponse<Void> updateOrg(@RequestBody BatchOrgCommand body) {
        service.updateOrg(body.getUserIds(), body.getOrgId());
        return ApiResponse.ok(null);
    }

    @PutMapping("/teachers/{userId}/roles")
    @Operation(summary = "Update teacher roles", description = "Replaces the teacher role bindings and returns an empty success response.")
    public ApiResponse<Void> updateRoles(@PathVariable Long userId, @RequestBody RoleBindingCommand body) {
        service.updateRoles(userId, body.getRoleIds());
        return ApiResponse.ok(null);
    }

    private ResponseEntity<byte[]> accountCsv(String filename, List<AdminAccountExportRow> rows) {
        List<List<String>> csvRows = new ArrayList<List<String>>();
        for (AdminAccountExportRow row : rows) {
            csvRows.add(Arrays.asList(
                    value(row.getUserId()),
                    value(row.getAccountNo()),
                    value(row.getRealName()),
                    value(row.getMaskedPhone()),
                    value(row.getMaskedIdCard()),
                    value(row.getUserType()),
                    value(row.getOrgName()),
                    value(row.getClassName()),
                    value(row.getJobTitle()),
                    value(row.getEnabled()),
                    value(row.getCreatedAt())));
        }
        return CsvExporter.toAttachment(filename, Arrays.asList(
                "User ID",
                "Account No",
                "Real Name",
                "Phone",
                "ID Card",
                "User Type",
                "Organization",
                "Class",
                "Job Title",
                "Enabled",
                "Created At"), csvRows);
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void applyDataScope(AdminAccountQuery query, HttpServletRequest request) {
        AdminDataScopeAccess access = AdminDataScopeContext.get(request);
        if (access != null) {
            service.applyDataScope(query, AdminContext.requireAdminId(request), access.getDataScope(), access.getManagedOrgIds());
        }
    }
}
