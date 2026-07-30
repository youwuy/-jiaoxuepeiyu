<template>
  <StudentShell eyebrow="个人中心" title="个人中心">
    <section class="profile-page">
      <aside class="profile-sidebar" aria-label="个人中心菜单">
        <button
          v-for="item in profileTabs"
          :key="item.key"
          class="profile-side-item"
          :class="{ active: activeTab === item.key }"
          type="button"
          @click="openTab(item.key)"
        >
          <el-icon>
            <User v-if="item.key === 'info'" />
            <Document v-else-if="item.key === 'archives'" />
            <Histogram v-else-if="item.key === 'score'" />
            <Bell v-else />
          </el-icon>
          <span>{{ item.label }}</span>
          <strong v-if="item.key === 'messages' && unreadCount > 0">{{ unreadCount }}</strong>
        </button>
      </aside>

      <main class="profile-workspace">
        <nav class="profile-breadcrumb" aria-label="面包屑">
          <span>个人中心</span>
          <el-icon><ArrowRight /></el-icon>
          <strong>{{ currentTitle }}</strong>
          <template v-if="showArchiveDetail">
            <el-icon><ArrowRight /></el-icon>
            <strong>实训详情</strong>
          </template>
        </nav>

        <section v-if="activeTab === 'info'" class="profile-info-card">
          <div v-for="field in profileFields" :key="field.label" class="profile-field">
            <span>{{ field.label }}</span>
            <div class="profile-field-box" :class="{ editable: field.editTarget }">
              <el-icon>
                <User v-if="field.icon === 'user'" />
                <Postcard v-else-if="field.icon === 'id'" />
                <OfficeBuilding v-else-if="field.icon === 'class'" />
                <Iphone v-else-if="field.icon === 'phone'" />
                <Lock v-else />
              </el-icon>
              <strong>{{ field.value }}</strong>
              <button v-if="field.editTarget" type="button" :aria-label="`修改${field.label}`" @click="openEditDialog(field.editTarget)">
                <el-icon><EditPen /></el-icon>
              </button>
            </div>
          </div>
        </section>

        <section v-else-if="activeTab === 'score'" class="profile-panel">
          <header class="profile-panel-head">
            <h2>综合成绩</h2>
            <p><el-icon><InfoFilled /></el-icon> 综合成绩 = （各模块得分 × 权重）之和</p>
          </header>
          <table class="profile-score-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>学年学期</th>
                <th>课件学习得分</th>
                <th>实训练习得分</th>
                <th>课程作业得分</th>
                <th>考试得分</th>
                <th>综合成绩</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in scoreRows" :key="row.term">
                <td>{{ row.index }}</td>
                <td>{{ row.term }}</td>
                <td>{{ row.courseware }}</td>
                <td>{{ row.training }}</td>
                <td>{{ row.assignment }}</td>
                <td>{{ row.exam }}</td>
                <td><strong>{{ row.total }}</strong></td>
              </tr>
            </tbody>
          </table>
        </section>

        <section v-else-if="activeTab === 'messages'" class="profile-message-page">
          <div class="profile-message-title">
            <div>
              <h1>消息通知</h1>
              <p>查看课程通知、实训通知及资源更新提醒</p>
            </div>
            <button type="button" class="profile-read-all" :disabled="unreadCount === 0 || markAllLoading" @click="handleMarkAllRead">
              <el-icon><Check /></el-icon>
              全部标记已读
            </button>
          </div>

          <article v-for="message in displayMessages" :key="message.id" class="profile-message-card" :class="{ read: !message.unread }">
            <span class="profile-message-icon" :class="message.iconClass">
              <el-icon>
                <Reading v-if="message.iconClass === 'is-course'" />
                <Monitor v-else-if="message.iconClass === 'is-training'" />
                <Tickets v-else />
              </el-icon>
            </span>
            <div>
              <h2>
                {{ message.type }}
                <i v-if="message.unread"></i>
                <em v-else>已读</em>
              </h2>
              <p>{{ message.title }}</p>
              <time><el-icon><Clock /></el-icon>{{ message.time }}</time>
            </div>
            <button
              v-if="message.unread"
              type="button"
              :disabled="messageReadLoadingIds.includes(message.id)"
              @click="handleMarkRead(message.id)"
            >
              标记已读
            </button>
          </article>

          <footer class="profile-message-footer">
            <p>共 <strong>{{ displayMessages.length }}</strong> 条通知， <strong class="danger">{{ unreadCount }}</strong> 条未读</p>
            <div class="profile-mini-pages">
              <button type="button"><el-icon><ArrowLeft /></el-icon></button>
              <button type="button" class="active">1</button>
              <button type="button">2</button>
              <button type="button"><el-icon><ArrowRight /></el-icon></button>
            </div>
          </footer>
        </section>

        <template v-else>
          <section v-if="showArchiveDetail" class="profile-archive-detail">
            <header class="archive-detail-head">
              <div class="archive-detail-meta">
                <span><el-icon><User /></el-icon>学生姓名 <strong>{{ archiveDetailStudentName }}</strong></span>
                <span><el-icon><Postcard /></el-icon>学生学号 <strong>{{ archiveDetailStudentNo }}</strong></span>
                <span><el-icon><OfficeBuilding /></el-icon>所属班级 <strong>{{ archiveDetailClassName }}</strong></span>
                <span><el-icon><Clock /></el-icon>提交时间 <strong>{{ archiveDetailSubmittedAt }}</strong></span>
              </div>
              <div class="archive-detail-title">
                <div>
                  <h1>{{ archiveDetailTitle }}</h1>
                  <p>实训模式: {{ archiveDetailMode }} <span>•</span> 满分分值: <strong>100 分</strong> <span>•</span> 提交类型: {{ archiveDetailSubmitType }}</p>
                </div>
                <button type="button" @click="showArchiveDetail = false">
                  <el-icon><ArrowLeft /></el-icon>
                  返回列表
                </button>
              </div>
            </header>

            <div class="archive-detail-grid">
              <section class="archive-step-card">
                <header>
                  <h2>实训步骤详情</h2>
                  <p><el-icon><InfoFilled /></el-icon> 点击步骤名称可查看对应操作视频</p>
                </header>
                <table class="archive-step-table">
                  <thead>
                    <tr>
                      <th>序号</th>
                      <th>步骤名称</th>
                      <th>正确结果</th>
                      <th>实际操作</th>
                      <th>得分</th>
                      <th>用时(秒)</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="step in visibleArchiveSteps" :key="step.index">
                      <td>{{ step.index }}</td>
                      <td><a>{{ step.name }}</a></td>
                      <td class="expected">{{ step.expected }}</td>
                      <td :class="step.score < 10 ? 'wrong' : 'actual'">{{ step.actual }}</td>
                      <td :class="step.score < 10 ? 'score-low' : 'score-high'">{{ step.score }}</td>
                      <td>{{ step.duration }}</td>
                    </tr>
                  </tbody>
                </table>
                <footer>步骤总得分: <strong>{{ archiveDetailScore }}</strong> / 100 分 <span></span> 总用时: <strong>{{ archiveDetailDurationSeconds }} 秒</strong> <em>（{{ archiveDetailDurationText }}）</em></footer>
              </section>

              <aside class="archive-video-card">
                <h2><el-icon><VideoPlay /></el-icon>实训操作视频</h2>
                <div class="archive-video-box"></div>
                <div class="archive-video-progress">
                  <span>01:24</span>
                  <b></b>
                  <span>04:58</span>
                </div>
                <div class="archive-video-controls">
                  <button type="button"><el-icon><VideoPause /></el-icon></button>
                  <el-icon><Headset /></el-icon>
                  <el-icon><FullScreen /></el-icon>
                </div>
              </aside>
            </div>
          </section>

          <template v-else>
            <section class="archive-filter-card">
              <div>
                <span>实训模式:</span>
                <button class="active" type="button">全部</button>
                <button type="button">单人实训</button>
                <button type="button">多人实训</button>
              </div>
              <el-input v-model="archiveKeyword" class="archive-search" :suffix-icon="Search" placeholder="搜索实训名称" clearable />
            </section>

            <section class="profile-panel archive-list-panel">
              <h2>实训档案</h2>
              <table class="profile-archive-table">
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>实训名称</th>
                    <th>实训模式</th>
                    <th>角色</th>
                    <th>提交时间</th>
                    <th>提交类型</th>
                    <th>时长</th>
                    <th>个人得分</th>
                    <th>整队总分</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in archiveRows" :key="row.index">
                    <td>{{ row.index }}</td>
                    <td><strong>{{ row.title }}</strong></td>
                    <td>{{ row.mode }}</td>
                    <td>{{ row.role }}</td>
                    <td>{{ row.submittedAt }}</td>
                    <td>{{ row.submitType }}</td>
                    <td>{{ row.duration }}</td>
                    <td><strong>{{ row.personalScore }}</strong></td>
                    <td><strong>{{ row.teamScore }}</strong></td>
                    <td><button type="button" @click="openArchiveDetail(row.id)">查看详情</button></td>
                  </tr>
                </tbody>
              </table>
              <footer class="archive-table-footer">
                <p>显示 1 到 5 条，共 5 条记录</p>
                <div class="profile-mini-pages">
                  <button type="button"><el-icon><ArrowLeft /></el-icon></button>
                  <button type="button" class="active">1</button>
                  <button type="button"><el-icon><ArrowRight /></el-icon></button>
                </div>
              </footer>
            </section>
          </template>
        </template>
      </main>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px" class="profile-edit-dialog">
      <template v-if="editTarget === 'password'">
        <el-input v-model="editForm.currentPassword" placeholder="请输入当前密码" show-password />
        <el-input v-model="editForm.newPassword" placeholder="请输入新密码" show-password />
        <el-input v-model="editForm.confirmPassword" placeholder="请再次输入新密码" show-password />
      </template>
      <el-input v-else v-model="editForm.value" :placeholder="editPlaceholder" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitProfileEdit">确定</el-button>
      </template>
    </el-dialog>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import {
  ArrowLeft,
  ArrowRight,
  Bell,
  Check,
  Clock,
  Document,
  EditPen,
  FullScreen,
  Headset,
  Histogram,
  InfoFilled,
  Iphone,
  Lock,
  Monitor,
  OfficeBuilding,
  Postcard,
  Reading,
  Search,
  Tickets,
  User,
  VideoPause,
  VideoPlay
} from '@element-plus/icons-vue';
import {
  fetchStudentArchiveDetail,
  fetchStudentProfile,
  markAllStudentMessagesRead,
  markStudentMessageRead,
  updateStudentIdCard,
  updateStudentPassword,
  updateStudentPhone
} from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';
import {
  calculateWeightedScore,
  mockArchives,
  mockMessages,
  mockScoreParts,
  summarizeUnreadMessages,
  type SemesterScore,
  type ScorePart,
  type StudentMessage,
  type TrainingArchive,
  type TrainingArchiveDetail
} from '../../features/student/profile';

