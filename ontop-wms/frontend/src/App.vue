<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled, Box, List, Menu as IconMenu,
  Search, Bell, PieChart, Van,
  Files, ShoppingCart, Warning, UserFilled, SwitchButton,
  Expand, Fold, Setting, Monitor, FullScreen, Avatar
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const isMobileShow = ref(false)
const isFullscreen = ref(false)

const isLoginPage = computed(() => route.path === '/login')

const userRole = computed(() => {
  route.path
  return localStorage.getItem('userRole')
})

const username = computed(() => {
  route.path
  const token = localStorage.getItem('token')
  if (!token) return 'Guest'
  return localStorage.getItem('userRole') === 'ADMIN' ? 'Administrator' : 'Nhân viên'
})

const warehouseName = computed(() => {
  route.path
  return localStorage.getItem('warehouseName') || ''
})

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}

const toggleSidebar = () => {
    if (window.innerWidth <= 768) {
        isMobileShow.value = !isMobileShow.value
    } else {
        isCollapse.value = !isCollapse.value
    }
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(err => {
      console.error(`Error attempting to enable full-screen mode: ${err.message}`);
    });
    isFullscreen.value = true;
  } else {
    document.exitFullscreen();
    isFullscreen.value = false;
  }
}
</script>

<template>
  <div class="app-shell">
    <!-- Login View (Isolated) -->
    <router-view v-if="isLoginPage" />

    <!-- Main App Layout -->
    <div v-else class="premium-layout d-flex vh-100 overflow-hidden">
      
      <!-- Mobile Backdrop -->
      <div v-if="isMobileShow" class="mobile-backdrop position-fixed top-0 start-0 w-100 h-100 z-1000 bg-dark bg-opacity-50 blur-sm" @click="isMobileShow = false"></div>

      <!-- Sidebar: Dark Glassmorphism -->
      <aside class="sidebar-wrapper transition-all d-flex flex-column shadow-lg" :class="{ 'collapsed': isCollapse, 'mobile-show': isMobileShow }">
        <div class="sidebar-header d-flex align-items-center px-4">
          <div class="logo-glow-box rounded-4 d-flex align-items-center justify-content-center shadow-lg text-white fw-bold">
            <el-icon :size="20"><Box /></el-icon>
          </div>
          <div v-if="!isCollapse" class="ms-3 sidebar-brand">
            <span class="fs-5 fw-black text-white">ONTOP</span>
            <span class="ms-1 tiny-tag text-primary">WMS</span>
          </div>
          <el-button v-if="isMobileShow" circle class="ms-auto bg-transparent border-0 text-white" @click="isMobileShow = false">
             <el-icon><Fold /></el-icon>
          </el-button>
        </div>

        <div v-if="!isCollapse" class="sidebar-account mx-3 mt-4 mb-3 p-3 rounded-4 glass-card border border-white border-opacity-10 shadow-sm">
          <div class="d-flex align-items-center gap-3">
             <el-avatar :size="48" src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin" class="border border-2 border-primary border-opacity-25" />
             <div class="account-info">
               <p class="mb-0 fw-bold text-white small">{{ username }}</p>
               <el-tag size="small" type="primary" effect="dark" class="mt-1 border-0 shadow-sm">{{ userRole }}</el-tag>
             </div>
          </div>
        </div>

        <div class="sidebar-navigation flex-grow-1 overflow-auto custom-scrollbar px-2">
          <el-menu :default-active="route.path" router :collapse="isCollapse" class="border-0 bg-transparent main-navigation">
            <div class="nav-section-label px-3" v-if="!isCollapse">Tổng quan</div>
            <el-menu-item index="/">
              <el-icon><PieChart /></el-icon>
              <template #title><span>Bảng điều khiển</span></template>
            </el-menu-item>

            <div class="nav-section-label px-3" v-if="!isCollapse">Vận hành</div>
            
            <el-menu-item index="/inbound">
              <el-icon><Van /></el-icon>
              <template #title><span>Nhập kho</span></template>
            </el-menu-item>

            <el-menu-item index="/outbound">
              <el-icon><ShoppingCart /></el-icon>
              <template #title><span>Xuất kho</span></template>
            </el-menu-item>

            <el-sub-menu index="catalog">
              <template #title>
                <el-icon><Box /></el-icon>
                <span>Hàng hóa & Kho</span>
              </template>
              <el-menu-item index="/products">Sản phẩm</el-menu-item>
              <el-menu-item v-if="['ADMIN', 'MANAGER'].includes(userRole)" index="/warehouses">Cơ cấu kho</el-menu-item>
            </el-sub-menu>

            <div class="nav-section-label px-3" v-if="!isCollapse && ['ADMIN', 'MANAGER'].includes(userRole)">Hệ thống</div>
            
            <el-menu-item index="/assets">
              <el-icon><Files /></el-icon>
              <template #title><span>Tài sản TB</span></template>
            </el-menu-item>

            <el-menu-item v-if="['ADMIN', 'MANAGER'].includes(userRole)" index="/reports">
              <el-icon><List /></el-icon>
              <template #title><span>Báo cáo</span></template>
            </el-menu-item>

            <el-menu-item v-if="['ADMIN', 'MANAGER'].includes(userRole)" index="/users">
              <el-icon><UserFilled /></el-icon>
              <template #title><span>Nhân sự</span></template>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="sidebar-footer p-3" v-if="!isCollapse">
          <div class="system-status glass-blur rounded-4 p-3 border border-white border-opacity-10 d-flex align-items-center gap-3">
             <div class="pulse-indicator"></div>
             <div>
                <p class="mb-0 text-white fw-bold tiny">HỆ THỐNG HOẠT ĐỘNG</p>
                <p class="mb-0 text-muted ultra-tiny">Đồng bộ: Vừa xong</p>
             </div>
          </div>
        </div>
      </aside>

      <!-- Main Section -->
      <div class="content-wrapper flex-grow-1 d-flex flex-column min-vw-0">
        
        <!-- Navbar: Light Glass -->
        <header class="top-navbar d-flex align-items-center justify-content-between px-4 sticky-top">
          <div class="navbar-left d-flex align-items-center gap-4">
            <button @click="toggleSidebar" class="btn-toggle text-dark shadow-sm">
                <el-icon :size="18"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
            </button>
            <div class="search-bar-premium d-none d-lg-flex align-items-center px-3 rounded-pill bg-white shadow-sm border border-light">
              <el-icon class="text-muted"><Search /></el-icon>
              <input type="text" placeholder="Tìm kiếm tài liệu, sản phẩm..." class="form-control-plaintext ps-2 small">
            </div>
          </div>

          <div class="navbar-right d-flex align-items-center gap-3">
            <div class="utility-actions d-none d-md-flex gap-2 pe-3 border-end">
              <button class="icon-btn" @click="toggleFullscreen"><el-icon><FullScreen /></el-icon></button>
              <button class="icon-btn position-relative"><el-icon><Bell /></el-icon><span class="badge-dot pulse"></span></button>
            </div>
            
            <el-dropdown trigger="click">
              <div class="user-pill bg-white rounded-pill p-1 pe-3 d-flex align-items-center gap-3 shadow-sm cursor-pointer hover-bg-light transition-all">
                <el-avatar :size="32" src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin" />
                <div class="d-none d-sm-block">
                  <p class="mb-0 fw-bold small text-dark" style="line-height: 1.1">{{ username }}</p>
                  <p class="mb-0 text-muted ultra-tiny">{{ warehouseName || 'Quản trị hệ thống' }}</p>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="premium-dropdown">
                  <el-dropdown-item :icon="UserFilled">Tài khoản</el-dropdown-item>
                  <el-dropdown-item :icon="Setting">Tùy chỉnh</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout" class="text-danger fw-bold"><el-icon><SwitchButton /></el-icon> Đăng xuất</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </header>

        <!-- Main Content -->
        <main class="main-viewport flex-grow-1 p-4 overflow-auto custom-scrollbar">
          <router-view v-slot="{ Component }">
            <transition name="view-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>
    </div>
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=Inter:wght@400;600&display=swap');

