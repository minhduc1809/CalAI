import { apiClient } from './client';
import { CheckIn, PaginatedResponse, CheckInStatus } from '../types';
import { mockCheckIns } from '../utils/mockData';

export interface CheckInQueryParams {
  userId?: string;
  status?: CheckInStatus;
  page?: number;
  limit?: number;
}

export interface ReviewCheckInPayload {
  status: CheckInStatus; // 'ACCEPTED' | 'DECLINED'
  proposedCalorieTarget?: number;
  proposedProteinTarget?: number;
  proposedCarbTarget?: number;
  proposedFatTarget?: number;
  note?: string;
}

let localCheckIns = [...mockCheckIns];

export const checkInsApi = {
  async getCheckIns(params?: CheckInQueryParams): Promise<PaginatedResponse<CheckIn>> {
    try {
      const res = await apiClient.get('/admin/checkins', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localCheckIns];
      if (params?.status) filtered = filtered.filter((c) => c.status === params.status);
      if (params?.userId) filtered = filtered.filter((c) => c.userId === params.userId);
      return {
        data: filtered,
        meta: {
          total: filtered.length,
          page: params?.page || 1,
          limit: params?.limit || 20,
          totalPages: 1,
        },
      };
    }
  },

  async getCheckInById(id: string): Promise<CheckIn> {
    try {
      const res = await apiClient.get(`/admin/checkins/${id}`);
      return res.data.data;
    } catch {
      const chk = localCheckIns.find((c) => c.id === id);
      if (!chk) throw new Error('Không tìm thấy check-in');
      return chk;
    }
  },

  async reviewCheckIn(id: string, payload: ReviewCheckInPayload): Promise<CheckIn> {
    try {
      const res = await apiClient.patch(`/admin/checkins/${id}/review`, payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localCheckIns = localCheckIns.map((c) => {
          if (c.id !== id) return c;
          return {
            ...c,
            status: payload.status,
            proposedCalorieTarget: payload.proposedCalorieTarget || c.proposedCalorieTarget,
            proposedProteinTarget: payload.proposedProteinTarget || c.proposedProteinTarget,
            proposedCarbTarget: payload.proposedCarbTarget || c.proposedCarbTarget,
            proposedFatTarget: payload.proposedFatTarget || c.proposedFatTarget,
            adminFeedback: payload.note || c.adminFeedback,
            reviewedAt: new Date().toISOString(),
          };
        });
        return localCheckIns.find((c) => c.id === id)!;
      }
      throw err;
    }
  },

  async deleteCheckIn(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/checkins/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localCheckIns = localCheckIns.filter((c) => c.id !== id);
        return { message: 'Xóa check-in thành công' };
      }
      throw err;
    }
  },
};
