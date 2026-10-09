import React, { useEffect, useState, useMemo } from 'react';
import {
  Bot,
  Cpu,
  DollarSign,
  Search,
  MessageSquare,
  Sparkles,
  User,
  ArrowRight,
  Clock,
  Layers,
} from 'lucide-react';
import { statsApi } from '../api/stats.api';
import { ApiUsageLog, AiMessage } from '../types';
import { Badge } from '../components/ui/Badge';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDate, formatCurrencyUsd, formatNumber } from '../utils/formatters';
import { toast } from 'sonner';

export const AiLogsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'usage' | 'messages'>('usage');
  const [usageLogs, setUsageLogs] = useState<ApiUsageLog[]>([]);
  const [aiMessages, setAiMessages] = useState<AiMessage[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [featureFilter, setFeatureFilter] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [usageRes, msgRes] = await Promise.all([
        statsApi.getAiUsageLogs(),
        statsApi.getAiMessages(),
      ]);
      setUsageLogs(usageRes.data);
      setAiMessages(msgRes.data);
    } catch {
      toast.error('Lỗi khi tải nhật ký AI');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const filteredUsage = useMemo(() => {
    return usageLogs.filter((log) => {
      const matchFeature = featureFilter === 'ALL' || log.feature === featureFilter;
      const matchSearch =
        !searchTerm ||
        log.feature.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (log.user?.username && log.user.username.toLowerCase().includes(searchTerm.toLowerCase()));
      return matchFeature && matchSearch;
    });
  }, [usageLogs, featureFilter, searchTerm]);

  const filteredMessages = useMemo(() => {
    return aiMessages.filter((msg) => {
      return (
        !searchTerm ||
        msg.content.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (msg.user?.username && msg.user.username.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    });
  }, [aiMessages, searchTerm]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Audit Logs & Lịch sử Trợ lý AI
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Giám sát lưu lượng tiêu thụ tokens, chi phí thực tế và lịch sử hội thoại của người dùng
          </p>
        </div>
      </div>

      {/* Tabs & Search */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-2 p-1.5 rounded-2xl bg-slate-100 dark:bg-slate-900/60 border border-slate-200/60 dark:border-white/5 w-fit">
          <button
            onClick={() => setActiveTab('usage')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'usage'
                ? 'bg-white dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 shadow-sm border border-slate-200/60 dark:border-emerald-500/30'
                : 'text-slate-500 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <Cpu className="w-4 h-4" />
            <span>API Usage Logs (Tokens & Cost)</span>
          </button>

          <button
            onClick={() => setActiveTab('messages')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'messages'
                ? 'bg-white dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 shadow-sm border border-slate-200/60 dark:border-emerald-500/30'
                : 'text-slate-500 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <MessageSquare className="w-4 h-4" />
            <span>AI Chat Messages History</span>
          </button>
        </div>

        <div className="flex items-center gap-3">
          {activeTab === 'usage' && (
            <select
              value={featureFilter}
              onChange={(e) => setFeatureFilter(e.target.value)}
              className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
            >
              <option value="ALL">Mọi Tính năng AI</option>
              <option value="ai_scan">ai_scan (Quét ảnh món ăn)</option>
              <option value="ai_chat">ai_chat (Trợ lý huấn luyện)</option>
              <option value="recommend_meal">recommend_meal (Gợi ý bữa ăn)</option>
            </select>
          )}

          <div className="relative w-full sm:w-64">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Tìm kiếm nội dung / username..."
              className="w-full bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl pl-9 pr-3.5 py-2 text-xs text-slate-900 dark:text-white focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>
      </div>

      {/* TAB 1: USAGE LOGS TABLE */}
      {activeTab === 'usage' && (
        <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
                <tr>
                  <th className="px-5 py-3.5">Người dùng</th>
                  <th className="px-5 py-3.5">Tính năng</th>
                  <th className="px-5 py-3.5">Prompt Tokens</th>
                  <th className="px-5 py-3.5">Output Tokens</th>
                  <th className="px-5 py-3.5">Tổng Tokens</th>
                  <th className="px-5 py-3.5">Chi phí (USD)</th>
                  <th className="px-5 py-3.5 text-right">Thời gian</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-white/5">
                {filteredUsage.length === 0 ? (
                  <tr>
                    <td colSpan={7}>
                      <EmptyState title="Không có bản ghi log nào" />
                    </td>
                  </tr>
                ) : (
                  filteredUsage.map((log) => (
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
                        <Badge
                          variant={
                            log.feature === 'ai_scan'
                              ? 'emerald'
                              : log.feature === 'ai_chat'
                              ? 'cyan'
                              : 'purple'
                          }
                        >
                          {log.feature}
                        </Badge>
                      </td>

                      <td className="px-5 py-3.5 text-slate-600 dark:text-slate-300 font-mono">
                        {formatNumber(log.promptTokens)}
                      </td>

                      <td className="px-5 py-3.5 text-slate-600 dark:text-slate-300 font-mono">
                        {formatNumber(log.outputTokens)}
                      </td>

                      <td className="px-5 py-3.5 font-bold text-slate-900 dark:text-white font-mono">
                        {formatNumber(log.promptTokens + log.outputTokens)}
                      </td>

                      <td className="px-5 py-3.5 font-bold text-emerald-500 font-mono">
                        {formatCurrencyUsd(log.costUsd)}
                      </td>

                      <td className="px-5 py-3.5 text-slate-500 text-right">
                        {formatDate(log.createdAt)}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: AI MESSAGES HISTORY */}
      {activeTab === 'messages' && (
        <div className="space-y-4">
          {filteredMessages.length === 0 ? (
            <EmptyState title="Chưa có tin nhắn AI nào" />
          ) : (
            filteredMessages.map((msg) => (
              <div
                key={msg.id}
                className={`p-4 rounded-2xl border transition-all ${
                  msg.sender === 'ASSISTANT'
                    ? 'bg-emerald-500/5 dark:bg-emerald-950/20 border-emerald-500/20 mr-12'
                    : 'bg-white dark:bg-[#111827] border-slate-200/80 dark:border-white/5 ml-12'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    {msg.sender === 'ASSISTANT' ? (
                      <div className="p-1 rounded-lg bg-emerald-500/20 text-emerald-400">
                        <Bot className="w-4 h-4" />
                      </div>
                    ) : (
                      <div className="p-1 rounded-lg bg-cyan-500/20 text-cyan-400">
                        <User className="w-4 h-4" />
                      </div>
                    )}
                    <span className="text-xs font-bold text-slate-900 dark:text-white">
                      {msg.sender === 'ASSISTANT' ? 'CalAI AI Coach' : `@${msg.user?.username || 'user'}`}
                    </span>
                    <Badge variant={msg.sender === 'ASSISTANT' ? 'emerald' : 'cyan'}>
                      {msg.sender}
                    </Badge>
                  </div>

                  <div className="flex items-center gap-3 text-[11px] text-slate-400">
                    {msg.tokensUsed && (
                      <span className="font-mono">{msg.tokensUsed} tokens</span>
                    )}
                    <span>{formatDate(msg.createdAt)}</span>
                  </div>
                </div>

                <p className="text-xs sm:text-sm text-slate-700 dark:text-slate-200 leading-relaxed pl-7">
                  {msg.content}
                </p>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};
