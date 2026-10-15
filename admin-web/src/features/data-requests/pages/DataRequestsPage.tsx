import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { DateRangeFilter } from '../../../components/data-display/DateRangeFilter';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { Alert } from '../../../components/feedback/States';
import { FormField, Select, Textarea, TextInput } from '../../../components/forms/controls';
import { FormActions, FormErrorSummary } from '../../../components/forms/FormParts';
import { ExportButton } from '../../../components/import-export/ExportButton';
import { Button } from '../../../components/ui/Button';
import { Overlay } from '../../../components/ui/Overlay';
import { useToast } from '../../../components/ui/Toast';
import { useForm } from '../../../hooks/useForm';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDateTime } from '../../../lib/format';
import { DataRequest, requestApi, RequestStatus } from '../api/request.api';

const TYPE = [
  { value: 'EXPORT', label: 'Xuất bản sao dữ liệu' },
  { value: 'DELETE', label: 'Xoá dữ liệu / tài khoản' },
  { value: 'COMPLAINT', label: 'Khiếu nại' },
];
const STATUS: Record<RequestStatus, { label: string; tone: 'warning' | 'info' | 'success' | 'danger' }> = {
  NEW: { label: 'Mới', tone: 'warning' }, IN_PROGRESS: { label: 'Đang xử lý', tone: 'info' }, DONE: { label: 'Hoàn tất', tone: 'success' }, REJECTED: { label: 'Từ chối', tone: 'danger' },
};
const typeLabel = (t: string) => TYPE.find((x) => x.value === t)?.label ?? t;