:root {
  --primary: #4f46e5;
  --primary-glow: rgba(79, 70, 229, 0.4);
  --dark-bg: #0f172a;
  --sidebar-w: 280px;
  --sidebar-c: 80px;
  --nav-h: 80px;
}

body {
  background-color: #f1f5f9;
  font-family: 'Plus Jakarta Sans', sans-serif;
  color: #1e293b;
  -webkit-font-smoothing: antialiased;
}

/* Sidebar Styles */
.sidebar-wrapper {
  width: var(--sidebar-w);
  background: var(--dark-bg);
  height: 100vh;
  z-index: 1000;
  position: relative;
}

.sidebar-wrapper.collapsed { width: var(--sidebar-c); }

.sidebar-header {
  height: var(--nav-h);
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.logo-glow-box {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, var(--primary), #818cf8);
  box-shadow: 0 4px 15px var(--primary-glow);
}

.sidebar-brand .fw-black { font-weight: 900; letter-spacing: -0.5px; }

.tiny-tag { font-size: 10px; padding: 2px 6px; background: rgba(79, 70, 229, 0.1); border-radius: 6px; font-weight: 800; }

.glass-card { background: rgba(255, 255, 255, 0.03); backdrop-filter: blur(10px); }

.nav-section-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #475569;
  margin-top: 25px;
  margin-bottom: 10px;
  font-weight: 700;
}

