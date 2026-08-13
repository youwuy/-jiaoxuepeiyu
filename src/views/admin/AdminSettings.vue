<template>
  <AdminShell activeKey="settings">
    <section class="admin-settings-page">
      <header class="admin-settings-header">
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
                <el-button class="admin-settings-edit" @click="openConfig(item.key)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button v-if="item.loggable" class="admin-settings-log" @click="openLogs(item)">操作日志</el-button>
              </div>
            </div>
          </div>
        </section>
      </section>

      <el-dialog v-model="configVisible" class="admin-settings-config-dialog" :width="configWidth" :show-close="false" :close-on-click-modal="false" append-to-body>
        <template #header>
          <div class="admin-settings-dialog-head">
            <strong>{{ configTitle }}</strong>
            <el-button text circle :icon="Close" @click="closeConfig" />
          </div>
        </template>

        <section v-if="activeConfig === 'semester'" class="admin-settings-panel">
          <el-button class="admin-settings-add-button" @click="openAdd('year')">
            <el-icon><Plus /></el-icon>
            添加学年
          </el-button>
          <table class="admin-settings-modal-table semester">
            <thead>
              <tr>
                <th>序号</th>
                <th>学年</th>
                <th>学期</th>
                <th>是否为当前学期</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in semesterRows" :key="row.semesterId">
                <td>{{ row.index }}</td>
                <td>{{ row.yearName }}</td>
                <td>{{ row.semesterName }}</td>
                <td>
                  <button type="button" class="admin-settings-radio" :class="{ active: row.semesterId === selectedSemesterId }" @click="selectCurrentSemester(row.semesterId)"></button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <section v-else-if="activeConfig === 'majors'" class="admin-settings-panel">
          <el-button class="admin-settings-add-button" @click="openAdd('major')">
            <el-icon><Plus /></el-icon>
            添加专业
          </el-button>
          <table class="admin-settings-modal-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>专业名称</th>
                <th>启用状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(major, index) in displayMajors" :key="major.majorId">
                <td>{{ index + 1 }}</td>
                <td>{{ major.majorName }}</td>
                <td><span class="admin-settings-status" :class="{ disabled: !major.enabled }">{{ major.enabled ? '已启用' : '已禁用' }}</span></td>
                <td>
                  <button class="admin-settings-link" @click="setMajorStatus(major.majorId, true)">启用</button>
                  <button class="admin-settings-link danger" @click="setMajorStatus(major.majorId, false)">禁用</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <section v-else-if="activeConfig === 'classes'" class="admin-settings-panel">
          <el-button class="admin-settings-add-button" @click="openAdd('class')">
            <el-icon><Plus /></el-icon>
            新增班级
          </el-button>
          <table class="admin-settings-modal-table classes">
            <colgroup>
              <col class="class-index-column" />
              <col class="class-name-column" />
              <col class="class-status-column" />
              <col class="class-action-column" />
            </colgroup>
            <thead>
              <tr>
                <th>序号</th>
                <th>班级名称</th>
                <th>启用状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in displayClasses" :key="item.classId">
                <td>{{ index + 1 }}</td>
                <td>{{ item.className }}</td>
                <td class="admin-settings-class-status-cell">
                  <span class="admin-settings-pill-status" :class="{ disabled: !item.enabled }">
                    <i></i>
                    {{ item.enabled ? '已启用' : '已禁用' }}
                  </span>
                </td>
                <td>
                  <div class="admin-settings-class-actions">
                    <el-button class="admin-settings-table-action" :disabled="item.enabled" @click="setClassStatus(item.classId, true)">启用</el-button>
                    <el-button class="admin-settings-table-action danger" :disabled="!item.enabled" @click="setClassStatus(item.classId, false)">禁用</el-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <footer class="admin-settings-modal-pagination">
            <span>共 {{ displayClasses.length }} 条记录</span>
            <el-pagination :current-page="1" :page-size="10" :total="displayClasses.length" layout="prev, pager, next" background />
          </footer>
        </section>

        <section v-else-if="activeConfig === 'classrooms'" class="admin-settings-panel classroom">
          <el-button class="admin-settings-add-button" @click="openAdd('room')">
            <el-icon><Plus /></el-icon>
            添加教室
          </el-button>
          <template v-if="classroomCameraRows.length">
            <table class="admin-settings-modal-table classroom">
              <thead>
                <tr>
                  <th rowspan="2">序号</th>
                  <th rowspan="2">教室名称</th>
                  <th colspan="6">摄像头参数</th>
                  <th rowspan="2">操作</th>
                </tr>
                <tr>
                  <th>NVR主机IP</th>
                  <th>端口</th>
                  <th>管理员账号</th>
                  <th>管理员密码</th>
                  <th>NVR通道编号</th>
                  <th>NVR对外RTSP取流地址</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(camera, index) in classroomCameraRows" :key="camera.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ camera.roomName }}</td>
                  <td>{{ camera.host }}</td>
                  <td>{{ camera.port }}</td>
                  <td>{{ camera.account }}</td>
                  <td>{{ camera.password }}</td>
                  <td>{{ camera.channel }}</td>
                  <td class="wrap">{{ camera.url }}</td>
                  <td>
                    <button class="admin-settings-link" @click="editRoom(camera)">编辑</button>
                    <button class="admin-settings-link danger" @click="removeCamera(camera.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </template>
          <el-empty v-else description="暂无教室摄像头配置" />
        </section>

        <section v-else-if="activeConfig === 'grades'" class="admin-settings-grade-form">
          <el-button class="admin-settings-add-button" @click="addGradeDraft">
            <el-icon><Plus /></el-icon>
            新增成绩等级
          </el-button>
          <table class="admin-settings-modal-table admin-settings-grade-table">
            <thead>
              <tr>
                <th>等级名称</th>
                <th>起始百分比(%)</th>
                <th>结束百分比(%)</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in gradeDraftRows" :key="row.id">
                <td>
                  <el-input
                    v-model="row.gradeName"
                    class="admin-settings-grade-name-input"
                    maxlength="10"
                    placeholder="请输入等级名称"
                  />
                </td>
                <td>
                  <el-input v-model.number="row.minScore" class="admin-settings-grade-score-input" type="number" :min="0" :max="100" />
                </td>
                <td>
                  <el-input v-model.number="row.maxScore" class="admin-settings-grade-score-input" type="number" :min="0" :max="100" />
                </td>
                <td>
                  <el-button class="admin-settings-grade-delete" text circle :icon="Delete" @click="removeGradeDraft(row.id)" />
                </td>
              </tr>
            </tbody>
          </table>
          <div class="admin-settings-effective">
            <strong>生效范围</strong>
            <span><i></i>等级修改后仅对本学期及未来学期的实训考试成绩生效</span>
          </div>
        </section>

        <section v-else-if="activeConfig === 'weights'" class="admin-settings-weight-form">
          <div class="admin-settings-alert">四项权重之和需等于100%，请合理分配</div>
          <label>
            <span>课件学习进度得分 <b>*</b></span>
            <div><el-input v-model.number="weightForm.coursewareWeight" class="admin-settings-weight-input" type="number" :min="0" :max="100" /><em>%</em></div>
          </label>
          <label>
            <span>实训练习得分 <b>*</b></span>
            <div><el-input v-model.number="weightForm.trainingPracticeWeight" class="admin-settings-weight-input" type="number" :min="0" :max="100" /><em>%</em></div>
          </label>
          <label>
            <span>课程作业得分 <b>*</b></span>
            <div><el-input v-model.number="weightForm.assignmentWeight" class="admin-settings-weight-input" type="number" :min="0" :max="100" /><em>%</em></div>
          </label>
          <label>
            <span>考试得分 <b>*</b></span>
            <div><el-input v-model.number="weightForm.examWeight" class="admin-settings-weight-input" type="number" :min="0" :max="100" /><em>%</em></div>
          </label>
          <p>当前权重总和：<strong :class="{ error: weightTotal !== 100 }">{{ weightTotal }}%</strong></p>
          <div class="admin-settings-effective">
            <strong>生效范围</strong>
            <span><i></i>权重修改后仅对本学期及未来学期的综合成绩生效</span>
          </div>
        </section>

        <template v-if="activeConfig === 'semester' || activeConfig === 'grades' || activeConfig === 'weights'" #footer>
          <div class="admin-settings-dialog-footer">
            <el-button @click="closeConfig">取消</el-button>
            <el-button type="primary" @click="saveActiveConfig">确定</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="addVisible" class="admin-settings-add-dialog" :width="addKind === 'room' ? '720px' : '480px'" :show-close="false" :close-on-click-modal="false" append-to-body>
        <template #header>
          <div class="admin-settings-dialog-head">
            <strong>{{ addTitle }}</strong>
            <el-button text circle :icon="Close" @click="addVisible = false" />
          </div>
        </template>

        <section v-if="addKind === 'year'" class="admin-settings-add-form">
          <label>
            <span>学年 <b>*</b></span>
            <el-select v-model="addYearValue" placeholder="请选择学年">
              <el-option v-for="year in yearOptions" :key="year" :label="year" :value="year" />
            </el-select>
          </label>
        </section>

        <section v-else-if="addKind === 'major'" class="admin-settings-add-form">
          <label>
            <span>专业名称 <b>*</b></span>
            <el-input v-model="addMajorName" maxlength="20" placeholder="请输入专业名称" />
            <small>最多输入20个字</small>
          </label>
        </section>

        <section v-else-if="addKind === 'class'" class="admin-settings-add-form">
          <label>
            <span>班级名称 <b>*</b></span>
            <el-input v-model="addClassName" maxlength="20" placeholder="请输入班级名称" />
            <small>最多输入20个字</small>
          </label>
        </section>

        <section v-else-if="addKind === 'room'" class="admin-settings-room-form">
          <label class="wide">
            <span>教室名称 <b>*</b></span>
            <el-input v-model="roomForm.roomName" maxlength="20" placeholder="请输入教室名称" />
            <small>最多输入20个字</small>
          </label>
          <div class="admin-settings-room-title">
            <strong>摄像头参数</strong>
            <el-button class="admin-settings-add-camera" @click="addCamera">
              <el-icon><Plus /></el-icon>
              添加摄像头
            </el-button>
          </div>
          <section v-for="(camera, index) in roomForm.cameras" :key="camera.id" class="admin-settings-camera-card">
            <header><strong>{{ index + 1 }}</strong><span>摄像头</span><button @click="removeRoomCamera(camera.id)">删除</button></header>
            <div class="admin-settings-camera-grid">
              <label class="host"><span>NVR主机IP <b>*</b></span><el-input v-model="camera.host" placeholder="请输入NVR主机IP" /></label>
              <label><span>端口 <b>*</b></span><el-input v-model="camera.port" placeholder="请输入端口" /></label>
              <label><span>管理员账号 <b>*</b></span><el-input v-model="camera.account" placeholder="请输入管理员账号" /></label>
              <label><span>管理员密码 <b>*</b></span><el-input v-model="camera.password" placeholder="请输入管理员密码" /></label>
              <label><span>NVR通道编号 <b>*</b></span><el-input v-model="camera.channel" placeholder="请输入通道编号" /></label>
              <label class="url"><span>NVR对外RTSP/HTTP取流地址</span><el-input v-model="camera.url" placeholder="请输入RTSP/HTTP取流地址" /></label>
            </div>
          </section>
        </section>

        <template #footer>
          <div class="admin-settings-dialog-footer">
            <el-button @click="addVisible = false">取消</el-button>
            <el-button type="primary" @click="saveAdd">确定</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="logVisible" class="admin-settings-log-dialog" width="720px" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-settings-dialog-head">
            <strong>操作日志</strong>
            <el-button text circle :icon="Close" @click="logVisible = false" />
          </div>
        </template>
        <template v-if="visibleLogs.length">
          <table class="admin-settings-modal-table logs">
            <thead>
              <tr>
                <th>序号</th>
                <th>修改前内容</th>
                <th>修改后内容</th>
                <th>操作人</th>
                <th>操作时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(log, index) in visibleLogs" :key="log.time">
                <td>{{ index + 1 }}</td>
                <td>{{ log.before }}</td>
                <td>{{ log.after }}</td>
                <td>{{ log.operator }}</td>
                <td>{{ log.time }}</td>
              </tr>
            </tbody>
          </table>
          <footer class="admin-settings-modal-pagination">
            <span>共 {{ visibleLogs.length }} 条记录</span>
            <el-pagination :current-page="1" :page-size="10" :total="visibleLogs.length" layout="prev, pager, next" background />
          </footer>
        </template>
        <el-empty v-else description="暂无操作日志" />
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, Delete, Edit, Plus } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  createAdminAcademicYear,
  createAdminClass,
  createAdminClassroom,
  createAdminMajor,
  createAdminScoreWeight,
  deleteAdminClassroom,
  disableAdminClass,
  disableAdminMajor,
  enableAdminClass,
  enableAdminMajor,
  fetchAdminAcademicYears,
  fetchAdminClasses,
  fetchAdminClassrooms,
  fetchAdminMajors,
  fetchAdminScoreGradeRuleLogs,
  fetchAdminScoreGradeRules,
  fetchAdminScoreWeights,
  replaceAdminScoreGradeRules,
  setAdminCurrentSemester,
  updateAdminClassroom,
  type AdminAcademicYear,
  type AdminCamera,
  type AdminCameraCommand,
  type AdminClass,
  type AdminClassroomCommand,
  type AdminClassroom,
  type AdminMajor,
  type AdminScoreGradeRule,
  type AdminScoreGradeRuleLog,
  type AdminScoreWeight
} from '../../api/admin-settings';

