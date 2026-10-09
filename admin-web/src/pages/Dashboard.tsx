import React, { useEffect, useState } from 'react';
import {
  Users,
  UtensilsCrossed,
  Dumbbell,
  ClipboardCheck,
  Cpu,
  DollarSign,
  TrendingUp,
  ArrowUpRight,
  ShieldAlert,
  Sparkles,
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
} from 'recharts';
import { statsApi } from '../api/stats.api';
import { OverviewStats } from '../types';
import { StatCard } from '../components/ui/StatCard';
import { formatNumber, formatCurrencyUsd } from '../utils/formatters';
import { useNavigate } from 'react-router-dom';

const dailyGrowthData = [
  { date: '03/10', users: 1180, meals: 1250 },
  { date: '04/10', users: 1195, meals: 1310 },
  { date: '05/10', users: 1210, meals: 1290 },
  { date: '06/10', users: 1222, meals: 1360 },
  { date: '07/10', users: 1235, meals: 1395 },
  { date: '08/10', users: 1242, meals: 1410 },
  { date: '09/10', users: 1248, meals: 1420 },
];

const goalDistributionData = [
  { name: 'Giảm cân (Cut)', value: 58, color: '#10B981' },
  { name: 'Tăng cơ (Bulk)', value: 26, color: '#06B6D4' },
  { name: 'Giữ cân (Maintain)', value: 16, color: '#F59E0B' },
];

const aiFeatureUsageData = [
  { feature: 'Scan Món ăn (Vision)', tokens: 6200000, color: '#10B981' },
  { feature: 'Chat Trợ lý CalAI', tokens: 3800000, color: '#06B6D4' },
  { feature: 'Gợi ý Thực đơn', tokens: 1570520, color: '#8B5CF6' },
];

