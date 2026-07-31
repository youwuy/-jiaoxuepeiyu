<template>
  <section class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <span class="admin-brand-icon">
          <el-icon><Monitor /></el-icon>
        </span>
        <strong>城轨实训系统</strong>
      </div>

      <div class="admin-nav-groups">
        <section v-for="group in navGroups" :key="group.title" class="admin-nav-group">
          <p>{{ group.title }}</p>
          <button
            v-for="item in group.items"
            :key="item.path"
            type="button"
            class="admin-nav-link"
            :class="{ active: item.key === activeKey }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>
        </section>
      </div>

      <div class="admin-sidebar-footer">
        <strong>李教师</strong>
        <el-button class="admin-logout-button" text circle aria-label="退出登录">
          <el-icon><SwitchButton /></el-icon>
        </el-button>
      </div>
    </aside>

    <main class="admin-content">
      <slot />
    </main>
  </section>
</template>

<script setup lang="ts">
import {
  Coin,
  Collection,
  DataAnalysis,
  Files,
  Folder,
  Menu,
  Monitor,
  Notebook,
  Operation,
  Setting,
  SwitchButton,
  User
} from '@element-plus/icons-vue';

defineProps<{
  activeKey: string;
}>();

const navGroups = [
  {
    title: '系统基础设置',
    items: [
      { key: 'organization', label: '组织管理', path: 'organization', icon: Operation },
      { key: 'users', label: '用户管理', path: 'users', icon: User },
      { key: 'permissions', label: '功能管理', path: 'permissions', icon: Menu },
      { key: 'roles', label: '角色管理', path: 'roles', icon: Folder },
      { key: 'settings', label: '配置管理', path: 'settings', icon: Setting }
    ]
  },
  {
    title: '资源管理',
    items: [
      { key: 'personal-resource', label: '个人资源库', path: 'personal-resource', icon: Files },
      { key: 'public-application', label: '资源公开申请', path: 'public-application', icon: Collection },
      { key: 'public-resource', label: '公开资源库', path: 'public-resource', icon: Notebook },
      { key: 'theory-question', label: '理论试题', path: 'theory-question', icon: Notebook },
      { key: 'theory-paper', label: '理论试卷', path: 'theory-paper', icon: Notebook }
    ]
  },
  {
    title: '教学实训',
    items: [
      { key: 'admin-courses', label: '教学课程', path: '/admin/courses', icon: Monitor },
      { key: 'admin-trainings', label: '实训组课', path: 'admin-trainings', icon: Coin }
    ]
  },
  {
    title: '成绩统计',
    items: [
      { key: 'semester-score', label: '综合成绩', path: 'semester-score', icon: DataAnalysis },
      { key: 'training-archive', label: '实训档案', path: 'training-archive', icon: Collection },
      { key: 'device-efficiency', label: '设备效能分析', path: 'device-efficiency', icon: Folder }
    ]
  }
];
</script>
