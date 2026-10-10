import React, { useState } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import {
  Search,
  ChevronRight,
  RefreshCw,
  LogOut,
  ShieldCheck,
  Activity,
  User,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'sonner';

interface HeaderProps {
  onRefresh?: () => void;
  isRefreshing?: boolean;
}

export const Header: React.FC<HeaderProps> = ({ onRefresh, isRefreshing = false }) => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  const pathMap: Record<string, string> = {
    '/dashboard': 'Tổng quan',
    '/orders': 'Quản lý Đơn VietQR',
    '/users': 'Người dùng & Gói cước',
    '/grants': 'Cấp Gói Thủ công',
    '/audit-logs': 'Nhật ký Kiểm toán',
  };

  const currentTitle = pathMap[location.pathname] || 'Bảng điều khiển';

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchTerm.trim()) return;
    const term = searchTerm.trim();
    // Intelligent quick navigation: if search starts with NW or looks like order code, go to orders
    if (/^NW/i.test(term)) {
      navigate(`/orders?search=${encodeURIComponent(term)}`);
    } else {
      navigate(`/users?search=${encodeURIComponent(term)}`);
    }
  };

  const handleRefreshClick = () => {
    if (onRefresh) {
      onRefresh();
    } else {
      window.dispatchEvent(new CustomEvent('admin-refresh-data'));
      toast.info('Đang làm mới dữ liệu hệ thống...');
    }
  };

  return (
    <header className="sticky top-0 z-30 h-16 w-full bg-[#0F172A]/90 backdrop-blur-xl border-b border-[#334155] px-6 flex items-center justify-between transition-colors">
      {/* Breadcrumbs */}
      <div className="flex items-center gap-2">
        <Link
          to="/dashboard"
          className="text-xs font-semibold text-[#94A3B8] hover:text-emerald-400 transition-colors"
        >
          CalAI Admin
        </Link>
        <ChevronRight className="w-3.5 h-3.5 text-[#94A3B8]/60" />
        <span className="text-sm font-bold text-[#F8FAFC]">
          {currentTitle}
        </span>
      </div>

      {/* Global Actions */}
      <div className="flex items-center gap-3">
        {/* Backend Status Badge */}
        <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-[#1E293B] border border-[#334155] text-[11px] font-medium text-[#94A3B8]">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span className="font-mono text-[10px] text-emerald-400">Backend Online</span>
        </div>

        {/* Global Quick Search */}
        <form onSubmit={handleSearchSubmit} className="relative hidden md:flex items-center w-64 lg:w-72">
          <Search className="w-3.5 h-3.5 text-[#94A3B8] absolute left-3 pointer-events-none" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm mã đơn, email user..."
            className="w-full bg-[#1E293B] border border-[#334155] rounded-xl pl-8.5 pr-3 py-1.5 text-xs text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all"
          />
        </form>

        {/* Manual Refresh Button */}
        <button
          onClick={handleRefreshClick}
          disabled={isRefreshing}
          className="p-2 rounded-xl text-[#94A3B8] hover:text-[#F8FAFC] hover:bg-[#1E293B] border border-[#334155] transition-all disabled:opacity-50"
          title="Làm mới dữ liệu tức thì"
        >
          <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-emerald-400' : ''}`} />
        </button>

        {/* Admin Quick Profile */}
        <div className="relative">
          <button
            onClick={() => setShowProfileMenu(!showProfileMenu)}
            className="flex items-center gap-2 p-1 rounded-xl hover:bg-[#1E293B] border border-transparent hover:border-[#334155] transition-all"
          >
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-emerald-500 to-teal-400 p-0.5 shrink-0">
              <div className="w-full h-full rounded-full bg-[#0F172A] flex items-center justify-center font-bold text-xs text-emerald-400">
                {(user?.name || user?.username || 'A')[0].toUpperCase()}
              </div>
            </div>
          </button>

          {showProfileMenu && (
            <div className="absolute right-0 mt-2 w-64 rounded-2xl bg-[#1E293B] border border-[#334155] shadow-2xl p-2.5 z-50 animate-slide-up">
              <div className="px-3 py-2 border-b border-[#334155]">
                <p className="text-xs font-bold text-[#F8FAFC] truncate">
                  {user?.name || user?.username || 'Administrator'}
                </p>
                <p className="text-[11px] text-[#94A3B8] truncate">{user?.email || 'admin@calai.com'}</p>
                <div className="mt-1.5 flex items-center gap-1 text-[10px] font-mono text-emerald-400 font-semibold">
                  <ShieldCheck className="w-3 h-3 text-emerald-400" />
                  Quyền: ADMIN (Toàn quyền hệ thống)
                </div>
              </div>

              <div className="pt-2">
                <button
                  onClick={logout}
                  className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-semibold text-red-400 hover:bg-red-500/10 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Đăng xuất tài khoản</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
