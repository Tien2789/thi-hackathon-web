<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const loginForm = ref({
  username: '',
  password: '',
  remember: false
})
const showPassword = ref(false)
const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('Vui lòng nhập đầy đủ tài khoản và mật khẩu')
    return
  }

  try {
    loading.value = true
    const response = await api.post('/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    localStorage.setItem('token', response.data.token)
    localStorage.setItem('userRole', response.data.role)
    localStorage.setItem('warehouseId', response.data.warehouseId || '')
    localStorage.setItem('warehouseName', response.data.warehouseName || '')
    ElMessage.success('Đăng nhập thành công')
    setTimeout(() => {
      router.push('/')
    }, 500)
  } catch (error) {
    console.error('Lỗi đăng nhập chi tiết:', error.response || error)
    ElMessage.error(error.response?.data?.message || 'Tài khoản hoặc mật khẩu không chính xác')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrapper d-flex vh-100 bg-white overflow-hidden">
    <!-- Left: Branding/Marketing -->
    <div
      class="login-aside d-none d-lg-flex flex-column justify-content-center p-5 bg-primary text-white position-relative">
      <div class="login-aside-content z-2">
        <div
          class="logo-box-large mb-4 rounded-4 bg-white text-primary d-flex align-items-center justify-content-center shadow">
          FW
        </div>
        <h1 class="display-4 fw-extrabold mb-3 line-height-1">OnTop<br />WMS 2026</h1>
        <p class="fs-5 opacity-75 mb-5">Hệ thống quản trị kho thông minh thế hệ mới tích hợp AI & QR Scan realtime.</p>

        <div
          class="trust-badge d-flex align-items-center gap-3 bg-white bg-opacity-10 p-3 rounded-4 border border-white border-opacity-10">
          <div class="trust-icons d-flex -space-x-2">
            <el-avatar :size="32" src="https://api.dicebear.com/7.x/avataaars/svg?seed=user1" class="border" />
            <el-avatar :size="32" src="https://api.dicebear.com/7.x/avataaars/svg?seed=user2" class="border" />
            <el-avatar :size="32" src="https://api.dicebear.com/7.x/avataaars/svg?seed=user3" class="border" />
          </div>
          <span class="small fw-medium">+2,500 doanh nghiệp đang tin dùng</span>
        </div>
      </div>

      <!-- Abstract BG patterns -->
      <div class="position-absolute bottom-0 start-0 w-100 opacity-25 pointer-events-none overflow-hidden"
        style="height: 300px;">
        <svg viewBox="0 0 500 200" class="w-100 h-100">
          <path fill="white" d="M0,160 C150,200 350,100 500,160 L500,200 L0,200 Z"></path>
        </svg>
      </div>
    </div>

    <!-- Right: Login Form -->
    <div class="login-main flex-grow-1 d-flex align-items-center justify-content-center p-4">
      <div class="login-card w-100" style="max-width: 420px;">
        <div class="text-center mb-5">
          <h2 class="fw-bold text-dark mb-2">Đăng nhập hệ thống</h2>
          <p class="text-muted">Nhập tài khoản để truy cập Warehouse Management</p>
        </div>

        <form @submit.prevent="handleLogin" class="d-grid gap-4">
          <div class="form-field">
            <label class="form-label fw-bold small text-uppercase text-muted mb-2 ls-1">Tài khoản</label>
            <el-input v-model="loginForm.username" placeholder="Nhập username của bạn" :prefix-icon="User" size="large"
              class="custom-input" />
          </div>

          <div class="form-field">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <label class="form-label fw-bold small text-uppercase text-muted mb-0 ls-1">Mật khẩu</label>
              <a href="#" class="text-decoration-none small fw-bold color-primary">Quên mật khẩu?</a>
            </div>
            <el-input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" placeholder="••••••••"
              :prefix-icon="Lock" size="large" class="custom-input">
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showPassword = !showPassword">
                  <View v-if="!showPassword" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </div>

          <div class="d-flex align-items-center mb-2">
            <el-checkbox v-model="loginForm.remember" label="Ghi nhớ đăng nhập" size="large" />
          </div>

          <el-button type="primary" size="large" class="w-100 py-4 fw-bold shadow-lg-primary" :loading="loading"
            native-type="submit">
            TRUY CẬP NGAY
          </el-button>
        </form>

        <div class="mt-5 pt-4 border-top text-center">
          <p class="small text-muted mb-0">Liên hệ kỹ thuật: <a href="tel:1900xxxx"
              class="text-decoration-none fw-bold text-dark">1900 68XX</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-aside {
  width: 480px;
}

.logo-box-large {
  width: 64px;
  height: 64px;
  font-size: 24px;
  font-weight: 800;
}

.ls-1 {
  letter-spacing: 1px;
}

.line-height-1 {
  line-height: 1.1;
}

.shadow-lg-primary {
  box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.4);
}

.custom-input :deep(.el-input__wrapper) {
  padding: 8px 15px;
  background-color: #f8fafc;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.-space-x-2>* {
  margin-left: -8px;
}

@media (max-width: 991px) {
  .login-card {
    padding: 20px;
  }
}
</style>