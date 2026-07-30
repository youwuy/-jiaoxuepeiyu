package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import javax.validation.constraints.NotBlank;

public class AdminMajorCommand {

    @NotBlank
    private String majorName;

    public AdminMajorCommand() {
    }

    public AdminMajorCommand(String majorName) {
        this.majorName = majorName;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }
}
