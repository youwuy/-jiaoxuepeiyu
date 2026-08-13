<template>
  <AdminShell activeKey="device-efficiency">
    <section class="admin-device-efficiency-page">
      <header class="admin-device-efficiency-top">
        <el-breadcrumb class="admin-device-efficiency-breadcrumb" separator="/">
          <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
          <el-breadcrumb-item>实训设备效能分析</el-breadcrumb-item>
        </el-breadcrumb>
        <button type="button" class="admin-device-efficiency-live" :disabled="loading" @click="refreshData">
          <i></i>
          数据实时更新
        </button>
      </header>

      <section class="admin-device-efficiency-metrics">
        <article v-for="item in metrics" :key="item.label" class="admin-device-efficiency-metric">
          <div>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <em v-if="item.delta">{{ item.delta }}</em>
          </div>
          <b :class="item.tone">
            <el-icon><component :is="item.icon" /></el-icon>
          </b>
        </article>
      </section>

      <section class="admin-device-efficiency-grid is-top">
        <article class="admin-device-efficiency-card is-live">
          <header>
            <strong><el-icon><Refresh /></el-icon>实训设备实时使用检测</strong>
            <el-button text @click="onlineVisible = true">查看在线信息</el-button>
          </header>
          <div class="admin-device-efficiency-device-list">
            <button v-for="device in liveDevices" :key="device.name" type="button" @click="openDevice(device)">
              <span :class="{ idle: device.status === '空闲' }"></span>
              <strong>{{ device.name }}</strong>
              <em>{{ device.type }}</em>
              <b :class="{ idle: device.status === '空闲' }">{{ device.status }}</b>
              <small v-if="device.usedToday">今日已用{{ device.usedToday }}</small>
            </button>
          </div>
        </article>

        <article class="admin-device-efficiency-card is-build">
          <header><strong><el-icon><OfficeBuilding /></el-icon>设备建设情况</strong></header>
          <button v-for="item in buildStats" :key="item.name" type="button" class="admin-device-efficiency-build-row" @click="openBuild(item)">
            <span><el-icon><component :is="item.icon" /></el-icon></span>
            <strong>{{ item.name }}</strong>
            <b>{{ item.count }}</b>
          </button>
        </article>
      </section>

    </section>

    <el-dialog v-model="onlineVisible" class="admin-device-efficiency-dialog" width="720px" :show-close="false" append-to-body>
      <template #header><DialogHead title="实时在线信息" @close="onlineVisible = false" /></template>
      <table class="admin-device-efficiency-dialog-table">
        <thead><tr><th>设备名称</th><th>类型</th><th>状态</th><th>IP地址</th><th>在线用户</th><th>最后心跳</th></tr></thead>
        <tbody>
          <tr v-for="item in liveDevices" :key="item.name">
            <td>{{ item.name }}</td><td>{{ item.type }}</td><td>{{ item.status }}</td><td>{{ item.ip }}</td><td>{{ item.user || '-' }}</td><td>{{ item.heartbeat }}</td>
          </tr>
        </tbody>
      </table>
    </el-dialog>

    <el-dialog v-model="detailVisible" class="admin-device-efficiency-dialog" width="600px" :show-close="false" append-to-body>
      <template #header><DialogHead :title="detailTitle" @close="detailVisible = false" /></template>
      <section class="admin-device-efficiency-detail-panel">
        <p v-for="item in detailRows" :key="item.label"><span>{{ item.label }}</span><b>{{ item.value }}</b></p>
      </section>
    </el-dialog>

  </AdminShell>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onBeforeUnmount, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Clock,
  Close,
  DataLine,
  Monitor,
  OfficeBuilding,
  Refresh,
  SetUp,
  User
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminDeviceEfficiencyReport,
  type AdminDeviceEfficiencyReport
} from '../../api/admin-device';

interface LiveDevice {
  name: string;
  type: string;
  status: '使用中' | '空闲';
  usedToday?: string;
  ip: string;
  user?: string;
  heartbeat: string;
}

interface NamedCount {
  name: string;
  count: number;
  icon: typeof Monitor;
}

const onlineVisible = ref(false);
const detailVisible = ref(false);
const detailTitle = ref('');
const detailRows = ref<{ label: string; value: string }[]>([]);
const loading = ref(false);
const report = ref<AdminDeviceEfficiencyReport>({
  summary: {},
  realtimeStates: [],
  monthlyTrends: [],
  heatRanking: []
});
let refreshTimer: ReturnType<typeof setInterval> | undefined;

