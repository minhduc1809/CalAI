import React, { useEffect, useState } from 'react';
import {
  Users as UsersIcon,
  Search,
  Sparkles,
  CreditCard,
  Gift,
  Copy,
  Check,
  ChevronLeft,
  ChevronRight,
  X,
  RefreshCw,
} from 'lucide-react';
import { usersApi } from '../api/users.api';
import { User, AdminUserBillingDetails } from '../types';
import { Button } from '../components/ui/Button';
import { formatCurrencyVnd, formatDateTimeVn } from '../utils/formatters';
import { toast } from 'sonner';
import { useNavigate } from 'react-router-dom';

export const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('ALL');
  const [premiumFilter, setPremiumFilter] = useState<string>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [copiedText, setCopiedText] = useState<string | null>(null);

  // User Billing Details Drawer
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [billingDetails, setBillingDetails] = useState<AdminUserBillingDetails | null>(null);
  const [isLoadingBilling, setIsLoadingBilling] = useState(false);

  const navigate = useNavigate();

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const res = await usersApi.getUsers({
        page,
        limit: 15,
        search: searchTerm.trim() || undefined,
        role: roleFilter !== 'ALL' ? (roleFilter as any) : undefined,
        isPremium: premiumFilter !== 'ALL' ? premiumFilter : undefined,
      });
      setUsers(res.data);
      setTotalPages(res.meta.totalPages || 1);
      setTotalRecords(res.meta.total || 0);
    } catch {
      toast.error('Lỗi khi tải danh sách người dùng');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [page, roleFilter, premiumFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    fetchUsers();
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
    setTimeout(() => setCopiedText(null), 2000);
  };

  const handleOpenBilling = async (user: User) => {
    setSelectedUser(user);
    setIsLoadingBilling(true);
    try {
      const details = await usersApi.getUserBilling(user.id);
      setBillingDetails(details);
    } catch {
      toast.error('Không thể lấy chi tiết tài chính người dùng');
    } finally {
      setIsLoadingBilling(false);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-[#334155]">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-[#F8FAFC] flex items-center gap-2.5">
            <UsersIcon className="w-6 h-6 text-emerald-400" />
            Quản Lý Người Dùng & Billing
          </h1>
          <p className="text-xs text-[#94A3B8] mt-1">
            Tra cứu tài khoản, quản lý gói Premium và đối soát tài chính người dùng (Tuân thủ quyền riêng tư BR-17.1)
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={fetchUsers}
            isLoading={isLoading}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
            className="border border-[#334155] text-xs text-[#F8FAFC]"
          >
            Làm mới
          </Button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 rounded-2xl bg-[#1E293B] border border-[#334155] flex flex-col md:flex-row items-center justify-between gap-3 shadow-sm">
        {/* Search */}
        <form onSubmit={handleSearchSubmit} className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-[#94A3B8] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo email hoặc tên..."
            className="w-full bg-[#0F172A] border border-[#334155] rounded-xl pl-9 pr-4 py-2 text-xs text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all font-mono"
          />
        </form>

        {/* Filters */}
        <div className="flex items-center gap-2 overflow-x-auto w-full md:w-auto">
          {/* Role Filter */}
          <div className="flex items-center gap-1 bg-[#0F172A] p-1 rounded-xl border border-[#334155] text-xs">
            <span className="text-[10px] uppercase font-bold text-[#94A3B8] px-2">Role:</span>
            {['ALL', 'USER', 'ADMIN'].map((r) => (
              <button
                key={r}
                onClick={() => {
                  setRoleFilter(r);
                  setPage(1);
                }}
                className={`px-2.5 py-1 rounded-lg font-semibold text-xs transition-all ${
                  roleFilter === r
                    ? 'bg-emerald-500 text-white shadow-sm'
                    : 'text-[#94A3B8] hover:text-[#F8FAFC]'
                }`}
              >
                {r === 'ALL' ? 'Tất cả' : r}
              </button>
            ))}
          </div>

          {/* Premium Filter */}
          <div className="flex items-center gap-1 bg-[#0F172A] p-1 rounded-xl border border-[#334155] text-xs">
            <span className="text-[10px] uppercase font-bold text-[#94A3B8] px-2">Gói:</span>
            {[
              { id: 'ALL', label: 'Tất cả' },
              { id: 'true', label: 'Premium' },
              { id: 'false', label: 'Free' },
            ].map((p) => (
              <button
                key={p.id}
                onClick={() => {
                  setPremiumFilter(p.id);
                  setPage(1);
                }}
                className={`px-2.5 py-1 rounded-lg font-semibold text-xs transition-all ${
                  premiumFilter === p.id
                    ? 'bg-emerald-500 text-white shadow-sm'
                    : 'text-[#94A3B8] hover:text-[#F8FAFC]'
                }`}
              >
                {p.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-[#1E293B] border border-[#334155] rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-[#334155] bg-[#0F172A]/80 text-[#94A3B8] uppercase text-[10px] tracking-wider font-semibold">
                <th className="py-3.5 px-4">Người Dùng / ID</th>
                <th className="py-3.5 px-4">Email</th>
                <th className="py-3.5 px-4">Quyền (Role)</th>
                <th className="py-3.5 px-4">Gói Dịch Vụ</th>
                <th className="py-3.5 px-4">Hạn Dùng / Auto-Renew</th>
                <th className="py-3.5 px-4">Ngày Tạo</th>
                <th className="py-3.5 px-4 text-right">Chi Tiết Billing</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#334155]">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#94A3B8]">
                    <div className="inline-block w-6 h-6 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mb-2" />
                    <p>Đang tải danh sách người dùng...</p>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#94A3B8]">
                    <p className="text-sm font-semibold text-[#F8FAFC]">Không có người dùng nào khớp</p>
                    <p className="text-xs mt-1 text-[#94A3B8]">Vui lòng thử điều chỉnh lại bộ lọc tìm kiếm</p>
                  </td>
                </tr>
              ) : (
                users.map((u) => {
                  const isUserPremium = u.isPremium;
                  const expiry = u.subscriptionState?.expiryTime;
                  const isAutoRenew = u.subscriptionState?.autoRenewing;

                  return (
                    <tr
                      key={u.id}
                      className="hover:bg-[#0F172A]/40 transition-colors cursor-pointer"
                      onClick={() => handleOpenBilling(u)}
                    >
                      {/* Tên & Username */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2.5">
                          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-emerald-500/20 to-teal-400/20 border border-emerald-500/30 flex items-center justify-center font-bold text-xs text-emerald-400 shrink-0">
                            {(u.name || u.username || 'U')[0].toUpperCase()}
                          </div>
                          <div>
                            <p className="font-bold text-[#F8FAFC] truncate max-w-[130px]">
                              {u.name || u.username}
                            </p>
                            <div className="flex items-center gap-1.5 mt-0.5">
                              <span className="font-mono text-[10px] text-[#94A3B8]">
                                @{u.username}
                              </span>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  copyToClipboard(u.id, 'User ID');
                                }}
                                className="p-0.5 text-[#94A3B8] hover:text-[#F8FAFC]"
                                title="Copy User ID"
                              >
                                {copiedText === u.id ? (
                                  <Check className="w-2.5 h-2.5 text-emerald-400" />
                                ) : (
                                  <Copy className="w-2.5 h-2.5" />
                                )}
                              </button>
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Email */}
                      <td className="py-3.5 px-4 font-mono text-[11px] text-[#F8FAFC]">
                        {u.email || '—'}
                      </td>

                      {/* Role */}
                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                            u.role === 'ADMIN'
                              ? 'bg-purple-500/15 text-purple-400 border-purple-500/30'
                              : 'bg-slate-700 text-slate-300 border-slate-600'
                          }`}
                        >
                          {u.role}
                        </span>
                      </td>

                      {/* Gói dịch vụ */}
                      <td className="py-3.5 px-4">
                        {isUserPremium ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                            <Sparkles className="w-3 h-3" />
                            PREMIUM
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-[#94A3B8] border border-[#334155]">
                            FREE TIER
                          </span>
                        )}
                      </td>

                      {/* Hạn dùng / Auto Renew */}
                      <td className="py-3.5 px-4 text-[#94A3B8] text-[11px]">
                        {expiry ? (
                          <div>
                            <span className="font-mono text-[#F8FAFC]">
                              {formatDateTimeVn(expiry).split(' - ')[1]}
                            </span>
                            {isAutoRenew && (
                              <span className="ml-1 text-[9px] px-1 py-0.2 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                                Tự gia hạn
                              </span>
                            )}
                          </div>
                        ) : (
                          '—'
                        )}
                      </td>

                      {/* Ngày tạo */}
                      <td className="py-3.5 px-4 font-mono text-[#94A3B8] text-[11px]">
                        {formatDateTimeVn(u.createdAt).split(' - ')[1]}
                      </td>

                      {/* Nút Xem Billing */}
                      <td className="py-3.5 px-4 text-right">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleOpenBilling(u);
                          }}
                          className="px-2.5 py-1.5 rounded-lg bg-[#0F172A] hover:bg-[#334155] text-emerald-400 border border-[#334155] font-semibold text-xs transition-colors inline-flex items-center gap-1"
                        >
                          <CreditCard className="w-3.5 h-3.5" />
                          <span>Chi tiết</span>
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="p-4 border-t border-[#334155] flex items-center justify-between text-xs text-[#94A3B8] bg-[#0F172A]/50">
          <div>
            Hiển thị <span className="font-bold text-[#F8FAFC]">{users.length}</span> /{' '}
            <span className="font-bold text-[#F8FAFC]">{totalRecords}</span> người dùng
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page <= 1}
              className="p-1.5 rounded-lg bg-[#1E293B] border border-[#334155] text-[#F8FAFC] disabled:opacity-40 hover:bg-[#334155] transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-mono px-2">
              Trang <span className="text-[#F8FAFC] font-bold">{page}</span> / {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page >= totalPages}
              className="p-1.5 rounded-lg bg-[#1E293B] border border-[#334155] text-[#F8FAFC] disabled:opacity-40 hover:bg-[#334155] transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* USER BILLING DRAWER (SLIDE OVER) */}
      {selectedUser && (
        <div className="fixed inset-0 z-50 overflow-hidden">
          {/* Backdrop */}
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity"
            onClick={() => setSelectedUser(null)}
          />

          <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
            <div className="w-screen max-w-md bg-[#1E293B] border-l border-[#334155] shadow-2xl flex flex-col">
              {/* Drawer Header */}
              <div className="p-5 border-b border-[#334155] flex items-center justify-between bg-[#0F172A]/80">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 p-0.5 flex items-center justify-center font-bold text-base text-emerald-400 shrink-0">
                    <div className="w-full h-full bg-[#0F172A] rounded-[10px] flex items-center justify-center">
                      {(selectedUser.name || selectedUser.username)[0].toUpperCase()}
                    </div>
                  </div>
                  <div>
                    <h3 className="font-extrabold text-base text-[#F8FAFC]">
                      {selectedUser.name || selectedUser.username}
                    </h3>
                    <p className="text-xs text-[#94A3B8] font-mono">{selectedUser.email}</p>
                  </div>
                </div>

                <button
                  onClick={() => setSelectedUser(null)}
                  className="p-1.5 rounded-lg text-[#94A3B8] hover:text-[#F8FAFC] hover:bg-[#334155] transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Drawer Content */}
              <div className="flex-1 overflow-y-auto p-5 space-y-6">
                {isLoadingBilling ? (
                  <div className="py-20 text-center text-[#94A3B8]">
                    <div className="inline-block w-8 h-8 border-3 border-emerald-500 border-t-transparent rounded-full animate-spin mb-3" />
                    <p className="text-xs">Đang tải hồ sơ tài chính người dùng...</p>
                  </div>
                ) : (
                  <>
                    {/* Subscription State Widget */}
                    <div className="p-4 rounded-2xl bg-[#0F172A] border border-[#334155] space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider">
                          Trạng Thái Gói Cước
                        </span>
                        {billingDetails?.user?.isPremium ? (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center gap-1">
                            <Sparkles className="w-3 h-3" /> ACTIVE PREMIUM
                          </span>
                        ) : (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-[#94A3B8]">
                            FREE TIER
                          </span>
                        )}
                      </div>

                      {billingDetails?.subscription ? (
                        <div className="space-y-2 text-xs pt-1 border-t border-[#334155]/60">
                          <div className="flex justify-between">
                            <span className="text-[#94A3B8]">Mã sản phẩm (SKU):</span>
                            <span className="font-mono font-bold text-[#F8FAFC]">
                              {billingDetails.subscription.productId || 'premium_monthly'}
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-[#94A3B8]">Hạn sử dụng:</span>
                            <span className="font-mono text-emerald-400 font-bold">
                              {billingDetails.subscription.expiryTime
                                ? formatDateTimeVn(billingDetails.subscription.expiryTime)
                                : 'Không giới hạn'}
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-[#94A3B8]">Tự động gia hạn:</span>
                            <span className="text-[#F8FAFC]">
                              {billingDetails.subscription.autoRenewing ? 'Có (Auto-renew)' : 'Không'}
                            </span>
                          </div>
                        </div>
                      ) : (
                        <p className="text-xs text-[#94A3B8] pt-1">
                          Người dùng chưa đăng ký gói Premium trả phí nào.
                        </p>
                      )}

                      {/* Quick Action: Cấp gói bù */}
                      <div className="pt-2">
                        <button
                          onClick={() => {
                            setSelectedUser(null);
                            navigate(`/grants?userId=${selectedUser.id}`);
                          }}
                          className="w-full py-2 rounded-xl bg-emerald-500/15 hover:bg-emerald-500/25 text-emerald-400 border border-emerald-500/30 font-bold text-xs flex items-center justify-center gap-2 transition-colors"
                        >
                          <Gift className="w-4 h-4" />
                          <span>Cấp ngày Premium thủ công (Đền bù/Hỗ trợ)</span>
                        </button>
                      </div>
                    </div>

                    {/* Order History */}
                    <div>
                      <h4 className="text-xs font-bold text-[#F8FAFC] uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                        <CreditCard className="w-3.5 h-3.5 text-emerald-400" />
                        Lịch Sử Đơn Nạp VietQR ({billingDetails?.orders?.length || 0})
                      </h4>

                      {billingDetails?.orders && billingDetails.orders.length > 0 ? (
                        <div className="space-y-2">
                          {billingDetails.orders.map((o) => (
                            <div
                              key={o.id}
                              className="p-3 rounded-xl bg-[#0F172A] border border-[#334155] text-xs flex items-center justify-between"
                            >
                              <div>
                                <span className="font-mono font-bold text-emerald-400">
                                  {o.orderCode}
                                </span>
                                <p className="text-[10px] text-[#94A3B8] mt-0.5 font-mono">
                                  {formatDateTimeVn(o.createdAt)}
                                </p>
                              </div>
                              <div className="text-right">
                                <p className="font-mono font-bold text-[#F8FAFC]">
                                  {formatCurrencyVnd(o.amount)}
                                </p>
                                <span
                                  className={`text-[9px] font-bold px-1.5 py-0.2 rounded border ${
                                    o.status === 'PAID'
                                      ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30'
                                      : 'bg-amber-500/15 text-amber-400 border-amber-500/30'
                                  }`}
                                >
                                  {o.status}
                                </span>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-xs text-[#94A3B8] p-3 rounded-xl bg-[#0F172A] border border-[#334155]/60">
                          Chưa có đơn nạp VietQR nào.
                        </p>
                      )}
                    </div>

                    {/* Manual Grants History */}
                    <div>
                      <h4 className="text-xs font-bold text-[#F8FAFC] uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                        <Gift className="w-3.5 h-3.5 text-purple-400" />
                        Lịch Sử Cấp Bù Premium Thủ Công ({billingDetails?.manualGrants?.length || 0})
                      </h4>

                      {billingDetails?.manualGrants && billingDetails.manualGrants.length > 0 ? (
                        <div className="space-y-2">
                          {billingDetails.manualGrants.map((g) => (
                            <div
                              key={g.id}
                              className="p-3 rounded-xl bg-[#0F172A] border border-[#334155] text-xs space-y-1.5"
                            >
                              <div className="flex items-center justify-between">
                                <span className="font-semibold text-[#F8FAFC]">
                                  Từ {formatDateTimeVn(g.startsAt).split(' - ')[1]} đến {formatDateTimeVn(g.endsAt).split(' - ')[1]}
                                </span>
                                {g.revokedAt ? (
                                  <span className="text-[9px] font-bold px-1.5 py-0.2 rounded bg-red-500/20 text-red-400 border border-red-500/30">
                                    ĐÃ THU HỒI
                                  </span>
                                ) : (
                                  <span className="text-[9px] font-bold px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                                    ĐANG HIỆU LỰC
                                  </span>
                                )}
                              </div>
                              <p className="text-[11px] text-[#94A3B8] italic">
                                Lý do: {g.reason}
                              </p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-xs text-[#94A3B8] p-3 rounded-xl bg-[#0F172A] border border-[#334155]/60">
                          Chưa có lịch sử cấp bù thủ công.
                        </p>
                      )}
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
