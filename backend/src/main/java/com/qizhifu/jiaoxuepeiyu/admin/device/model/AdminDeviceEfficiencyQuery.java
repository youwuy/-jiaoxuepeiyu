package com.qizhifu.jiaoxuepeiyu.admin.device.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminDeviceEfficiencyQuery {

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endExclusiveDateTime;
    private Long classroomId;
    private String deviceType;
    private String deviceStatus;
    private Integer dayCount;
    private Long totalAvailableMinutes;
    private int rankLimit = 10;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndExclusiveDateTime() {
        return endExclusiveDateTime;
    }

    public void setEndExclusiveDateTime(LocalDateTime endExclusiveDateTime) {
        this.endExclusiveDateTime = endExclusiveDateTime;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Integer getDayCount() {
        return dayCount;
    }

    public void setDayCount(Integer dayCount) {
        this.dayCount = dayCount;
    }

    public Long getTotalAvailableMinutes() {
        return totalAvailableMinutes;
    }

    public void setTotalAvailableMinutes(Long totalAvailableMinutes) {
        this.totalAvailableMinutes = totalAvailableMinutes;
    }

    public int getRankLimit() {
        return rankLimit;
    }

    public void setRankLimit(int rankLimit) {
        this.rankLimit = rankLimit;
    }
}