type SettingTone = 'blue' | 'amber' | 'rose' | 'violet' | 'green' | 'gray';
type ConfigKey = 'semester' | 'majors' | 'classes' | 'classrooms' | 'grades' | 'weights';
type AddKind = 'year' | 'major' | 'class' | 'room';

interface SettingRow {
  key: ConfigKey;
  name: string;
  value: string;
  tone: SettingTone;
  current?: boolean;
  loggable?: boolean;
}

interface SettingLog {
  time: string;
  operator: string;
  before: string;
  after: string;
}

interface SemesterDisplayRow {
  semesterId: number;
  index: number;
  yearName: string;
  semesterName: string;
  current: boolean;
}

interface CameraRow {
  id: number;
  classroomId?: number;
  cameraId?: number;
  roomName: string;
  host: string;
  port: string;
  account: string;
  password: string;
  channel: string;
  url: string;
}

interface GradeDraftRow {
  id: number;
  ruleId?: number;
  gradeName: string;
  minScore: number;
  maxScore: number;
}

const loading = ref(false);
const configVisible = ref(false);
const addVisible = ref(false);
const logVisible = ref(false);
const activeConfig = ref<ConfigKey>('semester');
const activeSetting = ref<SettingRow | null>(null);
const addKind = ref<AddKind>('year');
const addYearValue = ref('');
const selectedSemesterId = ref<number | null>(null);
const addMajorName = ref('');
const addClassName = ref('');
const editingClassroomId = ref<number | null>(null);

