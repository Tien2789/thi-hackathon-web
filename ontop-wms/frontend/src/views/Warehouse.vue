<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { 
  School, MapLocation, User, PieChart, 
  Menu as IconMenu, Edit, Delete, Connection,
  LocationFilled, Management, Histogram
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import api from '../api'

const warehouses = ref([])
const loading = ref(false)
const isEdit = ref(false)
const userRole = localStorage.getItem('userRole')
const isAdmin = userRole === 'ADMIN'

const fetchWarehouses = async () => {
  try {
    loading.value = true
    const response = await api.get('/warehouses')
    warehouses.value = response.data
  } catch (error) {
    console.error('Lỗi tải danh sách kho:', error)
    ElMessage.error('Không thể tải danh sách kho')
  } finally {
    loading.value = false
  }
}

const showAddModal = ref(false)
const whForm = ref({
  id: null,
  name: '',
  code: '',
  location: '',
  manager: '',
  capacity: 0
})

const openAddDialog = () => {
  isEdit.value = false
  whForm.value = { id: null, name: '', code: '', location: '', manager: '', capacity: 0 }
  showAddModal.value = true
}

const handleEdit = (wh) => {
  isEdit.value = true
  whForm.value = { ...wh }
  showAddModal.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('Hành động này không thể hoàn tác. Bạn có chắc chắn muốn xóa kho này?', 'Xác nhận xóa hệ thống', {
      confirmButtonText: 'Xóa vĩnh viễn',
      cancelButtonText: 'Hủy',
      type: 'warning'
    })

    await api.delete(`/warehouses/${id}`)
    ElMessage.success('Đã xóa cơ sở kho thành công')
    fetchWarehouses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Không thể xóa kho do ràng buộc dữ liệu')
    }
  }
}

const handleSaveWarehouse = async () => {
  try {
    if (isEdit.value) {
      await api.put(`/warehouses/${whForm.value.id}`, whForm.value)
      ElMessage.success('Cập nhật thông tin kho thành công')
    } else {
      await api.post('/warehouses', whForm.value)
      ElMessage.success('Đã khởi tạo cơ sở kho mới')
    }
    showAddModal.value = false
    fetchWarehouses()
  } catch (error) {
    ElMessage.error('Lỗi lưu trữ thông tin cơ sở')
  }
}

onMounted(() => {
  fetchWarehouses()
})
</script>

