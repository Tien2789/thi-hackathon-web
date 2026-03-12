import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/', component: () => import('../views/Dashboard.vue'), meta: { roles: ['ADMIN', 'MANAGER', 'STAFF'] } },
  { path: '/products', component: () => import('../views/ProductList.vue'), meta: { roles: ['ADMIN', 'MANAGER', 'STAFF'] } },
  { path: '/products/:id', component: () => import('../views/ProductDetail.vue'), meta: { roles: ['ADMIN', 'MANAGER', 'STAFF'] } },
  { path: '/warehouses', component: () => import('../views/Warehouse.vue'), meta: { roles: ['ADMIN', 'MANAGER'] } },
  { path: '/inbound', component: () => import('../views/Inbound.vue'), meta: { roles: ['ADMIN', 'MANAGER', 'STAFF'] } },
  { path: '/outbound', component: () => import('../views/Outbound.vue'), meta: { roles: ['ADMIN', 'MANAGER', 'STAFF'] } },
  { path: '/assets', component: () => import('../views/Assets.vue'), meta: { roles: ['ADMIN', 'MANAGER', 'STAFF'] } },
  { path: '/reports', component: () => import('../views/Reports.vue'), meta: { roles: ['ADMIN', 'MANAGER'] } },
  { path: '/users', component: () => import('../views/UserManagement.vue'), meta: { roles: ['ADMIN'] } },
  { path: '/profile', component: () => import('../views/Login.vue'), props: { mode: 'profile' } },
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  // 1. Tránh truy cập trang bảo mật khi chưa đăng nhập
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 2. Tránh vào trang login khi đã đăng nhập
  if (to.path === '/login' && token) {
    next('/')
    return
  }

  // 3. Kiểm tra phân quyền (RBAC)
  if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    // Nếu không có quyền, chuyển về Dashboard thay vì để kẹt
    if (to.path === '/') {
      // Nếu chính trang chủ cũng không vào được (do userRole null/sai), đẩy về login
      // FIX: Xóa token để tránh vòng lặp vô hạn (Login -> Home -> Login)
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      next('/login')
    } else {
      next('/')
    }
    return
  }

  next()
})

export default router;

//