const academicYears = ref<AdminAcademicYear[]>([]);
const majors = ref<AdminMajor[]>([]);
const classes = ref<AdminClass[]>([]);
const classrooms = ref<AdminClassroom[]>([]);
const scoreWeights = ref<AdminScoreWeight[]>([]);
const gradeRules = ref<AdminScoreGradeRule[]>([]);
const gradeRuleLogs = ref<AdminScoreGradeRuleLog[]>([]);
const localCameras = ref<CameraRow[]>([]);
const gradeDraftRows = ref<GradeDraftRow[]>([]);

const weightForm = reactive({
  coursewareWeight: 0,
  trainingPracticeWeight: 0,
  assignmentWeight: 0,
  examWeight: 0
});

const roomForm = reactive<{ roomName: string; cameras: CameraRow[] }>({
  roomName: '',
  cameras: []
});

const settingRows = computed<SettingRow[]>(() => [
  buildSemesterRow(),
  buildMajorRow(),
  buildClassRow(),
  buildClassroomRow(),
  buildGradeRow(),
  buildWeightRow()
]);

const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 6 }, (_, index) => `${currentYear - index}-${currentYear - index + 1}`);
});
const defaultAcademicYear = computed(() => yearOptions.value[0] ?? '');

const configTitle = computed(() => {
  const titles: Record<ConfigKey, string> = {
    semester: '编辑学年学期',
    majors: '专业目录配置',
    classes: '班级配置',
    classrooms: '教室配置',
    grades: '成绩等级配置',
    weights: '综合成绩权重配置'
  };
  return titles[activeConfig.value];
});

