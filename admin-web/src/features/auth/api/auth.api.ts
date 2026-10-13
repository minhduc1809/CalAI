import { api } from '../../../services/api-client';
import type { AdminInfo } from '../../../services/auth';

export interface TotpStatus { enabled: boolean; backupCodesLeft: number }
export interface TotpSetup { secret: string; otpauthUrl: string }
export interface SessionInfo { id: string; deviceName: string | null; createdAt: string; lastUsedAt: string }

export const authApi = {
  login: (email: string, password: string, totpCode?: string) =>
    api.post<{ accessToken: string; admin: AdminInfo }>('/admin/auth/login', { email, password, totpCode }),
  totpStatus: () => api.get<TotpStatus>('/admin/auth/totp/status'),
  totpSetup: () => api.post<TotpSetup>('/admin/auth/totp/setup'),
  totpEnable: (code: string) => api.post<{ backupCodes: string[] }>('/admin/auth/totp/enable', { code }),
  totpDisable: (password: string, code: string) => api.post('/admin/auth/totp/disable', { password, code }),
  changePassword: (oldPassword: string, newPassword: string) => api.patch('/auth/change-password', { oldPassword, newPassword }),
  sessions: () => api.get<SessionInfo[]>('/auth/sessions'),
  revokeSession: (id: string) => api.delete(`/auth/sessions/${id}`),
};
