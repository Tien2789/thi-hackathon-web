<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Box, CircleCheck, Timer, Search, View, Stamp, Camera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const inbounds = ref([])
const products = ref([])
const loading = ref(false)

const fetchProducts = async () => {
  try {
    const response = await api.get('/products')
    products.value = response.data
  } catch (error) {
    console.error('Lỗi tải sản phẩm:', error)
  }
}

const fetchInbounds = async () => {
  try {
    loading.value = true
    const response = await api.get('/inventory/inbounds')
    inbounds.value = response.data
  } catch (error) {
    console.error('Lỗi tải phiếu nhập:', error)
    ElMessage.error('Không thể tải danh sách phiếu nhập')
  } finally {
    loading.value = false
  }
}

const showCreateModal = ref(false)
const createForm = ref({
  receiptCode: ''
})

const handleAddInbound = async () => {
  try {
    if (!createForm.value.receiptCode) {
      createForm.value.receiptCode = `IN-${Date.now()}`
    }
    await api.post('/inventory/inbounds', {
      receiptCode: createForm.value.receiptCode,
      status: 'PENDING'
    })
    ElMessage.success('Tạo phiếu nhập nháp thành công')
    showCreateModal.value = false
    fetchInbounds()
  } catch (error) {
    console.error('Lỗi tạo phiếu nhập:', error)
    ElMessage.error('Không thể tạo phiếu nhập')
  }
}

// Approval logic
const showApproveModal = ref(false)
const selectedInboundId = ref(null)
const approveForm = ref({
  productId: null,
  quantity: 1
})

const openApproveDialog = (id) => {
  selectedInboundId.value = id
  approveForm.value = { productId: null, quantity: 1 }
  showApproveModal.value = true
}

const confirmApprove = async () => {
  if (!approveForm.value.productId || approveForm.value.quantity <= 0) {
    ElMessage.warning('Vui lòng chọn sản phẩm và số lượng hợp lệ')
    return
  }

  try {
    await api.post(`/inventory/inbounds/${selectedInboundId.value}/approve`, {
      productId: approveForm.value.productId,
      quantity: approveForm.value.quantity
    })
    ElMessage.success('Phê duyệt nhập kho thành công, tồn kho đã cập nhật')
    showApproveModal.value = false
    fetchInbounds()
  } catch (error) {
    console.error('Lỗi phê duyệt:', error)
    ElMessage.error(error.response?.data || 'Không thể phê duyệt phiếu nhập')
  }
}

onMounted(() => {
  fetchInbounds()
  fetchProducts()
})
</script>

<template>
  <div class="inbound-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý nhập kho</h4>
        <p class="text-muted small mb-0">Theo dõi và phê duyệt các lô hàng nhập bãi</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="showCreateModal = true">Tạo phiếu nhập mới</el-button>
    </div>

    <!-- Stats Summary -->
    <div class="row g-4 mb-4">
      <div class="col-md-4">
        <div class="card border-0 shadow-sm bg-primary bg-opacity-10">
          <div class="card-body py-4">
            <h6 class="text-primary fw-bold small text-uppercase mb-2">Đang chờ duyệt</h6>
            <h2 class="fw-extrabold mb-0">{{inbounds.filter(i => i.status === 'PENDING').length}}</h2>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card border-0 shadow-sm">
          <div class="card-body py-4">
            <h6 class="text-muted fw-bold small text-uppercase mb-2">Đã phê duyệt</h6>
            <h2 class="fw-extrabold mb-0">{{inbounds.filter(i => i.status === 'APPROVED').length}}</h2>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card border-0 shadow-sm">
          <div class="card-body py-4">
            <h6 class="text-muted fw-bold small text-uppercase mb-2">Tổng số phiếu</h6>
            <h2 class="fw-extrabold mb-0">{{ inbounds.length }}</h2>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card border-0 shadow-sm overflow-hidden mb-4" v-loading="loading">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
            <tr>
              <th class="ps-4">Mã phiếu</th>
              <th>Thời gian tạo</th>
              <th>Trạng thái</th>
              <th>Người duyệt</th>
              <th class="text-end pe-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in inbounds" :key="item.id">
              <td class="ps-4 fw-bold text-primary">{{ item.receiptCode }}</td>
              <td>{{ item.createdAt ? new Date(item.createdAt).toLocaleString() : 'N/A' }}</td>
              <td>
                <span :class="['badge rounded-pill px-3',
                  item.status === 'APPROVED' ? 'bg-success bg-opacity-10 text-success' : 'bg-warning bg-opacity-10 text-warning'
                ]">
                  <el-icon class="me-1">
                    <component :is="item.status === 'APPROVED' ? CircleCheck : Timer" />
                  </el-icon>
                  {{ item.status === 'APPROVED' ? 'Đã duyệt' : 'Chờ duyệt' }}
                </span>
              </td>
              <td>{{ item.approvedBy?.username || '-' }}</td>
              <td class="text-end pe-4">
                <el-button-group>
                  <el-button size="small" :icon="View" plain>Chi tiết</el-button>
                  <el-button v-if="item.status === 'PENDING'" size="small" type="success" :icon="Stamp"
                    @click="openApproveDialog(item.id)">Phê duyệt</el-button>
                </el-button-group>
              </td>
            </tr>
            <tr v-if="inbounds.length === 0">
              <td colspan="5" class="text-center py-5 text-muted">Không có dữ liệu phiếu nhập</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="showCreateModal" title="Tạo phiếu nhập kho mới" width="500px">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="Mã phiếu nhập (Để trống để tự tạo)">
          <el-input v-model="createForm.receiptCode" placeholder="Ví dụ: PN-001" />
        </el-form-item>
        <div class="p-3 bg-light rounded-3 mb-1">
          <p class="small text-muted mb-0">Phiếu nhập sẽ được tạo ở trạng thái <b>Chờ duyệt</b>. Bạn cần chọn sản phẩm
            và số lượng ở bước phê duyệt.</p>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateModal = false">Hủy bỏ</el-button>
          <el-button type="primary" @click="handleAddInbound">Tạo phiếu nháp</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveModal" title="Phê duyệt nhập kho" width="500px">
      <el-form :model="approveForm" label-position="top">
        <el-form-item label="Sản phẩm nhập kho" required>
          <el-select v-model="approveForm.productId" placeholder="Chọn sản phẩm" class="w-100" filterable>
            <el-option v-for="p in products" :key="p.id" :label="`${p.productName} (${p.skuCode})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Số lượng thực nhập" required>
          <el-input-number v-model="approveForm.quantity" :min="1" class="w-100" />
        </el-form-item>
        <div class="p-3 bg-success bg-opacity-10 border border-success border-opacity-25 rounded-3">
          <p class="small text-success fw-bold mb-1">Xác nhận</p>
          <p class="small text-dark mb-0">Sau khi duyệt, hệ thống sẽ cộng dồn số lượng vào tồn kho và ghi lại nhật ký
            giao
            dịch.</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showApproveModal = false">Hủy</el-button>
        <el-button type="success" @click="confirmApprove">Xác nhận Duyệt</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.fw-extrabold {
  font-weight: 800;
}

th {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  padding: 15px 10px;
}

td {
  padding: 15px 10px;
}
</style>
