<template>
  <AdminShell activeKey="device-efficiency">
    <section class="admin-device-efficiency-page">
      <header class="admin-device-efficiency-top">
        <el-breadcrumb class="admin-device-efficiency-breadcrumb" separator="/">
          <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
          <el-breadcrumb-item>实训设备效能分析</el-breadcrumb-item>
        </el-breadcrumb>
        <button type="button" class="admin-device-efficiency-live" @click="refreshData">
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
              <polyline points="26,166 116,142 206,112 296,84 386,54 476,34" />
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
import { computed, defineComponent, h, ref } from 'vue';
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
const periods = ['近一周', '近半年', '近一年', '自定义时段'];

const metrics = [
  { label: '今日实训数', value: '8', delta: '较昨日 +2', tone: 'blue', icon: DataLine },
  { label: '今日实训总时长', value: '32.5h', delta: '较昨日 +5.2h', tone: 'green', icon: Clock },
  { label: '今日使用设备总数', value: '45', delta: '', tone: 'orange', icon: Monitor },
  { label: '今日实训学生人数', value: '186', delta: '较昨日 +24', tone: 'purple', icon: User },
  { label: '今日空闲设备', value: '2', delta: '', tone: 'gray', icon: SetUp }
];

const liveDevices: LiveDevice[] = [
  { name: 'DESKTOP-168QTC2', type: '电脑', status: '使用中', usedToday: '1.5h', ip: '192.168.10.21', user: '王成祥', heartbeat: '30秒前' },
  { name: 'DESKTOP-micro', type: '电脑', status: '使用中', usedToday: '1.2h', ip: '192.168.10.26', user: '陈松', heartbeat: '22秒前' },
  { name: '我的电脑179', type: '电脑', status: '空闲', ip: '192.168.10.79', heartbeat: '1分钟前' }
];

const buildStats: NamedCount[] = [
  { name: '电脑', count: 60, icon: Monitor },
  { name: '仿真驾驶台', count: 5, icon: OfficeBuilding }
];

const monthPoints = [
  { month: '9月', value: 320, x: 26, y: 166 },
  { month: '10月', value: 380, x: 116, y: 142 },
  { month: '11月', value: 450, x: 206, y: 112 },
  { month: '12月', value: 520, x: 296, y: 84 },
  { month: '1月', value: 610, x: 386, y: 54 },
  { month: '2月', value: 680, x: 476, y: 34 }
];

const rankings: RankingItem[] = [
  { name: '驾驶模拟器A型', hours: 1280, percent: 94 },
  { name: '信号控制台', hours: 1120, percent: 82 },
  { name: '站务模拟终端', hours: 980, percent: 78 },
  { name: '调度指挥终端', hours: 820, percent: 64 },
  { name: '调度操作台', hours: 690, percent: 52 }
];

const roomRates: RoomRate[] = [
  { name: '实训室A-301', rate: 92, color: '#3b82f6' },
  { name: '驾驶模拟室B-101', rate: 85, color: '#18a8df' },
  { name: '实训室C-201', rate: 78, color: '#8b5cf6' },
  { name: '调度实训室D-401', rate: 72, color: '#f59e0b' },
  { name: '实训室E-501', rate: 45, color: '#ef4444' }
];

const trendRows = computed(() => monthPoints.map((item, index) => ({
  month: item.month,
  hours: item.value,
  growth: index === 0 ? '-' : `+${item.value - monthPoints[index - 1].value}h`,
  device: rankings[index % rankings.length].name
})));

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

function refreshData() {
  detailTitle.value = '数据实时更新';
  detailRows.value = [
    { label: '更新时间', value: '刚刚' },
    { label: '心跳规则', value: '30秒刷新，2分钟无心跳判定离线' },
    { label: '统计口径', value: '仅统计在线人数、IP、在线状态' }
  ];
  detailVisible.value = true;
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
</script>
