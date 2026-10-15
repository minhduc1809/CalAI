import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { Alert } from '../../../components/feedback/States';
import { Checkbox, FormField, NumberInput, Select, Textarea, TextInput } from '../../../components/forms/controls';
import { FormActions, FormErrorSummary } from '../../../components/forms/FormParts';
import { Button, IconButton } from '../../../components/ui/Button';
import { Overlay } from '../../../components/ui/Overlay';
import { useToast } from '../../../components/ui/Toast';
import { useForm } from '../../../hooks/useForm';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDateTime, fmtNumber } from '../../../lib/format';
import { SafetyEditor } from '../../food-catalog/components/SafetyEditor';
import { emptyTags, SafetyTags } from '../../food-catalog/types/food.types';
import { Submission, submissionApi } from '../api/submission.api';

const STATUS: Record<Submission['status'], { label: string; tone: 'warning' | 'success' | 'danger' | 'neutral' }> = {
  SUBMITTED: { label: 'Chờ duyệt', tone: 'warning' }, APPROVED: { label: 'Đã duyệt', tone: 'success' }, REJECTED: { label: 'Đã từ chối', tone: 'danger' }, CANCELED: { label: 'Người dùng huỷ', tone: 'neutral' },
};
const REJECT_PRESETS = ['Trùng với món đã có trong kho', 'Thiếu thông tin để xác minh số liệu', 'Số liệu dinh dưỡng không đúng hoặc không hợp lý', 'Không phải món ăn / nội dung không phù hợp'];

