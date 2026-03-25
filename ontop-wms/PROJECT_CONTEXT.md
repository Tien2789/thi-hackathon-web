# ONTOP WMS - HỆ THỐNG CONTEXT ĐẦY ĐỦ

> **MỤC ĐÍCH**: File này là "bộ não" của dự án. Bất kỳ AI agent nào đọc file này đều sẽ hiểu toàn bộ kiến trúc, luồng dữ liệu, và có thể thực hiện yêu cầu của người dùng ngay lập tức.

> [!IMPORTANT]
> **HƯỚNG DẪN CHO AI AGENT**: Khi nhận yêu cầu từ người dùng, hãy:
> 1. Đọc file này trước để hiểu hệ thống
> 2. Xác định module nào bị ảnh hưởng (xem Section 4: API MAP)
> 3. Tìm đúng file backend (Controller → Service → Repository → Entity) và frontend (View) cần sửa
> 4. Thực hiện thay đổi
> 5. **Cập nhật lại file PROJECT_CONTEXT.md này** nếu có thêm/sửa/xóa API, Entity, hoặc View

---

## 1. TỔNG QUAN HỆ THỐNG

| Thành phần | Công nghệ | Chi tiết |
|---|---|---|
| **Backend** | Spring Boot 3.x + Java | Port `8080`, context-path `/api` |
| **Frontend** | Vue 3 + Vite + Element Plus | Port `5173` |
| **Database** | MySQL | JPA/Hibernate auto-manage |
| **Auth** | JWT Bearer Token | Stateless, BCrypt password |
| **UI Library** | Element Plus + Bootstrap 5 | Sidebar layout, RBAC menu |

### Cấu trúc thư mục

```
ontop-wms/
├── backend/src/main/java/com/ontop/wms/
│   ├── config/          # SecurityConfig, DataInitializer, GlobalExceptionHandler
│   ├── controller/      # 8 REST Controllers (Auth, Product, Inventory, Asset, Alert, Report, User, Warehouse)
│   ├── dto/             # Data Transfer Objects (LoginRequest, AuthResponse, CreateUserRequest, ...)
│   ├── entity/          # 15 JPA Entities
│   ├── repository/      # 17 JPA Repositories
│   ├── service/         # 5 Service interfaces + 5 implementations
│   └── security/        # JwtAuthenticationFilter, JwtTokenUtil
├── frontend/src/
│   ├── api/index.js     # Axios client (baseURL: http://localhost:8080/api)
│   ├── router/index.js  # Vue Router + Route Guard (RBAC)
│   ├── App.vue          # Main layout: Sidebar + Header + Content
│   └── views/           # 11 Vue components (pages)
└── PROJECT_CONTEXT.md   # ← BẠN ĐANG ĐỌC FILE NÀY
```

---

## 2. BACKEND CHI TIẾT

### 2.1 Entities (JPA) — Bảng Database

| Entity | Table | Các trường chính | Quan hệ |
|---|---|---|---|
| `Product` | `products` | id, skuCode, barcode, qrCode, productName, currentStock, minStock, createdAt, updatedAt | ManyToOne: Category, Unit, Supplier, BinLocation |
| `Category` | `categories` | id, name | — |
| `Unit` | (units) | id, name | — |
| `Supplier` | (suppliers) | id, name | — |
| `BinLocation` | (bin_locations) | id, ... | — |
| `Warehouse` | `warehouses` | id, code, name, location, manager, capacity | — |
| `User` | `users` | id, username, password, isActive | ManyToOne: Role, Warehouse |
| `Role` | `roles` | id, roleName | — (3 giá trị: ADMIN, MANAGER, STAFF) |
| `InventoryIn` | `inventory_in` | id, receiptCode, status, createdAt | ManyToOne: User(approvedBy), Warehouse. OneToMany: InDetail |
| `InventoryOut` | `inventory_out` | id, issueCode, status, createdAt | ManyToOne: User(approvedBy), Warehouse. OneToMany: OutDetail |
| `InDetail` | (in_details) | id, quantity | ManyToOne: InventoryIn, Product |
| `OutDetail` | (out_details) | id, quantity | ManyToOne: InventoryOut, Product |
| `Asset` | `assets` | id, assetCode, status, createdAt | ManyToOne: Product |
| `Notification` | (notifications) | id, message, isRead, ... | — |
| `StockLedger` | (stock_ledger) | id, ... | — |

