import { useId } from 'react';
import { Alert } from '../../../components/feedback/States';
import { Checkbox, FormField, NumberInput, Textarea, TextInput } from '../../../components/forms/controls';
import { FormActions, FormErrorSummary, FormSection } from '../../../components/forms/FormParts';
import { Overlay } from '../../../components/ui/Overlay';
import { useToast } from '../../../components/ui/Toast';
import { useForm } from '../../../hooks/useForm';
import { foodApi } from '../api/food.api';
import { atwarterWarning, defaultFoodValues, FoodFormValues, toFormValues, toPayload, validateFood } from '../schemas/food.schema';
import type { Food } from '../types/food.types';
import { SafetyEditor } from './SafetyEditor';

interface Props {
  mode: 'create' | 'edit';
  food?: Food;
  categories: string[];
  onClose: () => void;
  onSaved: (message: string) => void;
}

/** Một form cho cả thêm và sửa món: cùng schema, cùng quy tắc; chỉ khác giá trị ban đầu và lời gọi API. */
export function FoodForm({ mode, food, categories, onClose, onSaved }: Props) {
  const toast = useToast();
  const listId = useId();
  const form = useForm<FoodFormValues>({
    initial: mode === 'edit' && food ? toFormValues(food) : defaultFoodValues(),
    validate: validateFood,
    onSubmit: async (v) => {
      const payload = toPayload(v);
      const r = mode === 'edit' && food ? await foodApi.update(food.id, payload) : await foodApi.create(payload);
      if (r.warnings?.length) toast.warning('Đã lưu, cần kiểm tra số liệu', r.warnings.join('\n'));
    },
    onSuccess: () => onSaved(mode === 'edit' ? 'Đã cập nhật món' : 'Đã thêm món vào kho'),
  });
  const v = form.values;
  const soft = atwarterWarning(v);
  const err = form.errors;
  const set = form.setValue;

  return (
    <Overlay title={mode === 'edit' ? `Sửa món: ${food?.name ?? ''}` : 'Thêm món vào kho'} size="lg" onClose={onClose} busy={form.submitting} dirty={form.dirty}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        <FormSection title="Thông tin cơ bản">
          <div className="form-grid">
            <FormField label="Tên món" required htmlFor="f-name" error={err.name}><TextInput id="f-name" invalid={!!err.name} value={v.name} onChange={(e) => set('name', e.target.value)} /></FormField>
            <FormField label="Nhóm món" required htmlFor="f-cat" error={err.category}>
              <TextInput id="f-cat" list={listId} invalid={!!err.category} value={v.category} onChange={(e) => set('category', e.target.value)} />
              <datalist id={listId}>{categories.map((c) => <option key={c} value={c} />)}</datalist>
            </FormField>
            <FormField label="Khẩu phần chuẩn" required htmlFor="f-serv" error={err.servingSize} help='Số liệu dinh dưỡng bên dưới tính cho khẩu phần này, ví dụ "1 tô (500g)".' className="full">
              <TextInput id="f-serv" invalid={!!err.servingSize} value={v.servingSize} onChange={(e) => set('servingSize', e.target.value)} />
            </FormField>
          </div>
        </FormSection>
        <FormSection title="Dinh dưỡng cho một khẩu phần">
          <div className="form-grid">
            <FormField label="Calo (kcal)" required htmlFor="f-cal" error={err.calories} help="0–5000"><NumberInput id="f-cal" invalid={!!err.calories} value={v.calories} onChange={(e) => set('calories', e.target.value)} /></FormField>
            <FormField label="Đạm (g)" required htmlFor="f-pro" error={err.protein} help="0–500"><NumberInput id="f-pro" invalid={!!err.protein} value={v.protein} onChange={(e) => set('protein', e.target.value)} /></FormField>
            <FormField label="Tinh bột (g)" required htmlFor="f-carb" error={err.carb} help="0–500"><NumberInput id="f-carb" invalid={!!err.carb} value={v.carb} onChange={(e) => set('carb', e.target.value)} /></FormField>
            <FormField label="Béo (g)" required htmlFor="f-fat" error={err.fat} help="0–500"><NumberInput id="f-fat" invalid={!!err.fat} value={v.fat} onChange={(e) => set('fat', e.target.value)} /></FormField>
          </div>
          {soft && <Alert kind="warning">{soft}</Alert>}
        </FormSection>
        <FormSection title="An toàn dinh dưỡng">
          <SafetyEditor value={v} onChange={(t) => (Object.keys(t) as (keyof typeof t)[]).forEach((k) => set(k, t[k] as never))} />
          {err.allergens && <span className="err">{err.allergens}</span>}
        </FormSection>
        <FormSection title="Nguồn và tìm kiếm">
          <div className="form-grid">
            <FormField label="Nguồn số liệu" required htmlFor="f-src" error={err.dataSource} help="Bảng thành phần thực phẩm, nhãn sản phẩm, công thức…" className="full"><TextInput id="f-src" invalid={!!err.dataSource} value={v.dataSource} onChange={(e) => set('dataSource', e.target.value)} /></FormField>
            <FormField label="Tên gọi khác" htmlFor="f-alias" help="Cách nhau bằng dấu phẩy, tối đa 10"><TextInput id="f-alias" value={v.aliases} onChange={(e) => set('aliases', e.target.value)} /></FormField>
            <FormField label="Ghi chú" htmlFor="f-note"><TextInput id="f-note" value={v.note} onChange={(e) => set('note', e.target.value)} /></FormField>
            <FormField label="Đơn vị khẩu phần" htmlFor="f-units" error={err.units} help={'Mỗi dòng "tên=số gram", thêm * cho đơn vị mặc định. Bỏ trống để dùng khẩu phần chuẩn.'} className="full">
              <Textarea id="f-units" invalid={!!err.units} value={v.units} onChange={(e) => set('units', e.target.value)} placeholder={'tô nhỏ=350\ntô vừa=500*\ntô lớn=650'} />
            </FormField>
            <div className="full"><Checkbox label="Cần chuyên gia rà soát số liệu" checked={v.needsReview} onChange={(e) => set('needsReview', e.target.checked)} /></div>
          </div>
        </FormSection>
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel={mode === 'edit' ? 'Lưu thay đổi' : 'Thêm món'} />
      </form>
    </Overlay>
  );
}
