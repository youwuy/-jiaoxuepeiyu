<template>
  <AdminShell activeKey="admin-trainings">
    <section v-if="!topicPickerVisible" class="admin-training-form-page">
      <header class="admin-training-form-topbar">
        <div class="admin-training-form-left">
          <el-button class="admin-training-form-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-training-form-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>实训组课</el-breadcrumb-item>
            <el-breadcrumb-item>{{ formMode === 'create' ? '新增实训课' : '编辑实训课' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ formMode === 'create' ? '新增实训课' : '编辑实训课' }}</h1>
        <span></span>
      </header>

      <main class="admin-training-form-content">
        <div class="admin-training-form-panel">
          <header class="admin-training-form-head">
            <div>
              <span>{{ formMode === 'create' ? '新增实训课' : '编辑实训课' }}</span>
              <h2>{{ form.name || (formMode === 'create' ? '新增实训课' : '编辑实训课') }}</h2>
            </div>
            <el-button class="admin-training-form-close" text circle :icon="Close" @click="goBack" />
          </header>

          <div class="admin-training-stepper">
            <button v-for="step in steps" :key="step.key" type="button" :class="{ active: activeStep === step.key }" @click="activeStep = step.key">
              <i>{{ step.index }}</i>
              <span>{{ step.label }}</span>
            </button>
          </div>

          <div class="admin-training-form-body">
            <section v-show="activeStep === 'base'" class="admin-training-form-card">
              <header><strong>基础信息</strong><p>配置实训课名称、类型、时间和说明</p></header>
              <div class="admin-training-form-grid">
                <label>
                  <span>实训课名称 <b>*</b></span>
                  <el-input v-model="form.name" maxlength="30" placeholder="请输入实训课名称" />
                </label>
                <label>
                  <span>实训类型 <b>*</b></span>
                  <el-radio-group v-model="form.type">
                    <el-radio-button label="考试" />
                    <el-radio-button label="练习" />
                  </el-radio-group>
                </label>
                <label>
                  <span>实训模式 <b>*</b></span>
                  <el-radio-group v-model="form.mode">
                    <el-radio-button label="单人实训" />
                    <el-radio-button label="协同实训" />
                  </el-radio-group>
                </label>
                <label>
                  <span>学年学期 <b>*</b></span>
                  <el-select v-model="form.semester" placeholder="请选择学期">
                    <el-option v-for="item in semesterOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </label>
                <label class="wide">
                  <span>实训时间 <b>*</b></span>
                  <el-date-picker v-model="form.range" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
                </label>
                <label class="wide">
                  <span>实训说明</span>
                  <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入实训说明" />
                </label>
              </div>
            </section>

            <section v-show="activeStep === 'resource'" class="admin-training-form-card">
              <header>
                <div><strong>实训内容</strong><p>选择实训任务、资源课件和理论试卷</p></div>
                <div class="admin-training-card-actions">
                  <el-button :icon="FolderOpened" @click="topicPickerVisible = true">添加实训题</el-button>
                  <el-button :icon="Document" @click="openSelector('resource')">选择资源</el-button>
                  <el-button :icon="Tickets" @click="openSelector('paper')">选择理论试卷</el-button>
                </div>
              </header>

              <div class="admin-training-topic-summary">
                <article>
                  <span>已选实训题</span>
                  <strong>{{ selectedTopics.length }} 个</strong>
                </article>
                <article>
                  <span>资源课件</span>
                  <strong>{{ selectedResources.length }} 个</strong>
                </article>
                <article>
                  <span>理论试卷</span>
                  <strong>{{ selectedPaper?.name || '未选择' }}</strong>
                </article>
              </div>

              <div class="admin-training-selected-list">
                <article v-for="item in selectedTopics" :key="item.id">
                  <div>
                    <strong>{{ item.name }}</strong>
                    <span>{{ item.meta }}</span>
                  </div>
                  <el-button text type="danger" @click="removeSelected('topic', item.id)">移除</el-button>
                </article>
              </div>

              <div class="admin-training-subsection">
                <p>资源课件</p>
                <div class="admin-training-resource-grid">
                  <article v-for="item in selectedResources" :key="item.id">
                    <el-icon><Document /></el-icon>
                    <div><strong>{{ item.name }}</strong><span>{{ item.type }} / {{ item.size }}</span></div>
                    <button @click="removeSelected('resource', item.id)">移除</button>
                  </article>
                </div>
              </div>

              <div class="admin-training-paper-row">
                <span>理论试卷</span>
                <strong>{{ selectedPaper?.name || '未选择' }}</strong>
                <el-button link type="primary" @click="openSelector('paper')">重新选择</el-button>
              </div>
            </section>

            <section v-show="activeStep === 'target'" class="admin-training-form-card">
              <header>
                <div><strong>参训对象与场地</strong><p>配置班级、学生、监考教师和实训教室</p></div>
                <div class="admin-training-card-actions">
                  <el-button :icon="UserFilled" @click="openSelector('class')">选择班级/学生</el-button>
                  <el-button :icon="User" @click="openSelector('teacher')">选择教师</el-button>
                  <el-button :icon="OfficeBuilding" @click="openSelector('room')">选择教室</el-button>
                </div>
              </header>
              <div class="admin-training-target-grid">
                <article>
                  <span>参训班级/学生</span>
                  <strong>{{ selectedClasses.map((item) => item.name).join('、') || '未选择' }}</strong>
                </article>
                <article>
                  <span>监考教师</span>
                  <strong>{{ selectedTeachers.map((item) => item.name).join('、') || '未选择' }}</strong>
                </article>
                <article>
                  <span>实训教室</span>
                  <strong>{{ selectedRoom?.name || '未选择' }}</strong>
                </article>
              </div>
            </section>

            <section v-show="activeStep === 'rule'" class="admin-training-form-card">
              <header>
                <div><strong>协同角色与评分规则</strong><p>配置小组角色、流程节点和得分规则</p></div>
                <div class="admin-training-card-actions">
                  <el-button :icon="Plus" @click="openRoleDialog">新增角色</el-button>
                  <el-button :icon="View" @click="previewVisible = true">预览组课</el-button>
                </div>
              </header>

              <div class="admin-training-role-grid">
                <article v-for="role in form.roles" :key="role.name">
                  <div><strong>{{ role.name }}</strong><span>{{ role.capacity }} 人 / {{ role.duty }}</span></div>
                  <el-input-number v-model="role.capacity" :min="1" :max="8" size="small" />
                </article>
              </div>

              <div class="admin-training-flow">
                <article v-for="(node, index) in form.flow" :key="node.name">
                  <i>{{ index + 1 }}</i>
                  <div><strong>{{ node.name }}</strong><span>{{ node.rule }}</span></div>
                  <el-input-number v-model="node.score" :min="0" :max="100" size="small" />
                </article>
              </div>
            </section>
          </div>
        </div>

        <div class="admin-training-form-footer">
          <el-button @click="goBack">取消</el-button>
          <el-button @click="saveDraft">保存草稿</el-button>
          <el-button type="primary" @click="saveAndPublish">保存并发布</el-button>
        </div>
      </main>
    </section>

    <section v-else class="admin-training-topic-page">
      <header class="admin-training-topic-topbar">
        <div class="admin-training-topic-left">
          <el-button class="admin-training-topic-back" :icon="ArrowLeft" @click="topicPickerVisible = false" />
          <el-breadcrumb class="admin-training-topic-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>实训组课</el-breadcrumb-item>
            <el-breadcrumb-item>添加实训题</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>添加实训题</h1>
        <span></span>
      </header>

      <main class="admin-training-topic-content">
        <header class="admin-training-topic-head">
          <div>
            <span>添加实训题</span>
            <h2>{{ form.name || '新增实训课' }}</h2>
          </div>
          <el-button class="admin-training-topic-close" @click="topicPickerVisible = false">返回实训课</el-button>
        </header>

        <section class="admin-training-topic-toolbar">
          <el-input v-model="topicKeyword" class="admin-training-topic-search" :prefix-icon="Search" placeholder="搜索实训题名称" clearable />
          <el-select v-model="topicType" class="admin-training-topic-select" placeholder="实训题类型" clearable>
            <el-option label="信号" value="信号" />
            <el-option label="站务" value="站务" />
            <el-option label="调度" value="调度" />
          </el-select>
          <el-button type="primary" class="admin-training-topic-confirm" @click="topicPickerVisible = false">完成选择</el-button>
        </section>

        <section class="admin-training-topic-card">
          <div class="admin-training-topic-card-head">
            <strong>实训题列表</strong>
            <span>已选 {{ selectedTopicIds.length }} 项</span>
          </div>
          <div class="admin-training-topic-table-scroll">
            <table class="admin-training-topic-table">
              <thead>
                <tr>
                  <th>选择</th>
                  <th>实训题名称</th>
                  <th>类型</th>
                  <th>时长</th>
                  <th>分值</th>
                  <th>说明</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in filteredTopics" :key="item.id">
                  <td>
                    <el-checkbox :model-value="selectedTopicIds.includes(item.id)" @change="toggleTopic(item.id)" />
                  </td>
                  <td class="topic-name">{{ item.name }}</td>
                  <td><span class="topic-pill">{{ item.category }}</span></td>
                  <td>{{ item.duration }} 分钟</td>
                  <td>{{ item.score }} 分</td>
                  <td>{{ item.meta }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </section>

    <el-dialog v-model="selectorVisible" class="admin-training-dialog" width="760px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-training-dialog-head">
          <strong>{{ selectorTitle }}</strong>
          <el-button text circle :icon="Close" @click="selectorVisible = false" />
        </div>
      </template>
      <div class="admin-training-selector">
        <div class="admin-training-selector-filter">
          <el-input v-model="selectorKeyword" :prefix-icon="Search" placeholder="请输入关键字" clearable />
          <el-select v-if="selectorKind === 'resource'" v-model="selectorType" placeholder="类型" clearable>
            <el-option label="资源" value="资源" />
            <el-option label="课件" value="课件" />
            <el-option label="试卷" value="试卷" />
          </el-select>
        </div>
        <div class="admin-training-selector-list">
          <article v-for="item in selectorItems" :key="item.id" :class="{ checked: isSelected(item.id) }" @click="toggleSelect(item.id)">
            <el-checkbox :model-value="isSelected(item.id)" @click.stop @change="toggleSelect(item.id)" />
            <div><strong>{{ item.name }}</strong><span>{{ item.meta }}</span></div>
          </article>
        </div>
      </div>
      <template #footer>
        <div class="admin-training-dialog-footer">
          <el-button @click="selectorVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSelector">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" class="admin-training-dialog" width="520px" :show-close="false" append-to-body>
      <template #header><div class="admin-training-dialog-head"><strong>新增协同角色</strong><el-button text circle :icon="Close" @click="roleVisible = false" /></div></template>
      <div class="admin-training-mini-form">
        <label><span>角色名称</span><el-input v-model="roleForm.name" placeholder="请输入角色名称" /></label>
        <label><span>人数</span><el-input-number v-model="roleForm.capacity" :min="1" :max="8" /></label>
        <label><span>职责说明</span><el-input v-model="roleForm.duty" type="textarea" :rows="3" placeholder="请输入职责说明" /></label>
      </div>
      <template #footer><div class="admin-training-dialog-footer"><el-button @click="roleVisible = false">取消</el-button><el-button type="primary" @click="addRole">确定</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="previewVisible" class="admin-training-dialog" width="820px" :show-close="false" append-to-body>
      <template #header><div class="admin-training-dialog-head"><strong>实训课预览</strong><el-button text circle :icon="Close" @click="previewVisible = false" /></div></template>
      <div class="admin-training-preview">
        <section>
          <h4>{{ form.name || '新增实训课' }}</h4>
          <p>{{ formatRange }}</p>
          <div><span>{{ form.type }}</span><span>{{ form.mode }}</span><span>{{ selectedRoom?.name || '未选择教室' }}</span></div>
        </section>
        <section class="admin-training-preview-grid">
          <article><span>参训对象</span><strong>{{ selectedClasses.map((item) => item.name).join('、') || '未选择' }}</strong></article>
          <article><span>监考教师</span><strong>{{ selectedTeachers.map((item) => item.name).join('、') || '未选择' }}</strong></article>
          <article><span>实训题</span><strong>{{ selectedTopics.length }} 个</strong></article>
          <article><span>总分</span><strong>{{ totalScore }} 分</strong></article>
        </section>
      </div>
      <template #footer><div class="admin-training-dialog-footer"><el-button @click="previewVisible = false">关闭</el-button><el-button type="primary" @click="saveAndPublish">发布</el-button></div></template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Close, Document, FolderOpened, OfficeBuilding, Plus, Search, Tickets, User, UserFilled, View } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { fetchAdminPapers } from '../../api/admin-paper';
import { fetchAdminResources } from '../../api/admin-resource';
import {
  createAdminTraining,
  fetchAdminTraining,
  publishAdminTraining,
  updateAdminTraining,
} from '../../api/admin-training';
import { fetchAdminAcademicYears, fetchAdminClassrooms, fetchAdminClasses as fetchAdminSettingsClasses } from '../../api/admin-settings';
import { fetchAdminTeachers, type AdminTeacherOption } from '../../api/admin-course';
import type { AdminClass } from '../../api/admin-settings';
import type { AdminPaper } from '../../api/admin-paper';
import type { AdminResource } from '../../api/admin-resource';

type StepKey = 'base' | 'resource' | 'target' | 'rule';
type SelectorKind = 'resource' | 'paper' | 'class' | 'teacher' | 'room';

interface TopicItem {
  id: number;
  name: string;
  category: string;
  duration: number;
  score: number;
  meta: string;
}

interface SelectableItem {
  id: number;
  name: string;
  meta: string;
  category?: string;
  duration?: number;
  score?: number;
  type?: string;
  size?: string;
}

interface TrainingRole {
  name: string;
  capacity: number;
  duty: string;
}

interface TrainingFlowNode {
  name: string;
  rule: string;
  score: number;
}

const route = useRoute();
const router = useRouter();
const trainingId = computed(() => Number(route.params.id));
const formMode = computed(() => (route.name === 'admin-training-edit' ? 'edit' : 'create'));
const activeStep = ref<StepKey>('base');
const loading = ref(false);
const topicPickerVisible = ref(false);
const previewVisible = ref(false);
const roleVisible = ref(false);
const selectorVisible = ref(false);
const selectorKind = ref<SelectorKind>('resource');
const selectorKeyword = ref('');
const selectorType = ref('');
const topicKeyword = ref('');
const topicType = ref('');

const academicYears = ref<Array<{ academicYearId: number; yearName: string; semesters?: Array<{ semesterId: number; semesterName: string }> }>>([]);
const classOptions = ref<SelectableItem[]>([]);
const teacherOptions = ref<SelectableItem[]>([]);
const resourceOptions = ref<SelectableItem[]>([]);
const paperOptions = ref<SelectableItem[]>([]);
const roomOptions = ref<SelectableItem[]>([]);
const topicOptions = ref<TopicItem[]>([
  { id: 1, name: '列车故障识别', category: '信号', duration: 40, score: 20, meta: '面向列车运行安全的基础识别题' },
  { id: 2, name: '站台门联动处置', category: '站务', duration: 35, score: 20, meta: '站台门故障联动处置流程' },
  { id: 3, name: '调度指令响应', category: '调度', duration: 30, score: 15, meta: '调度命令解析与执行' },
  { id: 4, name: '应急广播处理', category: '站务', duration: 25, score: 15, meta: '应急广播发布与回执' },
  { id: 5, name: '信号机状态确认', category: '信号', duration: 20, score: 10, meta: '基础状态识别与判读' }
]);

const selectedTopicIds = ref<number[]>([]);
const selectedResourceIds = ref<number[]>([]);
const selectedPaperId = ref<number>(0);
const selectedClassIds = ref<number[]>([]);
const selectedTeacherIds = ref<number[]>([]);
const selectedRoomId = ref<number>(0);

const form = reactive({
  id: 0,
  name: '',
  type: '考试' as '考试' | '练习',
  mode: '协同实训' as '单人实训' | '协同实训',
  semester: '',
  majorId: undefined as number | undefined,
  range: [] as string[],
  description: '',
  teacherIds: [] as number[],
  classIds: [] as number[],
  roles: [] as TrainingRole[],
  flow: [] as TrainingFlowNode[]
});

const roleForm = reactive({ name: '', capacity: 1, duty: '' });

const steps = [
  { key: 'base' as StepKey, index: 1, label: '基础信息' },
  { key: 'resource' as StepKey, index: 2, label: '实训内容' },
  { key: 'target' as StepKey, index: 3, label: '对象场地' },
  { key: 'rule' as StepKey, index: 4, label: '角色规则' }
];

const semesterOptions = computed(() =>
  academicYears.value.flatMap((year) =>
    (year.semesters ?? []).map((semester) => ({
      semesterId: semester.semesterId,
      value: `${year.yearName} ${semester.semesterName}`,
      label: `${year.yearName} ${semester.semesterName}`
    }))
  )
);

const selectedTopics = computed(() => topicOptions.value.filter((item) => selectedTopicIds.value.includes(item.id)));
const selectedResources = computed(() => resourceOptions.value.filter((item) => selectedResourceIds.value.includes(item.id)));
const selectedPaper = computed(() => paperOptions.value.find((item) => item.id === selectedPaperId.value));
const selectedClasses = computed(() => classOptions.value.filter((item) => selectedClassIds.value.includes(item.id)));
const selectedTeachers = computed(() => teacherOptions.value.filter((item) => selectedTeacherIds.value.includes(item.id)));
const selectedRoom = computed(() => roomOptions.value.find((item) => item.id === selectedRoomId.value));
const totalScore = computed(() => selectedTopics.value.reduce((sum, item) => sum + item.score, 0));
const formatRange = computed(() => (form.range.length === 2 ? `${form.range[0]} 至 ${form.range[1]}` : '未选择时间'));

const selectorTitle = computed(() => ({
  resource: '选择资源',
  paper: '选择理论试卷',
  class: '选择班级/学生',
  teacher: '选择教师',
  room: '选择教室'
})[selectorKind.value]);

const filteredTopics = computed(() =>
  topicOptions.value.filter((item) => {
    const keywordMatched = !topicKeyword.value || item.name.includes(topicKeyword.value) || item.meta.includes(topicKeyword.value);
    const typeMatched = !topicType.value || item.category === topicType.value;
    return keywordMatched && typeMatched;
  })
);

const selectorItems = computed(() => {
  const source = {
    resource: resourceOptions.value,
    paper: paperOptions.value,
    class: classOptions.value,
    teacher: teacherOptions.value,
    room: roomOptions.value
  }[selectorKind.value];
  return source.filter((item) => {
    const keywordMatched = !selectorKeyword.value || item.name.includes(selectorKeyword.value) || item.meta.includes(selectorKeyword.value);
    const typeMatched = !selectorType.value || item.category === selectorType.value || item.name.includes(selectorType.value);
    return keywordMatched && typeMatched;
  });
});

function goBack() {
  router.push('/admin/training');
}

function resetForm() {
  form.id = 0;
  form.name = '';
  form.type = '考试';
  form.mode = '协同实训';
  form.semester = '';
  form.majorId = undefined;
  form.range = [];
  form.description = '';
  form.teacherIds = [];
  form.classIds = [];
  form.roles = [];
  form.flow = [];
  selectedTopicIds.value = [];
  selectedResourceIds.value = [];
  selectedPaperId.value = 0;
  selectedClassIds.value = [];
  selectedTeacherIds.value = [];
  selectedRoomId.value = 0;
  activeStep.value = 'base';
}

async function loadDetail() {
  if (!trainingId.value) {
    return;
  }
  try {
    const detail = await fetchAdminTraining(trainingId.value);
    form.id = detail.trainingId;
    form.name = detail.trainingName || '';
    form.type = apiTrainingTypeToText(detail.trainingType);
    form.mode = apiTrainingModeToText(detail.trainingMode);
    form.semester = `${detail.academicYearName || ''} ${detail.semesterName || ''}`.trim();
    form.range = [detail.openStartTime || '', detail.openEndTime || ''].filter(Boolean);
    selectedClassIds.value = detail.classIds || [];
    selectedTeacherIds.value = detail.creatorName ? [1] : [];
    selectedPaperId.value = detail.paperId || 0;
    form.roles = (detail.roles || []).map((role) => ({
      name: role.roleName || role.roleCode || '角色',
      capacity: Number(role.capacity || 1),
      duty: role.roleCode || '待配置职责'
    }));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '实训课详情加载失败');
  }
}

