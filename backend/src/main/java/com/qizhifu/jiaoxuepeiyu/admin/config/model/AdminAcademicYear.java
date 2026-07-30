package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import java.util.ArrayList;
import java.util.List;

public class AdminAcademicYear {

    private Long academicYearId;
    private String yearName;
    private List<AdminSemester> semesters = new ArrayList<AdminSemester>();

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }

    public List<AdminSemester> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<AdminSemester> semesters) {
        this.semesters = semesters == null ? new ArrayList<AdminSemester>() : semesters;
    }
}
