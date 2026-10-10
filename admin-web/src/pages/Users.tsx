import React, { useEffect, useState, useMemo } from 'react';
import {
  Search,
  UserPlus,
  KeyRound,
  Trash2,
  Edit,
  Eye,
  Shield,
  X,
  Check,
  RefreshCw,
  Copy,
  Sparkles,
  CreditCard,
  Gift,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  ChevronRight,
  ExternalLink,
} from 'lucide-react';
import { usersApi, CreateUserPayload, UpdateUserPayload } from '../api/users.api';
import { billingApi } from '../api/billing.api';
import { User, Role, AdminUserBillingDetails } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDateOnly, formatDateTimeVn, formatNumber, formatCurrencyVnd } from '../utils/formatters';
import { toast } from 'sonner';

export const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [premiumFilter, setPremiumFilter] = useState<string>('ALL');

  // Modals & Drawer
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isResetPassOpen, setIsResetPassOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [isDetailDrawerOpen, setIsDetailDrawerOpen] = useState(false);
  const [isQuickGrantOpen, setIsQuickGrantOpen] = useState(false);

  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [billingDetails, setBillingDetails] = useState<AdminUserBillingDetails | null>(null);
  const [billingLoading, setBillingLoading] = useState(false);
  const [drawerTab, setDrawerTab] = useState<'billing' | 'account'>('billing');

  // Form states
  const [createForm, setCreateForm] = useState<CreateUserPayload>({
    username: '',
    email: '',
    password: '',
    name: '',
    role: 'USER',
    isActive: true,
    dailyAiQuota: 10,
  });

  const [editForm, setEditForm] = useState<UpdateUserPayload>({});
  const [newPassword, setNewPassword] = useState('');
  const [grantDays, setGrantDays] = useState(7);
  const [grantReason, setGrantReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const res = await usersApi.getUsers();
      setUsers(res.data);
    } catch (err: any) {
      toast.error('Lỗi khi tải danh sách người dùng');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRefresh = () => {
    setIsRefreshing(true);
    fetchUsers().then(() => toast.success('Đã làm mới danh sách người dùng'));
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
  };

  // Open User Detail Drawer & fetch Billing info
  const handleOpenDetail = async (user: User) => {
    setSelectedUser(user);
    setIsDetailDrawerOpen(true);
    setDrawerTab('billing');
    setBillingLoading(true);
    try {
      const billing = await usersApi.getUserBilling(user.id);
      setBillingDetails(billing);
    } catch {
      toast.error('Không thể tải chi tiết tài chính của người dùng');
    } finally {
      setBillingLoading(false);
    }
  };

  // Filtered users
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchSearch =
        u.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (u.email && u.email.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (u.name && u.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
        u.id.toLowerCase().includes(searchTerm.toLowerCase());

      const matchRole = roleFilter === 'ALL' || u.role === roleFilter;
      const matchStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && u.isActive) ||
        (statusFilter === 'INACTIVE' && !u.isActive);

      const matchPremium =
        premiumFilter === 'ALL' ||
        (premiumFilter === 'PREMIUM' && !!u.isPremium) ||
        (premiumFilter === 'FREE' && !u.isPremium);

      return matchSearch && matchRole && matchStatus && matchPremium;
    });
  }, [users, searchTerm, roleFilter, statusFilter, premiumFilter]);

  // Create User Handler
  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!createForm.username || !createForm.password) {
      toast.error('Vui lòng điền đầy đủ tên đăng nhập và mật khẩu');
      return;
    }
    setActionLoading(true);
    try {
      await usersApi.createUser(createForm);
      toast.success(`Đã tạo thành công tài khoản ${createForm.username}`);
      setIsCreateOpen(false);
      setCreateForm({
        username: '',
        email: '',
        password: '',
        name: '',
        role: 'USER',
        isActive: true,
        dailyAiQuota: 10,
      });
      fetchUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi tạo người dùng');
    } finally {
      setActionLoading(false);
    }
  };

  // Edit User Handler
  const handleEditUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser) return;
    setActionLoading(true);
    try {
      await usersApi.updateUser(selectedUser.id, editForm);
      toast.success(`Đã cập nhật thông tin tài khoản @${selectedUser.username}`);
      setIsEditOpen(false);
      fetchUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi cập nhật');
    } finally {
      setActionLoading(false);
    }
  };

  // Reset Password Handler
  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser || !newPassword) return;
    setActionLoading(true);
    try {
      await usersApi.resetPassword(selectedUser.id, newPassword);
      toast.success(`Đã đổi mật khẩu cho tài khoản @${selectedUser.username}`);
      setIsResetPassOpen(false);
      setNewPassword('');
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi đặt lại mật khẩu');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete User Handler
  const handleDeleteUser = async () => {
    if (!selectedUser) return;
    setActionLoading(true);
    try {
      await usersApi.deleteUser(selectedUser.id);
      toast.success(`Đã xóa người dùng @${selectedUser.username}`);
      setIsDeleteOpen(false);
      fetchUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi xóa người dùng');
    } finally {
      setActionLoading(false);
    }
  };

  // Quick Grant Handler
  const handleQuickGrant = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser) return;
    if (grantDays < 1 || grantDays > 90) {
      toast.error('Số ngày cấp bù bắt buộc từ 1 đến 90 ngày (BR-17.3)');
      return;
    }
    if (grantReason.trim().length < 10) {
      toast.error('Lý do giải trình đền bù bắt buộc tối thiểu 10 ký tự');
      return;
    }

    setActionLoading(true);
    try {
      await billingApi.grantPremium({
        userId: selectedUser.id,
        days: grantDays,
        reason: grantReason.trim(),
      });
      toast.success(`Đã cấp ${grantDays} ngày Premium cho @${selectedUser.username}`);
      setIsQuickGrantOpen(false);
      setGrantReason('');
      // Reload billing details
      const updatedBilling = await usersApi.getUserBilling(selectedUser.id);
      setBillingDetails(updatedBilling);
      fetchUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi cấp gói bù');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">
            Quản Lý Người Dùng & Billing
          </h1>
          <p className="text-xs text-slate-400 mt-1 max-w-xl">
            Quản lý tài khoản, trạng thái gói Premium, kiểm tra lịch sử đơn VietQR và hạn mức AI. Tuân thủ tiêu chuẩn bảo mật dữ liệu y tế <b className="text-slate-300">BR-17.1</b>.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            disabled={isRefreshing}
            className="px-3.5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-bold transition-all flex items-center gap-2 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-emerald-400' : ''}`} />
            <span>Làm mới</span>
          </button>
          <Button
            variant="primary"
            onClick={() => setIsCreateOpen(true)}
            className="flex items-center gap-2 text-xs shadow-glow"
          >
            <UserPlus className="w-4 h-4" />
            <span>Tạo người dùng</span>
          </Button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 rounded-2xl bg-[#1E293B]/60 border border-slate-700/60 flex flex-col md:flex-row items-center justify-between gap-4 text-xs">
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo username, họ tên, email, ID..."
            className="w-full bg-slate-900 border border-slate-700 rounded-xl pl-9 pr-3.5 py-2 text-white placeholder:text-slate-500 focus:outline-none focus:border-emerald-500"
          />
        </div>

        <div className="flex items-center gap-2.5 flex-wrap w-full md:w-auto">
          <select
            value={premiumFilter}
            onChange={(e) => setPremiumFilter(e.target.value)}
            className="bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-slate-300 focus:outline-none focus:border-emerald-500 font-semibold"
          >
            <option value="ALL">Tất cả gói</option>
            <option value="PREMIUM">Gói Premium</option>
            <option value="FREE">Tài khoản Free</option>
          </select>

          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-slate-300 focus:outline-none focus:border-emerald-500 font-semibold"
          >
            <option value="ALL">Mọi quyền (Roles)</option>
            <option value="USER">Thành viên (USER)</option>
            <option value="ADMIN">Quản trị (ADMIN)</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-slate-300 focus:outline-none focus:border-emerald-500 font-semibold"
          >
            <option value="ALL">Mọi trạng thái</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="INACTIVE">Đã khóa</option>
          </select>
        </div>
      </div>

      {/* Users Table */}
      <div className="rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg overflow-hidden">
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center gap-3">
            <div className="w-8 h-8 rounded-full border-2 border-emerald-500 border-t-transparent animate-spin" />
            <span className="text-xs text-slate-400">Đang tải danh sách người dùng...</span>
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="py-16 text-center">
            <EmptyState
              title="Không tìm thấy người dùng"
              description="Hãy thử đổi từ khóa tìm kiếm hoặc bỏ bớt bộ lọc."
            />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[11px] uppercase font-bold text-slate-400 bg-slate-900/60 border-b border-slate-700/80">
                <tr>
                  <th className="py-3.5 px-4">Tài Khoản & Định Danh</th>
                  <th className="py-3.5 px-4">Email Liên Hệ</th>
                  <th className="py-3.5 px-4">Gói Premium</th>
                  <th className="py-3.5 px-4">Quyền Hạn</th>
                  <th className="py-3.5 px-4">Trạng Thái</th>
                  <th className="py-3.5 px-4">Hạn Mức AI</th>
                  <th className="py-3.5 px-4">Ngày Tham Gia</th>
                  <th className="py-3.5 px-4 text-right">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-slate-800/40 transition-colors">
                    {/* User */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-3">
                        <img
                          src={
                            user.avatar ||
                            `https://api.dicebear.com/7.x/initials/svg?seed=${user.username}`
                          }
                          alt={user.username}
                          className="w-9 h-9 rounded-full object-cover ring-2 ring-emerald-500/30 shrink-0"
                        />
                        <div>
                          <p className="font-bold text-white flex items-center gap-1.5">
                            <span>{user.name || user.username}</span>
                            {user.isPremium && (
                              <span className="w-2 h-2 rounded-full bg-emerald-400" title="Premium Active" />
                            )}
                          </p>
                          <div className="flex items-center gap-1 text-[11px] text-slate-400 font-mono">
                            <span>@{user.username}</span>
                            <button
                              onClick={() => copyToClipboard(user.id, 'User ID')}
                              className="text-slate-500 hover:text-slate-300"
                              title="Copy ID"
                            >
                              <Copy className="w-3 h-3" />
                            </button>
                          </div>
                        </div>
                      </div>
                    </td>

                    {/* Email */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-1.5 text-slate-300">
                        <span>{user.email || '—'}</span>
                        {user.email && (
                          <button
                            onClick={() => copyToClipboard(user.email!, 'Email')}
                            className="text-slate-500 hover:text-slate-300"
                            title="Copy email"
                          >
                            <Copy className="w-3 h-3" />
                          </button>
                        )}
                      </div>
                    </td>

                    {/* Gói Premium */}
                    <td className="py-3.5 px-4">
                      {user.isPremium ? (
                        <div>
                          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 inline-flex items-center gap-1">
                            <Sparkles className="w-3 h-3" />
                            PREMIUM
                          </span>
                          {user.subscriptionState?.expiryTime && (
                            <span className="text-[10px] text-slate-400 block mt-0.5">
                              Hết hạn: {formatDateOnly(user.subscriptionState.expiryTime)}
                            </span>
                          )}
                        </div>
                      ) : (
                        <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-slate-800 text-slate-400 border border-slate-700">
                          Free Tier
                        </span>
                      )}
                    </td>

                    {/* Role */}
                    <td className="py-3.5 px-4">
                      <Badge variant={user.role === 'ADMIN' ? 'purple' : 'slate'} dot={user.role === 'ADMIN'}>
                        {user.role}
                      </Badge>
                    </td>

                    {/* Status */}
                    <td className="py-3.5 px-4">
                      <Badge variant={user.isActive ? 'emerald' : 'rose'} dot>
                        {user.isActive ? 'Hoạt động' : 'Đã khóa'}
                      </Badge>
                    </td>

                    {/* AI Quotas */}
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white">
                        {user.dailyAiQuota} <span className="text-[10px] text-slate-400">/ngày</span>
                      </div>
                      {(user.purchasedAiQuota > 0 || user.purchasedChatQuota > 0) && (
                        <div className="text-[10px] text-emerald-400 font-bold">
                          +{user.purchasedAiQuota + user.purchasedChatQuota} trả phí
                        </div>
                      )}
                    </td>

                    {/* CreatedAt */}
                    <td className="py-3.5 px-4 text-slate-400">
                      {formatDateOnly(user.createdAt)}
                    </td>

                    {/* Actions */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => handleOpenDetail(user)}
                          className="px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-emerald-400 text-xs font-bold transition-all flex items-center gap-1 border border-slate-700"
                          title="Xem Billing & Tài chính"
                        >
                          <CreditCard className="w-3.5 h-3.5" />
                          <span>Billing</span>
                        </button>

                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setEditForm({
                              name: user.name || '',
                              email: user.email || '',
                              role: user.role,
                              isActive: user.isActive,
                              dailyAiQuota: user.dailyAiQuota,
                            });
                            setIsEditOpen(true);
                          }}
                          className="p-1.5 rounded-xl text-slate-400 hover:text-cyan-400 hover:bg-cyan-500/10 transition-colors"
                          title="Sửa tài khoản"
                        >
                          <Edit className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setNewPassword('');
                            setIsResetPassOpen(true);
                          }}
                          className="p-1.5 rounded-xl text-slate-400 hover:text-amber-400 hover:bg-amber-500/10 transition-colors"
                          title="Đổi mật khẩu"
                        >
                          <KeyRound className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setIsDeleteOpen(true);
                          }}
                          className="p-1.5 rounded-xl text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
                          title="Xóa người dùng"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* DRAWER: USER DETAIL & BILLING OVERVIEW (/admin/users/:id/billing) */}
      {isDetailDrawerOpen && selectedUser && (
        <div className="fixed inset-0 z-50 overflow-hidden animate-fade-in">
          <div
            className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm transition-opacity"
            onClick={() => setIsDetailDrawerOpen(false)}
          />
          <div className="fixed inset-y-0 right-0 max-w-lg w-full bg-[#0F172A] border-l border-slate-700 shadow-2xl p-6 overflow-y-auto flex flex-col justify-between animate-slide-up">
            <div className="space-y-6">
              {/* Drawer Header */}
              <div className="flex items-center justify-between pb-4 border-b border-slate-800">
                <div className="flex items-center gap-3">
                  <img
                    src={
                      selectedUser.avatar ||
                      `https://api.dicebear.com/7.x/initials/svg?seed=${selectedUser.username}`
                    }
                    alt={selectedUser.username}
                    className="w-12 h-12 rounded-full object-cover ring-2 ring-emerald-500/30"
                  />
                  <div>
                    <h3 className="text-base font-bold text-white flex items-center gap-2">
                      <span>{selectedUser.name || selectedUser.username}</span>
                      {selectedUser.isPremium && (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                          PREMIUM
                        </span>
                      )}
                    </h3>
                    <p className="text-xs text-slate-400 font-mono">@{selectedUser.username}</p>
                  </div>
                </div>
                <button
                  onClick={() => setIsDetailDrawerOpen(false)}
                  className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Drawer Navigation Tabs */}
              <div className="flex rounded-xl bg-slate-900 p-1 border border-slate-800">
                <button
                  onClick={() => setDrawerTab('billing')}
                  className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-2 ${
                    drawerTab === 'billing'
                      ? 'bg-emerald-500 text-white shadow-glow'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  <CreditCard className="w-3.5 h-3.5" />
                  <span>Tài Chính & Billing</span>
                </button>
                <button
                  onClick={() => setDrawerTab('account')}
                  className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-2 ${
                    drawerTab === 'account'
                      ? 'bg-emerald-500 text-white shadow-glow'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  <Shield className="w-3.5 h-3.5" />
                  <span>Tài Khoản & Quota</span>
                </button>
              </div>

              {billingLoading ? (
                <div className="py-12 flex flex-col items-center justify-center gap-2 text-slate-400 text-xs">
                  <div className="w-6 h-6 rounded-full border-2 border-emerald-500 border-t-transparent animate-spin" />
                  <span>Đang tải thông tin tài chính...</span>
                </div>
              ) : drawerTab === 'billing' ? (
                <div className="space-y-5 text-xs">
                  {/* Current Subscription Card */}
                  <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-950/40 via-slate-900 to-slate-900 border border-emerald-500/30">
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-[11px] font-bold text-emerald-400 uppercase tracking-wider flex items-center gap-1.5">
                        <Sparkles className="w-3.5 h-3.5" />
                        Gói Đăng Ký Hiện Tại
                      </span>
                      <button
                        onClick={() => setIsQuickGrantOpen(true)}
                        className="px-2.5 py-1 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white text-[11px] font-bold transition-all shadow-sm"
                      >
                        + Cấp gói bù
                      </button>
                    </div>

                    <div className="space-y-1.5">
                      <div className="flex justify-between">
                        <span className="text-slate-400">Trạng thái:</span>
                        <span className="font-extrabold text-white">
                          {billingDetails?.subscription?.status || (selectedUser.isPremium ? 'ACTIVE' : 'FREE')}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Mã gói:</span>
                        <span className="font-bold text-slate-200 uppercase">
                          {billingDetails?.subscription?.productId || '—'}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Hạn sử dụng:</span>
                        <span className="font-bold text-emerald-400">
                          {billingDetails?.subscription?.expiryTime
                            ? formatDateTimeVn(billingDetails.subscription.expiryTime)
                            : 'Không thời hạn'}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* VietQR Orders History */}
                  <div>
                    <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-2">
                      <CreditCard className="w-3.5 h-3.5 text-emerald-400" />
                      <span>Lịch Sử Đơn Nạp VietQR ({billingDetails?.orders?.length || 0})</span>
                    </h4>

                    {!billingDetails?.orders || billingDetails.orders.length === 0 ? (
                      <p className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 text-slate-500 text-center">
                        Chưa có giao dịch nạp VietQR nào
                      </p>
                    ) : (
                      <div className="space-y-2">
                        {billingDetails.orders.map((ord) => (
                          <div
                            key={ord.id}
                            className="p-3 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between"
                          >
                            <div>
                              <p className="font-mono font-bold text-emerald-400">{ord.orderCode}</p>
                              <p className="text-[11px] text-slate-400 mt-0.5">
                                {formatDateTimeVn(ord.createdAt)}
                              </p>
                            </div>
                            <div className="text-right">
                              <p className="font-extrabold text-white">{formatCurrencyVnd(ord.amount)}</p>
                              <span
                                className={`text-[10px] font-bold px-2 py-0.5 rounded-full inline-block mt-0.5 ${
                                  ord.status === 'PAID'
                                    ? 'bg-emerald-500/15 text-emerald-400'
                                    : 'bg-amber-500/15 text-amber-400'
                                }`}
                              >
                                {ord.status}
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Manual Grants History */}
                  <div>
                    <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-2">
                      <Gift className="w-3.5 h-3.5 text-purple-400" />
                      <span>Gói Bù Được Cấp Thủ Công ({billingDetails?.manualGrants?.length || 0})</span>
                    </h4>

                    {!billingDetails?.manualGrants || billingDetails.manualGrants.length === 0 ? (
                      <p className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 text-slate-500 text-center">
                        Chưa có lần cấp bù nào
                      </p>
                    ) : (
                      <div className="space-y-2">
                        {billingDetails.manualGrants.map((grant) => (
                          <div
                            key={grant.id}
                            className="p-3 rounded-xl bg-slate-900 border border-slate-800 space-y-1"
                          >
                            <div className="flex items-center justify-between">
                              <span className="font-mono text-purple-400 font-bold">{grant.id}</span>
                              <span
                                className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                  grant.revokedAt
                                    ? 'bg-rose-500/15 text-rose-400'
                                    : 'bg-emerald-500/15 text-emerald-400'
                                }`}
                              >
                                {grant.revokedAt ? 'Đã thu hồi' : 'Đang hiệu lực'}
                              </span>
                            </div>
                            <p className="text-slate-300 italic">{grant.reason}</p>
                            <p className="text-[10px] text-slate-500">
                              Từ {formatDateOnly(grant.startsAt)} đến {formatDateOnly(grant.endsAt)}
                            </p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              ) : (
                /* Account Tab */
                <div className="space-y-4 text-xs">
                  <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 space-y-2.5">
                    <div className="flex justify-between items-center">
                      <span className="text-slate-400">User ID:</span>
                      <div className="flex items-center gap-1 font-mono text-emerald-400 font-bold">
                        <span>{selectedUser.id}</span>
                        <button
                          onClick={() => copyToClipboard(selectedUser.id, 'User ID')}
                          className="text-slate-500 hover:text-slate-300"
                        >
                          <Copy className="w-3 h-3" />
                        </button>
                      </div>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Email:</span>
                      <span className="text-slate-200 font-medium">{selectedUser.email || '—'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">AuthProvider:</span>
                      <span className="text-slate-200 font-bold">{selectedUser.authProvider}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Ngày tạo:</span>
                      <span className="text-slate-200">{formatDateTimeVn(selectedUser.createdAt)}</span>
                    </div>
                  </div>

                  <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 space-y-2">
                    <span className="text-slate-400 font-bold block mb-1">Hạn Mức AI Tokens:</span>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Daily Quota:</span>
                      <span className="font-bold text-white">{selectedUser.dailyAiQuota} lượt/ngày</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Purchased Quota:</span>
                      <span className="font-bold text-emerald-400">
                        {selectedUser.purchasedAiQuota + selectedUser.purchasedChatQuota} lượt đã mua
                      </span>
                    </div>
                  </div>

                  <div className="p-3.5 rounded-2xl bg-slate-900/60 border border-slate-800 text-[11px] text-slate-400 leading-relaxed">
                    <b>Tuân thủ BR-17.1:</b> Dữ liệu y tế riêng tư (cân nặng, chiều cao, nhật ký bữa ăn, tin nhắn AI cá nhân) của người dùng được bảo vệ nghiêm ngặt và không hiển thị trên Admin Console.
                  </div>
                </div>
              )}
            </div>

            <div className="pt-4 border-t border-slate-800 mt-6">
              <Button variant="ghost" onClick={() => setIsDetailDrawerOpen(false)} className="w-full">
                Đóng
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* QUICK GRANT MODAL FOR SELECTED USER */}
      <Modal
        isOpen={isQuickGrantOpen}
        onClose={() => setIsQuickGrantOpen(false)}
        title={`Cấp Gói Bù Cho @${selectedUser?.username}`}
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsQuickGrantOpen(false)} disabled={actionLoading}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleQuickGrant} isLoading={actionLoading}>
              Xác Nhận Cấp Bù
            </Button>
          </>
        }
      >
        <form onSubmit={handleQuickGrant} className="space-y-4 py-2 text-xs">
          <div className="p-3.5 rounded-2xl bg-purple-500/10 border border-purple-500/20 text-purple-300">
            Cấp thêm ngày Premium cho người dùng này theo quy chuẩn <b>BR-17.3</b> (1-90 ngày).
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">
              Số Ngày Cấp (1 - 90 ngày) <span className="text-rose-400">*</span>
            </label>
            <input
              type="number"
              min={1}
              max={90}
              value={grantDays}
              onChange={(e) => setGrantDays(Number(e.target.value))}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white focus:outline-none focus:border-purple-500"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">
              Lý Do Giải Trình Đền Bù <span className="text-rose-400">*</span>
            </label>
            <textarea
              rows={3}
              value={grantReason}
              onChange={(e) => setGrantReason(e.target.value)}
              placeholder="VD: Đền bù lỗi gián đoạn dịch vụ AI Vision..."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500"
            />
            <p className="text-[11px] text-slate-500 mt-1">
              Tối thiểu 10 ký tự ({grantReason.trim().length}/10)
            </p>
          </div>
        </form>
      </Modal>

      {/* CREATE USER MODAL */}
      <Modal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        title="Tạo Người Dùng Mới"
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsCreateOpen(false)} disabled={actionLoading}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleCreateUser} isLoading={actionLoading}>
              Tạo tài khoản
            </Button>
          </>
        }
      >
        <form onSubmit={handleCreateUser} className="space-y-4 py-1">
          <Input
            label="Tên đăng nhập"
            value={createForm.username}
            onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
            placeholder="VD: user01..."
            required
          />
          <Input
            label="Mật khẩu ban đầu"
            type="password"
            value={createForm.password}
            onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
            placeholder="Tối thiểu 6 ký tự..."
            required
          />
          <Input
            label="Email liên hệ"
            type="email"
            value={createForm.email}
            onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
            placeholder="user@example.com..."
          />
          <Input
            label="Họ và tên"
            value={createForm.name}
            onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
            placeholder="Nguyễn Văn A..."
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Vai trò"
              value={createForm.role}
              onChange={(e) => setCreateForm({ ...createForm, role: e.target.value as Role })}
              options={[
                { value: 'USER', label: 'Thành viên (USER)' },
                { value: 'ADMIN', label: 'Quản trị (ADMIN)' },
              ]}
            />
            <Input
              label="Hạn mức AI/ngày"
              type="number"
              value={createForm.dailyAiQuota}
              onChange={(e) => setCreateForm({ ...createForm, dailyAiQuota: Number(e.target.value) })}
            />
          </div>
        </form>
      </Modal>

      {/* EDIT USER MODAL */}
      <Modal
        isOpen={isEditOpen}
        onClose={() => setIsEditOpen(false)}
        title={`Chỉnh Sửa Tài Khoản @${selectedUser?.username}`}
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsEditOpen(false)} disabled={actionLoading}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleEditUser} isLoading={actionLoading}>
              Lưu thay đổi
            </Button>
          </>
        }
      >
        <form onSubmit={handleEditUser} className="space-y-4 py-1">
          <Input
            label="Họ và tên"
            value={editForm.name || ''}
            onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
          />
          <Input
            label="Email"
            type="email"
            value={editForm.email || ''}
            onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Vai trò"
              value={editForm.role}
              onChange={(e) => setEditForm({ ...editForm, role: e.target.value as Role })}
              options={[
                { value: 'USER', label: 'USER' },
                { value: 'ADMIN', label: 'ADMIN' },
              ]}
            />
            <Select
              label="Trạng thái"
              value={editForm.isActive ? 'true' : 'false'}
              onChange={(e) => setEditForm({ ...editForm, isActive: e.target.value === 'true' })}
              options={[
                { value: 'true', label: 'Hoạt động' },
                { value: 'false', label: 'Khóa tài khoản' },
              ]}
            />
          </div>
          <Input
            label="Hạn mức AI / ngày"
            type="number"
            value={editForm.dailyAiQuota || 10}
            onChange={(e) => setEditForm({ ...editForm, dailyAiQuota: Number(e.target.value) })}
          />
        </form>
      </Modal>

      {/* RESET PASSWORD MODAL */}
      <Modal
        isOpen={isResetPassOpen}
        onClose={() => setIsResetPassOpen(false)}
        title={`Đặt Lại Mật Khẩu @${selectedUser?.username}`}
        maxWidth="sm"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsResetPassOpen(false)} disabled={actionLoading}>
              Hủy
            </Button>
            <Button variant="danger" onClick={handleResetPassword} isLoading={actionLoading}>
              Xác nhận đổi
            </Button>
          </>
        }
      >
        <div className="space-y-3 py-1">
          <Input
            label="Mật khẩu mới"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="Tối thiểu 6 ký tự..."
            required
          />
          <p className="text-[11px] text-slate-400">
            Mật khẩu mới sẽ được mã hóa Bcrypt an toàn trên máy chủ backend.
          </p>
        </div>
      </Modal>

      {/* DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDeleteUser}
        title="Xóa người dùng"
        message={`Bạn có chắc chắn muốn xóa tài khoản @${selectedUser?.username}? Thao tác này không thể hoàn tác.`}
        isLoading={actionLoading}
      />
    </div>
  );
};