async function loadOptions() {
  try {
    const [years, classes, teachers, papers, resources, classrooms] = await Promise.all([
      fetchAdminAcademicYears(),
      fetchAdminSettingsClasses(),
      fetchAdminTeachers(),
      fetchAdminPapers({ page: 1, pageSize: 200 }),
      fetchAdminResources({ page: 1, pageSize: 200 }),
      fetchAdminClassrooms()
    ]);
    academicYears.value = years;
    classOptions.value = (classes as AdminClass[]).filter((item) => item.enabled !== false).map((item) => ({
      id: item.classId,
      name: item.className,
      meta: item.majorName || '班级',
      category: item.majorName || 'class'
    }));
    teacherOptions.value = (teachers as AdminTeacherOption[]).filter((item) => item.enabled !== false).map((item) => ({
      id: item.userId,
      name: item.realName || item.accountNo || `教师${item.userId}`,
      meta: item.accountNo || '教师',
      category: 'teacher'
    }));
    paperOptions.value = ((papers as { records?: AdminPaper[] }).records || []).map((item) => ({
      id: item.paperId,
      name: item.paperName,
      meta: `${item.questionCount || 0} 题 / ${item.totalScore || 0} 分`,
      category: item.publishStatus
    }));
    resourceOptions.value = ((resources as { records?: AdminResource[] }).records || []).map((item) => ({
      id: item.resourceId,
      name: item.resourceName,
      meta: `${item.resourceType || '资源'} / ${item.fileSize ? `${(item.fileSize / 1024 / 1024).toFixed(1)} MB` : '-'}`,
      category: item.resourceType,
      type: item.resourceType,
      size: item.fileSize ? `${(item.fileSize / 1024 / 1024).toFixed(1)} MB` : '-'
    }));
    roomOptions.value = classrooms.map((item) => ({
      id: item.classroomId,
      name: item.roomName,
      meta: `${item.cameraCount} 路摄像头`,
      category: 'room'
    }));
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '基础数据加载失败');
  }
}

