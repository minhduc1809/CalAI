import { apiClient } from './client';
import { AdminManualGrant, PaginatedResponse } from '../types';
import { mockManualGrants } from '../utils/mockData';

export interface GrantPayload {
  userId: string;
  days: number;
  reason: string;
}

export interface RevokePayload {
  grantId: string;
  reason: string;
}

// In-memory mock storage fallback
let localGrants: AdminManualGrant[] = [...mockManualGrants];

export const billingApi = {
  async getGrants(params?: { page?: number; limit?: number; userId?: string }): Promise<PaginatedResponse<AdminManualGrant>> {
    try {
      let res;
      try {
        res = await apiClient.get('/admin/billing/grants', { params });
      } catch (err: any) {
        if (err.response?.status === 404 && params?.userId) {
          // If direct grants endpoint is not mounted, fetch user's billing to get their grants
          const billingRes = await apiClient.get(`/admin/users/${params.userId}/billing`);
          const grants = billingRes.data?.data?.manualGrants || [];
          return {
            data: grants,
            meta: {
              page: 1,
              limit: 20,
              total: grants.length,
              totalPages: 1,
            },
          };
        }
        throw err;
      }

      const raw = res.data?.data || res.data;
      const rawItems = Array.isArray(raw) ? raw : (raw?.items || raw?.data || []);
      const pagination = raw?.pagination || res.data?.pagination || res.data?.meta || {
        page: params?.page || 1,
        limit: params?.limit || 20,
        total: rawItems.length,
        totalPages: 1,
      };

      return {
        data: rawItems,
        meta: {
          page: Number(pagination.page || 1),
          limit: Number(pagination.limit || 20),
          total: Number(pagination.total || rawItems.length),
          totalPages: Number(pagination.totalPages || 1),
        },
      };
    } catch {
      let filtered = [...localGrants];
      if (params?.userId) {
        filtered = filtered.filter((g) => g.userId === params.userId);
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

  async grantPremium(payload: GrantPayload): Promise<AdminManualGrant> {
    if (payload.days < 1 || payload.days > 90) {
      throw new Error('Số ngày cấp phải từ 1 đến 90 ngày (BR-17.3)');
    }
    if (!payload.reason || payload.reason.trim().length < 10) {
      throw new Error('Lý do cấp gói bắt buộc tối thiểu 10 ký tự');
    }

    try {
      let res;
      try {
        res = await apiClient.post('/admin/billing/grant', payload);
      } catch (err: any) {
        if (err.response?.status === 404) {
          res = await apiClient.post('/admin/billing/grants', payload);
        } else {
          throw err;
        }
      }

      const raw = res.data?.grant || res.data?.data || res.data;
      const newGrant: AdminManualGrant = {
        id: raw.id,
        userId: raw.userId || payload.userId,
        startsAt: raw.startsAt,
        endsAt: raw.endsAt,
        reason: raw.reason || payload.reason,
        revokedAt: raw.revokedAt || null,
        revokedReason: raw.revokedReason || null,
        grantedByAdminId: raw.adminId || raw.grantedByAdminId,
        createdAt: raw.createdAt || raw.startsAt,
      };
      localGrants.unshift(newGrant);
      return newGrant;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const now = new Date();
        const ends = new Date(now.getTime() + payload.days * 24 * 60 * 60 * 1000);
        const newGrant: AdminManualGrant = {
          id: `grant-${Date.now()}`,
          userId: payload.userId,
          startsAt: now.toISOString(),
          endsAt: ends.toISOString(),
          reason: payload.reason,
          revokedAt: null,
          revokedReason: null,
          grantedByAdminId: 'admin-id',
          createdAt: now.toISOString(),
          user: {
            id: payload.userId,
            email: `${payload.userId}@example.com`,
            name: `User ${payload.userId}`,
          },
        };
        localGrants.unshift(newGrant);
        return newGrant;
      }
      const msg = err.response?.data?.message || err.message || 'Lỗi cấp gói thủ công';
      throw new Error(Array.isArray(msg) ? msg.join(', ') : msg);
    }
  },

  async revokeGrant(payload: RevokePayload): Promise<AdminManualGrant> {
    if (!payload.reason || payload.reason.trim().length < 5) {
      throw new Error('Lý do thu hồi bắt buộc tối thiểu 5 ký tự');
    }

    try {
      let res;
      try {
        res = await apiClient.post('/admin/billing/revoke', payload);
      } catch (err: any) {
        if (err.response?.status === 404) {
          res = await apiClient.post(`/admin/billing/grants/${payload.grantId}/revoke`, {
            reason: payload.reason,
          });
        } else {
          throw err;
        }
      }

      const raw = res.data?.grant || res.data?.data || res.data;
      const idx = localGrants.findIndex((g) => g.id === payload.grantId);
      if (idx !== -1) {
        localGrants[idx] = {
          ...localGrants[idx],
          revokedAt: raw.revokedAt || new Date().toISOString(),
          revokedReason: payload.reason,
        };
      }
      return raw;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const idx = localGrants.findIndex((g) => g.id === payload.grantId);
        if (idx !== -1) {
          localGrants[idx] = {
            ...localGrants[idx],
            revokedAt: new Date().toISOString(),
            revokedReason: payload.reason,
          };
          return localGrants[idx];
        }
      }
      const msg = err.response?.data?.message || err.message || 'Lỗi thu hồi gói';
      throw new Error(Array.isArray(msg) ? msg.join(', ') : msg);
    }
  },
};
