export type StudentLoginMode = 'studentId' | 'phone';

export interface StudentLoginForm {
  studentId?: string;
  phone?: string;
  password?: string;
}

export interface AdminLoginForm {
  account?: string;
  password?: string;
}

export type StudentLoginErrors = Partial<Record<keyof StudentLoginForm, string>>;
export type AdminLoginErrors = Partial<Record<keyof AdminLoginForm, string>>;

const requiredMessage = '不能为空';

export function normalizePhoneInput(value: string): string {
  return value.replace(/\D/g, '').slice(0, 11);
}

export function normalizeStudentIdInput(value: string): string {
  return value.replace(/[^a-zA-Z0-9]/g, '');
}

export function normalizePasswordInput(value: string): string {
  return value.slice(0, 64);
}

export function validateStudentLogin(mode: StudentLoginMode, form: StudentLoginForm): StudentLoginErrors {
  const errors: StudentLoginErrors = {};
  const password = form.password?.trim() ?? '';

  if (mode === 'studentId') {
    const studentId = form.studentId?.trim() ?? '';
    if (!studentId) {
      errors.studentId = requiredMessage;
    }
  }

  if (mode === 'phone') {
    const phone = form.phone?.trim() ?? '';
    if (!phone) {
      errors.phone = requiredMessage;
    } else if (!/^\d{11}$/.test(phone)) {
      errors.phone = '手机号格式不正确';
    }
  }

  if (!password) {
    errors.password = requiredMessage;
  }

  return errors;
}

export function validateAdminLogin(form: AdminLoginForm): AdminLoginErrors {
  const errors: AdminLoginErrors = {};

  if (!form.account?.trim()) {
    errors.account = requiredMessage;
  }

  if (!form.password?.trim()) {
    errors.password = requiredMessage;
  }

  return errors;
}
