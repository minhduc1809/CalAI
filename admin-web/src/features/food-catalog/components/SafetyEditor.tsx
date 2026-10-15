import { Checkbox } from '../../../components/forms/controls';
import { ALLERGENS, ALLERGEN_LABEL, DIET_FLAGS, SafetyTags } from '../types/food.types';

/** Khai báo thẻ an toàn tường minh: thiếu thẻ thì món bị loại khỏi mọi gợi ý (fail-closed). Dùng cho thêm/sửa món và duyệt món gửi. */
export function SafetyEditor({ value, onChange }: { value: SafetyTags; onChange: (v: SafetyTags) => void }) {
  const toggle = (a: string) =>
    onChange({ ...value, allergens: value.allergens.includes(a) ? value.allergens.filter((x) => x !== a) : [...value.allergens, a] });
  return (
    <div className="stack" style={{ gap: 12 }}>
      <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
        <legend style={{ fontSize: 13, fontWeight: 500, marginBottom: 6 }}>Chất gây dị ứng <span className="muted">(không chọn nghĩa là món không có)</span></legend>
        <div className="row" style={{ gap: 14 }}>
          {ALLERGENS.map((a) => <Checkbox key={a} label={ALLERGEN_LABEL[a]} checked={value.allergens.includes(a)} onChange={() => toggle(a)} />)}
        </div>
      </fieldset>
      <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
        <legend style={{ fontSize: 13, fontWeight: 500, marginBottom: 6 }}>Cờ chế độ ăn</legend>
        <div className="row" style={{ gap: 14 }}>
          {DIET_FLAGS.map(([k, label]) => <Checkbox key={k} label={label} checked={value[k]} onChange={(e) => onChange({ ...value, [k]: e.target.checked })} />)}
        </div>
      </fieldset>
    </div>
  );
}
