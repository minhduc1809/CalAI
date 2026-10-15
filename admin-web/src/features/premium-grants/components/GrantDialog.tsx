import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { FormField, NumberInput, PasswordInput, Textarea, TextInput } from '../../../components/forms/controls';
import { FormActions, FormErrorSummary } from '../../../components/forms/FormParts';
import { Overlay } from '../../../components/ui/Overlay';
import { useToast } from '../../../components/ui/Toast';
import { MIN_REASON_LENGTH } from '../../../constants';
import { useDebounced } from '../../../hooks/useListParams';
import { useForm } from '../../../hooks/useForm';
import { userApi } from '../../users/api/user.api';
import { grantApi } from '../api/grant.api';

interface Values extends Record<string, unknown> { days: string; reason: string; password: string }

interface Props {
  /** Nếu đã biết người dùng (từ trang chi tiết) thì không cần ô tìm. */
  fixedUser?: { id: string; email: string | null };
  maxDays?: number;
  onClose: () => void;
}

/** Cấp Premium thủ công: chọn người dùng qua tìm email, số ngày, lý do và mật khẩu. Backend kiểm tra lại mọi điều kiện. */
export function GrantDialog({ fixedUser, maxDays = 90, onClose }: Props) {
  const toast = useToast();
  const qc = useQueryClient();
  const [search, setSearch] = useState('');
  const debounced = useDebounced(search);
  const [picked, setPicked] = useState<{ id: string; email: string | null } | null>(fixedUser ?? null);
  const [pickError, setPickError] = useState<string | null>(null);

  const candidates = useQuery({
    queryKey: ['grant-user-search', debounced],
    queryFn: ({ signal }) => userApi.list({ search: debounced, limit: 6, role: 'USER' }, signal),
    enabled: !fixedUser && !picked && debounced.length >= 2,
  });

  const form = useForm<Values>({
    initial: { days: '30', reason: '', password: '' },
    validate: (v) => {
      const d = Number(v.days);
      return {
        days: !Number.isInteger(d) || d < 1 || d > maxDays ? `Số ngày phải là số nguyên từ 1 đến ${maxDays}` : undefined,
        reason: v.reason.trim().length < MIN_REASON_LENGTH ? `Lý do cần ít nhất ${MIN_REASON_LENGTH} ký tự` : undefined,
        password: v.password ? undefined : 'Nhập mật khẩu của bạn để xác nhận',
      };
    },
    onSubmit: async (v) => {
      if (!picked) {
        setPickError('Chọn người dùng cần cấp');
        throw new Error('Chưa chọn người dùng');
      }
      setPickError(null);
      await grantApi.create({ userId: picked.id, days: Number(v.days), reason: v.reason.trim(), password: v.password });
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['grants'] });
      qc.invalidateQueries({ queryKey: ['users'] });
      qc.invalidateQueries({ queryKey: ['user'] });
      toast.success('Đã cấp Premium', `${picked?.email ?? ''} · ${form.values.days} ngày`);
      onClose();
    },
  });

  return (
    <Overlay title="Cấp Premium thủ công" onClose={onClose} busy={form.submitting}>
      <form onSubmit={form.handleSubmit} noValidate>
        <FormErrorSummary errors={form.formErrors.filter((e) => e !== 'Chưa chọn người dùng')} />
        {fixedUser || picked ? (
          <FormField label="Người dùng">
            <div className="row between">
              <b>{picked?.email}</b>
              {!fixedUser && <button type="button" className="btn btn-ghost btn-sm" onClick={() => setPicked(null)}>Đổi người dùng</button>}
            </div>
          </FormField>
        ) : (
          <FormField label="Người dùng" required htmlFor="grant-user" error={pickError ?? undefined} help="Gõ ít nhất 2 ký tự của email hoặc tên">
            <TextInput id="grant-user" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Tìm theo email" invalid={!!pickError} />
            {candidates.data?.items.length ? (
              <div className="card" style={{ padding: 6, marginTop: 6 }}>
                {candidates.data.items.map((u) => (
                  <button type="button" key={u.id} className="btn btn-ghost" style={{ width: '100%', justifyContent: 'space-between' }} onClick={() => setPicked({ id: u.id, email: u.email })}>
                    <span>{u.email}</span><span className="muted">{u.name ?? u.username}</span>
                  </button>
                ))}
              </div>
            ) : debounced.length >= 2 && !candidates.isFetching ? <span className="help">Không tìm thấy người dùng phù hợp</span> : null}
          </FormField>
        )}
        <FormField label="Số ngày" required htmlFor="grant-days" error={form.errors.days} help={`Tối đa ${maxDays} ngày mỗi lần. Nếu đang còn Premium thì cộng dồn vào hạn hiện tại.`}>
          <NumberInput id="grant-days" min={1} max={maxDays} step={1} invalid={!!form.errors.days} value={form.values.days} onChange={(e) => form.setValue('days', e.target.value)} />
        </FormField>
        <FormField label="Lý do" required htmlFor="grant-reason" error={form.errors.reason} help="Được ghi vào nhật ký quản trị.">
          <Textarea id="grant-reason" invalid={!!form.errors.reason} value={form.values.reason} onChange={(e) => form.setValue('reason', e.target.value)} />
        </FormField>
        <FormField label="Nhập lại mật khẩu của bạn" required htmlFor="grant-pass" error={form.errors.password}>
          <PasswordInput id="grant-pass" invalid={!!form.errors.password} value={form.values.password} onChange={(e) => form.setValue('password', e.target.value)} />
        </FormField>
        <FormActions onCancel={onClose} submitting={form.submitting} submitLabel="Cấp Premium" />
      </form>
    </Overlay>
  );
}
