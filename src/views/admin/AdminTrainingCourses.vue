<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-page">
      <el-breadcrumb class="admin-course-breadcrumb" separator="/">
        <el-breadcrumb-item>教学实训</el-breadcrumb-item>
        <el-breadcrumb-item>实训组课</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="admin-training-toolbar">
        <div class="admin-training-filter-row">
          <el-input v-model="filters.keyword" class="admin-training-search" :prefix-icon="Search" placeholder="搜索实训课名称" clearable />
          <el-date-picker
            v-model="filters.time"
            class="admin-training-time-filter"
            type="datetimerange"
            range-separator="至"
            start-placeholder="实训开始时间"
            end-placeholder="实训结束时间"
            clearable
          />
          <el-select v-model="filters.status" class="admin-training-select" placeholder="发布状态" clearable>
            <el-option label="已发布" value="已发布" />
            <el-option label="未发布" value="未发布" />
          </el-select>
          <el-button class="admin-training-ghost" @click="refreshCourses">查询</el-button>
          <el-button class="admin-training-ghost" @click="resetFilters">重置</el-button>
        </div>
        <div class="admin-training-action-row">
          <el-button class="admin-training-primary" type="primary" :icon="Plus" @click="openCreate">新增实训课</el-button>
          <el-button class="admin-training-ghost" :icon="Upload" @click="openOfflineImport">导入线下成绩</el-button>
        </div>
      </div>

      <div class="admin-training-table-card">
        <div v-if="loading" class="admin-course-empty">实训课加载中...</div>
        <div v-else-if="courses.length === 0" class="admin-course-empty">
          <el-empty description="暂无实训课数据" />
        </div>
        <div v-else class="admin-training-table-scroll">
          <table class="admin-training-table">
            <colgroup>
              <col class="admin-training-col-name" />
              <col class="admin-training-col-type" />
              <col class="admin-training-col-time" />
              <col class="admin-training-col-target" />
              <col class="admin-training-col-teacher" />
              <col class="admin-training-col-room" />
              <col class="admin-training-col-status" />
              <col class="admin-training-col-created" />
              <col class="admin-training-col-operation" />
            </colgroup>
            <thead>
              <tr>
                <th>实训课名称</th>
                <th>类型</th>
                <th>实训起止时间</th>
                <th>参训班级/学生</th>
                <th>监考教师</th>
                <th>实训教室</th>
                <th>发布状态</th>
                <th>创建时间</th>
                <th class="admin-training-operation-cell">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="course in courses" :key="course.id">
                <td>
                  <div class="admin-training-name-cell">
                    <strong>{{ course.name }}</strong>
                    <span>{{ course.mode }} / {{ course.topicCount }} 个实训任务</span>
                  </div>
                </td>
                <td><span class="admin-training-type-pill">{{ course.type }}</span></td>
                <td><span class="admin-training-multiline">{{ course.time }}</span></td>
                <td class="admin-training-ellipsis">{{ course.target }}</td>
                <td class="admin-training-ellipsis">{{ course.teacher }}</td>
                <td>{{ course.room }}</td>
                <td>
                  <span class="status-pill" :class="{ muted: course.status === '未发布' }">
                    <i></i>{{ course.status }}
                  </span>
                </td>
                <td>{{ course.createdAt }}</td>
                <td class="admin-training-operation-cell">
                  <div class="admin-row-actions">
                    <template v-if="course.status === '未发布'">
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-button class="publish-action" link @click="openPublish(course)">发布</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                    </template>
                    <template v-else-if="course.exam">
                      <el-button v-if="course.mode === '协同实训' && !course.examStarted" class="primary-action" link @click="openExamStart(course)">开始考试</el-button>
                      <el-button v-else-if="isTrainingOpen(course)" class="primary-action" link @click="openMonitor(course)">监考</el-button>
                      <el-button v-if="isTrainingEnded(course)" class="primary-action" link @click="openMarking(course)">阅卷</el-button>
                      <el-button v-if="isTrainingEnded(course)" class="primary-action" link @click="openStats(course)">成绩统计</el-button>
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-button link @click="withdrawCourse(course)">取消发布</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                    </template>
                    <template v-else>
                      <el-button v-if="isTrainingOpen(course)" class="primary-action" link @click="openMonitor(course)">监考</el-button>
                      <el-button v-if="isTrainingEnded(course)" class="primary-action" link @click="openMarking(course)">阅卷</el-button>
                      <el-button v-if="isTrainingEnded(course)" class="primary-action" link @click="openStats(course)">成绩统计</el-button>
                      <el-button link type="primary" @click="openEdit(course)">编辑</el-button>
                      <el-button link type="danger" @click="confirmDelete(course)">删除</el-button>
                      <el-button link @click="withdrawCourse(course)">取消发布</el-button>
                      <el-button class="log-action" link @click="openLogs(course)">操作日志</el-button>
                    </template>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="admin-pagination">
          <span>共 {{ total }} 条记录</span>
          <el-pagination v-model:current-page="page" layout="prev, pager, next" :total="total" :page-size="pageSize" @current-change="loadCourses" />
        </div>
      </div>

      <el-dialog v-model="publishVisible" class="admin-training-dialog" width="560px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>发布确认</strong><el-button text circle :icon="Close" @click="publishVisible = false" /></div></template>
        <div class="admin-training-publish-confirm">
          <strong>{{ publishTarget?.name || '实训课' }}</strong>
          <p>确定要发布吗？发布后学员可以看到该实训。</p>
        </div>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="publishVisible = false">取消</el-button><el-button type="primary" @click="confirmPublish">确认发布</el-button></div></template>
      </el-dialog>

      <el-dialog v-model="importVisible" class="admin-training-dialog" width="760px" :show-close="false" append-to-body>
        <template #header><div class="admin-training-dialog-head"><strong>导入线下成绩</strong><el-button text circle :icon="Close" @click="importVisible = false" /></div></template>
        <div class="admin-training-import">
          <section class="offline-import-section">
            <strong>1. 下载模板</strong>
            <p>请选择已结束的实训组课，模板将按该组课的实训题生成成绩列。</p>
            <div class="offline-import-row">
              <el-select v-model="offlineTrainingId" placeholder="请选择已结束的实训组课" filterable @change="loadOfflineTopics">
                <el-option v-for="item in offlineTrainingOptions" :key="item.trainingId" :label="item.trainingName" :value="item.trainingId" />
              </el-select>
              <el-button :disabled="!offlineTrainingId" @click="downloadOfflineTemplate">下载导入模板</el-button>
            </div>
          </section>
          <div class="admin-training-upload-box">
            <el-icon><UploadFilled /></el-icon>
            <strong>2. 上传文件</strong>
            <span>仅支持系统模板生成的 .xlsx 文件，禁止修改表头</span>
            <input ref="importInput" class="admin-training-file-input" type="file" accept=".xlsx" @change="handleImportChange" />
            <el-button type="primary" plain :disabled="!offlineTrainingId" @click="importInput?.click()">选择文件</el-button>
            <small v-if="offlineImportFileName">{{ offlineImportFileName }}，共 {{ offlineImportRows.length }} 条</small>
          </div>
          <div class="admin-training-import-result" :class="{ active: offlineImportResult }">
            <strong>3. 导入记录提示</strong>
            <p v-if="offlineImportResult">共 {{ offlineImportResult.totalCount }} 条，成功 {{ offlineImportResult.successCount }} 条，失败 {{ offlineImportResult.failureCount }} 条。</p>
            <p v-else>导入后将展示成功数量和错误明细；重复导入只覆盖原线下成绩。</p>
            <el-button v-if="offlineImportResult?.failureCount" link type="primary" @click="downloadOfflineErrors">下载错误日志</el-button>
          </div>
        </div>
        <template #footer><div class="admin-training-dialog-footer"><el-button @click="importVisible = false">取消</el-button><el-button type="primary" :loading="importLoading" :disabled="!offlineTrainingId || !offlineImportRows.length" @click="confirmImport">确认导入</el-button></div></template>
      </el-dialog>

      <el-drawer v-model="logVisible" class="admin-training-log-drawer" direction="rtl" size="520px" :with-header="false">
        <div class="admin-training-drawer-head compact">
          <div><span>操作日志</span><h3>{{ selectedCourse?.name || '实训课记录' }}</h3></div>
          <el-button text circle :icon="Close" @click="logVisible = false" />
        </div>
        <div v-if="logs.length" class="admin-training-log-table-scroll">
          <table class="admin-training-log-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>操作人</th>
                <th>操作时间</th>
                <th>操作内容</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in logs" :key="`${item.time}-${index}`">
                <td>{{ index + 1 }}</td>
                <td>{{ item.operator }}</td>
                <td>{{ item.time }}</td>
                <td>{{ item.action }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <el-empty v-else description="暂无操作日志" />
      </el-drawer>

    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Plus, Search, Upload, UploadFilled } from '@element-plus/icons-vue';
