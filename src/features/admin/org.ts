import type { AdminOrgNode } from '../../api/admin-org';

export interface AdminOrgRow extends AdminOrgNode {
  level: number;
  hasChildren: boolean;
  visible: boolean;
}

export const mockAdminOrgs: AdminOrgNode[] = [];

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
