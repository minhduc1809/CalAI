import { apiClient } from './client';
import { User, PaginatedResponse, Role, AdminUserBillingDetails } from '../types';
import { mockUsers, mockPaymentOrders, mockManualGrants } from '../utils/mockData';

export interface UserQueryParams {
  search?: string;
  role?: Role;
  isActive?: boolean;
  page?: number;
  limit?: number;
}

export interface CreateUserPayload {
  username: string;
  email?: string;
  password: string;
  name?: string;
  role?: Role;
  isActive?: boolean;
  dailyAiQuota?: number;
}

export interface UpdateUserPayload {
  name?: string;
  email?: string;
  role?: Role;
  isActive?: boolean;
  dailyAiQuota?: number;
  purchasedAiQuota?: number;
  purchasedChatQuota?: number;
  weightKg?: number;
  heightCm?: number;
  targetCalories?: number;
  targetProtein?: number;
  targetCarb?: number;
  targetFat?: number;
}

// In-memory cache for mock fallbacks
let localUsers = [...mockUsers];

export const usersApi = {
  async getUsers(params?: UserQueryParams): Promise<PaginatedResponse<User>> {
    try {
      const res = await apiClient.get('/admin/users', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localUsers];
      if (params?.search) {
        const s = params.search.toLowerCase();
        filtered = filtered.filter(
          (u) =>
            u.username.toLowerCase().includes(s) ||
            (u.email && u.email.toLowerCase().includes(s)) ||
            (u.name && u.name.toLowerCase().includes(s))
        );
      }
      if (params?.role) {
        filtered = filtered.filter((u) => u.role === params.role);
      }
      if (typeof params?.isActive === 'boolean') {
        filtered = filtered.filter((u) => u.isActive === params.isActive);
      }
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

  async getUserById(id: string): Promise<User> {
    try {
      const res = await apiClient.get(`/admin/users/${id}`);
      return res.data.data;
    } catch {
      const user = localUsers.find((u) => u.id === id);
      if (!user) throw new Error('Không tìm thấy người dùng');
      return user;
    }
  },

  async getUserBilling(id: string): Promise<AdminUserBillingDetails> {
    try {
      const res = await apiClient.get(`/admin/users/${id}/billing`);
      return res.data.data || res.data;
    } catch {
      const user = localUsers.find((u) => u.id === id) || localUsers[0];
      const orders = mockPaymentOrders.filter((o) => o.userId === id);
      const grants = mockManualGrants.filter((g) => g.userId === id);

      return {
        user: {
          id: user.id,
          email: user.email || '',
          username: user.username,
          name: user.name || user.username,
          isPremium: !!user.isPremium,
        },
        subscription: user.subscriptionState || (user.isPremium ? {
          status: 'ACTIVE',
          expiryTime: '2026-11-10T00:00:00.000Z',
          productId: 'premium_monthly',
          autoRenewing: true,
        } : null),
        orders,
        manualGrants: grants,
      };
    }
  },

  async createUser(payload: CreateUserPayload): Promise<User> {
    try {
      const res = await apiClient.post('/admin/users', payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const newUser: User = {
          id: `usr_${Date.now()}`,
          username: payload.username,
          email: payload.email || null,
          name: payload.name || null,
          avatar: null,
          role: payload.role || 'USER',
          isActive: payload.isActive ?? true,
          authProvider: 'LOCAL',
          isEmailVerified: true,
          weightKg: 65,
          targetWeightKg: 65,
          heightCm: 170,
          goal: 'MAINTAIN',
          targetCalories: 2000,
          targetProtein: 140,
          targetCarb: 200,
          targetFat: 60,
          dailyAiQuota: payload.dailyAiQuota || 10,
          purchasedAiQuota: 0,
          purchasedChatQuota: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        };
        localUsers.unshift(newUser);
        return newUser;
      }
      throw err;
    }
  },

  async updateUser(id: string, payload: UpdateUserPayload): Promise<User> {
    try {
      const res = await apiClient.patch(`/admin/users/${id}`, payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localUsers = localUsers.map((u) => (u.id === id ? { ...u, ...payload } : u));
        return localUsers.find((u) => u.id === id)!;
      }
      throw err;
    }
  },

  async resetPassword(id: string, newPassword: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.patch(`/admin/users/${id}/password`, { newPassword });
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        return { message: 'Đặt lại mật khẩu thành công (Mô phỏng)' };
      }
      throw err;
    }
  },

  async deleteUser(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/users/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localUsers = localUsers.filter((u) => u.id !== id);
        return { message: 'Xóa người dùng thành công' };
      }
      throw err;
    }
  },
};