const metrics = computed(() => {
  const summary = report.value.summary || {};
  const total = Number(summary.totalDeviceCount || 0);
  const active = Number(summary.activeDeviceCount || 0);
  const online = Number(summary.onlineDeviceCount || 0);
  const idle = Math.max(0, total - active);
  return [
    { label: '当前实训数', value: String(summary.activeTrainingCount || 0), delta: '', tone: 'blue', icon: DataLine },
    { label: '累计实训总时长', value: formatHours(summary.totalUsageMinutes || 0), delta: '', tone: 'green', icon: Clock },
    { label: '设备总数', value: String(total), delta: '', tone: 'orange', icon: Monitor },
    { label: '在线设备数', value: String(online), delta: '', tone: 'purple', icon: User },
    { label: '当前空闲设备', value: String(idle), delta: '', tone: 'gray', icon: SetUp }
  ];
});

const liveDevices = computed<LiveDevice[]>(() => (report.value.realtimeStates || []).map((item) => ({
  name: item.deviceName || item.deviceCode || `设备${item.deviceId || ''}`,
  type: item.deviceType || '设备',
  status: normalizeDeviceStatus(item.deviceStatus),
  usedToday: item.currentUsageMinutes ? formatHours(item.currentUsageMinutes) : undefined,
  ip: item.ipAddress || '-',
  user: item.currentStudentName,
  heartbeat: formatHeartbeat(item.lastHeartbeatAt)
})));

const buildStats = computed<NamedCount[]>(() => {
  const counts = new Map<string, number>();
  (report.value.realtimeStates || []).forEach((item) => {
    const type = item.deviceType || '设备';
    counts.set(type, (counts.get(type) || 0) + 1);
  });
  return Array.from(counts.entries()).map(([name, count]) => ({ name, count, icon: name.includes('电脑') ? Monitor : OfficeBuilding }));
});

const DialogHead = defineComponent({
  props: { title: { type: String, required: true } },
  emits: ['close'],
  setup(props, { emit }) {
    return () => h('div', { class: 'admin-device-efficiency-dialog-head' }, [
      h('strong', props.title),
      h('button', { type: 'button', onClick: () => emit('close') }, [h(Close)])
    ]);
  }
});

async function refreshData() {
  await loadDashboard();
  detailTitle.value = '数据实时更新';
  detailRows.value = [
    { label: '更新时间', value: '刚刚' },
    { label: '心跳规则', value: '30秒刷新，2分钟无心跳判定离线' },
    { label: '统计口径', value: '仅统计在线人数、IP、在线状态' }
  ];
  detailVisible.value = true;
}

async function loadDashboard() {
  loading.value = true;
  try {
    report.value = await fetchAdminDeviceEfficiencyReport();
  } catch (error) {
    report.value = { summary: {}, realtimeStates: [], monthlyTrends: [], heatRanking: [] };
    ElMessage.error(error instanceof Error ? error.message : '设备效能分析加载失败');
  } finally {
    loading.value = false;
  }
}

function normalizeDeviceStatus(status?: string): '使用中' | '空闲' {
  const value = String(status || '').toUpperCase();
  return value === 'USING' || value === 'ACTIVE' || value === 'ONLINE' || value === '使用中' ? '使用中' : '空闲';
}

function formatHours(minutes: number) {
  const hours = Number(minutes || 0) / 60;
  return `${Number.isInteger(hours) ? hours : hours.toFixed(1)}h`;
}

function formatHeartbeat(value?: string) {
  if (!value) {
    return '-';
  }
  return value.includes('T') ? value.replace('T', ' ').slice(0, 16) : value.slice(0, 16);
}

function openDevice(device: LiveDevice) {
  detailTitle.value = device.name;
  detailRows.value = [
    { label: '设备类型', value: device.type },
    { label: '当前状态', value: device.status },
    { label: 'IP地址', value: device.ip },
    { label: '在线用户', value: device.user || '-' },
    { label: '今日已用', value: device.usedToday || '0h' }
  ];
  detailVisible.value = true;
}

function openBuild(item: NamedCount) {
  detailTitle.value = `${item.name}建设情况`;
  detailRows.value = [
    { label: '固定数量', value: `${item.count}` },
    { label: '统计方式', value: '管理员维护固定数量' },
    { label: '在线口径', value: '登录状态与心跳数据' }
  ];
  detailVisible.value = true;
}

onMounted(() => {
  void loadDashboard();
  refreshTimer = setInterval(() => void loadDashboard(), 30_000);
});

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer);
});
</script>
