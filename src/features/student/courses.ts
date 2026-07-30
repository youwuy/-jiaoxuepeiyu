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
  type: CourseItemType;
  title: string;
  status: CourseItemStatus;
  durationMinutes?: number;
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

export const mockStudentCourses: StudentCourse[] = [
  {
    id: 2026073003,
    name: '城市轨道交通概论',
    term: '2025-2026 学年下学期',
    startAt: '2026-07-01',
    endAt: '2026-08-31',
    completedItems: 7,
    totalItems: 9,
    resourceCount: 6,
    assignmentCount: 3,
    teachers: ['赵老师', '钱老师'],
    mode: 'sequence',
    chapters: [
      {
        id: 'chapter-1',
        title: '第一章 城轨系统认知',
        status: 'completed',
        items: [
          {
            id: 'ware-1-1',
            type: 'courseware',
            title: '1.1 城市轨道交通发展',
            status: 'completed',
            durationMinutes: 18,
            minDurationMinutes: 15,
            openStart: '2026-07-01',
            openEnd: '2026-08-31'
          },
          {
            id: 'work-1-1',
            type: 'assignment',
            title: '章节理论练习',
            status: 'completed',
            score: 92,
            deadline: '2026-07-20'
          }
        ]
      },
      {
        id: 'chapter-2',
        title: '第二章 车站类型与布局',
        status: 'learning',
        items: [
          {
            id: 'ware-2-1',
            type: 'courseware',
            title: '2.1 岛式与侧式站台',
            status: 'completed',
            durationMinutes: 22,
            minDurationMinutes: 20,
            openStart: '2026-07-01',
            openEnd: '2026-08-31'
          },
          {
            id: 'ware-2-2',
            type: 'courseware',
            title: '2.2 车站类型与布局',
            status: 'current',
            durationMinutes: 12,
            minDurationMinutes: 20,
            openStart: '2026-07-01',
            openEnd: '2026-08-31'
          },
          {
            id: 'work-2-1',
            type: 'assignment',
            title: '车站布局实训作业',
            status: 'pending',
            deadline: '2026-08-15'
          }
        ]
      },
      {
        id: 'chapter-3',
        title: '第三章 运营组织基础',
        status: 'notStarted',
        items: [
          {
            id: 'ware-3-1',
            type: 'courseware',
            title: '3.1 行车组织规则',
            status: 'locked',
            durationMinutes: 0,
            minDurationMinutes: 25,
            openStart: '2026-07-10',
            openEnd: '2026-08-31'
          },
          {
            id: 'work-3-1',
            type: 'assignment',
            title: '运营组织理论作业',
            status: 'locked',
            deadline: '2026-08-28'
          }
        ]
      }
    ]
  },
  {
    id: 2026073002,
    name: '轨道交通信号系统',
    term: '2025-2026 学年下学期',
    startAt: '2026-08-10',
    endAt: '2026-10-20',
    completedItems: 0,
    totalItems: 8,
    resourceCount: 5,
    assignmentCount: 3,
    teachers: ['孙老师'],
    mode: 'free',
    chapters: [
      {
        id: 'signal-1',
        title: '第一章 信号基础',
        status: 'notStarted',
        items: [
          {
            id: 'signal-ware-1',
            type: 'courseware',
            title: '信号机与联锁基础',
            status: 'pending',
            durationMinutes: 0,
            minDurationMinutes: 18,
            openStart: '2026-08-10',
            openEnd: '2026-10-20'
          }
        ]
      }
    ]
  },
  {
    id: 2026073001,
    name: '行车组织基础',
    term: '2025-2026 学年上学期',
    startAt: '2026-04-01',
    endAt: '2026-06-30',
    completedItems: 8,
    totalItems: 8,
    resourceCount: 4,
    assignmentCount: 4,
    teachers: ['李老师', '周老师'],
    mode: 'sequence',
    chapters: [
      {
        id: 'traffic-1',
        title: '课程归档',
        status: 'completed',
        items: [
          {
            id: 'traffic-report',
            type: 'assignment',
            title: '期末综合报告',
            status: 'completed',
            score: 88,
            deadline: '2026-06-20'
          }
        ]
      }
    ]
  }
];