### 2.2 Repositories

Mỗi Entity trên có 1 Repository JPA tương ứng trong `com.ontop.wms.repository`. Đáng chú ý:
- `NotificationRepository` → có method `findByIsReadFalse()`
- `OutDetailRepository` → có method `findByInventoryOut(InventoryOut)`
- `UserRepository` → có method `findByUsername(String)` với `@EntityGraph`

### 2.3 Services

| Service Interface | Implementation | Chức năng chính |
|---|---|---|
| `AuthService` | `AuthServiceImpl` | `login(LoginRequest)` → authenticate, generate JWT, return token+role+warehouseId |
| `ProductService` | `ProductServiceImpl` | `getAllProducts()`, `createProduct()`, `deleteProduct()` |
| `InventoryService` | `InventoryServiceImpl` | `getAllInbounds()`, `createInbound()`, `approveInbound()`, `getAllOutbounds()`, `createOutbound()`, `approveOutbound()`, `undoOutbound()` |
| `UserService` | `UserServiceImpl` | `getAllUsers()`, `getAllRoleNames()`, `createUser()`, `deleteUser()` |
| `AssetService` | (class trực tiếp) | `calculateDepreciation(assetId)` — công thức khấu hao đường thẳng 10%/năm |

### 2.4 Controllers — Chi tiết từng endpoint

File cấu hình: `SecurityConfig.java`
- `/api/auth/**` → permitAll (không cần token)
- Tất cả endpoint khác → authenticated (cần Bearer Token)
- CORS cho: `http://localhost:5173`, `http://localhost:5174`

---

## 3. SECURITY & AUTH FLOW

```
[Login.vue] --POST /api/auth/login--> [AuthController] --> [AuthServiceImpl]
    ↓ authenticate via AuthenticationManager
    ↓ load User + Role + Warehouse
    ↓ generate JWT via JwtTokenUtil
    ↓ return { token, role, warehouseId, warehouseName }
    
[Frontend] lưu vào localStorage:
    - token → dùng cho Authorization header
    - userRole → dùng cho RBAC trên UI (ẩn/hiện menu, route guard)
    - warehouseId, warehouseName → hiển thị kho được gán

[JwtAuthenticationFilter] chặn mọi request:
    ↓ đọc header Authorization: Bearer <token>
    ↓ extract username từ JWT
    ↓ set SecurityContext authentication
    
[Router Guard] (frontend/src/router/index.js):
    ↓ Nếu chưa có token → redirect /login
    ↓ Nếu đã có token mà vào /login → redirect /
    ↓ Kiểm tra role có trong meta.roles → nếu không → redirect /
```

**3 vai trò (Roles)**:
| Role | Quyền truy cập |
|---|---|
| `ADMIN` | Tất cả: Dashboard, Products, Categories, Warehouses, Inbound, Outbound, Assets, Reports, Users |
| `MANAGER` | Dashboard, Products, Categories, Warehouses, Inbound, Outbound, Assets, Reports |
| `STAFF` | Dashboard, Products, Categories, Inbound, Outbound, Assets |

---

## 4. API MAP — BACKEND ↔ FRONTEND

> [!TIP]
> **Đây là phần QUAN TRỌNG NHẤT**. Mỗi dòng cho biết: Frontend file nào → gọi API nào → Backend xử lý ở đâu.

### 4.1 Auth (`AuthController` → `/auth`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| POST | `/auth/login` | `Login.vue` | `api.post('/auth/login', {username, password})` | Đăng nhập, trả về JWT |

**Request**: `LoginRequest { username, password }`  
**Response**: `AuthResponse { token, role, warehouseId, warehouseName }`

---

### 4.2 Products (`ProductController` → `/products`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/products` | `ProductList.vue`, `Inbound.vue`, `Outbound.vue`, `Assets.vue` | `api.get('/products')` | Lấy tất cả sản phẩm |
| POST | `/products` | `ProductList.vue` | `api.post('/products', form)` | Thêm sản phẩm mới |
| DELETE | `/products/{id}` | `ProductList.vue` | `api.delete('/products/${id}')` | Xóa sản phẩm |
| GET | `/products/categories` | `ProductList.vue` | `api.get('/products/categories')` | Metadata cho filter |
| GET | `/products/suppliers` | `ProductList.vue` | `api.get('/products/suppliers')` | Metadata cho filter |
| GET | `/products/units` | `ProductList.vue` | `api.get('/products/units')` | Metadata cho filter |