const configWidth = computed(() => (activeConfig.value === 'classrooms' ? '1080px' : activeConfig.value === 'grades' ? '680px' : activeConfig.value === 'weights' ? '520px' : '560px'));
const addTitle = computed(() => ({
  year: '添加学年',
  major: '添加专业',
  class: '新增班级',
  room: '添加教室'
})[addKind.value]);
const weightTotal = computed(() => weightForm.coursewareWeight + weightForm.trainingPracticeWeight + weightForm.assignmentWeight + weightForm.examWeight);

const semesterRows = computed<SemesterDisplayRow[]>(() => {
  const rows: SemesterDisplayRow[] = [];
  academicYears.value.forEach((year, index) => {
    const semesters = year.semesters ?? [];
    semesters.forEach((semester) => {
      rows.push({ semesterId: semester.semesterId, index: index + 1, yearName: year.yearName.replace('学年', ''), semesterName: semester.semesterName, current: semester.current });
    });
  });
  return rows;
});

const displayMajors = computed(() => majors.value);

const displayClasses = computed(() => classes.value);

const displayGradeRules = computed(() => gradeRules.value);

const sortedGradeRules = computed(() => [...displayGradeRules.value].sort(compareGradeRows));

const classroomCameraRows = computed(() => {
  if (localCameras.value.length) {
    return localCameras.value;
  }

  const rows = classrooms.value.flatMap((classroom) => (classroom.cameras ?? []).map((camera) => toCameraRow(classroom, camera)));
  return rows.length ? rows : buildClassroomCameras();
});