type ProfileTab = 'info' | 'archives' | 'score' | 'messages';
type EditTarget = 'phone' | 'idCard' | 'password';

interface ProfileMenuItem {
  key: ProfileTab;
  label: string;
}

interface ScoreRow {
  index: number;
  term: string;
  courseware: string;
  training: string;
  assignment: string;
  exam: string;
  total: string;
}

interface ArchiveRow {
  id: number;
  index: number;
  title: string;
  mode: string;
  role: string;
  submittedAt: string;
  submitType: string;
  duration: string;
  personalScore: string;
  teamScore: string;
}

interface ArchiveStep {
  index: number;
  name: string;
  expected: string;
  actual: string;
  score: number;
  duration: string;
}

const activeTab = ref<ProfileTab>('info');
const showArchiveDetail = ref(false);
const selectedArchiveDetail = ref<TrainingArchiveDetail>();
const archiveKeyword = ref('');
const editTarget = ref<EditTarget>();
const editForm = ref({
  value: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});
const editLoading = ref(false);
const markAllLoading = ref(false);
const messageReadLoadingIds = ref<number[]>([]);
const dialogTitle = ref('');
const dialogVisible = computed({
  get: () => dialogTitle.value.length > 0,
  set: (value: boolean) => {
    if (!value) {
      dialogTitle.value = '';
    }
  }
});

