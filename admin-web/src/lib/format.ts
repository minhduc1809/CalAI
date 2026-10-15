import { TIME_ZONE } from '../constants';

/** Mọi thời gian hiển thị theo giờ Việt Nam; tiền theo vi-VN, VND. Không định nghĩa lại ở nơi khác. */
const dtf = new Intl.DateTimeFormat('vi-VN', { timeZone: TIME_ZONE, dateStyle: 'short', timeStyle: 'short' });
const df = new Intl.DateTimeFormat('vi-VN', { timeZone: TIME_ZONE, dateStyle: 'short' });
const vnd = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 });

export const fmtDateTime = (v?: string | Date | null) => (v ? dtf.format(new Date(v)) : '—');
export const fmtDate = (v?: string | Date | null) => (v ? df.format(new Date(v)) : '—');
export const fmtVnd = (n?: number | null) => (n === undefined || n === null ? '—' : vnd.format(n));
export const fmtNumber = (n?: number | null, digits = 0) =>
  n === undefined || n === null ? '—' : new Intl.NumberFormat('vi-VN', { maximumFractionDigits: digits }).format(n);
export const fmtPct = (n?: number | null, digits = 1) => (n === undefined || n === null ? '—' : `${fmtNumber(n, digits)}%`);
export const fmtUsd = (n?: number | null) =>
  n === undefined || n === null ? '—' : `$${new Intl.NumberFormat('en-US', { maximumFractionDigits: 4 }).format(n)}`;

/** Ngày yyyy-MM-dd theo giờ Việt Nam. */
export function todayKey(offsetDays = 0): string {
  const d = new Date(Date.now() + offsetDays * 86_400_000);
  return new Intl.DateTimeFormat('en-CA', { timeZone: TIME_ZONE }).format(d);
}

/** Chênh lệch phần trăm so với kỳ trước; null nếu kỳ trước bằng 0 (không chia cho 0). */
export function deltaPct(current: number, previous: number): number | null {
  if (!previous) return null;
  return ((current - previous) / previous) * 100;
}
