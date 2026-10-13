import { ChangeEvent, useState } from 'react';
import { Alert } from '../feedback/States';
import { Textarea } from '../forms/controls';
import { Button } from '../ui/Button';
import { Overlay } from '../ui/Overlay';
import { ApiError } from '../../services/api-client';

export interface ImportPreview {
  importId: string;
  total: number;
  valid: number;
  willCreate: number;
  skippedExisting: string[];
  errors: { line: number; message: string }[];
  sample: Record<string, string | number>[];
}

interface Props {
  title: string;
  /** Cột bắt buộc và quy tắc, hiển thị ở bước 1. */
  guide: string;
  templateFileName: string;
  templateCsv: string;
  maxRows: number;
  preview: (csv: string) => Promise<ImportPreview>;
  commit: (importId: string) => Promise<{ created: number }>;
  onClose: () => void;
  /** Gọi sau khi ghi thành công để làm mới danh sách. */
  onImported: () => void;
}

type Step = 'file' | 'preview' | 'result';
const MAX_BYTES = 2 * 1024 * 1024;

/**
 * Nhập dữ liệu từ CSV theo 4 bước: chọn tệp → xem trước và kiểm tra theo từng dòng → xác nhận → kết quả. Không ghi gì cho tới
 * khi xác nhận; dòng lỗi được liệt kê rõ và KHÔNG tự sửa âm thầm. Schema cố định nên bỏ bước ánh xạ cột.
 */
