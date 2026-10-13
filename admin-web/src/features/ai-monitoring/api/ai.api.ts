import { api } from '../../../services/api-client';

export interface AiDaily {
  range: { from: string; to: string; days: number };
  days: { date: string; calls: number; errors: number; errorRatePct: number | null; costUsd: number; tokens: number }[];
  todayCostUsd: number | null;
  pricing: { configured: boolean; note: string };
}

export interface AiUsageMonth {
  month: string;
  totals: { calls: number; errors: number; errorRatePct: number; callsWithoutUsage: number; promptTokens: number; outputTokens: number; costUsd: number };
  byFeature: { feature: string; calls: number; errors: number; errorRatePct: number; promptTokens: number; outputTokens: number; costUsd: number; avgLatencyMs: number }[];
  perUserMonthlyCostUsd: Record<string, number | null>;
}

export const aiApi = {
  daily: (from: string, to: string, signal?: AbortSignal) => api.get<AiDaily>('/admin/ai/usage/daily', { from, to }, signal),
  month: (month: string, signal?: AbortSignal) => api.get<AiUsageMonth>('/admin/ai/usage', { month }, signal),
};
