import React, { useEffect, useState, useMemo } from 'react';
import {
  Search,
  UserPlus,
  MoreVertical,
  KeyRound,
  Trash2,
  Edit,
  Eye,
  Shield,
  Activity,
  Flame,
  Scale,
  X,
  Check,
  RefreshCw,
} from 'lucide-react';
import { usersApi, CreateUserPayload, UpdateUserPayload } from '../api/users.api';
import { User, Role } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDateOnly, formatNumber, formatCalories } from '../utils/formatters';
import { toast } from 'sonner';

export const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Modals state
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isResetPassOpen, setIsResetPassOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [isDetailDrawerOpen, setIsDetailDrawerOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  // Form states
  const [createForm, setCreateForm] = useState<CreateUserPayload>({
    username: '',
    email: '',
    password: '',
    name: '',
    role: 'USER',
    isActive: true,
    dailyAiQuota: 10,
  });

  const [editForm, setEditForm] = useState<UpdateUserPayload>({});
  const [newPassword, setNewPassword] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const res = await usersApi.getUsers();
      setUsers(res.data);
    } catch (err: any) {
      toast.error('Lỗi khi tải danh sách người dùng');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  // Filtered users
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchSearch =
        u.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (u.email && u.email.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (u.name && u.name.toLowerCase().includes(searchTerm.toLowerCase()));

      const matchRole = roleFilter === 'ALL' || u.role === roleFilter;
      const matchStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && u.isActive) ||
        (statusFilter === 'INACTIVE' && !u.isActive);

      return matchSearch && matchRole && matchStatus;
    });
  }, [users, searchTerm, roleFilter, statusFilter]);

  // Create User Handler
  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!createForm.username || !createForm.password) {
      toast.error('Vui lòng điền đầy đủ tên đăng nhập và mật khẩu');
      return;
    }
    setActionLoading(true);
    try {
      await usersApi.createUser(createForm);
      toast.success(`Đã tạo thành công tài khoản ${createForm.username}`);
      setIsCreateOpen(false);
      setCreateForm({
        username: '',
        email: '',
        password: '',
        name: '',
        role: 'USER',
        isActive: true,
        dailyAiQuota: 10,
      });
      fetchUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi tạo người dùng');
    } finally {
      setActionLoading(false);
    }
  };

  // Edit User Handler
  const handleEditUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser) return;
    setActionLoading(true);
    try {
      await usersApi.updateUser(selectedUser.id, editForm);
      toast.success('Cập nhật người dùng thành công');
      setIsEditOpen(false);
      fetchUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi cập nhật người dùng');
    } finally {
      setActionLoading(false);
    }
  };

  // Reset Password Handler
  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser || !newPassword) return;
    if (newPassword.length < 6) {
      toast.error('Mật khẩu mới tối thiểu 6 ký tự');
      return;
    }
    setActionLoading(true);
    try {
      await usersApi.resetPassword(selectedUser.id, newPassword);
      toast.success(`Đã đổi mật khẩu cho user ${selectedUser.username}`);
      setIsResetPassOpen(false);
      setNewPassword('');
    } catch (err: any) {
      toast.error(err.message || 'Lỗi đặt lại mật khẩu');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete User Handler
  const handleDeleteUser = async () => {
    if (!selectedUser) return;
    setActionLoading(true);
    try {
      await usersApi.deleteUser(selectedUser.id);
      toast.success(`Đã xóa tài khoản ${selectedUser.username}`);
      setIsDeleteOpen(false);
      fetchUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi xóa người dùng');
    } finally {
      setActionLoading(false);
    }
  };

  // Calculate Health Metrics
  const calculateBMI = (weight?: number | null, height?: number | null) => {
    if (!weight || !height) return '—';
    const hMeter = height / 100;
    const bmi = weight / (hMeter * hMeter);
    return bmi.toFixed(1);
  };

  const calculateBMR = (weight?: number | null, height?: number | null, gender?: string | null) => {
    if (!weight || !height) return '—';
    // Mifflin-St Jeor
    const base = 10 * weight + 6.25 * height - 5 * 25;
    return Math.round(gender === 'FEMALE' ? base - 161 : base + 5);
  };

  return (
    <div className="space-y-6">
      {/* Top Header & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Quản lý Người dùng
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Tổng cộng {users.length} tài khoản trong hệ thống CalAI
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="secondary"
            size="sm"
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
            onClick={fetchUsers}
          >
            Làm mới
          </Button>
          <Button
            variant="primary"
            size="sm"
            leftIcon={<UserPlus className="w-4 h-4" />}
            onClick={() => setIsCreateOpen(true)}
          >
            Tạo người dùng
          </Button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 rounded-2xl glass-panel flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo username, họ tên, email..."
            className="w-full bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl pl-9 pr-3.5 py-2 text-xs text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-emerald-500"
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="ALL">Mọi Vai trò (Roles)</option>
            <option value="USER">Thành viên (USER)</option>
            <option value="ADMIN">Quản trị (ADMIN)</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="ALL">Mọi Trạng thái</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="INACTIVE">Bị khóa (Inactive)</option>
          </select>
        </div>
      </div>

      {/* Data Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3.5">Người dùng</th>
                <th className="px-5 py-3.5">Email</th>
                <th className="px-5 py-3.5">Vai trò</th>
                <th className="px-5 py-3.5">Trạng thái</th>
                <th className="px-5 py-3.5">Quota AI</th>
                <th className="px-5 py-3.5">Ngày tham gia</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={7}>
                    <EmptyState
                      title="Không tìm thấy người dùng"
                      description="Hãy thử đổi từ khóa tìm kiếm hoặc bỏ bớt bộ lọc."
                    />
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr
                    key={user.id}
                    className="hover:bg-slate-50/50 dark:hover:bg-white/[0.02] transition-colors"
                  >
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-3">
                        <img
                          src={
                            user.avatar ||
                            `https://api.dicebear.com/7.x/initials/svg?seed=${user.username}`
                          }
                          alt={user.username}
                          className="w-9 h-9 rounded-full object-cover ring-2 ring-slate-100 dark:ring-white/10 shrink-0"
                        />
                        <div>
                          <p className="font-bold text-slate-900 dark:text-white">
                            {user.name || user.username}
                          </p>
                          <p className="text-[11px] text-slate-400 font-mono">@{user.username}</p>
                        </div>
                      </div>
                    </td>

                    <td className="px-5 py-3.5 text-slate-600 dark:text-slate-300">
                      {user.email || '—'}
                    </td>

                    <td className="px-5 py-3.5">
                      <Badge variant={user.role === 'ADMIN' ? 'purple' : 'slate'} dot={user.role === 'ADMIN'}>
                        {user.role}
                      </Badge>
                    </td>

                    <td className="px-5 py-3.5">
                      <Badge variant={user.isActive ? 'emerald' : 'rose'} dot>
                        {user.isActive ? 'Hoạt động' : 'Đã khóa'}
                      </Badge>
                    </td>

                    <td className="px-5 py-3.5">
                      <div className="font-semibold text-slate-800 dark:text-slate-200">
                        {user.dailyAiQuota} <span className="text-[10px] text-slate-400">/ngày</span>
                      </div>
                      {(user.purchasedAiQuota > 0 || user.purchasedChatQuota > 0) && (
                        <div className="text-[10px] text-emerald-500 font-medium">
                          +{user.purchasedAiQuota + user.purchasedChatQuota} trả phí
                        </div>
                      )}
                    </td>

                    <td className="px-5 py-3.5 text-slate-500">
                      {formatDateOnly(user.createdAt)}
                    </td>

                    <td className="px-5 py-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setIsDetailDrawerOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-emerald-500 hover:bg-emerald-500/10 transition-colors"
                          title="Xem chi tiết hồ sơ & chỉ số"
                        >
                          <Eye className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setEditForm({
                              name: user.name || '',
                              email: user.email || '',
                              role: user.role,
                              isActive: user.isActive,
                              dailyAiQuota: user.dailyAiQuota,
                              targetCalories: user.targetCalories || undefined,
                              weightKg: user.weightKg || undefined,
                              heightCm: user.heightCm || undefined,
                            });
                            setIsEditOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-500 hover:bg-cyan-500/10 transition-colors"
                          title="Sửa tài khoản"
                        >
                          <Edit className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setIsResetPassOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-amber-500 hover:bg-amber-500/10 transition-colors"
                          title="Đặt lại mật khẩu"
                        >
                          <KeyRound className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setIsDeleteOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
                          title="Xóa người dùng"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* MODAL 1: CREATE USER */}
      <Modal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        title="Tạo Người dùng Mới"
        description="Thêm tài khoản mới trực tiếp vào cơ sở dữ liệu CalAI"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsCreateOpen(false)}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleCreateUser} isLoading={actionLoading}>
              Xác nhận tạo
            </Button>
          </>
        }
      >
        <form className="space-y-3.5">
          <Input
            label="Tên đăng nhập (Username)"
            value={createForm.username}
            onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
            placeholder="vd: duc_fitness"
            required
          />
          <Input
            label="Họ và tên hiển thị"
            value={createForm.name}
            onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
            placeholder="vd: Nguyễn Văn A"
          />
          <Input
            label="Email"
            type="email"
            value={createForm.email}
            onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
            placeholder="vd: duc@example.com"
          />
          <Input
            label="Mật khẩu khởi tạo"
            type="password"
            value={createForm.password}
            onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
            placeholder="Tối thiểu 6 ký tự"
            required
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Vai trò (Role)"
              value={createForm.role}
              onChange={(e) => setCreateForm({ ...createForm, role: e.target.value as Role })}
              options={[
                { value: 'USER', label: 'USER (Người dùng thường)' },
                { value: 'ADMIN', label: 'ADMIN (Quản trị viên)' },
              ]}
            />
            <Input
              label="Quota AI miễn phí / ngày"
              type="number"
              value={createForm.dailyAiQuota}
              onChange={(e) => setCreateForm({ ...createForm, dailyAiQuota: Number(e.target.value) })}
            />
          </div>
        </form>
      </Modal>

      {/* MODAL 2: EDIT USER */}
      <Modal
        isOpen={isEditOpen}
        onClose={() => setIsEditOpen(false)}
        title={`Chỉnh sửa: @${selectedUser?.username}`}
        description="Cập nhật vai trò, hạn ngạch AI và thông số cơ thể"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsEditOpen(false)}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleEditUser} isLoading={actionLoading}>
              Lưu thay đổi
            </Button>
          </>
        }
      >
        <form className="space-y-3.5">
          <Input
            label="Họ và tên"
            value={editForm.name || ''}
            onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
          />
          <Input
            label="Email"
            type="email"
            value={editForm.email || ''}
            onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Vai trò"
              value={editForm.role}
              onChange={(e) => setEditForm({ ...editForm, role: e.target.value as Role })}
              options={[
                { value: 'USER', label: 'USER' },
                { value: 'ADMIN', label: 'ADMIN' },
              ]}
            />
            <Select
              label="Trạng thái tài khoản"
              value={editForm.isActive ? 'true' : 'false'}
              onChange={(e) => setEditForm({ ...editForm, isActive: e.target.value === 'true' })}
              options={[
                { value: 'true', label: 'Kích hoạt (Active)' },
                { value: 'false', label: 'Khóa (Inactive)' },
              ]}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Daily AI Quota"
              type="number"
              value={editForm.dailyAiQuota || 0}
              onChange={(e) => setEditForm({ ...editForm, dailyAiQuota: Number(e.target.value) })}
            />
            <Input
              label="Target Calo (kcal)"
              type="number"
              value={editForm.targetCalories || 0}
              onChange={(e) => setEditForm({ ...editForm, targetCalories: Number(e.target.value) })}
            />
          </div>
        </form>
      </Modal>

      {/* MODAL 3: RESET PASSWORD */}
      <Modal
        isOpen={isResetPassOpen}
        onClose={() => setIsResetPassOpen(false)}
        title="Đặt lại Mật khẩu"
        description={`Cập nhật mật khẩu bảo mật mới cho user @${selectedUser?.username}`}
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsResetPassOpen(false)}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleResetPassword} isLoading={actionLoading}>
              Xác nhận đổi mật khẩu
            </Button>
          </>
        }
      >
        <div className="space-y-3">
          <Input
            label="Mật khẩu mới"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="Nhập mật khẩu an toàn mới..."
            required
          />
          <p className="text-[11px] text-slate-500">
            Mật khẩu mới sẽ được mã hóa an toàn qua Bcrypt tại máy chủ backend.
          </p>
        </div>
      </Modal>

      {/* MODAL 4: DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDeleteUser}
        title="Xóa người dùng"
        message={`Bạn có chắc chắn muốn xóa vĩnh viễn tài khoản @${selectedUser?.username}? Tất cả bữa ăn, bài tập, cân nặng và nhật ký AI liên quan sẽ bị xóa hoàn toàn.`}
        isLoading={actionLoading}
      />

      {/* SLIDE-OVER DRAWER: USER DETAIL & BODY METRICS */}
      {isDetailDrawerOpen && selectedUser && (
        <div className="fixed inset-0 z-50 overflow-hidden animate-fade-in">
          <div
            className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity"
            onClick={() => setIsDetailDrawerOpen(false)}
          />
          <div className="fixed inset-y-0 right-0 max-w-md w-full bg-white dark:bg-[#111827] border-l border-slate-200 dark:border-white/10 shadow-2xl p-6 overflow-y-auto flex flex-col justify-between animate-slide-up">
            <div className="space-y-6">
              {/* Drawer Header */}
              <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-white/10">
                <div className="flex items-center gap-3">
                  <img
                    src={
                      selectedUser.avatar ||
                      `https://api.dicebear.com/7.x/initials/svg?seed=${selectedUser.username}`
                    }
                    alt={selectedUser.username}
                    className="w-12 h-12 rounded-full object-cover ring-2 ring-emerald-500/30"
                  />
                  <div>
                    <h3 className="text-base font-bold text-slate-900 dark:text-white">
                      {selectedUser.name || selectedUser.username}
                    </h3>
                    <p className="text-xs text-slate-400 font-mono">@{selectedUser.username}</p>
                  </div>
                </div>
                <button
                  onClick={() => setIsDetailDrawerOpen(false)}
                  className="p-2 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/5"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Body Stats & Calculator Cards */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Chỉ số Thể trạng & Trao đổi chất
                </h4>
                <div className="grid grid-cols-3 gap-2.5">
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 text-center">
                    <p className="text-[10px] text-slate-400 font-semibold uppercase">BMI</p>
                    <p className="text-lg font-bold text-emerald-500">
                      {calculateBMI(selectedUser.weightKg, selectedUser.heightCm)}
                    </p>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 text-center">
                    <p className="text-[10px] text-slate-400 font-semibold uppercase">BMR</p>
                    <p className="text-lg font-bold text-cyan-500">
                      {calculateBMR(selectedUser.weightKg, selectedUser.heightCm, selectedUser.gender)}
                    </p>
                    <span className="text-[9px] text-slate-500">kcal/ngày</span>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 text-center">
                    <p className="text-[10px] text-slate-400 font-semibold uppercase">TDEE Target</p>
                    <p className="text-lg font-bold text-amber-500">
                      {selectedUser.targetCalories || 2000}
                    </p>
                    <span className="text-[9px] text-slate-500">kcal</span>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-2.5 pt-2">
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5">
                    <span className="text-[11px] text-slate-400">Cân nặng hiện tại:</span>
                    <p className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                      {selectedUser.weightKg ? `${selectedUser.weightKg} kg` : 'Chưa cập nhật'}
                    </p>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5">
                    <span className="text-[11px] text-slate-400">Cân nặng mục tiêu:</span>
                    <p className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                      {selectedUser.targetWeightKg ? `${selectedUser.targetWeightKg} kg` : '—'}
                    </p>
                  </div>
                </div>
              </div>

              {/* Macro Goals */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Mục tiêu Macro Hàng ngày
                </h4>
                <div className="p-4 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 space-y-2.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-emerald-500 font-semibold">Protein (Đạm)</span>
                    <span className="font-bold">{selectedUser.targetProtein || 140}g</span>
                  </div>
                  <div className="flex justify-between text-xs">
                    <span className="text-cyan-500 font-semibold">Carbs (Tinh bột)</span>
                    <span className="font-bold">{selectedUser.targetCarb || 200}g</span>
                  </div>
                  <div className="flex justify-between text-xs">
                    <span className="text-amber-500 font-semibold">Fat (Chất béo)</span>
                    <span className="font-bold">{selectedUser.targetFat || 60}g</span>
                  </div>
                </div>
              </div>

              {/* Activity Summary Counters */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Hoạt động đã ghi nhận
                </h4>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5">
                    <span className="text-slate-400">Số bữa ăn đã log:</span>
                    <p className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
                      {selectedUser._count?.meals || 0} bữa
                    </p>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5">
                    <span className="text-slate-400">Số buổi tập luyện:</span>
                    <p className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
                      {selectedUser._count?.workoutLogs || 0} buổi
                    </p>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5">
                    <span className="text-slate-400">Số lần cân nặng:</span>
                    <p className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
                      {selectedUser._count?.weightLogs || 0} lần
                    </p>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5">
                    <span className="text-slate-400">Hội thoại AI Coach:</span>
                    <p className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
                      {selectedUser._count?.aiMessages || 0} tin nhắn
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="pt-6 border-t border-slate-100 dark:border-white/10">
              <Button
                variant="outline"
                className="w-full"
                onClick={() => setIsDetailDrawerOpen(false)}
              >
                Đóng ngăn kéo
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