function openSelector(kind: SelectorKind) {
  selectorKind.value = kind;
  selectorKeyword.value = '';
  selectorType.value = '';
  selectorVisible.value = true;
}

function isSelected(id: number) {
  const current = currentSelectedIds(selectorKind.value);
  return current.includes(id);
}

function currentSelectedIds(kind: SelectorKind) {
  if (kind === 'resource') return selectedResourceIds.value;
  if (kind === 'paper') return selectedPaperId.value ? [selectedPaperId.value] : [];
  if (kind === 'class') return selectedClassIds.value;
  if (kind === 'teacher') return selectedTeacherIds.value;
  return selectedRoomId.value ? [selectedRoomId.value] : [];
}

function toggleSelect(id: number) {
  const single = selectorKind.value === 'paper' || selectorKind.value === 'room';
  if (single) {
    if (selectorKind.value === 'paper') {
      selectedPaperId.value = id;
    }
    if (selectorKind.value === 'room') {
      selectedRoomId.value = id;
    }
    return;
  }

  if (selectorKind.value === 'resource') {
    selectedResourceIds.value = selectedResourceIds.value.includes(id)
      ? selectedResourceIds.value.filter((item) => item !== id)
      : [...selectedResourceIds.value, id];
  }
  if (selectorKind.value === 'class') {
    selectedClassIds.value = selectedClassIds.value.includes(id)
      ? selectedClassIds.value.filter((item) => item !== id)
      : [...selectedClassIds.value, id];
  }
  if (selectorKind.value === 'teacher') {
    selectedTeacherIds.value = selectedTeacherIds.value.includes(id)
      ? selectedTeacherIds.value.filter((item) => item !== id)
      : [...selectedTeacherIds.value, id];
  }
}

