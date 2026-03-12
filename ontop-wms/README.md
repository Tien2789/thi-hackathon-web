# OnTop WMS Project

Đây là dự án Warehouse Management System (WMS) bao gồm cả Frontend và Backend.

## Cấu trúc thư mục
- `backend/`: Spring Boot Application (Java)
- `frontend/`: Vue.js Application (Vite/Vue 3)

---

## Hướng dẫn chạy hệ thống

### 1. Backend (Spring Boot)
Yêu cầu: **Java 21+** và **Apache Maven**.

1. **Cài đặt Maven (nếu chưa có)**:
   - Nếu bạn thấy lỗi `'mvn' is not recognized`, hãy tải Maven tại [maven.apache.org](https://maven.apache.org/download.cgi).
   - Giải nén và thêm thư mục `bin` của Maven vào biến môi trường **PATH** của Windows.
2. **Cấu hình Database**:
   - Đảm bảo MySQL đang chạy và có database tên `OnTop`.
   - Kiểm tra `backend/src/main/resources/application.yml` xem password (đã cập nhật thành `123456`) đã đúng chưa.
3. **Chạy Project**:
   Mở terminal tại thư mục `backend/` và chạy:
   ```powershell
   .\mvnw spring-boot:run
   ```
   Backend sẽ chạy tại: `http://localhost:8080/api`

### 2. Frontend (Vue 3 + Vite)
Yêu cầu: **Node.js 18+**.

1. **Cài đặt & Sửa lỗi `element-plus`**:
   Nếu bạn gặp lỗi `Failed to resolve entry for package "element-plus"`, hãy chạy các lệnh sau tại thư mục `frontend/`:
   ```powershell
   # Xóa cache và cài đặt lại hoàn toàn
   rm -Rf node_modules
   npm install
   ```
2. **Chạy Project**:
   ```powershell
   npm run dev
   ```
   Frontend sẽ chạy tại: `http://localhost:5173`

---

## Kết nối Frontend & Backend
- **Frontend** gọi API tại: `http://localhost:8080/api` (Cấu hình trong `frontend/src/api/index.js`).
- **Backend** cho phép CORS từ: `http://localhost:5173` và có prefix `/api` (Cấu hình trong `backend/src/main/resources/application.yml`).

## Xử lý sự cố thường gặp
- **Lỗi 403 (CORS)**: Kiểm tra `SecurityConfig.java` trong Backend xem port của Frontend (`5173`) đã đúng chưa.
- **Lỗi 'mvn' không nhận diện**: Đây là do Maven chưa được cài đặt hoặc chưa đặt PATH. Bạn có thể sử dụng IntelliJ IDEA để chạy trực tiếp project Spring Boot mà không cần lệnh terminal.
