<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { 
  Tools, Plus, Search, Edit, Delete, Warning, 
  InfoFilled, Wallet, Connection, Monitor,
  Compass, Histogram, Calendar, Management
} from '@element-plus/icons-vue'
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

    ElMessage({ message: 'Tài sản đã được vào sổ hệ thống', type: 'success' })
    showAssetModal.value = false
    fetchAssets()
  } catch (error) {
    ElMessage.error('Lỗi lưu trữ thông tin tài sản')
  }
}

const handleGetDepreciation = async (id, name) => {
  try {
    const response = await api.get(`/assets/${id}/depreciation`)
    const val = response.data.depreciationValue
    
    ElMessageBox.alert(
      `<div class="text-center">
        <h2 class="fw-black text-primary mb-1">${val.toFixed(2)}%</h2>
        <p class="text-muted small">Khấu hao ước tính của <strong>${name}</strong></p>
        <div class="progress rounded-pill mt-3" style="height: 10px;">
          <div class="progress-bar bg-primary" style="width: ${val}%"></div>
        </div>
      </div>`, 
      'Chỉ số Khấu hao', 
      { dangerouslyUseHTMLString: true, confirmButtonText: 'Đã hiểu' }
    )
  } catch (error) {
    ElMessage.error('Lỗi tính toán chỉ số khấu hao')
  }
}

onMounted(() => {
  fetchAssets()
  fetchProducts()
})
</script>

