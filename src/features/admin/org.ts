import type { AdminOrgNode } from '../../api/admin-org';

export interface AdminOrgRow extends AdminOrgNode {
  level: number;
  hasChildren: boolean;
  visible: boolean;
}

export const mockAdminOrgs: AdminOrgNode[] = [
  {
    orgId: 1,
    parentId: null,
    orgName: '交通与车辆工程学院',
    sortOrder: 1,
    enabled: true,
    creatorName: '张明悦',
    createdAt: '2024-01-15T09:00:00',
    updaterName: '张明悦',
    updatedAt: '2025-03-20T14:30:00',
    children: [
      {
        orgId: 2,
        parentId: 1,
        orgName: '运输管理教研室',
        sortOrder: 1,
        enabled: true,
        creatorName: '张明悦',
        createdAt: '2024-02-20T10:15:00',
        updaterName: '张明悦',
        updatedAt: '2025-03-18T09:45:00',
        children: [
          {
            orgId: 3,
            parentId: 2,
            orgName: '车辆管理',
            sortOrder: 1,
            enabled: true,
            creatorName: '张明悦',
            createdAt: '2024-03-10T08:30:00',
            updaterName: '张明悦',
            updatedAt: '2025-01-05T16:20:00'
          },
          {
            orgId: 4,
            parentId: 2,
            orgName: '车辆运营',
            sortOrder: 2,
            enabled: true,
            creatorName: '张明悦',
            createdAt: '2024-03-15T11:00:00',
            updaterName: '张明悦',
            updatedAt: '2025-02-28T10:10:00'
          }
        ]
      }
    ]
  },
  {
    orgId: 5,
    parentId: null,
    orgName: '城轨学院',
    sortOrder: 2,
    enabled: false,
    creatorName: '张明悦',
    createdAt: '2024-04-01T09:00:00',
    updaterName: '张明悦',
    updatedAt: '2025-01-10T15:00:00'
  }
];

export function flattenAdminOrgTree(tree: AdminOrgNode[], expandedIds: Set<number>, keyword = ''): AdminOrgRow[] {
  const normalizedKeyword = keyword.replace(/\s+/g, '').toLowerCase();
  const rows: AdminOrgRow[] = [];

  const walk = (items: AdminOrgNode[], level: number, parentVisible: boolean) => {
    items.forEach((item) => {
      const children = item.children ?? [];
      const selfMatches = !normalizedKeyword || item.orgName.replace(/\s+/g, '').toLowerCase().includes(normalizedKeyword);
      const descendantsMatch = hasMatchingDescendant(children, normalizedKeyword);
      const visible = parentVisible && (selfMatches || descendantsMatch || !normalizedKeyword);
      const row: AdminOrgRow = {
        ...item,
        level,
        hasChildren: children.length > 0,
        visible
      };
      rows.push(row);

      const shouldShowChildren = normalizedKeyword ? visible : visible && expandedIds.has(item.orgId);
      if (children.length > 0) {
        walk(children, level + 1, shouldShowChildren);
      }
    });
  };

  walk(tree, 0, true);
  return rows.filter((row) => row.visible);
}

export function collectAdminOrgIds(tree: AdminOrgNode[]): number[] {
  return tree.flatMap((item) => [item.orgId, ...collectAdminOrgIds(item.children ?? [])]);
}

export function countAdminOrgs(tree: AdminOrgNode[]): number {
  return collectAdminOrgIds(tree).length;
}

function hasMatchingDescendant(children: AdminOrgNode[], keyword: string): boolean {
  if (!keyword) {
    return true;
  }

  return children.some(
    (child) => child.orgName.replace(/\s+/g, '').toLowerCase().includes(keyword) || hasMatchingDescendant(child.children ?? [], keyword)
  );
}
