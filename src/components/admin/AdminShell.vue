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
            @click="goTo(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>
        </section>
      </div>

      <div class="admin-sidebar-footer">
        <button type="button" class="admin-sidebar-user" @click="goTo('/admin/profile')">{{ currentUserName }}</button>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
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
import { fetchAdminPermissionTree, type AdminPermissionNode } from '../../api/admin-permission';
import { fetchAdminProfile } from '../../api/admin-profile';

defineProps<{
  activeKey: string;
}>();

const router = useRouter();
const permissionTree = ref<AdminPermissionNode[]>([]);
const permissionsLoaded = ref(false);
const currentUserName = ref(storedAdminName() || '教师');
const adminPermissionsChangedEvent = 'admin-permissions-changed';

function goTo(path: string) {
  if (path.startsWith('/')) {
    router.push(path);
  }
}

interface NavItem {
  key: string;
  label: string;
  path: string;
  icon: typeof Operation;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

const staticNavGroups: NavGroup[] = [
  {
    title: '系统基础设置',
    items: [
      { key: 'organization', label: '组织管理', path: '/admin/organization', icon: Operation },
      { key: 'users', label: '用户管理', path: '/admin/users', icon: User },
      { key: 'permissions', label: '功能管理', path: '/admin/permissions', icon: Menu },
      { key: 'roles', label: '角色管理', path: '/admin/roles', icon: Folder },
      { key: 'settings', label: '配置管理', path: '/admin/settings', icon: Setting }
    ]
  },
  {
    title: '资源管理',
    items: [
      { key: 'personal-resource', label: '个人资源库', path: '/admin/personal-resource', icon: Files },
      { key: 'public-application', label: '资源公开申请', path: '/admin/public-application', icon: Collection },
      { key: 'public-resource', label: '公开资源库', path: '/admin/public-resource', icon: Notebook },
      { key: 'theory-question', label: '理论题库', path: '/admin/theory-question', icon: Notebook },
      { key: 'theory-paper', label: '理论试卷', path: '/admin/theory-paper', icon: Notebook }
    ]
  },
  {
    title: '教学实训',
    items: [
      { key: 'admin-courses', label: '教学课程', path: '/admin/courses', icon: Monitor },
      { key: 'admin-trainings', label: '实训组课', path: '/admin/training', icon: Coin }
    ]
  },
  {
    title: '成绩统计',
    items: [
      { key: 'semester-score', label: '综合成绩', path: '/admin/semester-score', icon: DataAnalysis },
      { key: 'training-archive', label: '实训档案', path: '/admin/training-archive', icon: Collection },
      { key: 'device-efficiency', label: '设备效能分析', path: '/admin/device-efficiency', icon: Folder }
    ]
  }
];

const navGroups = computed(() => {
  const visibleRoutes = collectVisibleRoutes(permissionTree.value);
  if (!permissionsLoaded.value) {
    return staticNavGroups;
  }

  return staticNavGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => visibleRoutes.has(item.path))
    }))
    .filter((group) => group.items.length > 0);
});

function collectVisibleRoutes(tree: AdminPermissionNode[]) {
  const routes = new Set<string>();

  const walk = (nodes: AdminPermissionNode[], ancestorsVisible: boolean) => {
    nodes.forEach((node) => {
      const visible = ancestorsVisible && node.visible !== false;
      if (visible && node.routePath?.startsWith('/admin')) {
        routes.add(node.routePath);
      }
      walk(node.children ?? [], visible);
    });
  };

  walk(tree, true);
  return routes;
}

async function loadPermissionTree() {
  try {
    permissionTree.value = await fetchAdminPermissionTree();
  } catch {
    permissionTree.value = [];
  } finally {
    permissionsLoaded.value = true;
  }
}

function storedAdminName() {
  const storedUser = localStorage.getItem('jiaoxuepeiyu_admin_user') || localStorage.getItem('jiaoxuepeiyu_user');
  if (!storedUser) {
    return '';
  }

  try {
    const user = JSON.parse(storedUser) as {
      accountNo?: string;
      name?: string;
      realName?: string;
      username?: string;
    };
    return user.realName || user.name || user.accountNo || user.username || '';
  } catch {
    return '';
  }
}

function cacheAdminName(realName: string, accountNo?: string) {
  const storedUser = localStorage.getItem('jiaoxuepeiyu_admin_user') || '{}';
  let user: Record<string, unknown> = {};

  try {
    user = JSON.parse(storedUser) as Record<string, unknown>;
  } catch {
    user = {};
  }

  localStorage.setItem('jiaoxuepeiyu_admin_user', JSON.stringify({ ...user, realName, accountNo }));
}

async function loadCurrentUserName() {
  const cachedName = storedAdminName();
  if (cachedName) {
    currentUserName.value = cachedName;
  }

  try {
    const profile = await fetchAdminProfile();
    const name = profile.realName || profile.accountNo || cachedName || '教师';
    currentUserName.value = name;
    cacheAdminName(profile.realName || name, profile.accountNo);
  } catch {
    if (!cachedName) {
      currentUserName.value = '教师';
    }
  }
}

onMounted(() => {
  void loadPermissionTree();
  void loadCurrentUserName();
  window.addEventListener(adminPermissionsChangedEvent, loadPermissionTree);
});

onBeforeUnmount(() => {
  window.removeEventListener(adminPermissionsChangedEvent, loadPermissionTree);
});
</script>
