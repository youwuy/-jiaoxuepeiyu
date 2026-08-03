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

export const mockAdminCourses: AdminCourseRecord[] = [];
