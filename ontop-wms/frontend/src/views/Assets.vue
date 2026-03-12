<script setup>
import { ref, onMounted } from 'vue'
import { Tools, Plus, Search, Edit, Delete, Warning, InfoFilled, Wallet } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const assets = ref([])
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

const fetchAssets = async () => {
  try {
    loading.value = true
    const response = await api.get('/assets')
    assets.value = response.data
  } catch (error) {
    console.error('Lỗi tải tài sản:', error)
    ElMessage.error('Không thể tải danh sách tài sản')
  } finally {
    loading.value = false
  }
}

const showAssetModal = ref(false)
const assetForm = ref({
  product: { id: null },
  conditionStatus: 'Tốt',
  assetCode: ''
})

const handleAddAsset = async () => {
  try {
    if (!assetForm.value.product.id) {
      ElMessage.warning('Vui lòng chọn sản phẩm liên kết')
      return
    }
    if (!assetForm.value.assetCode) {
      assetForm.value.assetCode = `AST-${Date.now()}`
    }

    await api.post('/assets', {
      product: assetForm.value.product,
      assetCode: assetForm.value.assetCode,
      conditionStatus: assetForm.value.conditionStatus
    })

    ElMessage.success('Đăng ký tài sản thành công')
    showAssetModal.value = false
    fetchAssets()
  } catch (error) {
    console.error('Lỗi lưu tài sản:', error)
    ElMessage.error('Không thể lưu tài sản')
  }
}

const handleGetDepreciation = async (id) => {
  try {
    const response = await api.get(`/assets/${id}/depreciation`)
    const val = response.data.depreciationValue
    ElMessageBox.alert(`Giá trị khấu hao hiện tại ước tính: ${val.toFixed(2)}%`, 'Khấu hao tài sản', {
      confirmButtonText: 'Đóng'
    })
  } catch (error) {
    ElMessage.error('Lỗi tính khấu hao')
  }
}

onMounted(() => {
  fetchAssets()
  fetchProducts()
})
</script>

<template>
  <div class="assets-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý tài sản</h4>
        <p class="text-muted small mb-0">Theo dõi trang thiết bị và tài sản cố định trong kho</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="showAssetModal = true">Thêm tài sản mới</el-button>
    </div>

    <!-- Assets Table -->
    <div class="card border-0 shadow-sm overflow-hidden mb-4" v-loading="loading">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
            <tr>
              <th class="ps-4">Tên tài sản (Sản phẩm)</th>
              <th>Mã TS</th>
              <th>Loại sản phẩm</th>
              <th>Ngày đăng ký</th>
              <th>Tình trạng</th>
              <th class="text-end pe-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="asset in assets" :key="asset.id">
              <td class="ps-4">
                <div class="fw-bold">{{ asset.product?.productName || 'N/A' }}</div>
                <div class="text-muted small">SKU: {{ asset.product?.skuCode || '---' }}</div>
              </td>
              <td><span class="badge bg-light text-dark border">{{ asset.assetCode }}</span></td>
              <td>{{ asset.product?.category?.name || '---' }}</td>
              <td>{{ asset.createdAt ? new Date(asset.createdAt).toLocaleDateString() : 'N/A' }}</td>
              <td>
                <span :class="['badge rounded-pill px-3',
                  asset.conditionStatus === 'Tốt' ? 'bg-success bg-opacity-10 text-success' : 'bg-warning bg-opacity-10 text-warning'
                ]">
                  <el-icon class="me-1">
                    <Tools v-if="asset.conditionStatus === 'Tốt'" />
                    <Warning v-else />
                  </el-icon>
                  {{ asset.conditionStatus }}
                </span>
              </td>
              <td class="text-end pe-4">
                <el-button size="small" :icon="Wallet" @click="handleGetDepreciation(asset.id)" plain>Khấu
                  hao</el-button>
                <el-button size="small" :icon="Edit" plain circle></el-button>
              </td>
            </tr>
            <tr v-if="assets.length === 0">
              <td colspan="6" class="text-center py-5 text-muted">Chưa có tài sản nào được đăng ký</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Add Asset Dialog -->
    <el-dialog v-model="showAssetModal" title="Đăng ký tài sản cố định" width="500px">
      <el-form :model="assetForm" label-position="top">
        <el-form-item label="Chọn sản phẩm/thiết bị từ kho" required>
          <el-select v-model="assetForm.product.id" placeholder="Tìm sản phẩm" class="w-100" filterable>
            <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Mã định danh tài sản">
          <el-input v-model="assetForm.assetCode" placeholder="Ví dụ: FORKLIFT-01" />
        </el-form-item>
        <el-form-item label="Tình trạng hiện tại">
          <el-radio-group v-model="assetForm.conditionStatus">
            <el-radio-button label="Tốt">Hoạt động tốt</el-radio-button>
            <el-radio-button label="Cần bảo trì">Cần bảo trì</el-radio-button>
            <el-radio-button label="Hỏng">Hỏng</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <div class="p-3 bg-info bg-opacity-10 border border-info border-opacity-25 rounded-3 mb-2">
          <p class="small text-info fw-bold mb-1"><el-icon>
              <InfoFilled />
            </el-icon> Thông tin</p>
          <p class="small text-dark mb-0">Tài sản sẽ được tự động tính khấu hao mô phỏng dựa trên thời gian kể từ ngày
            đăng ký.</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAssetModal = false">Hủy</el-button>
        <el-button type="primary" @click="handleAddAsset">Xác nhận Đăng ký</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
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