function formatWeightContent(weight?: AdminScoreWeight) {
  if (!weight) {
    return '未配置';
  }
  return `课件学习进度得分*${weight.coursewareWeight}%+实训练习得分*${weight.trainingPracticeWeight}%+课程作业得分*${weight.assignmentWeight}%+考试得分*${weight.examWeight}%`;
}

const visibleLogs = computed<SettingLog[]>(() => {
  if (activeSetting.value?.key === 'grades') {
    return gradeRuleLogs.value.map((log) => ({
      time: log.createdAt ?? '-',
      operator: log.operatorName || '系统管理员',
      before: log.beforeContent,
      after: log.afterContent
    }));
  }
  if (activeSetting.value?.key !== 'weights') {
    return [];
  }
  const history = [...scoreWeights.value].sort((left, right) =>
    String(right.createdAt ?? right.effectiveFrom ?? '').localeCompare(String(left.createdAt ?? left.effectiveFrom ?? ''))
  );
  return history.map((current, index) => {
    const previous = history.slice(index + 1).find((item) => item.semesterId === current.semesterId);
    return {
      time: current.createdAt ?? current.effectiveFrom ?? '-',
      operator: current.operatorName || '系统管理员',
      before: formatWeightContent(previous),
      after: formatWeightContent(current)
    };
  });
});

function enabledNames<T extends { enabled?: boolean }>(items: T[], pick: (item: T) => string) {
  return items.filter((item) => item.enabled !== false).map(pick).filter(Boolean);
}

function summarize(names: string[], unit: string) {
  if (names.length === 0) {
    return '-';
  }
  const prefix = names.slice(0, 4).join('、');
  return names.length > 4 ? `${prefix} 等${names.length}个${unit}` : prefix;
}

function buildSemesterRow(): SettingRow {
  const currentYear = academicYears.value.find((year) => year.semesters?.some((semester) => semester.current));
  const currentSemester = currentYear?.semesters.find((semester) => semester.current);
  return {
    key: 'semester',
    name: '学年学期配置',
    value: currentYear && currentSemester ? `${currentYear.yearName} ${currentSemester.semesterName}` : '-',
    tone: 'blue',
    current: true
  };
}

function buildMajorRow(): SettingRow {
  return { key: 'majors', name: '专业目录配置', value: summarize(enabledNames(displayMajors.value, (item) => item.majorName), '专业'), tone: 'amber' };
}

function buildClassRow(): SettingRow {
  return { key: 'classes', name: '班级配置', value: summarize(enabledNames(displayClasses.value, (item) => item.className), '班级'), tone: 'rose' };
}

function buildClassroomRow(): SettingRow {
  return { key: 'classrooms', name: '教室配置', value: summarize(classrooms.value.map((item) => item.roomName), '实训室'), tone: 'violet' };
}

