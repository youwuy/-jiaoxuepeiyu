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
  activeRoomId?: number;
  latestAttemptId?: number;
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

export const mockTrainings: StudentTraining[] = [];
