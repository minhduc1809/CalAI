import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Column, DataTable } from '../../../components/data-display/DataTable';
import { FilterBar, FilterField } from '../../../components/data-display/FilterBar';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { Alert, ErrorState, LoadingState } from '../../../components/feedback/States';
import { FormField, NumberInput, PasswordInput, Select, Textarea } from '../../../components/forms/controls';
import { FormActions, FormErrorSummary } from '../../../components/forms/FormParts';
import { IconButton } from '../../../components/ui/Button';
import { Overlay } from '../../../components/ui/Overlay';
import { useToast } from '../../../components/ui/Toast';
import { MIN_REASON_LENGTH } from '../../../constants';
import { useForm } from '../../../hooks/useForm';
import { useListParams } from '../../../hooks/useListParams';
import { fmtDateTime } from '../../../lib/format';
import { ConfigItem, configApi, HistoryRow } from '../api/config.api';

type Mode = { kind: 'set' | 'reset'; item: ConfigItem } | { kind: 'restore'; row: HistoryRow; item: ConfigItem };

const show = (v: unknown) => (v === null || v === undefined ? 'mặc định' : String(v));

export default function SystemSettingsPage() {
  const list = useListParams(['group']);
  const { params } = list;
  const toast = useToast();
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ['config'], queryFn: ({ signal }) => configApi.list(signal) });
  const [mode, setMode] = useState<Mode | null>(null);
  const [historyKey, setHistoryKey] = useState<string | null>(null);

  const groups = useMemo(() => [...new Set((q.data?.items ?? []).map((i) => i.group))].map((g) => ({ value: g, label: g })), [q.data]);
  const rows = useMemo(() => {
    const term = params.q.toLowerCase();
    return (q.data?.items ?? []).filter((i) => (!params.filters.group || i.group === params.filters.group) && (!term || i.key.toLowerCase().includes(term) || i.description.toLowerCase().includes(term)));
  }, [q.data, params.q, params.filters.group]);

  const done = (msg: string) => { qc.invalidateQueries({ queryKey: ['config'] }); qc.invalidateQueries({ queryKey: ['config-history'] }); toast.success(msg); };

  const columns: Column<ConfigItem>[] = [
    { key: 'key', header: 'Tham số', cell: (i) => (<><b>{i.key}</b> {i.isSafetyCritical && <StatusBadge tone="danger">An toàn</StatusBadge>}<div className="sub">{i.description}</div></>) },
    { key: 'group', header: 'Nhóm', cell: (i) => i.group },
    { key: 'value', header: 'Hiện tại', cell: (i) => <StatusBadge tone={i.isOverridden ? 'warning' : 'neutral'}>{String(i.value)} {i.unit ?? ''}</StatusBadge> },
    { key: 'default', header: 'Mặc định', cell: (i) => `${String(i.default)} ${i.unit ?? ''}` },
    { key: 'limit', header: 'Giới hạn', cell: (i) => (i.type === 'bool' ? 'Bật/tắt' : `${i.min ?? '—'} → ${i.max ?? '—'}`) },
    { key: 'updated', header: 'Sửa lúc', cell: (i) => (i.isOverridden ? <span className="nowrap">{fmtDateTime(i.updatedAt)}</span> : '—') },
  ];

  return (
    <>
      <PageHeader
        title="Tham số hệ thống"
        description={`Sửa không hồi tố: chỉ áp cho lần tính tiếp theo, không đổi mục tiêu của người dùng hiện có. Phiên bản cấu hình: ${q.data?.configVersion ?? '—'}.`}
        breadcrumbs={[{ label: 'Cấu hình' }, { label: 'Tham số hệ thống' }]}
        total={q.data?.items.length}
      />
      <FilterBar query={params.q} onSearch={list.setQuery} searchPlaceholder="Tìm theo tên hoặc mô tả tham số" activeCount={list.activeFilterCount} onClear={list.clear}>
        <FilterField label="Nhóm"><Select placeholder="Tất cả nhóm" options={groups} value={params.filters.group} onChange={(e) => list.setFilter('group', e.target.value)} /></FilterField>
      </FilterBar>
      <DataTable
        columns={columns}
        rows={rows}
        rowKey={(i) => i.key}
        loading={q.isLoading}
        error={q.isError ? (q.error as Error).message : null}
        onRetry={() => q.refetch()}
        emptyTitle="Không có tham số phù hợp"
        actions={(i) => (
          <>
            <IconButton icon="Edit" label="Sửa tham số" onClick={() => setMode({ kind: 'set', item: i })} />
            {i.isOverridden && <IconButton icon="Reset" label="Về giá trị mặc định" onClick={() => setMode({ kind: 'reset', item: i })} />}
            <IconButton icon="AuditLog" label="Lịch sử thay đổi" onClick={() => setHistoryKey(i.key)} />
          </>
        )}
      />
      {mode && (
        <ChangeDialog
          mode={mode}
          onClose={() => setMode(null)}
          onDone={(m) => { done(m); setMode(null); }}
        />
      )}
      {historyKey && q.data && (
        <HistoryDialog
          item={q.data.items.find((i) => i.key === historyKey)!}
          onClose={() => setHistoryKey(null)}
          onRestore={(row, item) => { setHistoryKey(null); setMode({ kind: 'restore', row, item }); }}
        />
      )}
    </>
  );
}

interface FormValues extends Record<string, unknown> { value: string; reason: string; password: string }

