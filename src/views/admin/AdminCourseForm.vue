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
        <div class="admin-course-form-tags">
          <el-select v-model="form.teacherIds" multiple filterable placeholder="请选择教师">
            <el-option
              v-for="item in teacherOptions"
              :key="item.userId"
              :label="item.realName || item.accountNo"
              :value="item.userId"
            />
          </el-select>
        </div>
      </section>

      <section class="admin-course-form-card compact">
        <header class="admin-course-form-card-title">
          <el-icon><User /></el-icon>
          <strong>授课班级</strong>
        </header>
        <div class="admin-course-form-tags">
          <el-select v-model="form.classIds" multiple filterable placeholder="请选择班级">
            <el-option
              v-for="item in classOptions"
              :key="item.classId"
              :label="item.majorName ? `${item.className}（${item.majorName}）` : item.className"
              :value="item.classId"
            />
          </el-select>
        </div>
      </section>

      <section class="admin-course-form-card content">
        <header class="admin-course-form-card-title split">
          <span>
            <el-icon><Menu /></el-icon>
            <strong>教学内容</strong>
          </span>
          <el-button type="primary" class="admin-course-form-primary" @click="showComingSoon('新增章节')">
            <el-icon><Plus /></el-icon>
            新增章节
          </el-button>
        </header>

        <div class="admin-course-outline">
          <article v-for="chapter in chapters" :key="chapter.id" class="admin-course-outline-chapter">
            <div class="admin-course-outline-row admin-course-outline-chapter-row">
              <span class="admin-course-outline-left">
                <el-icon><ArrowDown /></el-icon>
                <el-icon class="folder"><Folder /></el-icon>
                <strong>{{ chapter.title }}</strong>
              </span>
              <span class="admin-course-outline-actions">
                <el-button text type="success" @click="showComingSoon('添加课件资源')">添加课件资源</el-button>
                <el-button text type="success" @click="showComingSoon('添加作业')">添加作业</el-button>
                <el-button text type="primary" @click="showComingSoon('编辑')">编辑</el-button>
                <el-button text type="danger" @click="showComingSoon('删除')">删除</el-button>
              </span>
            </div>

            <template v-for="section in chapter.sections" :key="section.id">
              <div class="admin-course-outline-row admin-course-outline-section-row">
                <span class="admin-course-outline-left">
                  <el-icon><ArrowDown /></el-icon>
                  <strong>{{ section.title }}</strong>
                </span>
                <span class="admin-course-outline-actions">
                  <el-button text type="success" @click="showComingSoon('添加课件资源')">添加课件资源</el-button>
                  <el-button text type="success" @click="showComingSoon('添加作业')">添加作业</el-button>
                  <el-button text type="primary" @click="showComingSoon('编辑')">编辑</el-button>
                  <el-button text type="danger" @click="showComingSoon('删除')">删除</el-button>
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
                  <el-button text type="primary" @click="showComingSoon('编辑')">编辑</el-button>
                  <el-button text type="danger" @click="showComingSoon('删除')">删除</el-button>
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
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowDown,
  ArrowLeft,
  Checked,
  Document,
  Folder,
  InfoFilled,
  Menu,
  Plus,
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

const chapters = [
  {
    id: 1,
    title: '第一章 信号系统概述',
    sections: [
      {
        id: 11,
        title: '1.1 轨道交通与信号系统基本概念',
        items: [
          {
            id: 111,
            type: 'homework',
            title: '课程作业：信号系统组成分析报告',
            desc: '提交即完成 | 截止时间：2025-04-15 23:59'
          },
          {
            id: 112,
            type: 'resource',
            title: '课件资源：信号系统原理.pptx',
            desc: '最低预览：2分钟 | 可学时段：2025-01-01至2025-04-10'
          }
        ]
      },
      {
        id: 12,
        title: '1.2 信号系统核心作用与特点',
        items: []
      }
    ]
  },
  {
    id: 2,
    title: '第二章 联锁系统原理',
    sections: []
  },
  {
    id: 3,
    title: '第三章 ATS系统',
    sections: []
  }
];

function goBack() {
  router.push('/admin/courses');
}

function showComingSoon(label: string) {
  ElMessage.info(`${label}功能待接入资源/作业选择接口`);
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
      chapters: chapters.map((chapter, index) => ({
        chapterTitle: chapter.title.slice(0, 20),
        sortOrder: index + 1,
        children: chapter.sections.map((section, sectionIndex) => ({
          chapterTitle: section.title.slice(0, 20),
          sortOrder: sectionIndex + 1,
          contents: []
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
    form.semesterKey = semesterOptions.value.find((item) => item.current)?.key || semesterOptions.value[0]?.key || '';
    form.majorId = majorOptions.value[0]?.majorId;
    form.teacherIds = teacherOptions.value[0]?.userId ? [teacherOptions.value[0].userId] : [];
    form.classIds = classOptions.value[0]?.classId ? [classOptions.value[0].classId] : [];
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