import * as XLSX from 'xlsx';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  cancelPublishAdminTraining,
  deleteAdminTraining,
  fetchAdminTraining,
  fetchAdminTrainingLogs,
  fetchAdminTrainingTopics,
  fetchAdminTrainings,
  importAdminTrainingOfflineScores,
  publishAdminTraining,
  type AdminTraining,
  type AdminTrainingLog,
  type AdminTrainingOfflineScoreImportResult,
  type AdminTrainingOfflineScoreImportRow,
  type AdminTrainingTopic
} from '../../api/admin-training';

const router = useRouter();

type CourseStatus = '已发布' | '未发布';

interface CourseRow {
  id: number;
  name: string;
  type: '考试' | '练习';
  mode: '单人实训' | '协同实训';
  time: string;
  target: string;
  teacher: string;
  room: string;
  status: CourseStatus;
  createdAt: string;
  topicCount: number;
  openStartTime?: string;
  openEndTime?: string;
  exam?: boolean;
  examStarted?: boolean;
}

const filters = reactive({ keyword: '', time: [] as Date[], status: '' });
const loading = ref(false);
const page = ref(1);
const pageSize = 8;
const total = ref(0);
const selectedCourse = ref<CourseRow>();
const logVisible = ref(false);
const publishVisible = ref(false);
const publishTarget = ref<CourseRow>();
const importVisible = ref(false);
const importInput = ref<HTMLInputElement>();
const importLoading = ref(false);
const offlineTrainingId = ref<number>();
const offlineTrainingOptions = ref<AdminTraining[]>([]);
const offlineTrainingTopics = ref<AdminTrainingTopic[]>([]);
const offlineImportFileName = ref('');
const offlineImportRows = ref<AdminTrainingOfflineScoreImportRow[]>([]);
const offlineImportResult = ref<AdminTrainingOfflineScoreImportResult>();