export default function FoodSubmissionsPage() {
  const toast = useToast();
  const qc = useQueryClient();
  const list = useListParams(['status', 'duplicate'], { status: 'SUBMITTED' });
  const { params } = list;
  const [approving, setApproving] = useState<Submission | null>(null);
  const [rejecting, setRejecting] = useState<Submission | null>(null);

  const apiQuery = { page: params.page, limit: params.limit, status: params.filters.status, duplicate: params.filters.duplicate === 'true' ? 'true' : undefined, query: params.q || undefined, order: params.order ?? 'asc' };
  const q = useQuery({ queryKey: ['submissions', apiQuery], queryFn: ({ signal }) => submissionApi.list(apiQuery, signal), placeholderData: (p) => p });
  const refresh = () => { qc.invalidateQueries({ queryKey: ['submissions'] }); qc.invalidateQueries({ queryKey: ['pending-work'] }); qc.invalidateQueries({ queryKey: ['foods'] }); qc.invalidateQueries({ queryKey: ['food-progress'] }); };

  const columns: Column<Submission>[] = [
    { key: 'name', header: 'Món', cell: (s) => (<><b>{s.name}</b>{s.possibleDuplicate && <> <StatusBadge tone="warning">Nghi trùng: {s.duplicateOfName}</StatusBadge></>}<div className="sub">{s.servingSize}{s.note ? ` · ${s.note}` : ''}</div>{s.rejectReason && <div className="sub">Lý do từ chối: {s.rejectReason}</div>}</>) },
    { key: 'kcal', header: 'kcal', align: 'right', cell: (s) => fmtNumber(s.calories) },
    { key: 'macro', header: 'Đạm / Tinh bột / Béo', cell: (s) => `${fmtNumber(s.protein, 1)} / ${fmtNumber(s.carb, 1)} / ${fmtNumber(s.fat, 1)}` },
    { key: 'by', header: 'Người gửi', cell: (s) => s.user.email ?? s.user.username },
    { key: 'at', header: 'Gửi lúc', sortKey: 'createdAt', cell: (s) => <span className="nowrap">{fmtDateTime(s.createdAt)}</span> },
    { key: 'status', header: 'Trạng thái', cell: (s) => <StatusBadge tone={STATUS[s.status].tone}>{STATUS[s.status].label}</StatusBadge> },
  ];

  return (
    <>
      <PageHeader
        title="Món người dùng đề xuất"
        breadcrumbs={[{ label: 'Danh mục' }, { label: 'Món người dùng đề xuất' }]}
        total={q.data?.total}
      />
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Tìm theo tên món" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Trạng thái"><Select options={Object.entries(STATUS).map(([value, v]) => ({ value, label: v.label }))} value={params.filters.status} onChange={(e) => list.setFilter('status', e.target.value)} /></FilterField>
        <div className="field" style={{ justifyContent: 'flex-end' }}><Checkbox label="Chỉ món nghi trùng" checked={params.filters.duplicate === 'true'} onChange={(e) => list.setFilter('duplicate', e.target.checked ? 'true' : '')} /></div>
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(s) => s.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Không có món nào"
        emptyDescription="Hàng đợi trống hoặc bộ lọc không khớp."
        sort={{ sort: 'createdAt', order: (params.order ?? 'asc') }}
        onSort={(s) => list.setSort({ sort: 'createdAt', order: s.order === 'desc' ? 'desc' : 'asc' })}
        actions={(s) => s.status === 'SUBMITTED' && (
          <>
            <Button size="sm" variant="primary" onClick={() => setApproving(s)}>Duyệt</Button>{' '}
            <IconButton icon="XCircle" label="Từ chối" onClick={() => setRejecting(s)} />
          </>
        )}
        pagination={q.data ? { page: q.data.page, limit: q.data.limit, total: q.data.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {approving && <ApproveDialog sub={approving} onClose={() => setApproving(null)} onDone={(warn) => { setApproving(null); refresh(); toast.success('Đã duyệt và thêm vào kho', warn); }} />}
      {rejecting && <RejectDialog sub={rejecting} onClose={() => setRejecting(null)} onDone={() => { setRejecting(null); refresh(); toast.success('Đã từ chối', 'Người gửi sẽ nhận thông báo kèm lý do.'); }} />}
    </>
  );
}

interface ApproveValues extends SafetyTags, Record<string, unknown> { name: string; category: string; calories: string; protein: string; carb: string; fat: string }

function ApproveDialog({ sub, onClose, onDone }: { sub: Submission; onClose: () => void; onDone: (warnings?: string) => void }) {
  let warn: string | undefined;
  const form = useForm<ApproveValues>({
    initial: { ...emptyTags(), name: sub.name, category: '', calories: String(sub.calories), protein: String(sub.protein), carb: String(sub.carb), fat: String(sub.fat) },
    validate: (v) => {
      const num = (s: string, max: number) => { const n = Number(s); return s === '' || !Number.isFinite(n) || n < 0 || n > max ? `Giá trị từ 0 đến ${max}` : undefined; };
      return { name: v.name.trim() ? undefined : 'Nhập tên món', category: v.category.trim() ? undefined : 'Bắt buộc chọn nhóm món', calories: num(v.calories, 5000), protein: num(v.protein, 500), carb: num(v.carb, 500), fat: num(v.fat, 500) };
    },
    onSubmit: async (v) => {
      const r = await submissionApi.approve(sub.id, {
        category: v.category.trim(), name: v.name.trim(), calories: Number(v.calories), protein: Number(v.protein), carb: Number(v.carb), fat: Number(v.fat),
        allergens: v.allergens, containsMeat: v.containsMeat, containsFish: v.containsFish, containsEgg: v.containsEgg, containsDairy: v.containsDairy, containsPork: v.containsPork,
      });
      warn = r.warnings?.join(' ') || undefined;
    },
    onSuccess: () => onDone(warn),
  });
  const v = form.values;
  const d = sub.duplicateOf;
  return (
    <Overlay title={`Duyệt: ${sub.name}`} size="lg" onClose={onClose} busy={form.submitting} dirty={form.dirty}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        {sub.possibleDuplicate && (
          <Alert kind="warning">
            Tên giống món <b>{sub.duplicateOfName}</b> trong kho.
            {d ? <> So sánh: <b>{sub.name}</b> {fmtNumber(sub.calories)} kcal ({sub.servingSize}) với <b>{d.name}</b> {fmtNumber(d.calories)} kcal ({d.servingSize}, nhóm {d.category}{d.isActive ? '' : ', đang ẩn'}).</> : ' Không còn tìm thấy món đó trong kho.'}
          </Alert>
        )}
        <p className="muted">Người dùng không khai báo thẻ an toàn, bạn phải xác nhận dị ứng và chế độ ăn trước khi món vào kho.</p>
        <div className="form-grid">
          <FormField label="Tên món" required htmlFor="a-name" error={form.errors.name}><TextInput id="a-name" invalid={!!form.errors.name} value={v.name} onChange={(e) => form.setValue('name', e.target.value)} /></FormField>
          <FormField label="Nhóm món" required htmlFor="a-cat" error={form.errors.category}><TextInput id="a-cat" invalid={!!form.errors.category} value={v.category} onChange={(e) => form.setValue('category', e.target.value)} /></FormField>
          <FormField label="Calo (kcal)" required htmlFor="a-cal" error={form.errors.calories}><NumberInput id="a-cal" invalid={!!form.errors.calories} value={v.calories} onChange={(e) => form.setValue('calories', e.target.value)} /></FormField>
          <FormField label="Đạm (g)" required htmlFor="a-pro" error={form.errors.protein}><NumberInput id="a-pro" invalid={!!form.errors.protein} value={v.protein} onChange={(e) => form.setValue('protein', e.target.value)} /></FormField>
          <FormField label="Tinh bột (g)" required htmlFor="a-carb" error={form.errors.carb}><NumberInput id="a-carb" invalid={!!form.errors.carb} value={v.carb} onChange={(e) => form.setValue('carb', e.target.value)} /></FormField>
          <FormField label="Béo (g)" required htmlFor="a-fat" error={form.errors.fat}><NumberInput id="a-fat" invalid={!!form.errors.fat} value={v.fat} onChange={(e) => form.setValue('fat', e.target.value)} /></FormField>
        </div>
        <SafetyEditor value={v} onChange={(t) => (Object.keys(t) as (keyof SafetyTags)[]).forEach((k) => form.setValue(k, t[k] as never))} />
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel="Duyệt và thêm vào kho" />
      </form>
    </Overlay>
  );
}

function RejectDialog({ sub, onClose, onDone }: { sub: Submission; onClose: () => void; onDone: () => void }) {
  const form = useForm<{ reason: string } & Record<string, unknown>>({
    initial: { reason: '' },
    validate: (v) => ({ reason: v.reason.trim().length < 5 ? 'Ghi lý do từ chối (ít nhất 5 ký tự)' : undefined }),
    onSubmit: (v) => submissionApi.reject(sub.id, v.reason.trim()) as Promise<void>,
    onSuccess: onDone,
  });
  return (
    <Overlay title={`Từ chối: ${sub.name}`} onClose={onClose} busy={form.submitting} dirty={form.dirty}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        <div className="row" style={{ marginBottom: 10 }}>{REJECT_PRESETS.map((p) => <Button key={p} size="sm" variant="secondary" onClick={() => form.setValue('reason', p)}>{p}</Button>)}</div>
        <FormField label="Lý do (người dùng sẽ thấy)" required htmlFor="r-reason" error={form.errors.reason}>
          <Textarea id="r-reason" rows={3} invalid={!!form.errors.reason} value={form.values.reason} onChange={(e) => form.setValue('reason', e.target.value)} />
        </FormField>
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel="Từ chối" submitVariant="danger" />
      </form>
    </Overlay>
  );
}
