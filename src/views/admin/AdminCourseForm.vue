<template>
  <AdminShell activeKey="admin-courses">
    <section class="admin-course-form-page">
      <header class="admin-course-form-topbar">
        <el-breadcrumb class="admin-course-form-breadcrumb" separator="/">
          <el-breadcrumb-item>教学实训</el-breadcrumb-item>
          <el-breadcrumb-item>教学课程</el-breadcrumb-item>
          <el-breadcrumb-item>新增课程</el-breadcrumb-item>
        </el-breadcrumb>

        <el-button class="admin-course-form-back" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </header>

      <section class="admin-course-form-card basic">
        <header class="admin-course-form-card-title">
          <el-icon><InfoFilled /></el-icon>
          <strong>基本信息</strong>
        </header>

        <div class="admin-course-form-fields">
          <label class="admin-course-form-field full">
            <span>课程名称 <b>*</b></span>
            <el-input v-model="form.courseName" placeholder="请输入课程名称" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学开始时间 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" />
          </label>

          <label class="admin-course-form-field">
            <span>
              教学结束时间 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="请选择结束时间" />
          </label>

          <label class="admin-course-form-field">
            <span>所属学年学期 <b>*</b></span>
            <el-select v-model="form.semesterKey" placeholder="请选择学期" filterable>
              <el-option
                v-for="item in semesterOptions"
                :key="item.key"
                :label="item.label"
                :value="item.key"
              />
            </el-select>
          </label>

          <label class="admin-course-form-field">
            <span>所属专业 <b>*</b></span>
            <el-select v-model="form.majorId" placeholder="请选择专业" filterable @change="handleMajorChange">
              <el-option v-for="item in majorOptions" :key="item.majorId" :label="item.majorName" :value="item.majorId" />
            </el-select>
          </label>

          <label class="admin-course-form-field">
            <span>
              课件完成度满分 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-input v-model="form.coursewareScore" placeholder="请输入满分值" />
          </label>

          <div class="admin-course-form-field full radio">
            <span>
              学生学习模式 <b>*</b>
              <el-icon><InfoFilled /></el-icon>
            </span>
            <el-radio-group v-model="form.learningMode" class="admin-course-form-radio">
              <el-radio label="SELF_PACED">自由学习</el-radio>
              <el-radio label="SEQUENTIAL">顺序解锁</el-radio>
            </el-radio-group>
          </div>
        </div>
      </section>

      <section class="admin-course-form-card compact">
        <header class="admin-course-form-card-title">
          <el-icon><UserFilled /></el-icon>
          <strong>教学团队</strong>
        </header>
        <div class="admin-course-form-select-block">
          <el-select v-model="form.teacherIds" multiple filterable collapse-tags collapse-tags-tooltip placeholder="请选择教师">
            <el-option
              v-for="item in teacherOptions"
              :key="item.userId"
              :label="item.realName || item.accountNo"
              :value="item.userId"
            >
              <div class="admin-course-form-option">
                <strong>{{ item.realName || item.accountNo }}</strong>
                <span>{{ item.accountNo || '未配置工号' }}</span>
              </div>
            </el-option>
          </el-select>
          <div class="admin-course-selected-box">
            <span v-if="selectedTeachers.length === 0" class="admin-course-selected-empty">暂未选择教学团队</span>
            <el-tag
              v-for="item in selectedTeachers"
              v-else
              :key="item.userId"
              closable
              @close="removeTeacher(item.userId)"
            >
              {{ item.realName || item.accountNo }}
            </el-tag>
          </div>
        </div>
      </section>

      <section class="admin-course-form-card compact">
        <header class="admin-course-form-card-title">
          <el-icon><User /></el-icon>
          <strong>授课班级</strong>
        </header>
        <div class="admin-course-form-select-block">
          <el-select v-model="form.classIds" multiple filterable collapse-tags collapse-tags-tooltip placeholder="请选择班级">
            <el-option
              v-for="item in classOptions"
              :key="item.classId"
              :label="item.majorName ? `${item.className}（${item.majorName}）` : item.className"
              :value="item.classId"
            >
              <div class="admin-course-form-option">
                <strong>{{ item.className }}</strong>
                <span>{{ item.majorName || '未配置专业' }}</span>
              </div>
            </el-option>
          </el-select>
          <div class="admin-course-selected-box">
            <span v-if="selectedClasses.length === 0" class="admin-course-selected-empty">暂未选择授课班级</span>
            <el-tag
              v-for="item in selectedClasses"
              v-else
              :key="item.classId"
              closable
              @close="removeClass(item.classId)"
            >
              {{ item.className }}
              <small v-if="item.majorName">{{ item.majorName }}</small>
            </el-tag>
          </div>
        </div>
      </section>

      <section class="admin-course-form-card content">
        <header class="admin-course-form-card-title split">
          <span>
            <el-icon><Menu /></el-icon>
            <strong>教学内容</strong>
          </span>
          <el-button type="primary" class="admin-course-form-primary" @click="addChapter">
            <el-icon><Plus /></el-icon>
            新增章节
          </el-button>
        </header>

        <div class="admin-course-outline">
          <div v-if="chapters.length === 0" class="admin-course-empty inline">
            <el-empty description="暂无教学内容，请先新增章节" />
          </div>
          <article v-else v-for="chapter in chapters" :key="chapter.id" class="admin-course-outline-chapter">
            <div class="admin-course-outline-row admin-course-outline-chapter-row">
              <span class="admin-course-outline-left">
                <el-icon><ArrowDown /></el-icon>
                <el-icon class="folder"><Folder /></el-icon>
                <strong>{{ chapter.title }}</strong>
              </span>
              <span class="admin-course-outline-actions">
                <el-button text type="success" @click="addSection(chapter)">新增小节</el-button>
                <el-button text type="primary" @click="editChapter(chapter)">编辑</el-button>
                <el-button text type="danger" @click="removeChapter(chapters.indexOf(chapter))">删除</el-button>
              </span>
            </div>

            <template v-for="section in chapter.sections" :key="section.id">
              <div class="admin-course-outline-row admin-course-outline-section-row">
                <span class="admin-course-outline-left">
                  <el-icon><ArrowDown /></el-icon>
                  <strong>{{ section.title }}</strong>
                </span>
                <span class="admin-course-outline-actions">
                  <el-button text type="success" @click="addOutlineItem(section, 'resource')">添加课件资源</el-button>
                  <el-button text type="success" @click="addOutlineItem(section, 'homework')">添加作业</el-button>
                  <el-button text type="primary" @click="editSection(section)">编辑</el-button>
                  <el-button text type="danger" @click="removeSection(chapter, chapter.sections.indexOf(section))">删除</el-button>
                </span>
              </div>

              <div
                v-for="item in section.items"
                :key="item.id"
                class="admin-course-outline-row admin-course-outline-resource-row"
              >
                <span class="admin-course-outline-drag">::</span>
                <span class="admin-course-outline-icon" :class="item.type">
                  <el-icon><component :is="item.type === 'homework' ? Checked : Document" /></el-icon>
                </span>
                <span class="admin-course-outline-info">
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.desc }}</small>
                </span>
                <span class="admin-course-outline-actions compact-actions">
                  <el-button text type="primary" @click="editOutlineItem(item)">编辑</el-button>
                  <el-button text type="danger" @click="removeOutlineItem(section, section.items.indexOf(item))">删除</el-button>
                </span>
              </div>
            </template>
          </article>
        </div>
      </section>

      <footer class="admin-course-form-footer">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCourse">保存</el-button>
      </footer>
    </section>

    <el-dialog
      v-model="resourceDialogVisible"
      class="admin-course-resource-dialog"
      width="860px"
      :show-close="false"
      append-to-body
    >
      <template #header>
        <div class="admin-course-resource-dialog-head">
          <strong>添加教学资源</strong>
          <el-button text circle :icon="Close" @click="resourceDialogVisible = false" />
        </div>
      </template>

      <div class="admin-course-resource-dialog-body">
        <div class="admin-course-resource-searchbar">
          <el-input
            v-model="resourceKeyword"
            :prefix-icon="Search"
            clearable
            placeholder="请输入资源名称"
            @keyup.enter="handleResourceSearch"
            @clear="handleResourceSearch"
          />
          <el-button type="primary" @click="handleResourceSearch">查询</el-button>
        </div>

        <div class="admin-course-resource-tabs">
          <button
            v-for="item in resourceTypeTabs"
            :key="item.value"
            type="button"
            :class="{ active: resourceTypeFilter === item.value }"
            @click="changeResourceType(item.value)"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="admin-course-resource-table" v-loading="resourceLoading">
          <div class="admin-course-resource-table-head">
            <span>资源名称</span>
            <span>资源类型</span>
            <span>所属课程</span>
            <span>上传人</span>
            <span>上传时间</span>
          </div>
          <button
            v-for="item in resourceRows"
            :key="item.resourceId"
            type="button"
            class="admin-course-resource-row"
            :class="{ active: selectedResource?.resourceId === item.resourceId }"
            @click="selectResource(item)"
          >
            <span class="admin-course-resource-name">
              <i>{{ resourceInitial(item) }}</i>
              <strong>{{ item.resourceName }}</strong>
              <small>{{ item.fileName || formatResourceSize(item.fileSize) }}</small>
            </span>
            <span>{{ resourceTypeLabel(item.resourceType) }}</span>
            <span>{{ item.courseName || '-' }}</span>
            <span>{{ item.uploaderName || '-' }}</span>
            <span>{{ formatResourceDate(item.createdAt) }}</span>
          </button>
          <el-empty v-if="!resourceLoading && resourceRows.length === 0" description="暂无可添加资源" />
        </div>

        <div class="admin-course-resource-pagination">
          <span>共 {{ resourceTotal }} 条</span>
          <el-pagination
            layout="prev, pager, next"
            :current-page="resourcePage"
            :page-size="resourcePageSize"
            :total="resourceTotal"
            @current-change="handleResourcePageChange"
          />
        </div>
      </div>

      <template #footer>
        <div class="admin-course-resource-dialog-footer">
          <el-button @click="resourceDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!selectedResource" @click="confirmAddResource">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ArrowDown,
  ArrowLeft,
  Checked,
  Close,
  Document,
  Folder,
  InfoFilled,
  Menu,
  Plus,
  Search,
  User,
  UserFilled
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  createAdminCourse,
  fetchAdminAcademicYears,
  fetchAdminClasses,
  fetchAdminMajors,
  fetchAdminTeachers,
  type AdminAcademicYearOption,
  type AdminClassOption,
  type AdminMajorOption,
  type AdminTeacherOption
} from '../../api/admin-course';
import { fetchAdminResources, type AdminResource } from '../../api/admin-resource';