const student = ref({
  name: '张林林',
  className: '城轨运营2301班',
  studentId: '20240301001',
  phone: '',
  idCard: ''
});
const scoreParts = ref<ScorePart[]>(mockScoreParts);
const semesterScores = ref<SemesterScore[]>([]);
const messages = ref<StudentMessage[]>(mockMessages);
const archives = ref<TrainingArchive[]>(mockArchives);
const weightedScore = computed(() => calculateWeightedScore(scoreParts.value));

const profileTabs: ProfileMenuItem[] = [
  { key: 'info', label: '个人信息' },
  { key: 'archives', label: '实训档案' },
  { key: 'score', label: '综合成绩' },
  { key: 'messages', label: '消息通知' }
];

const currentTitle = computed(() => profileTabs.find((item) => item.key === activeTab.value)?.label ?? '个人信息');

function maskPhone(value?: string): string {
  return value ? value.replace(/^(\d{3})\d{4}(\d+)/, '$1****$2') : '138****6789';
}

function maskIdCard(value?: string): string {
  return value ? value.replace(/^(.{3}).+(.{4})$/, '$1***********$2') : '320***********1234';
}

const profileFields = computed(() => [
  { label: '姓名', value: student.value.name, icon: 'user', editable: false },
  { label: '学号', value: student.value.studentId, icon: 'id', editable: false },
  { label: '所属班级', value: student.value.className, icon: 'class', editable: false },
  { label: '手机号', value: maskPhone(student.value.phone), icon: 'phone', editTarget: 'phone' as const },
  { label: '身份证号', value: maskIdCard(student.value.idCard), icon: 'id', editTarget: 'idCard' as const },
  { label: '密码', value: '••••••••', icon: 'lock', editTarget: 'password' as const }
]);

