import { format, parseISO } from 'date-fns';

export function formatDate(dateString?: string | null, formatStr: string = 'dd/MM/yyyy HH:mm'): string {
  if (!dateString) return '—';
  try {
    const d = typeof dateString === 'string' ? parseISO(dateString) : new Date(dateString);
    if (isNaN(d.getTime())) return dateString;
    return format(d, formatStr);
  } catch {
    return dateString;
  }
}

export function formatDateOnly(dateString?: string | null): string {
  return formatDate(dateString, 'dd/MM/yyyy');
}

export function formatNumber(val?: number | null, decimals: number = 0): string {
  if (val === undefined || val === null) return '0';
  return val.toLocaleString('vi-VN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

export function formatCurrencyUsd(val?: number | null): string {
  if (val === undefined || val === null) return '$0.00';
  if (val > 0 && val < 0.01) {
    return `$${val.toFixed(4)}`;
  }
  return `$${val.toFixed(2)}`;
}

export function formatCalories(val?: number | null): string {
  return `${formatNumber(val)} kcal`;
}

export function formatGrams(val?: number | null): string {
  return `${formatNumber(val)} g`;
}
