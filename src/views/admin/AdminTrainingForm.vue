<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-form-page">
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

      <main v-loading="loading" class="admin-training-form-content">
        <section class="admin-training-base-card">
          <header class="admin-training-section-head">
            <span class="admin-training-section-icon blue">
              <el-icon><InfoFilled /></el-icon>
            </span>
            <div>
              <strong>基础信息</strong>
              <p>填写实训课程的基本信息，带 * 为必填项</p>
            </div>
          </header>

          <div class="admin-training-base-grid">
            <label class="admin-training-form-item">
              <span><b>*</b> 实训课程名</span>
              <el-input v-model="form.name" maxlength="20" show-word-limit placeholder="请输入实训课程名称" />
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 类型</span>
              <el-radio-group v-model="form.type" class="admin-training-radio-line">
                <el-radio label="练习">练习</el-radio>
                <el-radio label="考试">考试</el-radio>
              </el-radio-group>
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 起止时间</span>
              <div class="admin-training-date-range">
                <el-date-picker v-model="form.range[0]" type="datetime" placeholder="开始时间" />
                <em>至</em>
                <el-date-picker v-model="form.range[1]" type="datetime" placeholder="结束时间" />
              </div>
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 所属学年学期</span>
              <el-select v-model="form.semester" placeholder="请选择学年学期">
                <el-option v-for="item in semesterOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 参训班级/学员</span>
              <el-select v-model="selectedClassIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择参训班级/学员（可多选）">
                <el-option v-for="item in classOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 监考教师</span>
              <el-select v-model="selectedTeacherIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择监考教师（可多选）">
                <el-option v-for="item in teacherOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 教室</span>
              <el-select v-model="selectedRoomId" placeholder="请选择教室">
                <el-option v-for="item in roomOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 学生实训时是否自动录屏</span>
              <el-radio-group v-model="form.recordingEnabled" class="admin-training-radio-line">
                <el-radio :label="false">否</el-radio>
                <el-radio :label="true">是</el-radio>
              </el-radio-group>
            </label>
            <label class="admin-training-form-item">
              <span><b>*</b> 最终成绩取值依据</span>
              <el-radio-group v-model="form.scoreBasis" class="admin-training-radio-line">
                <el-radio label="最高成绩">最高成绩</el-radio>
                <el-radio label="最后一次提交的成绩">最后一次提交的成绩</el-radio>
              </el-radio-group>
            </label>
          </div>
        </section>

        <section class="admin-training-question-card">
          <header class="admin-training-question-head">
            <div class="admin-training-section-head">
              <span class="admin-training-section-icon purple">
                <el-icon><Document /></el-icon>
              </span>
              <div>
                <strong>实训题列表</strong>
                <p>选择本次实训包含的题目，可拖拽排序；考试类型只能添加一道多人实训题，或全部使用单人实训题</p>
              </div>
            </div>
            <el-button type="primary" class="admin-training-add-topic" :icon="Plus" @click="openTopicPicker">添加实训题</el-button>
          </header>

          <div class="admin-training-topic-table-scroll">
            <table class="admin-training-topic-table">
              <thead>
                <tr>
                  <th>排序</th>
                  <th>序号</th>
                  <th>实训题名称</th>
                  <th>实训模式</th>
                  <th>训练角色 <el-icon><QuestionFilled /></el-icon></th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in selectedTopicRows" :key="item.id">
                  <td><span class="admin-training-drag-handle">⠿</span></td>
                  <td>{{ index + 1 }}</td>
                  <td class="topic-name">{{ item.name }}</td>
                  <td>{{ item.mode }}</td>
                  <td>
                    <div v-if="item.roles.length" class="admin-training-role-checks">
                      <el-checkbox v-for="role in item.roles" :key="role" :model-value="true" disabled>{{ role }}</el-checkbox>
                    </div>
                    <span v-else>-</span>
                  </td>
                  <td>
                    <el-button class="admin-training-delete-topic" text type="danger" :icon="Delete" @click="removeSelected('topic', item.id)">删除</el-button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <footer class="admin-training-question-summary">已选择 <b>{{ selectedTopics.length }}</b> 道实训题</footer>
        </section>

        <footer class="admin-training-form-footer">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :icon="Check" @click="saveDraft">保存草稿</el-button>
        </footer>
      </main>
    </section>

    <el-dialog v-model="topicPickerVisible" class="admin-training-topic-dialog" width="920px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-training-dialog-head">
          <strong>添加实训题</strong>
          <el-button text circle :icon="Close" @click="topicPickerVisible = false" />
        </div>
      </template>
      <section class="admin-training-topic-picker">
        <div class="admin-training-topic-toolbar">
          <el-input v-model="topicKeyword" class="admin-training-topic-search" :prefix-icon="Search" placeholder="请输入实训题名搜索" clearable />
          <el-select v-model="topicType" class="admin-training-topic-select" placeholder="实训题类型" clearable>
            <el-option label="信号" value="信号" />
            <el-option label="站务" value="站务" />
            <el-option label="调度" value="调度" />
          </el-select>
          <el-button @click="queryTopics">查询</el-button>
          <el-button @click="resetTopicQuery">重置</el-button>
        </div>
        <div class="admin-training-topic-overview">
          <p>
            共 <b>{{ filteredTopicRows.length }}</b> 条实训题，已选 <b>{{ topicPickerIds.length }}</b> 条
          </p>
          <span>
            <el-icon><InfoFilled /></el-icon>
            灰色复选框表示实训题已绑定当前课程，不可取消
          </span>
        </div>
        <div class="admin-training-topic-dialog-table">
          <table class="admin-training-topic-table">
            <thead>
              <tr>
                <th>选择</th>
                <th>实训题名称</th>
                <th>类型</th>
                <th>实训模式</th>
                <th>时长</th>
                <th>分值</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredTopicRows" :key="item.id">
                <td><el-checkbox :model-value="topicPickerIds.includes(item.id)" :disabled="isBoundTopic(item.id)" @change="toggleTopic(item.id)" /></td>
                <td class="topic-name">{{ item.name }}</td>
                <td><span class="topic-pill">{{ item.category }}</span></td>
                <td>{{ item.mode }}</td>
                <td>{{ item.duration }} 分钟</td>
                <td>{{ item.score }} 分</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
      <template #footer>
        <div class="admin-training-dialog-footer">
          <el-button @click="topicPickerVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmTopicSelection">确定添加</el-button>
        </div>
      </template>
    </el-dialog>

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
      <template #footer><div class="admin-training-dialog-footer"><el-button @click="previewVisible = false">关闭</el-button></div></template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Check, Close, Delete, Document, InfoFilled, Plus, QuestionFilled, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { fetchAdminPapers } from '../../api/admin-paper';
