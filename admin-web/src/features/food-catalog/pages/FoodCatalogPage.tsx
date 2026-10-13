import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { ConfirmDialog } from '../../../components/feedback/ConfirmDialogs';
import { Alert, ErrorState, LoadingState } from '../../../components/feedback/States';
import { Checkbox, Select } from '../../../components/forms/controls';
import { ExportButton } from '../../../components/import-export/ExportButton';
import { ImportWizard } from '../../../components/import-export/ImportWizard';
import { Button, IconButton } from '../../../components/ui/Button';
import { Overlay } from '../../../components/ui/Overlay';
import { useToast } from '../../../components/ui/Toast';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDateTime, fmtNumber } from '../../../lib/format';
import { foodApi } from '../api/food.api';
import { FoodForm } from '../components/FoodForm';
import type { Food } from '../types/food.types';

const STATUS = [{ value: 'active', label: 'Đang hiển thị' }, { value: 'inactive', label: 'Đã ẩn' }, { value: 'all', label: 'Tất cả' }];
const SOURCE = [{ value: 'OFFICIAL', label: 'Chính thức' }, { value: 'COMMUNITY', label: 'Cộng đồng' }];

const TEMPLATE =
  'name,category,servingSize,calories,protein,carb,fat,allergens,containsMeat,containsFish,containsEgg,containsDairy,containsPork,dataSource,aliases,note\n' +
  'Bánh bông lan,Bánh,1 cái (80g),240,5,34,9,GLUTEN;EGG,false,false,true,false,false,Nhãn sản phẩm,bánh bông lan trứng,\n';

