import { ReactNode } from 'react';
import { Button } from '../ui/Button';

export function FormSection({ title, children }: { title?: string; children: ReactNode }) {
  return (
    <section className="form-section">
      {title && <h3>{title}</h3>}
      {children}
    </section>
  );
}

/** Tóm tắt lỗi cấp form (lỗi từ backend hoặc lỗi không gắn được vào một trường). */
export function FormErrorSummary({ errors }: { errors: string[] }) {
  if (errors.length === 0) return null;
  return (
    <div className="form-error-summary" role="alert">
      {errors.length === 1 ? errors[0] : (
        <>
          Vui lòng kiểm tra lại:
          <ul>{errors.map((e) => <li key={e}>{e}</li>)}</ul>
        </>
      )}
    </div>
  );
}

/** Nút Huỷ và Lưu ở vị trí cố định: Huỷ bên trái Lưu, cùng trạng thái loading cho mọi form. */
export function FormActions({
  onCancel, submitting, submitLabel = 'Lưu', cancelLabel = 'Huỷ', submitVariant = 'primary', disabled,
}: { onCancel?: () => void; submitting?: boolean; submitLabel?: string; cancelLabel?: string; submitVariant?: 'primary' | 'danger'; disabled?: boolean }) {
  return (
    <div className="form-actions">
      {onCancel && <Button variant="secondary" onClick={onCancel} disabled={submitting}>{cancelLabel}</Button>}
      <Button type="submit" variant={submitVariant} loading={submitting} disabled={disabled}>{submitLabel}</Button>
    </div>
  );
}