import { fetchAdminResources } from '../../api/admin-resource';
import {
  createAdminTraining,
  fetchAdminTraining,
  fetchAdminTrainingTopics,
  updateAdminTraining,
} from '../../api/admin-training';
import { fetchAdminAcademicYears, fetchAdminClassrooms, fetchAdminClasses as fetchAdminSettingsClasses } from '../../api/admin-settings';
import { fetchAdminTeachers, type AdminTeacherOption } from '../../api/admin-course';
import type { AdminClass } from '../../api/admin-settings';
import type { AdminPaper } from '../../api/admin-paper';
import type { AdminResource } from '../../api/admin-resource';
import trainingCoverUrl from '../../assets/course-station-preview.png';

type SelectorKind = 'resource' | 'paper' | 'class' | 'teacher' | 'room';

interface TopicItem {
  id: number;
  name: string;
  category: string;
  duration: number;
  score: number;
  meta: string;
  mode: '单人实训' | '多人实训';
  roles: string[];
}

interface TopicRow extends TopicItem {
}

interface SelectableItem {
  id: number;
  name: string;
  meta: string;
  majorId?: number;
  category?: string;
  duration?: number;
  score?: number;
  type?: string;
  size?: string;
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
const loading = ref(false);
const topicPickerVisible = ref(false);
const previewVisible = ref(false);
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
const topicOptions = ref<TopicItem[]>([]);

const selectedTopicIds = ref<number[]>([]);
const topicPickerIds = ref<number[]>([]);
const boundTopicIds = ref<number[]>([]);
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
  recordingEnabled: false,
  scoreBasis: '最高成绩' as '最高成绩' | '最后一次提交的成绩',
  flow: [] as TrainingFlowNode[]
});

