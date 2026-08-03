import { requestJson } from './http';
import type { CourseCatalogItem, CourseChapter, CourseItemStatus, CourseItemType, StudentCourse } from '../features/student/courses';
import type { ScorePart, SemesterScore, StudentMessage, TrainingArchive, TrainingArchiveDetail, TrainingArchiveStep } from '../features/student/profile';
import { coverForResourceType, type StudentResource } from '../features/student/resources';
import type { StudentTraining, TrainingMode, TrainingStatus } from '../features/student/training';

interface PageResult<T> {
  records?: T[];
  rows?: T[];
  list?: T[];
  data?: T[];
}

export interface StudentProfileResult {
  student?: {
    name: string;
    className: string;
    studentId: string;
    phone?: string;
    idCard?: string;
  };
  scoreParts?: ScorePart[];
  semesterScores?: SemesterScore[];
  messages?: StudentMessage[];
  archives?: TrainingArchive[];
}

interface BackendCourseCard {
  courseId: number;
  courseName: string;
  status?: string;
  academicTerm?: string;
  progressPercent?: number;
  coursewareCount?: number;
  assignmentCount?: number;
  teacherNames?: string;
  openStartTime?: string;
  openEndTime?: string;
}

interface BackendCourseDetail extends BackendCourseCard {
  lastContentId?: number;
  chapters?: BackendCourseChapter[];
}

interface BackendCourseChapter {
  chapterId: number;
  chapterTitle: string;
  items?: BackendCourseItem[];
}

interface BackendCourseItem {
  contentId: number;
  itemType?: string;
  title: string;
  resourceId?: number;
  assignmentId?: number;
  requiredDurationSeconds?: number;
  studiedSeconds?: number;
  completed?: boolean;
}

interface BackendTraining {
  trainingId: number;
  trainingName: string;
  trainingMode?: string;
  status?: string;
  openStartTime?: string;
  openEndTime?: string;
  teamSize?: number;
  roleCount?: number;
  appRequired?: boolean;
  appInstalled?: boolean;
  activeRoomId?: number;
  latestAttemptId?: number;
}

export interface TrainingAppInstallation {
  installed?: boolean;
  version?: string;
  downloadUrl?: string;
  message?: string;
}

export interface TrainingRoom {
  roomId: number;
  trainingId: number;
  trainingName?: string;
  roomCode?: string;
  roomStatus?: string;
  teamSize?: number;
}

interface BackendResource {
  resourceId: number;
  resourceName: string;
  resourceType?: string;
  coverUrl?: string;
  previewUrl?: string;
  fileUrl?: string;
  fileSize?: number;
  majorName?: string;
  uploaderName?: string;
  updatedAt?: string;
}

interface BackendProfile {
  studentId?: number;
  studentNo?: string;
  realName?: string;
  phone?: string;
  idCard?: string;
  className?: string;
}

interface BackendSemesterScore {
  academicTerm?: string;
  coursewareLearningScore?: number;
  trainingPracticeScore?: number;
  courseAssignmentScore?: number;
  examScore?: number;
  coursewareWeight?: number;
  trainingPracticeWeight?: number;
  assignmentWeight?: number;
  examWeight?: number;
  comprehensiveScore?: number;
}

interface BackendMessageSummary {
  unreadCount?: number;
  messages?: BackendMessage[];
}

interface BackendMessage {
  id: number;
  messageType?: string;
  title: string;
  content?: string;
  read?: boolean;
  createdAt?: string;
}

interface BackendArchive {
  archiveId: number;
  trainingName: string;
  trainingMode?: string;
  roleName?: string;
  durationSeconds?: number;
  submittedAt?: string;
  submitType?: string;
  personalScore?: number;
  teamScore?: number;
}

interface BackendArchiveStep {
  stepId: number;
  stepName: string;
  standardOperation?: string;
  actualOperation?: string;
  score?: number;
  durationSeconds?: number;
  videoStartSecond?: number;
}

interface BackendArchiveDetail extends BackendArchive {
  studentName?: string;
  studentNo?: string;
  className?: string;
  recordingUrl?: string;
  steps?: BackendArchiveStep[];
}

