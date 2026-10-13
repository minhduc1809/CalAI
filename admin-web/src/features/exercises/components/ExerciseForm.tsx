import { FormField, NumberInput, Select, Textarea, TextInput } from '../../../components/forms/controls';
import { FormActions, FormErrorSummary, FormSection } from '../../../components/forms/FormParts';
import { Overlay } from '../../../components/ui/Overlay';
import { useForm } from '../../../hooks/useForm';
import { EQUIPMENT, Exercise, exerciseApi, GENDERS, LEVELS } from '../api/exercise.api';

interface Values extends Record<string, unknown> {
  name: string; genderTarget: string; level: string; targetMuscle: string; equipment: string;
  sets: string; repsOrDuration: string; restSeconds: string; caloriesBurnedEstimate: string; imageUrl: string;
  preparation: string; execution: string; commonMistakes: string; breathing: string;
}

const blank = (): Values => ({
  name: '', genderTarget: 'ALL', level: 'BEGINNER', targetMuscle: '', equipment: 'NO_EQUIPMENT', sets: '3', repsOrDuration: '10-12 lần',
  restSeconds: '60', caloriesBurnedEstimate: '20', imageUrl: '', preparation: '', execution: '', commonMistakes: '', breathing: '',
});

const fromExercise = (e: Exercise): Values => ({
  name: e.name, genderTarget: e.genderTarget, level: e.level, targetMuscle: e.targetMuscle, equipment: e.equipment,
  sets: String(e.data.sets), repsOrDuration: e.data.repsOrDuration, restSeconds: String(e.data.restSeconds), caloriesBurnedEstimate: String(e.data.caloriesBurnedEstimate),
  imageUrl: e.data.imageUrl ?? '', ...e.data.instructions,
});

function intRange(s: string, label: string, min: number, max: number): string | undefined {
  const n = Number(s);
  return s === '' || !Number.isInteger(n) || n < min || n > max ? `${label} phải là số nguyên từ ${min} đến ${max}` : undefined;
}

interface Props { mode: 'create' | 'edit'; exercise?: Exercise; onClose: () => void; onSaved: (message: string) => void }

