package com.qizhifu.jiaoxuepeiyu.admin.config.port;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYear;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYearCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClass;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajor;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajorCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminSemester;
import java.util.List;

public interface AdminEducationConfigRepository {

    List<AdminAcademicYear> findAcademicYears();

    List<AdminSemester> findSemesters();

    Long createAcademicYear(AdminAcademicYearCommand command);

    void createSemester(Long academicYearId, String semesterName);

    void clearCurrentSemesters();

    void markCurrentSemester(Long semesterId);

    List<AdminMajor> findMajors();

    Long createMajor(AdminMajorCommand command);

    void updateMajorStatus(Long majorId, boolean enabled);

    List<AdminClass> findClasses(Long majorId);

    Long createClass(AdminClassCommand command);
}
