import { request } from './request';

export interface TrendPoint {
  label: string;
  value: number;
}

export interface SkillScore {
  skillId: string;
  skillName: string;
  avgScore: number;
  interviewCount: number;
  fullMark: number;
}

export interface WeakCategory {
  category: string;
  avgScore: number;
  questionCount: number;
  interviewCount: number;
}

export interface InterviewDashboardData {
  interviewCountTrend: TrendPoint[];
  skillScores: SkillScore[];
  scoreTrend: TrendPoint[];
  weakCategories: WeakCategory[];
}

export type TrendPeriod = 'week' | 'month';

export const dashboardApi = {
  async getDashboard(period: TrendPeriod = 'week'): Promise<InterviewDashboardData> {
    return request.get<InterviewDashboardData>('/api/interview/dashboard', {
      params: { period },
    });
  },
};