export default function FoodCatalogPage() {
  const toast = useToast();
  const qc = useQueryClient();
  const list = useListParams(['category', 'source', 'status', 'missingTags', 'needsReview'], { status: 'active' });
  const { params } = list;
  const [form, setForm] = useState<{ mode: 'create' | 'edit'; food?: Food } | null>(null);
  const [importing, setImporting] = useState(false);
  const [toggling, setToggling] = useState<Food | null>(null);
  const [history, setHistory] = useState<Food | null>(null);

  const apiQuery = {
    page: params.page, limit: params.limit, query: params.q || undefined, sort: params.sort, order: params.order,
    category: params.filters.category, source: params.filters.source, status: params.filters.status,
    missingTags: params.filters.missingTags === 'true' ? 'true' : undefined, needsReview: params.filters.needsReview === 'true' ? 'true' : undefined,
  };
  const q = useQuery({ queryKey: ['foods', apiQuery], queryFn: ({ signal }) => foodApi.list(apiQuery, signal), placeholderData: (p) => p });
  const cats = useQuery({ queryKey: ['food-categories'], queryFn: () => foodApi.categories() });
  const progress = useQuery({ queryKey: ['food-progress'], queryFn: () => foodApi.progress() });

  const refresh = () => {
    qc.invalidateQueries({ queryKey: ['foods'] });
    qc.invalidateQueries({ queryKey: ['food-categories'] });
    qc.invalidateQueries({ queryKey: ['food-progress'] });
  };
  const toggle = useMutation({
    mutationFn: (f: Food) => (f.isActive ? foodApi.hide(f.id) : foodApi.show(f.id)),
    onSuccess: (_d, f) => { refresh(); toast.success(f.isActive ? 'Đã ẩn món' : 'Đã hiện lại món', f.name); },
  });

  const columns: Column<Food>[] = [
    {
      key: 'name', header: 'Món', sortKey: 'name',
      cell: (f) => (<><b>{f.name}</b>{!f.safetyTagged && <> <StatusBadge tone="danger">Thiếu thẻ an toàn</StatusBadge></>}{f.needsReview && <> <StatusBadge tone="warning">Cần rà soát</StatusBadge></>}<div className="sub">{f.servingSize}</div></>),
    },
    { key: 'category', header: 'Nhóm', sortKey: 'category', cell: (f) => f.category },
    { key: 'kcal', header: 'kcal', sortKey: 'calories', align: 'right', cell: (f) => fmtNumber(f.calories) },
    { key: 'macro', header: 'Đạm / Tinh bột / Béo', cell: (f) => `${fmtNumber(f.protein, 1)} / ${fmtNumber(f.carb, 1)} / ${fmtNumber(f.fat, 1)}` },
    { key: 'source', header: 'Nguồn', cell: (f) => <StatusBadge tone={f.source === 'COMMUNITY' ? 'info' : 'neutral'}>{f.source === 'COMMUNITY' ? 'Cộng đồng' : 'Chính thức'}</StatusBadge> },
    { key: 'active', header: 'Hiển thị', cell: (f) => <StatusBadge tone={f.isActive ? 'success' : 'neutral'}>{f.isActive ? 'Đang hiển thị' : 'Đã ẩn'}</StatusBadge> },
  ];

  const exportQuery = { query: apiQuery.query, category: apiQuery.category, source: apiQuery.source, status: apiQuery.status, missingTags: apiQuery.missingTags, needsReview: apiQuery.needsReview, sort: apiQuery.sort, order: apiQuery.order };
  const p = progress.data;

  return (
    <>
      <PageHeader
        title="Kho món"
        description="Thêm, sửa, ẩn món. Mỗi món bắt buộc khai báo đủ thẻ an toàn và nguồn số liệu; món thiếu thẻ bị loại khỏi mọi gợi ý. Ẩn thay cho xoá để giữ lịch sử."
        breadcrumbs={[{ label: 'Nội dung' }, { label: 'Kho món' }]}
        total={q.data?.total}
        actions={(
          <>
            <ExportButton kind="foods" query={exportQuery} title="Xuất kho món" />
            <Button icon="Upload" onClick={() => setImporting(true)}>Nhập CSV</Button>
            <Button variant="primary" icon="Plus" onClick={() => setForm({ mode: 'create' })}>Thêm món</Button>
          </>
        )}
      />
      {p && (
        <div style={{ marginBottom: 12 }}>
          <Alert kind={p.active >= p.targets.release ? 'success' : 'info'}>
            Kho hiện có <b>{fmtNumber(p.active)}</b> món đang hiển thị ({fmtNumber(p.tagged)} đã gắn thẻ an toàn, {fmtNumber(p.needsReview)} cần rà soát). Mục tiêu: {fmtNumber(p.targets.demo)} món cho bản demo, {fmtNumber(p.targets.release)} món cho bản phát hành.
          </Alert>
        </div>
      )}
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Tìm món (không cần gõ dấu)" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Nhóm món"><Select placeholder="Tất cả" options={(cats.data ?? []).map((c) => ({ value: c.category, label: `${c.category} (${c.count})` }))} value={params.filters.category} onChange={(e) => list.setFilter('category', e.target.value)} /></FilterField>
        <FilterField label="Nguồn"><Select placeholder="Tất cả" options={SOURCE} value={params.filters.source} onChange={(e) => list.setFilter('source', e.target.value)} /></FilterField>
        <FilterField label="Hiển thị"><Select options={STATUS} value={params.filters.status} onChange={(e) => list.setFilter('status', e.target.value)} /></FilterField>
        <div className="field" style={{ justifyContent: 'flex-end' }}><Checkbox label="Thiếu thẻ an toàn" checked={params.filters.missingTags === 'true'} onChange={(e) => list.setFilter('missingTags', e.target.checked ? 'true' : '')} /></div>
        <div className="field" style={{ justifyContent: 'flex-end' }}><Checkbox label="Cần rà soát" checked={params.filters.needsReview === 'true'} onChange={(e) => list.setFilter('needsReview', e.target.checked ? 'true' : '')} /></div>
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(f) => f.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Không có món phù hợp"
        emptyDescription="Thử đổi hoặc xoá bớt bộ lọc, hoặc thêm món mới."
        sort={{ sort: params.sort, order: params.order }}
        onSort={list.setSort}
        actions={(f) => (
          <>
            <IconButton icon="Edit" label="Sửa món" onClick={() => setForm({ mode: 'edit', food: f })} />
            <IconButton icon="AuditLog" label="Lịch sử chỉnh sửa" onClick={() => setHistory(f)} />
            <IconButton icon={f.isActive ? 'EyeOff' : 'Eye'} label={f.isActive ? 'Ẩn món' : 'Hiện lại món'} onClick={() => setToggling(f)} />
          </>
        )}
        pagination={q.data ? { page: q.data.page, limit: q.data.limit, total: q.data.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {form && (
        <FoodForm
          mode={form.mode}
          food={form.food}
          categories={(cats.data ?? []).map((c) => c.category)}
          onClose={() => setForm(null)}
          onSaved={(m) => { setForm(null); refresh(); toast.success(m); }}
        />
      )}
      {importing && (
        <ImportWizard
          title="Nhập kho món bằng CSV"
          guide="Cột bắt buộc: name, category, servingSize, calories, protein, carb, fat, allergens (cách nhau bằng ;), containsMeat, containsFish, containsEgg, containsDairy, containsPork, dataSource. Cột tuỳ chọn: aliases, note. Mỗi món phải khai báo đủ thẻ an toàn."
          templateFileName="mau-nhap-kho-mon.csv"
          templateCsv={TEMPLATE}
          maxRows={1000}
          preview={foodApi.importPreview}
          commit={foodApi.importCommit}
          onClose={() => setImporting(false)}
          onImported={refresh}
        />
      )}
      {toggling && (
        <ConfirmDialog
          title={toggling.isActive ? 'Ẩn món khỏi kho?' : 'Hiện lại món?'}
          description={toggling.isActive ? <span>Món <b>{toggling.name}</b> sẽ biến khỏi gợi ý và tra cứu. Bữa ăn đã ghi vẫn giữ nguyên số liệu.</span> : <span>Món <b>{toggling.name}</b> sẽ xuất hiện lại trong gợi ý và tra cứu.</span>}
          confirmLabel={toggling.isActive ? 'Ẩn món' : 'Hiện món'}
          danger={toggling.isActive}
          onConfirm={async () => { await toggle.mutateAsync(toggling); }}
          onClose={() => setToggling(null)}
        />
      )}
      {history && <HistoryDrawer food={history} onClose={() => setHistory(null)} />}
    </>
  );
}

const ACTION: Record<string, string> = { CREATE_FOOD: 'Thêm món', UPDATE_FOOD: 'Sửa món', HIDE_FOOD: 'Ẩn món', APPROVE_FOOD_SUBMISSION: 'Duyệt từ món người dùng gửi', IMPORT_FOODS: 'Nhập CSV' };

function HistoryDrawer({ food, onClose }: { food: Food; onClose: () => void }) {
  const h = useQuery({ queryKey: ['food-history', food.id], queryFn: () => foodApi.history(food.id) });
  return (
    <Overlay title={`Lịch sử: ${food.name}`} variant="drawer" onClose={onClose}>
      {h.isLoading ? <LoadingState lines={4} /> : h.isError ? <ErrorState message={(h.error as Error).message} onRetry={() => h.refetch()} /> : h.data!.items.length === 0 ? <p className="muted">Chưa có chỉnh sửa nào được ghi nhận cho món này.</p> : (
        <ul style={{ paddingLeft: 18 }}>{h.data!.items.map((i) => (<li key={i.id}><b>{ACTION[i.action] ?? i.action}</b> · {i.adminEmail ?? 'Hệ thống'} · {fmtDateTime(i.createdAt)}{i.reason && <div className="muted">{i.reason}</div>}</li>))}</ul>
      )}
    </Overlay>
  );
}