const router = useRouter();

const form = reactive({
  courseName: '',
  startTime: undefined as Date | undefined,
  endTime: undefined as Date | undefined,
  semesterKey: '',
  majorId: undefined as number | undefined,
  coursewareScore: '100',
  learningMode: 'SELF_PACED',
  teacherIds: [] as number[],
  classIds: [] as number[]
});

const saving = ref(false);
const academicYears = ref<AdminAcademicYearOption[]>([]);
const majorOptions = ref<AdminMajorOption[]>([]);
const classOptions = ref<AdminClassOption[]>([]);
const teacherOptions = ref<AdminTeacherOption[]>([]);

const semesterOptions = computed(() =>
  academicYears.value.flatMap((year) =>
    (year.semesters ?? []).map((semester) => ({
      key: `${year.academicYearId}:${semester.semesterId}`,
      label: `${year.yearName} ${semester.semesterName}`,
      academicYearId: year.academicYearId,
      semesterId: semester.semesterId,
      current: semester.current
    }))
  )
);

const selectedTeachers = computed(() => teacherOptions.value.filter((item) => form.teacherIds.includes(item.userId)));
const selectedClasses = computed(() => classOptions.value.filter((item) => form.classIds.includes(item.classId)));

interface OutlineItem {
  id: number;
  type: 'homework' | 'resource';
  title: string;
  desc: string;
  resourceId?: number;
}

