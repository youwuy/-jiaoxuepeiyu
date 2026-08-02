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

      <section class="admin-device-efficiency-grid is-middle">
        <article class="admin-device-efficiency-card is-trend">
          <header>
            <strong><el-icon><TrendCharts /></el-icon>设备月度使用走势</strong>
            <el-button text @click="trendVisible = true">趋势详情</el-button>
          </header>
          <div class="admin-device-efficiency-line-chart">
            <div class="line-axis"></div>
            <svg viewBox="0 0 560 220" role="img" aria-label="设备月度使用走势">
              <polyline :points="trendPolyline" />
              <g v-for="point in monthPoints" :key="point.month" :transform="`translate(${point.x} ${point.y})`">
                <circle r="5" />
                <text x="-18" y="-10">{{ point.value }}h</text>
              </g>
            </svg>
            <div class="admin-device-efficiency-months">
              <span v-for="item in monthPoints" :key="item.month">{{ item.month }}</span>
            </div>
          </div>
        </article>

        <article class="admin-device-efficiency-card is-ranking">
          <header>
            <strong><el-icon><HotWater /></el-icon>实训设备热度排行</strong>
            <div class="admin-device-efficiency-tabs">
              <button v-for="item in periods" :key="item" type="button" :class="{ active: activeRankPeriod === item }" @click="activeRankPeriod = item">{{ item }}</button>
            </div>
          </header>
          <div class="admin-device-efficiency-rank-list">
            <button v-for="(item, index) in rankings" :key="item.name" type="button" :class="{ first: index === 0 }" @click="openRank(item)">
              <i>{{ index + 1 }}</i>
              <strong>{{ item.name }}</strong>
              <span><b :style="{ width: `${item.percent}%` }"></b></span>
              <em>{{ item.hours }}h</em>
            </button>
          </div>
        </article>
      </section>

      <article class="admin-device-efficiency-card is-rate">
        <header>
          <strong><el-icon><PieChart /></el-icon>设备利用率对比分析（按实训室）</strong>
          <div class="admin-device-efficiency-tabs">
            <button v-for="item in periods" :key="item" type="button" :class="{ active: activeRatePeriod === item }" @click="activeRatePeriod = item">{{ item }}</button>
          </div>
        </header>
        <div class="admin-device-efficiency-bar-chart">
          <div class="bar-axis"></div>
          <button v-for="item in roomRates" :key="item.name" type="button" @click="openRoom(item)">
            <span :style="{ height: `${item.rate * 1.8}px`, background: item.color }"></span>
            <b>{{ item.rate }}%</b>
            <em>{{ item.name }}</em>
          </button>
        </div>
      </article>
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

    <el-dialog v-model="trendVisible" class="admin-device-efficiency-dialog" width="680px" :show-close="false" append-to-body>
      <template #header><DialogHead title="设备月度使用走势详情" @close="trendVisible = false" /></template>
      <table class="admin-device-efficiency-dialog-table">
        <thead><tr><th>月份</th><th>使用时长</th><th>环比变化</th><th>主要设备</th></tr></thead>
        <tbody>
          <tr v-for="item in trendRows" :key="item.month"><td>{{ item.month }}</td><td>{{ item.hours }}h</td><td>{{ item.growth }}</td><td>{{ item.device }}</td></tr>
        </tbody>
      </table>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Clock,
  Close,
  DataLine,
  HotWater,
  Monitor,
  OfficeBuilding,
  PieChart,
  Refresh,
  SetUp,
  TrendCharts,
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

interface RankingItem {
  name: string;
  hours: number;
  percent: number;
}

interface RoomRate {
  name: string;
  rate: number;
  color: string;
}

const onlineVisible = ref(false);
const detailVisible = ref(false);
const trendVisible = ref(false);
const activeRankPeriod = ref('近一周');
const activeRatePeriod = ref('近一周');
const detailTitle = ref('');
const detailRows = ref<{ label: string; value: string }[]>([]);
const loading = ref(false);
const report = ref<AdminDeviceEfficiencyReport>({
  summary: {},
  realtimeStates: [],
  monthlyTrends: [],
  heatRanking: []
});
const periods = ['近一周', '近半年', '近一年', '自定义时段'];

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
  ip: item.deviceCode || '-',
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

const monthPoints = computed(() => {
  const trends = (report.value.monthlyTrends || []).slice(-6);
  const maxMinutes = Math.max(1, ...trends.map((item) => Number(item.usageMinutes || 0)));
  return trends.map((item, index) => {
    const x = 26 + index * 90;
    const value = Math.round(Number(item.usageMinutes || 0) / 60);
    const y = 190 - Math.round((Number(item.usageMinutes || 0) / maxMinutes) * 156);
    return { month: item.month, value, x, y };
  });
});

const rankings = computed<RankingItem[]>(() => {
  const rows = report.value.heatRanking || [];
  const maxMinutes = Math.max(1, ...rows.map((item) => Number(item.usageMinutes || 0)));
  return rows.map((item) => ({
    name: item.deviceName || item.deviceCode || `设备${item.deviceId || ''}`,
    hours: Math.round(Number(item.usageMinutes || 0) / 60),
    percent: Math.round((Number(item.usageMinutes || 0) / maxMinutes) * 100)
  }));
});

const roomRates = computed<RoomRate[]>(() => {
  const colors = ['#3b82f6', '#18a8df', '#8b5cf6', '#f59e0b', '#ef4444'];
  const rooms = new Map<string, number[]>();
  (report.value.heatRanking || []).forEach((item) => {
    const room = item.classroomName || '未分配实训室';
    const values = rooms.get(room) || [];
    values.push(Number(item.utilizationRate || 0));
    rooms.set(room, values);
  });
  return Array.from(rooms.entries()).map(([name, rates], index) => ({
    name,
    rate: Math.round(rates.reduce((sum, item) => sum + item, 0) / Math.max(1, rates.length)),
    color: colors[index % colors.length]
  }));
});

const trendRows = computed(() => monthPoints.value.map((item, index) => ({
  month: item.month,
  hours: item.value,
  growth: index === 0 ? '-' : `${item.value - monthPoints.value[index - 1].value}h`,
  device: rankings.value[index % Math.max(1, rankings.value.length)]?.name || '-'
})));
const trendPolyline = computed(() => monthPoints.value.map((item) => `${item.x},${item.y}`).join(' '));

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

function openRank(item: RankingItem) {
  detailTitle.value = `${item.name}热度详情`;
  detailRows.value = [
    { label: '统计周期', value: activeRankPeriod.value },
    { label: '累计使用时长', value: `${item.hours}h` },
    { label: '热度占比', value: `${item.percent}%` }
  ];
  detailVisible.value = true;
}

function openRoom(item: RoomRate) {
  detailTitle.value = `${item.name}利用率详情`;
  detailRows.value = [
    { label: '统计周期', value: activeRatePeriod.value },
    { label: '设备利用率', value: `${item.rate}%` },
    { label: '统计说明', value: '按固定设备数量与使用时长计算' }
  ];
  detailVisible.value = true;
}

onMounted(() => {
  void loadDashboard();
});
</script>
