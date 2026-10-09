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

* **Tài khoản mặc định (Demo & Backend Admin)**:
  * **Tên đăng nhập / Username**: `admin`
  * **Mật khẩu / Password**: `admin123`

> 💡 **Cơ chế hoạt động**:
> * Khi Backend API đang chạy (`http://localhost:3000/api/v1`), hệ thống sẽ xác thực JWT token qua API `/auth/login` với quyền `ADMIN`.
> * Nếu Backend API chưa bật hoặc lỗi kết nối, hệ thống sẽ tự động kích hoạt **Chế độ Xem thử (Demo Preview Mode)** với bộ dữ liệu mẫu đầy đủ để bạn có thể xem và trải nghiệm trọn vẹn toàn bộ các màn hình giao diện.

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
   * Thống kê tổng số Users, Active Members, Premium Users.
   * Lượng Calo & Bữa ăn ghi nhận hôm nay, số lượt tập luyện.
   * Thống kê lượng Check-in cần duyệt, biểu đồ xu hướng AI API token usage.
2. **Quản lý Người dùng (`/users`)**:
   * Danh sách tài khoản, trạng thái Active / Banned.
   * Cập nhật thông tin, cấp/hủy quyền Admin, đặt lại mật khẩu, xóa tài khoản.
3. **Quản lý Dinh dưỡng & Bữa ăn (`/meals`)**:
   * Xem lịch sử nhật ký ăn uống của người dùng, phân tích chi tiết Calo/Carb/Protein/Fat.
4. **Quản lý Tập luyện (`/workouts`)**:
   * Xem nhật ký bài tập, calo tiêu hao, sets & reps.
5. **Duyệt Check-in Tiến độ (`/checkins`)**:
   * Danh sách check-in tuần của học viên/người dùng.
   * Duyệt / từ chối và gửi ghi chú nhận xét của Coach/Chuyên gia.
6. **Thư viện Thực phẩm & Mẫu (`/foods` & `/templates`)**:
   * Quản lý kho thực phẩm tùy chỉnh và thực đơn mẫu.
7. **Lịch sử & Giám sát AI (`/ai-logs`)**:
   * Theo dõi lượng token tiêu thụ của Chatbot AI và Camera AI Scan, chi phí API ước tính.
