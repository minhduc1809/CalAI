import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ChartContainer, SimpleBarChart, TrendLineChart } from '../../../components/charts/Charts';
import { DataTable, Column } from '../../../components/data-display/DataTable';
import { StatCard } from '../../../components/data-display/parts';
import { Alert, EmptyState, ErrorState, Skeleton } from '../../../components/feedback/States';
import { Select } from '../../../components/forms/controls';
import { AppIcon, IconName } from '../../../components/ui/icons';
import { deltaPct, fmtDate, fmtDateTime, fmtNumber, fmtPct, fmtUsd, fmtVnd, todayKey } from '../../../lib/format';
import { authStore } from '../../../services/auth';
import { aiApi } from '../../ai-monitoring/api/ai.api';
import { healthApi } from '../../system-health/api/health.api';
import { orderApi } from '../../qr-orders/api/order.api';
import { OrderStatusBadge } from '../../qr-orders/components/OrderStatusBadge';
import type { QrOrder } from '../../qr-orders/types/order.types';
import { submissionApi, Submission } from '../../food-submissions/api/submission.api';
import { dashboardApi } from '../api/dashboard.api';

const RANGES = [
  { value: '7', label: '7 ngày gần nhất' },
  { value: '30', label: '30 ngày gần nhất' },
  { value: '90', label: '90 ngày gần nhất' },
];

const errText = (e: unknown) => (e instanceof Error ? e.message : 'Lỗi không xác định');

function greeting(): string {
  const h = Number(new Intl.DateTimeFormat('en-GB', { hour: '2-digit', hour12: false, timeZone: 'Asia/Ho_Chi_Minh' }).format(new Date()));
  return h < 11 ? 'Chào buổi sáng' : h < 14 ? 'Chào buổi trưa' : h < 18 ? 'Chào buổi chiều' : 'Chào buổi tối';
}

