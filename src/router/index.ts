import { createRouter, createWebHistory } from 'vue-router';
import AdminLogin from '../views/auth/AdminLogin.vue';
import StudentLogin from '../views/auth/StudentLogin.vue';
import StudentCourseLearn from '../views/student/StudentCourseLearn.vue';
import StudentCourses from '../views/student/StudentCourses.vue';

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
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLogin
    }
  ]
});