interface OutlineSection {
  id: number;
  title: string;
  items: OutlineItem[];
}

interface OutlineChapter {
  id: number;
  title: string;
  sections: OutlineSection[];
}

const chapters = ref<OutlineChapter[]>([]);
let outlineIdSeed = 1;
const resourceDialogVisible = ref(false);
const resourceLoading = ref(false);
const resourceKeyword = ref('');
const resourceTypeFilter = ref('');
const resourceRows = ref<AdminResource[]>([]);
const resourceTotal = ref(0);
const resourcePage = ref(1);
const resourcePageSize = 8;
const selectedResource = ref<AdminResource>();
const resourceTargetSection = ref<OutlineSection | null>(null);
const resourceTypeTabs = [
  { label: '全部', value: '' },
  { label: '文档', value: 'DOCUMENT' },
  { label: '课件', value: 'PRESENTATION' },
  { label: '视频', value: 'VIDEO' },
  { label: '图片', value: 'IMAGE' }
];

function goBack() {
  router.push('/admin/courses');
}

function nextOutlineId() {
  outlineIdSeed += 1;
  return outlineIdSeed;
}

async function promptOutlineText(message: string, title: string, initialValue = '') {
  try {
    const result = await ElMessageBox.prompt(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: initialValue,
      inputValidator: (value) => {
        if (!value || !value.trim()) {
          return '请输入内容';
        }
        return true;
      }
    });
    return String(result.value || '').trim();
  } catch {
    return '';
  }
}