function confirmSelector() {
  selectorVisible.value = false;
}

function removeSelected(kind: 'topic' | 'resource', id: number) {
  if (kind === 'topic') {
    selectedTopicIds.value = selectedTopicIds.value.filter((item) => item !== id);
  }
  if (kind === 'resource') {
    selectedResourceIds.value = selectedResourceIds.value.filter((item) => item !== id);
  }
}

function toggleTopic(id: number) {
  selectedTopicIds.value = selectedTopicIds.value.includes(id)
    ? selectedTopicIds.value.filter((item) => item !== id)
    : [...selectedTopicIds.value, id];
}

function openRoleDialog() {
  roleForm.name = '';
  roleForm.capacity = 1;
  roleForm.duty = '';
  roleVisible.value = true;
}

function addRole() {
  if (!roleForm.name.trim()) {
    ElMessage.warning('请输入角色名称');
    return;
  }
  form.roles.push({ name: roleForm.name, capacity: roleForm.capacity, duty: roleForm.duty || '待配置职责' });
  roleVisible.value = false;
}

function buildTrainingCommand(publishStatus: string) {
  return {
    trainingName: form.name.trim(),
    semesterId: semesterIdFromLabel(form.semester),
    trainingType: trainingTypeToApi(form.type),
    trainingMode: trainingModeToApi(form.mode),
    paperMode: selectedPaperId.value ? 'THEORY_PAPER' : 'NONE',
    paperId: selectedPaperId.value || undefined,
    openStartTime: form.range[0],
    openEndTime: form.range[1],
    teamSize: form.roles.reduce((sum, role) => sum + Number(role.capacity || 0), 0) || 1,
    appRequired: true,
    classIds: [...selectedClassIds.value],
    roles: form.roles.map((role, index) => ({
      roleName: role.name,
      roleCode: role.name,
      capacity: Number(role.capacity || 1),
      aiFillEnabled: true,
      sortOrder: index + 1
    })),
    publishStatus
  };
}

