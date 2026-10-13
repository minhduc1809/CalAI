import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { DateRangeFilter } from '../../../components/data-display/DateRangeFilter';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader } from '../../../components/data-display/parts';
import { ReasonPasswordDialog } from '../../../components/feedback/ConfirmDialogs';
import { Alert, ErrorState, LoadingState } from '../../../components/feedback/States';
import { NumberInput, Select, Textarea } from '../../../components/forms/controls';
import { ExportButton } from '../../../components/import-export/ExportButton';
import { Button, IconButton } from '../../../components/ui/Button';
import { Overlay } from '../../../components/ui/Overlay';
import { useToast } from '../../../components/ui/Toast';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDateTime, fmtVnd } from '../../../lib/format';
import { orderApi } from '../api/order.api';
import { ORDER_STATUS_OPTIONS, OrderStatusBadge } from '../components/OrderStatusBadge';
import type { QrOrder } from '../types/order.types';

const FILTERS = ['status', 'from', 'to', 'minAmount', 'maxAmount'];
const ACTION_LABEL: Record<string, string> = { APPROVE_PAYMENT: 'Duyệt đơn', REJECT_PAYMENT: 'Huỷ đơn', NOTE_PAYMENT: 'Ghi chú' };

export default function QrOrdersPage() {
  const toast = useToast();
  const qc = useQueryClient();
  const list = useListParams(FILTERS);
  const { params } = list;
  const [openId, setOpenId] = useState<string | null>(null);
  const [approving, setApproving] = useState<QrOrder | null>(null);
  const [rejecting, setRejecting] = useState<QrOrder | null>(null);

  const apiQuery = {
    page: params.page, limit: params.limit, search: params.q || undefined, sort: params.sort, order: params.order,
    status: params.filters.status, from: params.filters.from, to: params.filters.to, minAmount: params.filters.minAmount, maxAmount: params.filters.maxAmount,
  };
  const q = useQuery({ queryKey: ['orders', apiQuery], queryFn: ({ signal }) => orderApi.list(apiQuery, signal), placeholderData: (p) => p });

  const refresh = () => {
    qc.invalidateQueries({ queryKey: ['orders'] });
    qc.invalidateQueries({ queryKey: ['order'] });
    qc.invalidateQueries({ queryKey: ['pending-work'] });
    qc.invalidateQueries({ queryKey: ['grants'] });
  };
  // Tiền và Premium: chỉ báo thành công sau khi máy chủ xác nhận (không cập nhật lạc quan)
  const approve = useMutation({
    mutationFn: ({ o, password, reason }: { o: QrOrder; password: string; reason: string }) => orderApi.approve(o.id, { password, note: reason || undefined }),
    onSuccess: (_d, v) => { refresh(); toast.success('Đã duyệt đơn', `${v.o.code} · Premium đã được kích hoạt`); },
  });
  const reject = useMutation({
    mutationFn: ({ o, password, reason }: { o: QrOrder; password: string; reason: string }) => orderApi.reject(o.id, { reason, password }),
    onSuccess: (_d, v) => { refresh(); toast.success('Đã huỷ đơn', v.o.code); },
  });

  const columns: Column<QrOrder>[] = [
    { key: 'code', header: 'Mã nội dung CK', cell: (o) => <b>{o.code}</b> },
    { key: 'user', header: 'Người dùng', cell: (o) => o.user.email ?? o.user.name ?? '—' },
    { key: 'product', header: 'Gói', cell: (o) => o.productId },
    { key: 'amount', header: 'Số tiền', sortKey: 'amount', cell: (o) => <b>{fmtVnd(o.amount)}</b> },
    { key: 'status', header: 'Trạng thái', cell: (o) => <OrderStatusBadge status={o.status} /> },
    { key: 'created', header: 'Tạo lúc', sortKey: 'createdAt', cell: (o) => <span className="nowrap">{fmtDateTime(o.createdAt)}</span> },
    { key: 'expires', header: 'Hạn thanh toán', sortKey: 'expiresAt', cell: (o) => <span className="nowrap">{fmtDateTime(o.expiresAt)}</span> },
    { key: 'paid', header: 'Xử lý lúc', cell: (o) => <span className="nowrap">{fmtDateTime(o.paidAt)}</span> },
  ];

  const exportQuery = { search: params.q || undefined, ...params.filters, sort: params.sort, order: params.order };

  return (
    <>
      <PageHeader
        title="Đơn chuyển khoản QR"
        description="Đối chiếu nội dung chuyển khoản, số tiền và thời điểm với sao kê ngân hàng rồi mới duyệt."
        breadcrumbs={[{ label: 'Doanh thu & Thanh toán' }, { label: 'Đơn chuyển khoản QR' }]}
        total={q.data?.pagination.total}
        actions={<ExportButton kind="orders" query={exportQuery} title="Xuất danh sách đơn QR" />}
      />
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Tìm theo mã nội dung hoặc email" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Trạng thái"><Select placeholder="Tất cả" options={ORDER_STATUS_OPTIONS} value={params.filters.status} onChange={(e) => list.setFilter('status', e.target.value)} /></FilterField>
        <DateRangeFilter label="Ngày tạo đơn" from={params.filters.from} to={params.filters.to} onChange={(f, t) => { list.setFilter('from', f); list.setFilter('to', t); }} />
        <FilterField label="Số tiền từ (VND)"><NumberInput min={0} value={params.filters.minAmount} onChange={(e) => list.setFilter('minAmount', e.target.value)} style={{ width: 130 }} /></FilterField>
        <FilterField label="đến (VND)"><NumberInput min={0} value={params.filters.maxAmount} onChange={(e) => list.setFilter('maxAmount', e.target.value)} style={{ width: 130 }} /></FilterField>
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(o) => o.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Không có đơn nào"
        emptyDescription="Thử đổi hoặc xoá bớt bộ lọc."
        sort={{ sort: params.sort, order: params.order }}
        onSort={list.setSort}
        onRowClick={(o) => setOpenId(o.id)}
        actions={(o) => (
          <>
            <IconButton icon="Eye" label="Xem chi tiết" onClick={() => setOpenId(o.id)} />
            {o.status === 'PENDING' && (<>
              <IconButton icon="CheckCircle" label="Duyệt đơn" onClick={() => setApproving(o)} />
              <IconButton icon="XCircle" label="Huỷ đơn" onClick={() => setRejecting(o)} />
            </>)}
          </>
        )}
        pagination={q.data ? { page: q.data.pagination.page, limit: q.data.pagination.limit, total: q.data.pagination.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {openId && <OrderDrawer id={openId} onClose={() => setOpenId(null)} onApprove={setApproving} onReject={setRejecting} />}
      {approving && (
        <ReasonPasswordDialog
          title="Duyệt đơn và kích hoạt Premium"
          subject={<span><b>{approving.code}</b> · {approving.user.email} · <b>{fmtVnd(approving.amount)}</b></span>}
          description="Chỉ duyệt khi đã thấy tiền về tài khoản với đúng nội dung chuyển khoản và đúng số tiền. Hệ thống không tự đối chiếu sao kê."
          requireReason={false}
          confirmLabel="Duyệt đơn"
          onConfirm={async (v) => { await approve.mutateAsync({ o: approving, ...v }); }}
          onClose={() => setApproving(null)}
        />
      )}
      {rejecting && (
        <ReasonPasswordDialog
          title="Huỷ đơn"
          subject={<span><b>{rejecting.code}</b> · {rejecting.user.email}</span>}
          description="Huỷ khác với thanh toán thất bại: đơn bị đóng và người dùng sẽ không nhận Premium từ đơn này."
          confirmLabel="Huỷ đơn"
          danger
          onConfirm={async (v) => { await reject.mutateAsync({ o: rejecting, ...v }); }}
          onClose={() => setRejecting(null)}
        />
      )}
    </>
  );
}

function OrderDrawer({ id, onClose, onApprove, onReject }: { id: string; onClose: () => void; onApprove: (o: QrOrder) => void; onReject: (o: QrOrder) => void }) {
  const toast = useToast();
  const qc = useQueryClient();
  const d = useQuery({ queryKey: ['order', id], queryFn: () => orderApi.detail(id) });
  const [note, setNote] = useState<string | null>(null);
  const save = useMutation({
    mutationFn: (text: string) => orderApi.setNote(id, text),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['order', id] }); qc.invalidateQueries({ queryKey: ['orders'] }); toast.success('Đã lưu ghi chú'); setNote(null); },
    onError: (e: Error) => toast.error('Không lưu được ghi chú', e.message),
  });
  const o = d.data;
  return (
    <Overlay title="Chi tiết đơn" variant="drawer" onClose={onClose} busy={save.isPending}>
      {d.isLoading ? <LoadingState lines={6} /> : d.isError || !o ? <ErrorState message={(d.error as Error)?.message ?? 'Không tải được'} onRetry={() => d.refetch()} /> : (
        <div className="stack">
          {o.pastDue && <Alert kind="warning">Đơn đã quá hạn thanh toán nhưng vẫn còn trong thời gian cho phép duyệt muộn. Đối chiếu thật kỹ thời điểm chuyển khoản trên sao kê.</Alert>}
          <dl className="kv">
            <dt>Mã nội dung CK</dt><dd><b>{o.code}</b></dd>
            <dt>Số tiền cần nhận</dt><dd><b>{fmtVnd(o.amount)}</b></dd>
            <dt>Gói</dt><dd>{o.productId}</dd>
            <dt>Người dùng</dt><dd>{o.user.email}</dd>
            <dt>Trạng thái</dt><dd><OrderStatusBadge status={o.status} /></dd>
            <dt>Tạo lúc</dt><dd>{fmtDateTime(o.createdAt)}</dd>
            <dt>Hạn thanh toán</dt><dd>{fmtDateTime(o.expiresAt)}</dd>
            <dt>Xử lý lúc</dt><dd>{fmtDateTime(o.paidAt)}</dd>
            <dt>Người duyệt</dt><dd>{o.approvedBy === 'AUTO:BANK' ? 'Tự động (ngân hàng)' : o.approvedBy ? 'Admin' : '—'}</dd>
          </dl>
          <div>
            <b>Ghi chú nội bộ</b>
            <Textarea rows={2} value={note ?? o.adminNote ?? ''} onChange={(e) => setNote(e.target.value)} placeholder="Ví dụ: đã đối chiếu sao kê ngày…" />
            <div className="row" style={{ marginTop: 8 }}><Button size="sm" loading={save.isPending} disabled={note === null || note === (o.adminNote ?? '')} onClick={() => save.mutate(note ?? '')}>Lưu ghi chú</Button></div>
          </div>
          <div>
            <b>Lịch sử xử lý</b>
            {o.history.length === 0 ? <p className="muted">Chưa có thao tác nào.</p> : (
              <ul style={{ paddingLeft: 18 }}>{o.history.map((h) => (<li key={h.id}><b>{ACTION_LABEL[h.action] ?? h.action}</b> · {h.adminEmail ?? 'Hệ thống'} · {fmtDateTime(h.createdAt)}{h.reason && <div className="muted">{h.reason}</div>}</li>))}</ul>
            )}
          </div>
          {o.status === 'PENDING' && (
            <div className="row">
              <Button variant="primary" icon="CheckCircle" onClick={() => onApprove(o)}>Duyệt đơn</Button>
              <Button variant="danger" icon="XCircle" onClick={() => onReject(o)}>Huỷ đơn</Button>
            </div>
          )}
        </div>
      )}
    </Overlay>
  );
}
