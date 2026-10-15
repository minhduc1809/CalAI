import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev: gọi backend qua proxy để không vướng CORS. Production: đặt VITE_API_URL tới tên miền API (HTTPS).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: process.env.VITE_DEV_API ?? 'http://localhost:3000', changeOrigin: true },
    },
  },
});
