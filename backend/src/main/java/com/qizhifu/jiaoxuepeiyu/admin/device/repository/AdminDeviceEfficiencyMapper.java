package com.qizhifu.jiaoxuepeiyu.admin.device.repository;

import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencyQuery;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceEfficiencySummary;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceHeatRank;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceMonthlyTrend;
import com.qizhifu.jiaoxuepeiyu.admin.device.model.AdminDeviceRealtimeState;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminDeviceEfficiencyMapper {

    @Select("<script>"
            + "SELECT "
            + "COUNT(DISTINCT d.id) AS total_device_count, "
            + "SUM(CASE WHEN d.device_status IN ('IDLE', 'IN_USE') THEN 1 ELSE 0 END) AS online_device_count, "
            + "SUM(CASE WHEN d.device_status = 'IN_USE' THEN 1 ELSE 0 END) AS active_device_count, "
            + "SUM(CASE WHEN d.device_status = 'FAULT' THEN 1 ELSE 0 END) AS fault_device_count, "
            + "COALESCE(SUM(ds.usage_minutes), 0) AS total_usage_minutes, "
            + "CASE WHEN COUNT(DISTINCT d.id) = 0 THEN 0 ELSE "
            + "ROUND(COALESCE(SUM(ds.usage_minutes), 0) / (COUNT(DISTINCT d.id) * #{totalAvailableMinutes}) * 100, 2) END "
            + "AS average_utilization_rate, "
            + "(SELECT COUNT(DISTINCT e.training_id) FROM device_usage_event e "
            + "JOIN device d2 ON d2.id = e.device_id AND d2.deleted_flag = 0 "
            + "WHERE e.training_id IS NOT NULL AND e.event_time &gt;= #{startDateTime} "
            + "AND e.event_time &lt; #{endExclusiveDateTime} "
            + "<if test='classroomId != null'>AND d2.classroom_id = #{classroomId}</if> "
            + "<if test='deviceType != null'>AND d2.device_type = #{deviceType}</if> "
            + "<if test='deviceStatus != null'>AND d2.device_status = #{deviceStatus}</if> "
            + ") AS active_training_count "
            + "FROM device d "
            + "LEFT JOIN device_usage_daily_summary ds ON ds.device_id = d.id "
            + "AND ds.usage_date BETWEEN #{startDate} AND #{endDate} "
            + "WHERE d.deleted_flag = 0 "
            + "<if test='classroomId != null'>AND d.classroom_id = #{classroomId}</if> "
            + "<if test='deviceType != null'>AND d.device_type = #{deviceType}</if> "
            + "<if test='deviceStatus != null'>AND d.device_status = #{deviceStatus}</if> "
            + "</script>")
    AdminDeviceEfficiencySummary findSummary(AdminDeviceEfficiencyQuery query);

    @Select("<script>"
            + "SELECT d.id AS device_id, d.device_code, d.device_name, d.device_type, d.device_status, "
            + "d.classroom_id, tr.room_name AS classroom_name, e.training_id AS current_training_id, "
            + "tc.training_name AS current_training_name, e.student_id AS current_student_id, "
            + "u.real_name AS current_student_name, e.started_at AS current_started_at, "
            + "CASE WHEN e.started_at IS NULL THEN 0 ELSE TIMESTAMPDIFF(MINUTE, e.started_at, NOW()) END AS current_usage_minutes, "
            + "d.last_heartbeat_at "
            + "FROM device d "
            + "LEFT JOIN training_room tr ON tr.id = d.classroom_id "
            + "LEFT JOIN device_usage_event e ON e.id = ("
            + "SELECT e2.id FROM device_usage_event e2 "
            + "WHERE e2.device_id = d.id AND e2.event_type = 'START' AND e2.ended_at IS NULL "
            + "ORDER BY e2.event_time DESC, e2.id DESC LIMIT 1"
            + ") "
            + "LEFT JOIN training_course tc ON tc.id = e.training_id "
            + "LEFT JOIN sys_user u ON u.id = e.student_id "
            + "WHERE d.deleted_flag = 0 "
            + "<if test='classroomId != null'>AND d.classroom_id = #{classroomId}</if> "
            + "<if test='deviceType != null'>AND d.device_type = #{deviceType}</if> "
            + "<if test='deviceStatus != null'>AND d.device_status = #{deviceStatus}</if> "
            + "ORDER BY tr.room_name ASC, d.device_code ASC, d.id ASC "
            + "</script>")
    List<AdminDeviceRealtimeState> findRealtimeStates(AdminDeviceEfficiencyQuery query);

    @Select("<script>"
            + "SELECT DATE_FORMAT(ds.usage_date, '%Y-%m') AS month, "
            + "SUM(ds.usage_minutes) AS usage_minutes, SUM(ds.usage_count) AS usage_count, "
            + "CASE WHEN COUNT(DISTINCT d.id) = 0 THEN 0 ELSE "
            + "ROUND(SUM(ds.usage_minutes) / (COUNT(DISTINCT d.id) * DAY(LAST_DAY(ds.usage_date)) * 24 * 60) * 100, 2) END "
            + "AS utilization_rate "
            + "FROM device_usage_daily_summary ds "
            + "JOIN device d ON d.id = ds.device_id AND d.deleted_flag = 0 "
            + "WHERE ds.usage_date BETWEEN #{startDate} AND #{endDate} "
            + "<if test='classroomId != null'>AND d.classroom_id = #{classroomId}</if> "
            + "<if test='deviceType != null'>AND d.device_type = #{deviceType}</if> "
            + "<if test='deviceStatus != null'>AND d.device_status = #{deviceStatus}</if> "
            + "GROUP BY DATE_FORMAT(ds.usage_date, '%Y-%m') ORDER BY month ASC "
            + "</script>")
    List<AdminDeviceMonthlyTrend> findMonthlyTrends(AdminDeviceEfficiencyQuery query);

    @Select("<script>"
            + "SELECT d.id AS device_id, d.device_code, d.device_name, d.device_type, d.classroom_id, "
            + "tr.room_name AS classroom_name, COALESCE(SUM(ds.usage_minutes), 0) AS usage_minutes, "
            + "COALESCE(SUM(ds.usage_count), 0) AS usage_count, "
            + "ROUND(COALESCE(SUM(ds.usage_minutes), 0) / #{totalAvailableMinutes} * 100, 2) AS utilization_rate "
            + "FROM device d "
            + "LEFT JOIN training_room tr ON tr.id = d.classroom_id "
            + "LEFT JOIN device_usage_daily_summary ds ON ds.device_id = d.id "
            + "AND ds.usage_date BETWEEN #{startDate} AND #{endDate} "
            + "WHERE d.deleted_flag = 0 "
            + "<if test='classroomId != null'>AND d.classroom_id = #{classroomId}</if> "
            + "<if test='deviceType != null'>AND d.device_type = #{deviceType}</if> "
            + "<if test='deviceStatus != null'>AND d.device_status = #{deviceStatus}</if> "
            + "GROUP BY d.id, d.device_code, d.device_name, d.device_type, d.classroom_id, tr.room_name "
            + "ORDER BY usage_minutes DESC, usage_count DESC, d.id ASC LIMIT #{rankLimit} "
            + "</script>")
    List<AdminDeviceHeatRank> findHeatRanking(AdminDeviceEfficiencyQuery query);
}
