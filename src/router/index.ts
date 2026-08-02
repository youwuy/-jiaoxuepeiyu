import { createRouter, createWebHistory } from 'vue-router';
import { adminRoutes } from './admin';
import { studentRoutes } from './student';

export const router = createRouter({
  history: createWebHistory(),
  routes: [...studentRoutes, ...adminRoutes]
});
