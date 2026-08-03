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

      <el-dialog v-model="configVisible" class="admin-settings-config-dialog" :width="configWidth" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-settings-dialog-head">
            <strong>{{ configTitle }}</strong>
            <el-button text circle :icon="Close" @click="configVisible = false" />
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
                  <button type="button" class="admin-settings-radio" :class="{ active: row.current }" @click="setCurrentSemester(row.semesterId)"></button>
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
                <td>
                  <span class="admin-settings-pill-status" :class="{ disabled: !item.enabled }">
                    <i></i>
                    {{ item.enabled ? '已启用' : '已禁用' }}
                  </span>
                </td>
                <td>
                  <el-button class="admin-settings-table-action" @click="setClassStatus(item.classId, true)">启用</el-button>
                  <el-button class="admin-settings-table-action danger" @click="setClassStatus(item.classId, false)">禁用</el-button>
                </td>
              </tr>
            </tbody>
          </table>
          <footer class="admin-settings-modal-pagination">
            <span>共 {{ displayClasses.length }} 条记录</span>
            <el-pagination :current-page="1" :page-size="10" :total="displayClasses.length" layout="prev, pager, next" background />
          </footer>
        </section>

        <section v-else-if="activeConfig === 'jobRoles'" class="admin-settings-panel">
          <el-button class="admin-settings-add-button" @click="openAdd('jobRole')">
            <el-icon><Plus /></el-icon>
            添加岗位角色
          </el-button>
          <table class="admin-settings-modal-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>岗位角色</th>
                <th>排序</th>
                <th>启用状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(role, index) in displayJobRoles" :key="role.jobRoleId">
                <td>{{ index + 1 }}</td>
                <td>{{ role.roleName }}</td>
                <td>{{ role.sortOrder ?? 0 }}</td>
                <td><span class="admin-settings-status" :class="{ disabled: !role.enabled }">{{ role.enabled ? '已启用' : '已禁用' }}</span></td>
                <td>
                  <button class="admin-settings-link" @click="setJobRoleStatus(role.jobRoleId, true)">启用</button>
                  <button class="admin-settings-link danger" @click="setJobRoleStatus(role.jobRoleId, false)">禁用</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <section v-else-if="activeConfig === 'classrooms'" class="admin-settings-panel classroom">
          <el-button class="admin-settings-add-button" @click="openAdd('room')">
            <el-icon><Plus /></el-icon>
            添加教室
          </el-button>
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
        </section>

        <section v-else-if="activeConfig === 'grades'" class="admin-settings-panel">
          <el-button class="admin-settings-add-button" @click="openGradeCreate">
            <el-icon><Plus /></el-icon>
            新增成绩等级
          </el-button>
          <table class="admin-settings-modal-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>等级名称</th>
                <th>分数范围</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(rule, index) in sortedGradeRules" :key="rule.ruleId">
                <td>{{ index + 1 }}</td>
                <td>
                  <el-input
                    v-if="editingGradeRuleId === rule.ruleId"
                    v-model="gradeForm.gradeName"
                    class="admin-settings-grade-name-input"
                    maxlength="10"
                    placeholder="请输入等级名称"
                  />
                  <template v-else>{{ rule.gradeName }}</template>
                </td>
                <td>
                  <div v-if="editingGradeRuleId === rule.ruleId" class="admin-settings-grade-range">
                    <el-input v-model.number="gradeForm.minScore" class="admin-settings-grade-score-input" type="number" :min="0" :max="100" />
                    <span>-</span>
                    <el-input v-model.number="gradeForm.maxScore" class="admin-settings-grade-score-input" type="number" :min="0" :max="100" />
                  </div>
                  <template v-else>{{ rule.minScore }}%-{{ rule.maxScore }}%</template>
                </td>
                <td>
                  <template v-if="editingGradeRuleId === rule.ruleId">
                    <button class="admin-settings-link" @click="saveGradeInline">保存</button>
                    <button class="admin-settings-link danger" @click="cancelGradeInline">取消</button>
                  </template>
                  <button v-else class="admin-settings-link" @click="editGrade(rule)">编辑</button>
                </td>
              </tr>
              <tr v-if="gradeCreating" class="admin-settings-grade-edit-row">
                <td>{{ sortedGradeRules.length + 1 }}</td>
                <td>
                  <el-input v-model="gradeForm.gradeName" class="admin-settings-grade-name-input" maxlength="10" placeholder="请输入等级名称" />
                </td>
                <td>
                  <div class="admin-settings-grade-range">
                    <el-input v-model.number="gradeForm.minScore" class="admin-settings-grade-score-input" type="number" :min="0" :max="100" />
                    <span>-</span>
                    <el-input v-model.number="gradeForm.maxScore" class="admin-settings-grade-score-input" type="number" :min="0" :max="100" />
                  </div>
                </td>
                <td>
                  <button class="admin-settings-link" @click="saveGradeInline">保存</button>
                  <button class="admin-settings-link danger" @click="cancelGradeInline">取消</button>
                </td>
              </tr>
            </tbody>
          </table>
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

        <template v-if="activeConfig === 'weights'" #footer>
          <div class="admin-settings-dialog-footer">
            <el-button @click="configVisible = false">取消</el-button>
            <el-button type="primary" @click="saveWeight">确定</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="addVisible" class="admin-settings-add-dialog" :width="addKind === 'room' ? '720px' : '480px'" :show-close="false" append-to-body>
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
          <label>
            <span>所属专业 <b>*</b></span>
            <el-select v-model="addClassMajorId" placeholder="请选择所属专业">
              <el-option v-for="major in displayMajors" :key="major.majorId" :label="major.majorName" :value="major.majorId" />
            </el-select>
          </label>
        </section>

        <section v-else-if="addKind === 'jobRole'" class="admin-settings-add-form">
          <label>
            <span>岗位角色 <b>*</b></span>
            <el-input v-model="addJobRoleName" maxlength="20" placeholder="请输入岗位角色" />
            <small>最多输入20个字</small>
          </label>
          <label>
            <span>排序</span>
            <el-input-number v-model="addJobRoleSort" :min="0" controls-position="right" />
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
        <table class="admin-settings-modal-table logs">
          <thead>
            <tr>
              <th>序号</th>
              <th>操作内容</th>
              <th>操作人</th>
              <th>操作时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(log, index) in visibleLogs" :key="log.time">
              <td>{{ index + 1 }}</td>
              <td>{{ log.content }}</td>
              <td>{{ log.operator }}</td>
              <td>{{ log.time }}</td>
            </tr>
          </tbody>
        </table>
        <footer class="admin-settings-modal-pagination">
          <span>共 {{ visibleLogs.length }} 条记录</span>
          <el-pagination :current-page="1" :page-size="10" :total="visibleLogs.length" layout="prev, pager, next" background />
        </footer>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, Edit, Plus } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  createAdminAcademicYear,
  createAdminClass,
  createAdminClassroom,
  createAdminJobRole,
  createAdminMajor,
  createAdminScoreWeight,
  deleteAdminClassroom,
  disableAdminClass,
  disableAdminJobRole,
  disableAdminMajor,
  enableAdminClass,
  enableAdminJobRole,
  enableAdminMajor,
  fetchAdminAcademicYears,
  fetchAdminClasses,
  fetchAdminClassrooms,
  fetchAdminJobRoles,
  fetchAdminMajors,
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
  type AdminJobRole,
  type AdminMajor,
  type AdminScoreGradeRule,
  type AdminScoreWeight
} from '../../api/admin-settings';