export default function DashboardPage() {
  const admin = authStore.getAdmin();
  const [days, setDays] = useState('30');
  const range = useMemo(() => ({ from: todayKey(-(Number(days) - 1)), to: todayKey() }), [days]);

  const pending = useQuery({ queryKey: ['pending-work'], queryFn: ({ signal }) => dashboardApi.pendingWork(signal), refetchInterval: 60_000 });
  const overview = useQuery({ queryKey: ['dash-overview', range], queryFn: ({ signal }) => dashboardApi.overview(range, signal) });
  const series = useQuery({ queryKey: ['dash-series', range], queryFn: ({ signal }) => dashboardApi.timeseries(range, signal) });
  const funnel = useQuery({ queryKey: ['dash-funnel', range], queryFn: ({ signal }) => dashboardApi.funnel(range, signal) });
  const retention = useQuery({ queryKey: ['dash-retention', range], queryFn: ({ signal }) => dashboardApi.retention(range, signal) });
  const aiDaily = useQuery({ queryKey: ['dash-ai', range], queryFn: ({ signal }) => aiApi.daily(range.from, range.to, signal) });
  const health = useQuery({ queryKey: ['dash-health'], queryFn: ({ signal }) => healthApi.get(signal), refetchInterval: 120_000 });
  const orders = useQuery({ queryKey: ['dash-orders'], queryFn: ({ signal }) => orderApi.list({ page: 1, limit: 6 }, signal) });
  const subs = useQuery({ queryKey: ['dash-subs'], queryFn: ({ signal }) => submissionApi.list({ status: 'SUBMITTED', page: 1, limit: 6 }, signal) });

  const o = overview.data;
  const aiTotals = useMemo(() => {
    const d = aiDaily.data?.days ?? [];
    const calls = d.reduce((a, x) => a + x.calls, 0);
    const errors = d.reduce((a, x) => a + x.errors, 0);
    return { calls, errors, cost: d.reduce((a, x) => a + x.costUsd, 0), tokens: d.reduce((a, x) => a + x.tokens, 0), rate: calls ? (errors / calls) * 100 : null };
  }, [aiDaily.data]);

  const chartDays = (series.data?.days ?? []).map((d) => ({ ...d, label: d.date.slice(5) }));
  const hasUsers = chartDays.some((d) => d.newUsers > 0 || d.activeUsers > 0);
  const hasRevenue = chartDays.some((d) => d.revenueVnd > 0 || d.paidOrders > 0);

  const todos: { icon: IconName; label: string; count: number; to: string; tone: 'warning' | 'danger' }[] = [];
  const p = pending.data;
  if (p) {
    if (p.pendingOrders) todos.push({ icon: 'Payments', label: 'Đơn QR chờ duyệt', count: p.pendingOrders, to: '/payments?status=PENDING', tone: 'warning' });
    if (p.expiringOrders) todos.push({ icon: 'Clock', label: 'Đơn QR sắp hết hạn', count: p.expiringOrders, to: '/payments?status=PENDING&sort=expiresAt&order=asc', tone: 'danger' });
    if (p.pendingSubmissions) todos.push({ icon: 'FoodReview', label: 'Món chờ duyệt', count: p.pendingSubmissions, to: '/submissions', tone: 'warning' });
    if (p.billingIssues) todos.push({ icon: 'Warning', label: 'Gói gia hạn gặp lỗi (7 ngày)', count: p.billingIssues, to: '/users?plan=GRACE', tone: 'danger' });
    if (p.dataRequests) todos.push({ icon: 'DataRequests', label: p.dataRequestsOverdue ? `Yêu cầu dữ liệu (${p.dataRequestsOverdue} quá hạn)` : 'Yêu cầu dữ liệu đang mở', count: p.dataRequests, to: '/data-requests', tone: p.dataRequestsOverdue ? 'danger' : 'warning' });
  }

  const orderCols: Column<QrOrder>[] = [
    { key: 'code', header: 'Mã đơn', cell: (r) => <b>{r.code}</b> },
    { key: 'user', header: 'Người dùng', cell: (r) => r.user.email ?? '—' },
    { key: 'amount', header: 'Số tiền', cell: (r) => fmtVnd(r.amount) },
    { key: 'status', header: 'Trạng thái', cell: (r) => <OrderStatusBadge status={r.status} /> },
    { key: 'created', header: 'Tạo lúc', cell: (r) => <span className="nowrap">{fmtDateTime(r.createdAt)}</span> },
    { key: 'expires', header: 'Hết hạn', cell: (r) => <span className="nowrap">{fmtDateTime(r.expiresAt)}</span> },
  ];
  const subCols: Column<Submission>[] = [
    { key: 'name', header: 'Tên món', cell: (r) => <b>{r.name}</b> },
    { key: 'by', header: 'Người gửi', cell: (r) => r.user.email ?? r.user.username },
    { key: 'at', header: 'Gửi lúc', cell: (r) => <span className="nowrap">{fmtDateTime(r.createdAt)}</span> },
    { key: 'dup', header: 'Nghi trùng', cell: (r) => (r.possibleDuplicate ? <span className="badge warning">Nghi trùng</span> : '—') },
  ];

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">{greeting()}, {admin?.name || 'quản trị viên'}</h1>
          <p className="page-sub">Hôm nay {fmtDate(new Date())} · Tổng quan vận hành NutriWise{o ? ` · cập nhật ${fmtDateTime(o.updatedAt)}` : ''}</p>
        </div>
        <div className="page-actions">
          <div style={{ minWidth: 190 }}><Select aria-label="Khoảng thời gian" options={RANGES} value={days} onChange={(e) => setDays(e.target.value)} /></div>
        </div>
      </div>

      {/* Việc đang chờ bạn */}
      <div className="card" style={{ marginBottom: 16 }}>
        <div className="card-title"><AppIcon name="Bell" size={18} /> Việc đang chờ bạn</div>
        {pending.isLoading ? <Skeleton height={44} /> : pending.isError ? <ErrorState message={errText(pending.error)} onRetry={() => pending.refetch()} /> : todos.length === 0 ? (
          <Alert kind="success">Không có việc nào đang chờ. Mọi thứ đã được xử lý.</Alert>
        ) : (
          <div className="row" style={{ gap: 10 }}>
            {todos.map((t) => (
              <Link key={t.label} to={t.to} className="card hoverable" style={{ padding: '10px 14px', textDecoration: 'none', color: 'inherit', display: 'flex', alignItems: 'center', gap: 10 }}>
                <span className={`stat-icon ${t.tone}`} style={{ width: 34, height: 34 }}><AppIcon name={t.icon} size={18} /></span>
                <span><b style={{ fontSize: 18 }}>{fmtNumber(t.count)}</b> <span className="muted">{t.label}</span></span>
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* 6 thẻ chỉ số */}
      <div className="grid cols-6" style={{ marginBottom: 16 }}>
        <StatCard title="Tổng người dùng" icon="Users" loading={overview.isLoading} error={overview.isError} value={o ? fmtNumber(o.users.total) : ''}
          delta={o ? deltaPct(o.users.total, o.users.totalPrevious) : null} deltaLabel="so với đầu kỳ" tooltip="Số tài khoản người dùng (không gồm admin) tại thời điểm hiện tại" to="/users" />
        <StatCard title="Người dùng mới" icon="Plus" tone="info" loading={overview.isLoading} error={overview.isError} value={o ? fmtNumber(o.users.newInRange.current) : ''}
          delta={o ? deltaPct(o.users.newInRange.current, o.users.newInRange.previous) : null} tooltip="Số tài khoản đăng ký trong khoảng đã chọn" />
        <StatCard title="Người dùng hoạt động" icon="Activity" tone="success" loading={overview.isLoading} error={overview.isError} value={o?.users.active ? fmtNumber(o.users.active.current) : '—'}
          delta={o?.users.active ? deltaPct(o.users.active.current, o.users.active.previous) : null} tooltip="Số người có ghi ít nhất một bữa ăn trong khoảng đã chọn (số tổng hợp ẩn danh)" />
        <StatCard title="Premium đang hoạt động" icon="Grants" tone="warning" loading={overview.isLoading} error={overview.isError} value={o ? fmtNumber(o.premium.active) : ''}
          hint={o ? `QR ${o.premium.byQr} · Cấp tay ${o.premium.byManual} · Google ${o.premium.byGooglePlay}` : undefined} tooltip="Số người có Premium còn hiệu lực, mỗi người chỉ đếm một lần" to="/users?plan=PREMIUM" />
        <StatCard title="Doanh thu thực nhận" icon="Revenue" tone="success" loading={overview.isLoading} error={overview.isError} value={o ? fmtVnd(o.revenue.received.current) : ''}
          delta={o ? deltaPct(o.revenue.received.current, o.revenue.received.previous) : null} hint={o ? `${o.revenue.ordersPaid.current} đơn đã thanh toán` : undefined}
          tooltip="Chỉ tính đơn QR đã thanh toán; đơn chờ, hết hạn hoặc huỷ không được cộng" to="/revenue" />
        <StatCard title="Đơn QR chờ duyệt" icon="Payments" tone="danger" loading={overview.isLoading} error={overview.isError} value={o ? fmtNumber(o.orders.pending) : ''}
          hint={o && o.orders.expiringSoon ? `${o.orders.expiringSoon} đơn sắp hết hạn` : undefined} to="/payments?status=PENDING" />
      </div>

      {/* Biểu đồ */}
      <div className="grid charts" style={{ marginBottom: 16 }}>
        <ChartContainer title="Xu hướng người dùng" loading={series.isLoading} error={series.isError ? errText(series.error) : null} onRetry={() => series.refetch()} empty={!hasUsers}>
          <TrendLineChart data={chartDays} xKey="label" series={[{ key: 'newUsers', label: 'Người dùng mới' }, { key: 'activeUsers', label: 'Người dùng hoạt động' }]} />
        </ChartContainer>
        <ChartContainer title="Doanh thu thực nhận" loading={series.isLoading} error={series.isError ? errText(series.error) : null} onRetry={() => series.refetch()} empty={!hasRevenue} emptyText="Chưa có đơn thanh toán trong khoảng này">
          <SimpleBarChart data={chartDays} xKey="label" series={[{ key: 'revenueVnd', label: 'Doanh thu (VND)' }]} format={(v) => fmtNumber(v)} />
        </ChartContainer>
      </div>

      <div className="grid charts" style={{ marginBottom: 16 }}>
        <div className="card">
          <div className="card-title">Phễu chuyển đổi</div>
          {funnel.isLoading ? <Skeleton height={160} /> : funnel.isError ? <ErrorState message={errText(funnel.error)} onRetry={() => funnel.refetch()} /> : !funnel.data || funnel.data.steps[0].count === 0 ? (
            <EmptyState icon="Chart" title="Chưa có nhóm người dùng đủ điều kiện" description="Phễu chỉ tính người đăng ký trong khoảng đã chọn và đã đủ 7 ngày." />
          ) : (
            <>
              {funnel.data.steps.map((s) => (
                <div className="funnel-row" key={s.key}>
                  <span>{s.label}</span>
                  <div className="funnel-bar"><div style={{ width: `${funnel.data!.steps[0].count ? (s.count / funnel.data!.steps[0].count) * 100 : 0}%` }} /></div>
                  <span style={{ textAlign: 'right' }}><b>{fmtNumber(s.count)}</b>{s.fromPrevPct !== null && <span className="muted"> · {fmtPct(s.fromPrevPct)}</span>}</span>
                </div>
              ))}
            </>
          )}
        </div>

        <div className="card">
          <div className="card-title">Giữ chân người dùng</div>
          {retention.isLoading ? <Skeleton height={120} /> : retention.isError ? <ErrorState message={errText(retention.error)} onRetry={() => retention.refetch()} /> : (
            <>
              <table className="table">
                <thead><tr><th>Chỉ số</th><th>Đủ điều kiện</th><th>Còn quay lại</th><th>Tỷ lệ</th></tr></thead>
                <tbody>
                  {retention.data!.rows.map((r) => (
                    <tr key={r.key}><td>{r.label}</td><td>{fmtNumber(r.eligible)}</td><td>{fmtNumber(r.retained)}</td><td><b>{r.ratePct === null ? '—' : fmtPct(r.ratePct)}</b></td></tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </div>
      </div>

      {/* Bảng nhanh */}
      <div className="grid cols-2" style={{ marginBottom: 16 }}>
        <div>
          <div className="row between" style={{ marginBottom: 8 }}><b>Đơn QR gần đây</b><Link to="/payments">Xem tất cả</Link></div>
          <DataTable columns={orderCols} rows={orders.data?.items} rowKey={(r) => r.id} loading={orders.isLoading} error={orders.isError ? errText(orders.error) : null} onRetry={() => orders.refetch()} emptyTitle="Chưa có đơn nào" />
        </div>
        <div>
          <div className="row between" style={{ marginBottom: 8 }}><b>Món đang chờ duyệt</b><Link to="/submissions">Mở hàng đợi</Link></div>
          <DataTable columns={subCols} rows={subs.data?.items} rowKey={(r) => r.id} loading={subs.isLoading} error={subs.isError ? errText(subs.error) : null} onRetry={() => subs.refetch()} emptyTitle="Không có món nào chờ duyệt" />
        </div>
      </div>

      {/* AI & vận hành */}
      <div className="card">
        <div className="row between" style={{ marginBottom: 8 }}>
          <div className="card-title" style={{ marginBottom: 0 }}><AppIcon name="AIUsage" size={18} /> Chi phí AI và vận hành</div>
          <div className="row"><Link to="/ai">Giám sát AI</Link><Link to="/system-health">Tình trạng hệ thống</Link></div>
        </div>
        <div className="grid cols-3">
          <div>
            <div className="muted">Chi phí AI ước tính ({days} ngày)</div>
            <div className="stat-value">{aiDaily.isLoading ? <Skeleton height={28} width="50%" /> : aiDaily.isError ? '—' : fmtUsd(aiTotals.cost)}</div>
            <div className="muted" style={{ fontSize: 12.5 }}>{aiDaily.data ? `Hôm nay ${fmtUsd(aiDaily.data.todayCostUsd)} · ${fmtNumber(aiTotals.calls)} lời gọi · ${fmtNumber(aiTotals.tokens)} token` : ''}</div>
            {aiDaily.data && !aiDaily.data.pricing.configured && <div className="muted" style={{ fontSize: 12.5 }}>Chưa cấu hình đơn giá, chi phí có thể là 0.</div>}
          </div>
          <div>
            <div className="muted">Tỉ lệ lỗi AI</div>
            <div className="stat-value">{aiDaily.isLoading ? <Skeleton height={28} width="40%" /> : aiTotals.rate === null ? '—' : fmtPct(aiTotals.rate)}</div>
            <div className="muted" style={{ fontSize: 12.5 }}>{fmtNumber(aiTotals.errors)} lỗi trên {fmtNumber(aiTotals.calls)} lời gọi</div>
          </div>
          <div>
            <div className="muted">Trạng thái hệ thống</div>
            {health.isLoading ? <Skeleton height={28} width="60%" /> : health.isError ? <span className="muted">Không tải được</span> : (
              <div className="stack" style={{ gap: 6, marginTop: 4 }}>
                {([['Cơ sở dữ liệu', health.data!.database.level], ['Dịch vụ AI', health.data!.ai.level], ['Email', health.data!.email.level]] as const).map(([name, level]) => (
                  <div className="row between" key={name}><span>{name}</span><span className={`badge ${level === 'OK' ? 'success' : level === 'DOWN' ? 'danger' : level === 'WARN' ? 'warning' : ''}`}>{level === 'OK' ? 'Tốt' : level === 'DOWN' ? 'Lỗi' : level === 'WARN' ? 'Cần chú ý' : 'Chưa rõ'}</span></div>
                ))}
                <div className="row between"><span>Lần sao lưu cuối</span><span>{health.data!.backup.lastBackupAt ? fmtDateTime(health.data!.backup.lastBackupAt) : <span className="muted">Chưa có dữ liệu</span>}</span></div>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}

