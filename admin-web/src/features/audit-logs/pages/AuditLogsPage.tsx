import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { DateRangeFilter } from '../../../components/data-display/DateRangeFilter';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { Select, TextInput } from '../../../components/forms/controls';
import { ExportButton } from '../../../components/import-export/ExportButton';
import { IconButton } from '../../../components/ui/Button';
import { Overlay } from '../../../components/ui/Overlay';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDateTime } from '../../../lib/format';
import { api } from '../../../services/api-client';

interface AuditLog {
  id: string; adminEmail: string | null; adminId: string; action: string; targetType: string; targetId: string | null;
  reason: string | null; before: unknown; after: unknown; createdAt: string;
}
interface AuditList { items: AuditLog[]; pagination: { total: number; page: number; limit: number } }

const TARGET_TYPES = ['User', 'PaymentOrder', 'ManualGrant', 'FoodCatalog', 'FoodSubmission', 'ExerciseCatalog', 'SystemConfig', 'DataRequest', 'AdminAuth', 'Export']
  .map((v) => ({ value: v, label: v }));

/** Nhật ký chỉ đọc (append-only): không có nút sửa hay xoá. Trước/sau chỉ hiển thị trường đã được lưu cho phép. */
export default function AuditLogsPage() {
  const list = useListParams(['action', 'targetType', 'targetId', 'adminId', 'from', 'to']);
  const { params } = list;
  const [open, setOpen] = useState<AuditLog | null>(null);
  const apiQuery = { page: params.page, limit: params.limit, action: params.filters.action || params.q || undefined, targetType: params.filters.targetType, targetId: params.filters.targetId, adminId: params.filters.adminId, from: params.filters.from, to: params.filters.to };
  const q = useQuery({
    queryKey: ['audit', apiQuery],
    queryFn: ({ signal }) => api.get<AuditList>('/admin/audit-logs', apiQuery, signal),
    placeholderData: (p) => p,
  });

  const columns: Column<AuditLog>[] = [
    { key: 'time', header: 'Thời gian', cell: (l) => <span className="nowrap">{fmtDateTime(l.createdAt)}</span> },
    { key: 'admin', header: 'Admin', cell: (l) => l.adminEmail ?? '—' },
    { key: 'action', header: 'Hành động', cell: (l) => <StatusBadge tone="primary">{l.action}</StatusBadge> },
    { key: 'type', header: 'Đối tượng', cell: (l) => l.targetType },
    { key: 'id', header: 'Mã đối tượng', cell: (l) => <code>{l.targetId ? l.targetId.slice(0, 13) : '—'}</code> },
    { key: 'reason', header: 'Lý do', cell: (l) => <span style={{ maxWidth: 260, display: 'inline-block' }}>{l.reason ?? '—'}</span> },
  ];
  const exportQuery = { action: apiQuery.action, targetType: apiQuery.targetType, targetId: apiQuery.targetId, adminId: apiQuery.adminId, from: apiQuery.from, to: apiQuery.to };

  return (
    <>
      <PageHeader
        title="Nhật ký hoạt động"
        breadcrumbs={[{ label: 'Bảo mật' }, { label: 'Nhật ký hoạt động' }]}
        total={q.data?.pagination.total}
        actions={<ExportButton kind="audit" query={exportQuery} title="Xuất nhật ký quản trị" />}
      />
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Hành động chính xác, ví dụ APPROVE_PAYMENT" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Loại đối tượng"><Select placeholder="Tất cả" options={TARGET_TYPES} value={params.filters.targetType} onChange={(e) => list.setFilter('targetType', e.target.value)} /></FilterField>
        <FilterField label="Mã đối tượng"><TextInput value={params.filters.targetId} onChange={(e) => list.setFilter('targetId', e.target.value.trim())} placeholder="UUID" style={{ width: 200 }} /></FilterField>
        <DateRangeFilter from={params.filters.from} to={params.filters.to} onChange={(f, t) => { list.setFilter('from', f); list.setFilter('to', t); }} />
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(l) => l.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Chưa có nhật ký phù hợp"
        onRowClick={setOpen}
        actions={(l) => <IconButton icon="Eye" label="Xem chi tiết" onClick={() => setOpen(l)} />}
        pagination={q.data ? { page: q.data.pagination.page, limit: q.data.pagination.limit, total: q.data.pagination.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {open && (
        <Overlay title="Chi tiết nhật ký" size="lg" onClose={() => setOpen(null)}>
          <dl className="kv" style={{ marginBottom: 12 }}>
            <dt>Thời gian</dt><dd>{fmtDateTime(open.createdAt)}</dd>
            <dt>Admin</dt><dd>{open.adminEmail ?? open.adminId}</dd>
            <dt>Hành động</dt><dd>{open.action}</dd>
            <dt>Đối tượng</dt><dd>{open.targetType} · {open.targetId ?? '—'}</dd>
            <dt>Lý do</dt><dd>{open.reason ?? '—'}</dd>
          </dl>
          <div className="grid cols-2">
            <div><b>Giá trị trước</b><pre style={{ background: 'var(--bg)', padding: 10, borderRadius: 8, overflow: 'auto', fontSize: 12 }}>{open.before ? JSON.stringify(open.before, null, 2) : '—'}</pre></div>
            <div><b>Giá trị sau</b><pre style={{ background: 'var(--bg)', padding: 10, borderRadius: 8, overflow: 'auto', fontSize: 12 }}>{open.after ? JSON.stringify(open.after, null, 2) : '—'}</pre></div>
          </div>
        </Overlay>
      )}
    </>
  );
}