type SettingTone = 'blue' | 'amber' | 'rose' | 'violet' | 'green' | 'gray';
type ConfigKey = 'semester' | 'majors' | 'classes' | 'jobRoles' | 'classrooms' | 'grades' | 'weights';
type AddKind = 'year' | 'major' | 'class' | 'jobRole' | 'room';

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
  content: string;
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

const loading = ref(false);
const configVisible = ref(false);
const addVisible = ref(false);
const logVisible = ref(false);
const activeConfig = ref<ConfigKey>('semester');
const activeSetting = ref<SettingRow | null>(null);
const addKind = ref<AddKind>('year');
const addYearValue = ref('2025-2026');
const addMajorName = ref('');
const addClassName = ref('');
const addClassMajorId = ref<number | null>(null);
const addJobRoleName = ref('');
const addJobRoleSort = ref(0);
const editingClassroomId = ref<number | null>(null);
const editingGradeRuleId = ref<number | null>(null);
const gradeCreating = ref(false);

const academicYears = ref<AdminAcademicYear[]>([]);
const majors = ref<AdminMajor[]>([]);
const classes = ref<AdminClass[]>([]);
const classrooms = ref<AdminClassroom[]>([]);
const jobRoles = ref<AdminJobRole[]>([]);
const scoreWeights = ref<AdminScoreWeight[]>([]);
const gradeRules = ref<AdminScoreGradeRule[]>([]);
const localCameras = ref<CameraRow[]>([]);

