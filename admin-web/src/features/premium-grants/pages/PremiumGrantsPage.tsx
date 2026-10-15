import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { ReasonPasswordDialog } from '../../../components/feedback/ConfirmDialogs';
import { Select } from '../../../components/forms/controls';
import { Alert } from '../../../components/feedback/States';
import { Button } from '../../../components/ui/Button';
import { useToast } from '../../../components/ui/Toast';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDate, fmtDateTime } from '../../../lib/format';
import { Grant, grantApi } from '../api/grant.api';
import { GrantDialog } from '../components/GrantDialog';

const STATUS = [
  { value: 'ACTIVE', label: 'Còn hiệu lực' },
  { value: 'EXPIRED', label: 'Đã hết hạn' },
  { value: 'REVOKED', label: 'Đã thu hồi' },
];
const SOURCE = [
  { value: 'ADMIN', label: 'Admin cấp tay' },
  { value: 'QR_ORDER', label: 'Từ đơn QR' },
];

export default function PremiumGrantsPage() {
  const toast = useToast();
  const qc = useQueryClient();
  const list = useListParams(['status', 'source']);
  const { params } = list;
  const [granting, setGranting] = useState(false);
  const [revoking, setRevoking] = useState<Grant | null>(null);

  const apiQuery = { page: params.page, limit: params.limit, search: params.q || undefined, status: params.filters.status, source: params.filters.source };
  const q = useQuery({ queryKey: ['grants', apiQuery], queryFn: ({ signal }) => grantApi.list(apiQuery, signal), placeholderData: (p) => p });

  const revoke = useMutation({
    mutationFn: ({ g, reason, password }: { g: Grant; reason: string; password: string }) => grantApi.revoke(g.id, { reason, password }),
    onSuccess: (_d, v) => {
      qc.invalidateQueries({ queryKey: ['grants'] });
      toast.success('Đã thu hồi quyền Premium', v.g.user.email ?? '');
    },
  });

  const columns: Column<Grant>[] = [
    { key: 'user', header: 'Người dùng', cell: (g) => <b>{g.user.email ?? g.user.name ?? '—'}</b> },
    { key: 'source', header: 'Nguồn', cell: (g) => <StatusBadge tone={g.source === 'QR_ORDER' ? 'info' : 'primary'}>{g.source === 'QR_ORDER' ? `Đơn QR${g.orderCode ? ` · ${g.orderCode}` : ''}` : 'Admin cấp tay'}</StatusBadge> },
    { key: 'by', header: 'Người cấp', cell: (g) => g.grantedBy ?? <span className="muted">Tự động</span> },
    { key: 'reason', header: 'Lý do', cell: (g) => <span style={{ maxWidth: 260, display: 'inline-block' }}>{g.reason}</span> },
    { key: 'time', header: 'Hiệu lực', cell: (g) => <span className="nowrap">{fmtDate(g.startsAt)} → {fmtDate(g.endsAt)}</span> },
    { key: 'left', header: 'Còn lại', cell: (g) => (g.status === 'ACTIVE' ? `${g.remainingDays} ngày` : '—') },
    { key: 'status', header: 'Trạng thái', cell: (g) => <StatusBadge tone={g.status === 'ACTIVE' ? 'success' : g.status === 'REVOKED' ? 'danger' : 'neutral'}>{g.status === 'ACTIVE' ? 'Còn hiệu lực' : g.status === 'REVOKED' ? `Đã thu hồi ${fmtDateTime(g.revokedAt)}` : 'Đã hết hạn'}</StatusBadge> },
  ];

  return (
    <>
      <PageHeader
        title="Cấp Premium thủ công"
        breadcrumbs={[{ label: 'Thanh toán' }, { label: 'Cấp Premium thủ công' }]}
        total={q.data?.pagination.total}
        actions={<Button variant="primary" icon="Plus" onClick={() => setGranting(true)}>Cấp Premium</Button>}
      />
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Tìm theo email người dùng" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Trạng thái"><Select placeholder="Tất cả" options={STATUS} value={params.filters.status} onChange={(e) => list.setFilter('status', e.target.value)} /></FilterField>
        <FilterField label="Nguồn"><Select placeholder="Tất cả" options={SOURCE} value={params.filters.source} onChange={(e) => list.setFilter('source', e.target.value)} /></FilterField>
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(g) => g.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Chưa có quyền Premium nào"
        actions={(g) => (g.status === 'ACTIVE' ? <Button size="sm" variant="secondary" onClick={() => setRevoking(g)}>Thu hồi</Button> : null)}
        pagination={q.data ? { page: q.data.pagination.page, limit: q.data.pagination.limit, total: q.data.pagination.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {granting && <GrantDialog onClose={() => setGranting(false)} />}
      {revoking && (
        <ReasonPasswordDialog
          title="Thu hồi quyền Premium"
          subject={<span>{revoking.user.email} · còn {revoking.remainingDays} ngày</span>}
          description={revoking.source === 'QR_ORDER' ? 'Quyền này sinh ra từ đơn đã thanh toán. Chỉ thu hồi khi có lý do chính đáng (ví dụ hoàn tiền).' : undefined}
          confirmLabel="Thu hồi quyền"
          danger
          onConfirm={async (v) => { await revoke.mutateAsync({ g: revoking, ...v }); }}
          onClose={() => setRevoking(null)}
        />
      )}
    </>
  );
}
