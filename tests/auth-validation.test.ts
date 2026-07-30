import { describe, expect, it } from 'vitest';
import {
  normalizeStudentIdInput,
  normalizePhoneInput,
  validateAdminLogin,
  validateStudentLogin
} from '../src/features/auth/validation';

describe('student login validation', () => {
  it('accepts student id login when student id and password are valid', () => {
    expect(validateStudentLogin('studentId', { studentId: 'A20260001', password: 'abc123' })).toEqual({});
  });

  it('rejects an empty student id and password', () => {
    expect(validateStudentLogin('studentId', { studentId: '', password: '' })).toEqual({
      studentId: '不能为空',
      password: '不能为空'
    });
  });

  it('rejects an invalid phone number', () => {
    expect(validateStudentLogin('phone', { phone: '1380013800', password: 'abc123' })).toEqual({
      phone: '手机号格式不正确'
    });
  });

  it('keeps only digits for phone input and limits to 11 characters', () => {
    expect(normalizePhoneInput('13a80013800099')).toBe('13800138000');
  });

  it('keeps only letters and digits for student id input', () => {
    expect(normalizeStudentIdInput('A-2026_001')).toBe('A2026001');
  });
});

describe('admin login validation', () => {
  it('requires both account and password', () => {
    expect(validateAdminLogin({ account: '', password: '' })).toEqual({
      account: '不能为空',
      password: '不能为空'
    });
  });

  it('accepts admin credentials when account and password are present', () => {
    expect(validateAdminLogin({ account: 'admin', password: 'abc123' })).toEqual({});
  });
});
