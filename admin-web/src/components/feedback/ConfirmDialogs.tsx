import { ReactNode, useState } from 'react';
import { MIN_REASON_LENGTH } from '../../constants';
import { useForm } from '../../hooks/useForm';
import { ApiError } from '../../services/api-client';
import { FormField, PasswordInput, Textarea } from '../forms/controls';
import { FormActions, FormErrorSummary } from '../forms/FormParts';
import { Button } from '../ui/Button';
import { Overlay } from '../ui/Overlay';

interface ConfirmProps {
  title: string;
  /** Đối tượng bị tác động và hậu quả, hiển thị rõ cho người xác nhận. */
  description: ReactNode;
  confirmLabel?: string;
  danger?: boolean;
  onConfirm: () => Promise<void>;
  onClose: () => void;
}

/** Xác nhận thao tác ít rủi ro hoặc có thể đảo ngược. */
export function ConfirmDialog({ title, description, confirmLabel = 'Xác nhận', danger, onConfirm, onClose }: ConfirmProps) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const run = async () => {
    if (busy) return;
    setBusy(true);
    setError(null);
    try {
      await onConfirm();
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
      onClose={onClose}
      busy={busy}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>Huỷ</Button>
          <Button variant={danger ? 'danger' : 'primary'} loading={busy} onClick={run}>{confirmLabel}</Button>
        </>
      }
    >
      <div>{description}</div>
      {error && <div className="form-error-summary" role="alert" style={{ marginTop: 12 }}>{error}</div>}
    </Overlay>
  );
}

interface ReasonValues extends Record<string, unknown> { reason: string; password: string }

interface ReasonProps {
  title: string;
  /** Đối tượng bị tác động (ví dụ email, mã đơn). */
  subject?: ReactNode;
  description?: ReactNode;
  requireReason?: boolean;
  requirePassword?: boolean;
  confirmLabel?: string;
  danger?: boolean;
  onConfirm: (input: { reason: string; password: string }) => Promise<void>;
  onClose: () => void;
}

/**
 * Thao tác nhạy cảm (tiền, tài khoản, cấu hình an toàn): lý do ≥ 10 ký tự và nhập lại mật khẩu. Mật khẩu chỉ nằm trong state
 * của form này, không ghi log hay URL; backend là nơi kiểm tra cuối cùng, ẩn nút ở frontend không phải cơ chế bảo mật.
 */
export function ReasonPasswordDialog({
  title, subject, description, requireReason = true, requirePassword = true, confirmLabel = 'Xác nhận', danger, onConfirm, onClose,
}: ReasonProps) {
  const form = useForm<ReasonValues>({
    initial: { reason: '', password: '' },
    validate: (v) => ({
      reason: requireReason && v.reason.trim().length < MIN_REASON_LENGTH ? `Lý do cần ít nhất ${MIN_REASON_LENGTH} ký tự` : undefined,
      password: requirePassword && !v.password ? 'Nhập mật khẩu của bạn để xác nhận' : undefined,
    }),
    onSubmit: (v) => onConfirm({ reason: v.reason.trim(), password: v.password }),
    onSuccess: onClose,
  });
  return (
    <Overlay title={title} onClose={onClose} busy={form.submitting}>
      <form onSubmit={form.handleSubmit} noValidate>
        {subject && <div className="alert info" style={{ marginBottom: 12 }}>{subject}</div>}
        {description && <p className="muted" style={{ marginTop: 0 }}>{description}</p>}
        <FormErrorSummary errors={form.formErrors} />
        {requireReason && (
          <FormField label="Lý do" required htmlFor="rp-reason" error={form.errors.reason} help={`Ít nhất ${MIN_REASON_LENGTH} ký tự. Được ghi vào nhật ký quản trị.`}>
            <Textarea id="rp-reason" invalid={!!form.errors.reason} value={form.values.reason} onChange={(e) => form.setValue('reason', e.target.value)} />
          </FormField>
        )}
        {requirePassword && (
          <FormField label="Nhập lại mật khẩu của bạn" required htmlFor="rp-pass" error={form.errors.password}>
            <PasswordInput id="rp-pass" invalid={!!form.errors.password} value={form.values.password} onChange={(e) => form.setValue('password', e.target.value)} />
          </FormField>
        )}
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel={confirmLabel} submitVariant={danger ? 'danger' : 'primary'} />
      </form>
    </Overlay>
  );
}