function buildQuery(params: Record<string, string | number | boolean | undefined>): string {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

function normalizeList<T>(value: T[] | PageResult<T>): T[] {
  if (Array.isArray(value)) {
    return value;
  }

  return value.records || value.rows || value.list || value.data || [];
}

function formatDate(value?: string): string {
  return value?.slice(0, 10) || '';
}

function formatDateTime(value?: string): string {
  return value ? value.replace('T', ' ').slice(0, 16) : '';
}

function formatFileSize(bytes?: number): string {
  if (!bytes || bytes <= 0) {
    return '-';
  }

  if (bytes < 1024 * 1024) {
    return `${Math.round(bytes / 1024)} KB`;
  }

  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function formatDuration(seconds?: number): string {
  const totalSeconds = Math.max(seconds ?? 0, 0);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const restSeconds = totalSeconds % 60;
  return [hours, minutes, restSeconds].map((item) => String(item).padStart(2, '0')).join(':');
}

function splitTeacherNames(value?: string): string[] {
  return value?.split(/[、,，]/).map((item) => item.trim()).filter(Boolean) ?? [];
}

function mapItemType(type?: string): CourseItemType {
  return type?.toLowerCase().includes('assignment') || type === '作业' ? 'assignment' : 'courseware';
}

function mapItemStatus(item: BackendCourseItem, lastContentId?: number): CourseItemStatus {
  if (item.completed) {
    return 'completed';
  }

  if (lastContentId && item.contentId === lastContentId) {
    return 'current';
  }

  if ((item.studiedSeconds ?? 0) > 0) {
    return 'inProgress';
  }

  return 'pending';
}

function mapChapterStatus(items: CourseCatalogItem[]): CourseChapter['status'] {
  if (items.length > 0 && items.every((item) => item.status === 'completed')) {
    return 'completed';
  }

  if (items.some((item) => ['current', 'inProgress', 'completed'].includes(item.status))) {
    return 'learning';
  }

  return 'notStarted';
}

function mapCourse(card: BackendCourseCard | BackendCourseDetail): StudentCourse {
  const detail = card as BackendCourseDetail;
  const chapters: CourseChapter[] = (detail.chapters ?? []).map((chapter) => {
    const items: CourseCatalogItem[] = (chapter.items ?? []).map((item) => {
      const type = mapItemType(item.itemType);
      return {
        id: String(item.contentId),
        type,
        title: item.title,
        status: mapItemStatus(item, detail.lastContentId),
        resourceType: type === 'courseware' ? '课件' : undefined,
        durationMinutes: Math.ceil((item.studiedSeconds ?? 0) / 60),
        learnedSeconds: item.studiedSeconds ?? 0,
        minDurationMinutes: Math.ceil((item.requiredDurationSeconds ?? 0) / 60),
        openStart: formatDate(card.openStartTime),
        openEnd: formatDate(card.openEndTime)
      };
    });

    return {
      id: String(chapter.chapterId),
      title: chapter.chapterTitle,
      status: mapChapterStatus(items),
      items
    };
  });

  const progressPercent = card.progressPercent ?? 0;
  return {
    id: card.courseId,
    name: card.courseName,
    term: card.academicTerm || '',
    startAt: formatDate(card.openStartTime),
    endAt: formatDate(card.openEndTime),
    completedItems: progressPercent,
    totalItems: 100,
    resourceCount: card.coursewareCount ?? chapters.flatMap((chapter) => chapter.items).filter((item) => item.type === 'courseware').length,
    assignmentCount: card.assignmentCount ?? chapters.flatMap((chapter) => chapter.items).filter((item) => item.type === 'assignment').length,
    teachers: splitTeacherNames(card.teacherNames),
    mode: 'sequence',
    chapters
  };
}

function mapTraining(item: BackendTraining): StudentTraining {
  const mode: TrainingMode = item.trainingMode?.toLowerCase().includes('team') ? 'team' : 'single';
  const normalizedStatus = item.status?.toLowerCase() ?? '';
  const status: TrainingStatus = normalizedStatus.includes('complete') || normalizedStatus.includes('finished')
    ? 'completed'
    : normalizedStatus.includes('not') || normalizedStatus.includes('pending')
      ? 'notStarted'
      : 'available';

  return {
    id: item.trainingId,
    title: item.trainingName,
    mode,
    status,
    category: 'practice',
    term: '',
    startAt: formatDate(item.openStartTime),
    deadline: formatDate(item.openEndTime),
    topicCount: Math.max(item.roleCount ?? 1, 1),
    countdown: status === 'notStarted' ? `${formatDate(item.openEndTime)} 开放` : undefined,
    attempts: item.appInstalled ? 1 : 0,
    activeRoomId: item.activeRoomId,
    latestAttemptId: item.latestAttemptId,
    roles: mode === 'team' ? [`${item.roleCount ?? 0} 个角色`, `${item.teamSize ?? 0} 人协作`] : undefined,
    steps: [
      {
        id: item.trainingId,
        title: item.trainingName,
        mode,
        action: status === 'completed' ? 'score' : mode === 'team' ? 'team' : 'start'
      }
    ]
  };
}

export async function fetchStudentTrainingScoreSheet(attemptId: number) {
  return requestJson(`/student/training-attempts/${attemptId}/score-sheet`, {
    fallbackLabel: '实训成绩单'
  });
}

function mapResource(item: BackendResource): StudentResource {
  const type = item.resourceType || '资源';
  return {
    id: item.resourceId,
    title: item.resourceName,
    category: item.majorName || '城市轨道交通通信信号技术',
    type,
    courseName: item.majorName || item.uploaderName,
    author: item.uploaderName,
    coverUrl: item.coverUrl || coverForResourceType(type),
    previewUrl: item.previewUrl,
    fileUrl: item.fileUrl,
    updatedAt: formatDate(item.updatedAt),
    size: formatFileSize(item.fileSize)
  };
}

function mapSemesterScore(score: BackendSemesterScore): SemesterScore {
  return {
    academicTerm: score.academicTerm || '',
    coursewareLearningScore: Number(score.coursewareLearningScore ?? 0),
    trainingPracticeScore: Number(score.trainingPracticeScore ?? 0),
    courseAssignmentScore: Number(score.courseAssignmentScore ?? 0),
    examScore: Number(score.examScore ?? 0),
    coursewareWeight: score.coursewareWeight ?? 0,
    trainingPracticeWeight: score.trainingPracticeWeight ?? 0,
    assignmentWeight: score.assignmentWeight ?? 0,
    examWeight: score.examWeight ?? 0,
    comprehensiveScore: Number(score.comprehensiveScore ?? 0)
  };
}

function mapScoreParts(scores: SemesterScore[]): ScorePart[] {
  const latest = scores[0];
  if (!latest) {
    return [];
  }

  return [
    { label: '课件完成度', score: latest.coursewareLearningScore, weight: latest.coursewareWeight / 100 },
    { label: '课程作业', score: latest.courseAssignmentScore, weight: latest.assignmentWeight / 100 },
    { label: '实训练习', score: latest.trainingPracticeScore, weight: latest.trainingPracticeWeight / 100 },
    { label: '考试', score: latest.examScore, weight: latest.examWeight / 100 }
  ];
}

function mapMessage(message: BackendMessage): StudentMessage {
  return {
    id: message.id,
    title: message.content || message.title,
    unread: !message.read,
    type: message.messageType || message.title,
    content: message.content,
    time: formatDateTime(message.createdAt)
  };
}

function mapArchive(archive: BackendArchive): TrainingArchive {
  return {
    id: archive.archiveId,
    title: archive.trainingName,
    score: Number(archive.personalScore ?? archive.teamScore ?? 0),
    duration: formatDuration(archive.durationSeconds),
    finishedAt: formatDateTime(archive.submittedAt),
    mode: archive.trainingMode,
    role: archive.roleName,
    submitType: archive.submitType,
    teamScore: archive.teamScore === undefined ? undefined : Number(archive.teamScore)
  };
}

function mapArchiveStep(step: BackendArchiveStep): TrainingArchiveStep {
  return {
    id: step.stepId,
    name: step.stepName,
    expected: step.standardOperation || '',
    actual: step.actualOperation || '',
    score: Number(step.score ?? 0),
    durationSeconds: step.durationSeconds ?? 0,
    videoStartSecond: step.videoStartSecond
  };
}

function mapArchiveDetail(detail: BackendArchiveDetail): TrainingArchiveDetail {
  return {
    ...mapArchive(detail),
    studentName: detail.studentName,
    studentNo: detail.studentNo,
    className: detail.className,
    recordingUrl: detail.recordingUrl,
    steps: (detail.steps ?? []).map(mapArchiveStep)
  };
}

export async function fetchStudentCourses(keyword = ''): Promise<StudentCourse[]> {
  const result = await requestJson<BackendCourseCard[] | PageResult<BackendCourseCard>>(`/student/courses${buildQuery({ keyword })}`);

  return normalizeList(result).map(mapCourse);
}

export async function fetchStudentCourse(courseId: number): Promise<StudentCourse> {
  const result = await requestJson<BackendCourseDetail>(`/student/courses/${courseId}`);
  return mapCourse(result);
}

export async function updateCoursewareProgress(
  courseId: number,
  contentId: number,
  studiedSeconds: number,
  completed: boolean
): Promise<void> {
  await requestJson<void>(`/student/courses/${courseId}/progress`, {
    method: 'POST',
    body: JSON.stringify({
      contentId,
      studiedSeconds,
      completed
    }),
    fallbackLabel: '学习进度'
  });
}

export async function fetchStudentTrainings(filters: { mode?: string; keyword?: string } = {}): Promise<StudentTraining[]> {
  const result = await requestJson<BackendTraining[] | PageResult<BackendTraining>>(`/student/trainings${buildQuery(filters)}`);

  return normalizeList(result).map(mapTraining);
}

export async function fetchTrainingAppInstallation(): Promise<TrainingAppInstallation> {
  return requestJson<TrainingAppInstallation>('/student/trainings/app-installation', {
    fallbackLabel: '实训应用安装状态'
  });
}

export async function createTrainingRoom(trainingId: number): Promise<TrainingRoom> {
  return requestJson<TrainingRoom>(`/student/trainings/${trainingId}/rooms`, {
    method: 'POST',
    fallbackLabel: '创建实训房间'
  });
}

export async function fetchTrainingRoom(roomId: number): Promise<TrainingRoom> {
  return requestJson<TrainingRoom>(`/student/training-rooms/${roomId}`, {
    fallbackLabel: '实训房间'
  });
}

export async function startTrainingRoom(roomId: number): Promise<TrainingRoom> {
  return requestJson<TrainingRoom>(`/student/training-rooms/${roomId}/start`, {
    method: 'POST',
    fallbackLabel: '开始组队实训'
  });
}

export async function fetchStudentResources(filters: { keyword?: string; resourceType?: string; majorId?: number } = {}): Promise<StudentResource[]> {
  const result = await requestJson<BackendResource[] | PageResult<BackendResource>>(`/student/resources/public${buildQuery(filters)}`);

  return normalizeList(result).map(mapResource);
}

export async function fetchStudentArchiveDetail(archiveId: number): Promise<TrainingArchiveDetail> {
  const result = await requestJson<BackendArchiveDetail>(`/student/archives/${archiveId}`, {
    fallbackLabel: '实训档案详情'
  });

  return mapArchiveDetail(result);
}

export async function markStudentMessageRead(messageId: number): Promise<void> {
  await requestJson<void>(`/student/messages/${messageId}/read`, {
    method: 'POST',
    fallbackLabel: '标记消息已读'
  });
}

export async function markAllStudentMessagesRead(): Promise<void> {
  await requestJson<void>('/student/messages/read-all', {
    method: 'POST',
    fallbackLabel: '全部标记已读'
  });
}

export async function updateStudentPhone(phone: string): Promise<void> {
  await requestJson<void>('/student/profile/phone', {
    method: 'PUT',
    body: JSON.stringify({ phone }),
    fallbackLabel: '修改手机号'
  });
}

export async function updateStudentIdCard(idCard: string): Promise<void> {
  await requestJson<void>('/student/profile/id-card', {
    method: 'PUT',
    body: JSON.stringify({ idCard }),
    fallbackLabel: '修改身份证号'
  });
}

export async function updateStudentPassword(currentPassword: string, newPassword: string, confirmPassword: string): Promise<void> {
  await requestJson<void>('/student/profile/password', {
    method: 'PUT',
    body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
    fallbackLabel: '修改密码'
  });
}

export async function fetchStudentProfile(): Promise<StudentProfileResult> {
  const profile = await requestJson<BackendProfile>('/student/profile', { fallbackLabel: '个人资料' });
  const [scores, messages, archives] = await Promise.all([
    optionalStudentRequest<BackendSemesterScore[]>('/student/scores/semester', []),
    optionalStudentRequest<BackendMessageSummary>('/student/messages', { messages: [] }),
    optionalStudentRequest<BackendArchive[]>('/student/archives', [])
  ]);

  return {
    student: {
      name: profile.realName || '学员',
      className: profile.className || '',
      studentId: profile.studentNo || String(profile.studentId ?? ''),
      phone: profile.phone,
      idCard: profile.idCard
    },
    semesterScores: scores.map(mapSemesterScore),
    scoreParts: mapScoreParts(scores.map(mapSemesterScore)),
    messages: (messages.messages ?? []).map(mapMessage),
    archives: archives.map(mapArchive)
  };
}

async function optionalStudentRequest<T>(path: string, fallback: T): Promise<T> {
  try {
    return await requestJson<T>(path);
  } catch {
    return fallback;
  }
}