> [!NOTE]
> Tất cả endpoint metadata và CRUD cho Product đã được implement đầy đủ.

---

### 4.3 Categories (Không có Controller riêng)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/categories` | `Category.vue` | `api.get('/categories')` | Lấy tất cả danh mục |
| POST | `/categories` | `Category.vue` | `api.post('/categories', form)` | Thêm danh mục |
| PUT | `/categories/{id}` | `Category.vue` | `api.put('/categories/${id}', form)` | Cập nhật danh mục |
| DELETE | `/categories/{id}` | `Category.vue` | `api.delete('/categories/${id}')` | Xóa danh mục |

> [!NOTE]
> `CategoryController` đã được tạo và hỗ trợ full CRUD cho trang Category.vue.

---

### 4.4 Warehouses (`WarehouseController` → `/warehouses`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/warehouses` | `Warehouse.vue`, `UserManagement.vue` | `api.get('/warehouses')` | Lấy danh sách kho (ADMIN=tất cả, khác=kho được gán) |
| POST | `/warehouses` | `Warehouse.vue` | `api.post('/warehouses', form)` | Thêm kho mới |
| PUT | `/warehouses/{id}` | `Warehouse.vue` | `api.put('/warehouses/${id}', form)` | Cập nhật kho |
| DELETE | `/warehouses/{id}` | `Warehouse.vue` | `api.delete('/warehouses/${id}')` | Xóa kho |
| GET | `/warehouses/{id}/bin-stock` | *(chưa dùng)* | — | Placeholder, chưa implement |

---

### 4.5 Inventory — Inbound (`InventoryController` → `/inventory/inbounds`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/inventory/inbounds` | `Inbound.vue` | `api.get('/inventory/inbounds')` | Danh sách phiếu nhập |
| POST | `/inventory/inbounds` | `Inbound.vue` | `api.post('/inventory/inbounds', {receiptCode})` | Tạo phiếu nhập nháp (PENDING) |
| POST | `/inventory/inbounds/{id}/approve` | `Inbound.vue` | `api.post('/inventory/inbounds/${id}/approve', {productId, quantity})` | Duyệt → cộng tồn kho |

**Business Logic (approveInbound)**:
1. Kiểm tra status = PENDING
2. Tìm Product, cộng `currentStock += quantity`
3. Tạo `InDetail` record
4. Đổi status → APPROVED

---

### 4.6 Inventory — Outbound (`InventoryController` → `/inventory/outbounds`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/inventory/outbounds` | `Outbound.vue` | `api.get('/inventory/outbounds')` | Danh sách lệnh xuất |
| POST | `/inventory/outbounds` | `Outbound.vue` | `api.post('/inventory/outbounds', {issueCode})` | Tạo lệnh xuất nháp (PENDING) |
| POST | `/inventory/outbounds/{id}/approve` | `Outbound.vue` | `api.post('/inventory/outbounds/${id}/approve', {productId, quantity})` | Duyệt → trừ tồn kho (kiểm tra đủ hàng) |
| POST | `/inventory/outbounds/{id}/undo` | `Outbound.vue` | `api.post('/inventory/outbounds/${id}/undo')` | Hoàn tác → cộng lại tồn kho, status=CANCELLED |

**Business Logic (approveOutbound)**:
1. Kiểm tra status = PENDING
2. Tìm Product, kiểm tra `currentStock >= quantity`
3. Trừ `currentStock -= quantity`
4. Tạo `OutDetail` record, status → APPROVED

**Business Logic (undoOutbound)**:
1. Kiểm tra status = APPROVED
2. Tìm OutDetail → cộng lại quantity vào Product
3. Status → CANCELLED

---

### 4.7 Assets (`AssetController` → `/assets`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/assets` | `Assets.vue` | `api.get('/assets')` | Danh sách tài sản |
| POST | `/assets` | `Assets.vue` | `api.post('/assets', {product, assetCode, conditionStatus})` | Đăng ký tài sản |
| GET | `/assets/{id}/depreciation` | `Assets.vue` | `api.get('/assets/${id}/depreciation')` | Tính khấu hao (10%/năm dựa trên ngày tạo) |

