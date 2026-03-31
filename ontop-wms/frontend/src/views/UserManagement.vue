<script setup>
import { ref, onMounted } from 'vue'
import { 
  User as UserIcon, 
  Plus as PlusIcon, 
  Search as SearchIcon, 
  Delete as DeleteIcon, 
  Edit as EditIcon, 
  Lock as LockIcon, 
  Connection as ConnectionIcon 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const users = ref([])
const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)

const warehouses = ref([])

const handleAuthError = (error) => {
    const status = error.response?.status;
    const backendError = error.response?.data?.error || error.response?.data?.message;

    if (status === 401) {
        ElMessage.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
        localStorage.clear()
        window.location.href = '/login'
    } else if (status === 403 || (status === 400 && backendError === 'Access Denied')) {
        ElMessage.error(`Từ chối truy cập (403): ${backendError || 'Bạn không đủ quyền.'}`)
    } else if (status === 400) {
        console.error('Lỗi 400 từ API:', error.response?.data)
        ElMessage.error(`Lỗi API (400): ${backendError || 'Yêu cầu không hợp lệ hoặc lỗi phân quyền.'}`)
    }
}

const fetchWarehouses = async () => {
    try {
        const response = await api.get('/warehouses')
        warehouses.value = response.data
    } catch (error) {
        if (error.response?.status === 403 || error.response?.status === 400) {
            handleAuthError(error)
        } else {
            console.error('Lỗi tải danh sách kho', error)
        }
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
        if (error.response?.status === 403 || error.response?.status === 400) {
            handleAuthError(error)
        } else {
            console.error('Lỗi tải danh sách user:', error)
            ElMessage.error('Không thể tải danh sách nhân sự')
        }
    } finally {
        loading.value = false
    }
}

const fetchRoles = async () => {
    try {
        const response = await api.get('/users/roles')
        roles.value = response.data
    } catch (error) {
        if (error.response?.status === 403 || error.response?.status === 400) {
            handleAuthError(error)
        } else {
            console.error('Lỗi tải danh sách vai trò', error)
        }
    }
}

const handleAddUser = async () => {
    try {
        // Nếu vai trò là ADMIN, đảm bảo warehouseId là null
        if (form.value.roleName === 'ADMIN') {
            form.value.warehouseId = null;
        }
        await api.post('/users', form.value)
        ElMessage.success('Thêm nhân sự mới thành công')
        dialogVisible.value = false
        fetchUsers()
        form.value = { username: '', password: '', roleName: '', warehouseId: null }
    } catch (error) {
        if (error.response?.status === 403 || error.response?.status === 400) {
            handleAuthError(error)
        } else {
            console.error(error)
            ElMessage.error(error.response?.data?.message || 'Lỗi khi tạo người dùng')
        }
    }
}

const handleDelete = (id) => {
    ElMessageBox.confirm('Xóa nhân sự này khỏi hệ thống?', 'Cảnh báo', {
        type: 'warning',
        confirmButtonText: 'Xác nhận xóa'
    }).then(async () => {
        await api.delete(`/users/${id}`)
        ElMessage.success('Đã xóa nhân sự')
        fetchUsers()
    })
}

onMounted(() => {
    const userRole = localStorage.getItem('userRole');
    const userId = localStorage.getItem('userId'); // Giả định userId được lưu khi login
    if (userRole === 'ADMIN' || userRole === 'MANAGER' || userId === '1') {
        fetchUsers()
        fetchRoles()
        fetchWarehouses()
    }
})
</script>

<template>
    <div class="user-management">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h4 class="fw-bold mb-1">Quản lý nhân sự</h4>
                <p class="text-muted small mb-0">Quản lý tài khoản và phân quyền truy cập hệ thống</p>
            </div>
            <el-button type="primary" :icon="PlusIcon" @click="dialogVisible = true">Thêm nhân sự mới</el-button>
        </div>

        <div class="card border-0 shadow-sm overflow-hidden" v-loading="loading">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="bg-light">
                        <tr>
                            <th class="ps-4">Nhân viên</th>
                            <th>Vai trò</th>
                            <th>Kho bãi</th>
                            <th>ID Hệ thống</th>
                            <th>Trạng thái</th>
                            <th class="text-end pe-4">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="user in users" :key="user.id">
                            <td class="ps-4">
                                <div class="d-flex align-items-center gap-3">
                                    <el-avatar :size="36"
                                        :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`" />
                                    <div>
                                        <h6 class="mb-0 fw-bold">{{ user.username }}</h6>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span :class="['badge rounded-pill px-3',
                                    user.role?.roleName === 'ADMIN' ? 'bg-danger bg-opacity-10 text-danger' :
                                        user.role?.roleName === 'MANAGER' ? 'bg-primary bg-opacity-10 text-primary' : 'bg-info bg-opacity-10 text-info'
                                ]">
                                    <el-icon class="me-1">
                                        <LockIcon />
                                    </el-icon>
                                    {{ user.role?.roleName }}
                                </span>
                            </td>
                            <td>
                                <span v-if="user.warehouse" class="text-dark small">
                                    <el-icon class="me-1">
                                        <ConnectionIcon />
                                    </el-icon>{{ user.warehouse.name }} <span class="text-muted">(ID: {{ user.warehouse.id }})</span>
                                </span>
                                <span v-else class="text-muted small italic">Tất cả kho</span>
                            </td>
                            <td>#{{ user.id }}</td>
                            <td>
                                <el-tag :type="user.isActive !== false ? 'success' : 'danger'" size="small">
                                    {{ user.isActive !== false ? 'Đang hoạt động' : 'Bị khóa' }}
                                </el-tag>
                            </td>
                            <td class="text-end pe-4">
                                <el-button size="small" :icon="DeleteIcon" type="danger" plain
                                    @click="handleDelete(user.id)" circle></el-button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Add User Dialog -->
        <el-dialog v-model="dialogVisible" title="Tạo tài khoản nhân sự" width="450px">
            <el-form :model="form" label-position="top">
                <el-form-item label="Tên đăng nhập" required>
                    <el-input v-model="form.username" placeholder="Ví dụ: nhanvien_kho1" />
                </el-form-item>
                <el-form-item label="Mật khẩu" required>
                    <el-input v-model="form.password" type="password" show-password placeholder="Nhập mật khẩu" />
                </el-form-item>
                <el-form-item label="Phân quyền vai trò" required>
                    <el-select v-model="form.roleName" placeholder="Chọn vai trò truy cập" class="w-100">
                        <el-option label="Quản trị viên (ADMIN)" value="ADMIN" />
                        <el-option label="Quản lý kho (MANAGER)" value="MANAGER" />
                        <el-option label="Nhân viên kho (STAFF)" value="STAFF" />
                    </el-select>
                </el-form-item>
                <el-form-item label="Gán vào Kho" v-if="form.roleName !== 'ADMIN'">
                    <el-select v-model="form.warehouseId" placeholder="Chọn kho làm việc" class="w-100" clearable>
                        <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">Hủy</el-button>
                <el-button type="primary" @click="handleAddUser">Xác nhận tạo</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
th {
    padding: 15px 10px;
    color: #64748b;
    font-size: 13px;
    font-weight: 600;
    text-transform: uppercase;
}

td {
    padding: 15px 10px;
}
</style>