export const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<OverviewStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await statsApi.getOverview();
        setStats(data);
      } catch (err) {
        console.error('Error fetching overview stats:', err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchStats();
  }, []);

  return (
    <div className="space-y-8 animate-fade-in pb-12">
      {/* Top Welcome Banner */}
      <div className="relative overflow-hidden rounded-3xl p-6 sm:p-8 bg-gradient-to-r from-emerald-950/60 via-slate-900/80 to-slate-900 border border-emerald-500/20 backdrop-blur-xl shadow-glow">
        <div className="absolute right-0 top-0 translate-x-8 -translate-y-8 w-72 h-72 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-semibold mb-3">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Hệ thống CalAI vận hành thời gian thực</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Trung tâm Quản trị CalAI AI Coach
            </h1>
            <p className="text-sm text-slate-400 mt-1 max-w-2xl">
              Theo dõi trực tiếp người dùng, phân tích bữa ăn, tiến trình tập luyện và mức tiêu thụ token trí tuệ nhân tạo.
            </p>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <button
              onClick={() => navigate('/checkins')}
              className="px-4 py-2.5 rounded-xl bg-amber-500/15 hover:bg-amber-500/25 border border-amber-500/30 text-amber-300 text-xs font-bold transition-all flex items-center gap-2"
            >
              <ClipboardCheck className="w-4 h-4 text-amber-400" />
              <span>Duyệt {stats?.checkIns.pending || 18} Check-ins chờ</span>
            </button>
            <button
              onClick={() => navigate('/ai-logs')}
              className="px-4 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold transition-all shadow-glow flex items-center gap-2"
            >
              <Cpu className="w-4 h-4" />
              <span>Xem AI Logs</span>
            </button>
          </div>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <StatCard
          title="Người dùng & Trạng thái"
          value={formatNumber(stats?.users.total || 1248)}
          subtitle={`${formatNumber(stats?.users.active || 1180)} đang hoạt động`}
          icon={<Users className="w-5 h-5" />}
          trend={{ value: '+14% tuần này', isPositive: true }}
          accentColor="emerald"
        />

        <StatCard
          title="Bữa ăn hôm nay / Tổng"
          value={formatNumber(stats?.meals.today || 1420)}
          subtitle={`/ ${formatNumber(stats?.meals.total || 38450)} all-time`}
          icon={<UtensilsCrossed className="w-5 h-5" />}
          trend={{ value: '+8.2% so hôm qua', isPositive: true }}
          accentColor="cyan"
        />

        <StatCard
          title="Buổi tập luyện đã log"
          value={formatNumber(stats?.workouts.total || 15320)}
          subtitle="Toàn hệ thống"
          icon={<Dumbbell className="w-5 h-5" />}
          trend={{ value: '+185 buổi mới', isPositive: true }}
          accentColor="indigo"
        />

        <StatCard
          title="Check-ins Cần duyệt"
          value={formatNumber(stats?.checkIns.pending || 18)}
          subtitle={`/ ${formatNumber(stats?.checkIns.total || 940)} đã gửi`}
          icon={<ClipboardCheck className="w-5 h-5" />}
          highlight={(stats?.checkIns.pending || 18) > 0}
          trend={{ value: 'Cần duyệt ngay', isPositive: false }}
          accentColor="amber"
        />
      </div>

      {/* AI Token & Cost Metric Bar */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 p-5 rounded-2xl glass-panel">
        <div className="flex items-center gap-4">
          <div className="p-3 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
            <Cpu className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase">Tổng Token AI Đã Dùng</p>
            <p className="text-xl font-bold text-slate-900 dark:text-white">
              {formatNumber(stats?.ai.totalTokens || 11570520)} <span className="text-xs font-normal text-slate-500">tokens</span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase">Ước tính Chi phí API (USD)</p>
            <p className="text-xl font-bold text-emerald-600 dark:text-emerald-400">
              {formatCurrencyUsd(stats?.ai.estimatedCostUsd || 23.14)}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="p-3 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            <TrendingUp className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase">Tổng lượt gọi API AI</p>
            <p className="text-xl font-bold text-slate-900 dark:text-white">
              {formatNumber(stats?.ai.totalApiRequests || 48920)} <span className="text-xs font-normal text-slate-500">requests</span>
            </p>
          </div>
        </div>
      </div>

      {/* Recharts Analytics Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Growth Area Chart */}
        <div className="lg:col-span-2 p-6 rounded-3xl glass-panel flex flex-col justify-between">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">
                Tăng trưởng Người dùng & Bữa ăn (7 ngày gần nhất)
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Xu hướng người dùng tích cực ghi nhận nhật ký dinh dưỡng
              </p>
            </div>
            <div className="flex items-center gap-4 text-xs font-semibold">
              <span className="flex items-center gap-1.5 text-emerald-500">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                Bữa ăn
              </span>
              <span className="flex items-center gap-1.5 text-cyan-500">
                <span className="w-2.5 h-2.5 rounded-full bg-cyan-500" />
                Người dùng
              </span>
            </div>
          </div>

          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={dailyGrowthData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="mealGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10B981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10B981" stopOpacity={0.0} />
                  </linearGradient>
                  <linearGradient id="userGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#06B6D4" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#06B6D4" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                <XAxis dataKey="date" stroke="#64748B" fontSize={11} tickLine={false} />
                <YAxis stroke="#64748B" fontSize={11} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderRadius: '12px',
                    border: '1px solid rgba(255,255,255,0.1)',
                    fontSize: '12px',
                  }}
                />
                <Area type="monotone" dataKey="meals" stroke="#10B981" strokeWidth={2.5} fillOpacity={1} fill="url(#mealGradient)" />
                <Area type="monotone" dataKey="users" stroke="#06B6D4" strokeWidth={2.5} fillOpacity={1} fill="url(#userGradient)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* User Goal Distribution Pie Chart */}
        <div className="p-6 rounded-3xl glass-panel flex flex-col justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Phân bổ Mục tiêu Người dùng
            </h3>
            <p className="text-xs text-slate-400 mt-0.5">Tỷ lệ Cut / Bulk / Maintain</p>
          </div>

          <div className="h-52 w-full my-auto">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={goalDistributionData}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {goalDistributionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderRadius: '12px',
                    border: '1px solid rgba(255,255,255,0.1)',
                    fontSize: '12px',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="space-y-2 pt-2 border-t border-slate-100 dark:border-white/5">
            {goalDistributionData.map((item) => (
              <div key={item.name} className="flex items-center justify-between text-xs">
                <span className="flex items-center gap-2 text-slate-600 dark:text-slate-300">
                  <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: item.color }} />
                  {item.name}
                </span>
                <span className="font-bold text-slate-900 dark:text-white">{item.value}%</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* AI Token Consumption by Feature Bar Chart */}
      <div className="p-6 rounded-3xl glass-panel">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Tiêu thụ Token theo Tính năng AI
            </h3>
            <p className="text-xs text-slate-400 mt-0.5">
              Phân tách giữa Quét món ăn (Vision Scanner), Chat dinh dưỡng & Tạo thực đơn
            </p>
          </div>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={aiFeatureUsageData} layout="vertical" margin={{ top: 10, right: 30, left: 40, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis type="number" stroke="#64748B" fontSize={11} tickFormatter={(val) => `${(val / 1000000).toFixed(1)}M`} />
              <YAxis dataKey="feature" type="category" stroke="#64748B" fontSize={11} width={130} />
              <Tooltip
                formatter={(val: any) => [`${formatNumber(val)} tokens`, 'Lượng Token']}
                contentStyle={{
                  backgroundColor: '#0F172A',
                  borderRadius: '12px',
                  border: '1px solid rgba(255,255,255,0.1)',
                  fontSize: '12px',
                }}
              />
              <Bar dataKey="tokens" fill="#10B981" radius={[0, 8, 8, 0]}>
                {aiFeatureUsageData.map((entry, index) => (
                  <Cell key={`bar-${index}`} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
