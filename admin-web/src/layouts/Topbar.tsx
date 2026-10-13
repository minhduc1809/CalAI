import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { titleFor } from '../app/navigation';
import { AppIcon } from '../components/ui/icons';
import { IconButton } from '../components/ui/Button';
import { authStore } from '../services/auth';

/** Thanh trên: tên trang, thông tin admin và menu tài khoản. Không có tìm kiếm toàn cục hay chuông thông báo vì chưa có nguồn thật. */
export function Topbar({ onOpenMenu }: { onOpenMenu: () => void }) {
  const { pathname } = useLocation();
  const admin = authStore.getAdmin();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const close = (e: MouseEvent) => !ref.current?.contains(e.target as Node) && setOpen(false);
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false);
    document.addEventListener('mousedown', close);
    document.addEventListener('keydown', esc);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('keydown', esc);
    };
  }, [open]);

  const initial = (admin?.name || admin?.email || 'A').trim().charAt(0).toUpperCase();
  return (
    <header className="topbar">
      <IconButton icon="Menu" label="Mở menu" className="menu-toggle" onClick={onOpenMenu} />
      <strong style={{ fontSize: 16 }}>{titleFor(pathname)}</strong>
      <div className="spacer" />
      <div className="admin-menu" ref={ref}>
        <button className="row" style={{ gap: 10, background: 'none', border: 0, cursor: 'pointer' }} onClick={() => setOpen((o) => !o)} aria-haspopup="menu" aria-expanded={open}>
          <span className="stat-icon" style={{ width: 34, height: 34, fontWeight: 700 }}>{initial}</span>
          <span style={{ textAlign: 'left', lineHeight: 1.2 }}>
            <span style={{ display: 'block', fontWeight: 600, fontSize: 13.5 }}>{admin?.name || 'Quản trị viên'}</span>
            <span className="muted" style={{ fontSize: 12 }}>{admin?.email}</span>
          </span>
          <AppIcon name="ChevronDown" size={16} />
        </button>
        {open && (
          <div className="menu-pop" role="menu">
            <Link to="/security" role="menuitem" onClick={() => setOpen(false)}><AppIcon name="Security" size={16} /> Bảo mật tài khoản</Link>
            <div className="sep" />
            <button role="menuitem" className="danger" onClick={() => authStore.logout()}><AppIcon name="Logout" size={16} /> Đăng xuất</button>
          </div>
        )}
      </div>
    </header>
  );
}
