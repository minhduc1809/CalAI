import { useQuery } from '@tanstack/react-query';
import { ChartContainer, SimpleBarChart } from '../../../components/charts/Charts';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { DateRangeFilter } from '../../../components/data-display/DateRangeFilter';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatCard } from '../../../components/data-display/parts';
import { Alert } from '../../../components/feedback/States';
import { Select } from '../../../components/forms/controls';
import { ExportButton } from '../../../components/import-export/ExportButton';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDateTime, fmtNumber, fmtPct, fmtVnd, todayKey } from '../../../lib/format';
import { PaidOrder, revenueApi } from '../api/revenue.api';

const GROUPS = [{ value: 'day', label: 'Theo ngày' }, { value: 'month', label: 'Theo tháng' }];

export default function RevenuePage() {
  const defaults = { from: todayKey(-29), to: todayKey(), group: 'day' };
  const list = useListParams(['from', 'to', 'group'], defaults);
  const { params } = list;
  const { from, to, group } = params.filters;

  const summary = useQuery({ queryKey: ['revenue', from, to, group], queryFn: ({ signal }) => revenueApi.summary(from, to, group, signal) });
  const orders = useQuery({
    queryKey: ['revenue-orders', from, to, params.page, params.limit],
    queryFn: ({ signal }) => revenueApi.orders(from, to, params.page, params.limit, signal),
    placeholderData: (p) => p,
  });
  const s = summary.data;

  const columns: Column<PaidOrder>[] = [
    { key: 'code', header: 'Mã nội dung CK', cell: (o) => <b>{o.code}</b> },
    { key: 'user', header: 'Email', cell: (o) => o.user.email ?? '—' },
    { key: 'product', header: 'Gói', cell: (o) => o.productId },
    { key: 'amount', header: 'Số tiền', align: 'right', cell: (o) => <b>{fmtVnd(o.amount)}</b> },
    { key: 'paid', header: 'Thanh toán lúc', cell: (o) => <span className="nowrap">{fmtDateTime(o.paidAt)}</span> },
    { key: 'by', header: 'Duyệt bởi', cell: (o) => (o.approvedBy === 'AUTO:BANK' ? 'Tự động (ngân hàng)' : 'Admin') },
  ];

  return (
    <>
      <PageHeader
        title="Đối soát và doanh thu"
        breadcrumbs={[{ label: 'Thanh toán' }, { label: 'Báo cáo doanh thu' }]}
        actions={<ExportButton kind="revenue" query={{ from, to }} title="Xuất báo cáo đối soát" />}
      />
      <FilterBar activeCount={0} onClear={list.clear}>
        <DateRangeFilter label="Kỳ báo cáo" from={from} to={to} onChange={(f, t) => { list.setFilter('from', f); list.setFilter('to', t); }} />
        <FilterField label="Nhóm theo"><Select options={GROUPS} value={group} onChange={(e) => list.setFilter('group', e.target.value)} /></FilterField>
      </FilterBar>

      <div className="grid cols-6" style={{ marginBottom: 16 }}>
        <StatCard title="Doanh thu thực nhận" icon="Revenue" tone="success" loading={summary.isLoading} error={summary.isError} value={s ? fmtVnd(s.totals.revenueVnd) : ''} />
        <StatCard title="Đơn đã thanh toán" icon="CheckCircle" loading={summary.isLoading} error={summary.isError} value={s ? fmtNumber(s.totals.paidOrders) : ''} hint={s?.totals.averageOrderVnd ? `Trung bình ${fmtVnd(s.totals.averageOrderVnd)}` : undefined} />
        <StatCard title="Hết hạn không thanh toán" icon="Clock" tone="warning" loading={summary.isLoading} error={summary.isError} value={s ? fmtNumber(s.totals.expiredUnpaid) : ''} hint={s?.totals.unpaidRatePct !== null && s ? `${fmtPct(s.totals.unpaidRatePct)} trên đơn kết thúc` : undefined} />
        <StatCard title="Premium mới" icon="Plus" tone="info" loading={summary.isLoading} error={summary.isError} value={s ? fmtNumber(s.premium.newPayers) : ''} tooltip="Người thanh toán lần đầu trong kỳ" />
        <StatCard title="Gia hạn" icon="Reset" loading={summary.isLoading} error={summary.isError} value={s ? fmtNumber(s.premium.renewals) : ''} tooltip="Người đã từng thanh toán và thanh toán lại trong kỳ" />
        <StatCard title="Rời bỏ" icon="TrendDown" tone="danger" loading={summary.isLoading} error={summary.isError} value={s ? fmtNumber(s.premium.churned) : ''} tooltip="Quyền Premium hết hạn trong kỳ và không còn Premium nào hiệu lực vào cuối kỳ" />
      </div>

      <div className="grid charts" style={{ marginBottom: 16 }}>
        <ChartContainer title="Doanh thu theo kỳ" loading={summary.isLoading} error={summary.isError ? (summary.error as Error).message : null} onRetry={() => summary.refetch()} empty={!s || s.series.length === 0} emptyText="Chưa có đơn thanh toán trong kỳ này">
          <SimpleBarChart data={s?.series ?? []} xKey="period" series={[{ key: 'revenueVnd', label: 'Doanh thu (VND)' }]} format={(v) => fmtNumber(v)} />
        </ChartContainer>
        <div className="card">
          <div className="card-title">Theo gói</div>
          {!s || s.byProduct.length === 0 ? <span className="muted">Chưa có đơn.</span> : (
            <table className="table">
              <thead><tr><th>Gói</th><th>Đơn</th><th style={{ textAlign: 'right' }}>Doanh thu</th></tr></thead>
              <tbody>{s.byProduct.map((p) => (<tr key={p.productId}><td>{p.productId}</td><td>{fmtNumber(p.orders)}</td><td style={{ textAlign: 'right' }}>{fmtVnd(p.revenueVnd)}</td></tr>))}</tbody>
            </table>
          )}
          {s && <Alert kind="info">Google Play: {fmtNumber(s.googlePlay.purchases)} giao dịch trong kỳ. {s.googlePlay.note}</Alert>}
        </div>
      </div>

      <div style={{ marginBottom: 8 }}><b>Chi tiết đơn đã thanh toán</b></div>
      <DataTable
        columns={columns}
        rows={orders.data?.items}
        rowKey={(o) => o.id}
        loading={orders.isLoading}
        fetching={orders.isFetching}
        error={orders.isError ? (orders.error as Error).message : null}
        onRetry={() => orders.refetch()}
        emptyTitle="Chưa có đơn thanh toán trong kỳ"
        pagination={orders.data ? { page: orders.data.pagination.page, limit: orders.data.pagination.limit, total: orders.data.pagination.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
    </>
  );
}
