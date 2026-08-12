package com.qizhifu.jiaoxuepeiyu.admin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminEducationConfigServiceTests {

    @Test
    void createsAcademicYearWithTwoSemesters() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        Long academicYearId = service.createAcademicYear(new AdminAcademicYearCommand("2026-2027"));

        assertEquals(100L, academicYearId.longValue());
        assertEquals(Arrays.asList("FIRST", "SECOND"), repository.semesters);
    }

    @Test
    void normalizesLegacyAcademicYearSuffix() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        service.createAcademicYear(new AdminAcademicYearCommand("2026-2027学年"));

        assertEquals("2026-2027", repository.savedAcademicYear.getYearName());
    }

    @Test
    void rejectsInvalidAcademicYearRange() {
        AdminEducationConfigService service = new AdminEducationConfigService(new FakeEducationConfig());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createAcademicYear(new AdminAcademicYearCommand("2026-2028"));
        });

        assertEquals("Academic year format must be YYYY-YYYY", exception.getMessage());
    }

    @Test
    void rejectsDuplicateAcademicYear() {
        FakeEducationConfig repository = new FakeEducationConfig();
        repository.duplicateAcademicYear = true;
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createAcademicYear(new AdminAcademicYearCommand("2026-2027"));
        });

        assertEquals("Academic year already exists", exception.getMessage());
    }

    @Test
    void marksOnlyOneSemesterAsCurrent() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        service.setCurrentSemester(2L);

        assertEquals(true, repository.clearedCurrent);
        assertEquals(2L, repository.currentSemesterId.longValue());
    }

    @Test
    void rejectsMissingSemesterWithoutClearingCurrentSemester() {
        FakeEducationConfig repository = new FakeEducationConfig();
        repository.existingSemester = false;
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.setCurrentSemester(99L));

        assertEquals("Semester not found", exception.getMessage());
        assertEquals(false, repository.clearedCurrent);
    }

    @Test
    void rejectsBlankMajorName() {
        AdminEducationConfigService service = new AdminEducationConfigService(new FakeEducationConfig());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createMajor(new AdminMajorCommand(" "));
        });

        assertEquals("Major name is required", exception.getMessage());
    }

    @Test
    void createsClassWithoutMajor() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        Long classId = service.createClass(new AdminClassCommand(null, "Class 2026-01"));

        assertEquals(300L, classId.longValue());
    }

    @Test
    void updatesMajorStatus() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        service.disableMajor(3L);
        service.enableMajor(3L);

        assertEquals(3L, repository.statusMajorId.longValue());
        assertEquals(true, repository.majorEnabled);
    }

    @Test
    void updatesClassStatus() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        service.disableClass(5L);
        service.enableClass(5L);

        assertEquals(5L, repository.statusClassId.longValue());
        assertEquals(true, repository.classEnabled);
    }

    @Test
    void createsJobRoleWithTrimmedNameAndDefaultSortOrder() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        Long jobRoleId = service.createJobRole(new AdminJobRoleCommand(" Driver ", null));

        assertEquals(400L, jobRoleId.longValue());
        assertEquals("Driver", repository.savedJobRole.getRoleName());
        assertEquals(0, repository.savedJobRole.getSortOrder().intValue());
    }

    @Test
    void rejectsBlankJobRoleName() {
        AdminEducationConfigService service = new AdminEducationConfigService(new FakeEducationConfig());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createJobRole(new AdminJobRoleCommand(" ", 1));
        });

        assertEquals("Job role name is required", exception.getMessage());
    }

    @Test
    void updatesJobRoleAndStatus() {
        FakeEducationConfig repository = new FakeEducationConfig();
        AdminEducationConfigService service = new AdminEducationConfigService(repository);

        service.updateJobRole(4L, new AdminJobRoleCommand("Dispatcher", 2));
        service.disableJobRole(4L);
        service.enableJobRole(4L);

        assertEquals(4L, repository.updatedJobRoleId.longValue());
        assertEquals("Dispatcher", repository.savedJobRole.getRoleName());
        assertEquals(2, repository.savedJobRole.getSortOrder().intValue());
        assertEquals(4L, repository.statusJobRoleId.longValue());
        assertEquals(true, repository.jobRoleEnabled);
    }

    private static class FakeEducationConfig implements AdminEducationConfigRepository {
        private final List<String> semesters = new ArrayList<String>();
        private boolean clearedCurrent;
        private Long currentSemesterId;
        private Long statusMajorId;
        private boolean majorEnabled;
        private Long statusClassId;
        private boolean classEnabled;
        private AdminJobRoleCommand savedJobRole;
        private Long updatedJobRoleId;
        private Long statusJobRoleId;
        private boolean jobRoleEnabled;
        private boolean duplicateAcademicYear;
        private boolean existingSemester = true;
        private AdminAcademicYearCommand savedAcademicYear;

        @Override
        public List<AdminAcademicYear> findAcademicYears() {
            AdminAcademicYear year = new AdminAcademicYear();
            year.setAcademicYearId(100L);
            year.setYearName("2026-2027");
            return Arrays.asList(year);
        }

        @Override
        public List<AdminSemester> findSemesters() {
            AdminSemester first = new AdminSemester();
            first.setSemesterId(1L);
            first.setAcademicYearId(100L);
            first.setSemesterName("FIRST");
            return Arrays.asList(first);
        }

        @Override
        public boolean academicYearExists(String yearName) {
            return duplicateAcademicYear;
        }

        @Override
        public boolean semesterExists(Long semesterId) {
            return existingSemester;
        }

        @Override
        public Long createAcademicYear(AdminAcademicYearCommand command) {
            savedAcademicYear = command;
            return 100L;
        }

        @Override
        public void createSemester(Long academicYearId, String semesterName) {
            semesters.add(semesterName);
        }

        @Override
        public void clearCurrentSemesters() {
            clearedCurrent = true;
        }

        @Override
        public void markCurrentSemester(Long semesterId) {
            currentSemesterId = semesterId;
        }

        @Override
        public List<AdminMajor> findMajors() {
            return new ArrayList<AdminMajor>();
        }

        @Override
        public boolean majorExists(String majorName) {
            return false;
        }

        @Override
        public Long createMajor(AdminMajorCommand command) {
            return 200L;
        }

        @Override
        public void updateMajorStatus(Long majorId, boolean enabled) {
            this.statusMajorId = majorId;
            this.majorEnabled = enabled;
        }

        @Override
        public List<AdminClass> findClasses(Long majorId) {
            return new ArrayList<AdminClass>();
        }

        @Override
        public boolean classExists(String className) {
            return false;
        }

        @Override
        public Long createClass(AdminClassCommand command) {
            return 300L;
        }

        @Override
        public void updateClassStatus(Long classId, boolean enabled) {
            this.statusClassId = classId;
            this.classEnabled = enabled;
        }

        @Override
        public List<AdminJobRole> findJobRoles() {
            return new ArrayList<AdminJobRole>();
        }

        @Override
        public Long createJobRole(AdminJobRoleCommand command) {
            this.savedJobRole = command;
            return 400L;
        }

        @Override
        public void updateJobRole(Long jobRoleId, AdminJobRoleCommand command) {
            this.updatedJobRoleId = jobRoleId;
            this.savedJobRole = command;
        }

        @Override
        public void updateJobRoleStatus(Long jobRoleId, boolean enabled) {
            this.statusJobRoleId = jobRoleId;
            this.jobRoleEnabled = enabled;
        }
    }
}
