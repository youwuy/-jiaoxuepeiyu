<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-page">
      <el-breadcrumb class="admin-course-breadcrumb" separator="/">
        <el-breadcrumb-item>教学实训</el-breadcrumb-item>
        <el-breadcrumb-item>实训课</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="admin-filterbar">
        <div class="admin-filter-row">
          <el-input v-model="filters.keyword" class="admin-keyword" :prefix-icon="Search" placeholder="搜索实训课名称" clearable />
          <el-select v-model="filters.time" class="admin-time-filter" placeholder="实训时间" clearable>
            <el-option label="本周" value="week" />
            <el-option label="本月" value="month" />
          </el-select>
          <el-select v-model="filters.status" class="admin-status-filter" placeholder="发布状态" clearable>
            <el-option label="已发布" value="已发布" />
            <el-option label="未发布" value="未发布" />
          </el-select>
          <el-button @click="refreshCourses">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
        <div class="admin-action-row">
          <el-button class="admin-add-course" type="primary" :icon="Plus">新增实训课</el-button>
        </div>
      </div>

      <div class="admin-table-panel">
        <el-table :data="filteredCourses" stripe>
          <el-table-column prop="name" label="实训课名称" width="180" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="80" />
          <el-table-column prop="time" label="实训起止时间" width="200">
            <template #default="{ row }">
              <span class="time-cell">{{ row.time }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="target" label="参训班级/学生" width="160" show-overflow-tooltip />
          <el-table-column prop="teacher" label="监考教师" width="120" show-overflow-tooltip />
          <el-table-column prop="room" label="实训教室" width="120" />
          <el-table-column label="发布状态" width="100">
            <template #default="{ row }">
              <span class="status-pill" :class="{ muted: row.status === '未发布' }">
                <i></i>{{ row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="140" />
          <el-table-column label="操作" width="300">
            <template #default="{ row }">
              <div class="admin-row-actions">
                <template v-if="row.status === '未发布'">
                  <el-button link type="primary">编辑</el-button>
                  <el-button link type="danger">删除</el-button>
                  <el-button class="publish-action" link>发布</el-button>
                  <el-button class="log-action" link>操作日志</el-button>
                </template>
                <template v-else-if="row.exam">
                  <el-button class="primary-action" link @click="openMonitor(row)">开始考试</el-button>
                  <el-button link type="primary">编辑</el-button>
                  <el-button link type="danger">删除</el-button>
                  <el-button class="more-action" link>更多 <el-icon><ArrowDown /></el-icon></el-button>
                </template>
                <template v-else>
                  <el-button class="primary-action" link @click="openMonitor(row)">监考</el-button>
                  <el-button class="primary-action" link @click="openMarking(row)">阅卷</el-button>
                  <el-button class="primary-action" link @click="openStats(row)">成绩统计</el-button>
                  <el-button class="log-action" link>操作日志</el-button>
                  <el-button class="more-action" link>更多 <el-icon><ArrowDown /></el-icon></el-button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="admin-pagination">
          <span>共 {{ totalCount }} 条记录</span>
          <el-pagination layout="prev, pager, next" :total="totalCount" :page-size="8" />
        </div>
      </div>

      <el-drawer v-model="monitorVisible" size="72%" direction="rtl" class="monitor-drawer">
        <template #header>
          <div class="monitor-title">
            <h2>{{ selectedCourse?.name || '实时监考' }}</h2>
            <p>{{ selectedCourse?.time }} / {{ selectedCourse?.room }}</p>
          </div>
        </template>

        <div class="monitor-grid">
          <article v-for="camera in cameras" :key="camera.name" class="camera-card">
            <div class="camera-screen">
              <span class="live-dot">直播</span>
              <strong>{{ camera.name }}</strong>
              <p>RTSP 可配置接入</p>
            </div>
            <footer>
              <span>{{ camera.location }}</span>
              <el-tag size="small" type="success">在线</el-tag>
            </footer>
          </article>
        </div>

        <div class="monitor-student-panel">
          <div class="panel-heading">
            <h3>学员监控</h3>
            <el-button :icon="Monitor" type="primary" plain>查看学员桌面</el-button>
          </div>
          <el-table :data="students">
            <el-table-column prop="name" label="学员姓名" min-width="100" />
            <el-table-column prop="studentNo" label="学号" min-width="120" />
            <el-table-column prop="topic" label="当前实训题" min-width="160" />
            <el-table-column prop="mode" label="模式" width="92" />
            <el-table-column prop="room" label="所在房间" width="120" />
            <el-table-column prop="ip" label="IP" width="140" />
            <el-table-column label="在线状态" width="104">
              <template #default="{ row }">
                <el-tag :type="row.online ? 'success' : 'info'" size="small">{{ row.online ? '在线' : '离线' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-drawer>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowDown, Monitor, Plus, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

interface CourseRow {
  name: string;
  type: string;
  time: string;
  target: string;
  teacher: string;
  room: string;
  status: '已发布' | '未发布';
  createdAt: string;
  exam?: boolean;
}

const filters = reactive({
  keyword: '',
  time: '',
  status: ''
});

const monitorVisible = ref(false);
const selectedCourse = ref<CourseRow>();
const totalCount = 12;

const courses: CourseRow[] = [
  {
    name: '期末考试',
    type: '考试',
    time: '2025-03-20 08:00\n至 2025-03-20 10:00',
    target: '城轨信号2401班',
    teacher: '李明峰、王志强',
    room: '实训室A-301',
    status: '已发布',
    createdAt: '2025-03-15 10:00',
    exam: true
  },
  {
    name: '信号故障处理综合实训',
    type: '考试',
    time: '2025-03-20 08:00\n至 2025-03-20 10:00',
    target: '城轨信号2401班',
    teacher: '李明峰、王志强',
    room: '实训室A-301',
    status: '已发布',
    createdAt: '2025-03-15 10:00'
  },
  {
    name: '列车驾驶模拟实训考核',
    type: '考试',
    time: '2025-03-12 09:00\n至 2025-03-13 11:00',
    target: '城轨车辆2401班',
    teacher: '赵建国',
    room: '驾驶模拟室B-101',
    status: '未发布',
    createdAt: '2025-03-18 14:30'
  },
  {
    name: '站务应急处置实训',
    type: '练习',
    time: '2025-03-10 14:00\n至 2025-03-10 16:00',
    target: '张明亮、孙志强...',
    teacher: '陈志远、李明峰',
    room: '实训室C-201',
    status: '已发布',
    createdAt: '2025-03-05 09:00'
  },
  {
    name: '调度指挥综合实训',
    type: '练习',
    time: '2025-03-08 08:30\n至 2025-03-08 11:30',
    target: '城轨运营2401班',
    teacher: '陈志远',
    room: '调度实训室D-401',
    status: '已发布',
    createdAt: '2025-03-01 10:00'
  }
];

const cameras = [
  { name: '教室全景摄像头 01', location: '第一实训室 / 前方' },
  { name: '操作区摄像头 02', location: '第一实训室 / 操作台' },
  { name: '走廊入口摄像头 03', location: '第一实训室 / 入口' },
  { name: '教师端摄像头 04', location: '第一实训室 / 讲台' }
];

const students = [
  { name: '李明', studentNo: '202601001', topic: '设备停送电流程', mode: '协同', room: 'A-01', ip: '192.168.1.21', online: true },
  { name: '周雨', studentNo: '202601002', topic: '安全隔离确认', mode: '协同', room: 'A-01', ip: '192.168.1.22', online: true },
  { name: '陈晓', studentNo: '202601003', topic: '故障票填写', mode: '单人', room: 'B-03', ip: '192.168.1.47', online: false }
];

const filteredCourses = computed(() =>
  courses.filter((course) => {
    const keywordMatched = !filters.keyword || course.name.includes(filters.keyword) || course.target.includes(filters.keyword);
    const statusMatched = !filters.status || course.status === filters.status;
    return keywordMatched && statusMatched;
  })
);

function resetFilters() {
  filters.keyword = '';
  filters.time = '';
  filters.status = '';
}

function refreshCourses() {
  ElMessage.success('已刷新');
}

function openMonitor(row: CourseRow) {
  selectedCourse.value = row;
  monitorVisible.value = true;
}

function openMarking(row: CourseRow) {
  ElMessage.info(`${row.name} 的阅卷入口已预留`);
}

function openStats(row: CourseRow) {
  ElMessage.info(`${row.name} 的成绩统计入口已预留`);
}
</script>