const semesterOptions = computed(() =>
  academicYears.value.flatMap((year) =>
    (year.semesters ?? []).map((semester) => ({
      semesterId: semester.semesterId,
      academicYearId: year.academicYearId,
      value: `${year.yearName} ${semester.semesterName}`,
      label: `${year.yearName} ${semester.semesterName}`
    }))
  )
);

const selectedTopics = computed(() => topicOptions.value.filter((item) => selectedTopicIds.value.includes(item.id)));
const selectedClasses = computed(() => classOptions.value.filter((item) => selectedClassIds.value.includes(item.id)));
const selectedTeachers = computed(() => teacherOptions.value.filter((item) => selectedTeacherIds.value.includes(item.id)));
const selectedRoom = computed(() => roomOptions.value.find((item) => item.id === selectedRoomId.value));
const totalScore = computed(() => selectedTopics.value.reduce((sum, item) => sum + item.score, 0));
const formatRange = computed(() => (form.range.length === 2 ? `${form.range[0]} 至 ${form.range[1]}` : '未选择时间'));
const selectedTopicRows = computed<TopicRow[]>(() => selectedTopics.value.map(mapTopicRow));
const filteredTopicRows = computed<TopicRow[]>(() => filteredTopics.value.map(mapTopicRow));

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
  form.recordingEnabled = false;
  form.scoreBasis = '最高成绩';
  form.flow = [];
  selectedTopicIds.value = [];
  topicPickerIds.value = [];
  boundTopicIds.value = [];
  selectedResourceIds.value = [];
  selectedPaperId.value = 0;
  selectedClassIds.value = [];
  selectedTeacherIds.value = [];
  selectedRoomId.value = 0;
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
    selectedTeacherIds.value = detail.teacherIds || [];
    selectedRoomId.value = detail.classroomId || 0;
    form.recordingEnabled = detail.appRequired === true;
    form.scoreBasis = detail.scoreBasis === 'LAST_SUBMIT' ? '最后一次提交的成绩' : '最高成绩';
    selectedTopicIds.value = detail.topicIds || [];
    topicPickerIds.value = [...selectedTopicIds.value];
    boundTopicIds.value = [...selectedTopicIds.value];
    selectedPaperId.value = detail.paperId || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '实训课详情加载失败');
  }
}

