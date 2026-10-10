import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Lock, Mail, Sparkles, ArrowRight, ShieldAlert, KeyRound } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Button } from '../components/ui/Button';
import { toast } from 'sonner';

export const Login: React.FC = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('admin@calai.com');
  const [password, setPassword] = useState('Admin@123456');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!usernameOrEmail.trim() || !password.trim()) {
      toast.error('Vui lòng nhập đầy đủ email và mật khẩu');
      return;
    }

    if (password.trim().length < 6) {
      toast.error('Mật khẩu quản trị phải có tối thiểu 6 ký tự');
      return;
    }

    setIsLoading(true);
    try {
      await login(usernameOrEmail.trim(), password);
      toast.success('Đăng nhập quản trị thành công! Chào mừng trở lại.');
      navigate('/dashboard');
    } catch (err: any) {
      toast.error(err.message || 'Email hoặc mật khẩu không chính xác');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFillDemo = () => {
    setUsernameOrEmail('admin@calai.com');
    setPassword('Admin@123456');
    toast.info('Đã điền tài khoản Quản trị viên mẫu');
  };

  return (
    <div className="min-h-screen w-full bg-[#0F172A] text-[#F8FAFC] flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background ambient lighting */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-tr from-emerald-600/15 via-teal-500/10 to-transparent rounded-full blur-[140px] pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-96 h-96 bg-emerald-500/5 rounded-full blur-[100px] pointer-events-none" />

      {/* Decorative Grid */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#33415515_1px,transparent_1px),linear-gradient(to_bottom,#33415515_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_50%,#000_70%,transparent_100%)] pointer-events-none" />

      {/* Login Card */}
      <div className="relative w-full max-w-md bg-[#1E293B]/90 backdrop-blur-2xl border border-[#334155] rounded-3xl p-8 sm:p-10 shadow-2xl z-10 animate-fade-in">
        {/* Brand Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-400 p-0.5 shadow-glow mb-4">
            <div className="w-full h-full bg-[#0F172A] rounded-[14px] flex items-center justify-center">
              <Sparkles className="w-7 h-7 text-emerald-400 animate-pulse" />
            </div>
          </div>
          <h1 className="text-2xl font-extrabold tracking-tight text-[#F8FAFC]">
            CalAI Admin Console
          </h1>
          <p className="text-xs text-[#94A3B8] mt-1.5">
            Cổng Vận hành Doanh thu & Quản trị Hệ thống (Phase 1)
          </p>
        </div>

        {/* Security Alert Notice */}
        <div className="mb-6 p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-start gap-2.5 text-[11px] text-amber-300">
          <ShieldAlert className="w-4 h-4 shrink-0 text-amber-400 mt-0.5" />
          <span>
            Bảo vệ Brute-force: Đăng nhập sai 5 lần liên tiếp sẽ bị khóa tài khoản tạm thời trong 15 phút.
          </span>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-[#F8FAFC] mb-1.5">
              Email Quản trị viên
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-[#94A3B8] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="text"
                value={usernameOrEmail}
                onChange={(e) => setUsernameOrEmail(e.target.value)}
                placeholder="admin@calai.com"
                className="w-full bg-[#0F172A] border border-[#334155] rounded-xl pl-10 pr-4 py-2.5 text-sm text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all font-mono"
                autoComplete="email"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#F8FAFC] mb-1.5">
              Mật khẩu
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 text-[#94A3B8] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-[#0F172A] border border-[#334155] rounded-xl pl-10 pr-11 py-2.5 text-sm text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all font-mono"
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#F8FAFC] transition-colors"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          <div className="pt-2">
            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full bg-[#10B981] hover:bg-[#059669] text-white font-bold py-2.5 rounded-xl shadow-glow transition-all"
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Đăng nhập Quản trị
            </Button>
          </div>
        </form>

        {/* Demo Credentials Helper */}
        <div className="mt-6 p-3.5 rounded-2xl bg-[#0F172A]/70 border border-[#334155] text-[11px] text-[#94A3B8] flex items-center justify-between">
          <div>
            <p className="text-[#F8FAFC] font-semibold">Tài khoản Admin đã kích hoạt:</p>
            <p className="font-mono text-emerald-400 text-xs mt-0.5">admin@calai.com / Admin@123456</p>
          </div>
          <button
            type="button"
            onClick={handleFillDemo}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-semibold transition-colors"
          >
            <KeyRound className="w-3.5 h-3.5" />
            <span>Điền mẫu</span>
          </button>
        </div>
      </div>
    </div>
  );
};
