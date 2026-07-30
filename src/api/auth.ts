import { saveAuthSession, tryRequestJson } from './http';
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
      ? { loginType: 'studentId', studentId: form.studentId, password: form.password }
      : { loginType: 'phone', phone: form.phone, password: form.password };

  const result = await tryRequestJson<LoginResult>(['/student/login', '/auth/student/login', '/login/student'], {
    method: 'POST',
    body: JSON.stringify(payload),
    fallbackLabel: '学员登录'
  });

  saveAuthSession(normalizeToken(result), result.user);
  return result;
}

export async function loginAdmin(form: AdminLoginForm): Promise<LoginResult> {
  const result = await tryRequestJson<LoginResult>(['/admin/login', '/auth/admin/login', '/login/admin'], {
    method: 'POST',
    body: JSON.stringify({
      account: form.account,
      password: form.password
    }),
    fallbackLabel: '管理端登录'
  });

  saveAuthSession(normalizeToken(result), result.user);
  return result;
}