function buildGradeRow(): SettingRow {
  return {
    key: 'grades',
    name: '成绩等级配置',
    value: displayGradeRules.value.length ? displayGradeRules.value.map((rule) => `${rule.gradeName}（${rule.minScore}%-${rule.maxScore}%）`).join('、') : '-',
    tone: 'green',
    loggable: true
  };
}

function buildWeightRow(): SettingRow {
  return {
    key: 'weights',
    name: '综合成绩权重配置',
    value: scoreWeights.value.length
      ? `课件学习进度得分*${weightForm.coursewareWeight}%+实训练习得分*${weightForm.trainingPracticeWeight}%+课程作业得分*${weightForm.assignmentWeight}%+考试得分*${weightForm.examWeight}%`
      : '-',
    tone: 'gray',
    loggable: true
  };
}

function buildClassroomCameras(): CameraRow[] {
  return [];
}

function toCameraRow(classroom: AdminClassroom, camera: AdminCamera): CameraRow {
  return {
    id: camera.cameraId,
    classroomId: classroom.classroomId,
    cameraId: camera.cameraId,
    roomName: classroom.roomName,
    host: camera.nvrHost,
    port: String(camera.nvrPort),
    account: camera.adminUsername,
    password: camera.adminPassword,
    channel: camera.nvrChannel,
    url: camera.streamUrl ?? ''
  };
}

async function safeLoad<T>(label: string, loader: () => Promise<T[]>) {
  try {
    return await loader();
  } catch (error) {
    ElMessage.error(`${label}加载失败：${error instanceof Error ? error.message : '接口请求失败'}`);
    return [];
  }
}

async function loadSettings() {
  loading.value = true;
  try {
    const [yearRows, majorRows, classRows, classroomRows, weightRows, gradeRuleRows, gradeLogRows] = await Promise.all([
      safeLoad('学年学期', fetchAdminAcademicYears),
      safeLoad('专业目录', fetchAdminMajors),
      safeLoad('班级配置', fetchAdminClasses),
      safeLoad('教室配置', fetchAdminClassrooms),
      safeLoad('成绩权重', fetchAdminScoreWeights),
      safeLoad('成绩等级', fetchAdminScoreGradeRules),
      safeLoad('成绩等级日志', fetchAdminScoreGradeRuleLogs)
    ]);
    academicYears.value = yearRows;
    majors.value = majorRows;
    classes.value = classRows;
    classrooms.value = classroomRows;
    scoreWeights.value = weightRows;
    gradeRules.value = gradeRuleRows;
    gradeRuleLogs.value = gradeLogRows;
    const latest = weightRows[0];
    if (latest) {
      Object.assign(weightForm, latest);
    }
  } finally {
    loading.value = false;
  }
}

function openConfig(key: ConfigKey) {
  activeConfig.value = key;
  if (key === 'semester') {
    selectedSemesterId.value = semesterRows.value.find((row) => row.current)?.semesterId ?? null;
  }
  if (key === 'grades') {
    resetGradeDraftRows();
  }
  configVisible.value = true;
}

function openAdd(kind: AddKind) {
  addKind.value = kind;
  editingClassroomId.value = null;
  if (kind === 'year') addYearValue.value = defaultAcademicYear.value;
  if (kind === 'major') addMajorName.value = '';
  if (kind === 'class') {
    addClassName.value = '';
  }
  if (kind === 'room') {
    roomForm.roomName = '';
    roomForm.cameras = [newCamera(1), newCamera(2)];
  }
  addVisible.value = true;
}

function openLogs(item: SettingRow) {
  if (item.key !== 'weights' && item.key !== 'grades') {
    return;
  }
  activeSetting.value = item;
  logVisible.value = true;
}

function selectCurrentSemester(semesterId: number) {
  selectedSemesterId.value = semesterId;
}

async function setMajorStatus(majorId: number, enabled: boolean) {
  try {
    if (enabled) {
      await enableAdminMajor(majorId);
    } else {
      await disableAdminMajor(majorId);
    }
    await loadSettings();
    ElMessage.success(enabled ? '专业已启用' : '专业已禁用');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '专业状态更新失败');
  }
}

async function setClassStatus(classId: number, enabled: boolean) {
  try {
    if (enabled) {
      await enableAdminClass(classId);
    } else {
      await disableAdminClass(classId);
    }
    await loadSettings();
    ElMessage.success(enabled ? '班级已启用' : '班级已禁用');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '班级状态更新失败');
  }
}

