import { onMounted, ref } from 'vue';
import { fetchMyAdminPermissionTree } from '../../api/admin-permission';
import { canAdminAction, collectPermissionCodes } from './permission-check';

export function useAdminPermissions(pageCode: string) {
  const codes = ref<Set<string>>(new Set());
  const loaded = ref(false);

  const canFor = (targetPageCode: string, action: string) => !loaded.value || canAdminAction(codes.value, targetPageCode, action);
  const can = (action: string) => canFor(pageCode, action);

  onMounted(async () => {
    try {
      codes.value = collectPermissionCodes(await fetchMyAdminPermissionTree());
    } finally {
      loaded.value = true;
    }
  });

  return { can, canFor, permissionsLoaded: loaded };
}