const editPlaceholder = computed(() => {
  if (editTarget.value === 'phone') {
    return '请输入新手机号';
  }

  if (editTarget.value === 'idCard') {
    return '请输入新身份证号';
  }

  return '请输入新内容';
});

const scoreRows = computed<ScoreRow[]>(() => [
  ...(semesterScores.value.length > 0
    ? semesterScores.value.map((item, index) => ({
      index: index + 1,
      term: item.academicTerm || '-',
      courseware: `${item.coursewareLearningScore}*${item.coursewareWeight}%`,
      training: `${item.trainingPracticeScore}*${item.trainingPracticeWeight}%`,
      assignment: `${item.courseAssignmentScore}*${item.assignmentWeight}%`,
      exam: `${item.examScore}${item.examWeight ? `*${item.examWeight}%` : ''}`,
      total: String(item.comprehensiveScore || weightedScore.value)
    }))
    : [
      {
        index: 1,
        term: '2024-2025学年 上学期',
        courseware: `${scoreParts.value[0]?.score ?? 92.0}*20%`,
        training: '88.5*25%',
        assignment: '90.0*20%',
        exam: '85.0*35%',
        total: String(weightedScore.value || 88.1)
      },
      { index: 2, term: '2023-2024学年 下学期', courseware: '96.0*15%', training: '93.0*30%', assignment: '95.0*25%', exam: '94.0', total: '94.2' },
      { index: 3, term: '2023-2024学年 上学期', courseware: '82.0*15%', training: '78.0*20%', assignment: '80.0*20%', exam: '79.0*35%', total: '79.8' }
    ])
]);