async function saveDraft() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入实训课名称');
    return;
  }
  try {
    const command = buildTrainingCommand('DRAFT');
    if (formMode.value === 'edit' && form.id) {
      await updateAdminTraining(form.id, command);
    } else {
      await createAdminTraining(command);
    }
    ElMessage.success('草稿已保存');
    goBack();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存草稿失败');
  }
}

async function saveAndPublish() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入实训课名称');
    return;
  }
  try {
    const command = buildTrainingCommand('PUBLISHED');
    let trainingKey = form.id;
    if (formMode.value === 'edit' && form.id) {
      await updateAdminTraining(form.id, command);
      trainingKey = form.id;
    } else {
      const result = await createAdminTraining(command);
      trainingKey = result.trainingId;
    }
    await publishAdminTraining(trainingKey);
    ElMessage.success('已保存并发布');
    goBack();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发布失败');
  }
}

function semesterIdFromLabel(label: string) {
  return semesterOptions.value.find((item) => item.value === label)?.semesterId;
}

function trainingTypeToApi(type?: string) {
  if (type === '考试') return 'EXAM';
  if (type === '练习') return 'PRACTICE';
  return undefined;
}

function apiTrainingTypeToText(type?: string): '考试' | '练习' {
  return type === 'PRACTICE' ? '练习' : '考试';
}