async function loadOptions() {
  try {
    const [years, classes, teachers, papers, resources, classrooms, questions] = await Promise.all([
      fetchAdminAcademicYears(),
      fetchAdminSettingsClasses(),
      fetchAdminTeachers(),
      fetchAdminPapers({ page: 1, pageSize: 200 }),
      fetchAdminResources({ page: 1, pageSize: 200 }),
      fetchAdminClassrooms(),
      fetchAdminTrainingTopics()
    ]);
    academicYears.value = years;
    classOptions.value = (classes as AdminClass[]).filter((item) => item.enabled !== false).map((item) => ({
      id: item.classId,
      name: item.className,
      meta: item.majorName || '班级',
      majorId: item.majorId,
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
    topicOptions.value = questions.map((item) => ({
      id: item.topicId,
      name: item.topicName,
      category: item.category || '实训题',
      duration: item.durationMinutes || 0,
      score: item.score || 0,
      meta: `${item.trainingMode === 'TEAM' ? '多人实训' : '单人实训'} / ${item.score || 0} 分`,
      mode: item.trainingMode === 'TEAM' ? '多人实训' : '单人实训',
      roles: item.roleNames ? item.roleNames.split(',').map((role) => role.trim()).filter(Boolean) : []
    }));
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '基础数据加载失败');
  }
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
  if (isBoundTopic(id)) return;
  topicPickerIds.value = topicPickerIds.value.includes(id)
    ? topicPickerIds.value.filter((item) => item !== id)
    : [...topicPickerIds.value, id];
}

function isBoundTopic(id: number) {
  return boundTopicIds.value.includes(id);
}

function openTopicPicker() {
  topicPickerIds.value = [...selectedTopicIds.value];
  topicKeyword.value = '';
  topicType.value = '';
  topicPickerVisible.value = true;
}

function queryTopics() {
  topicKeyword.value = topicKeyword.value.trim();
}

function resetTopicQuery() {
  topicKeyword.value = '';
  topicType.value = '';
}

function confirmTopicSelection() {
  const added = topicPickerIds.value.filter((id) => !boundTopicIds.value.includes(id));
  if (!added.length && !selectedTopicIds.value.length) {
    ElMessage.warning('请选择需要添加的实训题');
    return;
  }
  selectedTopicIds.value = [...new Set([...boundTopicIds.value, ...added])];
  topicPickerVisible.value = false;
}

function buildTrainingCommand(publishStatus: string) {
  const semester = semesterOptions.value.find((item) => item.value === form.semester);
  const majorId = classOptions.value.find((item) => selectedClassIds.value.includes(item.id))?.majorId;
  const hasTeamTopic = selectedTopics.value.some((item) => item.mode === '多人实训');
  const topicRoleNames = Array.from(new Set(selectedTopics.value.flatMap((item) => item.roles)));
  const trainingMode = hasTeamTopic ? 'TEAM' : 'SINGLE';
  const roles = topicRoleNames.map((roleName, index) => ({
    roleName,
    roleCode: roleName,
    capacity: 1,
    aiFillEnabled: true,
    sortOrder: index + 1
  }));

  return {
    trainingName: form.name.trim(),
    academicYearId: semester?.academicYearId,
    semesterId: semester?.semesterId,
    majorId,
    coverUrl: trainingCoverUrl,
    trainingType: trainingTypeToApi(form.type),
    trainingMode,
    paperMode: selectedPaperId.value ? 'MANUAL' : 'NONE',
    paperId: selectedPaperId.value || undefined,
    openStartTime: form.range[0],
    openEndTime: form.range[1],
    teamSize: hasTeamTopic ? roles.length : 1,
    appRequired: form.recordingEnabled,
    classIds: [...selectedClassIds.value],
    teacherIds: [...selectedTeacherIds.value],
    classroomId: selectedRoomId.value,
    scoreBasis: (form.scoreBasis === '最后一次提交的成绩' ? 'LAST_SUBMIT' : 'HIGHEST') as 'HIGHEST' | 'LAST_SUBMIT',
    topicIds: [...selectedTopicIds.value],
    roles,
    publishStatus
  };
}

async function saveDraft() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入实训课名称');
    return;
  }
  if (form.range.length !== 2 || !form.range[0] || !form.range[1]) {
    ElMessage.warning('请选择实训起止时间');
    return;
  }
  if (!semesterOptions.value.some((item) => item.value === form.semester)) {
    ElMessage.warning('请选择所属学年学期');
    return;
  }
  if (!selectedClassIds.value.length) {
    ElMessage.warning('请选择参训班级或学员');
    return;
  }
  if (!selectedTeacherIds.value.length) {
    ElMessage.warning('请选择监考教师');
    return;
  }
  if (!selectedRoomId.value) {
    ElMessage.warning('请选择教室');
    return;
  }
  if (!selectedTopicIds.value.length) {
    ElMessage.warning('请至少添加一道实训题');
    return;
  }
  const teamTopics = selectedTopics.value.filter((item) => item.mode === '多人实训');
  const singleTopics = selectedTopics.value.filter((item) => item.mode === '单人实训');
  if (form.type === '考试' && teamTopics.length > 1) {
    ElMessage.warning('考试类型最多只能添加一道多人实训题');
    return;
  }
  if (form.type === '考试' && teamTopics.length && singleTopics.length) {
    ElMessage.warning('考试类型不能混合添加单人和多人实训题');
    return;
  }
  if (teamTopics.some((item) => item.roles.length < 2)) {
    ElMessage.warning('多人实训题至少需要配置两个角色');
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
    ElMessage.error(error instanceof Error ? error.message : '发布失败');
  }
}

