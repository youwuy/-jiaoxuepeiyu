export type AdminCoursePublishStatus = 'PUBLISHED' | 'DRAFT' | 'OFFLINE';

export interface AdminCourseContent {
  contentId?: number;
  chapterId?: number;
  itemType?: string;
  title?: string;
  resourceId?: number;
  assignmentId?: number;
  requiredDurationSeconds?: number;
  sortOrder?: number;
}

export interface AdminCourseChapter {
  chapterId?: number;
  courseId?: number;
  parentChapterId?: number;
  chapterTitle?: string;
  sortOrder?: number;
  contents?: AdminCourseContent[];
  children?: AdminCourseChapter[];
}

export interface AdminCourseRecord {
  courseId: number;
  courseName: string;
  academicYearName?: string;
  semesterName?: string;
  academicTerm?: string;
  majorName?: string;
  classNames?: string;
  teacherNames?: string;
  learningMode?: string;
  assignmentCompletionRule?: string;
  coursewareScoreCap?: number;
  coursewareCount?: number;
  assignmentCount?: number;
  publishStatus?: string;
  openStartTime?: string;
  openEndTime?: string;
  createdBy?: number;
  creatorName?: string;
  createdAt?: string;
  updatedAt?: string;
  teacherIds?: number[];
  classIds?: number[];
  chapters?: AdminCourseChapter[];
  pendingReviewCount?: number;
}

export interface AdminCourseView extends AdminCourseRecord {
  id: number;
  title: string;
  termLabel: string;
  periodLabel: string;
  classLabel: string;
  teacherLabel: string;
  statusLabel: string;
  statusTone: 'published' | 'draft';
  createdAtLabel: string;
  updatedAtLabel: string;
  pendingReviewLabel: string;
  chapterCount: number;
  contentCount: number;
}

function formatDateTime(value?: string): string {
  if (!value) {
    return '-';
  }

  const normalized = value.includes('T') ? value.replace('T', ' ') : value;
  return normalized.slice(0, 16);
}

function formatDate(value?: string): string {
  if (!value) {
    return '-';
  }

  return value.slice(0, 10);
}

function splitDisplayText(value?: string): string {
  if (!value) {
    return '-';
  }

  return value
    .split(/[，,、\n]/)
    .map((item) => item.trim())
    .filter(Boolean)
    .join('\n');
}

function countContents(chapters?: AdminCourseChapter[]): number {
  return (chapters ?? []).reduce(
    (total, chapter) => total + (chapter.contents?.length ?? 0) + countContents(chapter.children),
    0
  );
}

export function mapAdminCourseView(course: AdminCourseRecord): AdminCourseView {
  const isPublished = (course.publishStatus ?? '').toUpperCase() === 'PUBLISHED';
  const chapterCount = course.chapters?.length ?? 0;
  const contentCount = countContents(course.chapters);

  return {
    ...course,
    id: course.courseId,
    title: course.courseName,
    termLabel: course.academicTerm || [course.academicYearName, course.semesterName].filter(Boolean).join(' ') || '-',
    periodLabel:
      course.openStartTime || course.openEndTime
        ? `${formatDate(course.openStartTime)} 至 ${formatDate(course.openEndTime)}`
        : '-',
    classLabel: splitDisplayText(course.classNames),
    teacherLabel: splitDisplayText(course.teacherNames),
    statusLabel: isPublished ? '已发布' : '未发布',
    statusTone: isPublished ? 'published' : 'draft',
    createdAtLabel: formatDateTime(course.createdAt),
    updatedAtLabel: formatDateTime(course.updatedAt),
    pendingReviewLabel: String(course.pendingReviewCount ?? course.assignmentCount ?? 0),
    chapterCount,
    contentCount
  };
}

export function buildAdminCourseViews(records: AdminCourseRecord[]): AdminCourseView[] {
  return records.map(mapAdminCourseView);
}

export function formatCourseContentTitle(content: AdminCourseContent): string {
  return content.title || '未命名内容';
}

