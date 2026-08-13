import type { AdminPermissionNode } from '../../api/admin-permission';

export function collectPermissionCodes(tree: AdminPermissionNode[]): Set<string> {
  const result = new Set<string>();
  const walk = (nodes: AdminPermissionNode[]) => nodes.forEach((node) => {
    if (node.permissionCode) result.add(node.permissionCode);
    walk(node.children ?? []);
  });
  walk(tree);
  return result;
}

export function canAdminAction(codes: Set<string>, pageCode: string, action: string): boolean {
  return codes.has(`${pageCode}:${action}`) || codes.has(`${pageCode}:list`) && action === 'list';
}
