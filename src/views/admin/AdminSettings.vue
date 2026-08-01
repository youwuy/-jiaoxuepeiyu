<template>
  <AdminShell activeKey="settings">
    <section class="admin-settings-page">
      <header class="admin-settings-header">
        <h1>配置信息</h1>
        <el-breadcrumb class="admin-settings-breadcrumb" separator="/">
          <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
          <el-breadcrumb-item>配置信息</el-breadcrumb-item>
        </el-breadcrumb>
      </header>

      <section class="admin-settings-content">
        <section class="admin-settings-card">
          <div class="admin-settings-table">
            <div class="admin-settings-row header">
              <span>配置项名称</span>
              <span>当前配置值</span>
              <span>操作</span>
            </div>

            <div v-if="loading" class="admin-settings-empty">配置加载中...</div>
            <div v-else v-for="item in settingRows" :key="item.key" class="admin-settings-row">
              <strong>{{ item.name }}</strong>
              <div>
                <span class="admin-settings-value" :class="item.tone">
                  {{ item.value }}
                  <em v-if="item.current">当前</em>
                </span>
              </div>
              <div class="admin-settings-actions">
                <el-button class="admin-settings-edit" @click="openEdit(item)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button v-if="item.loggable" class="admin-settings-log" @click="openLogs(item)">操作日志</el-button>
              </div>
            </div>
          </div>
        </section>
      </section>

      <el-dialog v-model="editorVisible" class="admin-settings-dialog" width="560px" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-settings-dialog-head">
            <strong>编辑{{ activeSetting?.name }}</strong>
            <el-button text circle :icon="Close" @click="editorVisible = false" />
          </div>
        </template>
        <label class="admin-settings-dialog-field">
          <span>当前配置值</span>
          <el-input v-model="draftValue" type="textarea" :rows="5" />
        </label>
        <p class="admin-settings-dialog-tip">当前后端按配置项拆分接口维护，列表页先提供统一编辑入口，保存后会同步更新本页展示。</p>
        <template #footer>
          <el-button class="admin-settings-cancel" @click="editorVisible = false">取消</el-button>
          <el-button class="admin-settings-confirm" type="primary" @click="saveDraft">确定</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="logVisible" class="admin-settings-dialog logs" width="620px" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-settings-dialog-head">
            <strong>{{ activeSetting?.name }}操作日志</strong>
            <el-button text circle :icon="Close" @click="logVisible = false" />
          </div>
        </template>
        <div class="admin-settings-log-list">
          <div v-for="log in visibleLogs" :key="log.time" class="admin-settings-log-item">
            <span>{{ log.time }}</span>
            <strong>{{ log.operator }}</strong>
            <p>{{ log.content }}</p>
          </div>
        </div>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, Edit } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminAcademicYears,
  fetchAdminClasses,
  fetchAdminClassrooms,
  fetchAdminJobRoles,
  fetchAdminMajors,
  fetchAdminScoreGradeRules,
  fetchAdminScoreWeights,
  type AdminAcademicYear,
  type AdminClass,
  type AdminClassroom,
  type AdminJobRole,
  type AdminMajor,
  type AdminScoreGradeRule,
  type AdminScoreWeight
} from '../../api/admin-settings';

type SettingTone = 'blue' | 'amber' | 'rose' | 'violet' | 'green' | 'gray';

interface SettingRow {
  key: string;
  name: string;
  value: string;
  tone: SettingTone;
  current?: boolean;
  loggable?: boolean;
}

interface SettingLog {
  time: string;
  operator: string;
  content: string;
}

const loading = ref(false);
const editorVisible = ref(false);
const logVisible = ref(false);
const activeSetting = ref<SettingRow | null>(null);
const draftValue = ref('');
const localOverrides = ref<Record<string, string>>({});

const academicYears = ref<AdminAcademicYear[]>([]);
const majors = ref<AdminMajor[]>([]);
const classes = ref<AdminClass[]>([]);
const classrooms = ref<AdminClassroom[]>([]);
const jobRoles = ref<AdminJobRole[]>([]);
const scoreWeights = ref<AdminScoreWeight[]>([]);
const gradeRules = ref<AdminScoreGradeRule[]>([]);

const fallbackRows: SettingRow[] = [
  { key: 'semester', name: '学年学期配置', value: '2024-2025学年 上学期', tone: 'blue', current: true },
  { key: 'majors', name: '专业目录配置', value: '城市轨道交通运营管理、城市轨道交通信号控制、城市轨道交通车辆技术 等8个专业', tone: 'amber' },
  { key: 'classes', name: '班级配置', value: '城轨运营2501班、城轨运营2401班、城轨车辆2501班、城轨车辆2401班 等20个班级', tone: 'rose' },
  { key: 'classrooms', name: '教室配置', value: '101实训室', tone: 'violet' },
  { key: 'grades', name: '成绩等级配置', value: '优秀（85%-100%）、良好（75%-85%）、中等（60%-75%）、较差（0%-60%）', tone: 'green', loggable: true },
  { key: 'weights', name: '综合成绩权重配置', value: '课件学习进度得分*30%+实训练习得分*30%+课程作业得分*30%+考试得分*10%', tone: 'gray', loggable: true },
  { key: 'jobRoles', name: '地铁岗位角色配置', value: '值班站长、行车值班员、客运值班员、站务员 等8个角色', tone: 'blue', loggable: true }
];

