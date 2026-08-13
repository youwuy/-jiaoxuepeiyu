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
            <strong><el-icon><Refresh /></el-icon>学员实时在线信息</strong>
            <el-button text @click="onlineVisible = true">查看在线信息</el-button>
          </header>
          <div class="admin-device-efficiency-device-list">
            <button v-for="user in onlineUsers" :key="user.userId" type="button" @click="openUser(user)">
              <span :class="{ idle: !user.online }"></span>
              <strong>{{ user.realName || user.username || '-' }}</strong>
              <em>{{ user.username || '-' }}</em>
              <b :class="{ idle: !user.online }">{{ user.online ? '在线' : '离线' }}</b>
              <small>{{ user.lastLoginIp || '-' }}</small>
            </button>
            <el-empty v-if="onlineUsers.length === 0" description="暂无在线学员" />
          </div>
        </article>

        <article class="admin-device-efficiency-card is-build">
          <header><strong><el-icon><OfficeBuilding /></el-icon>实训室固定设备数量</strong></header>
          <button v-for="item in classroomStats" :key="item.name" type="button" class="admin-device-efficiency-build-row" @click="openClassroom(item)">
            <span><el-icon><component :is="item.icon" /></el-icon></span>
            <strong>{{ item.name }}</strong>
            <b>{{ item.count }}</b>
          </button>
          <el-empty v-if="classroomStats.length === 0" description="暂无实训室配置" />
        </article>
      </section>

    </section>

    <el-dialog v-model="onlineVisible" class="admin-device-efficiency-dialog" width="720px" :show-close="false" append-to-body>
      <template #header><DialogHead title="实时在线信息" @close="onlineVisible = false" /></template>
      <table class="admin-device-efficiency-dialog-table">
        <thead><tr><th>姓名</th><th>账号</th><th>在线状态</th><th>IP地址</th><th>最后心跳</th></tr></thead>
        <tbody>
          <tr v-for="item in onlineUsers" :key="item.userId">
            <td>{{ item.realName || '-' }}</td><td>{{ item.username || '-' }}</td><td>{{ item.online ? '在线' : '离线' }}</td><td>{{ item.lastLoginIp || '-' }}</td><td>{{ formatHeartbeat(item.lastHeartbeatTime) }}</td>
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
import { Close, Monitor, OfficeBuilding, Refresh, SetUp, User } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminOnlineStudents,
  type AdminOnlineDashboard,
  type AdminOnlineUser
} from '../../api/admin-device';
import { fetchAdminClassrooms, type AdminClassroom } from '../../api/admin-settings';

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
const onlineDashboard = ref<AdminOnlineDashboard>({ users: [] });
const classrooms = ref<AdminClassroom[]>([]);
let refreshTimer: ReturnType<typeof setInterval> | undefined;

const metrics = computed(() => {
  const total = classrooms.value.reduce((sum, item) => sum + Number(item.cameraCount || 0), 0);
  const online = Number(onlineDashboard.value.onlineCount || 0);
  const idle = Math.max(0, total - online);
  return [
    { label: '实训室数量', value: String(classrooms.value.length), delta: '', tone: 'blue', icon: OfficeBuilding },
    { label: '固定设备总数', value: String(total), delta: '', tone: 'orange', icon: Monitor },
    { label: '实时在线人数', value: String(online), delta: '', tone: 'purple', icon: User },
    { label: '当前空闲名额', value: String(idle), delta: '', tone: 'gray', icon: SetUp }
  ];
});

const onlineUsers = computed(() => (onlineDashboard.value.users || []).filter((item) => item.online));
const classroomStats = computed<NamedCount[]>(() => classrooms.value.map((item) => ({
  name: item.roomName,
  count: Number(item.cameraCount || 0),
  icon: OfficeBuilding
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
    const [presence, classroomRows] = await Promise.all([
      fetchAdminOnlineStudents(),
      fetchAdminClassrooms()
    ]);
    onlineDashboard.value = presence;
    classrooms.value = classroomRows;
  } catch (error) {
    onlineDashboard.value = { users: [] };
    classrooms.value = [];
    ElMessage.error(error instanceof Error ? error.message : '在线信息加载失败');
  } finally {
    loading.value = false;
  }
}

function formatHeartbeat(value?: string) {
  if (!value) {
    return '-';
  }
  return value.includes('T') ? value.replace('T', ' ').slice(0, 16) : value.slice(0, 16);
}

function openUser(user: AdminOnlineUser) {
  detailTitle.value = user.realName || user.username || '在线学员';
  detailRows.value = [
    { label: '学员账号', value: user.username || '-' },
    { label: '当前状态', value: user.online ? '在线' : '离线' },
    { label: 'IP地址', value: user.lastLoginIp || '-' },
    { label: '最后心跳', value: formatHeartbeat(user.lastHeartbeatTime) }
  ];
  detailVisible.value = true;
}

function openClassroom(item: NamedCount) {
  detailTitle.value = item.name;
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
