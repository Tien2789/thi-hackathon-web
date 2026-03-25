<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Box,
  List,
  Search,
  Bell,
  PieChart,
  Van,
  Files,
  ShoppingCart,
  UserFilled,
  SwitchButton,
  Expand,
  Fold,
  Setting,
  FullScreen,
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const isCollapse = ref(false);
const isFullscreen = ref(false);

// Kiểm tra nếu ở trang login thì không hiện Sidebar/Topbar
const isLoginPage = computed(() => route.path === "/login");

const userRole = ref(localStorage.getItem("userRole"));
const username = ref("Guest");
const warehouseName = ref("");

// Hàm cập nhật trạng thái đăng nhập từ LocalStorage
const updateAuthInfo = () => {
  const token = localStorage.getItem("token");
  userRole.value = localStorage.getItem("userRole");
  warehouseName.value = localStorage.getItem("warehouseName") || "";

  if (!token) {
    username.value = "Guest";
  } else {
    username.value = userRole.value === "ADMIN" ? "Administrator" : "Nhân viên";
  }
};

// Theo dõi sự thay đổi của route (ví dụ: khi vừa đăng nhập xong và chuyển trang)
watch(
  () => route.path,
  () => {
    updateAuthInfo();
  }
);

const handleLogout = () => {
  localStorage.clear();
  router.push("/login");
};

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value;
};

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch((err) => {
      console.error(`Error attempting to enable full-screen mode: ${err.message}`);
    });
    isFullscreen.value = true;
  } else {
    document.exitFullscreen();
    isFullscreen.value = false;
  }
};

// Cập nhật lần đầu khi app chạy
onMounted(() => {
  updateAuthInfo();
});
</script>