<template>
  <div class="warehouse-view-premium p-4">
    <!-- Hero Header -->
    <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
        <div class="d-flex align-items-center gap-4">
            <div class="icon-box-warehouse bg-primary text-white rounded-4 p-3 shadow-primary">
                <el-icon :size="32"><Connection /></el-icon>
            </div>
            <div>
                <h3 class="fw-black text-dark mb-1 ls-tight">Cơ Cấu Kho Bãi</h3>
                <p class="text-muted small mb-0 fw-medium">Quản lý các điểm lưu kho, phân phối và theo dõi công suất vận hành</p>
            </div>
        </div>
        <div class="d-flex gap-3">
            <el-button v-if="isAdmin" type="primary" size="large" class="rounded-pill px-4 shadow-sm fw-bold" :icon="Plus" @click="openAddDialog">Thêm cơ sở mới</el-button>
        </div>
    </div>

    <!-- Warehouse Grid -->
    <div class="row g-4 mb-5" v-loading="loading">
      <div v-for="wh in warehouses" :key="wh.id" class="col-12 col-xl-4 col-md-6">
        <div class="wh-premium-card bg-white rounded-4 border-0 shadow-sm transition-all overflow-hidden h-100 position-relative">
          <div class="card-strip" :style="{ backgroundColor: wh.capacity > 90 ? '#ef4444' : '#4f46e5' }"></div>
          
          <div class="p-4 pt-5">
            <div class="d-flex justify-content-between align-items-start mb-4">
               <div>
                  <h4 class="fw-bold text-dark mb-1 ls-tight">{{ wh.name }}</h4>
                  <span class="badge rounded-pill bg-light text-primary border border-primary border-opacity-10 ultra-tiny fw-bold tracking-widest px-2 py-1">CODE: {{ wh.code }}</span>
               </div>
               <div class="d-flex gap-1" v-if="isAdmin">
                  <el-button circle :icon="markRaw(Edit)" size="small" type="primary" plain @click="handleEdit(wh)" />
                  <el-button circle :icon="markRaw(Delete)" size="small" type="danger" plain @click="handleDelete(wh.id)" />
               </div>
            </div>

            <div class="d-flex align-items-center gap-3 mb-4 text-muted small">
                <el-icon :size="18" class="text-primary opacity-75"><LocationFilled /></el-icon>
                <span class="fw-medium">{{ wh.location || 'Chưa cập nhật địa chỉ' }}</span>
            </div>

            <div class="capacity-gauge p-3 rounded-4 bg-light mb-4 position-relative">
                <div class="d-flex justify-content-between mb-2">
                    <span class="ultra-tiny fw-bold text-muted">STORAGE CAPACITY</span>
                    <span :class="['tiny fw-black', wh.capacity > 90 ? 'text-danger' : 'text-primary']">{{ wh.capacity || 0 }}%</span>
                </div>
                <div class="progress rounded-pill shadow-inner" style="height: 10px;">
                    <div class="progress-bar rounded-pill transition-all" 
                         :class="wh.capacity > 90 ? 'bg-danger' : 'bg-primary'" 
                         role="progressbar" 
                         :style="{ width: (wh.capacity || 0) + '%' }">
                         <div class="shimmer-effect"></div>
                    </div>
                </div>
            </div>

            <div class="card-footer-v2 d-flex justify-content-between align-items-center mt-auto pt-3 border-top">
                <div class="d-flex align-items-center gap-2">
                    <el-avatar :size="28" src="https://api.dicebear.com/7.x/initials/svg?seed=Manager" class="shadow-sm" />
                    <div>
                        <p class="ultra-tiny text-muted mb-0 fw-bold">QUẢN LÝ</p>
                        <p class="tiny text-dark fw-bold mb-0">{{ wh.manager || 'N/A' }}</p>
                    </div>
                </div>
                <el-button type="primary" link class="fw-bold tiny" icon="Histogram">XEM BÁO CÁO</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="warehouses.length === 0" class="col-12 text-center py-5">
          <el-empty description="Hệ thống chưa có cơ sở kho nào" />
      </div>
    </div>

    <!-- Premium Add/Edit Dialog -->
    <el-dialog v-model="showAddModal" :title="isEdit ? 'Cấu Hình Cơ Sở Kho' : 'Thiết Lập Kho Mới'" width="550px" align-center class="premium-dialog rounded-4">
      <div class="px-3 pb-3">
        <el-form :model="whForm" label-position="top" class="premium-form">
            <div class="row g-3">
                <div class="col-12">
                    <el-form-item label="Tên gọi cơ sở kho" required>
                        <el-input v-model="whForm.name" placeholder="Ví dụ: Warehouse Central - Hà Nội" />
                    </el-form-item>
                </div>
                <div class="col-6">
                    <el-form-item label="Mã định danh (Code)" required>
                        <el-input v-model="whForm.code" placeholder="WH-001" :disabled="isEdit" />
                    </el-form-item>
                </div>
                <div class="col-6">
                    <el-form-item label="Ước tính công suất (%)">
                        <el-input-number v-model="whForm.capacity" :min="0" :max="100" class="w-100" />
                    </el-form-item>
                </div>
                <div class="col-12">
                    <el-form-item label="Địa chỉ/Vị trí địa lý">
                        <el-input v-model="whForm.location" placeholder="Số nhà, đường, quận/huyện, tỉnh" />
                    </el-form-item>
                </div>
                <div class="col-12">
                    <el-form-item label="Quản lý chi nhánh">
                        <el-input v-model="whForm.manager" placeholder="Họ và tên người chịu trách nhiệm" />
                    </el-form-item>
                </div>
            </div>
        </el-form>
      </div>
      <template #footer>
        <div class="d-flex justify-content-end gap-2 px-3 pb-3 border-top pt-3">
          <el-button @click="showAddModal = false" class="rounded-pill px-4">Hủy bỏ</el-button>
          <el-button type="primary" @click="handleSaveWarehouse" class="rounded-pill px-4 shadow-sm fw-bold">{{ isEdit ? 'Cập nhật hệ thống' : 'Khởi tạo ngay' }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.warehouse-view-premium {
    font-family: 'Plus Jakarta Sans', sans-serif;
}

.fw-black { font-weight: 850; }
.ls-tight { letter-spacing: -1px; }
.tiny { font-size: 10px; }
.ultra-tiny { font-size: 9px; }

.glass-hero {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
}

.icon-box-warehouse {
    background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%);
}

.shadow-primary { box-shadow: 0 10px 20px rgba(79, 70, 229, 0.3); }

.wh-premium-card {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.wh-premium-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0,0,0,0.1) !important;
}

.card-strip {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 6px;
    background: var(--primary);
}

.shimmer-effect {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    animation: shimmer 2s infinite;
}

@keyframes shimmer {
    0% { transform: translateX(-100%); }
    100% { transform: translateX(100%); }
}

.premium-dialog :deep(.el-dialog__title) {
    font-weight: 800;
}

.premium-form :deep(.el-form-item__label) {
    font-weight: 700;
    color: #475569;
    font-size: 12px;
}

.shadow-inner {
    box-shadow: inset 0 2px 4px rgba(0,0,0,0.06);
}
</style>