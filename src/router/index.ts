import { createRouter, createWebHistory } from 'vue-router';
import AdminLogin from '../views/auth/AdminLogin.vue';
import StudentLogin from '../views/auth/StudentLogin.vue';

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
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLogin
    }
  ]
});
