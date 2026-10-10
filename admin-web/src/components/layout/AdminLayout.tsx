import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { Toaster } from 'sonner';

export const AdminLayout: React.FC = () => {
  const [isRefreshing, setIsRefreshing] = useState(false);

  useEffect(() => {
    const handleRefresh = () => {
      setIsRefreshing(true);
      setTimeout(() => setIsRefreshing(false), 800);
    };
    window.addEventListener('admin-refresh-data', handleRefresh);
    return () => window.removeEventListener('admin-refresh-data', handleRefresh);
  }, []);

  return (
    <div className="min-h-screen bg-[#0F172A] text-[#F8FAFC] flex transition-colors selection:bg-emerald-500/30 selection:text-emerald-300">
      <Sidebar />
      <div className="flex-1 ml-[240px] flex flex-col min-w-0 transition-all">
        <Header
          isRefreshing={isRefreshing}
          onRefresh={() => {
            window.dispatchEvent(new CustomEvent('admin-refresh-data'));
          }}
        />
        <main className="flex-1 p-6 md:p-8 max-w-7xl w-full mx-auto animate-fade-in">
          <Outlet />
        </main>
      </div>
      <Toaster
        position="top-right"
        richColors
        closeButton
        theme="dark"
      />
    </div>
  );
};
