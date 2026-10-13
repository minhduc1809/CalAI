import { ReactNode, useEffect, useRef, useState } from 'react';
import { AppIcon, IconName } from './icons';
import { IconButton } from './Button';

export interface MenuItem {
  label: string;
  icon?: IconName;
  onSelect: () => void;
  danger?: boolean;
  disabled?: boolean;
}

/** Menu thao tác phụ (dấu ba chấm). Chỉ truyền các hành động hợp lệ với trạng thái của bản ghi. */
export function DropdownMenu({ items, label = 'Thao tác khác', trigger }: { items: MenuItem[]; label?: string; trigger?: ReactNode }) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (!open) return;
    const close = (e: MouseEvent) => !ref.current?.contains(e.target as Node) && setOpen(false);
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false);
    document.addEventListener('mousedown', close);
    document.addEventListener('keydown', esc);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('keydown', esc);
    };
  }, [open]);
  if (items.length === 0) return null;
  return (
    <div className="admin-menu" ref={ref}>
      {trigger ? (
        <span onClick={() => setOpen((o) => !o)}>{trigger}</span>
      ) : (
        <IconButton icon="More" label={label} aria-haspopup="menu" aria-expanded={open} onClick={() => setOpen((o) => !o)} />
      )}
      {open && (
        <div className="menu-pop" role="menu">
          {items.map((it) => (
            <button
              key={it.label}
              role="menuitem"
              className={it.danger ? 'danger' : ''}
              disabled={it.disabled}
              onClick={() => {
                setOpen(false);
                it.onSelect();
              }}
            >
              {it.icon && <AppIcon name={it.icon} size={16} />}
              {it.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
