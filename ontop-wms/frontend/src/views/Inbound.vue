<script setup>
import { ref, onMounted } from 'vue'
import { 
  Plus as PlusIcon, 
  Box as BoxIcon, 
  CircleCheck as CircleCheckIcon, 
  Timer as TimerIcon, 
  Search as SearchIcon, 
  View as ViewIcon, 
  Stamp as StampIcon, 
  Camera as CameraIcon 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import FormCircular200 from '../components/FormCircular200.vue'
import { Check as CheckIcon } from '@element-plus/icons-vue'

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
  receiptCode: '',
  delivererName: '',
  documentNumber: '',
  warehouseId: null,
  signerEmails: []
})

const newEmail = ref('')
const addEmail = () => {
  if (newEmail.value && !createForm.value.signerEmails.includes(newEmail.value)) {
    createForm.value.signerEmails.push(newEmail.value)
    newEmail.value = ''
  }
}
const removeEmail = (email) => {
  createForm.value.signerEmails = createForm.value.signerEmails.filter(e => e !== email)
}

const handleAddInbound = async () => {
  try {
    if (!createForm.value.receiptCode) {
      createForm.value.receiptCode = `IN-${Date.now()}`
    }
    await api.post('/inventory/inbounds', createForm.value)
    ElMessage.success('Tạo phiếu nhập và gửi yêu cầu ký duyệt thành công')
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
  quantity: 1,
  unitPrice: 0,
  expiryDate: null
})

// Preview logic
const showPreviewModal = ref(false)
const previewData = ref({})
const openPreview = (item) => {
  previewData.value = { ...item, details: item.details || [] }
  showPreviewModal.value = true
}

const openApproveDialog = (id) => {
  selectedInboundId.value = id
  approveForm.value = { productId: null, quantity: 1, unitPrice: 0, expiryDate: null }
  showApproveModal.value = true
}

const confirmApprove = async () => {
  if (!approveForm.value.productId || approveForm.value.quantity <= 0) {
    ElMessage.warning('Vui lòng chọn sản phẩm và số lượng hợp lệ')
    return
  }

  try {
    await api.post(`/inventory/inbounds/${selectedInboundId.value}/approve`, approveForm.value)
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
      <el-button type="primary" :icon="PlusIcon" @click="showCreateModal = true">Tạo phiếu nhập mới</el-button>
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
              <th>Người giao</th>
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
                    <component :is="item.status === 'APPROVED' ? CircleCheckIcon : TimerIcon" />
                  </el-icon>
                  {{ item.status === 'APPROVED' ? 'Đã duyệt' : (item.status === 'PENDING_SIGNATURE' ? 'Chờ ký' : 'Chờ duyệt') }}
                </span>
              </td>
              <td>{{ item.delivererName || '-' }}</td>
              <td class="text-end pe-4">
                <el-button-group>
                  <el-button size="small" :icon="ViewIcon" plain @click="openPreview(item)">Xem mẫu 01-VT</el-button>
                  <el-button v-if="item.status !== 'APPROVED'" size="small" type="success" :icon="StampIcon"
                    @click="openApproveDialog(item.id)">Nhập kho</el-button>
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
    <el-dialog v-model="showCreateModal" title="Lập phiếu nhập kho (Mẫu 01-VT)" width="600px">
      <el-form :model="createForm" label-position="top">
        <div class="row">
          <div class="col-md-6">
            <el-form-item label="Mã phiếu nhập">
              <el-input v-model="createForm.receiptCode" placeholder="Tự động nếu để trống" />
            </el-form-item>
          </div>
          <div class="col-md-6">
            <el-form-item label="Số chứng từ gốc">
              <el-input v-model="createForm.documentNumber" placeholder="Số hóa đơn/biên bản" />
            </el-form-item>
          </div>
        </div>
        <el-form-item label="Họ tên người giao hàng">
          <el-input v-model="createForm.delivererName" />
        </el-form-item>
        
        <el-divider content-position="left">Luồng ký duyệt điện tử</el-divider>
        <el-form-item label="Email các bên liên quan (để gửi link ký)">
          <div class="d-flex gap-2 mb-2">
            <el-input v-model="newEmail" placeholder="example@gmail.com" @keyup.enter="addEmail" />
            <el-button type="primary" @click="addEmail">Thêm</el-button>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <el-tag v-for="email in createForm.signerEmails" :key="email" closable @close="removeEmail(email)">
              {{ email }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateModal = false">Hủy bỏ</el-button>
          <el-button type="primary" @click="handleAddInbound">Lập phiếu & Gửi yêu cầu ký</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveModal" title="Thực nhập kho & Cập nhật tồn" width="500px">
      <el-form :model="approveForm" label-position="top">
        <el-form-item label="Sản phẩm nhập kho" required>
          <el-select v-model="approveForm.productId" placeholder="Chọn sản phẩm" class="w-100" filterable>
            <el-option v-for="p in products" :key="p.id" :label="`${p.productName} (${p.skuCode})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <div class="row">
          <div class="col-6">
            <el-form-item label="Số lượng thực nhập" required>
              <el-input-number v-model="approveForm.quantity" :min="1" class="w-100" />
            </el-form-item>
          </div>
          <div class="col-6">
            <el-form-item label="Đơn giá (VNĐ)">
              <el-input-number v-model="approveForm.unitPrice" :min="0" :step="1000" class="w-100" />
            </el-form-item>
          </div>
        </div>
        <el-form-item label="Hạn sử dụng">
          <el-date-picker v-model="approveForm.expiryDate" type="date" placeholder="Chọn ngày" class="w-100" format="DD/MM/YYYY" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApproveModal = false">Hủy</el-button>
        <el-button type="success" @click="confirmApprove">Xác nhận Nhập kho</el-button>
      </template>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog v-model="showPreviewModal" title="Xem trước biểu mẫu" width="1050px" top="5vh">
      <FormCircular200 type="inbound" :data="previewData" />
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
