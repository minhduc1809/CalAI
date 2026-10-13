import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { DateRangeFilter } from '../../../components/data-display/DateRangeFilter';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { ReasonPasswordDialog } from '../../../components/feedback/ConfirmDialogs';
import { Select } from '../../../components/forms/controls';
import { ExportButton } from '../../../components/import-export/ExportButton';
import { IconButton } from '../../../components/ui/Button';
import { useToast } from '../../../components/ui/Toast';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDate } from '../../../lib/format';
import { userApi } from '../api/user.api';
import { AccountStatusBadge, PlanBadge, planOf } from '../components/UserBadges';
import { PLAN_OPTIONS, STATUS_OPTIONS, UserRow, VERIFIED_OPTIONS } from '../types/user.types';

const FILTERS = ['plan', 'status', 'emailVerified', 'from', 'to'];

export default function UsersPage() {
  const nav = useNavigate();
  const toast = useToast();
  const qc = useQueryClient();
  const list = useListParams(FILTERS);
  const { params } = list;
  const [lockTarget, setLockTarget] = useState<UserRow | null>(null);

  const apiQuery = {
    page: params.page, limit: params.limit, search: params.q || undefined, sort: params.sort, order: params.order,
    plan: params.filters.plan, status: params.filters.status, emailVerified: params.filters.emailVerified, from: params.filters.from, to: params.filters.to,
  };
  const q = useQuery({ queryKey: ['users', apiQuery], queryFn: ({ signal }) => userApi.list(apiQuery, signal), placeholderData: (prev) => prev });

  const lockMutation = useMutation({
    mutationFn: ({ user, reason, password }: { user: UserRow; reason: string; password: string }) =>
      user.isActive ? userApi.lock(user.id, { reason, password }) : userApi.unlock(user.id, { reason, password }),
    onSuccess: (_d, v) => {
      qc.invalidateQueries({ queryKey: ['users'] });
      toast.success(v.user.isActive ? 'Đã khoá tài khoản' : 'Đã mở khoá tài khoản', v.user.email ?? v.user.username);
    },
  });

  const columns: Column<UserRow>[] = [
    { key: 'email', header: 'Email', sortKey: 'email', cell: (u) => (<><b>{u.email ?? '—'}</b><div className="sub">{u.name ?? u.username}</div></>) },
    { key: 'plan', header: 'Gói', cell: (u) => <PlanBadge user={u} /> },
    { key: 'ends', header: 'Hết hạn Premium', cell: (u) => fmtDate(planOf(u).endsAt) },
    { key: 'status', header: 'Trạng thái', cell: (u) => <AccountStatusBadge active={u.isActive} /> },
    { key: 'verified', header: 'Email', cell: (u) => <StatusBadge tone={u.isEmailVerified ? 'success' : 'warning'}>{u.isEmailVerified ? 'Đã xác thực' : 'Chưa xác thực'}</StatusBadge> },
    { key: 'created', header: 'Ngày đăng ký', sortKey: 'createdAt', cell: (u) => <span className="nowrap">{fmtDate(u.createdAt)}</span> },
  ];

  const exportQuery = { search: params.q || undefined, ...params.filters, sort: params.sort, order: params.order };

  return (
    <>
      <PageHeader
        title="Danh sách người dùng"
        description="Chỉ thông tin tài khoản, gói và trạng thái. Không có dữ liệu sức khỏe của người dùng."
        breadcrumbs={[{ label: 'Người dùng & Hỗ trợ' }, { label: 'Danh sách người dùng' }]}
        total={q.data?.pagination.total}
        actions={<ExportButton kind="users" query={exportQuery} title="Xuất danh sách người dùng" />}
      />
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Tìm theo email, tên hoặc tên đăng nhập" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Gói"><Select placeholder="Tất cả" options={PLAN_OPTIONS} value={params.filters.plan} onChange={(e) => list.setFilter('plan', e.target.value)} /></FilterField>
        <FilterField label="Trạng thái"><Select placeholder="Tất cả" options={STATUS_OPTIONS} value={params.filters.status} onChange={(e) => list.setFilter('status', e.target.value)} /></FilterField>
        <FilterField label="Xác thực email"><Select placeholder="Tất cả" options={VERIFIED_OPTIONS} value={params.filters.emailVerified} onChange={(e) => list.setFilter('emailVerified', e.target.value)} /></FilterField>
        <DateRangeFilter label="Ngày đăng ký" from={params.filters.from} to={params.filters.to} onChange={(f, t) => { list.setFilter('from', f); list.setFilter('to', t); }} />
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(u) => u.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Không có người dùng phù hợp"
        emptyDescription="Thử đổi hoặc xoá bớt bộ lọc."
        sort={{ sort: params.sort, order: params.order }}
        onSort={list.setSort}
        onRowClick={(u) => nav(`/users/${u.id}`)}
        actions={(u) => (
          <>
            <IconButton icon="Eye" label="Xem chi tiết" onClick={() => nav(`/users/${u.id}`)} />
            {u.role === 'USER' && <IconButton icon={u.isActive ? 'Lock' : 'Unlock'} label={u.isActive ? 'Khoá tài khoản' : 'Mở khoá tài khoản'} onClick={() => setLockTarget(u)} />}
          </>
        )}
        pagination={q.data ? { page: q.data.pagination.page, limit: q.data.pagination.limit, total: q.data.pagination.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {lockTarget && (
        <ReasonPasswordDialog
          title={lockTarget.isActive ? 'Khoá tài khoản' : 'Mở khoá tài khoản'}
          subject={<span><b>{lockTarget.email ?? lockTarget.username}</b></span>}
          description={lockTarget.isActive ? 'Người dùng sẽ bị đăng xuất khỏi mọi thiết bị và không đăng nhập được cho tới khi mở khoá.' : 'Người dùng sẽ đăng nhập lại được bình thường.'}
          confirmLabel={lockTarget.isActive ? 'Khoá tài khoản' : 'Mở khoá'}
          danger={lockTarget.isActive}
          onConfirm={async (v) => { await lockMutation.mutateAsync({ user: lockTarget, ...v }); }}
          onClose={() => setLockTarget(null)}
        />
      )}
    </>
  );
}
