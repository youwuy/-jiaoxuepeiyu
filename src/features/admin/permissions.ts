import type { AdminPermissionNode, AdminPermissionType } from '../../api/admin-permission';

export interface AdminPermissionRow extends AdminPermissionNode {
  level: number;
  hasChildren: boolean;
}

export const permissionTypeLabels: Record<AdminPermissionType, string> = {
  MENU: '目录',
  PAGE: '菜单',
  BUTTON: '按钮'
};

export const mockAdminPermissions: AdminPermissionNode[] = [
  {
    permissionId: 1,
    parentId: null,
    permissionName: '系统基础设置',
    permissionCode: 'system',
    permissionType: 'MENU',
    routePath: '/system',
    visible: true,
    sortOrder: 1,
    children: [
      {
        permissionId: 2,
        parentId: 1,
        permissionName: '组织管理',
        permissionCode: 'system:org',
        permissionType: 'PAGE',
        routePath: '/system/organization',
        visible: true,
        sortOrder: 1,
        children: [
          {
            permissionId: 3,
            parentId: 2,
            permissionName: '新增组织',
            permissionCode: 'system:org:create',
            permissionType: 'BUTTON',
            routePath: null,
            visible: true,
            sortOrder: 1
          },
          {
            permissionId: 4,
            parentId: 2,
            permissionName: '编辑组织',
            permissionCode: 'system:org:update',
            permissionType: 'BUTTON',
            routePath: null,
            visible: true,
            sortOrder: 2
          },
          {
            permissionId: 5,
            parentId: 2,
            permissionName: '注销组织',
            permissionCode: 'system:org:disable',
            permissionType: 'BUTTON',
            routePath: null,
            visible: true,
            sortOrder: 3
          }
        ]
      }
    ]
  },
  {
    permissionId: 6,
    parentId: null,
    permissionName: '资源管理',
    permissionCode: 'resource',
    permissionType: 'MENU',
    routePath: '/resource',
    visible: true,
    sortOrder: 2,
    children: [
      {
        permissionId: 7,
        parentId: 6,
        permissionName: '个人资源库',
        permissionCode: 'resource:personal',
        permissionType: 'PAGE',
        routePath: '/admin/personal-resource',
        visible: true,
        sortOrder: 1
      },
      {
        permissionId: 8,
        parentId: 6,
        permissionName: '资源公开申请',
        permissionCode: 'resource:public-apply',
        permissionType: 'PAGE',
        routePath: '/admin/public-application',
        visible: true,
        sortOrder: 2
      },
      {
        permissionId: 9,
        parentId: 6,
        permissionName: '公开资源库',
        permissionCode: 'resource:public-library',
        permissionType: 'PAGE',
        routePath: '/admin/public-resource',
        visible: true,
        sortOrder: 3
      },
      {
        permissionId: 10,
        parentId: 6,
        permissionName: '理论试题',
        permissionCode: 'resource:theory-question',
        permissionType: 'PAGE',
        routePath: '/admin/theory-question',
        visible: true,
        sortOrder: 4
      },
      {
        permissionId: 11,
        parentId: 6,
        permissionName: '理论试卷',
        permissionCode: 'resource:theory-paper',
        permissionType: 'PAGE',
        routePath: '/admin/theory-paper',
        visible: true,
        sortOrder: 5
      }
    ]
  },
  {
    permissionId: 12,
    parentId: null,
    permissionName: '教学实训',
    permissionCode: 'teaching',
    permissionType: 'MENU',
    routePath: '/teaching',
    visible: false,
    sortOrder: 3,
    children: [
      {
        permissionId: 13,
        parentId: 12,
        permissionName: '教学课程',
        permissionCode: 'teaching:course',
        permissionType: 'PAGE',
        routePath: '/admin/courses',
        visible: true,
        sortOrder: 1
      },
      {
        permissionId: 14,
        parentId: 12,
        permissionName: '实训组课',
        permissionCode: 'teaching:training',
        permissionType: 'PAGE',
        routePath: '/admin/training',
        visible: true,
        sortOrder: 2
      }
    ]
  },
  {
    permissionId: 15,
    parentId: null,
    permissionName: '角色管理',
    permissionCode: 'role',
    permissionType: 'MENU',
    routePath: '/role',
    visible: true,
    sortOrder: 4,
    children: [
      {
        permissionId: 16,
        parentId: 15,
        permissionName: '角色列表',
        permissionCode: 'role:list',
        permissionType: 'PAGE',
        routePath: '/admin/roles',
        visible: true,
        sortOrder: 1
      },
      {
        permissionId: 17,
        parentId: 15,
        permissionName: '新增角色',
        permissionCode: 'role:create',
        permissionType: 'BUTTON',
        routePath: null,
        visible: true,
        sortOrder: 2
      },
      {
        permissionId: 18,
        parentId: 15,
        permissionName: '编辑角色',
        permissionCode: 'role:update',
        permissionType: 'BUTTON',
        routePath: null,
        visible: true,
        sortOrder: 3
      },
      {
        permissionId: 19,
        parentId: 15,
        permissionName: '删除角色',
        permissionCode: 'role:delete',
        permissionType: 'BUTTON',
        routePath: null,
        visible: true,
        sortOrder: 4
      }
    ]
  },
  {
    permissionId: 20,
    parentId: null,
    permissionName: '配置信息',
    permissionCode: 'config',
    permissionType: 'MENU',
    routePath: '/config',
    visible: true,
    sortOrder: 5,
    children: [
      {
        permissionId: 21,
        parentId: 20,
        permissionName: '学年学期',
        permissionCode: 'config:term',
        permissionType: 'PAGE',
        routePath: '/admin/education-config',
        visible: true,
        sortOrder: 1
      },
      {
        permissionId: 22,
        parentId: 20,
        permissionName: '专业管理',
        permissionCode: 'config:major',
        permissionType: 'PAGE',
        routePath: '/admin/organization',
        visible: true,
        sortOrder: 2
      },
      {
        permissionId: 23,
        parentId: 20,
        permissionName: '班级管理',
        permissionCode: 'config:class',
        permissionType: 'PAGE',
        routePath: '/admin/organization',
        visible: true,
        sortOrder: 3
      }
    ]
  },
  {
    permissionId: 24,
    parentId: null,
    permissionName: '成绩统计',
    permissionCode: 'score',
    permissionType: 'MENU',
    routePath: '/score',
    visible: true,
    sortOrder: 6,
    children: [
      {
        permissionId: 25,
        parentId: 24,
        permissionName: '综合成绩',
        permissionCode: 'score:semester',
        permissionType: 'PAGE',
        routePath: '/admin/semester-score',
        visible: true,
        sortOrder: 1
      },
      {
        permissionId: 26,
        parentId: 24,
        permissionName: '实训档案',
        permissionCode: 'score:archive',
        permissionType: 'PAGE',
        routePath: '/admin/training-archive',
        visible: true,
        sortOrder: 2
      },
      {
        permissionId: 27,
        parentId: 24,
        permissionName: '设备效能分析',
        permissionCode: 'score:device',
        permissionType: 'PAGE',
        routePath: '/admin/device-efficiency',
        visible: true,
        sortOrder: 3
      }
    ]
  },
  {
    permissionId: 28,
    parentId: null,
    permissionName: '理论试题',
    permissionCode: 'exam',
    permissionType: 'MENU',
    routePath: '/exam',
    visible: true,
    sortOrder: 7,
    children: [
      {
        permissionId: 29,
        parentId: 28,
        permissionName: '题库管理',
        permissionCode: 'exam:question-bank',
        permissionType: 'PAGE',
        routePath: '/admin/theory-question',
        visible: true,
        sortOrder: 1
      },
      {
        permissionId: 30,
        parentId: 28,
        permissionName: '试卷管理',
        permissionCode: 'exam:paper',
        permissionType: 'PAGE',
        routePath: '/admin/theory-paper',
        visible: true,
        sortOrder: 2
      },
      {
        permissionId: 31,
        parentId: 28,
        permissionName: '考试发布',
        permissionCode: 'exam:publish',
        permissionType: 'BUTTON',
        routePath: null,
        visible: true,
        sortOrder: 3
      }
    ]
  },
  {
    permissionId: 32,
    parentId: null,
    permissionName: '权限日志',
    permissionCode: 'log',
    permissionType: 'MENU',
    routePath: '/log',
    visible: true,
    sortOrder: 8
  }
];

