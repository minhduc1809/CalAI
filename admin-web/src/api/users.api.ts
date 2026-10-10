import { apiClient } from './client';
import { User, PaginatedResponse, Role, AdminUserBillingDetails } from '../types';

export interface UserQueryParams {
  search?: string;
  role?: Role;
  isPremium?: string;
  page?: number;
  limit?: number;
}

interface PaginatedItems<T> {
  items: T[];
  pagination?: {
    page?: number;
    limit?: number;
    total?: number;
    totalPages?: number;
  };
}

function normalizeUser(user: Record<string, any>): User {
  if (!user.id || !user.role || typeof user.isActive !== 'boolean' || !user.createdAt) {
    throw new Error('Phản hồi người dùng không đúng định dạng');
  }

  const subscriptionState = user.subscriptionState ?? null;
  const isPremium =
    user.isPremium !== undefined
      ? Boolean(user.isPremium)
      : Boolean(
          subscriptionState?.expiryTime &&
            new Date(subscriptionState.expiryTime).getTime() > Date.now(),
        );

  return {
    id: String(user.id),
    username: user.username ?? user.email?.split('@')[0] ?? String(user.id),
    email: user.email ?? null,
    name: user.name ?? null,
    avatar: user.avatar ?? null,
    role: user.role,
    isActive: user.isActive,
    authProvider: user.authProvider,
    isEmailVerified: user.isEmailVerified,
    dailyAiQuota: user.dailyAiQuota,
    purchasedAiQuota: user.purchasedAiQuota,
    purchasedChatQuota: user.purchasedChatQuota,
    isPremium,
    subscriptionState,
    createdAt: String(user.createdAt),
    updatedAt: user.updatedAt,
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

function normalizeBillingOrder(order: Record<string, any>, userId: string) {
  const amount = Number(order.amount);
  if (!order.id || !order.createdAt || !Number.isFinite(amount)) {
    throw new Error('Phản hồi lịch sử đơn thanh toán không đúng định dạng');
  }

  return {
    id: String(order.id),
    orderCode: String(order.orderCode ?? order.code ?? order.id),
    userId: String(order.userId ?? userId),
    amount,
    status: order.status === 'CANCELED' ? 'CANCELED' : order.status,
    itemSku: order.itemSku ?? order.productId,
    createdAt: String(order.createdAt),
    paidAt: order.paidAt ?? null,
  };
}

export const usersApi = {
  async getUsers(params?: UserQueryParams): Promise<PaginatedResponse<User>> {
    const res = await apiClient.get<{ data: PaginatedItems<Record<string, any>> }>(
      '/admin/users',
      { params },
    );
    const result = res.data.data;
    const users = result.items.map(normalizeUser);
    const pagination = result.pagination ?? {};

    return {
      data: users,
      meta: {
        page: pagination.page ?? params?.page ?? 1,
        limit: pagination.limit ?? params?.limit ?? 20,
        total: pagination.total ?? users.length,
        totalPages: pagination.totalPages ?? (users.length > 0 ? 1 : 0),
      },
    };
  },

  async getUserBilling(id: string): Promise<AdminUserBillingDetails> {
    const res = await apiClient.get<{ data: Record<string, any> }>(
      `/admin/users/${encodeURIComponent(id)}/billing`,
    );
    const raw = res.data.data;
    const user = raw.user ?? raw;
    const subscription = raw.subscription ?? raw.subscriptionState ?? null;
    const orders = (raw.orders ?? raw.paymentOrders ?? []).map((order: Record<string, any>) =>
      normalizeBillingOrder(order, id),
    );
    const manualGrants = (raw.manualGrants ?? []).map((grant: Record<string, any>) => {
      if (!grant.id || !grant.startsAt || !grant.endsAt || !grant.reason) {
        throw new Error('Phản hồi lịch sử cấp Premium không đúng định dạng');
      }
      return {
        id: String(grant.id),
        userId: String(grant.userId ?? id),
        startsAt: String(grant.startsAt),
        endsAt: String(grant.endsAt),
        reason: String(grant.reason),
        revokedAt: grant.revokedAt ?? null,
        revokedReason: grant.revokedReason ?? null,
        grantedByAdminId: grant.grantedByAdminId ?? grant.adminId ?? undefined,
        createdAt: String(grant.createdAt ?? grant.startsAt),
      };
    });
    const email = user.email ?? '';

    return {
      user: {
        id: String(user.id ?? id),
        email,
        username: user.username ?? email.split('@')[0] ?? String(user.id ?? id),
        name: user.name ?? email,
        isPremium: Boolean(
          user.isPremium ??
            (subscription?.expiryTime &&
              new Date(subscription.expiryTime).getTime() > Date.now()),
        ),
      },
      subscription,
      orders,
      manualGrants,
    };
  },
};
