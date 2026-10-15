import { useEffect, useState } from 'react';
import QRCode from 'qrcode';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { Alert, EmptyState, ErrorState, LoadingState } from '../../../components/feedback/States';
import { FormField, PasswordInput, TextInput } from '../../../components/forms/controls';
import { FormActions, FormErrorSummary } from '../../../components/forms/FormParts';
import { Button } from '../../../components/ui/Button';
import { useToast } from '../../../components/ui/Toast';
import { useForm } from '../../../hooks/useForm';
import { fmtDateTime } from '../../../lib/format';
import { authApi, TotpSetup } from '../../auth/api/auth.api';

interface PwValues extends Record<string, unknown> { oldPassword: string; newPassword: string; confirm: string }

function ChangePasswordCard() {
  const toast = useToast();
  const form = useForm<PwValues>({
    initial: { oldPassword: '', newPassword: '', confirm: '' },
    validate: (v) => ({
      oldPassword: v.oldPassword ? undefined : 'Nhập mật khẩu hiện tại',
      newPassword: v.newPassword.length < 8 || !/[a-z]/.test(v.newPassword) || !/[A-Z]/.test(v.newPassword) || !/\d/.test(v.newPassword) ? 'Tối thiểu 8 ký tự, gồm chữ hoa, chữ thường và chữ số' : undefined,
      confirm: v.confirm === v.newPassword ? undefined : 'Xác nhận mật khẩu không khớp',
    }),
    onSubmit: (v) => authApi.changePassword(v.oldPassword, v.newPassword) as Promise<void>,
    onSuccess: () => toast.success('Đã đổi mật khẩu', 'Các thiết bị khác đã bị đăng xuất.'),
  });
  return (
    <div className="card">
      <div className="card-title">Đổi mật khẩu</div>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        <div className="form-grid">
          <FormField label="Mật khẩu hiện tại" required htmlFor="pw-old" error={form.errors.oldPassword} className="full">
            <PasswordInput id="pw-old" invalid={!!form.errors.oldPassword} value={form.values.oldPassword} onChange={(e) => form.setValue('oldPassword', e.target.value)} />
          </FormField>
          <FormField label="Mật khẩu mới" required htmlFor="pw-new" error={form.errors.newPassword}>
            <PasswordInput id="pw-new" autoComplete="new-password" invalid={!!form.errors.newPassword} value={form.values.newPassword} onChange={(e) => form.setValue('newPassword', e.target.value)} />
          </FormField>
          <FormField label="Nhập lại mật khẩu mới" required htmlFor="pw-confirm" error={form.errors.confirm}>
            <PasswordInput id="pw-confirm" autoComplete="new-password" invalid={!!form.errors.confirm} value={form.values.confirm} onChange={(e) => form.setValue('confirm', e.target.value)} />
          </FormField>
        </div>
        <FormActions submitting={form.submitting} submitLabel="Đổi mật khẩu" />
      </form>
    </div>
  );
}