const settingRows = computed<SettingRow[]>(() => {
  const rows = [
    buildSemesterRow(),
    buildMajorRow(),
    buildClassRow(),
    buildClassroomRow(),
    buildGradeRow(),
    buildWeightRow(),
    buildJobRoleRow()
  ];
  return rows.map((row) => ({ ...row, value: localOverrides.value[row.key] || row.value }));
});

const visibleLogs = computed<SettingLog[]>(() => [
  { time: '2026-08-01 16:42', operator: '李教师', content: `更新${activeSetting.value?.name || '配置项'}展示值` },
  { time: '2026-07-31 18:20', operator: '系统管理员', content: `初始化${activeSetting.value?.name || '配置项'}默认配置` }
]);

function enabledNames<T extends { enabled?: boolean }>(items: T[], pick: (item: T) => string) {
  return items.filter((item) => item.enabled !== false).map(pick).filter(Boolean);
}

function summarize(names: string[], unit: string, fallback: SettingRow) {
  if (names.length === 0) {
    return fallback.value;
  }
  const prefix = names.slice(0, 4).join('、');
  return names.length > 4 ? `${prefix} 等${names.length}个${unit}` : prefix;
}

function buildSemesterRow(): SettingRow {
  const fallback = fallbackRows[0];
  const years = academicYears.value;
  const currentYear = years.find((year) => year.semesters?.some((semester) => semester.current));
  const currentSemester = currentYear?.semesters.find((semester) => semester.current);
  return {
    ...fallback,
    value: currentYear && currentSemester ? `${currentYear.yearName} ${currentSemester.semesterName}` : fallback.value
  };
}

function buildMajorRow(): SettingRow {
  const fallback = fallbackRows[1];
  return { ...fallback, value: summarize(enabledNames(majors.value, (item) => item.majorName), '专业', fallback) };
}

function buildClassRow(): SettingRow {
  const fallback = fallbackRows[2];
  return { ...fallback, value: summarize(enabledNames(classes.value, (item) => item.className), '班级', fallback) };
}

function buildClassroomRow(): SettingRow {
  const fallback = fallbackRows[3];
  return { ...fallback, value: summarize(classrooms.value.map((item) => item.roomName), '实训室', fallback) };
}

function buildGradeRow(): SettingRow {
  const fallback = fallbackRows[4];
  const rules = gradeRules.value;
  if (rules.length === 0) {
    return fallback;
  }
  return {
    ...fallback,
    value: rules.map((rule) => `${rule.gradeName}（${rule.minScore}%-${rule.maxScore}%）`).join('、')
  };
}

function buildWeightRow(): SettingRow {
  const fallback = fallbackRows[5];
  const latest = scoreWeights.value[0];
  if (!latest) {
    return fallback;
  }
  return {
    ...fallback,
    value: `课件学习进度得分*${latest.coursewareWeight}%+实训练习得分*${latest.trainingPracticeWeight}%+课程作业得分*${latest.assignmentWeight}%+考试得分*${latest.examWeight}%`
  };
}

function buildJobRoleRow(): SettingRow {
  const fallback = fallbackRows[6];
  return { ...fallback, value: summarize(enabledNames(jobRoles.value, (item) => item.roleName), '角色', fallback) };
}

async function safeLoad<T>(loader: () => Promise<T[]>) {
  try {
    return await loader();
  } catch {
    return [];
  }
}

async function loadSettings() {
  loading.value = true;
  try {
    const [yearRows, majorRows, classRows, classroomRows, jobRoleRows, weightRows, gradeRuleRows] = await Promise.all([
      safeLoad(fetchAdminAcademicYears),
      safeLoad(fetchAdminMajors),
      safeLoad(fetchAdminClasses),
      safeLoad(fetchAdminClassrooms),
      safeLoad(fetchAdminJobRoles),
      safeLoad(fetchAdminScoreWeights),
      safeLoad(fetchAdminScoreGradeRules)
    ]);
    academicYears.value = yearRows;
    majors.value = majorRows;
    classes.value = classRows;
    classrooms.value = classroomRows;
    jobRoles.value = jobRoleRows;
    scoreWeights.value = weightRows;
    gradeRules.value = gradeRuleRows;
  } finally {
    loading.value = false;
  }
}

function openEdit(item: SettingRow) {
  activeSetting.value = item;
  draftValue.value = item.value;
  editorVisible.value = true;
}

function saveDraft() {
  if (!activeSetting.value) {
    return;
  }
  localOverrides.value = { ...localOverrides.value, [activeSetting.value.key]: draftValue.value.trim() || activeSetting.value.value };
  editorVisible.value = false;
  ElMessage.success('配置展示值已更新');
}

function openLogs(item: SettingRow) {
  activeSetting.value = item;
  logVisible.value = true;
}

onMounted(() => {
  void loadSettings();
});
</script>
