package com.qizhifu.jiaoxuepeiyu.admin.config;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYear;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYearCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClass;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminJobRole;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminJobRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajor;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajorCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminSemester;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminEducationConfigRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminEducationConfigService {

    private final AdminEducationConfigRepository repository;

    public AdminEducationConfigService(AdminEducationConfigRepository repository) {
        this.repository = repository;
    }

    public List<AdminAcademicYear> listAcademicYears() {
        List<AdminAcademicYear> years = repository.findAcademicYears();
        Map<Long, AdminAcademicYear> byId = new LinkedHashMap<Long, AdminAcademicYear>();
        for (AdminAcademicYear year : years) {
            byId.put(year.getAcademicYearId(), year);
        }
        for (AdminSemester semester : repository.findSemesters()) {
            AdminAcademicYear year = byId.get(semester.getAcademicYearId());
            if (year != null) {
                year.getSemesters().add(semester);
            }
        }
        return years;
    }

    @Transactional
    public Long createAcademicYear(AdminAcademicYearCommand command) {
        AdminAcademicYearCommand normalized = normalizedYear(command);
        Long academicYearId = repository.createAcademicYear(normalized);
        repository.createSemester(academicYearId, "FIRST");
        repository.createSemester(academicYearId, "SECOND");
        return academicYearId;
    }

    @Transactional
    public void setCurrentSemester(Long semesterId) {
        if (semesterId == null) {
            throw new BusinessException(400, "Semester is required");
        }
        repository.clearCurrentSemesters();
        repository.markCurrentSemester(semesterId);
    }

    public List<AdminMajor> listMajors() {
        return repository.findMajors();
    }

    public Long createMajor(AdminMajorCommand command) {
        AdminMajorCommand normalized = normalizedMajor(command);
        return repository.createMajor(normalized);
    }

    public void enableMajor(Long majorId) {
        repository.updateMajorStatus(majorId, true);
    }

    public void disableMajor(Long majorId) {
        repository.updateMajorStatus(majorId, false);
    }

    public List<AdminClass> listClasses(Long majorId) {
        return repository.findClasses(majorId);
    }

    public Long createClass(AdminClassCommand command) {
        AdminClassCommand normalized = normalizedClass(command);
        return repository.createClass(normalized);
    }

    public void enableClass(Long classId) {
        repository.updateClassStatus(classId, true);
    }

    public void disableClass(Long classId) {
        repository.updateClassStatus(classId, false);
    }

    public List<AdminJobRole> listJobRoles() {
        return repository.findJobRoles();
    }

    public Long createJobRole(AdminJobRoleCommand command) {
        return repository.createJobRole(normalizedJobRole(command));
    }

    public void updateJobRole(Long jobRoleId, AdminJobRoleCommand command) {
        if (jobRoleId == null) {
            throw new BusinessException(400, "Job role is required");
        }
        repository.updateJobRole(jobRoleId, normalizedJobRole(command));
    }

    public void enableJobRole(Long jobRoleId) {
        repository.updateJobRoleStatus(jobRoleId, true);
    }

    public void disableJobRole(Long jobRoleId) {
        repository.updateJobRoleStatus(jobRoleId, false);
    }

    private AdminAcademicYearCommand normalizedYear(AdminAcademicYearCommand command) {
        if (command == null || isBlank(command.getYearName())) {
            throw new BusinessException(400, "Academic year name is required");
        }
        return new AdminAcademicYearCommand(command.getYearName().trim());
    }

    private AdminMajorCommand normalizedMajor(AdminMajorCommand command) {
        if (command == null || isBlank(command.getMajorName())) {
            throw new BusinessException(400, "Major name is required");
        }
        return new AdminMajorCommand(command.getMajorName().trim());
    }

    private AdminClassCommand normalizedClass(AdminClassCommand command) {
        if (command == null || isBlank(command.getClassName())) {
            throw new BusinessException(400, "Class name is required");
        }
        return new AdminClassCommand(command.getMajorId(), command.getClassName().trim());
    }

    private AdminJobRoleCommand normalizedJobRole(AdminJobRoleCommand command) {
        if (command == null || isBlank(command.getRoleName())) {
            throw new BusinessException(400, "Job role name is required");
        }
        String roleName = command.getRoleName().trim();
        if (roleName.length() > 20) {
            throw new BusinessException(400, "Job role name cannot exceed 20 characters");
        }
        return new AdminJobRoleCommand(roleName, command.getSortOrder() == null ? Integer.valueOf(0) : command.getSortOrder());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
