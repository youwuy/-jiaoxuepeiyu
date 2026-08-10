import { describe, expect, it } from 'vitest';
import type { AdminPermissionNode } from '../src/api/admin-permission';
import {
  buildRolePermissionRows,
  isRolePermissionPageChecked,
  isRolePermissionPageIndeterminate
} from '../src/features/admin/roles';

function node(
  permissionId: number,
  permissionName: string,
  permissionCode: string,
  permissionType: AdminPermissionNode['permissionType'],
  children: AdminPermissionNode[] = []
): AdminPermissionNode {
  return {
    permissionId,
    permissionName,
    permissionCode,
    permissionType,
    visible: true,
    sortOrder: permissionId,
    children
  };
}

function standardPage(id: number, name: string, code: string): AdminPermissionNode {
  return node(id, name, code, 'PAGE', [
    node(id + 50, '列表', `${code}:list`, 'BUTTON'),
    node(id + 100, '新增', `${code}:create`, 'BUTTON'),
    node(id + 200, '删除', `${code}:delete`, 'BUTTON'),
    node(id + 300, '修改', `${code}:update`, 'BUTTON'),
    node(id + 400, '启用', `${code}:enable`, 'BUTTON'),
    node(id + 500, '禁用', `${code}:disable`, 'BUTTON')
  ]);
}

describe('role permission matrix', () => {
  it('flattens nested menus into module and page rows without exposing buttons as pages', () => {
    const userPage = standardPage(2, '用户管理', 'system:user');
    userPage.children?.push(node(999, '异常按钮', 'system:user:111', 'BUTTON'));
    const rolePage = standardPage(3, '角色列表', 'role:list');
    const termPage = standardPage(4, '学年配置', 'config:term');
    const classPage = standardPage(5, '班级配置', 'config:class');
    const tree = [
      node(1, '系统管理', 'system', 'MENU', [
        userPage,
        node(10, '角色管理', 'role', 'MENU', [rolePage, node(11, '新增角色', 'role:create', 'BUTTON')]),
        node(20, '基础数据', 'config', 'MENU', [termPage, classPage])
      ])
    ];

    const rows = buildRolePermissionRows(tree);

    expect(rows.map((row) => [row.moduleName, row.pageName])).toEqual([
      ['系统管理', '用户管理'],
      ['', '角色管理'],
      ['基础数据', '学年配置'],
      ['', '班级配置']
    ]);
    expect(rows[0].actions.map((action) => action.label)).toEqual(['列表', '新增', '删除', '修改', '启用', '禁用']);
    expect(rows[0].actions.map((action) => action.id)).toEqual([52, 102, 202, 302, 402, 502]);
    expect(rows[1].actions.find((action) => action.label === '新增')?.id).toBe(11);
    expect(rows.some((row) => row.pageName === '新增角色')).toBe(false);
    expect(rows[0].moduleIds).not.toContain(999);
    expect(rows[0].moduleIds).not.toContain(4);
    expect(rows[2].moduleIds).toContain(4);
  });

  it('marks only missing standard actions as unavailable', () => {
    const page = node(2, '只读页面', 'report:view', 'PAGE');
    const rows = buildRolePermissionRows([node(1, '报表管理', 'report', 'MENU', [page])]);

    expect(rows[0].actions.map((action) => action.id)).toEqual([2, null, null, null, null, null]);
  });

  it('treats the page checkbox as the all-functions state', () => {
    const rows = buildRolePermissionRows([
      node(1, '系统管理', 'system', 'MENU', [standardPage(2, '用户管理', 'system:user')])
    ]);
    const actionIds = rows[0].actions.map((action) => action.id).filter((id): id is number => id !== null);

    expect(isRolePermissionPageChecked(rows[0], actionIds)).toBe(true);
    expect(isRolePermissionPageChecked(rows[0], actionIds.slice(0, -1))).toBe(false);
    expect(isRolePermissionPageIndeterminate(rows[0], actionIds.slice(0, -1))).toBe(true);
    expect(isRolePermissionPageIndeterminate(rows[0], [])).toBe(false);
  });
});
