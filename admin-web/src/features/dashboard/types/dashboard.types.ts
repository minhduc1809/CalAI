/** Việc đang chờ admin xử lý (số thật từ máy chủ). */
export interface PendingWork {
  pendingOrders: number;
  expiringOrders: number;
  pendingSubmissions: number;
  dataRequests: number;
  dataRequestsOverdue: number;
  billingIssues: number;
}

export interface Delta { current: number; previous: number }

export interface OverviewResponse {
  range: { from: string; to: string; days: number };
  updatedAt: string;
  users: { total: number; totalPrevious: number; newInRange: Delta; active: Delta | null };
  premium: { active: number; byManual: number; byQr: number; byGooglePlay: number };
  revenue: { received: Delta; ordersPaid: Delta };
  orders: { pending: number; expiringSoon: number };
}

export interface TimeSeriesResponse {
  days: { date: string; newUsers: number; activeUsers: number; revenueVnd: number; paidOrders: number }[];
}

export interface FunnelResponse {
  cohort: { from: string; to: string };
  definition: string;
  steps: { key: string; label: string; count: number; fromPrevPct: number | null; fromStartPct: number | null }[];
}

export interface RetentionResponse {
  definition: string;
  rows: { key: 'D1' | 'D7' | 'D30'; label: string; eligible: number; retained: number; ratePct: number | null }[];
}
