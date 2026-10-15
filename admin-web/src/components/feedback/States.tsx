import { ReactNode } from 'react';
import { AppIcon, IconName } from '../ui/icons';
import { Button } from '../ui/Button';

export function Skeleton({ width = '100%', height = 16, className = '' }: { width?: number | string; height?: number; className?: string }) {
  return <div className={`skeleton ${className}`} style={{ width, height }} aria-hidden />;
}

export function LoadingState({ lines = 4 }: { lines?: number }) {
  return (
    <div style={{ padding: 20, display: 'grid', gap: 12 }} role="status" aria-label="Đang tải dữ liệu">
      {Array.from({ length: lines }, (_, i) => <Skeleton key={i} height={18} width={`${100 - i * 8}%`} />)}
    </div>
  );
}

export function EmptyState({ title, description, icon = 'Search', action }: { title: string; description?: string; icon?: IconName; action?: ReactNode }) {
  return (
    <div className="state">
      <div className="state-icon"><AppIcon name={icon} size={22} /></div>
      <h3>{title}</h3>
      {description && <div>{description}</div>}
      {action}
    </div>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="state error" role="alert">
      <div className="state-icon"><AppIcon name="Warning" size={22} /></div>
      <h3>Không tải được dữ liệu</h3>
      <div style={{ whiteSpace: 'pre-line' }}>{message}</div>
      {onRetry && <Button size="sm" icon="Reset" onClick={onRetry}>Thử lại</Button>}
    </div>
  );
}

export function Alert({ kind = 'info', children }: { kind?: 'info' | 'warning' | 'danger' | 'success'; children: ReactNode }) {
  const icon: IconName = kind === 'danger' ? 'XCircle' : kind === 'warning' ? 'Warning' : kind === 'success' ? 'CheckCircle' : 'Info';
  return (
    <div className={`alert ${kind}`} role={kind === 'danger' ? 'alert' : undefined}>
      <AppIcon name={icon} size={18} />
      <div>{children}</div>
    </div>
  );
}
