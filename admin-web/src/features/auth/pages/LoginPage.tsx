import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useSyncExternalStore } from 'react';
import { FormField, PasswordInput, TextInput } from '../../../components/forms/controls';
import { FormErrorSummary } from '../../../components/forms/FormParts';
import { Button } from '../../../components/ui/Button';
import { AppIcon } from '../../../components/ui/icons';
import { useForm } from '../../../hooks/useForm';
import { ApiError } from '../../../services/api-client';
import { authStore } from '../../../services/auth';
import { authApi } from '../api/auth.api';

interface Values extends Record<string, unknown> { email: string; password: string; totpCode: string }

export function LoginPage() {
  const authed = useSyncExternalStore(authStore.subscribe, authStore.isAuthenticated);
  const nav = useNavigate();
  const loc = useLocation();
  const [needTotp, setNeedTotp] = useState(false);

  const form = useForm<Values>({
    initial: { email: '', password: '', totpCode: '' },
    validate: (v) => ({
      email: /^\S+@\S+\.\S+$/.test(v.email) ? undefined : 'Nhập email đúng định dạng',
      password: v.password ? undefined : 'Nhập mật khẩu',
      totpCode: needTotp && !v.totpCode.trim() ? 'Nhập mã xác thực hai lớp' : undefined,
    }),
    onSubmit: async (v) => {
      try {
        const r = await authApi.login(v.email.trim(), v.password, v.totpCode.trim() || undefined);
        authStore.login(r.accessToken, r.admin);
      } catch (e) {
        // Tài khoản đã bật xác thực hai lớp: hiện ô nhập mã rồi gửi lại, không coi là lỗi
        if (e instanceof ApiError && e.code === 'TOTP_REQUIRED') {
          setNeedTotp(true);
          throw new ApiError('Nhập mã xác thực hai lớp để tiếp tục', 401, e.code);
        }
        throw e;
      }
    },
    onSuccess: () => nav((loc.state as { from?: string } | null)?.from ?? '/', { replace: true }),
  });

  if (authed) return <Navigate to="/" replace />;

  return (
    <div className="login-page">
      <aside className="login-brand" aria-hidden="true">
        <div className="login-brand-logo"><AppIcon name="Activity" size={26} /></div>
        <h2>NutriWise</h2>
        <p>Trung tâm quản trị dinh dưỡng và luyện tập: theo dõi người dùng, duyệt thanh toán, quản lý kho món và bài tập.</p>
        <ul>
          <li><AppIcon name="Security" size={18} /> Thao tác nhạy cảm cần xác nhận mật khẩu</li>
          <li><AppIcon name="Activity" size={18} /> Mọi thay đổi được ghi nhật ký kiểm toán</li>
          <li><AppIcon name="Lock" size={18} /> Hỗ trợ xác thực hai lớp</li>
        </ul>
      </aside>
      <main className="login-panel">
        <form className="login-card" onSubmit={form.handleSubmit} noValidate>
          <h1>Đăng nhập</h1>
          <p className="muted login-sub">Dùng tài khoản quản trị để tiếp tục</p>
          <FormErrorSummary errors={form.formErrors} />
          <FormField label="Email" required htmlFor="login-email" error={form.errors.email}>
            <TextInput id="login-email" type="email" autoComplete="username" placeholder="admin@example.com" autoFocus invalid={!!form.errors.email} value={form.values.email} onChange={(e) => form.setValue('email', e.target.value)} />
          </FormField>
          <FormField label="Mật khẩu" required htmlFor="login-pass" error={form.errors.password}>
            <PasswordInput id="login-pass" autoComplete="current-password" invalid={!!form.errors.password} value={form.values.password} onChange={(e) => form.setValue('password', e.target.value)} />
          </FormField>
          {needTotp && (
            <FormField label="Mã xác thực hai lớp (6 số) hoặc mã dự phòng" required htmlFor="login-totp" error={form.errors.totpCode}>
              <TextInput id="login-totp" inputMode="numeric" autoComplete="one-time-code" autoFocus invalid={!!form.errors.totpCode} value={form.values.totpCode} onChange={(e) => form.setValue('totpCode', e.target.value)} />
            </FormField>
          )}
          <Button variant="primary" type="submit" loading={form.submitting} className="login-submit">Đăng nhập</Button>
          <p className="muted login-foot">Quên mật khẩu? Liên hệ quản trị viên cấp cao để được đặt lại.</p>
        </form>
      </main>
    </div>
  );
}
