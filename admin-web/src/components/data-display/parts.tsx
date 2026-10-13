import { ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fmtPct } from '../../lib/format';
import { Skeleton } from '../feedback/States';
import { AppIcon, IconName } from '../ui/icons';

export interface Crumb { label: string; to?: string }

export function Breadcrumbs({ items }: { items: Crumb[] }) {
  return (
    <nav className="breadcrumbs" aria-label="Điều hướng">
      {items.map((c, i) => (
        <span key={c.label} className="row" style={{ gap: 6 }}>
          {i > 0 && <AppIcon name="ChevronRight" size={14} />}
          {c.to && i < items.length - 1 ? <Link to={c.to}>{c.label}</Link> : <span className="current">{c.label}</span>}
        </span>
      ))}
    </nav>
  );
}

interface HeaderProps {
  title: string;
  description?: string;
  breadcrumbs: Crumb[];
  /** Nhóm hành động theo nghiệp vụ (nhập, xuất, nút chính). Chỉ truyền hành động thật sự hợp lệ. */
  actions?: ReactNode;
  total?: number;
}

export function PageHeader({ title, description, breadcrumbs, actions, total }: HeaderProps) {
  return (
    <>
      <Breadcrumbs items={breadcrumbs} />
      <div className="page-header">
        <div>
          <h1 className="page-title">{title}{typeof total === 'number' && <span className="muted" style={{ fontSize: 16, fontWeight: 500 }}> · {total.toLocaleString('vi-VN')}</span>}</h1>
          {description && <p className="page-sub">{description}</p>}
        </div>
        {actions && <div className="page-actions">{actions}</div>}
      </div>
    </>
  );
}

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'primary' | 'neutral';

/** Badge trạng thái: luôn có nhãn chữ, màu chỉ bổ trợ. */
export function StatusBadge({ tone = 'neutral', children }: { tone?: Tone; children: ReactNode }) {
  return <span className={`badge ${tone === 'neutral' ? '' : tone}`}>{children}</span>;
}

interface StatProps {
  title: string;
  value: ReactNode;
  icon: IconName;
  tone?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  /** % so với kỳ trước; null/undefined thì không hiển thị (không có dữ liệu kỳ trước). */
  delta?: number | null;
  deltaLabel?: string;
  hint?: string;
  tooltip?: string;
  to?: string;
  loading?: boolean;
  error?: boolean;
}

export function StatCard({ title, value, icon, tone = 'primary', delta, deltaLabel = 'so với kỳ trước', hint, tooltip, to, loading, error }: StatProps) {
  const nav = useNavigate();
  const clickable = !!to;
  return (
    <div
      className={`card stat ${clickable ? 'hoverable' : ''}`}
      title={tooltip}
      role={clickable ? 'link' : undefined}
      tabIndex={clickable ? 0 : undefined}
      onClick={clickable ? () => nav(to!) : undefined}
      onKeyDown={clickable ? (e) => e.key === 'Enter' && nav(to!) : undefined}
    >
      <div className="stat-top">
        <span>{title}</span>
        <span className={`stat-icon ${tone === 'primary' ? '' : tone}`}><AppIcon name={icon} size={20} /></span>
      </div>
      {loading ? <Skeleton height={30} width="60%" /> : error ? <span className="muted">Không tải được</span> : <div className="stat-value">{value}</div>}
      {!loading && !error && typeof delta === 'number' && (
        <div className={`stat-delta ${delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat'}`} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <AppIcon name={delta > 0 ? 'TrendUp' : delta < 0 ? 'TrendDown' : 'Minus'} size={14} /> {fmtPct(Math.abs(delta))} <span className="muted">{deltaLabel}</span>
        </div>
      )}
      {!loading && !error && hint && <div className="muted" style={{ fontSize: 12.5 }}>{hint}</div>}
    </div>
  );
}
