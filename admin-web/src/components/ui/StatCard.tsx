import React from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  trend?: {
    value: string;
    isPositive?: boolean;
    label?: string;
  };
  highlight?: boolean;
  accentColor?: 'emerald' | 'cyan' | 'amber' | 'rose' | 'indigo' | 'purple';
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  trend,
  highlight = false,
  accentColor = 'emerald',
}) => {
  const accentClasses = {
    emerald: 'from-emerald-500/10 to-transparent text-emerald-500 border-emerald-500/20',
    cyan: 'from-cyan-500/10 to-transparent text-cyan-500 border-cyan-500/20',
    amber: 'from-amber-500/10 to-transparent text-amber-500 border-amber-500/20',
    rose: 'from-rose-500/10 to-transparent text-rose-500 border-rose-500/20',
    indigo: 'from-indigo-500/10 to-transparent text-indigo-500 border-indigo-500/20',
    purple: 'from-purple-500/10 to-transparent text-purple-500 border-purple-500/20',
  };

  return (
    <div
      className={`relative p-5 rounded-2xl border transition-all duration-300 hover:translate-y-[-2px] bg-white dark:bg-[#111827]/80 backdrop-blur-md ${
        highlight
          ? 'border-emerald-500/40 shadow-glow'
          : 'border-slate-200/80 dark:border-white/5 hover:border-slate-300 dark:hover:border-white/10 shadow-sm'
      }`}
    >
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
          {title}
        </span>
        <div
          className={`p-2.5 rounded-xl border bg-gradient-to-br ${accentClasses[accentColor]}`}
        >
          {icon}
        </div>
      </div>

      <div className="mt-4 flex items-baseline gap-2">
        <h4 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          {value}
        </h4>
        {subtitle && (
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {subtitle}
          </span>
        )}
      </div>

      {trend && (
        <div className="mt-3 flex items-center gap-1.5 text-xs">
          <span
            className={`font-semibold ${
              trend.isPositive
                ? 'text-emerald-600 dark:text-emerald-400'
                : 'text-rose-600 dark:text-rose-400'
            }`}
          >
            {trend.value}
          </span>
          {trend.label && (
            <span className="text-slate-400 dark:text-slate-500">{trend.label}</span>
          )}
        </div>
      )}
    </div>
  );
};