function trainingTypeToApi(type?: string) {
  if (type === '考试') return 'EXAM';
  if (type === '练习') return 'PRACTICE';
  return undefined;
}

function apiTrainingTypeToText(type?: string): '考试' | '练习' {
  return type === 'PRACTICE' ? '练习' : '考试';
}

function apiTrainingModeToText(mode?: string): '单人实训' | '协同实训' {
  return mode === 'SINGLE' ? '单人实训' : '协同实训';
}

function mapTopicRow(item: TopicItem): TopicRow {
  return {
    ...item,
    mode: item.mode,
    roles: item.roles
  };
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

.admin-training-form-page {
  min-height: 100vh;
  padding: 0 20px 0;
  background: #f6f8fc;
}

.admin-training-form-topbar {
  min-height: 52px;
  margin: 0 -20px;
  padding: 0 20px;
  border-bottom: 2px solid #6d5efc;
  background: #ffffff;
}

.admin-training-form-left {
  gap: 12px;
}

.admin-training-form-back.el-button {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  font-size: 14px;
}

.admin-training-form-breadcrumb {
  font-size: 13px;
}

.admin-training-form-topbar h1 {
  font-size: 16px;
}

.admin-training-form-content {
  min-height: calc(100vh - 52px);
  display: grid;
  align-content: start;
  gap: 18px;
  border: 0;
  border-radius: 0;
  padding: 20px 0 76px;
  background: #f6f8fc;
}

.admin-training-base-card,
.admin-training-question-card {
  border: 1px solid #e1e8f2;
  border-radius: 10px;
  background: #ffffff;
}

.admin-training-base-card {
  min-height: 458px;
  padding: 22px 22px 18px;
}

.admin-training-section-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.admin-training-section-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  border-radius: 8px;
  font-size: 16px;
}

.admin-training-section-icon.blue {
  background: #eaf3ff;
  color: #3b82f6;
}

.admin-training-section-icon.purple {
  background: #f3e8ff;
  color: #a855f7;
}

.admin-training-section-head strong {
  display: block;
  color: #17233d;
  font-size: 16px;
  line-height: 20px;
  font-weight: 900;
}

.admin-training-section-head p {
  margin: 4px 0 0;
  color: #8aa0bd;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.admin-training-base-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 22px 26px;
  margin-top: 22px;
}

.admin-training-form-item {
  min-width: 0;
  display: grid;
  gap: 9px;
}

.admin-training-form-item > span {
  color: #334155;
  font-size: 13px;
  line-height: 18px;
  font-weight: 800;
}

.admin-training-form-item b {
  color: #ef4444;
  font-weight: 900;
}

.admin-training-form-item :deep(.el-input__wrapper),
.admin-training-form-item :deep(.el-select__wrapper),
.admin-training-date-range :deep(.el-input__wrapper) {
  min-height: 40px;
  border-radius: 8px;
  background: #f8fafc;
  box-shadow: 0 0 0 1px #dfe7f1 inset;
}

.admin-training-form-item :deep(.el-input__inner),
.admin-training-form-item :deep(.el-select__placeholder),
.admin-training-date-range :deep(.el-input__inner) {
  color: #8aa0bd;
  font-size: 13px;
  font-weight: 600;
}

.admin-training-date-range {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 24px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}

.admin-training-date-range em {
  color: #8aa0bd;
  font-style: normal;
  font-size: 13px;
  text-align: center;
  font-weight: 700;
}

.admin-training-radio-line {
  min-height: 40px;
  display: flex;
  align-items: center;
  gap: 34px;
}

.admin-training-radio-line :deep(.el-radio) {
  margin-right: 0;
  color: #64748b;
  font-weight: 700;
}

.admin-training-radio-line :deep(.el-radio__label) {
  font-size: 14px;
}

.admin-training-radio-line :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #3b82f6;
  background: #3b82f6;
}

.admin-training-radio-line :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #3b82f6;
}

.admin-training-question-card {
  overflow: hidden;
}

.admin-training-question-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px 16px;
}

.admin-training-add-topic.el-button {
  width: 112px;
  height: 36px;
  border: 0;
  border-radius: 7px;
  background: #3b82f6;
  font-size: 13px;
  font-weight: 900;
}

