import React from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { Toaster } from 'sonner';

export const AdminLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-[#0B0F17] flex transition-colors">
      <Sidebar />
      <div className="flex-1 ml-20 lg:ml-64 flex flex-col min-w-0 transition-all duration-300">
        <Header />
        <main className="flex-1 p-6 md:p-8 max-w-7xl w-full mx-auto animate-fade-in">
          <Outlet />
        </main>
      </div>
      <Toaster
        position="top-right"
        richColors
        closeButton
        theme="system"
      />
    </div>
  );
};
