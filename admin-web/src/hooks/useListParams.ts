import { useCallback, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { DEFAULT_PAGE_SIZE, PAGE_SIZES, SEARCH_DEBOUNCE_MS } from '../constants';

export function useDebounced<T>(value: T, ms = SEARCH_DEBOUNCE_MS): T {
  const [v, setV] = useState(value);
  useEffect(() => {
    const t = window.setTimeout(() => setV(value), ms);
    return () => window.clearTimeout(t);
  }, [value, ms]);
  return v;
}

export interface ListParams {
  page: number;
  limit: number;
  q: string;
  sort?: string;
  order?: 'asc' | 'desc';
  /** Các bộ lọc khác của màn hình (trạng thái, nhóm, ngày...). */
  filters: Record<string, string>;
}

/**
 * Trạng thái danh sách (trang, số dòng, tìm kiếm, sắp xếp, bộ lọc) giữ trong URL: mở chi tiết rồi quay lại vẫn còn
 * nguyên bộ lọc, có thể chia sẻ đường dẫn. Đổi bộ lọc thì về trang 1; đổi trang thì giữ nguyên bộ lọc.
 */
export function useListParams(filterKeys: string[] = [], defaults: Partial<Record<string, string>> = {}) {
  const [sp, setSp] = useSearchParams();

  const params = useMemo<ListParams>(() => {
    const limitRaw = Number(sp.get('limit'));
    const filters: Record<string, string> = {};
    for (const k of filterKeys) filters[k] = sp.get(k) ?? defaults[k] ?? '';
    return {
      page: Math.max(1, Number(sp.get('page')) || 1),
      limit: (PAGE_SIZES as readonly number[]).includes(limitRaw) ? limitRaw : DEFAULT_PAGE_SIZE,
      q: sp.get('q') ?? '',
      sort: sp.get('sort') ?? undefined,
      order: (sp.get('order') as 'asc' | 'desc' | null) ?? undefined,
      filters,
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sp]);

  const patch = useCallback((changes: Record<string, string | number | undefined>, resetPage: boolean) => {
    setSp((cur) => {
      const next = new URLSearchParams(cur);
      for (const [k, v] of Object.entries(changes)) {
        if (v === undefined || v === '') next.delete(k);
        else next.set(k, String(v));
      }
      if (resetPage) next.delete('page');
      return next;
    }, { replace: true });
  }, [setSp]);

  const activeFilterCount = filterKeys.filter((k) => params.filters[k] && params.filters[k] !== (defaults[k] ?? '')).length + (params.q ? 1 : 0);

  return {
    params,
    activeFilterCount,
    setPage: (page: number) => patch({ page }, false),
    setLimit: (limit: number) => patch({ limit }, true),
    setQuery: (q: string) => patch({ q }, true),
    setFilter: (key: string, value: string) => patch({ [key]: value }, true),
    setSort: (s: { sort?: string; order?: 'asc' | 'desc' }) => patch({ sort: s.sort, order: s.order }, true),
    clear: () => setSp(new URLSearchParams(), { replace: true }),
  };
}