const weightForm = reactive({
  coursewareWeight: 30,
  trainingPracticeWeight: 30,
  assignmentWeight: 30,
  examWeight: 10
});

const roomForm = reactive<{ roomName: string; cameras: CameraRow[] }>({
  roomName: '',
  cameras: []
});

const gradeForm = reactive({
  gradeName: '',
  minScore: 0,
  maxScore: 100
});

const fallbackRows: SettingRow[] = [
  { key: 'semester', name: '学年学期配置', value: '2024-2025学年 上学期', tone: 'blue', current: true },
  { key: 'majors', name: '专业目录配置', value: '城市轨道交通运营管理、城市轨道交通信号控制、城市轨道交通车辆技术 等8个专业', tone: 'amber' },
  { key: 'classes', name: '班级配置', value: '城轨运营2501班、城轨运营2401班、城轨车辆2501班、城轨车辆2401班 等20个班级', tone: 'rose' },
  { key: 'jobRoles', name: '岗位角色配置', value: '司机、调度员、站务员、值班员', tone: 'violet' },
  { key: 'classrooms', name: '教室配置', value: '101实训室', tone: 'violet' },
  { key: 'grades', name: '成绩等级配置', value: '优秀（85%-100%）、良好（75%-85%）、中等（60%-75%）、较差（0%-60%）', tone: 'green', loggable: true },
  { key: 'weights', name: '综合成绩权重配置', value: '课件学习进度得分*30%+实训练习得分*30%+课程作业得分*30%+考试得分*10%', tone: 'gray', loggable: true }
];

const yearOptions = ['2025-2026', '2024-2025', '2023-2024', '2022-2023', '2021-2022', '2020-2021'];

const settingRows = computed<SettingRow[]>(() => [
  buildSemesterRow(),
  buildMajorRow(),
  buildClassRow(),
  buildJobRoleRow(),
  buildClassroomRow(),
  buildGradeRow(),
  buildWeightRow()
]);

const configTitle = computed(() => {
  const titles: Record<ConfigKey, string> = {
    semester: '编辑学年学期',
    majors: '专业目录配置',
    classes: '班级配置',
    jobRoles: '岗位角色配置',
    classrooms: '教室配置',
    grades: '成绩等级配置',
    weights: '综合成绩权重配置'
  };
  return titles[activeConfig.value];
});

const configWidth = computed(() => (activeConfig.value === 'classrooms' ? '1080px' : activeConfig.value === 'weights' ? '520px' : '560px'));
const addTitle = computed(() => ({
  year: '添加学年',
  major: '添加专业',
  class: '新增班级',
  jobRole: '添加岗位角色',
  room: '添加教室'
})[addKind.value]);
const weightTotal = computed(() => weightForm.coursewareWeight + weightForm.trainingPracticeWeight + weightForm.assignmentWeight + weightForm.examWeight);

