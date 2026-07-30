import { describe, expect, it } from 'vitest';
import {
  calculateCourseProgress,
  deriveCourseStatus,
  filterCoursesByKeyword,
  sortCoursesForStudent
} from '../src/features/student/courses';

const now = new Date('2026-07-30T08:00:00');

const courses = [
  {
    id: 1001,
    name: '城市轨道交通概论',
    term: '2025-2026 学年下学期',
    startAt: '2026-07-01',
    endAt: '2026-08-31',
    completedItems: 7,
    totalItems: 9,
    resourceCount: 6,
    assignmentCount: 3,
    teachers: ['赵老师']
  },
  {
    id: 1002,
    name: '轨道交通信号系统',
    term: '2025-2026 学年下学期',
    startAt: '2026-09-01',
    endAt: '2026-10-01',
    completedItems: 0,
    totalItems: 8,
    resourceCount: 5,
    assignmentCount: 3,
    teachers: ['钱老师']
  },
  {
    id: 1003,
    name: '行车组织基础',
    term: '2025-2026 学年上学期',
    startAt: '2026-05-01',
    endAt: '2026-06-30',
    completedItems: 8,
    totalItems: 8,
    resourceCount: 4,
    assignmentCount: 4,
    teachers: ['孙老师']
  }
];

describe('student course rules', () => {
  it('derives course status from the current time and open period', () => {
    expect(deriveCourseStatus(courses[0], now)).toBe('learning');
    expect(deriveCourseStatus(courses[1], now)).toBe('notStarted');
    expect(deriveCourseStatus(courses[2], now)).toBe('completed');
  });

  it('calculates progress by flooring completed items divided by total items', () => {
    expect(calculateCourseProgress(courses[0])).toBe(77);
    expect(calculateCourseProgress({ ...courses[0], totalItems: 0 })).toBe(0);
  });

  it('filters courses by keyword and restores all courses for blank keyword', () => {
    expect(filterCoursesByKeyword(courses, '信号').map((course) => course.name)).toEqual(['轨道交通信号系统']);
    expect(filterCoursesByKeyword(courses, '   ')).toHaveLength(3);
  });

  it('sorts learning, not started, then completed courses for student cards', () => {
    expect(sortCoursesForStudent(courses, now).map((course) => course.id)).toEqual([1001, 1002, 1003]);
  });
});