export function flattenAdminPermissionTree(tree: AdminPermissionNode[], expandedIds: Set<number>): AdminPermissionRow[] {
  const rows: AdminPermissionRow[] = [];

  const walk = (items: AdminPermissionNode[], level: number, parentVisible: boolean) => {
    items.forEach((item) => {
      const children = item.children ?? [];
      const visibleRow = parentVisible;

      rows.push({
        ...item,
        level,
        hasChildren: children.length > 0
      });

      if (children.length > 0 && expandedIds.has(item.permissionId)) {
        walk(children, level + 1, visibleRow);
      }
    });
  };

  walk(tree, 0, true);
  return rows;
}

export function collectAdminPermissionIds(tree: AdminPermissionNode[]): number[] {
  return tree.flatMap((item) => [item.permissionId, ...collectAdminPermissionIds(item.children ?? [])]);
}

export function countAdminPermissions(tree: AdminPermissionNode[]): number {
  return collectAdminPermissionIds(tree).length;
}

export function flattenAllAdminPermissions(tree: AdminPermissionNode[]): AdminPermissionNode[] {
  return tree.flatMap((item) => [item, ...flattenAllAdminPermissions(item.children ?? [])]);
}

export function findAdminPermissionById(tree: AdminPermissionNode[], permissionId: number): AdminPermissionNode | null {
  for (const item of tree) {
    if (item.permissionId === permissionId) {
      return item;
    }
    const child = findAdminPermissionById(item.children ?? [], permissionId);
    if (child) {
      return child;
    }
  }
  return null;
}