const courses = ref<CourseRow[]>([]);

const logs = ref<Array<{ time: string; operator: string; action: string; content: string }>>([]);

function resetFilters() {
  filters.keyword = '';
  filters.time = [];
  filters.status = '';
  page.value = 1;
  void loadCourses();
}

function refreshCourses() {
  page.value = 1;
  void loadCourses();
}

function openCreate() {
  router.push({ name: 'admin-training-new' });
}

async function openEdit(course: CourseRow) {
  if (course.status === '已发布') {
    try {
      await ElMessageBox.confirm(`确定要编辑【${course.name}】吗？编辑后若有学习数据将无法恢复，请谨慎操作`, '编辑实训课', {
        type: 'warning',
        confirmButtonText: '继续编辑',
        cancelButtonText: '取消'
      });
    } catch {
      return;
    }
  }
  router.push({ name: 'admin-training-edit', params: { id: course.id } });
}

function openPublish(course: CourseRow) {
  publishTarget.value = course;
  publishVisible.value = true;
}

async function confirmPublish() {
  if (!publishTarget.value) return;
  try {
    await publishAdminTraining(publishTarget.value.id);
    publishVisible.value = false;
    ElMessage.success('已发布并通知参训学员');
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发布失败');
  }
}

async function openOfflineImport() {
  importVisible.value = true;
  offlineTrainingId.value = undefined;
  offlineTrainingTopics.value = [];
  offlineImportFileName.value = '';
  offlineImportRows.value = [];
  offlineImportResult.value = undefined;
  if (importInput.value) importInput.value.value = '';
  try {
    const result = await fetchAdminTrainings({ publishStatus: 'PUBLISHED', page: 1, pageSize: 100 });
    const now = Date.now();
    offlineTrainingOptions.value = result.records.filter((item) => item.openEndTime && new Date(item.openEndTime).getTime() < now);
  } catch (error) {
    offlineTrainingOptions.value = [];
    ElMessage.error(error instanceof Error ? error.message : '已结束实训组课加载失败');
  }
}

