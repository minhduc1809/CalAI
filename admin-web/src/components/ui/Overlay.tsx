import { ReactNode, useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Button, IconButton } from './Button';

interface OverlayProps {
  title: string;
  /** Gọi khi người dùng muốn đóng (Esc, bấm nền, nút X). Nơi dùng quyết định có hỏi "bỏ thay đổi?" hay không. */
  onClose: () => void;
  children: ReactNode;
  footer?: ReactNode;
  size?: 'md' | 'lg';
  variant?: 'dialog' | 'drawer';
  /** Khoá việc đóng khi đang gửi dữ liệu, để không đóng trước khi máy chủ xác nhận. */
  busy?: boolean;
  /** Form có thay đổi chưa lưu: đóng sẽ hỏi lại bằng thanh xác nhận ngay trong hộp, không dùng hộp thoại của trình duyệt. */
  dirty?: boolean;
}

const FOCUSABLE = 'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

/** Dialog / Drawer dùng chung: focus vào bên trong, giữ focus trong hộp, Esc để đóng, trả focus về nút đã mở. */
export function Overlay({ title, onClose, children, footer, size = 'md', variant = 'dialog', busy, dirty }: OverlayProps) {
  const [askDiscard, setAskDiscard] = useState(false);
  const requestClose = () => (dirty ? setAskDiscard(true) : onClose());
  const ref = useRef<HTMLDivElement>(null);
  const previous = useRef<Element | null>(null);

  // Chỉ lúc mở: ghi nhớ nơi đang focus, đưa focus vào hộp, và trả focus về khi đóng
  useEffect(() => {
    previous.current = document.activeElement;
    const el = ref.current;
    (el?.querySelector<HTMLElement>('input:not([disabled]), select, textarea') ?? el?.querySelector<HTMLElement>(FOCUSABLE))?.focus();
    return () => (previous.current as HTMLElement | null)?.focus?.();
  }, []);

  // Phím tắt: Esc để đóng (trừ khi đang gửi), Tab giữ focus trong hộp
  useEffect(() => {
    const el = ref.current;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !busy) {
        e.stopPropagation();
        requestClose();
      }
      if (e.key === 'Tab' && el) {
        const items = Array.from(el.querySelectorAll<HTMLElement>(FOCUSABLE));
        if (items.length === 0) return;
        const firstEl = items[0];
        const lastEl = items[items.length - 1];
        if (e.shiftKey && document.activeElement === firstEl) {
          e.preventDefault();
          lastEl.focus();
        } else if (!e.shiftKey && document.activeElement === lastEl) {
          e.preventDefault();
          firstEl.focus();
        }
      }
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [busy, dirty]);

  const panelClass = variant === 'drawer' ? 'drawer-panel' : `dialog ${size === 'lg' ? 'lg' : ''}`;
  return createPortal(
    <div className={`overlay ${variant === 'drawer' ? 'drawer' : ''}`} onMouseDown={(e) => e.target === e.currentTarget && !busy && requestClose()}>
      <div ref={ref} className={panelClass} role="dialog" aria-modal="true" aria-label={title}>
        <div className="dialog-head">
          <h2>{title}</h2>
          <IconButton icon="X" label="Đóng" onClick={requestClose} disabled={busy} />
        </div>
        {askDiscard && (
          <div className="alert warning" role="alertdialog" style={{ margin: '0 22px 8px', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>Bạn có thay đổi chưa lưu. Bỏ các thay đổi này?</span>
            <span className="row" style={{ gap: 8 }}>
              <Button size="sm" variant="secondary" onClick={() => setAskDiscard(false)}>Tiếp tục chỉnh sửa</Button>
              <Button size="sm" variant="danger" onClick={onClose}>Bỏ thay đổi</Button>
            </span>
          </div>
        )}
        <div className="dialog-body" style={{ flex: 1 }}>{children}</div>
        {footer && <div className="dialog-foot">{footer}</div>}
      </div>
    </div>,
    document.body,
  );
}
