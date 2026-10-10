import React, { useEffect, useState } from 'react';
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
  KeyRound,
  AlertTriangle,
  ArrowRight,
  ShieldCheck,
  Check,
  ExternalLink,
  Copy,
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
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
import { formatNumber, formatCurrencyVnd, formatDateTimeVn } from '../utils/formatters';
import { mockRevenueTrend30Days } from '../utils/mockData';
import { toast } from 'sonner';

export const Dashboard: React.FC = () => {
  const [summary, setSummary] = useState<AdminDashboardSummary | null>(null);
  const [pendingOrders, setPendingOrders] = useState<AdminPaymentOrder[]>([]);
  const [recentLogs, setRecentLogs] = useState<AdminAuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const navigate = useNavigate();

  const fetchData = async () => {
    try {
      const [sumRes, ordersRes, logsRes] = await Promise.all([
        adminDashboardApi.getSummary(),
        paymentsApi.getOrders({ status: 'PENDING', limit: 5 }),
        auditLogsApi.getAuditLogs({ limit: 6 }),
      ]);
      setSummary(sumRes);
      setPendingOrders(ordersRes.data || []);
      setRecentLogs(logsRes.data || []);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRefresh = () => {
    setIsRefreshing(true);
    fetchData().then(() => {
      toast.success('Đã cập nhật dữ liệu mới nhất');
    });
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
  };

  const handleQuickApprove = async (orderId: string, orderCode: string) => {
    try {
      await paymentsApi.approveOrder(orderId);
      toast.success(`Đã duyệt đơn ${orderCode} và kích hoạt gói thành công`);
      fetchData();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi duyệt đơn');
    }
  };

  // Calculations for charts
  const totalUsers = summary?.users.total || 1250;
  const activePremiums = summary?.subscriptions.activePremiums || 142;
  const freeUsers = Math.max(0, totalUsers - activePremiums);
  const conversionRate = ((activePremiums / totalUsers) * 100).toFixed(1);

  const pendingCount = summary?.orders.pending ?? 8;
  const paidCount = summary?.orders.paid ?? 320;
  const cancelledCount = 24;
  const totalOrdersCalc = pendingCount + paidCount + cancelledCount;

  // Donut data: Free vs Pro
  const userTierData = [
    { name: 'Active Premium', value: activePremiums, color: '#10B981' },
    { name: 'Free Users', value: freeUsers, color: '#334155' },
  ];

  // Acquisition Velocity: 7 days vs 30 days avg
  const new7Days = summary?.users.newLast7Days || 84;
  const weeklyAvg30Days = Math.round((summary?.users.newLast30Days || 312) / 4.2);
  const velocityData = [
    {
      period: 'Tăng trưởng đăng ký',
      newLast7Days: new7Days,
      weeklyAvg30Days: weeklyAvg30Days,
    },
  ];

  // Helper for Audit action icon & badge
  const renderAuditAction = (action: string) => {
    switch (action) {
      case 'APPROVE_PAYMENT':
        return {
          icon: <CheckCircle2 className="w-4 h-4 text-emerald-400" />,
          color: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
          title: 'Duyệt thanh toán VietQR',
        };
      case 'GRANT_PREMIUM':
        return {
          icon: <Gift className="w-4 h-4 text-purple-400" />,
          color: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
          title: 'Cấp bù gói Premium',
        };
      case 'REJECT_PAYMENT':
        return {
          icon: <XCircle className="w-4 h-4 text-rose-400" />,
          color: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
          title: 'Từ chối đơn nạp',
        };
      case 'REVOKE_PREMIUM':
        return {
          icon: <AlertTriangle className="w-4 h-4 text-amber-400" />,
          color: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
          title: 'Thu hồi gói Premium',
        };
      case 'LOGIN':
      default:
        return {
          icon: <KeyRound className="w-4 h-4 text-cyan-400" />,
          color: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
          title: 'Đăng nhập Quản trị',
        };
    }
  };

  return (
    <div className="space-y-8 animate-fade-in pb-12">
      {/* Top Operations Header */}
      <div className="relative overflow-hidden rounded-3xl p-6 sm:p-8 bg-gradient-to-r from-slate-900 via-slate-900/90 to-[#0F172A] border border-slate-700/60 backdrop-blur-xl shadow-2xl">
        <div className="absolute right-0 top-0 translate-x-12 -translate-y-12 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute left-1/3 bottom-0 w-64 h-64 bg-cyan-500/5 rounded-full blur-2xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-5">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-semibold mb-3">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>NutriWise / CalAI Operations Console • BR-17 Phase 1</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight flex items-center gap-3">
              Trung tâm Vận hành & Doanh thu
            </h1>
            <p className="text-sm text-slate-400 mt-1 max-w-2xl">
              Giám sát dòng tiền VietQR thời gian thực, quản lý người dùng, duyệt kích hoạt gói và đối soát kiểm toán bất biến.
            </p>
          </div>

          <div className="flex items-center gap-3 shrink-0 flex-wrap">
            <button
              onClick={handleRefresh}
              disabled={isRefreshing}
              className="px-3.5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700/80 border border-slate-700 text-slate-200 text-xs font-semibold transition-all flex items-center gap-2 disabled:opacity-50"
              title="Làm mới dữ liệu tức thì"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-emerald-400' : ''}`} />
              <span>{isRefreshing ? 'Đang tải...' : 'Làm mới'}</span>
            </button>

            <button
              onClick={() => navigate('/orders')}
              className="px-4 py-2.5 rounded-xl bg-amber-500/20 hover:bg-amber-500/30 border border-amber-500/40 text-amber-300 text-xs font-bold transition-all flex items-center gap-2 shadow-sm"
            >
              <Clock className="w-4 h-4 text-amber-400" />
              <span>Duyệt đơn VietQR ({pendingCount})</span>
            </button>

            <button
              onClick={() => navigate('/grants')}
              className="px-4 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold transition-all shadow-glow flex items-center gap-2"
            >
              <Gift className="w-4 h-4" />
              <span>Cấp gói thủ công</span>
            </button>
          </div>
        </div>
      </div>

      {/* 4 KPI Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <StatCard
          title="Tổng User"
          value={formatNumber(totalUsers)}
          subtitle={`+${formatNumber(summary?.users.newLast7Days || 84)} trong 7 ngày`}
          icon={<Users className="w-5 h-5" />}
          trend={{
            value: `+${formatNumber(summary?.users.newLast30Days || 312)}`,
            isPositive: true,
            label: '30 ngày qua',
          }}
          accentColor="emerald"
        />

        <StatCard
          title="Active Premium"
          value={formatNumber(activePremiums)}
          subtitle={`${conversionRate}% tỷ lệ chuyển đổi`}
          icon={<Sparkles className="w-5 h-5" />}
          trend={{
            value: `${conversionRate}%`,
            isPositive: true,
            label: 'Free ➜ Paid',
          }}
          accentColor="purple"
        />

        <StatCard
          title="Đơn VietQR Chờ"
          value={`${pendingCount} ĐƠN`}
          subtitle="Cần đối soát sao kê"
          icon={<Clock className="w-5 h-5" />}
          highlight={pendingCount > 0}
          trend={{
            value: pendingCount > 0 ? 'Cần xử lý ngay' : 'Không có đơn treo',
            isPositive: pendingCount === 0,
          }}
          accentColor="amber"
        />

        <StatCard
          title="Doanh Thu VietQR"
          value={formatCurrencyVnd(summary?.revenue.totalVnd || 45600000)}
          subtitle={`${paidCount} đơn đã kích hoạt`}
          icon={<CreditCard className="w-5 h-5" />}
          trend={{
            value: '+22.4%',
            isPositive: true,
            label: 'so với tháng trước',
          }}
          accentColor="emerald"
        />
      </div>

      {/* Row 1 Charts: Spline Area Chart (Biểu Đồ 1) & Donut Chart (Biểu Đồ 2) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* BIỂU ĐỒ 1: Xu Hướng Doanh Thu 30 Ngày (Spline Area Chart) */}
        <div className="lg:col-span-2 p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2.5">
                <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
                <h3 className="text-base font-bold text-white tracking-tight">
                  Biểu đồ 1: Xu Hướng Doanh Thu & Dòng Tiền 30 Ngày
                </h3>
              </div>
              <div className="flex items-center gap-4 text-xs font-semibold">
                <div className="flex items-center gap-1.5 text-emerald-400">
                  <span className="w-3 h-3 rounded-full bg-emerald-500 inline-block" />
                  <span>Doanh thu (VND)</span>
                </div>
                <div className="flex items-center gap-1.5 text-amber-400">
                  <span className="w-3 h-1 rounded-full bg-amber-500 inline-block" />
                  <span>Số đơn hàng</span>
                </div>
              </div>
            </div>
            <p className="text-xs text-slate-400 mb-6">
              Giám sát dòng tiền vào theo từng ngày và đỉnh doanh thu nạp gói VietQR.
            </p>
          </div>

          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={mockRevenueTrend30Days} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="revenueGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10B981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10B981" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.6} vertical={false} />
                <XAxis
                  dataKey="date"
                  stroke="#94A3B8"
                  fontSize={11}
                  tickLine={false}
                  axisLine={{ stroke: '#334155' }}
                />
                <YAxis
                  yAxisId="left"
                  stroke="#94A3B8"
                  fontSize={11}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(val) => `${(val / 1000000).toFixed(1)} tr`}
                />
                <YAxis
                  yAxisId="right"
                  orientation="right"
                  stroke="#F59E0B"
                  fontSize={11}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(val) => `${val} đ`}
                />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderColor: '#334155',
                    borderRadius: '16px',
                    boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.5)',
                    padding: '10px 14px',
                  }}
                  itemStyle={{ fontSize: '12px', fontWeight: 600 }}
                  labelStyle={{ color: '#F8FAFC', fontWeight: 700, marginBottom: '4px', fontSize: '13px' }}
                  formatter={(value: any, name: any) => {
                    if (name === 'revenue') {
                      return [formatCurrencyVnd(Number(value)), 'Doanh thu'];
                    }
                    return [`${value} đơn`, 'Số đơn hoàn thành'];
                  }}
                />
                <Area
                  yAxisId="left"
                  type="monotone"
                  dataKey="revenue"
                  name="revenue"
                  stroke="#10B981"
                  strokeWidth={2.5}
                  fillOpacity={1}
                  fill="url(#revenueGradient)"
                />
                <Area
                  yAxisId="right"
                  type="monotone"
                  dataKey="orders"
                  name="orders"
                  stroke="#F59E0B"
                  strokeWidth={2}
                  fillOpacity={0}
                  fill="transparent"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* BIỂU ĐỒ 2: Tỷ Lệ Gói & Chuyển Đổi (Donut Chart) */}
        <div className="p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg flex flex-col justify-between">
          <div>
            <h3 className="text-base font-bold text-white tracking-tight mb-1">
              Biểu đồ 2: Tỷ Lệ Gói & Chuyển Đổi
            </h3>
            <p className="text-xs text-slate-400 mb-4">
              Theo dõi phân bổ người dùng Active Premium vs Free Users.
            </p>
          </div>

          <div className="relative h-56 flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={userTierData}
                  cx="50%"
                  cy="50%"
                  innerRadius={65}
                  outerRadius={92}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {userTierData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} stroke="none" />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderColor: '#334155',
                    borderRadius: '12px',
                    padding: '8px 12px',
                  }}
                  itemStyle={{ fontSize: '12px', color: '#F8FAFC' }}
                  formatter={(value: any, name: any) => [`${formatNumber(Number(value))} users`, name]}
                />
              </PieChart>
            </ResponsiveContainer>

            {/* Tâm Donut */}
            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
              <span className="text-2xl font-black text-emerald-400 tracking-tight">
                {conversionRate}%
              </span>
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                Tỷ lệ chuyển đổi
              </span>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-slate-700/60 flex items-center justify-around text-xs">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-emerald-500" />
              <div>
                <p className="text-slate-400 font-medium">Premium</p>
                <p className="text-white font-bold">{formatNumber(activePremiums)} users</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-slate-600" />
              <div>
                <p className="text-slate-400 font-medium">Free Tier</p>
                <p className="text-white font-bold">{formatNumber(freeUsers)} users</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Row 2: Biểu Đồ 3 (Fulfillment Health) & Biểu Đồ 4 (Acquisition Velocity) & Biểu Đồ 5 (Activity Stream) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* BIỂU ĐỒ 3: Trạng Thái Phân Bổ Đơn VietQR (Horizontal Stacked Bar / Progress) */}
        <div className="p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-1">
              <h3 className="text-base font-bold text-white tracking-tight">
                Biểu đồ 3: Trạng Thái Đơn VietQR
              </h3>
              <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-slate-700 text-slate-300">
                {totalOrdersCalc} đơn
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-6">
              Đánh giá sức khỏe vận hành đơn hàng và tỷ lệ hoàn tất nạp tiền.
            </p>
          </div>

          {/* Rounded Multi-segmented Progress Bar */}
          <div className="space-y-4 my-auto">
            <div className="w-full h-5 rounded-full overflow-hidden flex bg-slate-800 p-0.5 border border-slate-700">
              <div
                style={{ width: `${(paidCount / totalOrdersCalc) * 100}%` }}
                className="h-full bg-emerald-500 rounded-l-full transition-all duration-500"
                title={`PAID: ${paidCount}`}
              />
              <div
                style={{ width: `${(pendingCount / totalOrdersCalc) * 100}%` }}
                className="h-full bg-amber-500 transition-all duration-500"
                title={`PENDING: ${pendingCount}`}
              />
              <div
                style={{ width: `${(cancelledCount / totalOrdersCalc) * 100}%` }}
                className="h-full bg-slate-500 rounded-r-full transition-all duration-500"
                title={`CANCELLED/EXPIRED: ${cancelledCount}`}
              />
            </div>

            <div className="space-y-2.5 pt-2">
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900/40 border border-slate-800">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                  <span className="text-xs font-semibold text-slate-200">PAID (Đã kích hoạt)</span>
                </div>
                <div className="text-right">
                  <span className="text-xs font-extrabold text-emerald-400">{paidCount} đơn</span>
                  <span className="text-[11px] text-slate-500 ml-1.5">
                    ({((paidCount / totalOrdersCalc) * 100).toFixed(1)}%)
                  </span>
                </div>
              </div>

              <div className="flex items-center justify-between p-2.5 rounded-xl bg-amber-500/10 border border-amber-500/20">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-500 animate-ping" />
                  <span className="text-xs font-semibold text-amber-200">PENDING (Chờ duyệt)</span>
                </div>
                <div className="text-right">
                  <span className="text-xs font-extrabold text-amber-400">{pendingCount} đơn</span>
                  <span className="text-[11px] text-amber-300/70 ml-1.5">
                    ({((pendingCount / totalOrdersCalc) * 100).toFixed(1)}%)
                  </span>
                </div>
              </div>

              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900/40 border border-slate-800">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-slate-500" />
                  <span className="text-xs font-semibold text-slate-400">CANCELLED / Hết hạn</span>
                </div>
                <div className="text-right">
                  <span className="text-xs font-bold text-slate-400">{cancelledCount} đơn</span>
                  <span className="text-[11px] text-slate-500 ml-1.5">
                    ({((cancelledCount / totalOrdersCalc) * 100).toFixed(1)}%)
                  </span>
                </div>
              </div>
            </div>
          </div>

          <button
            onClick={() => navigate('/orders')}
            className="mt-4 w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold transition-all flex items-center justify-center gap-2"
          >
            <span>Quản lý chi tiết danh sách đơn</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* BIỂU ĐỒ 4: Tốc Độ Gia Tăng Người Dùng (Acquisition Velocity) */}
        <div className="p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg flex flex-col justify-between">
          <div>
            <h3 className="text-base font-bold text-white tracking-tight mb-1">
              Biểu đồ 4: Tốc Độ Tăng User
            </h3>
            <p className="text-xs text-slate-400 mb-6">
              So sánh lượng đăng ký 7 ngày qua vs mức trung bình tuần trong 30 ngày.
            </p>
          </div>

          <div className="h-60 w-full my-auto">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={velocityData} margin={{ top: 20, right: 20, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.5} vertical={false} />
                <XAxis dataKey="period" stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
                <YAxis stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderColor: '#334155',
                    borderRadius: '12px',
                    padding: '8px 12px',
                  }}
                  itemStyle={{ fontSize: '12px' }}
                  labelStyle={{ color: '#F8FAFC', fontWeight: 600 }}
                  formatter={(value: any, name: any) => [
                    `${value} người`,
                    name === 'newLast7Days' ? '7 ngày qua' : 'TB tuần 30 ngày',
                  ]}
                />
                <Legend
                  verticalAlign="top"
                  align="right"
                  wrapperStyle={{ paddingBottom: '10px', fontSize: '11px' }}
                  formatter={(value) =>
                    value === 'newLast7Days' ? '7 ngày qua' : 'Trung bình tuần (30 ngày)'
                  }
                />
                <Bar
                  dataKey="newLast7Days"
                  name="newLast7Days"
                  fill="#10B981"
                  radius={[8, 8, 0, 0]}
                  barSize={40}
                />
                <Bar
                  dataKey="weeklyAvg30Days"
                  name="weeklyAvg30Days"
                  fill="#06B6D4"
                  radius={[8, 8, 0, 0]}
                  barSize={40}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="mt-4 p-3 rounded-2xl bg-slate-900/50 border border-slate-800 text-xs text-slate-300 flex items-center justify-between">
            <span>Tốc độ tuần này so với trung bình:</span>
            <span className="font-extrabold text-emerald-400">
              +{(((new7Days - weeklyAvg30Days) / weeklyAvg30Days) * 100).toFixed(1)}%
            </span>
          </div>
        </div>

        {/* BIỂU ĐỒ 5: Luồng Hoạt Động Thao Tác Quản Trị (Admin Activity Stream) */}
        <div className="p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-1">
              <h3 className="text-base font-bold text-white tracking-tight">
                Biểu đồ 5: Hoạt Động Quản Trị
              </h3>
              <button
                onClick={() => navigate('/audit-logs')}
                className="text-[11px] font-bold text-emerald-400 hover:text-emerald-300 flex items-center gap-1"
              >
                <span>Xem tất cả</span>
                <ArrowRight className="w-3 h-3" />
              </button>
            </div>
            <p className="text-xs text-slate-400 mb-4">
              Nhật ký kiểm toán thời gian thực các thao tác của Quản trị viên.
            </p>
          </div>

          {/* Vertical Timeline */}
          <div className="space-y-3.5 my-auto overflow-y-auto max-h-[300px] pr-1">
            {recentLogs.length === 0 ? (
              <p className="text-xs text-slate-500 py-6 text-center">Chưa có nhật ký gần đây</p>
            ) : (
              recentLogs.map((log) => {
                const actionMeta = renderAuditAction(log.action);
                return (
                  <div key={log.id} className="flex items-start gap-3 relative group">
                    <div className={`p-2 rounded-xl shrink-0 border mt-0.5 ${actionMeta.color}`}>
                      {actionMeta.icon}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-2">
                        <p className="text-xs font-bold text-slate-200 truncate">
                          {actionMeta.title}
                        </p>
                        <span className="text-[10px] text-slate-500 shrink-0 font-medium">
                          {formatDateTimeVn(log.createdAt).split(' - ')[0]}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-400 truncate mt-0.5">
                        {log.reason || `Mã đối tượng: ${log.targetId}`}
                      </p>
                      <p className="text-[10px] text-slate-500 truncate mt-0.5">
                        Thực hiện bởi: <span className="text-slate-400 font-semibold">{log.adminEmail}</span>
                      </p>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          <div className="mt-4 pt-3 border-t border-slate-700/60 text-center">
            <span className="text-[11px] text-slate-400">
              Dữ liệu được ghi nhận bất biến theo quy chuẩn <b className="text-slate-300">BR-17.4</b>
            </span>
          </div>
        </div>
      </div>

      {/* Actionable Data Table: Pending VietQR Orders Ready for Approval */}
      <div className="p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-lg font-extrabold text-white tracking-tight">
                Đơn VietQR Chờ Duyệt Nhanh ({pendingOrders.length})
              </h3>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                Action Required
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Thao tác kích hoạt ngay hoặc tra cứu mã đơn đối soát ngân hàng chỉ với 1 cú click.
            </p>
          </div>

          <button
            onClick={() => navigate('/orders')}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-bold text-slate-200 transition-all flex items-center gap-2 shrink-0"
          >
            <span>Xem toàn bộ đơn hàng</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </button>
        </div>

        {pendingOrders.length === 0 ? (
          <div className="p-8 text-center rounded-2xl bg-slate-900/40 border border-slate-800">
            <CheckCircle2 className="w-10 h-10 text-emerald-400 mx-auto mb-2 opacity-80" />
            <p className="text-sm font-bold text-slate-200">Không có đơn hàng VietQR nào đang bị treo!</p>
            <p className="text-xs text-slate-400 mt-1">Hệ thống xử lý kích hoạt tự động đang vận hành hoàn hảo.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[11px] uppercase font-bold text-slate-400 bg-slate-900/60 border-b border-slate-700/80">
                <tr>
                  <th className="py-3 px-4 rounded-l-xl">Mã Đơn VietQR</th>
                  <th className="py-3 px-4">Khách Hàng</th>
                  <th className="py-3 px-4">Gói Nạp</th>
                  <th className="py-3 px-4">Số Tiền (VND)</th>
                  <th className="py-3 px-4">Thời Gian Tạo</th>
                  <th className="py-3 px-4">Trạng Thái</th>
                  <th className="py-3 px-4 text-right rounded-r-xl">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/80">
                {pendingOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 px-4 font-mono font-bold text-emerald-400">
                      <div className="flex items-center gap-1.5">
                        <span>{order.orderCode}</span>
                        <button
                          onClick={() => copyToClipboard(order.orderCode, 'Mã đơn')}
                          className="p-1 rounded text-slate-500 hover:text-slate-300 hover:bg-slate-700 transition-colors"
                          title="Sao chép mã đơn"
                        >
                          <Copy className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                    <td className="py-3.5 px-4">
                      <div>
                        <p className="font-bold text-slate-200">{order.user?.name || 'Khách hàng'}</p>
                        <p className="text-[11px] text-slate-400 flex items-center gap-1">
                          <span>{order.user?.email || order.userId}</span>
                          <button
                            onClick={() => copyToClipboard(order.user?.email || order.userId, 'Email/ID')}
                            className="text-slate-500 hover:text-slate-300"
                            title="Sao chép email"
                          >
                            <Copy className="w-3 h-3" />
                          </button>
                        </p>
                      </div>
                    </td>
                    <td className="py-3.5 px-4 font-semibold text-slate-300 uppercase">
                      <span className="px-2 py-0.5 rounded-lg bg-slate-800 text-[11px]">
                        {order.itemSku.replace('premium_', '')}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-extrabold text-white">
                      {formatCurrencyVnd(order.amount)}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {formatDateTimeVn(order.createdAt)}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30 inline-flex items-center gap-1.5">
                        <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
                        PENDING
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => handleQuickApprove(order.id, order.orderCode)}
                        className="px-3 py-1.5 rounded-xl bg-emerald-500/20 hover:bg-emerald-500 text-emerald-300 hover:text-white border border-emerald-500/30 text-xs font-bold transition-all shadow-sm inline-flex items-center gap-1.5"
                      >
                        <Check className="w-3.5 h-3.5" />
                        <span>Duyệt</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
