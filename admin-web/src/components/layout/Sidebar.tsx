import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  CreditCard,
  Users,
  Gift,
  Shield,
  LogOut,
  Sparkles,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { adminDashboardApi } from '../../api/admin-dashboard.api';

interface SidebarProps {
  pendingOrdersCount?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({ pendingOrdersCount }) => {
  const [pendingCount, setPendingCount] = useState<number>(pendingOrdersCount ?? 0);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (pendingOrdersCount !== undefined) {
      setPendingCount(pendingOrdersCount);
      return;
    }
    const checkPending = () => {
      adminDashboardApi.getSummary().then((sum) => {
        if (sum?.orders?.pending !== undefined) {
          setPendingCount(sum.orders.pending);
        }
      }).catch(() => {});
    };

    checkPending();
    const interval = setInterval(checkPending, 30000); // 30s polling
    return () => clearInterval(interval);
  }, [pendingOrdersCount]);

  const menuItems = [
    { name: 'Tổng quan', path: '/dashboard', icon: LayoutDashboard },
    {
      name: 'Quản lý Đơn VietQR',
      path: '/orders',
      icon: CreditCard,
      badge: pendingCount > 0 ? pendingCount : undefined,
    },
    { name: 'Người dùng', path: '/users', icon: Users },
    { name: 'Cấp Gói Thủ công', path: '/grants', icon: Gift },
    { name: 'Nhật ký Kiểm toán', path: '/audit-logs', icon: Shield },
  ];

  return (
    <aside className="fixed top-0 left-0 z-40 h-screen w-[240px] bg-[#0F172A] border-r border-[#334155] flex flex-col select-none transition-all">
      {/* Brand Header */}
      <div className="h-16 px-4 flex items-center justify-between border-b border-[#334155]/70">
        <div
          onClick={() => navigate('/dashboard')}
          className="flex items-center gap-2.5 cursor-pointer group overflow-hidden"
        >
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-600 via-emerald-500 to-teal-400 p-0.5 shadow-glow flex items-center justify-center shrink-0">
            <div className="w-full h-full bg-[#0F172A] rounded-[10px] flex items-center justify-center">
              <Sparkles className="w-4.5 h-4.5 text-emerald-400 group-hover:rotate-12 transition-transform duration-300" />
            </div>
          </div>
          <div className="flex flex-col min-w-0">
            <div className="flex items-center gap-1.5">
              <span className="font-extrabold text-base tracking-tight text-[#F8FAFC]">
                CalAI
              </span>
              <span className="text-[10px] px-1.5 py-0.5 rounded font-mono font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                PRO
              </span>
            </div>
            <span className="text-[10px] font-semibold tracking-wider text-[#94A3B8] uppercase">
              Operations Console
            </span>
          </div>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 px-2.5 py-4 space-y-1 overflow-y-auto">
        <div className="px-3 pb-1.5 text-[10px] font-bold uppercase tracking-wider text-[#94A3B8]/70">
          Vận hành & Quản trị
        </div>

        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 group relative ${
                isActive
                  ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 shadow-sm font-bold'
                  : 'text-[#94A3B8] hover:text-[#F8FAFC] hover:bg-[#1E293B]/70 border border-transparent'
              }`
            }
          >
            <item.icon className="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" />
            <span className="truncate">{item.name}</span>

            {item.badge !== undefined && (
              <span className="ml-auto px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-red-500/20 text-red-400 border border-red-500/30 animate-pulse">
                {item.badge}
              </span>
            )}
          </NavLink>
        ))}
      </div>

      {/* Bottom Profile & Logout */}
      <div className="p-3 border-t border-[#334155]/70 bg-[#0F172A]/90">
        <div className="flex items-center gap-2.5 p-2 rounded-xl bg-[#1E293B] border border-[#334155]">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-emerald-500 to-teal-400 p-0.5 shrink-0">
            <div className="w-full h-full rounded-full bg-[#0F172A] flex items-center justify-center font-bold text-xs text-emerald-400">
              {(user?.name || user?.username || 'A')[0].toUpperCase()}
            </div>
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-bold text-[#F8FAFC] truncate">
              {user?.name || user?.username || 'Administrator'}
            </p>
            <p className="text-[10px] text-emerald-400 font-mono font-medium truncate">
              {user?.role || 'ADMIN'}
            </p>
          </div>
          <button
            onClick={logout}
            className="p-1.5 rounded-lg text-[#94A3B8] hover:text-red-400 hover:bg-red-500/10 transition-colors"
            title="Đăng xuất"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
};
