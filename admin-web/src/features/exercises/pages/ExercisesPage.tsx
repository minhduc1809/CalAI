import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { ConfirmDialog } from '../../../components/feedback/ConfirmDialogs';
import { Alert } from '../../../components/feedback/States';
import { Select, TextInput } from '../../../components/forms/controls';
import { ExportButton } from '../../../components/import-export/ExportButton';
import { ImportWizard } from '../../../components/import-export/ImportWizard';
import { Button, IconButton } from '../../../components/ui/Button';
import { useToast } from '../../../components/ui/Toast';
import { useListParams } from '../../../hooks/useListParams';
import { fmtNumber } from '../../../lib/format';
import { EQUIPMENT, Exercise, exerciseApi, GENDERS, labelOf, LEVELS } from '../api/exercise.api';
import { ExerciseForm } from '../components/ExerciseForm';

const STATUS = [{ value: 'active', label: 'Đang hiển thị' }, { value: 'inactive', label: 'Đã ẩn' }, { value: 'all', label: 'Tất cả' }];

const TEMPLATE =
  'name,targetMuscle,level,equipment,genderTarget,sets,repsOrDuration,restSeconds,caloriesBurnedEstimate,preparation,execution,commonMistakes,breathing,imageUrl\n' +
  'Plank,"Core, Bụng",BEGINNER,NO_EQUIPMENT,ALL,3,30 giây,45,12,Nằm sấp chống hai khuỷu tay,Giữ thân thẳng từ đầu tới gót chân,Võng lưng hoặc nhô mông,Thở đều không nín thở,\n';

