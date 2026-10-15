import { FilterField } from './FilterBar';

/** Khoảng ngày (giờ Việt Nam): hai ô ngày, kiểm tra từ ≤ đến; giá trị yyyy-MM-dd. */
export function DateRangeFilter({ from, to, onChange, label = 'Khoảng ngày' }: { from: string; to: string; onChange: (from: string, to: string) => void; label?: string }) {
  const invalid = !!from && !!to && from > to;
  return (
    <FilterField label={label}>
      <div className="row" style={{ gap: 6, flexWrap: 'nowrap' }}>
        <input className={`input ${invalid ? 'invalid' : ''}`} type="date" aria-label="Từ ngày" value={from} max={to || undefined} onChange={(e) => onChange(e.target.value, to)} style={{ width: 150 }} />
        <span className="muted">→</span>
        <input className={`input ${invalid ? 'invalid' : ''}`} type="date" aria-label="Đến ngày" value={to} min={from || undefined} onChange={(e) => onChange(from, e.target.value)} style={{ width: 150 }} />
      </div>
    </FilterField>
  );
}
