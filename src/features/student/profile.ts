export interface ScorePart {
  label: string;
  score: number;
  weight: number;
}

export interface SemesterScore {
  academicTerm: string;
  coursewareLearningScore: number;
  trainingPracticeScore: number;
  courseAssignmentScore: number;
  examScore: number;
  coursewareWeight: number;
  trainingPracticeWeight: number;
  assignmentWeight: number;
  examWeight: number;
  comprehensiveScore: number;
}

export interface StudentMessage {
  id: number;
  title: string;
  unread: boolean;
  type?: string;
  content?: string;
  time?: string;
}

export interface TrainingArchive {
  id: number;
  title: string;
  score: number;
  duration: string;
  finishedAt: string;
  mode?: string;
  role?: string;
  submitType?: string;
  teamScore?: number;
}

export interface TrainingArchiveStep {
  id: number;
  name: string;
  expected: string;
  actual: string;
  score: number;
  durationSeconds: number;
  videoStartSecond?: number;
}

export interface TrainingArchiveDetail extends TrainingArchive {
  studentName?: string;
  studentNo?: string;
  className?: string;
  recordingUrl?: string;
  steps: TrainingArchiveStep[];
}

export function calculateWeightedScore(parts: ScorePart[]): number {
  const total = parts.reduce((sum, part) => sum + part.score * part.weight, 0);
  return Number(total.toFixed(1));
}

export function summarizeUnreadMessages<T extends Pick<StudentMessage, 'unread'>>(messages: T[]): number {
  return messages.filter((message) => message.unread).length;
}

export const mockScoreParts: ScorePart[] = [
  { label: '课件完成度', score: 92, weight: 0.2 },
  { label: '课程作业', score: 86, weight: 0.3 },
  { label: '实训练习', score: 90, weight: 0.3 },
  { label: '考试', score: 88, weight: 0.2 }
];

export const mockMessages: StudentMessage[] = [
  {
    id: 5001,
    title: '城市轨道交通概论发布了新的课程作业',
    unread: true,
    type: '课程通知',
    time: '2026-07-30 09:20'
  },
  {
    id: 5002,
    title: '公开资源库上新：实训操作规范手册',
    unread: false,
    type: '资源上新',
    time: '2026-07-29 16:30'
  },
  {
    id: 5003,
    title: '站台门故障处置实训成绩已更新',
    unread: true,
    type: '实训通知',
    time: '2026-07-28 18:05'
  }
];

export const mockArchives: TrainingArchive[] = [
  {
    id: 6001,
    title: '站台门故障处置',
    score: 91,
    duration: '00:36:18',
    finishedAt: '2026-07-26 11:24'
  },
  {
    id: 6002,
    title: '突发客流组织',
    score: 88,
    duration: '00:42:06',
    finishedAt: '2026-06-18 15:12'
  }
];