async function removeCamera(id: number) {
  const target = classroomCameraRows.value.find((item) => item.id === id);
  if (!target?.classroomId) {
    localCameras.value = classroomCameraRows.value.filter((item) => item.id !== id);
    return;
  }

  const remaining = classroomCameraRows.value.filter((item) => item.classroomId === target.classroomId && item.id !== id);
  try {
    if (remaining.length === 0) {
      await deleteAdminClassroom(target.classroomId);
    } else {
      await updateAdminClassroom(target.classroomId, toClassroomCommand(target.roomName, remaining));
    }
    await loadSettings();
    ElMessage.success('教室摄像头配置已更新');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除摄像头失败');
  }
}

function editRoom(camera: CameraRow) {
  editingClassroomId.value = camera.classroomId ?? null;
  roomForm.roomName = camera.roomName;
  roomForm.cameras = classroomCameraRows.value.filter((item) => item.roomName === camera.roomName).map((item) => ({ ...item }));
  addKind.value = 'room';
  addVisible.value = true;
}

function newCamera(id: number): CameraRow {
  return { id: Date.now() + id, roomName: roomForm.roomName.trim(), host: '', port: '', account: '', password: '', channel: '', url: '' };
}

function toClassroomCommand(roomName: string, cameras: CameraRow[]): AdminClassroomCommand {
  return {
    roomName: roomName.trim(),
    cameras: cameras.map((camera): AdminCameraCommand => ({
      nvrHost: camera.host.trim(),
      nvrPort: Number(camera.port),
      adminUsername: camera.account.trim(),
      adminPassword: camera.password,
      nvrChannel: camera.channel.trim(),
      streamUrl: camera.url.trim() || undefined
    }))
  };
}

function currentSemesterId() {
  return semesterRows.value.find((semester) => semester.current)?.semesterId;
}

function addCamera() {
  roomForm.cameras.push(newCamera(roomForm.cameras.length + 1));
}

function removeRoomCamera(id: number) {
  roomForm.cameras = roomForm.cameras.filter((item) => item.id !== id);
}

async function saveAdd() {
  try {
    if (addKind.value === 'year') {
      if (!addYearValue.value) return ElMessage.warning('请选择学年');
      await createAdminAcademicYear({ yearName: addYearValue.value });
    }
    if (addKind.value === 'major') {
      if (!addMajorName.value.trim()) return ElMessage.warning('请输入专业名称');
      await createAdminMajor({ majorName: addMajorName.value.trim() });
    }
    if (addKind.value === 'class') {
      if (!addClassName.value.trim()) return ElMessage.warning('请输入班级名称');
      await createAdminClass({ className: addClassName.value.trim() });
    }
    if (addKind.value === 'room') {
      if (!roomForm.roomName.trim()) return ElMessage.warning('请输入教室名称');
      if (!roomForm.cameras.length) return ElMessage.warning('请至少添加一个摄像头');
      const command = toClassroomCommand(roomForm.roomName, roomForm.cameras);
      if (editingClassroomId.value) {
        await updateAdminClassroom(editingClassroomId.value, command);
      } else {
        await createAdminClassroom(command);
      }
    }
    addVisible.value = false;
    await loadSettings();
    ElMessage.success('已更新配置');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '配置更新失败');
  }
}

function compareGradeRows<T extends { minScore: number; maxScore: number; sortOrder?: number }>(prev: T, next: T) {
  return next.maxScore - prev.maxScore || next.minScore - prev.minScore || (prev.sortOrder ?? 0) - (next.sortOrder ?? 0);
}

function resetGradeDraftRows() {
  gradeDraftRows.value = sortedGradeRules.value
    .filter((rule) => rule.gradeName.trim())
    .map((rule) => ({
      id: rule.ruleId,
      ruleId: rule.ruleId,
      gradeName: rule.gradeName,
      minScore: rule.minScore,
      maxScore: rule.maxScore
    }));
}

