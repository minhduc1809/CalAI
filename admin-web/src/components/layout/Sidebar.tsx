import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  CreditCard,
  Users,
  Gift,
  Shield,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Sparkles,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { adminDashboardApi } from '../../api/admin-dashboard.api';

interface SidebarProps {
  pendingOrdersCount?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({ pendingOrdersCount }) => {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [pendingCount, setPendingCount] = useState<number>(pendingOrdersCount ?? 8);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (pendingOrdersCount !== undefined) {
      setPendingCount(pendingOrdersCount);
      return;
    }
    adminDashboardApi.getSummary().then((sum) => {
      if (sum?.orders?.pending !== undefined) {
        setPendingCount(sum.orders.pending);
      }
    }).catch(() => {});
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
    <aside
      className={`fixed top-0 left-0 z-40 h-screen transition-all duration-300 ease-in-out bg-white/90 dark:bg-[#0B0F17]/90 backdrop-blur-xl border-r border-slate-200/80 dark:border-white/5 flex flex-col ${
        isCollapsed ? 'w-20' : 'w-64'
      }`}
    >
      {/* Brand Header */}
      <div className="h-18 px-5 flex items-center justify-between border-b border-slate-100 dark:border-white/5">
        <div
          onClick={() => navigate('/dashboard')}
          className="flex items-center gap-3 cursor-pointer group overflow-hidden"
        >
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 via-emerald-500 to-teal-400 p-0.5 shadow-glow flex items-center justify-center shrink-0">
            <div className="w-full h-full bg-[#0B0F17] rounded-[10px] flex items-center justify-center">
              <Sparkles className="w-5 h-5 text-emerald-400 group-hover:rotate-12 transition-transform duration-300" />
            </div>
          </div>
          {!isCollapsed && (
            <div className="flex flex-col">
              <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-emerald-500 via-teal-400 to-cyan-400 bg-clip-text text-transparent">
                CalAI
              </span>
              <span className="text-[10px] font-bold tracking-wider text-slate-400 dark:text-slate-500 uppercase -mt-1">
                Admin Console
              </span>
            </div>
          )}
        </div>

        <button
          onClick={() => setIsCollapsed(!isCollapsed)}
          className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-white/5 transition-colors hidden lg:flex"
        >
          {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </button>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 px-3 py-4 space-y-1.5 overflow-y-auto">
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200 group relative ${
                isActive
                  ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/5'
              } ${isCollapsed ? 'justify-center px-0' : ''}`
            }
            title={isCollapsed ? item.name : undefined}
          >
            <item.icon className="w-5 h-5 shrink-0 transition-transform group-hover:scale-110" />
            {!isCollapsed && <span className="truncate">{item.name}</span>}

            {!isCollapsed && item.badge !== undefined && (
              <span className="ml-auto px-2 py-0.5 rounded-full text-[11px] font-bold bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/30 animate-pulse">
                {item.badge}
              </span>
            )}

            {isCollapsed && item.badge !== undefined && (
              <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 rounded-full bg-amber-500 ring-2 ring-white dark:ring-slate-900" />
            )}
          </NavLink>
        ))}
      </div>

      {/* User Info & Logout */}
      <div className="p-3 border-t border-slate-100 dark:border-white/5">
        <div
          className={`flex items-center gap-3 p-2 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 ${
            isCollapsed ? 'justify-center p-2' : ''
          }`}
        >
          <img
            src={
              user?.avatar ||
              'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150'
            }
            alt="Admin Avatar"
            className="w-9 h-9 rounded-full object-cover ring-2 ring-emerald-500/30 shrink-0"
          />
          {!isCollapsed && (
            <div className="flex-1 min-w-0">
              <p className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate">
                {user?.name || user?.username || 'Administrator'}
              </p>
              <p className="text-[11px] text-emerald-600 dark:text-emerald-400 font-medium">
                Super Admin
              </p>
            </div>
          )}
          <button
            onClick={logout}
            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
            title="Đăng xuất"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
};
