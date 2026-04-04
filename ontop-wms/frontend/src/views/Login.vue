<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, View, Hide, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
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
    ElMessage.warning('Vui lòng nhập tài khoản và mật khẩu')
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
    
    ElMessage({
      message: 'Xác thực thành công. Đang chuyển hướng...',
      type: 'success',
      plain: true,
      duration: 2000
    })
    
    setTimeout(() => {
      router.push('/')
    }, 800)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Thông tin đăng nhập không hợp lệ')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-premium-v3 d-flex vh-100 overflow-hidden position-relative">
    
    <!-- Dynamic Animated Mesh Background -->
    <div class="mesh-bg position-absolute top-0 start-0 w-100 h-100 z-n1">
        <div class="mesh-blob blob-1"></div>
        <div class="mesh-blob blob-2"></div>
        <div class="mesh-blob blob-3"></div>
    </div>

    <!-- Main Content Container -->
    <div class="container d-flex align-items-center justify-content-center h-100 px-4">
        
        <div class="row w-100 justify-content-center align-items-center g-0">
            
            <!-- Branding (Left - Desktop only) -->
            <div class="col-lg-6 d-none d-lg-block pe-5 branding-section">
                <div class="logo-circle bg-white rounded-circle d-flex align-items-center justify-content-center mb-4 shadow-lg">
                    <el-icon :size="32" color="#4f46e5"><Lock /></el-icon>
                </div>
                <h1 class="display-3 fw-black text-white ls-tight mb-3">NextGen<br/>WMS Intelligence</h1>
                <div class="divider bg-white bg-opacity-20 mb-4" style="width: 100px; height: 4px; border-radius: 99px;"></div>
                <p class="fs-5 text-white opacity-75 mb-5 lh-base fw-medium" style="max-width: 480px;">
                    Hệ thống quản trị kho bãi tập trung tích hợp Blockchain Tracking & AI Predictive Analytics.
                </p>
                <div class="badge-stack d-flex gap-4">
                    <div class="glass-pill rounded-pill px-4 py-2 text-white small border border-white border-opacity-10 d-flex align-items-center gap-2">
                        <el-icon><SuccessFilled /></el-icon> ISO 27001 Certified
                    </div>
                    <div class="glass-pill rounded-pill px-4 py-2 text-white small border border-white border-opacity-10 d-flex align-items-center gap-2">
                        <el-icon><WarningFilled /></el-icon> Zero Trust Security
                    </div>
                </div>
            </div>

            <!-- Login Form (Right) -->
            <div class="col-12 col-lg-5 col-xl-4">
                <div class="login-card-v3 rounded-5 p-5 shadow-2xl glass-morphism border border-white border-opacity-30">
                    <div class="text-center mb-5">
                         <div class="mobile-logo d-lg-none bg-primary text-white rounded-4 p-3 mb-3 d-inline-flex">
                            <el-icon :size="24"><Lock /></el-icon>
                         </div>
                         <h3 class="fw-black text-dark mb-1">Cổng Đăng Nhập</h3>
                         <p class="text-muted small fw-medium">Chào mừng trở lại! Vui lòng nhập định danh.</p>
                    </div>

                    <form @submit.prevent="handleLogin" class="d-grid gap-4">
                        <div class="field-wrapper">
                            <label class="ultra-tiny fw-bold text-muted text-uppercase mb-2 tracking-wider ps-1">User Identifier</label>
                            <el-input v-model="loginForm.username" placeholder="Sử dụng tên đăng nhập hoặc email" :prefix-icon="User" class="auth-input-pill" />
                        </div>

                        <div class="field-wrapper">
                            <div class="d-flex justify-content-between px-1 mb-2">
                                <label class="ultra-tiny fw-bold text-muted text-uppercase tracking-wider">Access Secret</label>
                                <a href="#" class="ultra-tiny fw-bold text-primary text-decoration-none">Lost data?</a>
                            </div>
                            <el-input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" placeholder="Mật khẩu bảo mật" :prefix-icon="Lock" class="auth-input-pill">
                                <template #suffix>
                                    <div class="cursor-pointer" @click="showPassword = !showPassword">
                                        <el-icon class="mt-1"><View v-if="!showPassword" /><Hide v-else /></el-icon>
                                    </div>
                                </template>
                            </el-input>
                        </div>

                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <el-checkbox v-model="loginForm.remember" label="Ghi nhớ danh tính" class="tiny-check" />
                        </div>

                        <button type="submit" class="auth-btn-v3 w-100 rounded-pill py-3 fw-black tracking-widest text-uppercase shadow-sm" :disabled="loading">
                            <template v-if="!loading">Tiến vào Hệ Thống</template>
                            <template v-else><el-icon class="is-loading"><Timer /></el-icon> Đang Xác Thực</template>
                        </button>
                    </form>

                    <div class="mt-5 pt-4 text-center border-top border-light opacity-50">
                        <p class="ultra-tiny text-muted fw-bold">ONTOP LOGISTICS • SECURED GATEWAY 2.0</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');

.login-premium-v3 {
    font-family: 'Plus Jakarta Sans', sans-serif;
    background-color: #0f172a;
}

.fw-black { font-weight: 850; }
.ls-tight { letter-spacing: -1px; }
.ultra-tiny { font-size: 9px; }
.tracking-wider { letter-spacing: 1px; }
.tracking-widest { letter-spacing: 2px; }

/* Animated Mesh Mesh Background */
.mesh-bg {
    filter: blur(80px);
    overflow: hidden;
}

.mesh-blob {
    position: absolute;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    animation: move 20s infinite alternate;
}

.blob-1 { background: rgba(79, 70, 229, 0.4); top: -10%; left: -10%; }
.blob-2 { background: rgba(236, 72, 153, 0.3); bottom: -10%; right: -10%; animation-delay: -5s; }
.blob-3 { background: rgba(14, 165, 233, 0.3); top: 50%; left: 50%; translate: -50% -50%; animation-delay: -10s; }

@keyframes move {
    0% { transform: translate(0, 0) scale(1); }
    100% { transform: translate(50px, 100px) scale(1.1); }
}

/* Glass UI */
.glass-morphism {
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
}

.logo-circle { width: 72px; height: 72px; }

.auth-input-pill :deep(.el-input__wrapper) {
    border-radius: 14px;
    padding: 10px 15px;
    background: #f8fafc;
    box-shadow: none;
    border: 1px solid #e2e8f0;
}

.auth-btn-v3 {
    background: linear-gradient(135deg, #4f46e5 0%, #3730a3 100%);
    color: #fff;
    border: none;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 10px 20px rgba(79, 70, 229, 0.3);
}

.auth-btn-v3:hover {
    transform: translateY(-2px);
    box-shadow: 0 15px 30px rgba(79, 70, 229, 0.4);
}

.auth-btn-v3:active { transform: scale(0.98); }

.tiny-check :deep(.el-checkbox__label) {
    font-size: 11px;
    font-weight: 700;
    color: #64748b;
}

@media (max-width: 991px) {
    .login-card-v3 { padding: 30px !important; }
}
</style>