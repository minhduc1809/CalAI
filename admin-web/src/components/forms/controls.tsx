import { InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes, forwardRef, useId, useState } from 'react';
import { AppIcon } from '../ui/icons';

/** Khung trường nhập: label thống nhất, dấu bắt buộc, help text và lỗi ngay dưới trường. */
export function FormField({
  label, required, help, error, children, className = '', htmlFor,
}: { label: string; required?: boolean; help?: string; error?: string; children: ReactNode; className?: string; htmlFor?: string }) {
  return (
    <div className={`field ${className}`}>
      <label htmlFor={htmlFor}>
        {label}
        {required && <span className="req" aria-hidden>*</span>}
      </label>
      {children}
      {error ? <span className="err" role="alert">{error}</span> : help ? <span className="help">{help}</span> : null}
    </div>
  );
}

interface BaseProps { invalid?: boolean }

export const TextInput = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement> & BaseProps>(function TextInput(
  { invalid, className = '', ...rest }, ref,
) {
  return <input ref={ref} className={`input ${invalid ? 'invalid' : ''} ${className}`} aria-invalid={invalid || undefined} {...rest} />;
});

/** Ô số: giữ chuỗi người dùng gõ, giá trị số do nơi dùng parse và kiểm giới hạn. */
export const NumberInput = forwardRef<HTMLInputElement, Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & BaseProps>(function NumberInput(
  { invalid, className = '', ...rest }, ref,
) {
  return <input ref={ref} type="number" inputMode="decimal" step="any" className={`input ${invalid ? 'invalid' : ''} ${className}`} aria-invalid={invalid || undefined} {...rest} />;
});

export function PasswordInput({ invalid, className = '', ...rest }: Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & BaseProps) {
  const [show, setShow] = useState(false);
  return (
    <div style={{ position: 'relative' }}>
      <input
        type={show ? 'text' : 'password'}
        autoComplete="current-password"
        className={`input ${invalid ? 'invalid' : ''} ${className}`}
        style={{ paddingRight: 40 }}
        aria-invalid={invalid || undefined}
        {...rest}
      />
      <button
        type="button"
        className="icon-btn"
        style={{ position: 'absolute', right: 3, top: 3 }}
        aria-label={show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
        onClick={() => setShow((s) => !s)}
      >
        <AppIcon name={show ? 'EyeOff' : 'Eye'} size={18} />
      </button>
    </div>
  );
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement> & BaseProps>(function Textarea(
  { invalid, className = '', rows = 3, ...rest }, ref,
) {
  return <textarea ref={ref} rows={rows} className={`textarea ${invalid ? 'invalid' : ''} ${className}`} aria-invalid={invalid || undefined} {...rest} />;
});

export interface Option { value: string; label: string }

export function Select({
  options, placeholder, invalid, className = '', ...rest
}: SelectHTMLAttributes<HTMLSelectElement> & BaseProps & { options: Option[]; placeholder?: string }) {
  return (
    <select className={`select ${invalid ? 'invalid' : ''} ${className}`} aria-invalid={invalid || undefined} {...rest}>
      {placeholder !== undefined && <option value="">{placeholder}</option>}
      {options.map((o) => (
        <option key={o.value} value={o.value}>{o.label}</option>
      ))}
    </select>
  );
}

export function Checkbox({ label, ...rest }: Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & { label: string }) {
  const id = useId();
  return (
    <label className="checkbox" htmlFor={id}>
      <input id={id} type="checkbox" {...rest} />
      {label}
    </label>
  );
}

export function RadioGroup({
  name, value, options, onChange,
}: { name: string; value: string; options: Option[]; onChange: (v: string) => void }) {
  return (
    <div className="row" role="radiogroup">
      {options.map((o) => (
        <label key={o.value} className="checkbox">
          <input type="radio" name={name} value={o.value} checked={value === o.value} onChange={() => onChange(o.value)} />
          {o.label}
        </label>
      ))}
    </div>
  );
}