async function loadOfflineTopics() {
  offlineImportFileName.value = '';
  offlineImportRows.value = [];
  offlineImportResult.value = undefined;
  if (importInput.value) importInput.value.value = '';
  if (!offlineTrainingId.value) {
    offlineTrainingTopics.value = [];
    return;
  }
  try {
    const [detail, topics] = await Promise.all([fetchAdminTraining(offlineTrainingId.value), fetchAdminTrainingTopics()]);
    const topicIds = new Set(detail.topicIds || []);
    offlineTrainingTopics.value = topics.filter((item) => topicIds.has(item.topicId));
  } catch (error) {
    offlineTrainingTopics.value = [];
    ElMessage.error(error instanceof Error ? error.message : '实训题信息加载失败');
  }
}

function offlineTopicHeader(topic: AdminTrainingTopic) {
  return `${topic.topicName}（题目ID:${topic.topicId}）`;
}

function downloadOfflineTemplate() {
  if (!offlineTrainingId.value || !offlineTrainingTopics.value.length) {
    ElMessage.warning('该实训组课未配置实训题');
    return;
  }
  const headers = ['学员姓名', '工号', '班级', ...offlineTrainingTopics.value.map(offlineTopicHeader), '总成绩', '训练备注'];
  const worksheet = XLSX.utils.aoa_to_sheet([headers]);
  worksheet['!cols'] = headers.map((header) => ({ wch: Math.max(14, Math.min(32, header.length + 4)) }));
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '线下实训成绩');
  const training = offlineTrainingOptions.value.find((item) => item.trainingId === offlineTrainingId.value);
  XLSX.writeFile(workbook, `${training?.trainingName || '实训组课'}-线下成绩导入模板.xlsx`);
}

function numericCell(value: unknown) {
  if (value === '' || value === null || value === undefined) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

async function handleImportChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;
  if (!/\.xlsx$/i.test(file.name)) {
    ElMessage.error('仅支持 .xlsx 格式');
    return;
  }
  try {
    const workbook = XLSX.read(await file.arrayBuffer(), { type: 'array' });
    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    const rows = XLSX.utils.sheet_to_json<Record<string, unknown>>(sheet, { defval: '' });
    const requiredHeaders = ['学员姓名', '工号', '班级', ...offlineTrainingTopics.value.map(offlineTopicHeader), '总成绩', '训练备注'];
    const actualHeaders = rows[0] ? Object.keys(rows[0]).map((item) => item.trim()) : [];
    if (requiredHeaders.some((header) => !actualHeaders.includes(header))) {
      ElMessage.error('模板表头不完整，请重新下载当前组课的导入模板');
      return;
    }
    offlineImportFileName.value = file.name;
    offlineImportResult.value = undefined;
    offlineImportRows.value = rows.map((row, index) => ({
      rowNumber: index + 2,
      studentName: String(row['学员姓名'] || '').trim(),
      studentNo: String(row['工号'] || '').trim(),
      className: String(row['班级'] || '').trim(),
      totalScore: numericCell(row['总成绩']),
      remark: String(row['训练备注'] || '').trim() || undefined,
      topicScores: Object.fromEntries(offlineTrainingTopics.value.map((topic) => [topic.topicId, numericCell(row[offlineTopicHeader(topic)])]))
    }));
    if (!rows.length) ElMessage.warning('导入文件没有可识别的数据行');
  } catch {
    ElMessage.error('文件解析失败，请使用当前组课下载的 .xlsx 模板');
  }
}