.main-navigation.el-menu { border: none !important; }

.main-navigation .el-menu-item, 
.main-navigation .el-sub-menu__title {
  height: 45px;
  margin: 4px 10px;
  border-radius: 12px;
  color: #94a3b8 !important;
  font-weight: 500;
  transition: all 0.3s;
}

.main-navigation .el-menu-item:hover,
.main-navigation .el-sub-menu__title:hover {
  background: rgba(255, 255, 255, 0.05) !important;
  color: #fff !important;
}

.main-navigation .el-menu--inline {
  background: transparent !important;
}

.main-navigation .el-menu-item.is-active {
  background: var(--primary) !important;
  color: #fff !important;
  box-shadow: 0 4px 15px var(--primary-glow);
}

/* Navbar Styles */
.top-navbar {
  height: var(--nav-h);
  background: rgba(241, 245, 249, 0.8);
  backdrop-filter: blur(15px);
  z-index: 999;
}

.btn-toggle {
  width: 40px;
  height: 40px;
  border: none;
  background: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-toggle:hover { transform: scale(1.05); }

.search-bar-premium {
  width: 350px;
  background: #fff;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border: none;
  background: transparent;
  border-radius: 10px;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn:hover { background: #fff; color: var(--primary); }

.badge-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid #f1f5f9;
}

.user-pill:hover { background: #fff !important; transform: translateY(-1px); }

/* Helpers */
.ultra-tiny { font-size: 9px; text-transform: uppercase; letter-spacing: 0.5px; }

.status-indicator.pulse-indicator {
  width: 10px;
  height: 10px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 0 rgba(16, 185, 129, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0px rgba(16, 185, 129, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0px rgba(16, 185, 129, 0); }
}

/* Animations */
.view-fade-enter-active, .view-fade-leave-active { transition: all 0.3s ease; }
.view-fade-enter-from { opacity: 0; transform: translateX(20px); }
.view-fade-leave-to { opacity: 0; transform: translateX(-20px); }

/* Scrollbar */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 10px; }

@media (max-width: 768px) {
  .sidebar-wrapper { position: fixed; left: -280px; z-index: 1001; }
  .sidebar-wrapper.mobile-show { left: 0; }
  .mobile-backdrop { backdrop-filter: blur(4px); }
  .top-navbar { padding: 0 15px !important; }
}
</style>