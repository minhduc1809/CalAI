import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';

const COLLAPSE_KEY = 'nw_sidebar_collapsed';

/** Khung chung: sidebar cố định bên trái, thanh trên, vùng nội dung cuộn độc lập. */
export function AdminLayout() {
  const [collapsed, setCollapsed] = useState(() => {
    try {
      return localStorage.getItem(COLLAPSE_KEY) === '1';
    } catch {
      return false;
    }
  });
  const [mobileOpen, setMobileOpen] = useState(false);
  const { pathname } = useLocation();

  useEffect(() => setMobileOpen(false), [pathname]);
  useEffect(() => {
    if (!mobileOpen) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') setMobileOpen(false); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [mobileOpen]);
  useEffect(() => {
    try {
      localStorage.setItem(COLLAPSE_KEY, collapsed ? '1' : '0');
    } catch {
      // bỏ qua nếu trình duyệt chặn lưu trữ
    }
  }, [collapsed]);

  return (
    <div className={`app ${collapsed ? 'collapsed' : ''} ${mobileOpen ? 'mobile-open' : ''}`}>
      <Sidebar collapsed={collapsed} onToggle={() => setCollapsed((c) => !c)} />
      <div className="sidebar-backdrop" onClick={() => setMobileOpen(false)} aria-hidden="true" />
      <div className="main-col">
        <Topbar onOpenMenu={() => setMobileOpen((o) => !o)} />
        <main className="content" id="main">
          <div className="page"><Outlet /></div>
        </main>
      </div>
    </div>
  );
}
