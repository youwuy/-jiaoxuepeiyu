export type TrainingModeFilter = 'all' | 'single' | 'team';
export type TrainingMode = 'single' | 'team';
export type TrainingStatus = 'available' | 'notStarted' | 'completed';

export interface StudentTraining {
  id: number;
  title: string;
  mode: TrainingMode;
  status: TrainingStatus;
  courseName?: string;
  deadline?: string;
  attempts?: number;
  bestScore?: number;
  roles?: string[];
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
    title: '站台门故障处置',
    mode: 'single',
    status: 'available',
    courseName: '城市轨道交通概论',
    deadline: '2026-08-15',
    attempts: 2,
    bestScore: 91
  },
  {
    id: 3002,
    title: '列车折返协同',
    mode: 'team',
    status: 'notStarted',
    courseName: '轨道交通信号系统',
    deadline: '2026-09-18',
    attempts: 0,
    roles: ['值班员', '司机', '调度员']
  },
  {
    id: 3003,
    title: '突发客流组织',
    mode: 'team',
    status: 'completed',
    courseName: '行车组织基础',
    deadline: '2026-06-20',
    attempts: 3,
    bestScore: 88,
    roles: ['站务员', '值班站长']
  }
];