const semesterRows = computed<SemesterDisplayRow[]>(() => {
  const rows: SemesterDisplayRow[] = [];
  academicYears.value.forEach((year, index) => {
    const semesters = year.semesters?.length ? year.semesters : [
      { semesterId: year.academicYearId * 10 + 1, academicYearId: year.academicYearId, semesterName: '上学期', current: false },
      { semesterId: year.academicYearId * 10 + 2, academicYearId: year.academicYearId, semesterName: '下学期', current: false }
    ];
    semesters.forEach((semester) => {
      rows.push({ semesterId: semester.semesterId, index: index + 1, yearName: year.yearName.replace('学年', ''), semesterName: semester.semesterName, current: semester.current });
    });
  });
  return rows;
});

const displayMajors = computed(() => majors.value.length ? majors.value : [
  { majorId: 1, majorName: '城市轨道交通运营管理', enabled: true },
  { majorId: 2, majorName: '城市轨道交通信号技术', enabled: true },
  { majorId: 3, majorName: '城市轨道交通车辆技术', enabled: true },
  { majorId: 4, majorName: '城市轨道交通供配电技术', enabled: true },
  { majorId: 5, majorName: '城市轨道交通通信信号技术', enabled: true },
  { majorId: 6, majorName: '城市轨道交通工程技术', enabled: true },
  { majorId: 7, majorName: '城市轨道交通机电技术', enabled: true },
  { majorId: 8, majorName: '高速铁路客运乘务', enabled: true }
]);

const displayClasses = computed(() => classes.value.length ? classes.value : [
  { classId: 1, majorId: 1, className: '城轨运营2101班', enabled: true },
  { classId: 2, majorId: 2, className: '城轨信号2101班', enabled: true },
  { classId: 3, majorId: 3, className: '城轨车辆2101班', enabled: false },
  { classId: 4, majorId: 4, className: '城轨供电2101班', enabled: true },
  { classId: 5, majorId: 6, className: '城轨工程2101班', enabled: true },
  { classId: 6, majorId: 7, className: '城轨机电2101班', enabled: false }
]);

const displayJobRoles = computed(() => jobRoles.value.length ? jobRoles.value : [
  { jobRoleId: 1, roleName: '司机', sortOrder: 1, enabled: true },
  { jobRoleId: 2, roleName: '调度员', sortOrder: 2, enabled: true },
  { jobRoleId: 3, roleName: '站务员', sortOrder: 3, enabled: true },
  { jobRoleId: 4, roleName: '值班员', sortOrder: 4, enabled: true }
]);

const displayGradeRules = computed(() => gradeRules.value.length ? gradeRules.value : [
  { ruleId: 1, gradeName: '优秀', minScore: 85, maxScore: 100, sortOrder: 1 },
  { ruleId: 2, gradeName: '良好', minScore: 75, maxScore: 85, sortOrder: 2 },
  { ruleId: 3, gradeName: '中等', minScore: 60, maxScore: 75, sortOrder: 3 },
  { ruleId: 4, gradeName: '较差', minScore: 0, maxScore: 60, sortOrder: 4 }
]);

const sortedGradeRules = computed(() => [...displayGradeRules.value].sort((prev, next) => next.maxScore - prev.maxScore || next.minScore - prev.minScore || prev.sortOrder - next.sortOrder));

const classroomCameraRows = computed(() => {
  if (localCameras.value.length) {
    return localCameras.value;
  }

  const rows = classrooms.value.flatMap((classroom) => (classroom.cameras ?? []).map((camera) => toCameraRow(classroom, camera)));
  return rows.length ? rows : buildClassroomCameras();
});