function messageIconClass(message: StudentMessage) {
  const text = `${message.type || ''}${message.title}`;
  if (text.includes('实训')) {
    return 'is-training';
  }

  if (text.includes('资源')) {
    return 'is-resource';
  }

  return 'is-course';
}

const displayMessages = computed(() =>
  messages.value.map((message) => ({
    ...message,
    type: message.type || '消息通知',
    title: message.content || message.title,
    time: message.time || '',
    iconClass: messageIconClass(message)
  }))
);

const unreadCount = computed(() => Math.max(summarizeUnreadMessages(messages.value), displayMessages.value.filter((message) => message.unread).length));

const archiveRows = computed<ArchiveRow[]>(() =>
  archives.value.map((archive, index) => ({
    id: archive.id,
    index: index + 1,
    title: archive.title,
    mode: archive.mode || (index % 2 === 0 ? '多人实训' : '单人实训'),
    role: archive.role || (index % 2 === 0 ? '值班站长' : '-'),
    submittedAt: archive.finishedAt || '-',
    submitType: archive.submitType || '正常提交',
    duration: archive.duration,
    personalScore: String(archive.score || '-'),
    teamScore: archive.teamScore === undefined ? '-' : String(archive.teamScore)
  }))
);

const archiveSteps: ArchiveStep[] = [
  { index: 1, name: '发现伤者并上报', expected: '立即按下紧急停止按钮并通过对讲机上报', actual: '立即按下紧急停止按钮并通过对讲机上报', score: 15, duration: '8.5' },
  { index: 2, name: '设置警戒区域', expected: '使用警戒带隔离事故区域，疏散围观乘客', actual: '使用警戒带隔离事故区域，疏散围观乘客', score: 10, duration: '12.3' },
  { index: 3, name: '初步伤情评估', expected: '检查伤者意识、呼吸、出血情况并记录', actual: '仅检查了伤者意识，未记录出血情况', score: 5, duration: '18.7' },
  { index: 4, name: '通知医疗急救', expected: '拨打120并准确报告位置与伤情', actual: '拨打120并准确报告位置与伤情', score: 15, duration: '22.1' },
  { index: 5, name: '现场应急处置', expected: '进行基础止血包扎，安抚伤者情绪', actual: '进行基础止血包扎，安抚伤者情绪', score: 15, duration: '45.6' },
  { index: 6, name: '填写事故报告', expected: '在系统中完整填写事故报告并提交', actual: '填写了事故报告但遗漏关键信息未提交', score: 5, duration: '35.2' },
  { index: 7, name: '恢复设备运行', expected: '确认安全后解除急停，恢复扶梯运行', actual: '确认安全后解除急停，恢复扶梯运行', score: 15, duration: '20.8' },
  { index: 8, name: '总结汇报', expected: '向值班领导口头汇报处置全过程', actual: '向值班领导口头汇报处置全过程', score: 10, duration: '15.4' }
];

const visibleArchiveSteps = computed<ArchiveStep[]>(() => {
  const detail = selectedArchiveDetail.value;
  if (!detail?.steps.length) {
    return archiveSteps;
  }

  return detail.steps.map((step, index) => ({
    index: index + 1,
    name: step.name,
    expected: step.expected,
    actual: step.actual,
    score: step.score,
    duration: String(step.durationSeconds)
  }));
});

