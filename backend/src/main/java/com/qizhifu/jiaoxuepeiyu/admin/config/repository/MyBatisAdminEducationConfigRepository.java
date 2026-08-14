package com.qizhifu.jiaoxuepeiyu.admin.config.repository;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYear;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYearCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClass;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajor;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajorCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminSemester;
import com.qizhifu.jiaoxuepeiyu.admin.config.port.AdminEducationConfigRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminEducationConfigRepository implements AdminEducationConfigRepository {

    private final AdminEducationConfigMapper mapper;

    public MyBatisAdminEducationConfigRepository(AdminEducationConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminAcademicYear> findAcademicYears() {
        return mapper.findAcademicYears();
    }

    @Override
    public List<AdminSemester> findSemesters() {
        return mapper.findSemesters();
    }

    @Override
    public boolean academicYearExists(String yearName) {
        return mapper.countAcademicYears(yearName) > 0;
    }

    @Override
    public boolean semesterExists(Long semesterId) {
        return mapper.countSemesters(semesterId) > 0;
    }

    @Override
    public Long createAcademicYear(AdminAcademicYearCommand command) {
        AdminAcademicYear year = new AdminAcademicYear();
        year.setYearName(command.getYearName());
        mapper.insertAcademicYear(year);
        return year.getAcademicYearId();
    }

    @Override
    public void createSemester(Long academicYearId, String semesterName) {
        mapper.insertSemester(academicYearId, semesterName);
    }

    @Override
    public void clearCurrentSemesters() {
        mapper.clearCurrentSemesters();
    }

    @Override
    public void markCurrentSemester(Long semesterId) {
        mapper.markCurrentSemester(semesterId);
    }

    @Override
    public List<AdminMajor> findMajors() {
        return mapper.findMajors();
    }

    @Override
    public boolean majorExists(String majorName) {
        return mapper.countMajorsByName(majorName) > 0;
    }

    @Override
    public Long createMajor(AdminMajorCommand command) {
        AdminMajor major = new AdminMajor();
        major.setMajorName(command.getMajorName());
        mapper.insertMajor(major);
        return major.getMajorId();
    }

    @Override
    public void updateMajorStatus(Long majorId, boolean enabled) {
        mapper.updateMajorStatus(majorId, enabled ? 1 : 0);
    }

    @Override
    public List<AdminClass> findClasses(Long majorId) {
        return mapper.findClasses(majorId);
    }

    @Override
    public boolean classExists(String className) {
        return mapper.countClassesByName(className) > 0;
    }

    @Override
    public Long createClass(AdminClassCommand command) {
        AdminClass adminClass = new AdminClass();
        adminClass.setMajorId(command.getMajorId());
        adminClass.setClassName(command.getClassName());
        mapper.insertClass(adminClass);
        return adminClass.getClassId();
    }

    @Override
    public void updateClassStatus(Long classId, boolean enabled) {
        mapper.updateClassStatus(classId, enabled ? 1 : 0);
    }

}