export function ImportWizard({ title, guide, templateFileName, templateCsv, maxRows, preview, commit, onClose, onImported }: Props) {
  const [step, setStep] = useState<Step>('file');
  const [csv, setCsv] = useState('');
  const [fileName, setFileName] = useState('');
  const [data, setData] = useState<ImportPreview | null>(null);
  const [result, setResult] = useState<{ created: number } | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const msg = (e: unknown) => (e instanceof ApiError ? e.message : (e as Error).message);

  const onFile = async (e: ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    setError(null);
    if (!f) return;
    if (!/\.csv$/i.test(f.name) && f.type !== 'text/csv') return setError('Chỉ nhận tệp CSV (.csv).');
    if (f.size > MAX_BYTES) return setError('Tệp quá lớn (tối đa 2 MB).');
    setCsv(await f.text());
    setFileName(f.name);
  };

  const download = () => {
    const url = URL.createObjectURL(new Blob(['﻿' + templateCsv], { type: 'text/csv;charset=utf-8' }));
    const a = document.createElement('a');
    a.href = url;
    a.download = templateFileName;
    a.click();
    URL.revokeObjectURL(url);
  };

  const doPreview = async () => {
    setBusy(true);
    setError(null);
    try {
      setData(await preview(csv));
      setStep('preview');
    } catch (e) {
      setError(msg(e));
    } finally {
      setBusy(false);
    }
  };

  const doCommit = async () => {
    if (!data) return;
    setBusy(true);
    setError(null);
    try {
      setResult(await commit(data.importId));
      setStep('result');
      onImported();
    } catch (e) {
      setError(msg(e));
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
        step === 'file' ? (
          <>
            <Button variant="secondary" onClick={onClose}>Huỷ</Button>
            <Button variant="primary" loading={busy} disabled={!csv.trim()} onClick={doPreview}>Kiểm tra dữ liệu</Button>
          </>
        ) : step === 'preview' ? (
          <>
            <Button variant="secondary" onClick={() => setStep('file')} disabled={busy}>Quay lại</Button>
            <Button variant="primary" loading={busy} disabled={!data || data.willCreate === 0} onClick={doCommit}>
              Xác nhận thêm {data?.willCreate ?? 0} bản ghi
            </Button>
          </>
        ) : (
          <Button variant="primary" onClick={onClose}>Đóng</Button>
        )
      }
    >
      <ol className="row" style={{ listStyle: 'none', padding: 0, margin: '0 0 12px', gap: 16 }} aria-label="Các bước">
        {([['file', '1. Chọn tệp'], ['preview', '2. Kiểm tra và xác nhận'], ['result', '3. Kết quả']] as const).map(([k, l]) => (
          <li key={k} style={{ fontWeight: step === k ? 700 : 400, color: step === k ? 'var(--primary)' : 'var(--text-2)' }}>{l}</li>
        ))}
      </ol>
      {error && <div className="form-error-summary" role="alert">{error}</div>}

      {step === 'file' && (
        <div className="stack">
          <Alert kind="info">{guide} Tối đa {maxRows} dòng và 2 MB mỗi lần. Chưa có gì được ghi cho tới khi bạn xác nhận ở bước sau.</Alert>
          <div className="row">
            <Button icon="Download" onClick={download}>Tải tệp mẫu</Button>
            <input type="file" accept=".csv,text/csv" onChange={onFile} aria-label="Chọn tệp CSV" />
            {fileName && <span className="muted">Đã chọn: {fileName}</span>}
          </div>
          <div>
            <label htmlFor="imp-text" className="muted">Hoặc dán nội dung CSV</label>
            <Textarea id="imp-text" rows={6} value={csv} onChange={(e) => { setCsv(e.target.value); setFileName(''); }} />
          </div>
        </div>
      )}

      {step === 'preview' && data && (
        <div className="stack">
          <div className="grid cols-3">
            <div className="card"><div className="muted">Tổng số dòng</div><div className="stat-value">{data.total}</div></div>
            <div className="card"><div className="muted">Hợp lệ / sẽ thêm</div><div className="stat-value" style={{ color: 'var(--success)' }}>{data.valid} / {data.willCreate}</div></div>
            <div className="card"><div className="muted">Dòng lỗi</div><div className="stat-value" style={{ color: data.errors.length ? 'var(--danger)' : undefined }}>{data.errors.length}</div></div>
          </div>
          {data.skippedExisting.length > 0 && <Alert kind="warning">Bỏ qua {data.skippedExisting.length} bản ghi đã có (trùng tên): {data.skippedExisting.slice(0, 6).join(', ')}{data.skippedExisting.length > 6 ? '…' : ''}</Alert>}
          {data.errors.length > 0 && (
            <div>
              <Alert kind="danger">Các dòng lỗi sẽ bị BỎ QUA, không được tự sửa. Sửa trong tệp rồi chọn lại nếu muốn nhập các dòng này.</Alert>
              <div className="table-wrap" style={{ maxHeight: 220, overflowY: 'auto', marginTop: 8 }}>
                <table className="table"><thead><tr><th>Dòng</th><th>Lỗi</th></tr></thead>
                  <tbody>{data.errors.slice(0, 100).map((e) => (<tr key={e.line}><td>{e.line}</td><td>{e.message}</td></tr>))}</tbody></table>
              </div>
            </div>
          )}
          {data.sample.length > 0 && (
            <div className="table-wrap"><table className="table">
              <thead><tr>{Object.keys(data.sample[0]).map((k) => <th key={k}>{k}</th>)}</tr></thead>
              <tbody>{data.sample.map((r, i) => (<tr key={i}>{Object.values(r).map((v, j) => <td key={j}>{String(v)}</td>)}</tr>))}</tbody>
            </table></div>
          )}
        </div>
      )}

      {step === 'result' && result && (
        <div className="stack">
          <Alert kind="success">Đã thêm <b>{result.created}</b> bản ghi. {data && data.errors.length > 0 ? `${data.errors.length} dòng lỗi đã bị bỏ qua.` : ''} {data && data.skippedExisting.length > 0 ? `${data.skippedExisting.length} bản ghi trùng đã bỏ qua.` : ''}</Alert>
          {data && data.errors.length > 0 && (
            <Button icon="Download" onClick={() => {
              const lines = ['Dòng,Lỗi', ...data.errors.map((e) => `${e.line},"${e.message.replace(/"/g, '""')}"`)];
              const url = URL.createObjectURL(new Blob(['﻿' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8' }));
              const a = document.createElement('a');
              a.href = url;
              a.download = 'loi-nhap-du-lieu.csv';
              a.click();
              URL.revokeObjectURL(url);
            }}>Tải danh sách dòng lỗi</Button>
          )}
        </div>
      )}
    </Overlay>
  );
}