export const mockAdminCourses: AdminCourseRecord[] = [
  {
    courseId: 1,
    courseName: '城市轨道交通信号系统原理',
    academicYearName: '2025-2026学年',
    semesterName: '下学期',
    academicTerm: '2025-2026学年下学期',
    majorName: '城轨信号',
    classNames: '城轨信号2401班\n城轨信号2402班\n城轨信号2501班',
    teacherNames: '李明、王建国',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 80,
    coursewareCount: 6,
    assignmentCount: 10,
    pendingReviewCount: 10,
    publishStatus: 'PUBLISHED',
    openStartTime: '2026-03-12T00:00:00',
    openEndTime: '2026-07-19T23:59:59',
    createdAt: '2026-02-15T10:34:00',
    chapters: [
      {
        chapterId: 101,
        chapterTitle: '信号系统概述',
        contents: [{ contentId: 1001, itemType: 'COURSEWARE', title: '信号基础知识' }]
      },
      {
        chapterId: 102,
        chapterTitle: '联锁与闭塞',
        contents: [
          { contentId: 1002, itemType: 'COURSEWARE', title: '联锁原理' },
          { contentId: 1003, itemType: 'ASSIGNMENT', title: '章节作业 1' }
        ]
      }
    ]
  },
  {
    courseId: 2,
    courseName: '地铁列车驾驶技术基础',
    academicYearName: '2025-2026学年',
    semesterName: '上学期',
    academicTerm: '2025-2026学年上学期',
    majorName: '城轨车辆',
    classNames: '城轨车辆2401班\n城轨车辆2402班',
    teacherNames: '赵志强',
    learningMode: 'TEACHER_LED',
    assignmentCompletionRule: 'PASS_SCORE',
    coursewareScoreCap: 100,
    coursewareCount: 4,
    assignmentCount: 0,
    pendingReviewCount: 0,
    publishStatus: 'DRAFT',
    openStartTime: '2025-09-01T00:00:00',
    openEndTime: '2026-01-15T23:59:59',
    createdAt: '2025-03-10T14:00:00',
    chapters: [{ chapterId: 201, chapterTitle: '驾驶室基础', contents: [{ contentId: 2001, itemType: 'COURSEWARE', title: '驾驶面板识别' }] }]
  },
  {
    courseId: 3,
    courseName: '站务管理实务',
    academicYearName: '2024-2025学年',
    semesterName: '下学期',
    academicTerm: '2024-2025学年下学期',
    majorName: '城轨运营',
    classNames: '城轨运营2401班\n城轨运营2402班\n城轨运营2403班\n城轨运营2404班',
    teacherNames: '陈伟、李芳',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 90,
    coursewareCount: 5,
    assignmentCount: 0,
    pendingReviewCount: 0,
    publishStatus: 'DRAFT',
    openStartTime: '2025-03-01T00:00:00',
    openEndTime: '2025-07-15T23:59:59',
    createdAt: '2025-02-20T09:00:00',
    chapters: [{ chapterId: 301, chapterTitle: '站务组织', contents: [{ contentId: 3001, itemType: 'COURSEWARE', title: '岗位职责' }] }]
  },
  {
    courseId: 4,
    courseName: '客运服务礼仪',
    academicYearName: '2024-2025学年',
    semesterName: '下学期',
    academicTerm: '2024-2025学年下学期',
    majorName: '城轨运营',
    classNames: '城轨运营2401班\n城轨运营2402班',
    teacherNames: '陈伟',
    learningMode: 'TEACHER_LED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 80,
    coursewareCount: 8,
    assignmentCount: 1,
    pendingReviewCount: 0,
    publishStatus: 'PUBLISHED',
    openStartTime: '2025-03-01T00:00:00',
    openEndTime: '2025-07-15T23:59:59',
    createdAt: '2025-02-18T11:00:00',
    chapters: [{ chapterId: 401, chapterTitle: '礼仪规范', contents: [{ contentId: 4001, itemType: 'COURSEWARE', title: '站姿与问候' }] }]
  },
  {
    courseId: 5,
    courseName: '轨道交通安全管理',
    academicYearName: '2025-2026学年',
    semesterName: '上学期',
    academicTerm: '2025-2026学年上学期',
    majorName: '城轨运营',
    classNames: '城轨运营2401班\n城轨运营2402班\n城轨运营2403班',
    teacherNames: '王建国',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'PASS_SCORE',
    coursewareScoreCap: 70,
    coursewareCount: 7,
    assignmentCount: 0,
    pendingReviewCount: 0,
    publishStatus: 'DRAFT',
    openStartTime: '2025-09-01T00:00:00',
    openEndTime: '2026-01-15T23:59:59',
    createdAt: '2025-01-15T16:30:00',
    chapters: [{ chapterId: 501, chapterTitle: '安全制度', contents: [{ contentId: 5001, itemType: 'COURSEWARE', title: '安全生产法基础' }] }]
  },
  {
    courseId: 6,
    courseName: '接发车作业组织',
    academicYearName: '2025-2026学年',
    semesterName: '下学期',
    academicTerm: '2025-2026学年下学期',
    majorName: '城轨运营',
    classNames: '城轨运营2301班\n城轨运营2302班',
    teacherNames: '李芳',
    learningMode: 'TEACHER_LED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 75,
    coursewareCount: 4,
    assignmentCount: 2,
    pendingReviewCount: 1,
    publishStatus: 'PUBLISHED',
    openStartTime: '2026-02-20T00:00:00',
    openEndTime: '2026-06-30T23:59:59',
    createdAt: '2026-02-10T08:20:00'
  },
  {
    courseId: 7,
    courseName: '车辆检修工艺',
    academicYearName: '2025-2026学年',
    semesterName: '下学期',
    academicTerm: '2025-2026学年下学期',
    majorName: '城轨车辆',
    classNames: '城轨车辆2301班\n城轨车辆2302班',
    teacherNames: '赵志强、孙敏',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'PASS_SCORE',
    coursewareScoreCap: 85,
    coursewareCount: 9,
    assignmentCount: 3,
    pendingReviewCount: 2,
    publishStatus: 'PUBLISHED',
    openStartTime: '2026-03-01T00:00:00',
    openEndTime: '2026-07-01T23:59:59',
    createdAt: '2026-02-16T09:10:00'
  },
  {
    courseId: 8,
    courseName: '车站客流控制',
    academicYearName: '2024-2025学年',
    semesterName: '上学期',
    academicTerm: '2024-2025学年上学期',
    majorName: '城轨运营',
    classNames: '城轨运营2303班\n城轨运营2304班',
    teacherNames: '王建国、李芳',
    learningMode: 'TEACHER_LED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 80,
    coursewareCount: 5,
    assignmentCount: 1,
    pendingReviewCount: 0,
    publishStatus: 'DRAFT',
    openStartTime: '2024-09-01T00:00:00',
    openEndTime: '2025-01-15T23:59:59',
    createdAt: '2024-08-27T10:00:00'
  },
  {
    courseId: 9,
    courseName: '轨道交通法律法规',
    academicYearName: '2024-2025学年',
    semesterName: '下学期',
    academicTerm: '2024-2025学年下学期',
    majorName: '综合',
    classNames: '综合管理2401班',
    teacherNames: '陈伟',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 60,
    coursewareCount: 3,
    assignmentCount: 0,
    pendingReviewCount: 0,
    publishStatus: 'PUBLISHED',
    openStartTime: '2025-03-05T00:00:00',
    openEndTime: '2025-06-20T23:59:59',
    createdAt: '2025-03-01T14:08:00'
  },
  {
    courseId: 10,
    courseName: '线路巡检与养护',
    academicYearName: '2025-2026学年',
    semesterName: '上学期',
    academicTerm: '2025-2026学年上学期',
    majorName: '城轨工务',
    classNames: '城轨工务2401班\n城轨工务2402班',
    teacherNames: '李明',
    learningMode: 'TEACHER_LED',
    assignmentCompletionRule: 'PASS_SCORE',
    coursewareScoreCap: 80,
    coursewareCount: 8,
    assignmentCount: 2,
    pendingReviewCount: 2,
    publishStatus: 'DRAFT',
    openStartTime: '2025-09-15T00:00:00',
    openEndTime: '2026-01-20T23:59:59',
    createdAt: '2025-09-01T09:30:00'
  },
  {
    courseId: 11,
    courseName: '站台应急处置',
    academicYearName: '2025-2026学年',
    semesterName: '下学期',
    academicTerm: '2025-2026学年下学期',
    majorName: '城轨运营',
    classNames: '城轨运营2401班\n城轨运营2402班',
    teacherNames: '王建国',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 90,
    coursewareCount: 6,
    assignmentCount: 4,
    pendingReviewCount: 3,
    publishStatus: 'PUBLISHED',
    openStartTime: '2026-03-02T00:00:00',
    openEndTime: '2026-07-10T23:59:59',
    createdAt: '2026-02-21T08:35:00'
  },
  {
    courseId: 12,
    courseName: '信号设备维护',
    academicYearName: '2025-2026学年',
    semesterName: '上学期',
    academicTerm: '2025-2026学年上学期',
    majorName: '城轨信号',
    classNames: '城轨信号2301班\n城轨信号2302班',
    teacherNames: '赵志强',
    learningMode: 'TEACHER_LED',
    assignmentCompletionRule: 'PASS_SCORE',
    coursewareScoreCap: 85,
    coursewareCount: 7,
    assignmentCount: 2,
    pendingReviewCount: 1,
    publishStatus: 'DRAFT',
    openStartTime: '2025-09-01T00:00:00',
    openEndTime: '2026-01-10T23:59:59',
    createdAt: '2025-08-28T17:22:00'
  },
  {
    courseId: 13,
    courseName: '乘务服务英语',
    academicYearName: '2024-2025学年',
    semesterName: '上学期',
    academicTerm: '2024-2025学年上学期',
    majorName: '城轨运营',
    classNames: '城轨运营2305班',
    teacherNames: '李芳',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 70,
    coursewareCount: 4,
    assignmentCount: 1,
    pendingReviewCount: 0,
    publishStatus: 'PUBLISHED',
    openStartTime: '2024-09-05T00:00:00',
    openEndTime: '2025-01-12T23:59:59',
    createdAt: '2024-08-30T09:00:00'
  },
  {
    courseId: 14,
    courseName: '铁路客运组织',
    academicYearName: '2024-2025学年',
    semesterName: '下学期',
    academicTerm: '2024-2025学年下学期',
    majorName: '城轨运营',
    classNames: '城轨运营2401班\n城轨运营2402班',
    teacherNames: '陈伟、王建国',
    learningMode: 'TEACHER_LED',
    assignmentCompletionRule: 'SUBMIT',
    coursewareScoreCap: 80,
    coursewareCount: 6,
    assignmentCount: 2,
    pendingReviewCount: 1,
    publishStatus: 'DRAFT',
    openStartTime: '2025-03-01T00:00:00',
    openEndTime: '2025-07-01T23:59:59',
    createdAt: '2025-02-25T11:15:00'
  },
  {
    courseId: 15,
    courseName: '综合实训课程设计',
    academicYearName: '2025-2026学年',
    semesterName: '下学期',
    academicTerm: '2025-2026学年下学期',
    majorName: '综合',
    classNames: '综合管理2401班\n综合管理2402班',
    teacherNames: '李明、李芳',
    learningMode: 'SELF_PACED',
    assignmentCompletionRule: 'PASS_SCORE',
    coursewareScoreCap: 95,
    coursewareCount: 10,
    assignmentCount: 5,
    pendingReviewCount: 4,
    publishStatus: 'PUBLISHED',
    openStartTime: '2026-03-01T00:00:00',
    openEndTime: '2026-07-31T23:59:59',
    createdAt: '2026-03-01T09:00:00'
  }
];
