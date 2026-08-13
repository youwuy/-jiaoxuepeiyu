import { requestJson } from './http';

export interface AdminDeviceEfficiencySummary {
  totalDeviceCount?: number;
  onlineDeviceCount?: number;
  activeDeviceCount?: number;
  faultDeviceCount?: number;
  totalUsageMinutes?: number;
  averageUtilizationRate?: number;
  activeTrainingCount?: number;
}

export interface AdminDeviceRealtimeState {
  deviceId?: number;
  deviceCode?: string;
  deviceName?: string;
  deviceType?: string;
  deviceStatus?: string;
  ipAddress?: string;
  classroomId?: number;
  classroomName?: string;
  currentTrainingName?: string;
  currentStudentName?: string;
  currentUsageMinutes?: number;
  lastHeartbeatAt?: string;
}

export interface AdminDeviceMonthlyTrend {
  month: string;
  usageMinutes?: number;
  usageCount?: number;
  utilizationRate?: number;
}

export interface AdminDeviceHeatRank {
  deviceId?: number;
  deviceCode?: string;
  deviceName?: string;
  deviceType?: string;
  classroomId?: number;
  classroomName?: string;
  usageMinutes?: number;
  usageCount?: number;
  utilizationRate?: number;
  rankNo?: number;
}

export interface AdminDeviceEfficiencyReport {
  summary?: AdminDeviceEfficiencySummary;
  realtimeStates?: AdminDeviceRealtimeState[];
  monthlyTrends?: AdminDeviceMonthlyTrend[];
  heatRanking?: AdminDeviceHeatRank[];
}

export function fetchAdminDeviceEfficiencyReport() {
  return requestJson<AdminDeviceEfficiencyReport>('/admin/devices/efficiency', {
    fallbackLabel: '设备效能分析'
  });
}

export interface AdminOnlineUser {
  userId?: number;
  username?: string;
  realName?: string;
  userType?: string;
  lastLoginIp?: string;
  lastHeartbeatTime?: string;
  online?: boolean;
}

export interface AdminOnlineDashboard {
  generatedAt?: string;
  totalCount?: number;
  onlineCount?: number;
  offlineCount?: number;
  heartbeatIntervalSeconds?: number;
  offlineTimeoutSeconds?: number;
  users?: AdminOnlineUser[];
}

export function fetchAdminOnlineStudents() {
  return requestJson<AdminOnlineDashboard>('/admin/online/users?userType=student&limit=1000', {
    fallbackLabel: '学员在线信息'
  });
}
