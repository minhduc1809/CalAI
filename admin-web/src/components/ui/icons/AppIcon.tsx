import { ICONS, IconName } from './icon-map';

interface Props {
  name: IconName;
  /** Kích thước px: 16–20 trong bảng/nút, 18–20 sidebar, 20–24 card thống kê. */
  size?: number;
  className?: string;
  /** Nhãn cho trình đọc màn hình khi icon đứng một mình; bỏ trống thì icon chỉ để trang trí. */
  label?: string;
}

const STROKE = 1.9;

/** Wrapper icon duy nhất: chuẩn hoá kích thước, độ dày nét và accessibility. */
export function AppIcon({ name, size = 18, className, label }: Props) {
  const Cmp = ICONS[name];
  return (
    <Cmp
      size={size}
      strokeWidth={STROKE}
      className={className}
      aria-hidden={label ? undefined : true}
      aria-label={label}
      role={label ? 'img' : undefined}
    />
  );
}
