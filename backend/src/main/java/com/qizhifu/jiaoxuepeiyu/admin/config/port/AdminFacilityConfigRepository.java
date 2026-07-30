package com.qizhifu.jiaoxuepeiyu.admin.config.port;

import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroomCommand;
import java.util.List;

public interface AdminFacilityConfigRepository {

    List<AdminClassroom> findClassrooms();

    AdminClassroom findClassroom(Long classroomId);

    Long createClassroom(AdminClassroomCommand command);

    void updateClassroom(Long classroomId, AdminClassroomCommand command);

    void deleteClassroom(Long classroomId);
}
