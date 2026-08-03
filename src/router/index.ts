import { createRouter, createWebHistory } from 'vue-router';
import { hasAuthSession } from '../api/http';
import { adminRoutes } from './admin';
import { studentRoutes } from './student';

export const router = createRouter({
  history: createWebHistory(),
  routes: [...studentRoutes, ...adminRoutes]
});

router.beforeEach((to) => {
  if (to.path.startsWith('/admin') && to.path !== '/admin/login' && !hasAuthSession('admin')) {
    return '/admin/login';
  }

  if (to.path.startsWith('/student') && to.path !== '/student/login' && !hasAuthSession('student')) {
    return '/student/login';
  }

  return true;
});
