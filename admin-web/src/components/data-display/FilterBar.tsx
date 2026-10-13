import { ReactNode, useEffect, useState } from 'react';
import { useDebounced } from '../../hooks/useListParams';
import { Button } from '../ui/Button';
import { AppIcon } from '../ui/icons';

interface Props {
  /** Giá trị tìm kiếm đang áp dụng (từ URL). Bỏ trống `onSearch` nếu màn hình không có tìm kiếm. */
  query?: string;
  onSearch?: (q: string) => void;
  searchPlaceholder?: string;
  children?: ReactNode;
  activeCount: number;
  onClear: () => void;
}

/** Thanh lọc thống nhất: tìm kiếm có debounce, các bộ lọc, đếm bộ lọc đang áp dụng và nút xoá bộ lọc (không xoá dữ liệu). */
export function FilterBar({ query = '', onSearch, searchPlaceholder = 'Tìm kiếm…', children, activeCount, onClear }: Props) {
  const [text, setText] = useState(query);
  const debounced = useDebounced(text);
  useEffect(() => setText(query), [query]);
  useEffect(() => {
    if (onSearch && debounced !== query) onSearch(debounced.trim());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debounced]);

  return (
    <div className="card" style={{ marginBottom: 14, padding: '14px 16px' }}>
      <div className="filter-bar" style={{ marginBottom: 0 }}>
        {onSearch && (
          <div className="search">
            <AppIcon name="Search" size={16} />
            <input className="input" type="search" placeholder={searchPlaceholder} value={text} onChange={(e) => setText(e.target.value)} aria-label="Tìm kiếm" />
          </div>
        )}
        {children}
        {activeCount > 0 && (
          <>
            <span className="filter-count">{activeCount} bộ lọc đang áp dụng</span>
            <Button variant="ghost" size="sm" icon="X" onClick={onClear}>Xoá bộ lọc</Button>
          </>
        )}
      </div>
    </div>
  );
}

export function FilterField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="field">
      <label>{label}</label>
      {children}
    </div>
  );
}