.admin-training-topic-table-scroll {
  margin: 0 22px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  overflow: hidden;
}

.admin-training-topic-table {
  min-width: 0;
  table-layout: fixed;
}

.admin-training-topic-table th,
.admin-training-topic-table td {
  height: 50px;
  padding: 0 18px;
  border-bottom: 1px solid #edf2f8;
  font-size: 13px;
}

.admin-training-topic-table th {
  background: #f8fafc;
  color: #52657d;
  font-weight: 900;
}

.admin-training-topic-table th:first-child,
.admin-training-topic-table td:first-child {
  width: 64px;
  text-align: center;
}

.admin-training-topic-table th:nth-child(2),
.admin-training-topic-table td:nth-child(2) {
  width: 72px;
  text-align: center;
}

.admin-training-topic-table th:nth-child(4),
.admin-training-topic-table td:nth-child(4) {
  width: 130px;
}

.admin-training-topic-table th:nth-child(5),
.admin-training-topic-table td:nth-child(5) {
  width: 250px;
}

.admin-training-topic-table th:nth-child(6),
.admin-training-topic-table td:nth-child(6) {
  width: 170px;
}

.admin-training-drag-handle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 7px;
  background: #f1f5f9;
  color: #9aabc0;
  font-size: 17px;
  line-height: 1;
}

.topic-name {
  color: #17233d;
  font-weight: 800;
}

.admin-training-role-checks {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-training-role-checks :deep(.el-checkbox) {
  height: 24px;
  margin-right: 0;
}

.admin-training-role-checks :deep(.el-checkbox__label) {
  padding-left: 5px;
  color: #52657d;
  font-size: 12px;
  font-weight: 700;
}

.admin-training-delete-topic.el-button {
  min-width: 58px;
  height: 28px;
  border-radius: 5px;
  background: #fff1f2;
  color: #ff5a67;
  font-size: 12px;
  font-weight: 900;
}

.admin-training-question-summary {
  height: 42px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  color: #8aa0bd;
  font-size: 13px;
  font-weight: 700;
}

.admin-training-question-summary b {
  margin: 0 4px;
  color: #3b82f6;
}

.admin-training-form-footer {
  position: fixed;
  right: 20px;
  bottom: 0;
  left: 20px;
  z-index: 8;
  min-height: 62px;
  align-items: center;
  padding: 0 20px;
  border: 1px solid #e1e8f2;
  border-bottom: 0;
  border-radius: 10px 10px 0 0;
}

.admin-training-form-footer .el-button {
  min-width: 76px;
  height: 36px;
  border-radius: 7px;
  font-weight: 800;
}

.admin-training-form-footer .el-button--primary {
  background: #3b82f6;
}

.admin-training-topic-dialog.el-dialog {
  border-radius: 10px;
}

.admin-training-topic-picker {
  display: grid;
  gap: 14px;
}

.admin-training-topic-overview {
  display: flex;
  min-height: 42px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 14px;
  border: 1px solid #edf1f7;
  border-radius: 6px;
  background: #fbfcfe;
}

.admin-training-topic-overview p {
  margin: 0;
  color: #52657d;
  font-size: 13px;
  white-space: nowrap;
}

.admin-training-topic-overview p b {
  margin: 0 4px;
  color: #3478f6;
}

.admin-training-topic-overview span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #98a4b5;
  font-size: 12px;
}

.admin-training-topic-overview .el-icon {
  flex: 0 0 auto;
}

.admin-training-topic-dialog-table {
  max-height: 440px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  overflow: auto;
}

.admin-training-topic-dialog-table .admin-training-topic-table {
  min-width: 820px;
}

@media (max-width: 980px) {
  .admin-training-base-grid {
    grid-template-columns: 1fr;
  }

  .admin-training-date-range {
    grid-template-columns: 1fr;
  }

  .admin-training-date-range em {
    display: none;
  }

  .admin-training-form-footer {
    right: 12px;
    left: 12px;
  }

  .admin-training-topic-overview {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
    padding-top: 10px;
    padding-bottom: 10px;
  }
}
</style>