<template>
  <div class="app-wrapper">
    <router-view v-if="isLoginPage" />

    <div
      v-else
      class="layout-container container-fluid p-0 d-flex vh-100 overflow-hidden bg-light"
    >
      <!-- Sidebar -->
      <aside
        class="sidebar transition-all d-flex flex-column shadow-sm border-end bg-white"
        :class="{ collapsed: isCollapse }"
      >
        <div class="sidebar-header d-flex align-items-center px-4 border-bottom">
          <div
            class="logo-box rounded-3 d-flex align-items-center justify-content-center shadow-sm text-white fw-bold bg-primary"
          >
            OT
          </div>
          <span v-if="!isCollapse" class="ms-3 fs-6 fw-extrabold text-nowrap"
            >ONTOP WMS</span
          >
        </div>

        <div
          v-if="!isCollapse"
          class="sidebar-user p-4 text-center border-bottom bg-light bg-opacity-50"
        >
          <el-avatar
            :size="56"
            src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin"
            class="border border-2 border-primary-subtle shadow-sm mx-auto"
          />
          <p class="mt-3 mb-1 fw-bold text-dark small text-uppercase tracking-widest">
            {{ username }}
          </p>
          <div class="d-flex align-items-center justify-content-center gap-2">
            <span class="status-indicator online"></span>
            <span class="text-muted" style="font-size: 11px">{{ userRole }}</span>
          </div>
        </div>

        <div class="sidebar-nav flex-grow-1 overflow-auto py-2 custom-scrollbar">
          <el-menu
            :default-active="route.path"
            router
            :collapse="isCollapse"
            class="border-0 w-100 main-menu"
          >
            <el-menu-item index="/">
              <el-icon>
                <PieChart />
              </el-icon>
              <template #title><span>Tổng quan</span></template>
            </el-menu-item>

            <el-sub-menu
              v-if="['ADMIN', 'MANAGER', 'STAFF'].includes(userRole)"
              index="products-group"
            >
              <template #title>
                <el-icon>
                  <Box />
                </el-icon>
                <span>Hàng hóa</span>
              </template>
              <el-menu-item index="/products">Danh mục sản phẩm</el-menu-item>
              <el-menu-item index="/categories">Phân loại sản phẩm</el-menu-item>
              <el-menu-item
                v-if="['ADMIN', 'MANAGER'].includes(userRole)"
                index="/warehouses"
                >Cơ cấu kho bãi</el-menu-item
              >
            </el-sub-menu>

            <el-menu-item
              v-if="['ADMIN', 'MANAGER', 'STAFF'].includes(userRole)"
              index="/inbound"
            >
              <el-icon>
                <Van />
              </el-icon>
              <template #title><span>Nhập kho</span></template>
            </el-menu-item>

            <el-menu-item
              v-if="['ADMIN', 'MANAGER', 'STAFF'].includes(userRole)"
              index="/outbound"
            >
              <el-icon>
                <ShoppingCart />
              </el-icon>
              <template #title><span>Xuất kho</span></template>
            </el-menu-item>

            <div
              class="menu-divider my-3 mx-4"
              v-if="!isCollapse && ['ADMIN', 'MANAGER'].includes(userRole)"
            >
              <span class="text-muted fw-bold text-uppercase" style="font-size: 10px"
                >Báo cáo & Tài sản</span
              >
            </div>

            <el-menu-item
              v-if="['ADMIN', 'MANAGER', 'STAFF'].includes(userRole)"
              index="/assets"
            >
              <el-icon>
                <Files />
              </el-icon>
              <template #title><span>Quản lý tài sản</span></template>
            </el-menu-item>

            <el-menu-item v-if="['ADMIN', 'MANAGER'].includes(userRole)" index="/reports">
              <el-icon>
                <List />
              </el-icon>
              <template #title><span>Báo cáo vận hành</span></template>
            </el-menu-item>

            <div
              class="menu-divider my-3 mx-4"
              v-if="!isCollapse && userRole === 'ADMIN'"
            >
              <span class="text-muted fw-bold text-uppercase" style="font-size: 10px"
                >Hệ thống</span
              >
            </div>

            <el-menu-item v-if="userRole === 'ADMIN'" index="/users">
              <el-icon>
                <UserFilled />
              </el-icon>
              <template #title><span>Quản lý nhân sự</span></template>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="sidebar-footer p-3 border-top mt-auto" v-if="!isCollapse">
          <div
            class="bg-primary bg-opacity-10 rounded p-3 text-center border border-primary border-opacity-10"
          >
            <p class="small text-primary mb-0 fw-bold">v3.5.0-premium</p>
          </div>
        </div>
      </aside>

      <!-- Main Content Area -->
      <div class="main-wrapper flex-grow-1 d-flex flex-column min-vw-0">
        <!-- Navbar -->
        <header
          class="navbar-top d-flex align-items-center justify-content-between px-4 bg-white border-bottom shadow-sm"
        >
          <div class="d-flex align-items-center gap-3">
            <button
              @click="toggleSidebar"
              class="btn btn-light rounded-circle shadow-sm border-0 nav-toggle-btn"
            >
              <el-icon class="fs-5 mt-1">
                <Fold v-if="!isCollapse" />
                <Expand v-else />
              </el-icon>
            </button>
            <nav aria-label="breadcrumb" class="d-none d-md-block ms-2 mt-3">
              <ol class="breadcrumb">
                <li class="breadcrumb-item">
                  <a href="#" class="text-decoration-none text-muted small">Warehouse</a>
                </li>
                <li class="breadcrumb-item active small" aria-current="page">
                  {{ route.path === "/" ? "Dashboard" : "Management" }}
                </li>
              </ol>
            </nav>
          </div>

          <div class="d-flex align-items-center gap-3">
            <div
              class="input-group d-none d-lg-flex bg-light rounded-pill border-0 px-2"
              style="width: 250px"
            >
              <span class="input-group-text bg-transparent border-0 text-muted"
                ><el-icon> <Search /> </el-icon
              ></span>
              <input
                type="text"
                class="form-control bg-transparent border-0 small py-2 no-focus"
                placeholder="Tìm kiếm nhanh..."
              />
            </div>

            <div class="d-flex gap-2">
              <button
                @click="toggleFullscreen"
                class="btn btn-white border-0 p-2 text-muted hover-primary transition-all"
              >
                <el-icon class="fs-5">
                  <FullScreen />
                </el-icon>
              </button>
              <button
                class="btn btn-white position-relative border-0 p-2 text-muted hover-primary transition-all"
              >
                <el-icon class="fs-5">
                  <Bell />
                </el-icon>
                <span
                  class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                  style="font-size: 8px"
                >
                  12
                </span>
              </button>
              <button
                class="btn btn-white border-0 p-2 text-muted hover-primary transition-all"
              >
                <el-icon class="fs-5">
                  <Setting />
                </el-icon>
              </button>
            </div>

            <div class="divider-vertical mx-2"></div>
            <span class="text-muted small me-2">{{ userRole }}</span>
            <span
              v-if="warehouseName"
              class="badge bg-secondary bg-opacity-10 text-secondary border border-secondary border-opacity-25 me-2 small"
            >
              {{ warehouseName }}
            </span>
            <el-dropdown trigger="click">
              <div
                class="user-profile-trigger d-flex align-items-center gap-3 cursor-pointer p-1 rounded-pill pe-3 hover-bg-light transition-all"
              >
                <el-avatar
                  :size="32"
                  src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin"
                  class="border"
                />
                <div class="d-none d-sm-block text-start">
                  <p class="mb-0 fw-bold text-dark small" style="line-height: 1">
                    {{ username }}
                  </p>
                  <p class="mb-0 text-muted" style="font-size: 10px">
                    {{ userRole }} Account
                  </p>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item
                    ><el-icon>
                      <UserFilled />
                    </el-icon>
                    Hồ sơ cá nhân</el-dropdown-item
                  >
                  <el-dropdown-item
                    ><el-icon>
                      <Setting />
                    </el-icon>
                    Cài đặt</el-dropdown-item
                  >
                  <el-dropdown-item
                    divided
                    @click="handleLogout"
                    class="text-danger fw-bold"
                  >
                    <el-icon>
                      <SwitchButton />
                    </el-icon>
                    Đăng xuất
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </header>

        <!-- Main Content -->
        <main class="content-body flex-grow-1 p-4 overflow-auto custom-scrollbar">
          <div class="container-fluid p-0">
            <router-view v-slot="{ Component }">
              <transition name="page-fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<style>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap");