---

### 4.8 Alerts (`AlertController` → `/alerts`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/alerts` | *(App.vue badge — hiện chưa gọi API)* | — | Lấy notifications chưa đọc |

---

### 4.9 Reports (`ReportController` → `/reports`)

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/reports/dashboard-stats` | `Dashboard.vue` | `api.get('/reports/dashboard-stats')` | 4 thẻ thống kê (Dữ liệu thực từ DB) |
| GET | `/reports/recent-activities` | `Dashboard.vue` | `api.get('/reports/recent-activities')` | Hoạt động gần đây (Dữ liệu thực từ DB) |
| GET | `/reports/inventory` | *(chưa dùng)* | — | Báo cáo tồn kho chung |
| GET | `/reports/dashboard` | *(chưa dùng)* | — | Dữ liệu biểu đồ |
| GET | `/reports/inventory-status` | `Reports.vue` | `api.get('/reports/inventory-status')` | Danh sách tồn kho sản phẩm |
| GET | `/reports/alerts` | `Reports.vue` | `api.get('/reports/alerts')` | Cảnh báo tồn kho thấp |

> [!NOTE]
> `ReportController` đã được cập nhật để sử dụng dữ liệu thực từ Database và bổ sung đầy đủ endpoint cho Reports.vue.

---

### 4.10 Users (`UserController` → `/users`) — ADMIN only

| Method | Endpoint | Frontend File | Hàm gọi API | Mô tả |
|---|---|---|---|---|
| GET | `/users` | `UserManagement.vue` | `api.get('/users')` | Danh sách users (với role & warehouse) |
| GET | `/users/roles` | `UserManagement.vue` | `api.get('/users/roles')` | Danh sách tên roles |
| POST | `/users` | `UserManagement.vue` | `api.post('/users', {username, password, roleName, warehouseId})` | Tạo user mới |
| DELETE | `/users/{id}` | `UserManagement.vue` | `api.delete('/users/${id}')` | Xóa user (không cho phép tự xóa) |

---

## 5. FRONTEND CHI TIẾT

### 5.1 API Client (`frontend/src/api/index.js`)
- Base URL: `http://localhost:8080/api`
- Request interceptor: tự gắn `Authorization: Bearer <token>` từ localStorage
- Response interceptor: nếu nhận 401 → xóa token, redirect `/login`

### 5.2 Router (`frontend/src/router/index.js`)

| Route Path | Vue Component | Roles cho phép | Ghi chú |
|---|---|---|---|
| `/login` | `Login.vue` | Public | Không hiện sidebar |
| `/` | `Dashboard.vue` | ADMIN, MANAGER, STAFF | Trang chủ |
| `/products` | `ProductList.vue` | ADMIN, MANAGER, STAFF | |
| `/products/:id` | `ProductDetail.vue` | ADMIN, MANAGER, STAFF | Chi tiết sản phẩm |
| `/categories` | `Category.vue` | ADMIN, MANAGER, STAFF | |
| `/warehouses` | `Warehouse.vue` | ADMIN, MANAGER | |
| `/inbound` | `Inbound.vue` | ADMIN, MANAGER, STAFF | |
| `/outbound` | `Outbound.vue` | ADMIN, MANAGER, STAFF | |
| `/assets` | `Assets.vue` | ADMIN, MANAGER, STAFF | |
| `/reports` | `Reports.vue` | ADMIN, MANAGER | |
| `/users` | `UserManagement.vue` | ADMIN | |
| `/profile` | `Login.vue` (mode: profile) | — | Chưa implement |

---

## 6. TRẠNG THÁI CÁC VẤN ĐỀ

| # | Vấn đề | Mức độ | Trạng thái | Giải pháp |
|---|---|---|---|---|
| 1 | Thiếu CategoryController | 🔴 Critical | ✅ **Fixed** | Đã tạo CategoryController với full CRUD |
| 2 | Thiếu endpoints metadata cho Product | 🟡 Medium | ✅ **Fixed** | Đã thêm /categories, /suppliers, /units vào ProductController |
| 3 | Thiếu endpoints cho Reports | 🟡 Medium | ✅ **Fixed** | Đã thêm /inventory-status và /alerts vào ReportController |
| 4 | Report data hardcoded | 🟡 Medium | ✅ **Fixed** | Đã thay bằng dữ liệu thực truy vấn từ Database |
| 5 | WarehouseController.getBinStock rỗng | 🟢 Low | 🕒 *Pending* | Cần bổ sung dữ liệu BinLocation thực tế |
| 6 | ProductList.vue delete chưa gọi API | 🟢 Low | ✅ **Fixed** | Đã thêm DELETE API + cập nhật frontend |
| 7 | Duplicate InventoryServiceImpl | 🟡 Medium | ✅ **Fixed** | Đã xóa các file rác trong folder controller/ |

