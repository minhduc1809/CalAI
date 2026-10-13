import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api, ApiError, Query } from '../../services/api-client';
import { Alert, ErrorState, LoadingState } from '../feedback/States';
import { Button, IconButton } from '../ui/Button';
import { Overlay } from '../ui/Overlay';
import { useToast } from '../ui/Toast';
import { Checkbox } from '../forms/controls';

interface Field { key: string; label: string }
interface Preview { headers: string[]; rows: string[][]; note?: string }

interface Props {
  /** Loại dữ liệu: users, orders, revenue, foods, exercises, audit, data-requests. */
  kind: string;
  /** Bộ lọc đang áp dụng; xuất đúng tập kết quả người dùng đang xem. */
  query?: Query;
  title?: string;
}

/**
 * Hộp thoại xuất dữ liệu dùng chung: chọn trường được phép và thứ tự cột, xem trước vài dòng, tải CSV. Máy chủ chỉ cho xuất các
 * trường trong danh sách cho phép (không token, mật khẩu, dữ liệu sức khỏe), chống formula injection và ghi nhật ký.
 */
export function ExportButton({ kind, query, title = 'Xuất dữ liệu' }: Props) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <Button icon="Download" onClick={() => setOpen(true)}>Xuất CSV</Button>
      {open && <ExportDialog kind={kind} query={query} title={title} onClose={() => setOpen(false)} />}
    </>
  );
}

function ExportDialog({ kind, query, title, onClose }: Props & { title: string; onClose: () => void }) {
  const toast = useToast();
  const fields = useQuery({ queryKey: ['export-fields', kind], queryFn: () => api.get<Field[]>(`/admin/exports/${kind}/fields`) });
  const [selected, setSelected] = useState<string[]>([]);
  const [busy, setBusy] = useState(false);
  const [preview, setPreview] = useState<Preview | null>(null);
  const [previewBusy, setPreviewBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const defaults = useMemo(() => (fields.data ?? []).map((f) => f.key), [fields.data]);
  useEffect(() => setSelected(defaults), [defaults]);
  const labelOf = (k: string) => fields.data?.find((f) => f.key === k)?.label ?? k;
  const available = (fields.data ?? []).filter((f) => !selected.includes(f.key));
  const fixed = kind === 'revenue';
  const params = { ...query, ...(fixed ? {} : { fields: selected.join(',') }) };

  const move = (key: string, dir: -1 | 1) =>
    setSelected((cur) => {
      const i = cur.indexOf(key);
      const j = i + dir;
      if (i < 0 || j < 0 || j >= cur.length) return cur;
      const next = [...cur];
      [next[i], next[j]] = [next[j], next[i]];
      return next;
    });

  const loadPreview = async () => {
    setPreviewBusy(true);
    setError(null);
    try {
      setPreview(await api.get<Preview>(`/admin/exports/${kind}/preview`, params));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : (e as Error).message);
    } finally {
      setPreviewBusy(false);
    }
  };

  const download = async () => {
    setBusy(true);
    setError(null);
    try {
      const blob = await api.blob(`/admin/exports/${kind}`, params);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${kind}-${new Date().toISOString().slice(0, 16).replace(/[-:T]/g, '')}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('Đã xuất dữ liệu', 'File CSV đã được tải xuống theo bộ lọc hiện tại.');
      onClose();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : (e as Error).message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Overlay
      title={title}
      size="lg"
      onClose={onClose}
      busy={busy}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Huỷ</Button>
          {!fixed && <Button variant="secondary" loading={previewBusy} disabled={selected.length === 0} onClick={loadPreview}>Xem trước</Button>}
          <Button variant="primary" icon="Download" loading={busy} disabled={!fixed && selected.length === 0} onClick={download}>Tải CSV</Button>
        </>
      }
    >
      <Alert kind="info">Xuất đúng theo bộ lọc đang áp dụng, tối đa 10.000 dòng. Không bao gồm mật khẩu, token hay dữ liệu sức khỏe cá nhân. Lần xuất được ghi vào nhật ký quản trị.</Alert>
      {error && <div className="form-error-summary" role="alert" style={{ marginTop: 12 }}>{error}</div>}
      {fields.isLoading ? <LoadingState lines={4} /> : fields.isError ? <ErrorState message={(fields.error as Error).message} onRetry={() => fields.refetch()} /> : fixed ? null : (
        <div className="grid cols-2" style={{ marginTop: 14 }}>
          <div>
            <b>Trường khả dụng</b>
            <div className="stack" style={{ gap: 6, marginTop: 8 }}>
              {available.length === 0 && <span className="muted">Đã chọn tất cả</span>}
              {available.map((f) => <Checkbox key={f.key} label={f.label} checked={false} onChange={() => setSelected((c) => [...c, f.key])} />)}
            </div>
          </div>
          <div>
            <div className="row between"><b>Trường sẽ xuất ({selected.length})</b><Button size="sm" variant="ghost" onClick={() => setSelected(defaults)}>Đặt lại mặc định</Button></div>
            <div className="stack" style={{ gap: 4, marginTop: 8 }}>
              {selected.map((k, i) => (
                <div key={k} className="row between" style={{ gap: 4 }}>
                  <span>{labelOf(k)}</span>
                  <span className="row" style={{ gap: 0 }}>
                    <IconButton icon="ArrowUp" label="Chuyển lên" disabled={i === 0} onClick={() => move(k, -1)} />
                    <IconButton icon="ArrowDown" label="Chuyển xuống" disabled={i === selected.length - 1} onClick={() => move(k, 1)} />
                    <IconButton icon="X" label="Bỏ trường" onClick={() => setSelected((c) => c.filter((x) => x !== k))} />
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
      {preview && preview.rows.length > 0 && (
        <div className="table-wrap" style={{ marginTop: 14 }}>
          <table className="table">
            <thead><tr>{preview.headers.map((h) => <th key={h}>{h}</th>)}</tr></thead>
            <tbody>{preview.rows.map((r, i) => <tr key={i}>{r.map((c, j) => <td key={j}>{c}</td>)}</tr>)}</tbody>
          </table>
        </div>
      )}
      {preview && preview.rows.length === 0 && <p className="muted">Không có dòng nào khớp bộ lọc hiện tại.</p>}
    </Overlay>
  );
}