function ChangeDialog({ mode, onClose, onDone }: { mode: Mode; onClose: () => void; onDone: (msg: string) => void }) {
  const item = mode.item;
  const form = useForm<FormValues>({
    initial: { value: String(item.value), reason: '', password: '' },
    validate: (v) => {
      let value: string | undefined;
      if (mode.kind === 'set' && item.type !== 'bool') {
        const n = Number(v.value);
        if (v.value === '' || !Number.isFinite(n)) value = 'Nhập một số hợp lệ';
        else if (item.type === 'int' && !Number.isInteger(n)) value = 'Phải là số nguyên';
        else if (item.min !== null && n < item.min) value = `Không nhỏ hơn ${item.min}`;
        else if (item.max !== null && n > item.max) value = `Không lớn hơn ${item.max}`;
      }
      return {
        value,
        reason: v.reason.trim().length < MIN_REASON_LENGTH ? `Lý do cần ít nhất ${MIN_REASON_LENGTH} ký tự` : undefined,
        password: item.isSafetyCritical && !v.password ? 'Tham số an toàn: nhập lại mật khẩu để xác nhận' : undefined,
      };
    },
    onSubmit: async (v) => {
      const auth = { reason: v.reason.trim(), password: v.password || undefined };
      if (mode.kind === 'set') await configApi.set(item.key, item.type === 'bool' ? v.value === 'true' : Number(v.value), auth);
      else if (mode.kind === 'reset') await configApi.reset(item.key, auth);
      else if (mode.kind === 'restore') await configApi.restore(mode.row.id, auth);
    },
    onSuccess: () => onDone(mode.kind === 'set' ? `Đã cập nhật ${item.key}` : mode.kind === 'reset' ? `Đã đặt ${item.key} về mặc định` : `Đã khôi phục ${item.key}`),
  });
  const title = mode.kind === 'set' ? `Sửa ${item.key}` : mode.kind === 'reset' ? `Về mặc định: ${item.key}` : `Khôi phục: ${item.key}`;

  return (
    <Overlay title={title} onClose={onClose} busy={form.submitting}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        <p className="muted" style={{ marginTop: 0 }}>{item.description}. Giới hạn: {item.type === 'bool' ? 'bật/tắt' : `${item.min ?? '—'} đến ${item.max ?? '—'}`} {item.unit ?? ''}.</p>
        <Alert kind={item.isSafetyCritical ? 'warning' : 'info'}>
          {item.isSafetyCritical ? 'Đây là tham số an toàn dinh dưỡng. ' : ''}Thay đổi chỉ áp cho lần tính tiếp theo (Check-in mới, mục tiêu mới, bản ghi mới); không tính lại mục tiêu của người dùng hiện có.
        </Alert>
        {mode.kind === 'set' && (
          <FormField label="Giá trị mới" required htmlFor="cfg-value" error={form.errors.value} help={`Hiện tại: ${String(item.value)} ${item.unit ?? ''} · mặc định: ${String(item.default)}`}>
            {item.type === 'bool'
              ? <Select id="cfg-value" options={[{ value: 'true', label: 'Bật' }, { value: 'false', label: 'Tắt' }]} value={form.values.value} onChange={(e) => form.setValue('value', e.target.value)} />
              : <NumberInput id="cfg-value" invalid={!!form.errors.value} step={item.type === 'int' ? 1 : 'any'} value={form.values.value} onChange={(e) => form.setValue('value', e.target.value)} />}
          </FormField>
        )}
        {mode.kind === 'restore' && <p>Khôi phục về <b>{show(mode.row.oldValue)}</b> (giá trị trước thay đổi ngày {fmtDateTime(mode.row.createdAt)}). Đây là một lần sửa mới và được ghi lịch sử.</p>}
        <FormField label="Lý do" required htmlFor="cfg-reason" error={form.errors.reason}>
          <Textarea id="cfg-reason" invalid={!!form.errors.reason} value={form.values.reason} onChange={(e) => form.setValue('reason', e.target.value)} />
        </FormField>
        {item.isSafetyCritical && (
          <FormField label="Nhập lại mật khẩu của bạn" required htmlFor="cfg-pass" error={form.errors.password}>
            <PasswordInput id="cfg-pass" invalid={!!form.errors.password} value={form.values.password} onChange={(e) => form.setValue('password', e.target.value)} />
          </FormField>
        )}
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel="Xác nhận" />
      </form>
    </Overlay>
  );
}

function HistoryDialog({ item, onClose, onRestore }: { item: ConfigItem; onClose: () => void; onRestore: (row: HistoryRow, item: ConfigItem) => void }) {
  const h = useQuery({ queryKey: ['config-history', item.key], queryFn: () => configApi.history(item.key) });
  return (
    <Overlay title={`Lịch sử: ${item.key}`} size="lg" onClose={onClose}>
      {h.isLoading ? <LoadingState lines={4} /> : h.isError ? <ErrorState message={(h.error as Error).message} onRetry={() => h.refetch()} /> : h.data!.items.length === 0 ? <p className="muted">Chưa có thay đổi nào.</p> : (
        <div className="table-wrap"><table className="table">
          <thead><tr><th>Phiên bản</th><th>Thay đổi</th><th>Người sửa</th><th>Lý do</th><th /></tr></thead>
          <tbody>{h.data!.items.map((r) => (
            <tr key={r.id}>
              <td>v{r.configVersion}<div className="sub">{fmtDateTime(r.createdAt)}</div></td>
              <td><StatusBadge tone="primary">{r.action}</StatusBadge> {show(r.oldValue)} → {show(r.newValue)}</td>
              <td>{r.adminEmail ?? '—'}</td>
              <td>{r.reason}</td>
              <td className="actions"><button className="btn btn-secondary btn-sm" onClick={() => onRestore(r, item)}>Khôi phục</button></td>
            </tr>
          ))}</tbody>
        </table></div>
      )}
    </Overlay>
  );
}
