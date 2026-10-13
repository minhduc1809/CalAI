import { createContext, ReactNode, useCallback, useContext, useMemo, useState } from 'react';
import { TOAST_MS } from '../../constants';
import { AppIcon, IconName } from './icons';

type Kind = 'success' | 'error' | 'warning' | 'info';
interface ToastItem { id: number; kind: Kind; title: string; message?: string }
interface ToastApi {
  success: (title: string, message?: string) => void;
  error: (title: string, message?: string) => void;
  warning: (title: string, message?: string) => void;
  info: (title: string, message?: string) => void;
}

const ToastContext = createContext<ToastApi | null>(null);
const ICON: Record<Kind, IconName> = { success: 'CheckCircle', error: 'XCircle', warning: 'Warning', info: 'Info' };

/** Hệ thống thông báo duy nhất (không dùng alert()). Chỉ gọi success sau khi máy chủ đã xác nhận. */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([]);
  const push = useCallback((kind: Kind, title: string, message?: string) => {
    const id = Date.now() + Math.random();
    setItems((cur) => [...cur.slice(-3), { id, kind, title, message }]);
    window.setTimeout(() => setItems((cur) => cur.filter((t) => t.id !== id)), TOAST_MS);
  }, []);
  const api = useMemo<ToastApi>(
    () => ({
      success: (t, m) => push('success', t, m),
      error: (t, m) => push('error', t, m),
      warning: (t, m) => push('warning', t, m),
      info: (t, m) => push('info', t, m),
    }),
    [push],
  );
  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {items.map((t) => (
          <div key={t.id} className={`toast ${t.kind}`}>
            <AppIcon name={ICON[t.kind]} size={18} />
            <div>
              <div className="t-title">{t.title}</div>
              {t.message && <div className="t-msg">{t.message}</div>}
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastApi {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast phải nằm trong ToastProvider');
  return ctx;
}
