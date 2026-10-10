import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Users,
  CreditCard,
  Clock,
  Sparkles,
  TrendingUp,
  RefreshCw,
  CheckCircle2,
  XCircle,
  Gift,
  ArrowRight,
  ShieldCheck,
  Check,
  Copy,
  ExternalLink,
  ChevronRight,
  UserCheck,
  DollarSign,
  Activity,
} from 'lucide-react';
import {
  ResponsiveContainer,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  CartesianGrid,
  Legend,
} from 'recharts';
import { adminDashboardApi } from '../api/admin-dashboard.api';
import { paymentsApi } from '../api/payments.api';
import { auditLogsApi } from '../api/audit-logs.api';
import { AdminDashboardSummary, AdminPaymentOrder, AdminAuditLog } from '../types';
import { StatCard } from '../components/ui/StatCard';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { formatNumber, formatCurrencyVnd, formatDateTimeVn } from '../utils/formatters';
import { toast } from 'sonner';

export const Dashboard: React.FC = () => {
  const [summary, setSummary] = useState<AdminDashboardSummary | null>(null);
  const [pendingOrders, setPendingOrders] = useState<AdminPaymentOrder[]>([]);
  const [recentLogs, setRecentLogs] = useState<AdminAuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Quick Approve State
  const [orderToApprove, setOrderToApprove] = useState<AdminPaymentOrder | null>(null);
  const [isApproving, setIsApproving] = useState(false);

  const navigate = useNavigate();

  const fetchData = async (): Promise<boolean> => {
    try {
      const [sumRes, ordersRes, logsRes] = await Promise.all([
        adminDashboardApi.getSummary(),
        paymentsApi.getOrders({ status: 'PENDING', limit: 5 }),
        auditLogsApi.getAuditLogs({ limit: 6 }),
      ]);
      setSummary(sumRes);
      setPendingOrders(ordersRes.data || []);
      setRecentLogs(logsRes.data || []);
      setLoadError(null);
      return true;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Lỗi không xác định';
      setLoadError(message);
      toast.error(`Không thể tải dữ liệu Dashboard: ${message}`);
      return false;
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();

    const handleGlobalRefresh = () => {
      setIsRefreshing(true);
      void fetchData().then((updated) => {
        if (updated) toast.success('Đã cập nhật dữ liệu Dashboard');
      });
    };

    window.addEventListener('admin-refresh-data', handleGlobalRefresh);
    return () => window.removeEventListener('admin-refresh-data', handleGlobalRefresh);
  }, []);

  const handleManualRefresh = () => {
    setIsRefreshing(true);
    void fetchData().then((updated) => {
      if (updated) toast.success('Đã làm mới dữ liệu mới nhất');
    });
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleConfirmApprove = async () => {
    if (!orderToApprove) return;
    setIsApproving(true);
    try {
      await paymentsApi.approveOrder(orderToApprove.id);
      toast.success(`Đã duyệt đơn ${orderToApprove.orderCode} & kích hoạt gói thành công`);
      setOrderToApprove(null);
      void fetchData();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi duyệt đơn nạp');
    } finally {
      setIsApproving(false);
    }
  };

  // KPI calculations
  const totalUsers = summary?.users?.total ?? 0;
  const new7d = summary?.users?.newLast7Days ?? 0;
  const new30d = summary?.users?.newLast30Days ?? 0;
  const activePremiums = summary?.subscriptions?.activePremiums ?? 0;
  const pendingOrdersCount = summary?.orders?.pending ?? 0;
  const paidOrdersCount = summary?.orders?.paid ?? 0;
  const totalRevenue = summary?.revenue?.totalVnd ?? 0;

  const conversionRate = totalUsers > 0 ? ((activePremiums / totalUsers) * 100).toFixed(1) : '0.0';
  const freeUsers = Math.max(0, totalUsers - activePremiums);

  // Biểu đồ 2: Data Tỷ lệ chuyển đổi Donut
  const conversionDonutData = useMemo(() => [
    { name: 'Active Premium', value: activePremiums, color: '#10B981' },
    { name: 'Free Users', value: freeUsers, color: '#334155' },
  ], [activePremiums, freeUsers]);

  // Biểu đồ 3: Data Phân bổ đơn hàng VietQR
  const totalOrdersTracked = pendingOrdersCount + paidOrdersCount || 1;
  const paidPct = Math.round((paidOrdersCount / totalOrdersTracked) * 100);
  const pendingPct = Math.round((pendingOrdersCount / totalOrdersTracked) * 100);

  // Biểu đồ 4: Data Tốc độ gia tăng người dùng (7 ngày vs TB tuần 30 ngày)
  const weeklyAvg30d = Math.round(new30d / 4.2);
  const velocityData = useMemo(() => [
    {
      period: 'Người dùng mới',
      '7 ngày qua': new7d,
      'TB tuần (30 ngày)': weeklyAvg30d,
    },
  ], [new7d, weeklyAvg30d]);

  return (
    <div className="space-y-7 animate-fade-in pb-12">
      {/* Top Banner Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-[#334155]">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-[#F8FAFC] flex items-center gap-2.5">
            Tổng quan Vận hành & Doanh thu
            <span className="text-xs px-2.5 py-0.5 rounded-full font-mono font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
              LIVE
            </span>
          </h1>
          <p className="text-xs text-[#94A3B8] mt-1">
            Giám sát thời gian thực các chỉ số tài chính, người dùng và luồng phê duyệt đơn VietQR (BR-17)
          </p>
        </div>

        {loadError && (
          <div className="rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
            Không thể tải đầy đủ dữ liệu từ backend: {loadError}
          </div>
        )}

        <div className="flex items-center gap-2.5 self-start sm:self-auto">
          <button
            onClick={handleManualRefresh}
            disabled={isRefreshing || isLoading}
            className="flex items-center gap-2 px-3.5 py-2 rounded-xl bg-[#1E293B] hover:bg-[#334155]/80 text-[#F8FAFC] border border-[#334155] text-xs font-semibold transition-all disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing || isLoading ? 'animate-spin text-emerald-400' : ''}`} />
            <span>{isRefreshing || isLoading ? 'Đang cập nhật...' : 'Làm mới số liệu'}</span>
          </button>
        </div>
      </div>

      {/* 4 THẺ KPI METRIC CARDS ĐẦU TRANG */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* KPI 1: Tổng Users */}
        <StatCard
          title="Tổng Người Dùng"
          value={formatNumber(totalUsers)}
          subtitle="Tài khoản hệ thống"
          icon={<Users className="w-5 h-5 text-emerald-400" />}
          trend={{
            value: `+${formatNumber(new7d)}`,
            isPositive: true,
            label: '7 ngày qua',
          }}
          accentColor="emerald"
        />

        {/* KPI 2: Active Premium */}
        <StatCard
          title="Thành Viên Premium"
          value={formatNumber(activePremiums)}
          subtitle={`Tỷ lệ chuyển đổi: ${conversionRate}%`}
          icon={<Sparkles className="w-5 h-5 text-teal-400" />}
          trend={{
            value: `${conversionRate}%`,
            isPositive: true,
            label: 'Conversion',
          }}
          accentColor="cyan"
          highlight={activePremiums > 0}
        />

        {/* KPI 3: Đơn Chờ Duyệt VietQR */}
        <StatCard
          title="Đơn VietQR Chờ Duyệt"
          value={`${formatNumber(pendingOrdersCount)} ĐƠN`}
          subtitle={pendingOrdersCount > 0 ? 'Cần xử lý kích hoạt' : 'Hệ thống đã khớp hết'}
          icon={<Clock className="w-5 h-5 text-amber-400" />}
          trend={{
            value: pendingOrdersCount > 0 ? 'Chờ duyệt' : '0 pending',
            isPositive: pendingOrdersCount === 0,
            label: 'Trạng thái',
          }}
          accentColor="amber"
          highlight={pendingOrdersCount > 0}
        />

        {/* KPI 4: Doanh Thu VietQR */}
        <StatCard
          title="Doanh Thu VietQR"
          value={formatCurrencyVnd(totalRevenue)}
          subtitle={`Từ ${formatNumber(paidOrdersCount)} đơn hoàn tất`}
          icon={<DollarSign className="w-5 h-5 text-emerald-400" />}
          trend={{
            value: `${formatNumber(paidOrdersCount)} đơn`,
            isPositive: true,
            label: 'Đã thanh toán',
          }}
          accentColor="emerald"
        />
      </div>

      {/* KHUNG ĐỒ THỊ TRỰC QUAN (CHARTS SECTION) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* BIỂU ĐỒ 1: XU HƯỚNG DOANH THU & DÒNG TIỀN 30 NGÀY (Chiếm 2 cột) */}
        <div className="lg:col-span-2 bg-[#1E293B] border border-[#334155] rounded-2xl p-5 sm:p-6 shadow-sm">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-[#334155] gap-2">
            <div>
              <div className="flex items-center gap-2">
                <span className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  <TrendingUp className="w-4 h-4" />
                </span>
                <h3 className="text-sm font-bold text-[#F8FAFC]">Doanh Thu VietQR</h3>
              </div>
              <p className="text-xs text-[#94A3B8] mt-1">
                Tổng doanh thu và số đơn đã thanh toán do API Dashboard cung cấp
              </p>
            </div>
          </div>

          <div className="h-72 flex flex-col items-center justify-center text-center">
            <DollarSign className="w-9 h-9 text-emerald-400 mb-3" />
            <p className="text-3xl font-extrabold tracking-tight text-[#F8FAFC]">
              {formatCurrencyVnd(totalRevenue)}
            </p>
            <p className="text-sm font-semibold text-amber-400 mt-2">
              {formatNumber(paidOrdersCount)} đơn đã thanh toán
            </p>
            <p className="text-xs text-[#94A3B8] mt-4 max-w-md">
              Backend hiện chỉ cung cấp số liệu tổng hợp, chưa có dữ liệu doanh thu theo ngày để vẽ xu hướng.
            </p>
          </div>
        </div>

        {/* BIỂU ĐỒ 2: TỶ LỆ CHUYỂN ĐỔI PREMIUM (DONUT / GAUGE METER) */}
        <div className="bg-[#1E293B] border border-[#334155] rounded-2xl p-5 sm:p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 pb-3 border-b border-[#334155]">
              <span className="p-1.5 rounded-lg bg-teal-500/10 text-teal-400 border border-teal-500/20">
                <Sparkles className="w-4 h-4" />
              </span>
              <h3 className="text-sm font-bold text-[#F8FAFC]">
                Cơ Cấu Người Dùng & Tỷ Lệ Chuyển Đổi Premium
              </h3>
            </div>
            <p className="text-xs text-[#94A3B8] mt-1.5">
              Tỷ lệ giữa thành viên trả phí (Active Pro) và người dùng miễn phí (Free Tier)
            </p>
          </div>

          <div className="relative h-56 flex items-center justify-center my-2">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={conversionDonutData}
                  cx="50%"
                  cy="50%"
                  innerRadius={65}
                  outerRadius={85}
                  paddingAngle={4}
                  dataKey="value"
                  stroke="#0F172A"
                  strokeWidth={3}
                >
                  {conversionDonutData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(value: any, name: any) => [
                    `${formatNumber(Number(value))} users`,
                    name,
                  ]}
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    fontSize: '12px',
                    color: '#F8FAFC',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>

            {/* Tâm Donut hiển thị phần trăm */}
            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
              <span className="text-3xl font-extrabold tracking-tight text-[#10B981]">
                {conversionRate}%
              </span>
              <span className="text-[11px] font-semibold text-[#94A3B8] uppercase tracking-wider mt-0.5">
                Chuyển đổi
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2 pt-3 border-t border-[#334155] text-xs">
            <div className="p-2 rounded-xl bg-[#0F172A] border border-[#334155]/60">
              <div className="flex items-center gap-1.5 text-emerald-400 font-semibold mb-0.5">
                <span className="w-2 h-2 rounded-full bg-[#10B981]" />
                Active Premium
              </div>
              <p className="text-base font-bold text-[#F8FAFC]">
                {formatNumber(activePremiums)}
              </p>
            </div>
            <div className="p-2 rounded-xl bg-[#0F172A] border border-[#334155]/60">
              <div className="flex items-center gap-1.5 text-[#94A3B8] font-semibold mb-0.5">
                <span className="w-2 h-2 rounded-full bg-[#334155]" />
                Free Users
              </div>
              <p className="text-base font-bold text-[#F8FAFC]">
                {formatNumber(freeUsers)}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* HÀNG BIỂU ĐỒ 3, 4 & 5 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* TRẠNG THÁI XỬ LÝ GIAO DỊCH VIETQR */}
        <div className="bg-[#1E293B] border border-[#334155] rounded-2xl p-5 sm:p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 pb-3 border-b border-[#334155]">
              <span className="p-1.5 rounded-lg bg-amber-500/10 text-amber-400 border border-amber-500/20">
                <CreditCard className="w-4 h-4" />
              </span>
              <h3 className="text-sm font-bold text-[#F8FAFC]">
                Trạng Thái Xử Lý Giao Dịch VietQR
              </h3>
            </div>
            <p className="text-xs text-[#94A3B8] mt-1.5">
              Tỷ lệ khớp lệnh hoàn tất (PAID) vs Đang chờ xử lý (PENDING)
            </p>
          </div>

          <div className="my-6 space-y-4">
            {/* Visual Rounded Stacked Progress Bar */}
            <div className="h-6 w-full bg-[#0F172A] rounded-full overflow-hidden flex border border-[#334155] p-0.5">
              <div
                style={{ width: `${paidPct}%` }}
                className="h-full bg-[#10B981] rounded-l-full transition-all duration-500 relative group"
                title={`Đã thanh toán: ${paidPct}%`}
              />
              <div
                style={{ width: `${pendingPct}%` }}
                className="h-full bg-[#F59E0B] rounded-r-full transition-all duration-500 relative group"
                title={`Đang chờ: ${pendingPct}%`}
              />
            </div>

            {/* Breakdown details */}
            <div className="space-y-2.5 pt-2">
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-[#0F172A] border border-[#334155]/60 text-xs">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 rounded bg-[#10B981]" />
                  <span className="text-[#F8FAFC] font-semibold">PAID (Đã kích hoạt)</span>
                </div>
                <div className="font-mono font-bold text-emerald-400">
                  {paidOrdersCount} đơn ({paidPct}%)
                </div>
              </div>

              <div className="flex items-center justify-between p-2.5 rounded-xl bg-[#0F172A] border border-[#334155]/60 text-xs">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 rounded bg-[#F59E0B]" />
                  <span className="text-[#F8FAFC] font-semibold">PENDING (Chờ quét mã)</span>
                </div>
                <div className="font-mono font-bold text-amber-400">
                  {pendingOrdersCount} đơn ({pendingPct}%)
                </div>
              </div>
            </div>
          </div>

          <div className="text-[11px] text-[#94A3B8] flex items-center justify-between pt-2 border-t border-[#334155]">
            <span>Tự động kiểm tra sao kê</span>
            <span className="font-mono text-emerald-400 font-bold">VietQR 24/7</span>
          </div>
        </div>

        {/* TỐC ĐỘ MỞ RỘNG NGƯỜI DÙNG MỚI (ACQUISITION VELOCITY) */}
        <div className="bg-[#1E293B] border border-[#334155] rounded-2xl p-5 sm:p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 pb-3 border-b border-[#334155]">
              <span className="p-1.5 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                <UserCheck className="w-4 h-4" />
              </span>
              <h3 className="text-sm font-bold text-[#F8FAFC]">
                Tốc Độ Mở Rộng Người Dùng Mới
              </h3>
            </div>
            <p className="text-xs text-[#94A3B8] mt-1.5">
              So sánh lượng đăng ký 7 ngày qua vs Mức TB tuần 30 ngày (Acquisition Velocity)
            </p>
          </div>

          <div className="h-56 w-full my-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={velocityData} margin={{ top: 15, right: 15, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.6} />
                <XAxis dataKey="period" stroke="#94A3B8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94A3B8" fontSize={11} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    fontSize: '12px',
                    color: '#F8FAFC',
                  }}
                />
                <Legend
                  wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }}
                />
                <Bar dataKey="7 ngày qua" fill="#10B981" radius={[6, 6, 0, 0]} />
                <Bar dataKey="TB tuần (30 ngày)" fill="#6366F1" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="p-2.5 rounded-xl bg-[#0F172A] border border-[#334155]/60 text-xs flex items-center justify-between">
            <span className="text-[#94A3B8]">Tổng mới 30 ngày:</span>
            <span className="font-mono font-bold text-[#F8FAFC]">+{new30d} tài khoản</span>
          </div>
        </div>

        {/* LUỒNG THAO TÁC QUẢN TRỊ THỜI GIAN THỰC (ACTIVITY STREAM) */}
        <div className="bg-[#1E293B] border border-[#334155] rounded-2xl p-5 sm:p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-3 border-b border-[#334155]">
              <div className="flex items-center gap-2">
                <span className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  <Activity className="w-4 h-4" />
                </span>
                <h3 className="text-sm font-bold text-[#F8FAFC]">
                  Luồng Thao Tác Quản Trị Trực Tiếp
                </h3>
              </div>
              <button
                onClick={() => navigate('/audit-logs')}
                className="text-xs text-emerald-400 hover:text-emerald-300 font-semibold flex items-center gap-1"
              >
                Tất cả <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
            <p className="text-xs text-[#94A3B8] mt-1.5">
              Nhật ký kiểm toán thời gian thực các thao tác phê duyệt & cấp gói (Live Stream)
            </p>
          </div>

          {/* Activity Stream Timeline */}
          <div className="my-3 space-y-3 max-h-56 overflow-y-auto pr-1">
            {recentLogs.length === 0 ? (
              <p className="text-xs text-[#94A3B8] text-center py-6">Chưa có nhật ký hoạt động nào</p>
            ) : (
              recentLogs.slice(0, 5).map((log) => {
                let badgeIcon = <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />;
                let badgeBg = 'bg-emerald-500/15 border-emerald-500/30 text-emerald-400';
                let actionText = log.action;

                if (log.action === 'APPROVE_PAYMENT') {
                  badgeIcon = <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />;
                  badgeBg = 'bg-emerald-500/15 border-emerald-500/30 text-emerald-400';
                  actionText = 'Duyệt đơn VietQR';
                } else if (log.action === 'GRANT_PREMIUM') {
                  badgeIcon = <Gift className="w-3.5 h-3.5 text-purple-400" />;
                  badgeBg = 'bg-purple-500/15 border-purple-500/30 text-purple-400';
                  actionText = 'Cấp Premium';
                } else if (log.action === 'REJECT_PAYMENT' || log.action === 'REVOKE_PREMIUM') {
                  badgeIcon = <XCircle className="w-3.5 h-3.5 text-red-400" />;
                  badgeBg = 'bg-red-500/15 border-red-500/30 text-red-400';
                  actionText = log.action === 'REJECT_PAYMENT' ? 'Từ chối đơn' : 'Thu hồi gói';
                } else if (log.action === 'LOGIN') {
                  actionText = 'Đăng nhập hệ thống';
                }

                return (
                  <div key={log.id} className="flex items-start gap-2.5 text-xs">
                    <div className={`p-1.5 rounded-lg border shrink-0 mt-0.5 ${badgeBg}`}>
                      {badgeIcon}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-1">
                        <span className="font-bold text-[#F8FAFC] truncate">
                          {actionText}
                        </span>
                        <span className="text-[10px] text-[#94A3B8] font-mono shrink-0">
                          {log.createdAt ? formatDateTimeVn(log.createdAt).split(' - ')[0] : ''}
                        </span>
                      </div>
                      <p className="text-[11px] text-[#94A3B8] truncate">
                        {log.adminEmail || '—'} {log.targetId ? `• ID: ${log.targetId.slice(0, 8)}` : ''}
                      </p>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          <div className="pt-2 border-t border-[#334155] text-[11px] text-[#94A3B8] flex items-center justify-between">
            <span>Bất biến (Append-Only)</span>
            <span className="font-mono text-emerald-400 font-bold">BR-17.1 Verified</span>
          </div>
        </div>
      </div>

      {/* BẢNG DỮ LIỆU TÁC NGHIỆP: 5 ĐƠN VIETQR CHỜ DUYỆT (PENDING ORDERS) */}
      <div className="bg-[#1E293B] border border-[#334155] rounded-2xl shadow-sm overflow-hidden">
        <div className="p-5 border-b border-[#334155] flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <Clock className="w-4.5 h-4.5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-[#F8FAFC]">
                Đơn Thanh Toán VietQR Cần Xử Lý Ngay (Pending Queue)
              </h3>
              <p className="text-xs text-[#94A3B8]">
                Duyệt thủ công kích hoạt gói cước ngay cho khách hàng hoặc từ chối đơn sai sót
              </p>
            </div>
          </div>

          <button
            onClick={() => navigate('/orders')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#0F172A] hover:bg-[#334155]/60 text-emerald-400 border border-[#334155] text-xs font-semibold transition-all"
          >
            <span>Xem toàn bộ đơn hàng</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {pendingOrders.length === 0 ? (
          <div className="p-10 text-center flex flex-col items-center justify-center">
            <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-3 border border-emerald-500/20">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <h4 className="text-sm font-bold text-[#F8FAFC]">Tất cả đơn hàng đã được xử lý!</h4>
            <p className="text-xs text-[#94A3B8] mt-1 max-w-sm">
              Không có đơn nạp VietQR nào đang bị treo ở trạng thái PENDING.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-[#334155] bg-[#0F172A]/70 text-[#94A3B8] uppercase text-[10px] tracking-wider font-semibold">
                  <th className="py-3 px-4">Mã Đơn / Khách hàng</th>
                  <th className="py-3 px-4">Gói SKU</th>
                  <th className="py-3 px-4">Số Tiền (VND)</th>
                  <th className="py-3 px-4">Thời Gian Tạo</th>
                  <th className="py-3 px-4">Trạng Thái</th>
                  <th className="py-3 px-4 text-right">Thao Tác Tác Nghiệp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#334155]">
                {pendingOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-[#0F172A]/40 transition-colors">
                    {/* Mã Đơn & Khách Hàng */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-emerald-400 text-xs">
                          {order.orderCode}
                        </span>
                        <button
                          onClick={() => copyToClipboard(order.orderCode, 'mã đơn')}
                          className="p-1 rounded text-[#94A3B8] hover:text-[#F8FAFC] hover:bg-[#334155] transition-colors"
                          title="Copy mã đơn"
                        >
                          {copiedId === order.orderCode ? (
                            <Check className="w-3 h-3 text-emerald-400" />
                          ) : (
                            <Copy className="w-3 h-3" />
                          )}
                        </button>
                      </div>
                      <div className="text-[11px] text-[#94A3B8] mt-0.5">
                        {order.user?.name || order.user?.email || `User: ${order.userId.slice(0, 8)}`}
                      </div>
                    </td>

                    {/* Gói SKU */}
                    <td className="py-3.5 px-4 font-mono font-semibold text-[#F8FAFC]">
                      {order.itemSku || '—'}
                    </td>

                    {/* Số tiền */}
                    <td className="py-3.5 px-4 font-bold text-emerald-400 font-mono text-sm">
                      {formatCurrencyVnd(order.amount)}
                    </td>

                    {/* Thời gian tạo */}
                    <td className="py-3.5 px-4 text-[#94A3B8] font-mono text-[11px]">
                      {formatDateTimeVn(order.createdAt)}
                    </td>

                    {/* Trạng thái */}
                    <td className="py-3.5 px-4">
                      <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">
                        <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
                        PENDING
                      </span>
                    </td>

                    {/* Nút tác nghiệp */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => setOrderToApprove(order)}
                          className="px-3 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs shadow-glow transition-all flex items-center gap-1"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          <span>Duyệt kích hoạt</span>
                        </button>
                        <button
                          onClick={() => navigate('/orders')}
                          className="p-1.5 rounded-lg bg-[#0F172A] hover:bg-[#334155] text-[#94A3B8] hover:text-[#F8FAFC] border border-[#334155] transition-colors"
                          title="Chi tiết đơn"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
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

      {/* QUICK APPROVE CONFIRM DIALOG */}
      <ConfirmDialog
        isOpen={!!orderToApprove}
        onClose={() => setOrderToApprove(null)}
        onConfirm={handleConfirmApprove}
        title="Xác nhận Duyệt Đơn Nạp VietQR"
        message={`Bạn có chắc chắn muốn duyệt đơn ${orderToApprove?.orderCode} (${formatCurrencyVnd(orderToApprove?.amount)})? Hệ thống sẽ tự động kích hoạt gói Premium tương ứng và ghi nhận vào Audit Log.`}
        confirmLabel="Duyệt đơn ngay"
        cancelLabel="Hủy bỏ"
        isLoading={isApproving}
        isDestructive={false}
      />
    </div>
  );
};
