import { ReactNode } from 'react';
import { Bar, BarChart, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { EmptyState, ErrorState, Skeleton } from '../feedback/States';

/** Màu series cố định, đủ phân biệt, lấy từ design tokens. */
export const CHART_COLORS = ['#2563eb', '#10b981', '#f59e0b', '#0ea5e9', '#8b5cf6'];

interface ContainerProps {
  title: string;
  description?: string;
  loading?: boolean;
  error?: string | null;
  onRetry?: () => void;
  empty?: boolean;
  emptyText?: string;
  toolbar?: ReactNode;
  children: ReactNode;
}

/** Khung biểu đồ: tiêu đề, loading, lỗi (có thử lại) và trạng thái chưa có dữ liệu thật, không vẽ số giả. */
export function ChartContainer({ title, description, loading, error, onRetry, empty, emptyText = 'Chưa có dữ liệu trong khoảng thời gian này', toolbar, children }: ContainerProps) {
  return (
    <div className="card">
      <div className="row between" style={{ marginBottom: 12 }}>
        <div>
          <div className="card-title" style={{ marginBottom: 2 }}>{title}</div>
          {description && <div className="muted" style={{ fontSize: 12.5 }}>{description}</div>}
        </div>
        {toolbar}
      </div>
      {loading ? <Skeleton height={240} /> : error ? <ErrorState message={error} onRetry={onRetry} /> : empty ? <EmptyState icon="Chart" title={emptyText} /> : <div className="chart-wrap">{children}</div>}
    </div>
  );
}

interface Series { key: string; label: string; color?: string }

const tickStyle = { fontSize: 12, fill: '#6b7280' };

export function TrendLineChart({ data, xKey, series, format }: { data: Record<string, unknown>[]; xKey: string; series: Series[]; format?: (v: number) => string }) {
  return (
    <ResponsiveContainer>
      <LineChart data={data} margin={{ top: 6, right: 12, left: 0, bottom: 0 }}>
        <CartesianGrid stroke="#eef0f4" vertical={false} />
        <XAxis dataKey={xKey} tick={tickStyle} tickLine={false} axisLine={false} minTickGap={24} />
        <YAxis tick={tickStyle} tickLine={false} axisLine={false} width={44} tickFormatter={format} allowDecimals={false} />
        <Tooltip formatter={(v: number) => (format ? format(v) : v)} />
        <Legend />
        {series.map((s, i) => (
          <Line key={s.key} type="monotone" dataKey={s.key} name={s.label} stroke={s.color ?? CHART_COLORS[i % CHART_COLORS.length]} strokeWidth={2.2} dot={false} />
        ))}
      </LineChart>
    </ResponsiveContainer>
  );
}

export function SimpleBarChart({ data, xKey, series, format }: { data: Record<string, unknown>[]; xKey: string; series: Series[]; format?: (v: number) => string }) {
  return (
    <ResponsiveContainer>
      <BarChart data={data} margin={{ top: 6, right: 12, left: 0, bottom: 0 }}>
        <CartesianGrid stroke="#eef0f4" vertical={false} />
        <XAxis dataKey={xKey} tick={tickStyle} tickLine={false} axisLine={false} minTickGap={24} />
        <YAxis tick={tickStyle} tickLine={false} axisLine={false} width={56} tickFormatter={format} />
        <Tooltip formatter={(v: number) => (format ? format(v) : v)} />
        <Legend />
        {series.map((s, i) => (
          <Bar key={s.key} dataKey={s.key} name={s.label} fill={s.color ?? CHART_COLORS[i % CHART_COLORS.length]} radius={[4, 4, 0, 0]} />
        ))}
      </BarChart>
    </ResponsiveContainer>
  );
}