/** Một form cho cả thêm và sửa bài tập. Ảnh minh họa nhập bằng đường dẫn (chưa có API tải ảnh lên). */
export function ExerciseForm({ mode, exercise, onClose, onSaved }: Props) {
  const form = useForm<Values>({
    initial: mode === 'edit' && exercise ? fromExercise(exercise) : blank(),
    validate: (v) => {
      const cal = Number(v.caloriesBurnedEstimate);
      return {
        name: v.name.trim() ? (v.name.trim().length > 120 ? 'Tên bài tối đa 120 ký tự' : undefined) : 'Nhập tên bài',
        targetMuscle: v.targetMuscle.trim() ? undefined : 'Nhập nhóm cơ',
        sets: intRange(v.sets, 'Số hiệp', 1, 20),
        restSeconds: intRange(v.restSeconds, 'Thời gian nghỉ', 0, 600),
        caloriesBurnedEstimate: v.caloriesBurnedEstimate === '' || !Number.isFinite(cal) || cal < 0 || cal > 500 ? 'Calo mỗi hiệp từ 0 đến 500' : undefined,
        repsOrDuration: v.repsOrDuration.trim() ? undefined : 'Nhập số lần hoặc thời gian',
        imageUrl: v.imageUrl && !/^https?:\/\//i.test(v.imageUrl) ? 'Đường dẫn phải bắt đầu bằng http:// hoặc https://' : undefined,
        preparation: v.preparation.trim() ? undefined : 'Nhập phần chuẩn bị',
        execution: v.execution.trim() ? undefined : 'Nhập cách thực hiện',
        commonMistakes: v.commonMistakes.trim() ? undefined : 'Nhập các lỗi sai phổ biến',
        breathing: v.breathing.trim() ? undefined : 'Nhập cách hít thở',
      };
    },
    onSubmit: async (v) => {
      const payload = {
        name: v.name.trim(), genderTarget: v.genderTarget, level: v.level, targetMuscle: v.targetMuscle.trim(), equipment: v.equipment,
        sets: Number(v.sets), repsOrDuration: v.repsOrDuration.trim(), restSeconds: Number(v.restSeconds), caloriesBurnedEstimate: Number(v.caloriesBurnedEstimate),
        ...(v.imageUrl ? { imageUrl: v.imageUrl } : {}),
        instructions: { preparation: v.preparation.trim(), execution: v.execution.trim(), commonMistakes: v.commonMistakes.trim(), breathing: v.breathing.trim() },
      };
      if (mode === 'edit' && exercise) await exerciseApi.update(exercise.id, payload);
      else await exerciseApi.create(payload);
    },
    onSuccess: () => onSaved(mode === 'edit' ? 'Đã cập nhật bài tập' : 'Đã thêm bài tập'),
  });
  const v = form.values;
  const e = form.errors;
  const set = form.setValue;

  return (
    <Overlay title={mode === 'edit' ? `Sửa bài: ${exercise?.name ?? ''}` : 'Thêm bài tập'} size="lg" onClose={onClose} busy={form.submitting} dirty={form.dirty}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors} />
        <FormSection title="Thông tin cơ bản">
          <div className="form-grid">
            <FormField label="Tên bài" required htmlFor="x-name" error={e.name} className="full"><TextInput id="x-name" invalid={!!e.name} value={v.name} onChange={(ev) => set('name', ev.target.value)} /></FormField>
            <FormField label="Nhóm cơ" required htmlFor="x-muscle" error={e.targetMuscle}><TextInput id="x-muscle" invalid={!!e.targetMuscle} value={v.targetMuscle} onChange={(ev) => set('targetMuscle', ev.target.value)} /></FormField>
            <FormField label="Mức độ" htmlFor="x-level"><Select id="x-level" options={LEVELS} value={v.level} onChange={(ev) => set('level', ev.target.value)} /></FormField>
            <FormField label="Dụng cụ" htmlFor="x-eq"><Select id="x-eq" options={EQUIPMENT} value={v.equipment} onChange={(ev) => set('equipment', ev.target.value)} /></FormField>
            <FormField label="Dành cho" htmlFor="x-gender"><Select id="x-gender" options={GENDERS} value={v.genderTarget} onChange={(ev) => set('genderTarget', ev.target.value)} /></FormField>
          </div>
        </FormSection>
        <FormSection title="Thông số buổi tập">
          <div className="form-grid">
            <FormField label="Số hiệp" required htmlFor="x-sets" error={e.sets} help="1–20"><NumberInput id="x-sets" invalid={!!e.sets} value={v.sets} onChange={(ev) => set('sets', ev.target.value)} /></FormField>
            <FormField label="Số lần / thời gian" required htmlFor="x-reps" error={e.repsOrDuration}><TextInput id="x-reps" invalid={!!e.repsOrDuration} value={v.repsOrDuration} onChange={(ev) => set('repsOrDuration', ev.target.value)} /></FormField>
            <FormField label="Nghỉ giữa hiệp (giây)" required htmlFor="x-rest" error={e.restSeconds} help="0–600"><NumberInput id="x-rest" invalid={!!e.restSeconds} value={v.restSeconds} onChange={(ev) => set('restSeconds', ev.target.value)} /></FormField>
            <FormField label="Calo mỗi hiệp (ước tính)" required htmlFor="x-cal" error={e.caloriesBurnedEstimate} help="0–500. MET từng loại tập chỉnh ở Cấu hình hệ thống."><NumberInput id="x-cal" invalid={!!e.caloriesBurnedEstimate} value={v.caloriesBurnedEstimate} onChange={(ev) => set('caloriesBurnedEstimate', ev.target.value)} /></FormField>
            <FormField label="Đường dẫn ảnh minh họa" htmlFor="x-img" error={e.imageUrl} help="Chưa có chức năng tải ảnh lên: dán đường dẫn ảnh đã đăng." className="full"><TextInput id="x-img" invalid={!!e.imageUrl} value={v.imageUrl} onChange={(ev) => set('imageUrl', ev.target.value)} /></FormField>
          </div>
        </FormSection>
        <FormSection title="Hướng dẫn">
          <FormField label="Chuẩn bị" required htmlFor="x-prep" error={e.preparation}><Textarea id="x-prep" invalid={!!e.preparation} value={v.preparation} onChange={(ev) => set('preparation', ev.target.value)} /></FormField>
          <FormField label="Thực hiện" required htmlFor="x-exec" error={e.execution}><Textarea id="x-exec" rows={4} invalid={!!e.execution} value={v.execution} onChange={(ev) => set('execution', ev.target.value)} /></FormField>
          <FormField label="Lỗi sai phổ biến" required htmlFor="x-mist" error={e.commonMistakes}><Textarea id="x-mist" invalid={!!e.commonMistakes} value={v.commonMistakes} onChange={(ev) => set('commonMistakes', ev.target.value)} /></FormField>
          <FormField label="Cách hít thở" required htmlFor="x-breath" error={e.breathing}><Textarea id="x-breath" invalid={!!e.breathing} value={v.breathing} onChange={(ev) => set('breathing', ev.target.value)} /></FormField>
        </FormSection>
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel={mode === 'edit' ? 'Lưu thay đổi' : 'Thêm bài'} />
      </form>
    </Overlay>
  );
}
