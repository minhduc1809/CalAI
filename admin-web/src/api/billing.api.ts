import { apiClient } from './client';
import { usersApi } from './users.api';
import { AdminManualGrant, PaginatedResponse } from '../types';

export interface GrantPayload {
  userId: string;
  days: number;
  reason: string;
}

export interface RevokePayload {
  grantId: string;
  reason: string;
}

function normalizeGrant(grant: Record<string, any>, fallbackUserId?: string): AdminManualGrant {
  if (!grant.id || !(grant.userId ?? fallbackUserId)) {
    throw new Error('Phản hồi gói Premium không đúng định dạng');
  }

  return {
    id: String(grant.id),
    userId: String(grant.userId ?? fallbackUserId),
    startsAt: String(grant.startsAt ?? ''),
    endsAt: String(grant.endsAt ?? ''),
    reason: String(grant.reason ?? ''),
    revokedAt: grant.revokedAt ?? null,
    revokedReason: grant.revokedReason ?? null,
    grantedByAdminId: grant.grantedByAdminId ?? grant.adminId ?? grant.grantedBy,
    createdAt: String(grant.createdAt ?? grant.startsAt ?? ''),
  };
}

function extractErrorMessage(err: any, fallback: string): string {
  const message = err.response?.data?.message ?? err.message ?? fallback;
  return Array.isArray(message) ? message.join(', ') : message;
}

export const billingApi = {
  async getGrants(params: {
    page?: number;
    limit?: number;
    userId: string;
  }): Promise<PaginatedResponse<AdminManualGrant>> {
    const billing = await usersApi.getUserBilling(params.userId);
    const page = params.page ?? 1;
    const limit = params.limit ?? 20;
    const start = (page - 1) * limit;

    return {
      data: billing.manualGrants.slice(start, start + limit),
      meta: {
        page,
        limit,
        total: billing.manualGrants.length,
        totalPages: Math.ceil(billing.manualGrants.length / limit),
      },
    };
  },

  async grantPremium(payload: GrantPayload): Promise<AdminManualGrant> {
    if (!Number.isInteger(payload.days) || payload.days < 1 || payload.days > 90) {
      throw new Error('Số ngày cấp phải từ 1 đến 90 ngày (BR-17.3)');
    }
    if (!payload.reason || payload.reason.trim().length < 10) {
      throw new Error('Lý do cấp gói bắt buộc tối thiểu 10 ký tự');
    }

    try {
      const res = await apiClient.post<{ data: Record<string, any> }>(
        '/admin/billing/grants',
        payload,
      );
      return normalizeGrant(res.data.data, payload.userId);
    } catch (err: any) {
      throw new Error(extractErrorMessage(err, 'Lỗi cấp gói thủ công'));
    }
  },

  async revokeGrant(payload: RevokePayload): Promise<AdminManualGrant> {
    if (!payload.reason || payload.reason.trim().length < 5) {
      throw new Error('Lý do thu hồi bắt buộc tối thiểu 5 ký tự');
    }

    try {
      const res = await apiClient.post<{ data: Record<string, any> }>(
        `/admin/billing/grants/${encodeURIComponent(payload.grantId)}/revoke`,
        { reason: payload.reason },
      );
      return normalizeGrant(res.data.data);
    } catch (err: any) {
      throw new Error(extractErrorMessage(err, 'Lỗi thu hồi gói'));
    }
  },
};