export default function ExercisesPage() {
  const toast = useToast();
  const qc = useQueryClient();
  const list = useListParams(['level', 'equipment', 'gender', 'muscle', 'status'], { status: 'active' });
  const { params } = list;
  const [form, setForm] = useState<{ mode: 'create' | 'edit'; exercise?: Exercise } | null>(null);
  const [importing, setImporting] = useState(false);
  const [toggling, setToggling] = useState<Exercise | null>(null);

  const apiQuery = {
    page: params.page, limit: params.limit, query: params.q || undefined, sort: params.sort, order: params.order,
    level: params.filters.level, equipment: params.filters.equipment, gender: params.filters.gender, muscle: params.filters.muscle || undefined, status: params.filters.status,
  };
  const q = useQuery({ queryKey: ['exercises', apiQuery], queryFn: ({ signal }) => exerciseApi.list(apiQuery, signal), placeholderData: (p) => p });
  const refresh = () => qc.invalidateQueries({ queryKey: ['exercises'] });
  const toggle = useMutation({
    mutationFn: (e: Exercise) => (e.isActive ? exerciseApi.hide(e.id) : exerciseApi.show(e.id)),
    onSuccess: (_d, e) => { refresh(); toast.success(e.isActive ? 'Đã ẩn bài tập' : 'Đã hiện lại bài tập', e.name); },
  });

  const columns: Column<Exercise>[] = [
    { key: 'name', header: 'Bài tập', sortKey: 'name', cell: (e) => (<><b>{e.name}</b>{e.source === 'ADMIN' && <> <StatusBadge tone="info">Admin thêm</StatusBadge></>}</>) },
    { key: 'muscle', header: 'Nhóm cơ', sortKey: 'targetMuscle', cell: (e) => e.targetMuscle },
    { key: 'level', header: 'Mức độ', sortKey: 'level', cell: (e) => labelOf(LEVELS, e.level) },
    { key: 'equipment', header: 'Dụng cụ', cell: (e) => labelOf(EQUIPMENT, e.equipment) },
    { key: 'gender', header: 'Dành cho', cell: (e) => labelOf(GENDERS, e.genderTarget) },
    { key: 'sets', header: 'Hiệp × lần', cell: (e) => `${e.data.sets} × ${e.data.repsOrDuration}` },
    { key: 'cal', header: 'Calo/hiệp', align: 'right', cell: (e) => fmtNumber(e.data.caloriesBurnedEstimate) },
    { key: 'active', header: 'Hiển thị', cell: (e) => <StatusBadge tone={e.isActive ? 'success' : 'neutral'}>{e.isActive ? 'Đang hiển thị' : 'Đã ẩn'}</StatusBadge> },
  ];
  const exportQuery = { query: apiQuery.query, level: apiQuery.level, equipment: apiQuery.equipment, gender: apiQuery.gender, muscle: apiQuery.muscle, status: apiQuery.status, sort: apiQuery.sort, order: apiQuery.order };

  return (
    <>
      <PageHeader
        title="Kho bài tập"
        description="Thêm, sửa, ẩn bài tập. Ẩn thay cho xoá để giữ lịch sử buổi tập. Sửa không ảnh hưởng buổi tập đã ghi."
        breadcrumbs={[{ label: 'Nội dung' }, { label: 'Kho bài tập' }]}
        total={q.data?.total}
        actions={(
          <>
            <ExportButton kind="exercises" query={exportQuery} title="Xuất kho bài tập" />
            <Button icon="Upload" onClick={() => setImporting(true)}>Nhập CSV</Button>
            <Button variant="primary" icon="Plus" onClick={() => setForm({ mode: 'create' })}>Thêm bài</Button>
          </>
        )}
      />
      <div style={{ marginBottom: 12 }}><Alert kind="info">Hệ số MET từng loại tập (dùng tính calo) chỉnh ở <Link to="/config?group=T%E1%BA%ADp%20luy%E1%BB%87n%20(MET)">Tham số hệ thống</Link>. Đổi MET chỉ áp cho buổi tập ghi sau đó.</Alert></div>
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Tìm bài (không cần gõ dấu)" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Nhóm cơ"><TextInput value={params.filters.muscle} onChange={(e) => list.setFilter('muscle', e.target.value)} placeholder="Ví dụ: Ngực" style={{ width: 150 }} /></FilterField>
        <FilterField label="Mức độ"><Select placeholder="Tất cả" options={LEVELS} value={params.filters.level} onChange={(e) => list.setFilter('level', e.target.value)} /></FilterField>
        <FilterField label="Dụng cụ"><Select placeholder="Tất cả" options={EQUIPMENT} value={params.filters.equipment} onChange={(e) => list.setFilter('equipment', e.target.value)} /></FilterField>
        <FilterField label="Dành cho"><Select placeholder="Tất cả" options={GENDERS} value={params.filters.gender} onChange={(e) => list.setFilter('gender', e.target.value)} /></FilterField>
        <FilterField label="Hiển thị"><Select options={STATUS} value={params.filters.status} onChange={(e) => list.setFilter('status', e.target.value)} /></FilterField>
      </FilterBar>
      <DataTable
        columns={columns}
        rows={q.data?.items}
        rowKey={(e) => e.id}
        loading={q.isLoading}
        fetching={q.isFetching}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Không có bài tập phù hợp"
        sort={{ sort: params.sort, order: params.order }}
        onSort={list.setSort}
        actions={(e) => (
          <>
            <IconButton icon="Edit" label="Sửa bài tập" onClick={() => setForm({ mode: 'edit', exercise: e })} />
            <IconButton icon={e.isActive ? 'EyeOff' : 'Eye'} label={e.isActive ? 'Ẩn bài tập' : 'Hiện lại bài tập'} onClick={() => setToggling(e)} />
          </>
        )}
        pagination={q.data ? { page: q.data.page, limit: q.data.limit, total: q.data.total, onPage: list.setPage, onLimit: list.setLimit } : undefined}
      />
      {form && <ExerciseForm mode={form.mode} exercise={form.exercise} onClose={() => setForm(null)} onSaved={(m) => { setForm(null); refresh(); toast.success(m); }} />}
      {importing && (
        <ImportWizard
          title="Nhập kho bài tập bằng CSV"
          guide="Cột bắt buộc: name, targetMuscle, level (BEGINNER/INTERMEDIATE/ADVANCED), equipment (NO_EQUIPMENT/DUMBBELL/GYM_MACHINE/RESISTANCE_BAND), genderTarget (MALE/FEMALE/ALL), sets, repsOrDuration, restSeconds, caloriesBurnedEstimate, preparation, execution, commonMistakes, breathing. Tuỳ chọn: imageUrl."
          templateFileName="mau-nhap-kho-bai-tap.csv"
          templateCsv={TEMPLATE}
          maxRows={500}
          preview={exerciseApi.importPreview}
          commit={exerciseApi.importCommit}
          onClose={() => setImporting(false)}
          onImported={refresh}
        />
      )}
      {toggling && (
        <ConfirmDialog
          title={toggling.isActive ? 'Ẩn bài tập?' : 'Hiện lại bài tập?'}
          description={toggling.isActive ? <span>Bài <b>{toggling.name}</b> sẽ biến khỏi kho bài tập của app. Buổi tập đã ghi vẫn giữ nguyên.</span> : <span>Bài <b>{toggling.name}</b> sẽ xuất hiện lại trong app.</span>}
          confirmLabel={toggling.isActive ? 'Ẩn bài' : 'Hiện bài'}
          danger={toggling.isActive}
          onConfirm={async () => { await toggle.mutateAsync(toggling); }}
          onClose={() => setToggling(null)}
        />
      )}
    </>
  );
}
