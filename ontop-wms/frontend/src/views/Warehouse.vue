<script setup>
import { ref, onMounted } from 'vue'
import { School, MapLocation, User, PieChart, Menu as IconMenu, Edit, Delete } from '@element-plus/icons-vue'
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
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xóa kho này?', 'Cảnh báo', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning'
    })

    await api.delete(`/warehouses/${id}`)
    ElMessage.success('Xóa kho thành công')
    fetchWarehouses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Lỗi xóa kho:', error)
      ElMessage.error('Không thể xóa kho')
    }
  }
}

const handleSaveWarehouse = async () => {
  try {
    if (isEdit.value) {
      await api.put(`/warehouses/${whForm.value.id}`, whForm.value)
      ElMessage.success('Cập nhật kho thành công')
    } else {
      await api.post('/warehouses', whForm.value)
      ElMessage.success('Thêm kho mới thành công')
    }
    showAddModal.value = false
    whForm.value = { id: null, name: '', code: '', location: '', manager: '', capacity: 0 }
    fetchWarehouses()
  } catch (error) {
    console.error('Lỗi lưu kho:', error)
    ElMessage.error('Không thể lưu thông tin kho')
  }
}

onMounted(() => {
  fetchWarehouses()
})
</script>

<template>
  <div class="warehouse-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-bold mb-1">Hệ thống kho bãi</h4>
        <p class="text-muted small mb-0">Quản lý các địa điểm lưu kho toàn quốc</p>
      </div>
      <el-button v-if="isAdmin" type="primary" :icon="School" @click="openAddDialog">Thêm kho mới</el-button>
    </div>

    <div class="row g-4 mb-5">
      <div v-for="wh in warehouses" :key="wh.id" class="col-12 col-xl-4">
        <div class="card border-0 shadow-sm h-100 overflow-hidden wh-card transition-all">
          <div class="card-header border-0 bg-white p-4 pb-0 d-flex justify-content-between align-items-start">
            <div class="wh-icon bg-primary bg-opacity-10 text-primary rounded-3 p-3">
              <el-icon class="fs-3">
                <School />
              </el-icon>
            </div>
            <div class="d-flex gap-2 align-items-center">
              <span class="badge bg-light text-primary border border-primary border-opacity-25 fw-bold">{{ wh.code
              }}</span>
              <el-dropdown v-if="isAdmin" trigger="click">
                <el-button link :icon="Edit" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(wh)">Chỉnh sửa</el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(wh.id)" class="text-danger">Xóa kho</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div class="card-body p-4">
            <h5 class="fw-bold mb-1 text-dark">{{ wh.name }}</h5>
            <p class="text-muted small mb-3"><el-icon class="me-1">
                <MapLocation />
              </el-icon>{{ wh.location }}</p>

            <div class="p-3 bg-light rounded-3 mb-4">
              <div class="d-flex justify-content-between mb-2">
                <span class="small text-muted">Công suất sử dụng</span>
                <span :class="['small fw-bold', (wh.capacity || 0) > 90 ? 'text-danger' : 'text-primary']">{{
                  wh.capacity || 0
                }}%</span>
              </div>
              <div class="progress" style="height: 8px;">
                <div class="progress-bar" :class="(wh.capacity || 0) > 90 ? 'bg-danger' : 'bg-primary'"
                  role="progressbar" :style="{ width: (wh.capacity || 0) + '%' }"></div>
              </div>
              <p class="small text-muted mt-2 mb-0">Tồn kho hiện tại: <strong>{{ (wh.stock || 0).toLocaleString()
                  }}</strong> SKU
              </p>
            </div>

            <div class="d-flex align-items-center gap-2 mb-4">
              <el-avatar :size="24" class="bg-info">{{ (wh.manager || 'N').charAt(0) }}</el-avatar>
              <span class="small">Quản lý: <strong>{{ wh.manager || 'N/A' }}</strong></span>
            </div>

            <div class="d-grid gap-2">
              <el-button plain :icon="IconMenu">Quản lý sơ đồ kho</el-button>
              <el-button type="primary" link :icon="PieChart">Báo cáo hiệu suất</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Warehouse Dialog (Add/Edit) -->
    <el-dialog v-model="showAddModal" :title="isEdit ? 'Chỉnh sửa cơ sở kho bãi' : 'Thêm cơ sở kho bãi mới'" width="500"
      class="rounded-4">
      <el-form :model="whForm" label-position="top">
        <el-form-item label="Tên kho">
          <el-input v-model="whForm.name" placeholder="Ví dụ: Kho Tổng Miền Bắc" />
        </el-form-item>
        <div class="row">
          <div class="col-6">
            <el-form-item label="Mã kho">
              <el-input v-model="whForm.code" placeholder="WH-001" :disabled="isEdit" />
            </el-form-item>
          </div>
          <div class="col-6">
            <el-form-item label="Công suất (0-100%)">
              <el-input-number v-model="whForm.capacity" :min="0" :max="100" class="w-100" />
            </el-form-item>
          </div>
        </div>
        <el-form-item label="Địa chỉ">
          <el-input v-model="whForm.location" placeholder="Tỉnh/Thành phố, Quận/Huyện" />
        </el-form-item>
        <el-form-item label="Quản lý trưởng">
          <el-input v-model="whForm.manager" placeholder="Họ và tên người quản lý" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">Đóng</el-button>
        <el-button type="primary" @click="handleSaveWarehouse">{{ isEdit ? 'Cập nhật' : 'Lưu hệ thống' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.wh-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08) !important;
}

.wh-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>