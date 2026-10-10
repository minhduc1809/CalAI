import { apiClient } from './client';
import { User, PaginatedResponse, Role, AdminUserBillingDetails } from '../types';
import { mockUsers, mockPaymentOrders, mockManualGrants } from '../utils/mockData';

export interface UserQueryParams {
  search?: string;
  role?: Role;
  isPremium?: string;
  page?: number;
  limit?: number;
}

// In-memory cache for mock fallbacks
let localUsers = [...mockUsers];

function normalizeUser(u: any): User {
  const isPremium =
    u.isPremium !== undefined
      ? Boolean(u.isPremium)
      : Boolean(u.subscriptionState?.expiryTime && new Date(u.subscriptionState.expiryTime) > new Date());

  return {
    id: u.id,
    username: u.username || u.email?.split('@')[0] || 'user',
    email: u.email || null,
    name: u.name || null,
    avatar: u.avatar || null,
    role: u.role || 'USER',
    isActive: u.isActive !== undefined ? u.isActive : true,
    authProvider: u.authProvider || 'LOCAL',
    isEmailVerified: u.isEmailVerified !== undefined ? u.isEmailVerified : true,
    dailyAiQuota: u.dailyAiQuota ?? 10,
    purchasedAiQuota: u.purchasedAiQuota ?? 0,
    purchasedChatQuota: u.purchasedChatQuota ?? 0,
    isPremium,
    subscriptionState: u.subscriptionState || null,
    createdAt: u.createdAt || new Date().toISOString(),
    updatedAt: u.updatedAt || new Date().toISOString(),
    // Strictly preserve privacy (BR-17.1)
    weightKg: null,
    targetWeightKg: null,
    heightCm: null,
    goal: null,
    targetCalories: null,
    targetProtein: null,
    targetCarb: null,
    targetFat: null,
  };
}

export const usersApi = {
  async getUsers(params?: UserQueryParams): Promise<PaginatedResponse<User>> {
    try {
      const apiParams: any = { ...params };
      const res = await apiClient.get('/admin/users', { params: apiParams });
      const raw = res.data?.data || res.data;
      const rawItems = Array.isArray(raw) ? raw : (raw?.items || raw?.data || []);
      const pagination = raw?.pagination || res.data?.pagination || res.data?.meta || {
        page: params?.page || 1,
        limit: params?.limit || 20,
        total: rawItems.length,
        totalPages: 1,
      };

      const items = rawItems.map(normalizeUser);

      return {
        data: items,
        meta: {
          page: Number(pagination.page || 1),
          limit: Number(pagination.limit || 20),
          total: Number(pagination.total || items.length),
          totalPages: Number(pagination.totalPages || 1),
        },
      };
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
      if (params?.isPremium === 'true') {
        filtered = filtered.filter((u) => u.isPremium);
      } else if (params?.isPremium === 'false') {
        filtered = filtered.filter((u) => !u.isPremium);
      }

      const page = params?.page || 1;
      const limit = params?.limit || 20;
      const start = (page - 1) * limit;

      return {
        data: filtered.slice(start, start + limit),
        meta: {
          total: filtered.length,
          page,
          limit,
          totalPages: Math.ceil(filtered.length / limit) || 1,
        },
      };
    }
  },

  async getUserBilling(id: string): Promise<AdminUserBillingDetails> {
    try {
      const res = await apiClient.get(`/admin/users/${id}/billing`);
      const raw = res.data?.data || res.data;

      const user = raw.user || raw;
      const subscription = raw.subscription || user.subscriptionState || null;
      const orders = (raw.orders || raw.paymentOrders || []).map((o: any) => ({
        id: o.id,
        orderCode: o.orderCode || o.code || o.id.slice(0, 8),
        userId: o.userId || id,
        amount: Number(o.amount || 0),
        status: o.status === 'CANCELED' ? 'CANCELLED' : o.status,
        itemSku: o.itemSku || o.productId || 'premium_1m',
        createdAt: o.createdAt,
        paidAt: o.paidAt || null,
      }));

      const manualGrants = (raw.manualGrants || []).map((g: any) => ({
        id: g.id,
        userId: g.userId || id,
        startsAt: g.startsAt,
        endsAt: g.endsAt,
        reason: g.reason,
        revokedAt: g.revokedAt || null,
        revokedReason: g.revokedReason || null,
        createdAt: g.createdAt || g.startsAt,
      }));

      return {
        user: {
          id: user.id,
          email: user.email || '',
          username: user.username || user.email?.split('@')[0] || 'user',
          name: user.name || user.username || 'User',
          isPremium: Boolean(
            subscription?.status === 'ACTIVE' ||
            (subscription?.expiryTime && new Date(subscription.expiryTime) > new Date())
          ),
        },
        subscription,
        orders,
        manualGrants,
      };
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
};