function trainingModeToApi(mode?: string) {
  if (mode === '单人实训') return 'SINGLE';
  if (mode === '协同实训') return 'COLLABORATIVE';
  return undefined;
}

function apiTrainingModeToText(mode?: string): '单人实训' | '协同实训' {
  return mode === 'SINGLE' ? '单人实训' : '协同实训';
}

onMounted(async () => {
  if (route.name === 'admin-training-new') {
    resetForm();
  }
  loading.value = true;
  try {
    await loadOptions();
    if (formMode.value === 'edit') {
      await loadDetail();
    }
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.admin-training-form-page,
.admin-training-topic-page {
  min-height: 100vh;
  padding: 0 24px 28px;
  background: #f5f7fb;
}

.admin-training-form-topbar,
.admin-training-topic-topbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  min-height: 68px;
}

.admin-training-form-left,
.admin-training-topic-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.admin-training-form-back.el-button,
.admin-training-topic-back.el-button {
  width: 44px;
  height: 44px;
  border: 1px solid #dce5f1;
  border-radius: 9px;
  background: #ffffff;
  color: #53657f;
  font-size: 18px;
}

.admin-training-form-breadcrumb,
.admin-training-topic-breadcrumb {
  font-size: 13px;
}

.admin-training-form-breadcrumb :deep(.el-breadcrumb__inner),
.admin-training-topic-breadcrumb :deep(.el-breadcrumb__inner) {
  color: #6c7d96;
}

.admin-training-form-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner),
.admin-training-topic-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #2f7cf6;
  font-weight: 800;
}