async function addChapter() {
  const title = await promptOutlineText('请输入章节名称', '新增章节');
  if (!title) return;
  chapters.value.push({ id: nextOutlineId(), title, sections: [] });
}

async function editChapter(chapter: OutlineChapter) {
  const title = await promptOutlineText('请输入章节名称', '编辑章节', chapter.title);
  if (!title) return;
  chapter.title = title;
}

async function removeChapter(index: number) {
  try {
    await ElMessageBox.confirm('确认删除该章节？', '删除章节', { type: 'warning' });
    chapters.value.splice(index, 1);
  } catch {
    return;
  }
}

async function addSection(chapter: OutlineChapter) {
  const title = await promptOutlineText('请输入小节名称', '新增小节');
  if (!title) return;
  chapter.sections.push({ id: nextOutlineId(), title, items: [] });
}

async function editSection(section: OutlineSection) {
  const title = await promptOutlineText('请输入小节名称', '编辑小节', section.title);
  if (!title) return;
  section.title = title;
}

async function removeSection(chapter: OutlineChapter, index: number) {
  try {
    await ElMessageBox.confirm('确认删除该小节？', '删除小节', { type: 'warning' });
    chapter.sections.splice(index, 1);
  } catch {
    return;
  }
}

async function addOutlineItem(section: OutlineSection, type: OutlineItem['type']) {
  if (type === 'resource') {
    openResourceDialog(section);
    return;
  }

  const title = await promptOutlineText(type === 'homework' ? '请输入作业名称' : '请输入课件资源名称', type === 'homework' ? '添加作业' : '添加课件资源');
  if (!title) return;
  const desc = await promptOutlineText(type === 'homework' ? '请输入作业说明' : '请输入资源说明', type === 'homework' ? '作业说明' : '资源说明');
  section.items.push({ id: nextOutlineId(), type, title, desc: desc || '-' });
}

function openResourceDialog(section: OutlineSection) {
  resourceTargetSection.value = section;
  selectedResource.value = undefined;
  resourceKeyword.value = '';
  resourceTypeFilter.value = '';
  resourcePage.value = 1;
  resourceDialogVisible.value = true;
  void loadResourceRows();
}

async function loadResourceRows() {
  resourceLoading.value = true;
  try {
    const result = await fetchAdminResources({
      keyword: resourceKeyword.value,
      resourceType: resourceTypeFilter.value,
      page: resourcePage.value,
      pageSize: resourcePageSize
    });
    resourceRows.value = result.records;
    resourceTotal.value = result.total;
  } catch (error) {
    resourceRows.value = [];
    resourceTotal.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '资源列表加载失败');
  } finally {
    resourceLoading.value = false;
  }
}

function handleResourceSearch() {
  resourcePage.value = 1;
  selectedResource.value = undefined;
  void loadResourceRows();
}

function changeResourceType(value: string) {
  resourceTypeFilter.value = value;
  handleResourceSearch();
}

function handleResourcePageChange(page: number) {
  resourcePage.value = page;
  selectedResource.value = undefined;
  void loadResourceRows();
}

function selectResource(item: AdminResource) {
  selectedResource.value = item;
}

function confirmAddResource() {
  if (!selectedResource.value || !resourceTargetSection.value) {
    ElMessage.warning('请选择教学资源');
    return;
  }

  const resource = selectedResource.value;
  resourceTargetSection.value.items.push({
    id: nextOutlineId(),
    type: 'resource',
    title: resource.resourceName,
    desc: `${resourceTypeLabel(resource.resourceType)} · ${resource.courseName || resource.fileName || '教学资源'}`,
    resourceId: resource.resourceId
  });
  resourceDialogVisible.value = false;
  ElMessage.success('已添加教学资源');
}

async function editOutlineItem(item: OutlineItem) {
  const title = await promptOutlineText('请输入名称', '编辑内容', item.title);
  if (!title) return;
  const desc = await promptOutlineText('请输入说明', '编辑说明', item.desc);
  item.title = title;
  item.desc = desc || item.desc;
}

async function removeOutlineItem(section: OutlineSection, index: number) {
  try {
    await ElMessageBox.confirm('确认删除该内容？', '删除内容', { type: 'warning' });
    section.items.splice(index, 1);
  } catch {
    return;
  }
}

