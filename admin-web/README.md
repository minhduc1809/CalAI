# 🚀 CalAI Admin Web Portal

Bảng điều khiển Quản trị viên (Admin Dashboard) của hệ thống **CalAI - Trợ lý Dinh dưỡng & Thể hình AI**.

---

## 📌 1. Yêu cầu & Cài đặt

Ứng dụng sử dụng:
* **Node.js**: Phiên bản 18+ trở lên
* **Vite + React 19 + TypeScript + Tailwind CSS**
* **Lucide React, Recharts, Sonner**

Cài đặt thư viện (nếu chưa cài):
```bash
cd admin-web
npm install
```

---

## ⚡ 2. Khởi chạy Dashboard Admin

### Chạy chế độ Phát triển (Dev Mode):
```bash
npm run dev
```
Mặc định Vite sẽ khởi chạy tại:
👉 **`http://localhost:5173`** (hoặc cổng được hiển thị trên terminal).

*(Nếu muốn mở cho mạng nội bộ/LAN, chạy: `npm run dev -- --host`)*

---

## 🔑 3. Thông tin Đăng nhập Quản trị viên

Khi truy cập `http://localhost:5173`, hệ thống sẽ đưa bạn đến màn hình đăng nhập:

* Đăng nhập bằng email và mật khẩu của tài khoản có quyền `ADMIN` trong backend.
* Không có tài khoản demo hoặc chế độ mock. Nếu backend không khả dụng, trang sẽ hiển thị lỗi thay vì dữ liệu giả.
* Phiên đăng nhập được xác thực lại qua `GET /admin/auth/me`; khi access token hết hạn, hãy đăng nhập lại.

---

## 🌐 4. Cấu hình Kết nối API Backend

File cấu hình môi trường nằm tại `.env`:
```env
VITE_API_URL=http://localhost:3000/api/v1
```
Nếu Backend của bạn chạy ở port hoặc domain khác, chỉ cần cập nhật biến `VITE_API_URL` trong file `.env` và khởi động lại `npm run dev`.

---

## 📊 5. Các Chức năng Quản trị trên Dashboard

1. **Dashboard Tổng quan (`/dashboard`)**:
   * KPI người dùng, Premium, đơn thanh toán và tổng doanh thu lấy từ `GET /admin/dashboard`.
   * Đơn chờ và hoạt động kiểm toán lấy từ API tương ứng. Backend hiện chưa cung cấp dữ liệu doanh thu theo ngày nên không hiển thị biểu đồ xu hướng giả.
2. **Quản lý Người dùng (`/users`)**:
   * Tra cứu và xem thông tin thanh toán/gói cước; không hiển thị dữ liệu sức khỏe cá nhân.
3. **Đơn thanh toán (`/orders`)**:
   * Tìm kiếm, lọc, duyệt và từ chối đơn VietQR.
4. **Cấp gói Premium (`/grants`)**:
   * Cấp/thu hồi quyền Premium; tra cứu lịch sử cấp theo User ID qua `GET /admin/users/:id/billing`.
   * Backend hiện không có endpoint liệt kê tất cả manual grant trên toàn hệ thống.
5. **Nhật ký kiểm toán (`/audit-logs`)**:
   * Xem và lọc các thao tác quản trị do backend ghi nhận.

Các endpoint thao tác billing được gọi theo route backend hiện tại: `POST /admin/billing/grants` và `POST /admin/billing/grants/:id/revoke`.
