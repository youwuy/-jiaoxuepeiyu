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
  resourceType?: string;
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

export const mockStudentCourses: StudentCourse[] = [
  {
    id: 2026073006,
    name: '城市轨道交通概论',
    term: '2024-2025学年 下学期',
    startAt: '2025-03-11',
    endAt: '2025-06-20',
    completedItems: 5,
    totalItems: 11,
    resourceCount: 12,
    assignmentCount: 8,
    teachers: ['王建国', '李忠华'],
    mode: 'sequence',
    chapters: [
      {
        id: 'chapter-1',
        title: '第一章 城市轨道交通概述',
        status: 'completed',
        items: [
          {
            id: 'ware-1-1',
            type: 'courseware',
            title: '1.1 城市轨道交通定义与分类',
            status: 'completed',
            resourceType: 'PPT文档',
            durationMinutes: 18,
            minDurationMinutes: 15,
            openStart: '2025-03-11',
            openEnd: '2025-06-20'
          },
          {
            id: 'ware-1-2',
            type: 'courseware',
            title: '1.2 国内外发展历程',
            status: 'completed',
            resourceType: 'PPT文档',
            durationMinutes: 16,
            minDurationMinutes: 15,
            openStart: '2025-03-11',
            openEnd: '2025-06-20'
          },
          {
            id: 'work-1-1',
            type: 'assignment',
            title: '第一章作业',
            status: 'completed',
            score: 92,
            deadline: '2025-04-20'
          }
        ]
      },
      {
        id: 'chapter-2',
        title: '第二章 线路与车站',
        status: 'learning',
        items: [
          {
            id: 'ware-2-1',
            type: 'courseware',
            title: '2.1 线路分类与特点',
            status: 'completed',
            resourceType: 'PPT文档',
            durationMinutes: 22,
            minDurationMinutes: 20,
            openStart: '2025-03-11',
            openEnd: '2025-06-20'
          },
          {
            id: 'ware-2-2',
            type: 'courseware',
            title: '2.2 车站类型与布局',
            status: 'current',
            resourceType: 'PPT文档',
            durationMinutes: 12,
            learnedSeconds: 512,
            minDurationMinutes: 15,
            openStart: '2025-03-11',
            openEnd: '2025-06-20'
          },
          {
            id: 'ware-2-3',
            type: 'courseware',
            title: '2.3 换乘站设计',
            status: 'pending',
            resourceType: 'PPT文档',
            durationMinutes: 0,
            minDurationMinutes: 15,
            openStart: '2025-03-11',
            openEnd: '2025-06-20'
          },
          {
            id: 'work-2-1',
            type: 'assignment',
            title: '第二章作业',
            status: 'locked',
            deadline: '2025-03-20'
          }
        ]
      },
      {
        id: 'chapter-3',
        title: '第三章 车辆系统',
        status: 'notStarted',
        items: [
          {
            id: 'ware-3-1',
            type: 'courseware',
            title: '3.1 行车组织规则',
            status: 'locked',
            durationMinutes: 0,
            minDurationMinutes: 25,
            openStart: '2025-03-20',
            openEnd: '2025-06-20'
          },
          {
            id: 'work-3-1',
            type: 'assignment',
            title: '运营组织理论作业',
            status: 'locked',
            deadline: '2025-06-10'
          }
        ]
      },
      {
        id: 'chapter-4',
        title: '第四章 信号与通信系统',
        status: 'notStarted',
        items: [
          {
            id: 'ware-4-1',
            type: 'courseware',
            title: '4.1 信号系统认知',
            status: 'locked',
            resourceType: 'PPT文档',
            durationMinutes: 0,
            minDurationMinutes: 15,
            openStart: '2025-03-20',
            openEnd: '2025-06-20'
          }
        ]
      },
      {
        id: 'chapter-5',
        title: '第五章 供电系统',
        status: 'notStarted',
        items: [
          {
            id: 'ware-5-1',
            type: 'courseware',
            title: '5.1 牵引供电基础',
            status: 'locked',
            resourceType: 'PPT文档',
            durationMinutes: 0,
            minDurationMinutes: 15,
            openStart: '2025-03-20',
            openEnd: '2025-06-20'
          }
        ]
      }
    ]
  },
  {
    id: 2026073005,
    name: '轨道交通信号系统',
    term: '2024-2025学年 下学期',
    startAt: '2025-03-11',
    endAt: '2025-06-20',
    completedItems: 2,
    totalItems: 10,
    resourceCount: 12,
    assignmentCount: 8,
    teachers: ['王建国', '李忠华'],
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
            openStart: '2025-03-11',
            openEnd: '2025-06-20'
          }
        ]
      }
    ]
  },
  {
    id: 2026073004,
    name: '城轨车辆构造与维护',
    term: '2024-2025学年 下学期',
    startAt: '2025-03-11',
    endAt: '2025-06-20',
    completedItems: 10,
    totalItems: 10,
    resourceCount: 12,
    assignmentCount: 8,
    teachers: ['王建国', '李忠华'],
    mode: 'sequence',
    chapters: [
      {
        id: 'vehicle-1',
        title: '第一章 车辆基础结构',
        status: 'completed',
        items: [
          {
            id: 'vehicle-ware-1',
            type: 'courseware',
            title: '转向架与车体结构',
            status: 'completed',
            durationMinutes: 24,
            minDurationMinutes: 20,
            openStart: '2025-03-11',
            openEnd: '2025-06-20'
          },
          {
            id: 'vehicle-work-1',
            type: 'assignment',
            title: '车辆构造单元测验',
            status: 'completed',
            score: 96,
            deadline: '2025-05-18'
          }
        ]
      }
    ]
  },
  {
    id: 2026073003,
    name: '城轨交通安全管理',
    term: '2025-2026学年 上学期',
    startAt: '2025-09-07',
    endAt: '2025-12-30',
    completedItems: 0,
    totalItems: 10,
    resourceCount: 12,
    assignmentCount: 8,
    teachers: ['王建国', '李忠华'],
    mode: 'sequence',
    chapters: [
      {
        id: 'safety-1',
        title: '第一章 安全制度认知',
        status: 'notStarted',
        items: [
          {
            id: 'safety-ware-1',
            type: 'courseware',
            title: '运营安全基础',
            status: 'locked',
            durationMinutes: 0,
            minDurationMinutes: 20,
            openStart: '2025-09-07',
            openEnd: '2025-12-30'
          }
        ]
      }
    ]
  },
  {
    id: 2026073002,
    name: '轨道交通供电系统',
    term: '2025-2026学年 上学期',
    startAt: '2025-09-07',
    endAt: '2025-12-30',
    completedItems: 0,
    totalItems: 10,
    resourceCount: 12,
    assignmentCount: 8,
    teachers: ['王建国', '李忠华'],
    mode: 'free',
    chapters: [
      {
        id: 'power-1',
        title: '第一章 供电系统组成',
        status: 'notStarted',
        items: [
          {
            id: 'power-ware-1',
            type: 'courseware',
            title: '牵引供电基础',
            status: 'locked',
            durationMinutes: 0,
            minDurationMinutes: 20,
            openStart: '2025-09-07',
            openEnd: '2025-12-30'
          }
        ]
      }
    ]
  },
  {
    id: 2026073001,
    name: '轨道交通运营管理',
    term: '2024-2025学年 上学期',
    startAt: '2024-09-15',
    endAt: '2025-01-03',
    completedItems: 17,
    totalItems: 22,
    resourceCount: 12,
    assignmentCount: 8,
    teachers: ['王建国', '李忠华'],
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
            deadline: '2024-12-20'
          }
        ]
      }
    ]
  }
];
