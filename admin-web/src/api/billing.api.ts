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

// In-memory mock storage
let localGrants: AdminManualGrant[] = [...mockManualGrants];

export const billingApi = {
  async getGrants(params?: { page?: number; limit?: number }): Promise<PaginatedResponse<AdminManualGrant>> {
    try {
      const res = await apiClient.get('/admin/billing/grants', { params });
      const data = res.data.data || res.data;
      const pagination = res.data.pagination || res.data.meta || {
        page: params?.page || 1,
        limit: params?.limit || 20,
        total: Array.isArray(data) ? data.length : 0,
        totalPages: 1,
      };
      return {
        data: Array.isArray(data) ? data : [],
        meta: pagination,
      };
    } catch {
      const page = params?.page || 1;
      const limit = params?.limit || 20;
      const start = (page - 1) * limit;
      return {
        data: localGrants.slice(start, start + limit),
        meta: {
          page,
          limit,
          total: localGrants.length,
          totalPages: Math.ceil(localGrants.length / limit) || 1,
        },
      };
    }
  },

  async grantPremium(payload: GrantPayload): Promise<AdminManualGrant> {
    try {
      const res = await apiClient.post('/admin/billing/grant', payload);
      return res.data.grant || res.data.data || res.data;
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
          grantedByAdminId: '15c03441-3491-48b4-bcc8-439d4dacdd9f',
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
      throw err;
    }
  },

  async revokeGrant(payload: RevokePayload): Promise<AdminManualGrant> {
    try {
      const res = await apiClient.post('/admin/billing/revoke', payload);
      return res.data.grant || res.data.data || res.data;
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
      throw err;
    }
  },
};
