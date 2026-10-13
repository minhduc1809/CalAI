import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ChartContainer, SimpleBarChart, TrendLineChart } from '../../../components/charts/Charts';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { DateRangeFilter } from '../../../components/data-display/DateRangeFilter';
import { FilterBar } from '../../../components/data-display/FilterBar';
import { PageHeader, StatCard } from '../../../components/data-display/parts';
import { Alert } from '../../../components/feedback/States';
import { useListParams } from '../../../hooks/useListParams';
import { fmtNumber, fmtPct, fmtUsd, todayKey } from '../../../lib/format';
import { aiApi, AiUsageMonth } from '../api/ai.api';

type FeatureRow = AiUsageMonth['byFeature'][number];

const FEATURE_LABEL: Record<string, string> = {
  AI_FOOD_SCAN: 'Nhận diện món bằng ảnh', AI_MENU_SCAN: 'Quét thực đơn', AI_SUGGEST_MEAL: 'Gợi ý món', AI_COACH: 'AI Coach (chat)', BARCODE: 'Tra mã vạch',
};

export default function AiMonitoringPage() {
  const list = useListParams(['from', 'to'], { from: todayKey(-29), to: todayKey() });
  const { from, to } = list.params.filters;
  const month = to.slice(0, 7);

  const daily = useQuery({ queryKey: ['ai-daily', from, to], queryFn: ({ signal }) => aiApi.daily(from, to, signal) });
  const monthly = useQuery({ queryKey: ['ai-month', month], queryFn: ({ signal }) => aiApi.month(month, signal) });

  const totals = useMemo(() => {
    const d = daily.data?.days ?? [];
    const calls = d.reduce((a, x) => a + x.calls, 0);
    const errors = d.reduce((a, x) => a + x.errors, 0);
    return { calls, errors, cost: d.reduce((a, x) => a + x.costUsd, 0), tokens: d.reduce((a, x) => a + x.tokens, 0), rate: calls ? (errors / calls) * 100 : null };
  }, [daily.data]);
  const chartData = (daily.data?.days ?? []).map((d) => ({ ...d, label: d.date.slice(5), errorRate: d.errorRatePct ?? 0 }));
  const hasData = chartData.some((d) => d.calls > 0);
  const m = monthly.data;

  const columns: Column<FeatureRow>[] = [
    { key: 'feature', header: 'Tính năng', cell: (f) => <b>{FEATURE_LABEL[f.feature] ?? f.feature}</b> },
    { key: 'calls', header: 'Lời gọi', cell: (f) => fmtNumber(f.calls) },
    { key: 'errors', header: 'Lỗi', cell: (f) => `${fmtNumber(f.errors)} (${fmtPct(f.errorRatePct)})` },
    { key: 'in', header: 'Token vào', cell: (f) => fmtNumber(f.promptTokens) },
    { key: 'out', header: 'Token ra', cell: (f) => fmtNumber(f.outputTokens) },
    { key: 'cost', header: 'Chi phí ước tính', cell: (f) => fmtUsd(f.costUsd) },
    { key: 'avg', header: 'Chi phí/lời gọi', cell: (f) => (f.calls ? fmtUsd(f.costUsd / f.calls) : '—') },
    { key: 'lat', header: 'Độ trễ TB', cell: (f) => `${fmtNumber(f.avgLatencyMs)} ms` },
  ];

  return (
    <>
      <PageHeader
        title="Theo dõi AI"
        description="Token lấy từ số liệu thật của nhà cung cấp; chi phí là ƯỚC TÍNH theo đơn giá cấu hình, chưa đối soát hoá đơn. Không có nội dung chat, ảnh hay dữ liệu sức khỏe."
        breadcrumbs={[{ label: 'AI & Vận hành' }, { label: 'Theo dõi AI' }]}
      />
      <FilterBar activeCount={0} onClear={list.clear}>
        <DateRangeFilter label="Khoảng thời gian" from={from} to={to} onChange={(f, t) => { list.setFilter('from', f); list.setFilter('to', t); }} />
      </FilterBar>
      {daily.data && !daily.data.pricing.configured && <div style={{ marginBottom: 12 }}><Alert kind="warning">Chưa cấu hình đơn giá (AI_PRICE_INPUT_USD_PER_MTOK, AI_PRICE_OUTPUT_USD_PER_MTOK) nên chi phí ước tính có thể bằng 0. {daily.data.pricing.note}</Alert></div>}

      <div className="grid cols-6" style={{ marginBottom: 16 }}>
        <StatCard title="Tổng lời gọi" icon="AIUsage" loading={daily.isLoading} error={daily.isError} value={fmtNumber(totals.calls)} />
        <StatCard title="Tỉ lệ lỗi" icon="Warning" tone={totals.rate && totals.rate > 10 ? 'danger' : 'warning'} loading={daily.isLoading} error={daily.isError} value={totals.rate === null ? '—' : fmtPct(totals.rate)} hint={`${fmtNumber(totals.errors)} lỗi`} />
        <StatCard title="Token" icon="Activity" tone="info" loading={daily.isLoading} error={daily.isError} value={fmtNumber(totals.tokens)} />
        <StatCard title="Chi phí ước tính" icon="Revenue" tone="success" loading={daily.isLoading} error={daily.isError} value={fmtUsd(totals.cost)} />
        <StatCard title="Chi phí hôm nay" icon="Clock" loading={daily.isLoading} error={daily.isError} value={fmtUsd(daily.data?.todayCostUsd ?? null)} />
        <StatCard title={`Tháng ${month}`} icon="Calendar" tone="info" loading={monthly.isLoading} error={monthly.isError} value={m ? fmtUsd(m.totals.costUsd) : ''} hint={m ? `${fmtNumber(m.totals.calls)} lời gọi` : undefined} />
      </div>

      <div className="grid cols-2" style={{ marginBottom: 16 }}>
        <ChartContainer title="Lời gọi theo ngày" loading={daily.isLoading} error={daily.isError ? (daily.error as Error).message : null} onRetry={() => daily.refetch()} empty={!hasData}>
          <SimpleBarChart data={chartData} xKey="label" series={[{ key: 'calls', label: 'Lời gọi' }, { key: 'errors', label: 'Lỗi', color: '#ef4444' }]} />
        </ChartContainer>
        <ChartContainer title="Chi phí ước tính theo ngày (USD)" loading={daily.isLoading} error={daily.isError ? (daily.error as Error).message : null} onRetry={() => daily.refetch()} empty={!hasData}>
          <TrendLineChart data={chartData} xKey="label" series={[{ key: 'costUsd', label: 'Chi phí (USD)' }]} format={(v) => fmtUsd(v)} />
        </ChartContainer>
      </div>
      <div className="grid cols-2" style={{ marginBottom: 16 }}>
        <ChartContainer title="Tỉ lệ lỗi theo ngày (%)" loading={daily.isLoading} error={daily.isError ? (daily.error as Error).message : null} onRetry={() => daily.refetch()} empty={!hasData}>
          <TrendLineChart data={chartData} xKey="label" series={[{ key: 'errorRate', label: 'Tỉ lệ lỗi (%)', color: '#ef4444' }]} format={(v) => `${v}%`} />
        </ChartContainer>
        <div className="card">
          <div className="card-title">Chi phí mỗi người dùng trong tháng {month} (USD)</div>
          {monthly.isLoading ? <span className="muted">Đang tải…</span> : !m || Object.values(m.perUserMonthlyCostUsd).every((v) => v === null) ? <span className="muted">Chưa có dữ liệu.</span> : (
            <dl className="kv">{Object.entries(m.perUserMonthlyCostUsd).map(([k, v]) => (<><dt key={`${k}d`}>{k}</dt><dd key={`${k}v`}>{typeof v === 'number' ? fmtUsd(v) : '—'}</dd></>))}</dl>
          )}
          {m && m.totals.callsWithoutUsage > 0 && <Alert kind="warning">{m.totals.callsWithoutUsage} lời gọi không có số token nên chi phí có thể bị đánh giá thấp.</Alert>}
        </div>
      </div>

      <div style={{ marginBottom: 8 }}><b>Theo tính năng · tháng {month}</b></div>
      <DataTable columns={columns} rows={m?.byFeature} rowKey={(f) => f.feature} loading={monthly.isLoading} error={monthly.isError ? (monthly.error as Error).message : null} onRetry={() => monthly.refetch()} emptyTitle="Chưa có lời gọi AI trong tháng này" />
    </>
  );
}
