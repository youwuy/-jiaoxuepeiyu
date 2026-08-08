import type { RouteRecordRaw } from 'vue-router';
import StudentLogin from '../views/auth/StudentLogin.vue';
import StudentCourseLearn from '../views/student/StudentCourseLearn.vue';
import StudentCourses from '../views/student/StudentCourses.vue';
import StudentProfile from '../views/student/StudentProfile.vue';
import StudentResources from '../views/student/StudentResources.vue';
import StudentTraining from '../views/student/StudentTraining.vue';
import StudentTrainingRoom from '../views/student/StudentTrainingRoom.vue';
import StudentTrainingRoomRoles from '../views/student/StudentTrainingRoomRoles.vue';
import StudentTrainingStart from '../views/student/StudentTrainingStart.vue';
import StudentTrainingScoreSheet from '../views/student/StudentTrainingScoreSheet.vue';

export const studentRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/student/login'
  },
  {
    path: '/student/login',
    name: 'student-login',
    component: StudentLogin
  },
  {
    path: '/student/courses',
    name: 'student-courses',
    component: StudentCourses
  },
  {
    path: '/student/courses/:id/learn',
    name: 'student-course-learn',
    component: StudentCourseLearn
  },
  {
    path: '/student/training',
    name: 'student-training',
    component: StudentTraining
  },
  {
    path: '/student/training/rooms/:roomId',
    name: 'student-training-room',
    component: StudentTrainingRoom
  },
  {
    path: '/student/training/rooms/:roomId/roles',
    name: 'student-training-room-roles',
    component: StudentTrainingRoomRoles
  },
  {
    path: '/student/training/rooms/:roomId/start',
    name: 'student-training-start',
    component: StudentTrainingStart
  },
  {
    path: '/student/training/score-sheet/:attemptId',
    name: 'student-training-score-sheet',
    component: StudentTrainingScoreSheet
  },
  {
    path: '/student/resources',
    name: 'student-resources',
    component: StudentResources
  },
  {
    path: '/student/profile',
    name: 'student-profile',
    component: StudentProfile
  }
];