async function confirmImport() {
  if (!offlineTrainingId.value || !offlineImportFileName.value || !offlineImportRows.value.length) return;
  importLoading.value = true;
  try {
    offlineImportResult.value = await importAdminTrainingOfflineScores({
      trainingId: offlineTrainingId.value,
      fileName: offlineImportFileName.value,
      rows: offlineImportRows.value
    });
    if (offlineImportResult.value.failureCount) {
      ElMessage.warning(`成功 ${offlineImportResult.value.successCount} 条，失败 ${offlineImportResult.value.failureCount} 条`);
    } else {
      ElMessage.success(`已导入 ${offlineImportResult.value.successCount} 条线下成绩`);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '线下成绩导入失败');
  } finally {
    importLoading.value = false;
  }
}

function downloadOfflineErrors() {
  if (!offlineImportResult.value?.errors.length) return;
  const worksheet = XLSX.utils.json_to_sheet(offlineImportResult.value.errors.map((item) => ({
    行号: item.rowNumber,
    工号: item.studentNo || '',
    错误原因: item.message
  })));
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '错误明细');
  XLSX.writeFile(workbook, `线下成绩导入错误日志-批次${offlineImportResult.value.batchId}.xlsx`);
}

function openMonitor(row: CourseRow) {
  router.push({
    name: 'admin-training-monitor',
    params: { id: row.id },
    query: { title: row.name, time: row.time, room: row.room }
  });
}

function openExamStart(row: CourseRow) {
  router.push({
    name: 'admin-training-exam-start',
    params: { id: row.id },
    query: { title: row.name, time: row.time, room: row.room }
  });
}

function openMarking(row: CourseRow) {
  if (!isTrainingEnded(row)) {
    ElMessage.warning('实训结束后才可进入阅卷');
    return;
  }
  router.push({
    name: 'admin-training-reviews',
    params: { id: row.id },
    query: { title: row.name }
  });
}

function openStats(row: CourseRow) {
  router.push({
    name: 'admin-training-statistics',
    params: { id: row.id },
    query: { title: row.name, target: row.target }
  });
}

async function openLogs(row: CourseRow) {
  selectedCourse.value = row;
  try {
    logs.value = (await fetchAdminTrainingLogs(row.id)).map(mapLog);
  } catch (error) {
    logs.value = [];
    ElMessage.error(error instanceof Error ? error.message : '操作日志加载失败');
  }
  logVisible.value = true;
}

