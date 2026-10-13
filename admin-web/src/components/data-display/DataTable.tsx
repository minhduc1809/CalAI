import { ReactNode } from 'react';
import { PAGE_SIZES } from '../../constants';
import { fmtNumber } from '../../lib/format';
import { EmptyState, ErrorState, LoadingState } from '../feedback/States';
import { AppIcon } from '../ui/icons';

export interface Column<T> {
  key: string;
  header: string;
  cell: (row: T) => ReactNode;
  /** Có khoá sắp xếp phía máy chủ thì bảng cho bấm tiêu đề; bỏ trống nếu backend không hỗ trợ sắp xếp cột này. */
  sortKey?: string;
  align?: 'left' | 'right';
  className?: string;
}

export interface SortState { sort?: string; order?: 'asc' | 'desc' }

interface Props<T> {
  columns: Column<T>[];
  rows: T[] | undefined;
  rowKey: (row: T) => string;
  loading?: boolean;
  fetching?: boolean;
  error?: string | null;
  onRetry?: () => void;
  emptyTitle?: string;
  emptyDescription?: string;
  sort?: SortState;
  onSort?: (next: SortState) => void;
  /** Cột thao tác cuối bảng; chỉ render các hành động hợp lệ theo trạng thái bản ghi. */
  actions?: (row: T) => ReactNode;
  pagination?: { page: number; limit: number; total: number; onPage: (p: number) => void; onLimit: (l: number) => void };
  onRowClick?: (row: T) => void;
}

/** Bảng dữ liệu dùng chung: phân trang/sắp xếp phía máy chủ, loading skeleton, trạng thái rỗng và lỗi có thử lại. */
export function DataTable<T>({
  columns, rows, rowKey, loading, fetching, error, onRetry, emptyTitle = 'Chưa có dữ liệu', emptyDescription, sort, onSort, actions, pagination, onRowClick,
}: Props<T>) {
  const colSpan = columns.length + (actions ? 1 : 0);
  const toggle = (key: string) => {
    if (!onSort) return;
    if (sort?.sort !== key) onSort({ sort: key, order: 'asc' });
    else if (sort.order === 'asc') onSort({ sort: key, order: 'desc' });
    else onSort({});
  };

  let body: ReactNode;
  if (error && !rows) body = <tr><td colSpan={colSpan}><ErrorState message={error} onRetry={onRetry} /></td></tr>;
  else if (loading) body = <tr><td colSpan={colSpan}><LoadingState lines={6} /></td></tr>;
  else if (!rows || rows.length === 0) body = <tr><td colSpan={colSpan}><EmptyState title={emptyTitle} description={emptyDescription} /></td></tr>;
  else
    body = rows.map((row) => (
      <tr key={rowKey(row)} onClick={onRowClick ? () => onRowClick(row) : undefined} style={onRowClick ? { cursor: 'pointer' } : undefined}>
        {columns.map((c) => (
          <td key={c.key} className={c.className} style={{ textAlign: c.align }}>{c.cell(row)}</td>
        ))}
        {actions && <td className="actions" onClick={(e) => e.stopPropagation()}>{actions(row)}</td>}
      </tr>
    ));

  return (
    <div className="card table-card" aria-busy={fetching || undefined}>
      <div className="table-wrap" style={{ opacity: fetching && !loading ? 0.7 : 1, transition: 'opacity 150ms' }}>
        <table className="table">
          <thead>
            <tr>
              {columns.map((c) => {
                const active = c.sortKey && sort?.sort === c.sortKey;
                return (
                  <th
                    key={c.key}
                    style={{ textAlign: c.align }}
                    className={c.sortKey && onSort ? 'sortable' : ''}
                    aria-sort={active ? (sort?.order === 'desc' ? 'descending' : 'ascending') : undefined}
                    onClick={c.sortKey && onSort ? () => toggle(c.sortKey!) : undefined}
                  >
                    {c.header}
                    {active && <AppIcon name={sort?.order === 'desc' ? 'ArrowDown' : 'ArrowUp'} size={13} className="sort-icon" />}
                  </th>
                );
              })}
              {actions && <th style={{ textAlign: 'right' }}>Thao tác</th>}
            </tr>
          </thead>
          <tbody>{body}</tbody>
        </table>
      </div>
      {pagination && rows && rows.length > 0 && <TablePagination {...pagination} />}
    </div>
  );
}

function pageWindow(page: number, pages: number): (number | '…')[] {
  if (pages <= 7) return Array.from({ length: pages }, (_, i) => i + 1);
  const out: (number | '…')[] = [1];
  if (page > 3) out.push('…');
  for (let p = Math.max(2, page - 1); p <= Math.min(pages - 1, page + 1); p++) out.push(p);
  if (page < pages - 2) out.push('…');
  out.push(pages);
  return out;
}

export function TablePagination({ page, limit, total, onPage, onLimit }: { page: number; limit: number; total: number; onPage: (p: number) => void; onLimit: (l: number) => void }) {
  const pages = Math.max(1, Math.ceil(total / limit));
  const from = total === 0 ? 0 : (page - 1) * limit + 1;
  const to = Math.min(total, page * limit);
  return (
    <div className="pagination">
      <span className="muted">Đang hiển thị {fmtNumber(from)}–{fmtNumber(to)} trên tổng số {fmtNumber(total)} bản ghi</span>
      <div className="row">
        <label className="row muted" style={{ gap: 6 }}>
          Dòng/trang
          <select className="select" style={{ width: 76, height: 34 }} value={limit} onChange={(e) => onLimit(Number(e.target.value))} aria-label="Số dòng mỗi trang">
            {PAGE_SIZES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
        <div className="pages">
          <button className="page-btn" disabled={page <= 1} onClick={() => onPage(page - 1)} aria-label="Trang trước"><AppIcon name="ChevronLeft" size={16} /></button>
          {pageWindow(page, pages).map((p, i) =>
            p === '…' ? <span key={`e${i}`} className="muted">…</span> : (
              <button key={p} className={`page-btn ${p === page ? 'active' : ''}`} aria-current={p === page ? 'page' : undefined} onClick={() => onPage(p)}>{p}</button>
            ),
          )}
          <button className="page-btn" disabled={page >= pages} onClick={() => onPage(page + 1)} aria-label="Trang sau"><AppIcon name="ChevronRight" size={16} /></button>
        </div>
      </div>
    </div>
  );
}