<template>
  <div class="assets-premium p-4" v-loading="loading">
    <!-- Hero Header -->
    <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
        <div class="d-flex align-items-center gap-4">
            <div class="icon-box-assets bg-amber text-white rounded-4 p-3 shadow-amber">
                <el-icon :size="32"><Monitor /></el-icon>
            </div>
            <div>
                <h3 class="fw-black text-dark mb-1 ls-tight">Asset & Equipment Registry</h3>
                <p class="text-muted small mb-0 fw-medium">Quản lý trang thiết bị, khấu hao tài sản cố định và lịch trình bảo trì định kỳ</p>
            </div>
        </div>
        <div class="d-flex gap-3">
            <el-button size="large" class="rounded-pill px-4" :icon="Calendar">Lịch bảo trì</el-button>
            <el-button type="warning" size="large" class="rounded-pill px-4 shadow-sm fw-bold border-0 text-white bg-amber" :icon="Plus" @click="showAssetModal = true">Khai báo tài sản</el-button>
        </div>
    </div>

    <!-- Asset Directory (Grid) -->
    <div class="row g-4 mb-5">
      <div v-for="asset in assets" :key="asset.id" class="col-12 col-md-6 col-xl-4">
        <div class="asset-premium-card bg-white rounded-4 shadow-sm border-0 transition-all overflow-hidden h-100 flex-column">
          <div class="p-4">
            <div class="d-flex justify-content-between align-items-start mb-4">
                <div class="asset-logo bg-light rounded-4 d-flex align-items-center justify-content-center text-amber">
                    <el-icon :size="24"><Tools /></el-icon>
                </div>
                <div class="d-flex flex-column align-items-end">
                    <span class="badge rounded-pill bg-amber bg-opacity-10 text-amber ultra-tiny fw-bold border-0 px-3 py-1 mb-1">{{ asset.assetCode }}</span>
                    <p class="mb-0 text-muted ultra-tiny fw-bold">{{ asset.product?.category?.name || 'LOGISTICS' }}</p>
                </div>
            </div>

            <h5 class="fw-bold text-dark mb-1 ls-tight">{{ asset.product?.productName || 'Tài sản chưa định danh' }}</h5>
            <p class="text-muted tiny fw-bold mb-3">MODEL: {{ asset.product?.skuCode || '---' }}</p>

            <div class="condition-badge-v2 d-flex align-items-center gap-2 p-2 rounded-pill bg-light mb-4">
                <div :class="['pulse-dot', asset.conditionStatus === 'Tốt' ? 'bg-success' : 'bg-warning']"></div>
                <span class="ultra-tiny fw-bold text-muted text-uppercase">{{ asset.conditionStatus }} CONDITION</span>
                <el-icon v-if="asset.conditionStatus !== 'Tốt'" class="ms-auto text-warning"><Warning /></el-icon>
            </div>

            <div class="asset-meta-row border-top pt-3 d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <el-icon class="text-muted"><Calendar /></el-icon>
                    <span class="ultra-tiny text-muted fw-bold">{{ asset.createdAt ? new Date(asset.createdAt).toLocaleDateString() : 'SINCE N/A' }}</span>
                </div>
                <div class="d-flex gap-2">
                    <el-button circle :icon="markRaw(Wallet)" size="small" type="primary" plain @click="handleGetDepreciation(asset.id, asset.product?.productName)" />
                    <el-button circle :icon="markRaw(Edit)" size="small" type="info" plain />
                </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="assets.length === 0" class="col-12 text-center py-5">
          <el-empty description="Chưa có tài sản cố định" />
      </div>
    </div>

    <!-- Premium Add Asset Dialog -->
    <el-dialog v-model="showAssetModal" title="Registration of Fixed Assets" width="600px" align-center class="premium-dialog rounded-4">
      <div class="px-3 pb-3">
        <el-form :model="assetForm" label-position="top" class="premium-form">
            <div class="row g-3">
                <div class="col-12">
                    <el-form-item label="Liên kết hạng mục sản phẩm" required>
                        <el-select v-model="assetForm.product.id" placeholder="Tìm trang thiết bị trong danh mục" class="w-100 premium-select" filterable>
                            <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
                        </el-select>
                    </el-form-item>
                </div>
                <div class="col-md-6">
                    <el-form-item label="Số hiệu tài sản (Asset Code)">
                        <el-input v-model="assetForm.assetCode" placeholder="Gợi ý: WH-EQ-XXX" />
                    </el-form-item>
                </div>
                <div class="col-md-6">
                    <el-form-item label="Đánh giá tình trạng khởi tạo">
                        <el-radio-group v-model="assetForm.conditionStatus" size="small" class="w-100">
                            <el-radio-button label="Tốt">TỐT</el-radio-button>
                            <el-radio-button label="Bảo trì">CẦN BẢO TRÌ</el-radio-button>
                            <el-radio-button label="Hỏng">LỖI/HỎNG</el-radio-button>
                        </el-radio-group>
                    </el-form-item>
                </div>
            </div>
            
            <div class="p-3 bg-amber-light rounded-4 border border-amber border-opacity-10 mt-4">
                <div class="d-flex align-items-center gap-2 text-amber mb-2">
                    <el-icon><InfoFilled /></el-icon>
                    <span class="ultra-tiny fw-bold">CHỈ SỐ KHẤU HAO</span>
                </div>
                <p class="ultra-tiny text-muted mb-0 lh-base">
                    Giá trị tài sản sẽ được thuật toán tự động khấu hao 1.5% mỗi tháng kể từ thời điểm đăng ký này để phục vụ báo cáo tài chính nội bộ.
                </p>
            </div>
        </el-form>
      </div>
      <template #footer>
        <div class="d-flex justify-content-end gap-2 px-3 pb-4 border-top pt-3">
          <el-button @click="showAssetModal = false" class="rounded-pill px-4">Hủy thao tác</el-button>
          <el-button type="warning" @click="handleAddAsset" class="rounded-pill px-4 shadow-sm fw-bold border-0 text-white bg-amber">Xác nhận Đăng ký</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.assets-premium {
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

.icon-box-assets {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.bg-amber { background-color: #f59e0b; }
.text-amber { color: #f59e0b; }
.bg-amber-light { background-color: rgba(245, 158, 11, 0.05); }

.asset-logo { width: 52px; height: 52px; }

.asset-premium-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0,0,0,0.08) !important;
}

.pulse-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    animation: alertPulse 2s infinite;
}

@keyframes alertPulse {
    0% { opacity: 1; }
    50% { opacity: 0.4; }
    100% { opacity: 1; }
}

.premium-dialog :deep(.el-dialog__title) {
    font-weight: 800;
}

.premium-form :deep(.el-form-item__label) {
    font-weight: 700;
    color: #475569;
    font-size: 12px;
}
</style>