async function confirmDelete(course: CourseRow) {
  await ElMessageBox.confirm(`确定要删除【${course.name}】吗？删除后实训课及学习数据将无法恢复，请谨慎操作`, '删除实训课', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' });
  try {
    await deleteAdminTraining(course.id);
    ElMessage.success('实训课已删除');
    await loadCourses();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  }
}

async function withdrawCourse(course: CourseRow) {
  try {
    await ElMessageBox.confirm(`确定要取消发布【${course.name}】吗？取消后学员将无法继续访问该实训`, '取消发布', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    await cancelPublishAdminTraining(course.id);
    ElMessage.success('已取消发布');
    await loadCourses();
  } catch (error) {
    if (error === 'cancel' || error === 'close') return;
    ElMessage.error(error instanceof Error ? error.message : '取消发布失败');
  }
}

async function loadCourses() {
  loading.value = true;
  try {
    const result = await fetchAdminTrainings({
      keyword: filters.keyword.trim() || undefined,
      publishStatus: statusToApi(filters.status),
      rangeStart: filters.time[0] ? formatLocalDateTime(filters.time[0]) : undefined,
      rangeEnd: filters.time[1] ? formatLocalDateTime(filters.time[1]) : undefined,
      page: page.value,
      pageSize
    });
    courses.value = result.records.map(mapCourse);
    total.value = result.total;
  } catch (error) {
    courses.value = [];
    total.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '实训课列表加载失败');
  } finally {
    loading.value = false;
  }
}

function mapCourse(item: AdminTraining): CourseRow {
  const type = apiTrainingTypeToText(item.trainingType);
  return {
    id: item.trainingId,
    name: item.trainingName || '-',
    type,
    mode: apiTrainingModeToText(item.trainingMode),
    time: `${formatDateTime(item.openStartTime)}\n至 ${formatDateTime(item.openEndTime)}`,
    target: item.classNames || '-',
    teacher: item.teacherNames || '-',
    room: item.classroomName || '-',
    status: apiStatusToText(item.publishStatus),
    createdAt: formatDateTime(item.createdAt),
    topicCount: item.topicCount || 0,
    openStartTime: item.openStartTime,
    openEndTime: item.openEndTime,
    exam: type === '考试',
    examStarted: Boolean(item.examStartedAt)
  };
}

function isTrainingOpen(course: CourseRow) {
  if (!course.openStartTime || !course.openEndTime) return false;
  const now = Date.now();
  return now >= new Date(course.openStartTime).getTime() && now <= new Date(course.openEndTime).getTime();
}

function isTrainingEnded(course: CourseRow) {
  return Boolean(course.openEndTime && Date.now() >= new Date(course.openEndTime).getTime());
}

function mapLog(item: AdminTrainingLog) {
  const actionText: Record<string, string> = {
    CREATE: '新增',
    UPDATE: '编辑',
    PUBLISH: '发布',
    CANCEL_PUBLISH: '取消发布',
    START_EXAM: '开始考试'
  };
  const action = String(item.action || '').toUpperCase();
  return {
    time: formatDateTime(item.createdAt),
    operator: item.operatorName || '-',
    action: actionText[action] || item.action || '-',
    content: item.content || '-'
  };
}

function apiStatusToText(status?: string): CourseStatus {
  return status === 'PUBLISHED' || status === 'published' ? '已发布' : '未发布';
}

function statusToApi(status?: string) {
  if (status === '已发布') return 'PUBLISHED';
  if (status === '未发布') return 'UNPUBLISHED';
  return undefined;
}

function apiTrainingTypeToText(type?: string): '考试' | '练习' {
  return type === 'PRACTICE' ? '练习' : '考试';
}

function apiTrainingModeToText(mode?: string): '单人实训' | '协同实训' {
  return mode === 'SINGLE' ? '单人实训' : '协同实训';
}

function formatDateTime(value?: string) {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

function formatLocalDateTime(value: Date) {
  const pad = (part: number) => String(part).padStart(2, '0');
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`;
}

onMounted(() => {
  void loadCourses();
});
</script>

<style scoped>
.admin-training-toolbar,
.admin-training-table-card {
  border: 1px solid #dfe6f0;
  border-radius: 10px;
  background: #ffffff;
}

.admin-training-toolbar {
  min-height: 124px;
  padding: 16px 20px;
}

.admin-training-filter-row,
.admin-training-action-row,
.admin-training-dialog-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-training-action-row {
  margin-top: 12px;
}

.admin-training-search {
  width: 240px;
}

.admin-training-select {
  width: 128px;
}

.admin-training-primary.el-button,
.admin-training-ghost.el-button {
  height: 40px;
  border-radius: 8px;
  font-weight: 800;
}

.admin-training-primary.el-button {
  border: 0;
  background: #3478f6;
}

.admin-training-ghost.el-button {
  border-color: #dfe6f0;
  background: #ffffff;
  color: #536681;
}

.admin-training-table-card {
  overflow: hidden;
}

.admin-training-table-scroll {
  width: 100%;
  overflow-x: auto;
}

.admin-training-table {
  width: 100%;
  min-width: 1584px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-col-name {
  width: 220px;
}

.admin-training-col-type {
  width: 82px;
}

.admin-training-col-time {
  width: 170px;
}

.admin-training-col-target {
  width: 170px;
}

.admin-training-col-teacher {
  width: 120px;
}

.admin-training-col-room {
  width: 120px;
}

.admin-training-col-status {
  width: 100px;
}

.admin-training-col-created {
  width: 142px;
}

.admin-training-col-operation {
  width: 360px;
}

.admin-training-table th {
  height: 52px;
  padding: 0 12px;
  border-bottom: 1px solid #edf2f8;
  background: #f8fafc;
  color: #263a55;
  font-size: 13px;
  text-align: left;
}

.admin-training-table td {
  height: 68px;
  padding: 0 12px;
  border-bottom: 1px solid #edf2f8;
  color: #334155;
  font-size: 13px;
  vertical-align: middle;
}

.admin-training-table th.admin-training-operation-cell,
.admin-training-table td.admin-training-operation-cell {
  padding-right: 14px;
  padding-left: 14px;
}

.admin-training-table td.admin-training-operation-cell {
  vertical-align: middle;
}

.admin-training-operation-cell .admin-row-actions {
  flex-wrap: nowrap;
  max-width: none;
  padding: 0;
}

.admin-training-name-cell {
  display: grid;
  gap: 4px;
}

.admin-training-name-cell strong {
  color: #1e293b;
  font-size: 14px;
}

.admin-training-name-cell span {
  color: #64748b;
  font-size: 12px;
}

.admin-training-type-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 24px;
  border-radius: 12px;
  background: #eef5ff;
  color: #3478f6;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-multiline {
  white-space: pre-line;
  line-height: 1.45;
}

.admin-training-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-training-drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #edf2f8;
  background: #ffffff;
}

.admin-training-drawer-head.compact {
  margin: -20px -20px 20px;
}

.admin-training-drawer-head span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.admin-training-drawer-head h3 {
  margin: 6px 0 0;
  color: #17233d;
  font-size: 20px;
  line-height: 28px;
}

.admin-training-dialog-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.admin-training-dialog-head strong {
  color: #17233d;
  font-size: 17px;
}

.admin-training-dialog-footer {
  justify-content: flex-end;
}

.admin-training-import {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.offline-import-section {
  grid-column: 1 / -1;
  display: grid;
  gap: 10px;
  padding: 18px;
  border: 1px solid #dfe7f1;
  border-radius: 8px;
}

.offline-import-section > p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.offline-import-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.admin-training-upload-box,
.admin-training-import-result,
.admin-training-publish-confirm {
  display: grid;
  gap: 10px;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
}

.admin-training-upload-box .el-icon {
  color: #3478f6;
  font-size: 34px;
}

.admin-training-import-result.active {
  border-style: solid;
  border-color: #10b981;
  background: #ecfdf5;
}

.admin-training-publish-confirm strong,
.admin-training-upload-box strong,
.admin-training-import-result strong {
  color: #17233d;
  font-size: 15px;
}

.admin-training-publish-confirm p,
.admin-training-upload-box span,
.admin-training-import-result p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.admin-training-log-table-scroll {
  overflow-x: auto;
}

.admin-training-log-table {
  width: 100%;
  min-width: 460px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-log-table th,
.admin-training-log-table td {
  height: 48px;
  border-bottom: 1px solid #e5ebf3;
  padding: 0 12px;
  color: #53657d;
  font-size: 13px;
  text-align: left;
}

.admin-training-log-table th {
  background: #f7f9fc;
  color: #8390a3;
  font-weight: 800;
}

.admin-training-log-table th:first-child,
.admin-training-log-table td:first-child {
  width: 56px;
  text-align: center;
}

.admin-training-log-table th:nth-child(2),
.admin-training-log-table td:nth-child(2) {
  width: 90px;
}

.admin-training-log-table th:nth-child(3),
.admin-training-log-table td:nth-child(3) {
  width: 150px;
}

@media (max-width: 980px) {
  .admin-training-filter-row,
  .admin-training-action-row {
    flex-wrap: wrap;
  }

  .admin-training-import {
    grid-template-columns: 1fr;
  }
}
</style>