function formatLocalDateTime(value?: Date) {
  if (!value) {
    return undefined;
  }

  const pad = (num: number) => String(num).padStart(2, '0');
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(
    value.getMinutes()
  )}:${pad(value.getSeconds())}`;
}

function selectedSemester() {
  return semesterOptions.value.find((item) => item.key === form.semesterKey);
}

function removeTeacher(userId: number) {
  form.teacherIds = form.teacherIds.filter((id) => id !== userId);
}

function removeClass(classId: number) {
  form.classIds = form.classIds.filter((id) => id !== classId);
}

function validateForm() {
  const semester = selectedSemester();
  if (!form.courseName.trim()) {
    throw new Error('请输入课程名称');
  }
  if (!form.startTime || !form.endTime) {
    throw new Error('请选择教学起止时间');
  }
  if (!semester) {
    throw new Error('请选择学年学期');
  }
  if (!form.majorId) {
    throw new Error('请选择所属专业');
  }
  if (!form.teacherIds.length) {
    throw new Error('请选择教学团队');
  }
  if (!form.classIds.length) {
    throw new Error('请选择授课班级');
  }

  const scoreCap = Number(form.coursewareScore);
  if (!Number.isFinite(scoreCap) || scoreCap <= 0 || scoreCap > 100) {
    throw new Error('课件完成度满分需为 1-100');
  }

  return { semester, scoreCap };
}

async function saveCourse() {
  let payload: ReturnType<typeof validateForm>;
  try {
    payload = validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善课程信息');
    return;
  }

  saving.value = true;
  try {
    await createAdminCourse({
      courseName: form.courseName.trim(),
      academicYearId: payload.semester.academicYearId,
      semesterId: payload.semester.semesterId,
      majorId: form.majorId,
      coverUrl: '/assets/course-station-preview-Dw2Sploi.png',
      openStartTime: formatLocalDateTime(form.startTime),
      openEndTime: formatLocalDateTime(form.endTime),
      teacherIds: form.teacherIds,
      classIds: form.classIds,
      learningMode: form.learningMode,
      assignmentCompletionRule: 'SUBMIT',
      coursewareScoreCap: payload.scoreCap,
      chapters: chapters.value.map((chapter, index) => ({
        chapterTitle: chapter.title.slice(0, 20),
        sortOrder: index + 1,
        children: chapter.sections.map((section, sectionIndex) => ({
          chapterTitle: section.title.slice(0, 20),
          sortOrder: sectionIndex + 1,
          contents: section.items.map((item, itemIndex) => ({
            itemType: item.type === 'homework' ? 'ASSIGNMENT' : 'COURSEWARE',
            title: item.title.slice(0, 30),
            sortOrder: itemIndex + 1,
            resourceId: item.resourceId
          }))
        }))
      }))
    });
    ElMessage.success('课程已保存');
    goBack();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程保存失败');
  } finally {
    saving.value = false;
  }
}

function resourceTypeLabel(type?: string) {
  const labels: Record<string, string> = {
    DOCUMENT: '文档',
    PRESENTATION: '课件',
    VIDEO: '视频',
    IMAGE: '图片',
    AUDIO: '音频',
    EXAM: '试题'
  };
  return type ? labels[type] || type : '资源';
}

function formatResourceSize(size?: number) {
  if (!size) {
    return '未配置大小';
  }
  if (size < 1024 * 1024) {
    return `${Math.max(1, Math.round(size / 1024))}KB`;
  }
  return `${(size / 1024 / 1024).toFixed(1)}MB`;
}

function formatResourceDate(value?: string) {
  if (!value) {
    return '-';
  }
  return value.slice(0, 10);
}

function resourceInitial(item: AdminResource) {
  return resourceTypeLabel(item.resourceType).slice(0, 1);
}

async function loadOptions() {
  try {
    const [years, majors, classes, teachers] = await Promise.all([
      fetchAdminAcademicYears(),
      fetchAdminMajors(),
      fetchAdminClasses(),
      fetchAdminTeachers()
    ]);
    academicYears.value = years;
    majorOptions.value = majors.filter((item) => item.enabled !== false);
    classOptions.value = classes.filter((item) => item.enabled !== false);
    teacherOptions.value = teachers.filter((item) => item.enabled !== false);
    form.semesterKey = semesterOptions.value.find((item) => item.current)?.key || '';
    form.majorId = undefined;
    form.teacherIds = [];
    form.classIds = [];
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '课程基础数据加载失败');
  }
}

async function handleMajorChange(value?: number) {
  try {
    classOptions.value = (await fetchAdminClasses(value)).filter((item) => item.enabled !== false);
    form.classIds = [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '班级列表加载失败');
  }
}

watch(
  () => form.majorId,
  (majorId, oldMajorId) => {
    if (majorId && oldMajorId && majorId !== oldMajorId) {
      void handleMajorChange(majorId);
    }
  }
);

onMounted(() => {
  void loadOptions();
});
</script>
