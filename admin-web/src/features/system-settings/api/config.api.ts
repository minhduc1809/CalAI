import { api } from '../../../services/api-client';

export interface ConfigItem {
  key: string; group: string; type: 'int' | 'float' | 'bool'; unit: string | null; description: string;
  min: number | null; max: number | null; default: number | boolean; value: number | boolean;
  isOverridden: boolean; isSafetyCritical: boolean; updatedAt: string | null; updatedBy: string | null;
}
export interface ConfigList { configVersion: number; items: ConfigItem[] }
export interface HistoryRow {
  id: string; key: string; action: 'SET' | 'RESET' | 'RESTORE'; oldValue: unknown; newValue: unknown;
  adminEmail: string | null; reason: string; configVersion: number; createdAt: string;
}

interface Auth { reason: string; password?: string }

export const configApi = {
  list: (signal?: AbortSignal) => api.get<ConfigList>('/admin/config', undefined, signal),
  history: (key: string) => api.get<{ items: HistoryRow[] }>('/admin/config/history', { key }),
  set: (key: string, value: number | boolean, a: Auth) => api.patch(`/admin/config/${key}`, { value, ...a }),
  reset: (key: string, a: Auth) => api.delete(`/admin/config/${key}`, a),
  restore: (historyId: string, a: Auth) => api.post(`/admin/config/restore/${historyId}`, a),
};