.admin-training-form-topbar h1,
.admin-training-topic-topbar h1 {
  margin: 0;
  color: #111827;
  font-size: 17px;
  font-weight: 900;
  text-align: center;
}

.admin-training-form-content,
.admin-training-topic-content {
  min-height: calc(100vh - 96px);
  border: 1px solid #e5ebf3;
  border-radius: 10px;
  padding: 28px;
  background: #ffffff;
}

.admin-training-form-panel {
  display: grid;
  gap: 0;
  min-height: calc(100vh - 158px);
  border: 1px solid #dfe7f1;
  border-radius: 10px;
  overflow: hidden;
}

.admin-training-form-head,
.admin-training-topic-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px 20px;
  border-bottom: 1px solid #edf2f8;
  background: #ffffff;
}

.admin-training-form-head span,
.admin-training-topic-head span {
  color: #6c7d96;
  font-size: 14px;
  font-weight: 700;
}

.admin-training-form-head h2,
.admin-training-topic-head h2 {
  margin: 6px 0 0;
  color: #17233d;
  font-size: 28px;
  line-height: 34px;
  font-weight: 900;
}

.admin-training-form-close,
.admin-training-topic-close {
  height: 38px;
  border-radius: 8px;
  color: #53657f;
}

.admin-training-stepper {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  padding: 20px 22px;
  background: #f8fafc;
}

.admin-training-stepper button {
  height: 56px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid #dfe6f0;
  border-radius: 8px;
  background: #ffffff;
  color: #64748b;
  cursor: pointer;
  font: inherit;
  font-weight: 800;
}

.admin-training-stepper button.active {
  border-color: #3478f6;
  color: #3478f6;
  background: #eef5ff;
}

.admin-training-stepper i {
  width: 24px;
  height: 24px;
  display: inline-grid;
  place-items: center;
  border-radius: 50%;
  background: currentColor;
  color: #ffffff;
  font-style: normal;
  font-size: 12px;
}

.admin-training-form-body {
  display: grid;
  gap: 18px;
  padding: 0 22px 92px;
  background: #f8fafc;
}

.admin-training-form-card {
  padding: 20px 22px 18px;
  border: 1px solid #dfe7f1;
  border-radius: 10px;
  background: #ffffff;
}

.admin-training-form-card header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.admin-training-form-card header strong,
.admin-training-topic-card-head strong {
  color: #17233d;
  font-size: 16px;
  font-weight: 800;
}

.admin-training-form-card header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.admin-training-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.admin-training-form-grid label {
  display: grid;
  gap: 8px;
}

.admin-training-form-grid label.wide {
  grid-column: 1 / -1;
}

.admin-training-form-grid span {
  color: #425268;
  font-size: 13px;
  font-weight: 800;
}

.admin-training-form-grid b {
  color: #ef4444;
}

.admin-training-card-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-training-topic-summary,
.admin-training-target-grid,
.admin-training-preview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.admin-training-topic-summary article,
.admin-training-target-grid article,
.admin-training-preview-grid article {
  min-height: 86px;
  padding: 14px 16px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #f8fafc;
}

.admin-training-topic-summary span,
.admin-training-target-grid span,
.admin-training-preview-grid span {
  color: #64748b;
  font-size: 12px;
}

.admin-training-topic-summary strong,
.admin-training-target-grid strong,
.admin-training-preview-grid strong {
  display: block;
  margin-top: 8px;
  color: #17233d;
  font-size: 15px;
  line-height: 22px;
}

.admin-training-selected-list,
.admin-training-resource-grid,
.admin-training-role-grid,
.admin-training-flow {
  display: grid;
  gap: 10px;
}

.admin-training-selected-list {
  margin-top: 16px;
}

.admin-training-selected-list article,
.admin-training-resource-grid article,
.admin-training-role-grid article,
.admin-training-flow article {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #ffffff;
}

.admin-training-selected-list strong,
.admin-training-resource-grid strong,
.admin-training-role-grid strong,
.admin-training-flow strong {
  color: #17233d;
  font-size: 13px;
}

