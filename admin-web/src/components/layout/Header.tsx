import React, { useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import {
  Sun,
  Moon,
  Bell,
  Search,
  ChevronRight,
  ShieldCheck,
  LogOut,
  Sliders,
  CheckCircle2,
} from 'lucide-react';
import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../context/AuthContext';

interface HeaderProps {
  onToggleMobileSidebar?: () => void;
}

export const Header: React.FC<HeaderProps> = () => {
  const { theme, toggleTheme } = useTheme();
  const { user, logout } = useAuth();
  const location = useLocation();
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);

  // Generate breadcrumb title from path
  const pathMap: Record<string, string> = {
    '/dashboard': 'Tổng quan',
    '/users': 'Quản lý Người dùng',
    '/meals': 'Quản lý Bữa ăn',
    '/workouts': 'Quản lý Tập luyện',
    '/weight-logs': 'Lịch sử Cân nặng',
    '/checkins': 'Duyệt Check-in',
    '/templates': 'Mẫu Thực đơn & Lịch tập',
    '/foods': 'Cơ sở dữ liệu Món ăn',
    '/ai-logs': 'Audit Logs & Lịch sử AI',
  };

  const currentTitle = pathMap[location.pathname] || 'Dashboard';

  return (
    <header className="sticky top-0 z-30 h-18 w-full bg-white/80 dark:bg-[#0B0F17]/80 backdrop-blur-xl border-b border-slate-200/80 dark:border-white/5 px-6 flex items-center justify-between transition-colors">
      {/* Breadcrumbs */}
      <div className="flex items-center gap-2">
        <Link
          to="/dashboard"
          className="text-xs font-medium text-slate-400 hover:text-emerald-500 transition-colors"
        >
          CalAI
        </Link>
        <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
        <span className="text-sm font-bold text-slate-900 dark:text-white">
          {currentTitle}
        </span>
      </div>

      {/* Global Actions */}
      <div className="flex items-center gap-3">
        {/* Search Bar */}
        <div className="relative hidden md:flex items-center w-64 lg:w-72">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 pointer-events-none" />
          <input
            type="text"
            placeholder="Tìm kiếm nhanh (Ctrl+K)..."
            className="w-full bg-slate-100 dark:bg-slate-900/60 border border-slate-200 dark:border-white/10 rounded-xl pl-9 pr-3.5 py-1.5 text-xs text-slate-800 dark:text-slate-200 placeholder:text-slate-400 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all"
          />
        </div>

        {/* Dark / Light Mode Toggle */}
        <button
          onClick={toggleTheme}
          className="p-2 rounded-xl text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/5 transition-all border border-transparent hover:border-slate-200 dark:hover:border-white/10"
          title={theme === 'dark' ? 'Chuyển sang Giao diện Sáng' : 'Chuyển sang Giao diện Tối'}
        >
          {theme === 'dark' ? (
            <Sun className="w-4.5 h-4.5 text-amber-400 hover:rotate-45 transition-transform" />
          ) : (
            <Moon className="w-4.5 h-4.5 text-indigo-500 hover:-rotate-12 transition-transform" />
          )}
        </button>

        {/* Notifications */}
        <div className="relative">
          <button
            onClick={() => {
              setShowNotifications(!showNotifications);
              setShowProfileMenu(false);
            }}
            className="p-2 rounded-xl text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/5 transition-all relative border border-transparent hover:border-slate-200 dark:hover:border-white/10"
          >
            <Bell className="w-4.5 h-4.5" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-500 ring-2 ring-white dark:ring-[#0B0F17]" />
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 rounded-2xl glass-dropdown p-4 z-50 animate-slide-up">
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-white/10">
                <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                  Thông báo hệ thống
                </h4>
                <span className="text-[10px] text-emerald-500 font-semibold cursor-pointer">
                  Đánh dấu đã đọc
                </span>
              </div>
              <div className="py-2 space-y-2">
                <div className="p-2.5 rounded-xl hover:bg-slate-50 dark:hover:bg-white/5 transition-colors flex items-start gap-3">
                  <CheckCircle2 className="w-4 h-4 text-emerald-500 mt-0.5 shrink-0" />
                  <div>
                    <p className="text-xs font-semibold text-slate-800 dark:text-slate-200">
                      18 Check-ins đang chờ duyệt
                    </p>
                    <p className="text-[11px] text-slate-400 mt-0.5">
                      Thành viên gửi kết quả tuần 4 cần điều chỉnh calo
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Profile Dropdown */}
        <div className="relative">
          <button
            onClick={() => {
              setShowProfileMenu(!showProfileMenu);
              setShowNotifications(false);
            }}
            className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-slate-100 dark:hover:bg-white/5 transition-all"
          >
            <img
              src={
                user?.avatar ||
                'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150'
              }
              alt="Avatar"
              className="w-8 h-8 rounded-full object-cover ring-2 ring-emerald-500/20"
            />
          </button>

          {showProfileMenu && (
            <div className="absolute right-0 mt-2 w-56 rounded-2xl glass-dropdown p-2 z-50 animate-slide-up">
              <div className="px-3 py-2 border-b border-slate-100 dark:border-white/10">
                <p className="text-xs font-bold text-slate-900 dark:text-white truncate">
                  {user?.name || user?.username}
                </p>
                <p className="text-[11px] text-slate-400 truncate">{user?.email || 'admin@calai.app'}</p>
              </div>

              <div className="py-1">
                <div className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5 transition-colors cursor-pointer">
                  <ShieldCheck className="w-4 h-4 text-emerald-500" />
                  <span>Quyền Quản trị viên</span>
                </div>
                <div className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5 transition-colors cursor-pointer">
                  <Sliders className="w-4 h-4 text-slate-400" />
                  <span>Cài đặt hệ thống</span>
                </div>
              </div>

              <div className="pt-1 border-t border-slate-100 dark:border-white/10">
                <button
                  onClick={logout}
                  className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-semibold text-rose-500 hover:bg-rose-500/10 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Đăng xuất</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
