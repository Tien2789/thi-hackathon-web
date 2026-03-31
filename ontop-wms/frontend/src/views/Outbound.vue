<script setup>
import { ref, onMounted } from 'vue'
import { 
  Plus as PlusIcon, 
  Box as BoxIcon, 
  CircleCheck as CircleCheckIcon, 
  Timer as TimerIcon, 
  View as ViewIcon, 
  Stamp as StampIcon, 
  RefreshLeft as RefreshLeftIcon 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import FormCircular200 from '../components/FormCircular200.vue'
import { Check as CheckIcon } from '@element-plus/icons-vue'

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
  issueCode: '',
  receiverName: '',
  reason: '',
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

const handleAddOutbound = async () => {
  try {
    if (!createForm.value.issueCode) {
      createForm.value.issueCode = `OUT-${Date.now()}`
    }
    await api.post('/inventory/outbounds', createForm.value)
    ElMessage.success('Tạo lệnh xuất và gửi yêu cầu ký duyệt thành công')
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

// Preview logic
const showPreviewModal = ref(false)
const previewData = ref({})
const openPreview = (item) => {
  previewData.value = { ...item, details: item.details || [] }
  showPreviewModal.value = true
}

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
    await api.post(`/inventory/outbounds/${selectedOutboundId.value}/approve`, approveForm.value)
    ElMessage.success('Phê duyệt xuất kho thành công (FIFO), tồn kho đã trừ')
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
      <el-button type="warning" :icon="PlusIcon" @click="showCreateModal = true">Tạo lệnh xuất mới</el-button>
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
              <th>Người nhận</th>
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
                    <component :is="item.status === 'APPROVED' ? CircleCheckIcon : TimerIcon" />
                  </el-icon>
                  {{ item.status === 'APPROVED' ? 'Đã duyệt' : (item.status === 'CANCELLED' ? 'Đã hủy' : (item.status === 'PENDING_SIGNATURE' ? 'Chờ ký' : 'Chờ duyệt')) }}
                </span>
              </td>
              <td>{{ item.receiverName || '-' }}</td>
              <td class="text-end pe-4">
                <el-button-group>
                  <el-button size="small" :icon="ViewIcon" plain @click="openPreview(item)">Xem mẫu 02-VT</el-button>
                  <el-button v-if="item.status === 'PENDING' || item.status === 'PENDING_SIGNATURE'" size="small" type="success" :icon="StampIcon"
                    @click="openApproveDialog(item.id)">Duyệt xuất</el-button>
                  <el-button v-if="item.status === 'APPROVED'" size="small" type="danger" plain :icon="RefreshLeftIcon"
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
    <el-dialog v-model="showCreateModal" title="Lập lệnh xuất kho (Mẫu 02-VT)" width="600px">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="Mã lệnh xuất">
          <el-input v-model="createForm.issueCode" placeholder="Tự động nếu để trống" />
        </el-form-item>
        <el-form-item label="Họ tên người nhận hàng">
          <el-input v-model="createForm.receiverName" />
        </el-form-item>
        <el-form-item label="Lý do xuất kho">
          <el-input v-model="createForm.reason" type="textarea" :rows="2" placeholder="Ví dụ: Xuất bán cho khách hàng A, Xuất lắp ráp..." />
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
        <el-button @click="showCreateModal = false">Hủy bỏ</el-button>
        <el-button type="warning" @click="handleAddOutbound">Lập lệnh & Gửi yêu cầu ký</el-button>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveModal" title="Xác nhận xuất kho (FIFO)" width="500px">
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
          <p class="small text-warning fw-bold mb-1">Quy tắc FIFO</p>
          <p class="small text-dark mb-0">Hệ thống sẽ tự động trừ vào các lô hàng cũ nhất nhập vào kho trước.</p>
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