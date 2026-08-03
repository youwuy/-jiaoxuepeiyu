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

export const mockScoreParts: ScorePart[] = [];

export const mockMessages: StudentMessage[] = [];

export const mockArchives: TrainingArchive[] = [];
