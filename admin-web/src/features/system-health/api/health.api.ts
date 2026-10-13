import { api } from '../../../services/api-client';

export type Level = 'OK' | 'WARN' | 'DOWN' | 'UNKNOWN';
export interface Check { level: Level; detail: string; latencyMs?: number | null }

export interface SystemHealth {
  checkedAt: string;
  overall: Level;
  database: Check & { migrations?: number | null; lastMigration?: string | null };
  ai: Check & { calls24h: number; errors24h: number; errorRatePct: number | null };
  email: Check;
  googlePlay: Check;
  http: {
    windowMinutes: number; requests: number; avgMs: number | null; p95Ms: number | null; errors5xx24h: number;
    recentErrors: { at: string; method: string; path: string; message: string }[];
  };
  backup: { lastBackupAt: string | null; note: string };
  runtime: { version: string | null; nodeVersion: string; uptimeSeconds: number; environment: string };
}

export const healthApi = {
  get: (signal?: AbortSignal) => api.get<SystemHealth>('/admin/system/health', undefined, signal),
};
