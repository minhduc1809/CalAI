export interface SubscriptionInfo {
  productId: string;
  status: string;
  expiryTime: string;
  autoRenewing: boolean;
  isTrial: boolean;
}

export interface UserRow {
  id: string;
  email: string | null;
  username: string;
  name: string | null;
  role: 'USER' | 'ADMIN';
  isActive: boolean;
  isEmailVerified: boolean;
  authProvider: string;
  createdAt: string;
  subscriptionState: SubscriptionInfo | null;
  manualGrants: { endsAt: string; source: string }[];
}

export interface UserList {
  items: UserRow[];
  pagination: { total: number; page: number; limit: number; totalPages: number };
}

export interface UserDetail {
  id: string;
  email: string | null;
  username: string;
  name: string | null;
  role: 'USER' | 'ADMIN';
  isActive: boolean;
  isEmailVerified: boolean;
  emailVerifiedAt: string | null;
  authProvider: string;
  createdAt: string;
  timezone: string;
  privacyAcceptedVersion: string | null;
  privacyAcceptedAt: string | null;
  plan: 'FREE' | 'PREMIUM';
  subscriptionState: SubscriptionInfo | null;
  manualGrants: { id: string; source: string; startsAt: string; endsAt: string; reason: string }[];
  refreshSessions: { id: string; deviceName: string | null; createdAt: string; lastUsedAt: string }[];
  usageToday: { aiPhoto: number; chatMessages: number; chatTokens: number; menuScans: number; suggestMeals: number; barcodeLookups: number };
}

export interface UserBilling {
  paymentOrders: { id: string; code: string; productId: string; amount: number; status: string; createdAt: string; paidAt: string | null }[];
  purchases: { id: string; orderId: string | null; productId: string; purchaseTime: string }[];
  billingEvents: { id: string; type: string; createdAt: string }[];
}

export const PLAN_OPTIONS = [
  { value: 'FREE', label: 'Free' },
  { value: 'PREMIUM', label: 'Premium' },
  { value: 'TRIAL', label: 'Dùng thử' },
  { value: 'GRACE', label: 'Ân hạn / tạm dừng' },
];
export const STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'LOCKED', label: 'Đã khoá' },
];
export const VERIFIED_OPTIONS = [
  { value: 'true', label: 'Đã xác thực email' },
  { value: 'false', label: 'Chưa xác thực email' },
];
