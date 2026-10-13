import { useQuery } from '@tanstack/react-query';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { ErrorState, LoadingState } from '../../../components/feedback/States';
import { Button } from '../../../components/ui/Button';
import { fmtDateTime, fmtNumber, fmtPct } from '../../../lib/format';
import { Check, healthApi, Level } from '../api/health.api';

const LEVEL: Record<Level, { label: string; tone: 'success' | 'warning' | 'danger' | 'neutral' }> = {
  OK: { label: 'Tốt', tone: 'success' },
  WARN: { label: 'Cần chú ý', tone: 'warning' },
  DOWN: { label: 'Lỗi', tone: 'danger' },
  UNKNOWN: { label: 'Chưa rõ', tone: 'neutral' },
};

function Row({ title, check, extra }: { title: string; check: Check; extra?: string }) {
  return (
    <div className="card">
      <div className="row between"><b>{title}</b><StatusBadge tone={LEVEL[check.level].tone}>{LEVEL[check.level].label}</StatusBadge></div>
      <p className="muted" style={{ margin: '8px 0 0' }}>{check.detail}{check.latencyMs != null ? ` · ${check.latencyMs} ms` : ''}</p>
      {extra && <p className="muted" style={{ margin: '4px 0 0', fontSize: 12.5 }}>{extra}</p>}
    </div>
  );
}

/** Trang chỉ đọc: chỉ hiển thị những gì backend đo hoặc đọc được, không có khoá hay chuỗi kết nối. */
export default function SystemHealthPage() {
  const q = useQuery({ queryKey: ['system-health'], queryFn: ({ signal }) => healthApi.get(signal), refetchInterval: 60_000 });
  const h = q.data;
  return (
    <>
      <PageHeader
        title="Tình trạng hệ thống"
        breadcrumbs={[{ label: 'Vận hành' }, { label: 'Tình trạng hệ thống' }]}
        actions={<Button icon="Reset" loading={q.isFetching} onClick={() => q.refetch()}>Làm mới</Button>}
      />
      {q.isLoading ? <LoadingState lines={6} /> : q.isError || !h ? <ErrorState message={(q.error as Error)?.message ?? 'Không tải được'} onRetry={() => q.refetch()} /> : (
        <div className="stack">
          <div className="card row between">
            <div><b>Tổng thể</b><div className="muted">Kiểm tra lúc {fmtDateTime(h.checkedAt)}</div></div>
            <StatusBadge tone={LEVEL[h.overall].tone}>{LEVEL[h.overall].label}</StatusBadge>
          </div>
          <div className="grid cols-2">
            <Row title="Cơ sở dữ liệu" check={h.database} extra={h.database.migrations != null ? `${h.database.migrations} migration đã áp dụng · mới nhất ${h.database.lastMigration ?? '—'}` : undefined} />
            <Row title="Dịch vụ AI (Gemini)" check={h.ai} extra={`24 giờ qua: ${fmtNumber(h.ai.calls24h)} lời gọi, ${fmtNumber(h.ai.errors24h)} lỗi (${h.ai.errorRatePct === null ? '—' : fmtPct(h.ai.errorRatePct)})`} />
            <Row title="Email (SMTP)" check={h.email} />
            <Row title="Google Play" check={h.googlePlay} />
          </div>
          <div className="grid cols-2">
            <div className="card">
              <div className="card-title">Độ trễ và lỗi HTTP</div>
              <dl className="kv">
                <dt>Số yêu cầu ({h.http.windowMinutes} phút)</dt><dd>{fmtNumber(h.http.requests)}</dd>
                <dt>Độ trễ trung bình</dt><dd>{h.http.avgMs === null ? '—' : `${h.http.avgMs} ms`}</dd>
                <dt>Độ trễ p95</dt><dd>{h.http.p95Ms === null ? '—' : `${h.http.p95Ms} ms`}</dd>
                <dt>Lỗi 5xx (24 giờ)</dt><dd><StatusBadge tone={h.http.errors5xx24h ? 'danger' : 'success'}>{h.http.errors5xx24h}</StatusBadge></dd>
              </dl>
              <p className="muted" style={{ fontSize: 12.5 }}>Số đo nằm trong bộ nhớ của tiến trình, mất khi khởi động lại; chạy nhiều instance thì mỗi instance có số đo riêng.</p>
            </div>
            <div className="card">
              <div className="card-title">Sao lưu và phiên bản</div>
              <dl className="kv">
                <dt>Lần sao lưu cuối</dt><dd>{h.backup.lastBackupAt ? fmtDateTime(h.backup.lastBackupAt) : <span className="muted">Chưa có dữ liệu</span>}</dd>
                <dt>Ghi chú</dt><dd className="muted">{h.backup.note}</dd>
                <dt>Phiên bản backend</dt><dd>{h.runtime.version ?? '—'}</dd>
                <dt>Môi trường</dt><dd>{h.runtime.environment} · Node {h.runtime.nodeVersion}</dd>
                <dt>Thời gian chạy</dt><dd>{Math.floor(h.runtime.uptimeSeconds / 3600)} giờ {Math.floor((h.runtime.uptimeSeconds % 3600) / 60)} phút</dd>
              </dl>
            </div>
          </div>
          <div className="card">
            <div className="card-title">Lỗi 5xx gần đây</div>
            {h.http.recentErrors.length === 0 ? <span className="muted">Không có lỗi nào được ghi nhận kể từ lần khởi động gần nhất.</span> : (
              <div className="table-wrap"><table className="table">
                <thead><tr><th>Thời gian</th><th>Yêu cầu</th><th>Thông báo</th></tr></thead>
                <tbody>{h.http.recentErrors.map((e, i) => (<tr key={i}><td className="nowrap">{fmtDateTime(e.at)}</td><td>{e.method} {e.path}</td><td>{e.message}</td></tr>))}</tbody>
              </table></div>
            )}
          </div>
        </div>
      )}
    </>
  );
}
