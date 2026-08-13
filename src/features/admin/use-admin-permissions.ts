import { onMounted, ref } from 'vue';
import { fetchMyAdminPermissionTree } from '../../api/admin-permission';
import { collectPermissionCodes } from './permission-check';

export function useAdminPermissions(pageCode: string) {
  const codes = ref<Set<string>>(new Set());
  const loaded = ref(false);

  const can = (action: string) => !loaded.value || codes.value.has(`${pageCode}:${action}`);

  onMounted(async () => {
    try {
      codes.value = collectPermissionCodes(await fetchMyAdminPermissionTree());
    } finally {
      loaded.value = true;
    }
  });

  return { can, permissionsLoaded: loaded };
}
