import { requestJson } from './http';

export interface AdminProfile {
  userId: number;
  accountNo: string;
  realName: string;
  userType: 'admin' | 'teacher' | string;
  phone?: string;
  idCard?: string;
  orgName?: string;
  jobTitle?: string;
}

export function fetchAdminProfile() {
  return requestJson<AdminProfile>('/admin/profile', { fallbackLabel: '管理端个人中心' });
}

export function updateAdminProfilePhone(phone: string) {
  return requestJson<void>('/admin/profile/phone', {
    method: 'PUT',
    body: JSON.stringify({ phone }),
    fallbackLabel: '修改手机号'
  });
}

export function updateAdminProfileIdCard(idCard: string) {
  return requestJson<void>('/admin/profile/id-card', {
    method: 'PUT',
    body: JSON.stringify({ idCard }),
    fallbackLabel: '修改身份证号'
  });
}

export function updateAdminPassword(currentPassword: string, newPassword: string, confirmPassword: string) {
  return requestJson<void>('/auth/password', {
    method: 'PUT',
    authPortal: 'admin',
    body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
    fallbackLabel: '修改密码'
  });
}
