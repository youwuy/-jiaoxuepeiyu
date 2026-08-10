import { describe, expect, it } from 'vitest';
import { normalizeAdminAcademicYears } from '../src/api/admin-settings';

describe('admin academic year normalization', () => {
  it('localizes semester codes and removes mixed-language duplicates', () => {
    const years = normalizeAdminAcademicYears([
      {
        academicYearId: 1,
        yearName: '2026-2027学年',
        semesters: [
          { semesterId: 11, academicYearId: 1, semesterName: 'FIRST', current: false },
          { semesterId: 12, academicYearId: 1, semesterName: 'SECOND', current: false },
          { semesterId: 13, academicYearId: 1, semesterName: '上学期', current: true }
        ]
      }
    ]);

    expect(years[0].semesters).toEqual([
      { semesterId: 13, academicYearId: 1, semesterName: '上学期', current: true },
      { semesterId: 12, academicYearId: 1, semesterName: '下学期', current: false }
    ]);
  });
});
