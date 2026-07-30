import { requestJson, saveAuthSession } from './http';
import type { AdminLoginForm, StudentLoginForm, StudentLoginMode } from '../features/auth/validation';

export interface LoginResult {
  token?: string;
  accessToken?: string;
  user?: unknown;
}

function normalizeToken(result: LoginResult): string {
  return result.token || result.accessToken || '';
}

export async function loginStudent(mode: StudentLoginMode, form: StudentLoginForm): Promise<LoginResult> {
  const payload =
    mode === 'studentId'
      ? { loginType: 'studentNo', account: form.studentId, password: form.password }
      : { loginType: 'phone', account: form.phone, password: form.password };

  const result = await requestJson<LoginResult>('/auth/student/login', {
    method: 'POST',
    body: JSON.stringify(payload),
    fallbackLabel: '学员登录'
  });

  saveAuthSession(normalizeToken(result), result.user);
  return result;
}

export async function loginAdmin(form: AdminLoginForm): Promise<LoginResult> {
  const result = await requestJson<LoginResult>('/auth/admin/login', {
    method: 'POST',
    body: JSON.stringify({
      loginType: 'username',
      account: form.account,
      password: form.password
    }),
    fallbackLabel: '管理端登录'
  });

  saveAuthSession(normalizeToken(result), result.user);
  return result;
}