.admin-training-selected-list span,
.admin-training-resource-grid span,
.admin-training-role-grid span,
.admin-training-flow span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.admin-training-subsection {
  margin-top: 16px;
}

.admin-training-subsection p,
.admin-training-paper-row span {
  color: #64748b;
  font-size: 12px;
}

.admin-training-resource-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-training-resource-grid article {
  justify-content: flex-start;
}

.admin-training-resource-grid .el-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  border-radius: 8px;
  background: #eef5ff;
  color: #3478f6;
}

.admin-training-resource-grid button {
  margin-left: auto;
  border: 0;
  background: transparent;
  color: #ef4444;
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-paper-row {
  min-height: 48px;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 16px;
  padding: 0 14px;
  border-radius: 8px;
  background: #f8fafc;
}

.admin-training-paper-row strong {
  color: #17233d;
  font-size: 14px;
}

.admin-training-flow i {
  width: 26px;
  height: 26px;
  flex: 0 0 auto;
  display: inline-grid;
  place-items: center;
  border-radius: 50%;
  background: #3478f6;
  color: #ffffff;
  font-style: normal;
  font-weight: 800;
}

.admin-training-form-footer {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px;
  border-top: 1px solid #edf2f8;
  background: #ffffff;
}

.admin-training-topic-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.admin-training-topic-search {
  width: 280px;
}

.admin-training-topic-select {
  width: 160px;
}

.admin-training-topic-confirm {
  height: 40px;
  border: 0;
  border-radius: 8px;
  font-weight: 800;
}

.admin-training-topic-card {
  border: 1px solid #dfe7f1;
  border-radius: 10px;
  background: #ffffff;
}

.admin-training-topic-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #edf2f8;
}

.admin-training-topic-card-head span {
  color: #64748b;
  font-size: 12px;
}

.admin-training-topic-table-scroll {
  overflow-x: auto;
}

.admin-training-topic-table {
  width: 100%;
  min-width: 900px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-topic-table th,
.admin-training-topic-table td {
  height: 54px;
  padding: 0 14px;
  border-bottom: 1px solid #edf2f8;
  color: #475569;
  font-size: 14px;
  text-align: left;
  white-space: nowrap;
}

.admin-training-topic-table th {
  color: #94a3b8;
  font-weight: 900;
}

.topic-name {
  color: #17233d;
  font-weight: 800;
}

.topic-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: #eef5ff;
  color: #3478f6;
  font-size: 12px;
  font-weight: 800;
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
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.admin-training-selector-filter,
.admin-training-mini-form {
  display: grid;
  gap: 12px;
}

.admin-training-selector-filter {
  grid-template-columns: minmax(0, 1fr) 140px;
  margin-bottom: 12px;
}

.admin-training-selector-list {
  max-height: 420px;
  display: grid;
  gap: 8px;
  overflow: auto;
}

.admin-training-selector-list article,
.admin-training-log-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #ffffff;
}

.admin-training-selector-list article.checked {
  border-color: #3478f6;
  background: #f4f8ff;
}

.admin-training-selector-list strong {
  color: #17233d;
  font-size: 13px;
}

.admin-training-selector-list span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.admin-training-preview section:first-child {
  padding: 16px 18px;
  border-radius: 10px;
  background: #eef5ff;
}

.admin-training-preview h4 {
  margin: 0;
  color: #17233d;
  font-size: 20px;
}

.admin-training-preview p {
  margin: 8px 0 0;
  color: #52647b;
  white-space: pre-line;
}

.admin-training-preview section:first-child div {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.admin-training-preview section:first-child span {
  padding: 4px 10px;
  border-radius: 999px;
  background: #ffffff;
  color: #3478f6;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-preview-grid {
  margin-top: 12px;
}

.admin-training-form-page :deep(.el-radio-button__inner),
.admin-training-topic-page :deep(.el-radio-button__inner) {
  border-radius: 8px;
}

@media (max-width: 980px) {
  .admin-training-form-page,
  .admin-training-topic-page {
    padding: 0 12px 20px;
  }

  .admin-training-form-topbar,
  .admin-training-topic-topbar {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 0;
  }

  .admin-training-form-topbar h1,
  .admin-training-topic-topbar h1 {
    order: -1;
    text-align: left;
  }

  .admin-training-form-content,
  .admin-training-topic-content {
    padding: 18px;
  }

  .admin-training-form-panel {
    min-height: auto;
  }

  .admin-training-stepper,
  .admin-training-form-grid,
  .admin-training-topic-summary,
  .admin-training-target-grid,
  .admin-training-preview-grid {
    grid-template-columns: 1fr;
  }

  .admin-training-resource-grid {
    grid-template-columns: 1fr;
  }

  .admin-training-selector-filter,
  .admin-training-topic-toolbar {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .admin-training-topic-search,
  .admin-training-topic-select {
    width: 100%;
  }
}
</style>