:root {
  --primary-color: #3b82f6;
  --sidebar-width: 280px;
  --sidebar-collapsed-width: 70px;
  --navbar-height: 70px;
}

body,
html {
  margin: 0;
  padding: 0;
  font-family: "Inter", sans-serif;
  color: #1e293b;
  -webkit-font-smoothing: antialiased;
}

/* Sidebar Styling */
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  z-index: 1000;
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-header {
  height: var(--navbar-height);
}

.logo-box {
  width: 40px;
  height: 40px;
  font-size: 18px;
  letter-spacing: -1px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-indicator.online {
  background-color: #10b981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

/* Menu Styling */
.main-menu.el-menu {
  border-right: none !important;
}

.main-menu .el-menu-item,
.main-menu .el-sub-menu__title {
  height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  color: #64748b !important;
}

.main-menu .el-menu-item:hover,
.main-menu .el-sub-menu .el-menu-item:hover {
  background-color: #f1f5f9 !important;
  color: var(--primary-color) !important;
}

.main-menu .el-menu-item.is-active {
  background-color: #eff6ff !important;
  color: var(--primary-color) !important;
  font-weight: 600;
}

.ai-menu-item {
  background: linear-gradient(90deg, #eff6ff 0%, transparent 100%);
}

/* Navbar Styling */
.navbar-top {
  height: var(--navbar-height);
  z-index: 999;
}

.nav-toggle-btn {
  width: 38px;
  height: 38px;
  background: #f8fafc;
}

.nav-toggle-btn:hover {
  background: #f1f5f9;
}

.divider-vertical {
  width: 1px;
  height: 24px;
  background-color: #e2e8f0;
}

.no-focus:focus {
  box-shadow: none;
}

.hover-primary:hover {
  color: var(--primary-color) !important;
}

.hover-bg-light:hover {
  background-color: #f1f5f9;
}

/* Transitions */
.transition-all {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}

.fw-extrabold {
  font-weight: 800;
}

.cursor-pointer {
  cursor: pointer;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -280px;
  }

  .sidebar.show {
    left: 0;
  }
}
</style>