function TotpCard() {
  const toast = useToast();
  const qc = useQueryClient();
  const status = useQuery({ queryKey: ['totp-status'], queryFn: () => authApi.totpStatus() });
  const [setup, setSetup] = useState<TotpSetup | null>(null);
  const [qr, setQr] = useState('');
  const [backup, setBackup] = useState<string[] | null>(null);
  const [code, setCode] = useState('');
  const [disabling, setDisabling] = useState(false);
  const [dis, setDis] = useState({ password: '', code: '' });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (setup) QRCode.toDataURL(setup.otpauthUrl, { width: 200, margin: 1 }).then(setQr);
  }, [setup]);

  const start = useMutation({ mutationFn: () => authApi.totpSetup(), onSuccess: (r) => { setSetup(r); setError(null); }, onError: (e: Error) => setError(e.message) });
  const enable = useMutation({
    mutationFn: () => authApi.totpEnable(code.trim()),
    onSuccess: (r) => { setBackup(r.backupCodes); setSetup(null); setCode(''); setError(null); qc.invalidateQueries({ queryKey: ['totp-status'] }); toast.success('Đã bật xác thực hai lớp'); },
    onError: (e: Error) => setError(e.message),
  });
  const disable = useMutation({
    mutationFn: () => authApi.totpDisable(dis.password, dis.code.trim()),
    onSuccess: () => { setDisabling(false); setDis({ password: '', code: '' }); setError(null); qc.invalidateQueries({ queryKey: ['totp-status'] }); toast.success('Đã tắt xác thực hai lớp'); },
    onError: (e: Error) => setError(e.message),
  });

  return (
    <div className="card">
      <div className="row between"><div className="card-title">Xác thực hai lớp (TOTP)</div>{status.data && <StatusBadge tone={status.data.enabled ? 'success' : 'warning'}>{status.data.enabled ? `Đã bật · còn ${status.data.backupCodesLeft} mã dự phòng` : 'Chưa bật'}</StatusBadge>}</div>
      {status.isLoading ? <LoadingState lines={2} /> : status.isError ? <ErrorState message={(status.error as Error).message} onRetry={() => status.refetch()} /> : (
        <div className="stack">
          {error && <div className="form-error-summary" role="alert">{error}</div>}
          {!status.data?.enabled && <Alert kind="warning">Nên bật xác thực hai lớp trước khi phát hành thật: mọi admin đều toàn quyền.</Alert>}
          {backup && (
            <Alert kind="warning">
              <b>Mã dự phòng (chỉ hiện một lần).</b> Lưu ở nơi an toàn; mỗi mã dùng được một lần khi mất điện thoại.
              <div className="row" style={{ marginTop: 8 }}>{backup.map((c) => <code key={c}>{c}</code>)}</div>
            </Alert>
          )}
          {!status.data?.enabled && !setup && <div><Button variant="primary" loading={start.isPending} onClick={() => start.mutate()}>Bật xác thực hai lớp</Button></div>}
          {setup && (
            <form onSubmit={(e) => { e.preventDefault(); enable.mutate(); }}>
              <p>Quét mã QR bằng ứng dụng xác thực (Google Authenticator, Authy…), rồi nhập mã 6 số.</p>
              {qr && <img src={qr} alt="Mã QR để thiết lập xác thực hai lớp" width={200} height={200} />}
              <p className="muted" style={{ fontSize: 12.5 }}>Không quét được? Nhập tay khoá: <code>{setup.secret}</code></p>
              <FormField label="Mã 6 số" required htmlFor="totp-code"><TextInput id="totp-code" inputMode="numeric" autoComplete="one-time-code" value={code} onChange={(e) => setCode(e.target.value)} /></FormField>
              <FormActions onCancel={() => setSetup(null)} submitting={enable.isPending} submitLabel="Xác nhận và bật" disabled={code.trim().length < 6} />
            </form>
          )}
          {status.data?.enabled && !disabling && <div><Button variant="danger" onClick={() => setDisabling(true)}>Tắt xác thực hai lớp</Button></div>}
          {status.data?.enabled && disabling && (
            <form onSubmit={(e) => { e.preventDefault(); disable.mutate(); }}>
              <FormField label="Mật khẩu" required htmlFor="dis-pass"><PasswordInput id="dis-pass" value={dis.password} onChange={(e) => setDis({ ...dis, password: e.target.value })} /></FormField>
              <FormField label="Mã 6 số hoặc mã dự phòng" required htmlFor="dis-code"><TextInput id="dis-code" value={dis.code} onChange={(e) => setDis({ ...dis, code: e.target.value })} /></FormField>
              <FormActions onCancel={() => setDisabling(false)} submitting={disable.isPending} submitLabel="Tắt" submitVariant="danger" disabled={!dis.password || !dis.code} />
            </form>
          )}
        </div>
      )}
    </div>
  );
}

function SessionsCard() {
  const toast = useToast();
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ['my-sessions'], queryFn: () => authApi.sessions() });
  const revoke = useMutation({
    mutationFn: (id: string) => authApi.revokeSession(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['my-sessions'] }); toast.success('Đã đăng xuất thiết bị'); },
    onError: (e: Error) => toast.error('Không đăng xuất được thiết bị', e.message),
  });
  return (
    <div className="card">
      <div className="card-title">Phiên đăng nhập của tôi</div>
      {q.isLoading ? <LoadingState lines={3} /> : q.isError ? <ErrorState message={(q.error as Error).message} onRetry={() => q.refetch()} /> : !q.data || q.data.length === 0 ? <EmptyState title="Không có phiên nào" icon="Security" /> : (
        <div className="table-wrap"><table className="table">
          <thead><tr><th>Thiết bị</th><th>Đăng nhập lúc</th><th>Dùng gần nhất</th><th /></tr></thead>
          <tbody>{q.data.map((s) => (
            <tr key={s.id}><td>{s.deviceName ?? 'Không rõ'}</td><td>{fmtDateTime(s.createdAt)}</td><td>{fmtDateTime(s.lastUsedAt)}</td>
              <td className="actions"><Button size="sm" variant="secondary" loading={revoke.isPending && revoke.variables === s.id} onClick={() => revoke.mutate(s.id)}>Đăng xuất</Button></td></tr>
          ))}</tbody>
        </table></div>
      )}
      <p className="muted" style={{ fontSize: 12.5, marginBottom: 0 }}>Không có đăng ký admin công khai: tài khoản quản trị chỉ được tạo bằng script trên máy chủ.</p>
    </div>
  );
}

export default function SecurityPage() {
  return (
    <>
      <PageHeader title="Bảo mật tài khoản" breadcrumbs={[{ label: 'Bảo mật' }, { label: 'Bảo mật tài khoản' }]} />
      <div className="stack">
        <ChangePasswordCard />
        <TotpCard />
        <SessionsCard />
      </div>
    </>
  );
}