function addGradeDraft() {
  if (gradeDraftRows.value.some((row) => !row.gradeName.trim())) {
    ElMessage.warning('请先填写当前新增的成绩等级');
    return;
  }
  const sortedRows = [...gradeDraftRows.value].sort(compareGradeRows);
  const lowestRule = sortedRows[sortedRows.length - 1];
  gradeDraftRows.value.push({
    id: Date.now(),
    gradeName: '',
    minScore: 0,
    maxScore: lowestRule ? Math.max(0, lowestRule.minScore - 1) : 100
  });
}

function removeGradeDraft(id: number) {
  gradeDraftRows.value = gradeDraftRows.value.filter((row) => row.id !== id);
}

function closeConfig() {
  configVisible.value = false;
  if (activeConfig.value === 'semester') {
    selectedSemesterId.value = semesterRows.value.find((row) => row.current)?.semesterId ?? null;
  }
  if (activeConfig.value === 'grades') {
    resetGradeDraftRows();
  }
}

async function saveActiveConfig() {
  if (activeConfig.value === 'semester') {
    await saveSemesterConfig();
    return;
  }
  if (activeConfig.value === 'grades') {
    await saveGradeRules();
    return;
  }
  await saveWeight();
}

async function saveSemesterConfig() {
  if (!selectedSemesterId.value) {
    ElMessage.warning('请选择当前学期');
    return;
  }
  const currentSemester = semesterRows.value.find((row) => row.current)?.semesterId;
  if (selectedSemesterId.value === currentSemester) {
    configVisible.value = false;
    return;
  }
  try {
    await setAdminCurrentSemester(selectedSemesterId.value);
    await loadSettings();
    configVisible.value = false;
    ElMessage.success('当前学期已更新');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '设置当前学期失败');
  }
}

/** 保存整张成绩等级配置表。 */
async function saveGradeRules() {
  const nextRules = gradeDraftRows.value
    .map((row) => ({
      gradeName: row.gradeName.trim(),
      minScore: Number(row.minScore),
      maxScore: Number(row.maxScore)
    }))
    .sort(compareGradeRows);

  if (!nextRules.length) {
    ElMessage.warning('请至少保留一个成绩等级');
    return;
  }

  const invalid = nextRules.find((rule) => !rule.gradeName || !Number.isFinite(rule.minScore) || !Number.isFinite(rule.maxScore));
  if (invalid) {
    ElMessage.warning('请完整填写等级名称和分数范围');
    return;
  }

  const outOfRange = nextRules.find((rule) => rule.minScore < 0 || rule.maxScore > 100 || rule.minScore > rule.maxScore);
  if (outOfRange) {
    ElMessage.warning('分数范围需满足0-100且起始百分比不能大于结束百分比');
    return;
  }

  if (nextRules.some((rule) => !Number.isInteger(rule.minScore) || !Number.isInteger(rule.maxScore))) {
    ElMessage.warning('成绩等级的分数范围必须使用整数');
    return;
  }

  if (new Set(nextRules.map((rule) => rule.gradeName)).size !== nextRules.length) {
    ElMessage.warning('成绩等级名称不能重复');
    return;
  }

  const overlaps = nextRules.some((rule, index) => index > 0 && rule.maxScore > nextRules[index - 1].minScore);
  if (overlaps) {
    ElMessage.warning('成绩等级的分数范围不能重叠');
    return;
  }

  const hasGap = nextRules[0].maxScore !== 100
    || nextRules[nextRules.length - 1].minScore !== 0
    || nextRules.some((rule, index) => index > 0
      && rule.maxScore !== nextRules[index - 1].minScore
      && rule.maxScore + 1 !== nextRules[index - 1].minScore);
  if (hasGap) {
    ElMessage.warning('成绩等级必须无空白地完整覆盖0-100分');
    return;
  }

  await replaceAdminScoreGradeRules(nextRules);
  await loadSettings();
  configVisible.value = false;
  ElMessage.success('成绩等级已更新');
}

async function saveWeight() {
  if (weightTotal.value !== 100) {
    ElMessage.warning('四项权重之和需等于100%');
    return;
  }
  const semesterId = currentSemesterId();
  if (!semesterId) {
    ElMessage.warning('请先配置学年学期');
    return;
  }
  try {
    await createAdminScoreWeight({ semesterId, ...weightForm });
    await loadSettings();
    configVisible.value = false;
    ElMessage.success('权重配置已更新');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '权重配置更新失败');
  }
}

onMounted(() => {
  void loadSettings();
});
</script>
