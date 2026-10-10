import { apiClient } from './client';
import { AdminAuditLog, PaginatedResponse } from '../types';
import { mockAuditLogs } from '../utils/mockData';

export interface AuditLogQueryParams {
  page?: number;
  limit?: number;
  adminId?: string;
  action?: string;
  targetType?: string;
}

// In-memory mock storage fallback
let localLogs: AdminAuditLog[] = [...mockAuditLogs];

function normalizeLog(l: any): AdminAuditLog {
  return {
    id: l.id,
    adminId: l.adminId || '',
    adminEmail: l.adminEmail || 'admin@calai.com',
    action: l.action || 'ACTIVITY',
    targetType: l.targetType || 'System',
    targetId: l.targetId || '',
    before: l.before || null,
    after: l.after || null,
    reason: l.reason || null,
    ipAddress: l.ipAddress || null,
    userAgent: l.userAgent || null,
    createdAt: l.createdAt || new Date().toISOString(),
  };
}

export const auditLogsApi = {
  async getAuditLogs(params?: AuditLogQueryParams): Promise<PaginatedResponse<AdminAuditLog>> {
    try {
      const apiParams: any = { ...params };
      if (apiParams.action === 'ALL') delete apiParams.action;
      if (apiParams.targetType === 'ALL') delete apiParams.targetType;

      const res = await apiClient.get('/admin/audit-logs', { params: apiParams });
      const raw = res.data?.data || res.data;
      const rawItems = Array.isArray(raw) ? raw : (raw?.items || raw?.data || []);
      const pagination = raw?.pagination || res.data?.pagination || res.data?.meta || {
        page: params?.page || 1,
        limit: params?.limit || 20,
        total: rawItems.length,
        totalPages: 1,
      };

      const normalizedItems = rawItems.map(normalizeLog);

      return {
        data: normalizedItems,
        meta: {
          page: Number(pagination.page || 1),
          limit: Number(pagination.limit || 20),
          total: Number(pagination.total || normalizedItems.length),
          totalPages: Number(pagination.totalPages || 1),
        },
      };
    } catch {
      let filtered = [...localLogs];

      if (params?.action && params.action !== 'ALL') {
        filtered = filtered.filter((l) => l.action === params.action);
      }
      if (params?.targetType && params.targetType !== 'ALL') {
        filtered = filtered.filter((l) => l.targetType === params.targetType);
      }
      if (params?.adminId) {
        filtered = filtered.filter((l) => l.adminId === params.adminId);
      }

      const page = params?.page || 1;
      const limit = params?.limit || 20;
      const start = (page - 1) * limit;

      return {
        data: filtered.slice(start, start + limit),
        meta: {
          page,
          limit,
          total: filtered.length,
          totalPages: Math.ceil(filtered.length / limit) || 1,
        },
      };
    }
  },
};
