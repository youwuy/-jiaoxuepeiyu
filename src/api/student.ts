import { requestJson, tryRequestJson } from './http';
import type { StudentCourse } from '../features/student/courses';
import type { ScorePart, StudentMessage, TrainingArchive } from '../features/student/profile';
import type { StudentResource } from '../features/student/resources';
import type { StudentTraining } from '../features/student/training';

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

function normalizeList<T>(value: T[] | PageResult<T>): T[] {
  if (Array.isArray(value)) {
    return value;
  }

  return value.records || value.rows || value.list || value.data || [];
}

export async function fetchStudentCourses(): Promise<StudentCourse[]> {
  const result = await tryRequestJson<StudentCourse[] | PageResult<StudentCourse>>([
    '/student/courses',
    '/student/course/list',
    '/courses/student'
  ]);

  return normalizeList(result);
}

export async function fetchStudentCourse(courseId: number): Promise<StudentCourse> {
  try {
    return await tryRequestJson<StudentCourse>([
      `/student/courses/${courseId}`,
      `/student/course/${courseId}`,
      `/courses/student/${courseId}`
    ]);
  } catch {
    const courses = await fetchStudentCourses();
    const matchedCourse = courses.find((course) => Number(course.id) === Number(courseId));

    if (!matchedCourse) {
      throw new Error('课程不存在');
    }

    return matchedCourse;
  }
}

export async function fetchStudentTrainings(): Promise<StudentTraining[]> {
  const result = await tryRequestJson<StudentTraining[] | PageResult<StudentTraining>>([
    '/student/trainings',
    '/student/training/list',
    '/training/student'
  ]);

  return normalizeList(result);
}

export async function fetchStudentResources(): Promise<StudentResource[]> {
  const result = await tryRequestJson<StudentResource[] | PageResult<StudentResource>>([
    '/student/resources',
    '/student/resource/list',
    '/resources/student'
  ]);

  return normalizeList(result);
}

export async function fetchStudentProfile(): Promise<StudentProfileResult> {
  return requestJson<StudentProfileResult>('/student/profile', {
    fallbackLabel: '个人中心'
  });
}
