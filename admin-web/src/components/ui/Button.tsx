import { ButtonHTMLAttributes, ReactNode } from 'react';
import { AppIcon, IconName } from './icons';

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: 'md' | 'sm';
  icon?: IconName;
  loading?: boolean;
  children?: ReactNode;
}

/** Nút chuẩn. `loading` khoá thao tác để không gửi lặp. */
export function Button({ variant = 'secondary', size = 'md', icon, loading, disabled, children, className = '', type = 'button', ...rest }: ButtonProps) {
  return (
    <button
      type={type}
      className={`btn btn-${variant} ${size === 'sm' ? 'btn-sm' : ''} ${className}`}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      {...rest}
    >
      {loading ? <span className="spinner" aria-hidden /> : icon ? <AppIcon name={icon} size={size === 'sm' ? 16 : 18} /> : null}
      {children}
    </button>
  );
}

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  icon: IconName;
  /** Bắt buộc: tên truy cập và tooltip của nút chỉ có icon. */
  label: string;
}

export function IconButton({ icon, label, className = '', type = 'button', ...rest }: IconButtonProps) {
  return (
    <button type={type} className={`icon-btn ${className}`} aria-label={label} title={label} {...rest}>
      <AppIcon name={icon} size={18} />
    </button>
  );
}
