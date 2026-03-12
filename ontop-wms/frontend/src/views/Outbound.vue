<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Box, CircleCheck, Timer, View, Stamp, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const outbounds = ref([])
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

const fetchOutbounds = async () => {
  try {
    loading.value = true
    const response = await api.get('/inventory/outbounds')
    outbounds.value = response.data
  } catch (error) {
    console.error('Lỗi tải phiếu xuất:', error)
    ElMessage.error('Không thể tải danh sách phiếu xuất')
  } finally {
    loading.value = false
  }
}

const showCreateModal = ref(false)
const createForm = ref({
  issueCode: ''
})

const handleAddOutbound = async () => {
  try {
    if (!createForm.value.issueCode) {
      createForm.value.issueCode = `OUT-${Date.now()}`
    }
    await api.post('/inventory/outbounds', {
      issueCode: createForm.value.issueCode,
      status: 'PENDING'
    })
    ElMessage.success('Tạo lệnh xuất nháp thành công')
    showCreateModal.value = false
    fetchOutbounds()
  } catch (error) {
    console.error('Lỗi tạo lệnh xuất:', error)
    ElMessage.error('Không thể tạo lệnh xuất')
  }
}

// Approval logic
const showApproveModal = ref(false)
const selectedOutboundId = ref(null)
const approveForm = ref({
  productId: null,
  quantity: 1
})

const openApproveDialog = (id) => {
  selectedOutboundId.value = id
  approveForm.value = { productId: null, quantity: 1 }
  showApproveModal.value = true
}

const confirmApprove = async () => {
  if (!approveForm.value.productId || approveForm.value.quantity <= 0) {
    ElMessage.warning('Vui lòng chọn sản phẩm và số lượng hợp lệ')
    return
  }

  try {
    await api.post(`/inventory/outbounds/${selectedOutboundId.value}/approve`, {
      productId: approveForm.value.productId,
      quantity: approveForm.value.quantity
    })
    ElMessage.success('Phê duyệt xuất kho thành công, tồn kho đã trừ')
    showApproveModal.value = false
    fetchOutbounds()
  } catch (error) {
    console.error('Lỗi phê duyệt:', error)
    ElMessage.error(error.response?.data || 'Không thể phê duyệt lệnh xuất')
  }
}

const handleUndo = (id) => {
  ElMessageBox.confirm('Hành động này sẽ hoàn tác việc xuất kho và cộng lại tồn kho. Bạn có chắc chắn?', 'Xác nhận hoàn tác', {
    confirmButtonText: 'Xác nhận',
    cancelButtonText: 'Hủy',
    type: 'warning'
  }).then(async () => {
    try {
      await api.post(`/inventory/outbounds/${id}/undo`)
      ElMessage.success('Hoàn tác thành công')
      fetchOutbounds()
    } catch (error) {
      ElMessage.error(error.response?.data || 'Không thể hoàn tác')
    }
  })
}

onMounted(() => {
  fetchOutbounds()
  fetchProducts()
})
</script>