---

## 7. HƯỚNG DẪN CHO AI AGENT

> [!IMPORTANT]
> **Khi nhận yêu cầu từ người dùng, AI agent cần tuân theo quy trình sau:**

### Bước 1: Đọc file này
Đọc `PROJECT_CONTEXT.md` để hiểu kiến trúc và xác định module liên quan.

### Bước 2: Xác định scope
- Yêu cầu liên quan đến **entity/database** → sửa file trong `entity/`, `repository/`
- Yêu cầu liên quan đến **business logic** → sửa file trong `service/`
- Yêu cầu liên quan đến **API endpoint** → sửa file trong `controller/`
- Yêu cầu liên quan đến **giao diện** → sửa file trong `frontend/src/views/`
- Yêu cầu liên quan đến **quyền truy cập** → sửa `SecurityConfig.java` + `router/index.js` + `App.vue`

### Bước 3: Luồng tạo feature mới
```
1. [Entity]      → Tạo/sửa Entity trong entity/ nếu cần bảng DB mới
2. [Repository]  → Tạo Repository JPA interface trong repository/
3. [DTO]         → Tạo Request/Response DTO trong dto/ nếu cần
4. [Service]     → Tạo Service interface + Implementation trong service/
5. [Controller]  → Tạo REST Controller trong controller/ với @RequestMapping
6. [Frontend]    → Tạo/sửa Vue component trong views/, gọi API qua api/index.js
7. [Router]      → Thêm route mới trong router/index.js với meta.roles
8. [Sidebar]     → Thêm menu item trong App.vue nếu cần hiện trên sidebar
```

### Bước 4: Cập nhật file này
Sau khi hoàn thành thay đổi, **BẮT BUỘC** cập nhật các section liên quan trong file `PROJECT_CONTEXT.md`:
- Section 2 (Entities): nếu thêm/sửa entity
- Section 4 (API MAP): nếu thêm/sửa API endpoint
- Section 5 (Frontend): nếu thêm/sửa view
- Section 6 (Vấn đề): nếu fix hoặc phát hiện vấn đề mới

### Convention quan trọng
- **Controller**: `@RestController` + `@RequestMapping("/resource")` + `@RequiredArgsConstructor`
- **Service**: Interface + Implementation pattern, dùng `@Transactional` cho write operations
- **Entity**: `@Entity` + Lombok (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor`)
- **Frontend API**: Luôn dùng `api` instance từ `import api from '../api'`
- **Error handling**: `ElMessage.error()` cho frontend, `ResponseStatusException` cho backend
- **RBAC Frontend**: Dùng `meta: { roles: [...] }` trong router, `v-if="userRole === 'ADMIN'"` trong template

---

## 8. CHẠY ỨNG DỤNG

### Backend
```bash
cd ontop-wms/backend
./mvnw spring-boot:run
# Hoặc trên Windows: mvnw.cmd spring-boot:run
# → http://localhost:8080
```

### Frontend
```bash
cd ontop-wms/frontend
npm install
npm run dev
# → http://localhost:5173
```

### Lỗi thường gặp
| Lỗi | Nguyên nhân | Fix |
|---|---|---|
| 403 Forbidden | Token hết hạn hoặc thiếu role | Kiểm tra `SecurityConfig`, `@PreAuthorize`, localStorage |
| 401 Unauthorized | Sai username/password hoặc token | Xóa localStorage, đăng nhập lại |
| CORS error | Frontend gọi từ origin không được phép | Thêm origin vào `SecurityConfig.corsConfigurationSource()` |
| 404 Not Found | API endpoint chưa tạo | Xem Section 6 — các endpoint thiếu |
| LazyInitializationException | Entity relationship lazily loaded ngoài transaction | Dùng `@EntityGraph` trên repository query |