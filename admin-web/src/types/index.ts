export type Role = 'USER' | 'ADMIN';

export interface ApiResponse<T> {
  statusCode: number;
  message?: string;
  data: T;
}

export interface PaginatedResponse<T> {
  data: T[];
  meta: {
    total: number;
    page: number;
    limit: number;
    totalPages: number;
  };
}

export interface AdminUserSubscriptionState {
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | string;
  expiryTime: string | null;
  productId: string | null;
  autoRenewing: boolean;
}

export interface User {
  id: string;
  username: string;
  email: string | null;
  name: string | null;
  avatar: string | null;
  role: Role;
  isActive: boolean;
  authProvider?: string;
  isEmailVerified: boolean;
  dailyAiQuota: number;
  purchasedAiQuota: number;
  purchasedChatQuota?: number;
  isPremium?: boolean;
  subscriptionState?: AdminUserSubscriptionState | null;
  createdAt: string;
  updatedAt: string;
  // BR-17.1: Zero personal medical / diet metrics
  weightKg?: null;
  targetWeightKg?: null;
  heightCm?: null;
  goal?: null;
  targetCalories?: null;
  targetProtein?: null;
  targetCarb?: null;
  targetFat?: null;
}

export interface AdminDashboardSummary {
  users: {
    total: number;
    newLast7Days: number;
    newLast30Days: number;
  };
  subscriptions: {
    activePremiums: number;
  };
  orders: {
    pending: number;
    paid: number;
  };
  revenue: {
    totalVnd: number;
  };
}

export type PaymentOrderStatus = 'PENDING' | 'PAID' | 'CANCELED' | 'CANCELLED' | 'FAILED' | 'EXPIRED';

export interface AdminPaymentOrder {
  id: string;
  orderCode: string;
  userId: string;
  amount: number;
  currency?: string;
  status: PaymentOrderStatus;
  itemSku: string;
  userNote?: string | null;
  paidAmount?: number;
  paidAt?: string | null;
  createdAt: string;
  user?: {
    email: string;
    name: string;
    username?: string;
  };
}

export interface AdminManualGrant {
  id: string;
  userId: string;
  startsAt: string;
  endsAt: string;
  reason: string;
  revokedAt?: string | null;
  revokedReason?: string | null;
  grantedByAdminId?: string;
  createdAt: string;
  user?: {
    id: string;
    email: string;
    name: string;
    username?: string;
  };
}

export type AuditLogAction =
  | 'LOGIN'
  | 'APPROVE_PAYMENT'
  | 'REJECT_PAYMENT'
  | 'GRANT_PREMIUM'
  | 'REVOKE_PREMIUM'
  | string;

export interface AdminAuditLog {
  id: string;
  adminId: string;
  adminEmail: string;
  action: AuditLogAction;
  targetType: 'User' | 'PaymentOrder' | 'ManualGrant' | string;
  targetId: string;
  before?: Record<string, any> | null;
  after?: Record<string, any> | null;
  reason?: string | null;
  ipAddress?: string | null;
  userAgent?: string | null;
  createdAt: string;
}

export interface AdminUserBillingDetails {
  user: {
    id: string;
    email: string;
    username: string;
    name: string;
    isPremium: boolean;
  };
  subscription?: AdminUserSubscriptionState | null;
  orders: AdminPaymentOrder[];
  manualGrants: AdminManualGrant[];
}