const visibleLogs = computed<SettingLog[]>(() => [
  { time: '2025-01-15 14:30:22', operator: '张建国', content: '课件学习进度得分*30%+实训练习得分*20%+课程作业得分*25%+考试得分*25%' },
  { time: '2025-01-10 09:15:08', operator: '李明辉', content: '课件学习进度得分*30%+实训练习得分*30%+课程作业得分*30%+考试得分*10%' },
  { time: '2025-01-05 16:42:35', operator: '王思远', content: '课件学习进度得分*25%+实训练习得分*25%+课程作业得分*25%+考试得分*25%' }
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
  const currentYear = academicYears.value.find((year) => year.semesters?.some((semester) => semester.current));
  const currentSemester = currentYear?.semesters.find((semester) => semester.current);
  return { ...fallback, value: currentYear && currentSemester ? `${currentYear.yearName} ${currentSemester.semesterName}` : fallback.value };
}

function buildMajorRow(): SettingRow {
  const fallback = fallbackRows[1];
  return { ...fallback, value: summarize(enabledNames(displayMajors.value, (item) => item.majorName), '专业', fallback) };
}

function buildClassRow(): SettingRow {
  const fallback = fallbackRows[2];
  return { ...fallback, value: summarize(enabledNames(displayClasses.value, (item) => item.className), '班级', fallback) };
}

function buildJobRoleRow(): SettingRow {
  const fallback = fallbackRows[3];
  return { ...fallback, value: summarize(enabledNames(displayJobRoles.value, (item) => item.roleName), '岗位角色', fallback) };
}

function buildClassroomRow(): SettingRow {
  const fallback = fallbackRows[4];
  return { ...fallback, value: summarize(classrooms.value.map((item) => item.roomName), '实训室', fallback) };
}

function buildGradeRow(): SettingRow {
  const fallback = fallbackRows[5];
  return { ...fallback, value: displayGradeRules.value.map((rule) => `${rule.gradeName}（${rule.minScore}%-${rule.maxScore}%）`).join('、') };
}

function buildWeightRow(): SettingRow {
  return { ...fallbackRows[6], value: `课件学习进度得分*${weightForm.coursewareWeight}%+实训练习得分*${weightForm.trainingPracticeWeight}%+课程作业得分*${weightForm.assignmentWeight}%+考试得分*${weightForm.examWeight}%` };
}

function buildClassroomCameras(): CameraRow[] {
  const roomName = classrooms.value[0]?.roomName || '101实训室';
  return [0, 1, 2, 3, 4].map((item) => ({
    id: item + 1,
    classroomId: classrooms.value[0]?.classroomId,
    roomName,
    host: `192.168.1.10${item}`,
    port: '8000',
    account: 'admin',
    password: ['Adm@101!', 'Pwd#202!', 'Rmt$303!', 'Sec%404!', 'Cam^505!'][item],
    channel: `CH0${item + 1}`,
    url: `rtsp://192.168.1.10${item}:554/stream1`
  }));
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
    academicYears.value = yearRows.length ? yearRows : seedYears();
    majors.value = majorRows;
    classes.value = classRows;
    classrooms.value = classroomRows;
    jobRoles.value = jobRoleRows;
    scoreWeights.value = weightRows;
    gradeRules.value = gradeRuleRows;
    const latest = weightRows[0];
    if (latest) {
      Object.assign(weightForm, latest);
    }
  } finally {
    loading.value = false;
  }
}

function seedYears(): AdminAcademicYear[] {
  return yearOptions.slice(1).map((year, index) => ({
    academicYearId: index + 1,
    yearName: `${year}学年`,
    semesters: [
      { semesterId: (index + 1) * 10 + 1, academicYearId: index + 1, semesterName: '上学期', current: index === 0 },
      { semesterId: (index + 1) * 10 + 2, academicYearId: index + 1, semesterName: '下学期', current: false }
    ]
  }));
}

function openConfig(key: ConfigKey) {
  activeConfig.value = key;
  cancelGradeInline();
  configVisible.value = true;
}

function openAdd(kind: AddKind) {
  addKind.value = kind;
  editingClassroomId.value = null;
  editingGradeRuleId.value = null;
  if (kind === 'year') addYearValue.value = '2025-2026';
  if (kind === 'major') addMajorName.value = '';
  if (kind === 'class') {
    addClassName.value = '';
    addClassMajorId.value = displayMajors.value[0]?.majorId ?? null;
  }
  if (kind === 'jobRole') {
    addJobRoleName.value = '';
    addJobRoleSort.value = displayJobRoles.value.length + 1;
  }
  if (kind === 'room') {
    roomForm.roomName = '';
    roomForm.cameras = [newCamera(1), newCamera(2)];
  }
  addVisible.value = true;
}

function openLogs(item: SettingRow) {
  activeSetting.value = item;
  logVisible.value = true;
}

