<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { 
  User, Plus, Search, Lock, Unlock, 
  Connection, Setting, Calendar, InfoFilled 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const users = ref([])
const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const warehouses = ref([])

const fetchWarehouses = async () => {
    try {
        const response = await api.get('/warehouses')
        warehouses.value = response.data
    } catch (error) {
        console.error('Lỗi tải danh sách kho')
    }
}

const form = ref({
    username: '',
    password: '',
    roleName: '',
    warehouseId: null
})

const fetchUsers = async () => {
    try {
        loading.value = true
        const response = await api.get('/users')
        users.value = response.data
    } catch (error) {
        ElMessage.error('Không thể tải danh sách nhân sự')
    } finally {
        loading.value = false
    }
}

const fetchRoles = async () => {
    try {
        const response = await api.get('/users/roles')
        roles.value = response.data
    } catch (error) {
        console.error('Lỗi tải danh sách vai trò')
    }
}

const handleAddUser = async () => {
    try {
        await api.post('/users', form.value)
        ElMessage.success('Thêm nhân sự mới thành công')
        dialogVisible.value = false
        fetchUsers()
        form.value = { username: '', password: '', roleName: '' }
    } catch (error) {
        ElMessage.error('Lỗi khi tạo người dùng')
    }
}

const handleToggleLock = (user) => {
    const action = user.isActive ? 'Khóa' : 'Mở khóa'
    ElMessageBox.confirm(`${action} tài khoản này?`, 'Xác nhận thay đổi', {
        type: 'warning',
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy'
    }).then(async () => {
        try {
            await api.post(`/users/${user.id}/lock`)
            ElMessage.success(`Đã ${action.toLowerCase()} tài khoản thành công`)
            fetchUsers()
        } catch (error) {
            ElMessage.error('Thực hiện thất bại')
        }
    })
}

onMounted(() => {
    fetchUsers()
    fetchRoles()
    fetchWarehouses()
})
</script>

<template>
    <div class="user-management-premium p-4">
        <!-- Hero Header -->
        <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
            <div class="d-flex align-items-center gap-4">
                <div class="icon-box bg-primary text-white rounded-4 p-3 shadow">
                    <el-icon :size="32"><User /></el-icon>
                </div>
                <div>
                    <h3 class="fw-bold text-dark mb-1">Danh Mục Nhân Sự</h3>
                    <p class="text-muted small mb-0">Quản lý định danh, bảo mật và quyền truy cập kho bãi tập trung</p>
                </div>
            </div>
            <div class="d-flex gap-3">
                <el-input placeholder="Tìm nhanh nhân viên..." :prefix-icon="Search" style="width: 250px" class="premium-search" />
                <el-button type="primary" size="large" class="rounded-pill px-4 shadow-sm" :icon="Plus" @click="dialogVisible = true">Thêm nhân sự mới</el-button>
            </div>
        </div>

        <!-- Grid of User Cards -->
        <div class="row g-4" v-loading="loading">
            <div v-for="user in users" :key="user.id" class="col-xl-3 col-lg-4 col-md-6">
                <div class="user-card bg-white rounded-4 border-0 shadow-sm transition-all h-100 position-relative overflow-hidden">
                    <div :class="['status-strip', user.isActive ? 'bg-success' : 'bg-secondary']"></div>
                    
                    <div class="card-body p-4 pt-5">
                        <div class="d-flex flex-column align-items-center text-center">
                            <div class="avatar-wrapper mb-3 position-relative">
                                <el-avatar :size="80" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`" class="shadow-sm border border-4 border-white" />
                                <div :class="['status-dot', user.isActive ? 'active' : 'locked']"></div>
                            </div>
                            
                            <h5 class="fw-bold text-dark mb-1">{{ user.username }}</h5>
                            <div class="role-badge mb-3">
                                <el-tag :type="user.role?.roleName === 'ADMIN' ? 'danger' : 'primary'" size="small" effect="light" class="rounded-pill border-0 px-3">
                                    <el-icon class="me-1"><Lock /></el-icon>{{ user.role?.roleName }}
                                </el-tag>
                            </div>
                        </div>

                        <div class="user-details border-top border-light pt-3 mt-2">
                            <div class="d-flex align-items-center gap-3 mb-2">
                                <el-icon class="text-muted"><Connection /></el-icon>
                                <span class="small text-dark fw-medium">Kho: {{ user.warehouse?.name || 'Toàn hệ thống' }}</span>
                            </div>
                            <div class="d-flex align-items-center gap-3">
                                <el-icon class="text-muted"><InfoFilled /></el-icon>
                                <span class="small text-muted">ID: #{{ user.id }}</span>
                            </div>
                        </div>

                        <div class="card-actions d-flex justify-content-between align-items-center mt-4 pt-3 border-top border-light">
                            <div :class="['status-label small fw-bold text-uppercase tracking-wider', user.isActive ? 'text-success' : 'text-secondary']">
                                {{ user.isActive ? 'ĐANG HOẠT ĐỘNG' : 'ĐÃ TỪ CHỐI/KHÓA' }}
                            </div>
                            <el-tooltip :content="user.isActive ? 'Khóa tài khoản' : 'Mở khóa tài khoản'" placement="top">
                                <el-button 
                                    :type="user.isActive ? 'warning' : 'success'" 
                                    :icon="user.isActive ? markRaw(Lock) : markRaw(Unlock)" 
                                    circle 
                                    plain 
                                    size="default"
                                    @click="handleToggleLock(user)"
                                    :disabled="user.username === 'admin'"
                                />
                            </el-tooltip>
                        </div>
                    </div>
                </div>
            </div>
            
            <div v-if="users.length === 0" class="col-12 text-center py-5">
                <el-empty description="Không có dữ liệu nhân sự" />
            </div>
        </div>

        <!-- Create User Dialog -->
        <el-dialog v-model="dialogVisible" title="Thiết Lập Tài Khoản Mới" width="500px" align-center class="premium-dialog rounded-4">
            <div class="px-3 pb-3">
                <div class="text-center mb-4">
                    <div class="dialog-icon-box bg-primary bg-opacity-10 text-primary mx-auto rounded-circle d-flex align-items-center justify-content-center mb-2" style="width: 60px; height: 60px;">
                        <el-icon :size="24"><Plus /></el-icon>
                    </div>
                    <p class="text-muted small">Cung cấp thông tin danh định và quyền hạn cho nhân viên mới</p>
                </div>
                
                <el-form :model="form" label-position="top" class="premium-form">
                    <el-row :gutter="20">
                        <col-12>
                            <el-form-item label="Tên đăng nhập (Username)" required>
                                <el-input v-model="form.username" placeholder="Ví dụ: nvkho_01" :prefix-icon="User" />
                            </el-form-item>
                        </col-12>
                        <col-12>
                            <el-form-item label="Mật khẩu khởi tạo" required>
                                <el-input v-model="form.password" type="password" show-password placeholder="Tối thiểu 6 ký tự" :prefix-icon="Lock" />
                            </el-form-item>
                        </col-12>
                        <div class="col-md-6">
                            <el-form-item label="Phân quyền (Role)" required>
                                <el-select v-model="form.roleName" placeholder="Chọn vai trò" class="w-100">
                                    <template #prefix><el-icon><Setting /></el-icon></template>
                                    <el-option label="Admin" value="ADMIN" />
                                    <el-option label="Manager" value="MANAGER" />
                                    <el-option label="Staff" value="STAFF" />
                                </el-select>
                            </el-form-item>
                        </div>
                        <div class="col-md-6">
                            <el-form-item label="Gán vào Kho">
                                <el-select v-model="form.warehouseId" placeholder="Mặc định" class="w-100" clearable :disabled="form.roleName === 'ADMIN'">
                                    <template #prefix><el-icon><Connection /></el-icon></template>
                                    <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
                                </el-select>
                            </el-form-item>
                        </div>
                    </el-row>
                </el-form>
            </div>
            <template #footer>
                <div class="d-flex justify-content-end gap-2 px-3 pb-3">
                    <el-button @click="dialogVisible = false" class="rounded-pill px-4">Hủy bỏ</el-button>
                    <el-button type="primary" @click="handleAddUser" class="rounded-pill px-4 shadow-sm">Kích hoạt tài khoản</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.user-management-premium {
    background-color: #f8fafc;
    min-height: 100vh;
    font-family: 'Inter', sans-serif;
}

.glass-hero {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
}

.user-card {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1) !important;
}

.status-strip {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
}

.status-dot {
    position: absolute;
    bottom: 5px;
    right: 5px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 3px solid #fff;
}

.status-dot.active { background-color: #10b981; box-shadow: 0 0 10px rgba(16, 185, 129, 0.5); }
.status-dot.locked { background-color: #94a3b8; }

.icon-box {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.tracking-wider {
    letter-spacing: 0.05em;
}

.premium-search :deep(.el-input__wrapper) {
    border-radius: 99px;
    box-shadow: none;
    border: 1px solid #e2e8f0;
}

.premium-dialog :deep(.el-dialog__header) {
    padding-top: 30px;
    text-align: center;
}

.premium-dialog :deep(.el-dialog__title) {
    font-weight: 800;
}

.premium-form :deep(.el-form-item__label) {
    font-weight: 600;
    margin-bottom: 8px;
    color: #475569;
}

.premium-form :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 10px 15px;
}
</style>
