package com.qizhifu.jiaoxuepeiyu.admin.config.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AdminClassCommand {

    @NotNull
    private Long majorId;
    @NotBlank
    private String className;

    public AdminClassCommand() {
    }

    public AdminClassCommand(Long majorId, String className) {
        this.majorId = majorId;
        this.className = className;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
