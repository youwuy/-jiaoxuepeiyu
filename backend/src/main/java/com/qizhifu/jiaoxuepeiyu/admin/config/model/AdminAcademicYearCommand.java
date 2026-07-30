package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import javax.validation.constraints.NotBlank;

public class AdminAcademicYearCommand {

    @NotBlank
    private String yearName;

    public AdminAcademicYearCommand() {
    }

    public AdminAcademicYearCommand(String yearName) {
        this.yearName = yearName;
    }

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }
}
