import { StatusBadge } from '../../../components/data-display/parts';
import type { UserRow } from '../types/user.types';

/** Gói hiện tại của người dùng, suy từ đăng ký còn hạn hoặc quyền cấp còn hiệu lực. */
export function planOf(u: Pick<UserRow, 'subscriptionState' | 'manualGrants'>, now = Date.now()): { label: string; tone: 'success' | 'warning' | 'neutral' | 'info'; endsAt: string | null } {
  const sub = u.subscriptionState;
  if (sub && ['IN_GRACE_PERIOD', 'ON_HOLD'].includes(sub.status)) return { label: sub.status === 'ON_HOLD' ? 'Tạm dừng' : 'Ân hạn', tone: 'warning', endsAt: sub.expiryTime };
  const ends = [sub && ['ACTIVE', 'CANCELED'].includes(sub.status) ? sub.expiryTime : null, u.manualGrants[0]?.endsAt ?? null]
    .filter((d): d is string => !!d && new Date(d).getTime() > now)
    .sort((a, b) => new Date(b).getTime() - new Date(a).getTime())[0];
  if (ends) return { label: sub?.isTrial ? 'Dùng thử' : 'Premium', tone: sub?.isTrial ? 'info' : 'success', endsAt: ends };
  return { label: 'Free', tone: 'neutral', endsAt: null };
}

export function PlanBadge({ user }: { user: Pick<UserRow, 'subscriptionState' | 'manualGrants'> }) {
  const p = planOf(user);
  return <StatusBadge tone={p.tone}>{p.label}</StatusBadge>;
}

export function AccountStatusBadge({ active }: { active: boolean }) {
  return <StatusBadge tone={active ? 'success' : 'danger'}>{active ? 'Hoạt động' : 'Đã khoá'}</StatusBadge>;
}
