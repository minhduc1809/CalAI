import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { NAV_GROUPS, BadgeKey } from '../app/navigation';
import { AppIcon } from '../components/ui/icons';
import { dashboardApi } from '../features/dashboard/api/dashboard.api';

/** Số việc đang chờ là số thật từ máy chủ; chưa tải được thì không hiện badge (không hiện số giả). */
function usePendingBadges(): Partial<Record<BadgeKey, number>> {
  const { data } = useQuery({
    queryKey: ['pending-work'],
    queryFn: ({ signal }) => dashboardApi.pendingWork(signal),
    refetchInterval: 60_000,
    retry: false,
  });
  if (!data) return {};
  return { pendingOrders: data.pendingOrders, pendingSubmissions: data.pendingSubmissions, dataRequests: data.dataRequests };
}

export function Sidebar({ collapsed, onToggle }: { collapsed: boolean; onToggle: () => void }) {
  const badges = usePendingBadges();
  const [closed, setClosed] = useState<Record<string, boolean>>({});

  return (
    <aside className="sidebar" aria-label="Điều hướng chính">
      <div className="sidebar-brand">
        <span className="logo"><AppIcon name="Activity" size={20} /></span>
        <span className="name">NutriWise</span>
      </div>
      <nav>
        {NAV_GROUPS.map((g) => (
          <div className="nav-group" key={g.id}>
            {g.items.length > 1 || g.id !== 'overview' ? (
              <button
                className="nav-group-title"
                onClick={() => setClosed((c) => ({ ...c, [g.id]: !c[g.id] }))}
                aria-expanded={!closed[g.id]}
              >
                {g.label}
                <AppIcon name={closed[g.id] ? 'ChevronRight' : 'ChevronDown'} size={14} />
              </button>
            ) : null}
            {!closed[g.id] && g.items.map((it) => {
              const count = it.badge ? badges[it.badge] : undefined;
              return (
                <NavLink
                  key={it.to}
                  to={it.to}
                  end={it.end}
                  className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                  title={collapsed ? it.label : undefined}
                  aria-label={collapsed ? it.label : undefined}
                >
                  <AppIcon name={it.icon} size={19} />
                  <span className="label">{it.label}</span>
                  {!!count && count > 0 && <span className="badge-count" aria-label={`${count} việc chờ xử lý`}>{count > 99 ? '99+' : count}</span>}
                </NavLink>
              );
            })}
          </div>
        ))}
      </nav>
      <div className="sidebar-foot">
        <button className="nav-link" style={{ width: '100%', background: 'none', border: 0, cursor: 'pointer' }} onClick={onToggle} aria-label={collapsed ? 'Mở rộng thanh bên' : 'Thu gọn thanh bên'}>
          <AppIcon name="CollapseSidebar" size={19} />
          <span className="label">Thu gọn</span>
        </button>
      </div>
    </aside>
  );
}
