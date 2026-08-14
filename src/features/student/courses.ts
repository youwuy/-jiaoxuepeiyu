export type CourseStatus = 'learning' | 'notStarted' | 'completed';
export type CourseItemStatus = 'completed' | 'current' | 'inProgress' | 'pending' | 'locked';
export type CourseItemType = 'courseware' | 'assignment';
export type CourseMode = 'free' | 'sequence';

export interface CourseTimeline {
  id: number;
  name: string;
  startAt: string;
  endAt: string;
  completedItems: number;
  totalItems: number;
}

export interface CourseCatalogItem {
  id: string;
  assignmentId?: number;
  assignmentType?: string;
  trainingId?: number;
  type: CourseItemType;
  title: string;
  status: CourseItemStatus;
  resourceType?: string;
  fileName?: string;
  fileUrl?: string;
  previewUrl?: string;
  durationMinutes?: number;
  learnedSeconds?: number;
  minDurationMinutes?: number;
  score?: number;
  deadline?: string;
  openStart?: string;
  openEnd?: string;
}

export interface CourseChapter {
  id: string;
  title: string;
  status: 'completed' | 'learning' | 'notStarted';
  items: CourseCatalogItem[];
}

export interface StudentCourse extends CourseTimeline {
  term: string;
  resourceCount: number;
  assignmentCount: number;
  teachers: string[];
  mode: CourseMode;
  chapters: CourseChapter[];
}

export interface StudentCourseView extends StudentCourse {
  status: CourseStatus;
  progress: number;
}

const statusRank: Record<CourseStatus, number> = {
  learning: 0,
  notStarted: 1,
  completed: 2
};

export function deriveCourseStatus(course: Pick<CourseTimeline, 'startAt' | 'endAt'>, currentTime = new Date()): CourseStatus {
  const startTime = new Date(`${course.startAt}T00:00:00`).getTime();
  const endTime = new Date(`${course.endAt}T23:59:59`).getTime();
  const now = currentTime.getTime();

  if (now < startTime) {
    return 'notStarted';
  }

  if (now > endTime) {
    return 'completed';
  }

  return 'learning';
}

export function calculateCourseProgress(course: Pick<CourseTimeline, 'completedItems' | 'totalItems'>): number {
  if (course.totalItems <= 0) {
    return 0;
  }

  return Math.floor((course.completedItems / course.totalItems) * 100);
}

export function filterCoursesByKeyword<T extends Pick<StudentCourse, 'name'>>(courses: T[], keyword: string): T[] {
  const normalizedKeyword = keyword.trim().toLowerCase();

  if (!normalizedKeyword) {
    return courses;
  }

  return courses.filter((course) => course.name.toLowerCase().includes(normalizedKeyword));
}

export function sortCoursesForStudent<T extends CourseTimeline>(courses: T[], currentTime = new Date()): T[] {
  return [...courses].sort((left, right) => {
    const leftStatus = deriveCourseStatus(left, currentTime);
    const rightStatus = deriveCourseStatus(right, currentTime);

    if (leftStatus !== rightStatus) {
      return statusRank[leftStatus] - statusRank[rightStatus];
    }

    if (leftStatus === 'completed') {
      const endDiff = new Date(left.endAt).getTime() - new Date(right.endAt).getTime();
      if (endDiff !== 0) {
        return endDiff;
      }
    } else {
      const startDiff = new Date(right.startAt).getTime() - new Date(left.startAt).getTime();
      if (startDiff !== 0) {
        return startDiff;
      }
    }

    return right.id - left.id;
  });
}

export function buildCourseViews(courses: StudentCourse[], currentTime = new Date()): StudentCourseView[] {
  return sortCoursesForStudent(courses, currentTime).map((course) => ({
    ...course,
    status: deriveCourseStatus(course, currentTime),
    progress: calculateCourseProgress(course)
  }));
}

export function formatOpenPeriod(course: Pick<StudentCourse, 'startAt' | 'endAt'>): string {
  return `${course.startAt} 至 ${course.endAt} 开放学习`;
}

export const mockStudentCourses: StudentCourse[] = [];