export default function DataRequestsPage() {
  const list = useListParams(['status', 'type', 'from', 'to']);
  const { params } = list;
  const toast = useToast();
  const qc = useQueryClient();
  const [creating, setCreating] = useState(false);
  const [closing, setClosing] = useState<{ row: DataRequest; status: 'DONE' | 'REJECTED' } | null>(null);

  const apiQuery = { page: params.page, limit: params.limit, status: params.filters.status, type: params.filters.type, from: params.filters.from, to: params.filters.to };
  const q = useQuery({ queryKey: ['data-requests', apiQuery], queryFn: ({ signal }) => requestApi.list(apiQuery, signal), placeholderData: (p) => p });

  const refresh = () => { qc.invalidateQueries({ queryKey: ['data-requests'] }); qc.invalidateQueries({ queryKey: ['pending-work'] }); };
  const start = useMutation({
    mutationFn: (r: DataRequest) => requestApi.update(r.id, { status: 'IN_PROGRESS' }),
    onSuccess: () => { refresh(); toast.success('Đã nhận xử lý yêu cầu'); },
    onError: (e: Error) => toast.error('Không cập nhật được', e.message),
  });

  const columns: Column<DataRequest>[] = [
    { key: 'email', header: 'Email', cell: (r) => (<>{r.email}{!r.userId && <div><StatusBadge tone="warning">Không khớp tài khoản</StatusBadge></div>}</>) },
    { key: 'type', header: 'Loại', cell: (r) => typeLabel(r.type) },
    { key: 'note', header: 'Nội dung', cell: (r) => (<>{r.note ?? '—'}{r.resultNote && <div className="sub">Xử lý: {r.resultNote}</div>}</>) },
    { key: 'received', header: 'Nhận lúc', cell: (r) => <span className="nowrap">{fmtDateTime(r.createdAt)}</span> },
    { key: 'due', header: 'Hạn xử lý', cell: (r) => (<><span className="nowrap">{fmtDateTime(r.dueAt)}</span>{r.overdue && <div><StatusBadge tone="danger">Quá hạn</StatusBadge></div>}</>) },
    { key: 'status', header: 'Trạng thái', cell: (r) => <StatusBadge tone={STATUS[r.status].tone}>{STATUS[r.status].label}</StatusBadge> },
  ];

  return (
    <>
      <PageHeader
        title="Yêu cầu quyền dữ liệu"
        breadcrumbs={[{ label: 'Khách hàng' }, { label: 'Yêu cầu dữ liệu' }]}
        total={q.data?.total}
        actions={(<><ExportButton kind="data-requests" query={{ status: params.filters.status, type: params.filters.type, from: params.filters.from, to: params.filters.to }} title="Xuất yêu cầu dữ liệu" /><Button variant="primary" icon="Plus" onClick={() => setCreating(true)}>Ghi nhận yêu cầu</Button></>)}
      />
      <div style={{ height: 12 }} />
      <FilterBar activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Trạng thái"><Select placeholder="Đang mở (Mới + Đang xử lý)" options={(Object.keys(STATUS) as RequestStatus[]).map((v) => ({ value: v, label: STATUS[v].label }))} value={params.filters.status} onChange={(e) => list.setFilter('status', e.target.value)} /></FilterField>
        <FilterField label="Loại yêu cầu"><Select placeholder="Tất cả" options={TYPE} value={params.filters.type} onChange={(e) => list.setFilter('type', e.target.value)} /></FilterField>
        <DateRangeFilter label="Ngày nhận" from={params.filters.from} to={params.filters.to} onChange={(f, t) => { list.setFilter('from', f); list.setFilter('to', t); }} />
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(r) => r.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Không có yêu cầu nào"
        actions={(r) => (r.status === 'NEW' || r.status === 'IN_PROGRESS') && (
          <>
            {r.status === 'NEW' && <Button size="sm" variant="secondary" loading={start.isPending && start.variables?.id === r.id} onClick={() => start.mutate(r)}>Nhận xử lý</Button>}{' '}
            <Button size="sm" variant="primary" onClick={() => setClosing({ row: r, status: 'DONE' })}>Hoàn tất</Button>{' '}
            <Button size="sm" variant="danger" onClick={() => setClosing({ row: r, status: 'REJECTED' })}>Từ chối</Button>
          </>
        )}
        pagination={q.data ? { page: q.data.page, limit: q.data.limit, total: q.data.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {creating && <CreateDialog onClose={() => setCreating(false)} onDone={(matched) => { setCreating(false); refresh(); toast.success('Đã ghi nhận yêu cầu', matched ? undefined : 'Email không khớp tài khoản nào'); }} />}
      {closing && <CloseDialog row={closing.row} status={closing.status} onClose={() => setClosing(null)} onDone={() => { setClosing(null); refresh(); toast.success('Đã cập nhật yêu cầu'); }} />}
    </>
  );
}

interface CreateValues extends Record<string, unknown> { email: string; type: string; note: string }

function CreateDialog({ onClose, onDone }: { onClose: () => void; onDone: (matched: boolean) => void }) {
  let matched = false;
  const form = useForm<CreateValues>({
    initial: { email: '', type: 'EXPORT', note: '' },
    validate: (v) => ({ email: /^\S+@\S+\.\S+$/.test(v.email.trim()) ? undefined : 'Nhập email đúng định dạng' }),
    onSubmit: async (v) => { matched = (await requestApi.create({ email: v.email.trim(), type: v.type, note: v.note.trim() || undefined })).matchedAccount; },
    onSuccess: () => onDone(matched),
  });
  return (
    <Overlay title="Ghi nhận yêu cầu từ email" onClose={onClose} busy={form.submitting}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        <FormField label="Email người gửi" required htmlFor="dr-email" error={form.errors.email} help="Phải là email đã đăng ký tài khoản để khớp được tài khoản."><TextInput id="dr-email" type="email" invalid={!!form.errors.email} value={form.values.email} onChange={(e) => form.setValue('email', e.target.value)} /></FormField>
        <FormField label="Loại yêu cầu" required htmlFor="dr-type"><Select id="dr-type" options={TYPE} value={form.values.type} onChange={(e) => form.setValue('type', e.target.value)} /></FormField>
        <FormField label="Nội dung" htmlFor="dr-note"><Textarea id="dr-note" value={form.values.note} onChange={(e) => form.setValue('note', e.target.value)} /></FormField>
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel="Ghi nhận" />
      </form>
    </Overlay>
  );
}

function CloseDialog({ row, status, onClose, onDone }: { row: DataRequest; status: 'DONE' | 'REJECTED'; onClose: () => void; onDone: () => void }) {
  const form = useForm<{ note: string } & Record<string, unknown>>({
    initial: { note: '' },
    validate: (v) => ({ note: v.note.trim() ? undefined : status === 'DONE' ? 'Ghi lại cách đã xử lý' : 'Ghi lý do từ chối' }),
    onSubmit: (v) => requestApi.update(row.id, { status, resultNote: v.note.trim() }) as Promise<void>,
    onSuccess: onDone,
  });
  return (
    <Overlay title={status === 'DONE' ? `Hoàn tất yêu cầu của ${row.email}` : `Từ chối yêu cầu của ${row.email}`} onClose={onClose} busy={form.submitting}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        <FormField label={status === 'DONE' ? 'Đã xử lý như thế nào' : 'Lý do từ chối'} required htmlFor="dr-close" error={form.errors.note}>
          <Textarea id="dr-close" rows={4} invalid={!!form.errors.note} value={form.values.note} onChange={(e) => form.setValue('note', e.target.value)} />
        </FormField>
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel="Xác nhận" submitVariant={status === 'REJECTED' ? 'danger' : 'primary'} />
      </form>
    </Overlay>
  );
}
