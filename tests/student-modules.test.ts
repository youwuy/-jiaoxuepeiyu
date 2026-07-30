import { describe, expect, it } from 'vitest';
import { calculateWeightedScore, summarizeUnreadMessages } from '../src/features/student/profile';
import { filterResources } from '../src/features/student/resources';
import { filterTrainings } from '../src/features/student/training';

describe('student training filters', () => {
  const trainings = [
    { id: 1, title: '站台门故障处置', mode: 'single' as const, status: 'available' as const },
    { id: 2, title: '列车折返协同', mode: 'team' as const, status: 'notStarted' as const },
    { id: 3, title: '突发客流组织', mode: 'team' as const, status: 'completed' as const }
  ];

  it('filters trainings by mode and keeps all for all mode', () => {
    expect(filterTrainings(trainings, { mode: 'team' }).map((item) => item.id)).toEqual([2, 3]);
    expect(filterTrainings(trainings, { mode: 'all' })).toHaveLength(3);
  });

  it('filters trainings by keyword and status', () => {
    expect(filterTrainings(trainings, { keyword: '折返', status: 'notStarted' }).map((item) => item.id)).toEqual([2]);
  });
});

describe('student resources filters', () => {
  const resources = [
    { id: 1, title: '城轨概论课件', category: '课程资源', type: 'PPT' },
    { id: 2, title: '车站布局视频', category: '实训资源', type: '视频' },
    { id: 3, title: '信号系统图册', category: '课程资源', type: '图片' }
  ];

  it('filters resources by keyword and category', () => {
    expect(filterResources(resources, { keyword: '车站', category: '实训资源' }).map((item) => item.id)).toEqual([2]);
  });

  it('returns all resources for blank filters', () => {
    expect(filterResources(resources, { keyword: '', category: '全部' })).toHaveLength(3);
  });
});

describe('student profile calculations', () => {
  it('calculates weighted score with one decimal place', () => {
    const score = calculateWeightedScore([
      { label: '课件完成度', score: 92, weight: 0.2 },
      { label: '课程作业', score: 86, weight: 0.3 },
      { label: '实训练习', score: 90, weight: 0.3 },
      { label: '考试', score: 88, weight: 0.2 }
    ]);

    expect(score).toBe(88.8);
  });

  it('counts only unread messages', () => {
    expect(
      summarizeUnreadMessages([
        { id: 1, title: '课程通知', unread: true },
        { id: 2, title: '资源上新', unread: false },
        { id: 3, title: '实训通知', unread: true }
      ])
    ).toBe(2);
  });
});
