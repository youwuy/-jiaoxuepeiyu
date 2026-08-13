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
