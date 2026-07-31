import { createRouter, createWebHistory } from 'vue-router';
import AdminLogin from '../views/auth/AdminLogin.vue';
import AdminCourses from '../views/admin/AdminCourses.vue';
import AdminTrainingCourses from '../views/admin/AdminTrainingCourses.vue';
import StudentLogin from '../views/auth/StudentLogin.vue';
import StudentCourseLearn from '../views/student/StudentCourseLearn.vue';
import StudentCourses from '../views/student/StudentCourses.vue';
import StudentProfile from '../views/student/StudentProfile.vue';
import StudentResources from '../views/student/StudentResources.vue';
import StudentTraining from '../views/student/StudentTraining.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
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
      path: '/student/resources',
      name: 'student-resources',
      component: StudentResources
    },
    {
      path: '/student/profile',
      name: 'student-profile',
      component: StudentProfile
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLogin
    },
    {
      path: '/admin',
      redirect: '/admin/courses'
    },
    {
      path: '/admin/courses',
      name: 'admin-courses',
      component: AdminCourses
    },
    {
      path: '/admin/training',
      name: 'admin-training',
      component: AdminTrainingCourses
    }
  ]
});
