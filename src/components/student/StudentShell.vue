<template>
  <section class="student-shell">
    <aside class="student-nav">
      <div class="student-brand">
        <strong>教辅系统</strong>
        <span>学员端</span>
      </div>
      <nav>
        <RouterLink v-for="item in navItems" :key="item.path" :to="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <section class="student-shell-main">
      <header class="student-topbar">
        <div>
          <p>{{ eyebrow }}</p>
          <h1>{{ title }}</h1>
        </div>
        <div class="student-userbar">
          <el-badge is-dot>
            <el-button :icon="Bell" circle aria-label="消息通知" />
          </el-badge>
          <el-dropdown>
            <button class="user-trigger">张同学</button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="student-main">
        <slot />
      </main>
    </section>
  </section>
</template>

<script setup lang="ts">
import { Bell, Collection, DataAnalysis, Files, Monitor } from '@element-plus/icons-vue';

defineProps<{
  eyebrow: string;
  title: string;
}>();

const navItems = [
  { label: '课程学习', path: '/student/courses', icon: Collection },
  { label: '实训中心', path: '/student/training', icon: Monitor },
  { label: '资源学习', path: '/student/resources', icon: Files },
  { label: '个人中心', path: '/student/profile', icon: DataAnalysis }
];
</script>
