import { requestJson } from './http';
import type { CourseCatalogItem, CourseChapter, CourseItemStatus, CourseItemType, StudentCourse } from '../features/student/courses';
import type { ScorePart, StudentMessage, TrainingArchive } from '../features/student/profile';
import type { StudentResource } from '../features/student/resources';
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
  };
  scoreParts?: ScorePart[];
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
  openEndTime?: string;
  teamSize?: number;
  roleCount?: number;
  appRequired?: boolean;
  appInstalled?: boolean;
}

interface BackendResource {
  resourceId: number;
  resourceName: string;
  resourceType?: string;
  fileSize?: number;
  majorName?: string;
  uploaderName?: string;
  updatedAt?: string;
}

interface BackendProfile {
  studentId?: number;
  studentNo?: string;
  realName?: string;
  className?: string;
}

interface BackendSemesterScore {
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
  read?: boolean;
  createdAt?: string;
}

interface BackendArchive {
  archiveId: number;
  trainingName: string;
  durationSeconds?: number;
  submittedAt?: string;
  personalScore?: number;
  teamScore?: number;
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
    term: '2024-2025学年 下学期',
    deadline: formatDate(item.openEndTime),
    topicCount: Math.max(item.roleCount ?? 1, 1),
    countdown: status === 'notStarted' ? `${formatDate(item.openEndTime)} 开放` : undefined,
    attempts: item.appInstalled ? 1 : 0,
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

function mapResource(item: BackendResource): StudentResource {
  return {
    id: item.resourceId,
    title: item.resourceName,
    category: '公开资料',
    type: item.resourceType || '资源',
    courseName: item.majorName || item.uploaderName,
    updatedAt: formatDate(item.updatedAt),
    size: formatFileSize(item.fileSize)
  };
}

function mapScoreParts(scores: BackendSemesterScore[]): ScorePart[] {
  const latest = scores[0];
  if (!latest) {
    return [];
  }

  return [
    { label: '课件完成度', score: Number(latest.coursewareLearningScore ?? 0), weight: (latest.coursewareWeight ?? 0) / 100 },
    { label: '课程作业', score: Number(latest.courseAssignmentScore ?? 0), weight: (latest.assignmentWeight ?? 0) / 100 },
    { label: '实训练习', score: Number(latest.trainingPracticeScore ?? 0), weight: (latest.trainingPracticeWeight ?? 0) / 100 },
    { label: '考试', score: Number(latest.examScore ?? 0), weight: (latest.examWeight ?? 0) / 100 }
  ];
}

export async function fetchStudentCourses(): Promise<StudentCourse[]> {
  const result = await requestJson<BackendCourseCard[] | PageResult<BackendCourseCard>>('/student/courses');

  return normalizeList(result).map(mapCourse);
}

export async function fetchStudentCourse(courseId: number): Promise<StudentCourse> {
  const result = await requestJson<BackendCourseDetail>(`/student/courses/${courseId}`);
  return mapCourse(result);
}

export async function fetchStudentTrainings(): Promise<StudentTraining[]> {
  const result = await requestJson<BackendTraining[] | PageResult<BackendTraining>>('/student/trainings');

  return normalizeList(result).map(mapTraining);
}

export async function fetchStudentResources(): Promise<StudentResource[]> {
  const result = await requestJson<BackendResource[] | PageResult<BackendResource>>('/student/resources/public');

  return normalizeList(result).map(mapResource);
}

export async function fetchStudentProfile(): Promise<StudentProfileResult> {
  const [profile, scores, messages, archives] = await Promise.all([
    requestJson<BackendProfile>('/student/profile', { fallbackLabel: '个人资料' }),
    requestJson<BackendSemesterScore[]>('/student/scores/semester', { fallbackLabel: '成绩统计' }),
    requestJson<BackendMessageSummary>('/student/messages', { fallbackLabel: '消息通知' }),
    requestJson<BackendArchive[]>('/student/archives', { fallbackLabel: '实训档案' })
  ]);

  return {
    student: {
      name: profile.realName || '学员',
      className: profile.className || '',
      studentId: profile.studentNo || String(profile.studentId ?? '')
    },
    scoreParts: mapScoreParts(scores),
    messages: (messages.messages ?? []).map((message) => ({
      id: message.id,
      title: message.title,
      unread: !message.read,
      type: message.messageType,
      time: formatDateTime(message.createdAt)
    })),
    archives: archives.map((archive) => ({
      id: archive.archiveId,
      title: archive.trainingName,
      score: Number(archive.personalScore ?? archive.teamScore ?? 0),
      duration: formatDuration(archive.durationSeconds),
      finishedAt: formatDateTime(archive.submittedAt)
    }))
  };
}