<template>
  <div class="outbound-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý xuất kho</h4>
        <p class="text-muted small mb-0">Điều phối và xác nhận lệnh xuất hàng</p>
      </div>
      <el-button type="warning" :icon="Plus" @click="showCreateModal = true">Tạo lệnh xuất mới</el-button>
    </div>

    <!-- Stats Summary -->
    <div class="row g-4 mb-4">
      <div class="col-md-3">
        <div class="card border-0 shadow-sm">
          <div class="card-body py-3">
            <h6 class="text-muted fw-bold small text-uppercase mb-1">Chờ xử lý</h6>
            <h3 class="fw-extrabold mb-0 text-warning">{{outbounds.filter(o => o.status === 'PENDING').length}}</h3>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card border-0 shadow-sm border-start border-4 border-success">
          <div class="card-body py-3">
            <h6 class="text-muted fw-bold small text-uppercase mb-1">Đã phê duyệt</h6>
            <h3 class="fw-extrabold mb-0 text-success">{{outbounds.filter(o => o.status === 'APPROVED').length}}</h3>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card border-0 shadow-sm border-start border-4 border-danger">
          <div class="card-body py-3">
            <h6 class="text-muted fw-bold small text-uppercase mb-1">Đã hủy/Hoàn tác</h6>
            <h3 class="fw-extrabold mb-0 text-danger">{{outbounds.filter(o => o.status === 'CANCELLED').length}}</h3>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card border-0 shadow-sm">
          <div class="card-body py-3">
            <h6 class="text-muted fw-bold small text-uppercase mb-1">Tổng cộng</h6>
            <h3 class="fw-extrabold mb-0">{{ outbounds.length }}</h3>
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
              <th class="ps-4">Mã vận đơn</th>
              <th>Ngày tạo</th>
              <th>Trạng thái</th>
              <th>Người duyệt</th>
              <th class="text-end pe-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in outbounds" :key="item.id">
              <td class="ps-4 fw-bold text-dark">{{ item.issueCode }}</td>
              <td>{{ item.createdAt ? new Date(item.createdAt).toLocaleString() : 'N/A' }}</td>
              <td>
                <span :class="['badge rounded-pill px-3',
                  item.status === 'APPROVED' ? 'bg-success bg-opacity-10 text-success' :
                    (item.status === 'CANCELLED' ? 'bg-danger bg-opacity-10 text-danger' : 'bg-warning bg-opacity-10 text-warning')
                ]">
                  <el-icon class="me-1">
                    <component :is="item.status === 'APPROVED' ? CircleCheck : Timer" />
                  </el-icon>
                  {{ item.status === 'APPROVED' ? 'Đã duyệt' : (item.status === 'CANCELLED' ? 'Đã hủy' : 'Chờ duyệt') }}
                </span>
              </td>
              <td>{{ item.approvedBy?.username || '-' }}</td>
              <td class="text-end pe-4">
                <el-button-group>
                  <el-button size="small" :icon="View" plain>Chi tiết</el-button>
                  <el-button v-if="item.status === 'PENDING'" size="small" type="success" :icon="Stamp"
                    @click="openApproveDialog(item.id)">Duyệt xuất</el-button>
                  <el-button v-if="item.status === 'APPROVED'" size="small" type="danger" plain :icon="RefreshLeft"
                    @click="handleUndo(item.id)">Hoàn tác</el-button>
                </el-button-group>
              </td>
            </tr>
            <tr v-if="outbounds.length === 0">
              <td colspan="5" class="text-center py-5 text-muted">Không có dữ liệu lệnh xuất</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="showCreateModal" title="Tạo lệnh xuất kho mới" width="500px">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="Mã lệnh xuất (Để trống để tự tạo)">
          <el-input v-model="createForm.issueCode" placeholder="Ví dụ: PX-001" />
        </el-form-item>
        <div class="p-3 bg-light rounded-3 mb-1">
          <p class="small text-muted mb-0">Lệnh xuất sẽ được tạo ở trạng thái <b>Chờ duyệt</b>. Hệ thống sẽ kiểm tra tồn
            kho tại bước phê duyệt thực tế.</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showCreateModal = false">Hủy bỏ</el-button>
        <el-button type="warning" @click="handleAddOutbound">Tạo lệnh nháp</el-button>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveModal" title="Phê duyệt xuất kho" width="500px">
      <el-form :model="approveForm" label-position="top">
        <el-form-item label="Sản phẩm xuất kho" required>
          <el-select v-model="approveForm.productId" placeholder="Chọn sản phẩm" class="w-100" filterable>
            <el-option v-for="p in products" :key="p.id"
              :label="`${p.productName} (${p.skuCode}) - Tồn: ${p.currentStock}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Số lượng xuất" required>
          <el-input-number v-model="approveForm.quantity" :min="1" class="w-100" />
        </el-form-item>
        <div class="p-3 bg-warning bg-opacity-10 border border-warning border-opacity-25 rounded-3">
          <p class="small text-warning fw-bold mb-1">Xác nhận</p>
          <p class="small text-dark mb-0">Hệ thống sẽ kiểm tra tồn kho. Nếu đủ số lượng, tồn kho sẽ bị trừ ngay lập tức
            và
            ghi log giao dịch.</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showApproveModal = false">Hủy</el-button>
        <el-button type="warning" @click="confirmApprove">Phê duyệt Xuất</el-button>
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