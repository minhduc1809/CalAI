# NutriWise Admin (web quản trị)

SPA React + Vite + TypeScript, gọi nhóm `admin/*` của cùng backend NestJS.

## Chạy thử
```
npm install
npm run dev          # http://localhost:5173, proxy /api tới http://localhost:3000
```
Đổi địa chỉ backend khi dev: `VITE_DEV_API=http://host:port npm run dev`.

## Triển khai
```
VITE_API_URL=https://api.ten-mien.vn npm run build   # thư mục dist/ là file tĩnh
```
- Deploy tách khỏi app, tên miền riêng, chỉ HTTPS.
- Backend cần cho phép CORS cho tên miền quản trị (hiện đang `origin: '*'`, nên siết lại trước khi phát hành).
- Tài khoản ADMIN tạo bằng seed/script, không có đăng ký qua API.

## Màn hình
Tổng quan, Người dùng, Thanh toán QR (duyệt đơn), Cấp Premium, Kho món, Kho bài tập, Duyệt món, Theo dõi AI, Tham số hệ thống, Nhật ký quản trị, Bảo mật (TOTP).
Thời gian hiển thị theo giờ Việt Nam. Tự đăng xuất sau 30 phút không thao tác. Token chỉ giữ trong sessionStorage.

## Chưa có
Refresh token bằng cookie HttpOnly.