async function setCurrentSemester(semesterId: number) {
  try {
    await setAdminCurrentSemester(semesterId);
    await loadSettings();
    ElMessage.success('当前学期已更新');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '设置当前学期失败');
  }
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

async function setJobRoleStatus(jobRoleId: number, enabled: boolean) {
  try {
    if (enabled) {
      await enableAdminJobRole(jobRoleId);
    } else {
      await disableAdminJobRole(jobRoleId);
    }
    await loadSettings();
    ElMessage.success(enabled ? '岗位角色已启用' : '岗位角色已禁用');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '岗位角色状态更新失败');
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

function editGrade(rule: AdminScoreGradeRule) {
  gradeCreating.value = false;
  editingGradeRuleId.value = rule.ruleId;
  Object.assign(gradeForm, {
    gradeName: rule.gradeName,
    minScore: rule.minScore,
    maxScore: rule.maxScore
  });
}

function openGradeCreate() {
  editingGradeRuleId.value = null;
  gradeCreating.value = true;
  const lowestRule = sortedGradeRules.value[sortedGradeRules.value.length - 1];
  Object.assign(gradeForm, {
    gradeName: '',
    minScore: 0,
    maxScore: lowestRule?.minScore ?? 100
  });
}

function cancelGradeInline() {
  gradeCreating.value = false;
  editingGradeRuleId.value = null;
  Object.assign(gradeForm, {
    gradeName: '',
    minScore: 0,
    maxScore: 100
  });
}

async function saveGradeInline() {
  const saved = await saveGradeRule();
  if (!saved) return;
  await loadSettings();
  cancelGradeInline();
  ElMessage.success('成绩等级已更新');
}

function newCamera(id: number): CameraRow {
  return { id: Date.now() + id, roomName: roomForm.roomName || '101实训室', host: '', port: '', account: '', password: '', channel: '', url: '' };
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
  return semesterRows.value.find((semester) => semester.current)?.semesterId ?? semesterRows.value[0]?.semesterId;
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
      await createAdminAcademicYear({ yearName: `${addYearValue.value}学年` });
    }
    if (addKind.value === 'major') {
      if (!addMajorName.value.trim()) return ElMessage.warning('请输入专业名称');
      await createAdminMajor({ majorName: addMajorName.value.trim() });
    }
    if (addKind.value === 'class') {
      if (!addClassName.value.trim()) return ElMessage.warning('请输入班级名称');
      if (!addClassMajorId.value) return ElMessage.warning('请选择所属专业');
      await createAdminClass({ majorId: addClassMajorId.value, className: addClassName.value.trim() });
    }
    if (addKind.value === 'jobRole') {
      if (!addJobRoleName.value.trim()) return ElMessage.warning('请输入岗位角色');
      await createAdminJobRole({ roleName: addJobRoleName.value.trim(), sortOrder: addJobRoleSort.value });
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

/** 保存新增或编辑后的成绩等级规则。 */
async function saveGradeRule(): Promise<boolean> {
  const gradeName = gradeForm.gradeName.trim();
  const minScore = Number(gradeForm.minScore);
  const maxScore = Number(gradeForm.maxScore);

  if (!gradeName) {
    ElMessage.warning('请输入等级名称');
    return false;
  }
  if (!Number.isFinite(minScore) || !Number.isFinite(maxScore)) {
    ElMessage.warning('请输入有效分数范围');
    return false;
  }
  if (minScore < 0 || maxScore > 100 || minScore >= maxScore) {
    ElMessage.warning('分数范围需满足0-100且最低分小于最高分');
    return false;
  }

  const editingId = editingGradeRuleId.value;
  const nextRules = displayGradeRules.value.map((rule) => ({
    gradeName: editingId === rule.ruleId ? gradeName : rule.gradeName,
    minScore: editingId === rule.ruleId ? minScore : rule.minScore,
    maxScore: editingId === rule.ruleId ? maxScore : rule.maxScore
  }));

  if (!editingId) {
    nextRules.push({ gradeName, minScore, maxScore });
  }

  await replaceAdminScoreGradeRules(nextRules);
  return true;
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
