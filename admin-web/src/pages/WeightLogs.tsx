import React, { useEffect, useState, useMemo } from 'react';
import {
  Scale,
  Plus,
  Trash2,
  Edit,
  TrendingDown,
  LineChart as LineChartIcon,
} from 'lucide-react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import { weightLogsApi, CreateWeightLogPayload, UpdateWeightLogPayload } from '../api/weight-logs.api';
import { usersApi } from '../api/users.api';
import { WeightLog, User } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDateOnly } from '../utils/formatters';
import { toast } from 'sonner';

export const WeightLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<WeightLog[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Filters & Selected User for Chart
  const [selectedUserFilter, setSelectedUserFilter] = useState('ALL');

  // Modals
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedLog, setSelectedLog] = useState<WeightLog | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Form states
  const [formUserId, setFormUserId] = useState('');
  const [formWeight, setFormWeight] = useState(70.0);
  const [formDate, setFormDate] = useState(new Date().toISOString().substring(0, 10));
  const [formNotes, setFormNotes] = useState('');

  const fetchLogsAndUsers = async () => {
    setIsLoading(true);
    try {
      const [logsRes, usersRes] = await Promise.all([
        weightLogsApi.getWeightLogs(),
        usersApi.getUsers(),
      ]);
      setLogs(logsRes.data);
      setUsers(usersRes.data);
      if (usersRes.data.length > 0 && !formUserId) {
        setFormUserId(usersRes.data[0].id);
      }
    } catch {
      toast.error('Lỗi khi tải lịch sử cân nặng');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLogsAndUsers();
  }, []);

  const filteredLogs = useMemo(() => {
    return logs.filter((l) => selectedUserFilter === 'ALL' || l.userId === selectedUserFilter);
  }, [logs, selectedUserFilter]);

  // Chart data for selected user or first user
  const chartData = useMemo(() => {
    const targetUserId = selectedUserFilter !== 'ALL' ? selectedUserFilter : logs[0]?.userId;
    if (!targetUserId) return [];

    return logs
      .filter((l) => l.userId === targetUserId)
      .map((l) => ({
        date: formatDateOnly(l.date),
        weight: l.weightKg,
      }))
      .reverse();
  }, [logs, selectedUserFilter]);

  // Create Log
  const handleCreateLog = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formUserId || !formWeight) {
      toast.error('Vui lòng điền người dùng và cân nặng');
      return;
    }

    setActionLoading(true);
    try {
      await weightLogsApi.createWeightLog({
        userId: formUserId,
        weightKg: Number(formWeight),
        date: new Date(formDate).toISOString(),
        notes: formNotes,
      });
      toast.success('Thêm bản ghi cân nặng thành công');
      setIsCreateOpen(false);
      fetchLogsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi thêm cân nặng');
    } finally {
      setActionLoading(false);
    }
  };

  // Open Edit
  const openEdit = (log: WeightLog) => {
    setSelectedLog(log);
    setFormWeight(log.weightKg);
    setFormDate(new Date(log.date).toISOString().substring(0, 10));
    setFormNotes(log.notes || '');
    setIsEditOpen(true);
  };

  // Submit Edit
  const handleEditLog = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedLog) return;

    setActionLoading(true);
    try {
      await weightLogsApi.updateWeightLog(selectedLog.id, {
        weightKg: Number(formWeight),
        date: new Date(formDate).toISOString(),
        notes: formNotes,
      });
      toast.success('Cập nhật cân nặng thành công');
      setIsEditOpen(false);
      fetchLogsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi cập nhật cân nặng');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete
  const handleDeleteLog = async () => {
    if (!selectedLog) return;
    setActionLoading(true);
    try {
      await weightLogsApi.deleteWeightLog(selectedLog.id);
      toast.success('Đã xóa bản ghi cân nặng');
      setIsDeleteOpen(false);
      fetchLogsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi xóa cân nặng');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Lịch sử Cân nặng (Weight Logs)
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Theo dõi xu hướng tăng / giảm cân và tiến độ tiến tới thể trạng mục tiêu
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => {
            setFormWeight(70.0);
            setIsCreateOpen(true);
          }}
        >
          Ghi nhận cân nặng
        </Button>
      </div>

      {/* Filter Bar */}
      <div className="p-4 rounded-2xl glass-panel flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <label className="text-xs font-semibold text-slate-400">Chọn người dùng:</label>
          <select
            value={selectedUserFilter}
            onChange={(e) => setSelectedUserFilter(e.target.value)}
            className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="ALL">Toàn bộ Người dùng</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>
                {u.name || u.username} (@{u.username})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Chart Section */}
      {chartData.length > 0 && (
        <div className="p-6 rounded-3xl glass-panel">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">
                Biểu đồ Xu hướng Cân nặng (kg)
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Dữ liệu biến thiên theo thời gian của người dùng được chọn
              </p>
            </div>
            <div className="flex items-center gap-2 text-xs font-bold text-emerald-500 bg-emerald-500/10 px-3 py-1 rounded-full border border-emerald-500/20">
              <TrendingDown className="w-3.5 h-3.5" />
              <span>Tiến độ ổn định</span>
            </div>
          </div>

          <div className="h-60 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                <XAxis dataKey="date" stroke="#64748B" fontSize={11} tickLine={false} />
                <YAxis domain={['dataMin - 2', 'dataMax + 2']} stroke="#64748B" fontSize={11} tickLine={false} />
                <Tooltip
                  formatter={(val: any) => [`${val} kg`, 'Cân nặng']}
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    borderRadius: '12px',
                    border: '1px solid rgba(255,255,255,0.1)',
                    fontSize: '12px',
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="weight"
                  stroke="#10B981"
                  strokeWidth={3}
                  dot={{ r: 4, fill: '#10B981', strokeWidth: 2, stroke: '#fff' }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3.5">Người dùng</th>
                <th className="px-5 py-3.5">Cân nặng (kg)</th>
                <th className="px-5 py-3.5">Ngày ghi nhận</th>
                <th className="px-5 py-3.5">Ghi chú</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {filteredLogs.length === 0 ? (
                <tr>
                  <td colSpan={5}>
                    <EmptyState
                      title="Chưa có dữ liệu cân nặng"
                      description="Hãy bấm Thêm bản ghi để bắt đầu theo dõi."
                    />
                  </td>
                </tr>
              ) : (
                filteredLogs.map((log) => (
                  <tr
                    key={log.id}
                    className="hover:bg-slate-50/50 dark:hover:bg-white/[0.02] transition-colors"
                  >
                    <td className="px-5 py-3.5">
                      <p className="font-bold text-slate-900 dark:text-white">
                        {log.user?.name || log.user?.username || log.userId}
                      </p>
                      <p className="text-[11px] text-slate-400">@{log.user?.username || 'user'}</p>
                    </td>

                    <td className="px-5 py-3.5">
                      <span className="text-base font-extrabold text-emerald-600 dark:text-emerald-400">
                        {log.weightKg} kg
                      </span>
                    </td>

                    <td className="px-5 py-3.5 text-slate-500">
                      {formatDateOnly(log.date)}
                    </td>

                    <td className="px-5 py-3.5 text-slate-600 dark:text-slate-300">
                      {log.notes || '—'}
                    </td>

                    <td className="px-5 py-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openEdit(log)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-500 hover:bg-cyan-500/10 transition-colors"
                          title="Sửa cân nặng"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => {
                            setSelectedLog(log);
                            setIsDeleteOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
                          title="Xóa bản ghi"
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

      {/* CREATE & EDIT MODAL */}
      <Modal
        isOpen={isCreateOpen || isEditOpen}
        onClose={() => {
          setIsCreateOpen(false);
          setIsEditOpen(false);
        }}
        title={isCreateOpen ? 'Ghi nhận Cân nặng Mới' : 'Sửa Cân nặng'}
        maxWidth="sm"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => {
                setIsCreateOpen(false);
                setIsEditOpen(false);
              }}
            >
              Hủy
            </Button>
            <Button
              variant="primary"
              onClick={isCreateOpen ? handleCreateLog : handleEditLog}
              isLoading={actionLoading}
            >
              {isCreateOpen ? 'Lưu bản ghi' : 'Cập nhật'}
            </Button>
          </>
        }
      >
        <div className="space-y-3.5">
          {isCreateOpen && (
            <Select
              label="Người dùng"
              value={formUserId}
              onChange={(e) => setFormUserId(e.target.value)}
              options={users.map((u) => ({
                value: u.id,
                label: `${u.name || u.username} (@${u.username})`,
              }))}
            />
          )}

          <Input
            label="Cân nặng (kg)"
            type="number"
            step="0.1"
            value={formWeight}
            onChange={(e) => setFormWeight(Number(e.target.value))}
            required
          />

          <Input
            label="Ngày ghi nhận"
            type="date"
            value={formDate}
            onChange={(e) => setFormDate(e.target.value)}
          />

          <Input
            label="Ghi chú (Tùy chọn)"
            placeholder="vd: Cân sáng sau khi thức dậy..."
            value={formNotes}
            onChange={(e) => setFormNotes(e.target.value)}
          />
        </div>
      </Modal>

      {/* DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDeleteLog}
        title="Xóa bản ghi cân nặng"
        message="Bạn có chắc chắn muốn xóa bản ghi cân nặng này khỏi hệ thống?"
        isLoading={actionLoading}
      />
    </div>
  );
};