const archiveDetailTitle = computed(() => selectedArchiveDetail.value?.title || '自动扶梯伤客任务演练');
const archiveDetailMode = computed(() => selectedArchiveDetail.value?.mode || '多人实训');
const archiveDetailSubmitType = computed(() => selectedArchiveDetail.value?.submitType || '正常提交');
const archiveDetailSubmittedAt = computed(() => selectedArchiveDetail.value?.finishedAt || '2025-01-15 14:30:22');
const archiveDetailStudentName = computed(() => selectedArchiveDetail.value?.studentName || student.value.name);
const archiveDetailStudentNo = computed(() => selectedArchiveDetail.value?.studentNo || student.value.studentId);
const archiveDetailClassName = computed(() => selectedArchiveDetail.value?.className || student.value.className || '城轨运营 2403 班');
const archiveDetailScore = computed(() => selectedArchiveDetail.value?.score ?? visibleArchiveSteps.value.reduce((sum, step) => sum + step.score, 0));
const archiveDetailDurationSeconds = computed(() =>
  Math.round(visibleArchiveSteps.value.reduce((sum, step) => sum + Number(step.duration || 0), 0))
);
const archiveDetailDurationText = computed(() => {
  const seconds = Math.round(archiveDetailDurationSeconds.value);
  const minutes = Math.floor(seconds / 60);
  const restSeconds = seconds % 60;
  return `${minutes} 分 ${restSeconds} 秒`;
});

function openTab(tab: ProfileTab) {
  activeTab.value = tab;
}

function openEditDialog(target: EditTarget) {
  editTarget.value = target;
  editForm.value = {
    value: '',
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  dialogTitle.value = target === 'phone' ? '修改手机号' : target === 'idCard' ? '修改身份证号' : '修改密码';
}

async function submitProfileEdit() {
  if (!editTarget.value) {
    dialogVisible.value = false;
    return;
  }

  editLoading.value = true;
  try {
    if (editTarget.value === 'phone') {
      await updateStudentPhone(editForm.value.value);
      student.value.phone = editForm.value.value;
    } else if (editTarget.value === 'idCard') {
      await updateStudentIdCard(editForm.value.value);
      student.value.idCard = editForm.value.value;
    } else {
      await updateStudentPassword(editForm.value.currentPassword, editForm.value.newPassword, editForm.value.confirmPassword);
    }

    ElMessage.success('修改成功');
    dialogVisible.value = false;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '修改失败');
  } finally {
    editLoading.value = false;
  }
}

async function handleMarkRead(messageId: number) {
  messageReadLoadingIds.value = [...messageReadLoadingIds.value, messageId];
  try {
    await markStudentMessageRead(messageId);
    messages.value = messages.value.map((message) => (message.id === messageId ? { ...message, unread: false } : message));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '标记已读失败');
  } finally {
    messageReadLoadingIds.value = messageReadLoadingIds.value.filter((id) => id !== messageId);
  }
}

async function handleMarkAllRead() {
  markAllLoading.value = true;
  try {
    await markAllStudentMessagesRead();
    messages.value = messages.value.map((message) => ({ ...message, unread: false }));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '全部标记已读失败');
  } finally {
    markAllLoading.value = false;
  }
}

async function openArchiveDetail(archiveId: number) {
  try {
    selectedArchiveDetail.value = await fetchStudentArchiveDetail(archiveId);
  } catch {
    selectedArchiveDetail.value = undefined;
  } finally {
    showArchiveDetail.value = true;
  }
}

watch(activeTab, () => {
  showArchiveDetail.value = false;
  selectedArchiveDetail.value = undefined;
});

onMounted(async () => {
  try {
    const profile = await fetchStudentProfile();
    student.value = profile.student ? { ...student.value, ...profile.student } : student.value;
    scoreParts.value = profile.scoreParts && profile.scoreParts.length > 0 ? profile.scoreParts : scoreParts.value;
    semesterScores.value = profile.semesterScores && profile.semesterScores.length > 0 ? profile.semesterScores : semesterScores.value;
    messages.value = profile.messages && profile.messages.length > 0 ? profile.messages : messages.value;
    archives.value = profile.archives && profile.archives.length > 0 ? profile.archives : archives.value;
  } catch {
    student.value = student.value;
  }
});
</script>
