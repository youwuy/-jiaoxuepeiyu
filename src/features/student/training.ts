export type TrainingModeFilter = 'all' | 'single' | 'team';
export type TrainingMode = 'single' | 'team';
export type TrainingStatus = 'available' | 'notStarted' | 'completed';
export type TrainingCategory = 'practice' | 'exam';

export interface StudentTrainingStep {
  id: number;
  title: string;
  mode: TrainingMode;
  action?: 'start' | 'team' | 'retry' | 'score';
}

export interface StudentTraining {
  id: number;
  title: string;
  mode: TrainingMode;
  status: TrainingStatus;
  category?: TrainingCategory;
  courseName?: string;
  term?: string;
  startAt?: string;
  deadline?: string;
  topicCount?: number;
  countdown?: string;
  attempts?: number;
  bestScore?: number;
  roles?: string[];
  steps?: StudentTrainingStep[];
}

export interface TrainingFilters {
  mode?: TrainingModeFilter;
  status?: TrainingStatus | 'all';
  keyword?: string;
}

export function filterTrainings<T extends Pick<StudentTraining, 'mode' | 'status' | 'title'>>(
  trainings: T[],
  filters: TrainingFilters
): T[] {
  const mode = filters.mode ?? 'all';
  const status = filters.status ?? 'all';
  const keyword = filters.keyword?.trim().toLowerCase() ?? '';

  return trainings.filter((training) => {
    const matchesMode = mode === 'all' || training.mode === mode;
    const matchesStatus = status === 'all' || training.status === status;
    const matchesKeyword = !keyword || training.title.toLowerCase().includes(keyword);
    return matchesMode && matchesStatus && matchesKeyword;
  });
}

export const mockTrainings: StudentTraining[] = [
  {
    id: 3001,
    title: '自动扶梯伤害任务演练',
    mode: 'single',
    status: 'available',
    category: 'practice',
    courseName: '城市轨道交通安全',
    term: '2024-2025学年 下学期',
    startAt: '2025-03-10',
    deadline: '2025-03-17',
    topicCount: 3,
    countdown: '剩3天6小时',
    steps: [
      { id: 1, title: '车站值班员基础操作', mode: 'single', action: 'start' },
      { id: 2, title: '信号设备识别与操作', mode: 'team', action: 'team' },
      { id: 3, title: '接发列车作业流程', mode: 'single', action: 'retry' }
    ]
  },
  {
    id: 3002,
    title: '期末考试1',
    mode: 'team',
    status: 'available',
    category: 'exam',
    courseName: '轨道交通信号系统',
    term: '2024-2025学年 下学期',
    startAt: '2025-03-12',
    deadline: '2025-03-19',
    topicCount: 1,
    countdown: '剩5天12小时',
    steps: [{ id: 1, title: '信号机故障分析', mode: 'team', action: 'start' }]
  },
  {
    id: 3003,
    title: '期末考试2',
    mode: 'team',
    status: 'available',
    category: 'exam',
    courseName: '轨道交通信号系统',
    term: '2024-2025学年 下学期',
    startAt: '2025-03-12',
    deadline: '2025-03-19',
    topicCount: 1,
    countdown: '剩5天12小时',
    steps: [{ id: 1, title: '信号机故障分析', mode: 'team', action: 'start' }]
  },
  {
    id: 3004,
    title: '垂直电梯困人任务演练',
    mode: 'single',
    status: 'notStarted',
    category: 'practice',
    courseName: '城市轨道交通安全',
    term: '2024-2025学年 下学期',
    startAt: '2025-03-22',
    deadline: '2025-03-28',
    topicCount: 1,
    countdown: '03-22 09:00 开放',
    steps: [{ id: 1, title: '列车启动与加速操作', mode: 'single', action: 'start' }]
  },
  {
    id: 3005,
    title: '城轨车辆检修实训',
    mode: 'team',
    status: 'completed',
    category: 'practice',
    courseName: '城轨车辆构造与维护',
    term: '2024-2025学年 上学期',
    startAt: '2025-03-01',
    deadline: '2025-03-08',
    topicCount: 2,
    bestScore: 88,
    steps: [
      { id: 1, title: '车辆转向架检修', mode: 'single', action: 'score' },
      { id: 2, title: '制动系统检修', mode: 'team', action: 'score' }
    ]
  },
  {
    id: 3006,
    title: '轨道交通应急演练实训',
    mode: 'team',
    status: 'completed',
    category: 'practice',
    courseName: '行车组织基础',
    term: '2024-2025学年 上学期',
    startAt: '2025-02-20',
    deadline: '2025-02-28',
    topicCount: 2,
    steps: [
      { id: 1, title: '火灾应急疏散', mode: 'team', action: 'score' },
      { id: 2, title: '列车脱轨应急处置', mode: 'single', action: 'score' }
    ]
  }
];
