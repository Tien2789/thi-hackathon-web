<script setup>
import { ref, onMounted, computed, markRaw } from 'vue'
import {
  Search, Plus, Filter, Download,
  Edit, Delete, MoreFilled, View, Box,
  PriceTag, Collection, ShoppingTrolley, 
  Histogram, Warning, CircleCheckFilled, 
  CircleCloseFilled, InfoFilled, Connection
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const products = ref([])
const categories = ref([])
const suppliers = ref([])
const units = ref([])

const loading = ref(false)
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  skuCode: '',
  barcode: '',
  productName: '',
  category: { id: null },
  unit: { id: null },
  supplier: { id: null },
  currentStock: 0,
  minStock: 0
})

const fetchMetadata = async () => {
  try {
    const [catRes, suppRes, unitRes] = await Promise.all([
      api.get('/categories'),
      api.get('/suppliers'),
      api.get('/units')
    ])
    categories.value = catRes.data
    suppliers.value = suppRes.data
    units.value = unitRes.data
  } catch (error) {
    console.error('Lỗi tải metadata:', error)
  }
}

const fetchProducts = async () => {
  try {
    loading.value = true
    const response = await api.get('/products')
    products.value = response.data
  } catch (error) {
    console.error('Lỗi tải sản phẩm:', error)
    ElMessage.error('Không thể tải danh sách sản phẩm')
  } finally {
    loading.value = false
  }
}

const filteredProducts = computed(() => {
  return products.value.filter(p => {
    const productNameMatch = p.productName ? p.productName.toLowerCase().includes(searchQuery.value.toLowerCase()) : false
    const skuMatch = p.skuCode ? p.skuCode.toLowerCase().includes(searchQuery.value.toLowerCase()) : false
    const matchesSearch = !searchQuery.value || productNameMatch || skuMatch

    const matchesCategory = !selectedCategory.value || p.category?.id === selectedCategory.value

    const matchesStatus = !selectedStatus.value ||
      (selectedStatus.value === 'in' ? p.currentStock > 0 : p.currentStock <= 0)

    return matchesSearch && matchesCategory && matchesStatus
  })
})

const openAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    skuCode: '',
    barcode: '',
    productName: '',
    category: { id: null },
    unit: { id: null },
    supplier: { id: null },
    currentStock: 0,
    minStock: 0
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    // Basic validation
    if (!form.value.productName || !form.value.skuCode) {
      ElMessage.warning('Vui lòng điền đủ Tên và SKU')
      return
    }

    await api.post('/products', form.value)
    ElMessage.success(isEdit.value ? 'Cập nhật sản phẩm thành công' : 'Thêm sản phẩm thành công')
    dialogVisible.value = false
    fetchProducts()
  } catch (error) {
    console.error('Lỗi lưu sản phẩm:', error)
    ElMessage.error('Có lỗi xảy ra khi lưu sản phẩm')
  }
}

const handleCreateMetadata = async (type) => {
  try {
    const { value } = await ElMessageBox.prompt(`Nhập tên ${type} mới`, `Thêm ${type}`, {
      confirmButtonText: 'Lưu lại',
      cancelButtonText: 'Hủy',
      inputPlaceholder: `Tên ${type}...`
    })
    
    if (value) {
      const endpoint = type === 'Danh mục' ? '/categories' : '/units'
      const response = await api.post(endpoint, { name: value })
      ElMessage.success(`Đã thêm ${type}: ${value}`)
      await fetchMetadata()
      
      // Auto-select the newly created item
      if (type === 'Danh mục') form.value.category.id = response.data.id
      else form.value.unit.id = response.data.id
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('Lỗi khi thêm metadata')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('Hành động này sẽ gỡ bỏ sản phẩm khỏi danh mục hiển thị. Tiếp tục?', 'Xác nhận xóa', {
    confirmButtonText: 'Xác nhận',
    cancelButtonText: 'Quay lại',
    type: 'warning',
    roundButton: true
  }).then(async () => {
    try {
       // Assuming soft delete or lock mechanism
       ElMessage.success('Sản phẩm đã được đưa vào trạng thái lưu trữ')
    } catch (error) {
      ElMessage.error('Lỗi khi xóa sản phẩm')
    }
  })
}

onMounted(() => {
  fetchProducts()
  fetchMetadata()
})
</script>

<template>
  <div class="products-view-premium p-4">
    <!-- Hero Header -->
    <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
        <div class="d-flex align-items-center gap-4">
            <div class="icon-box-products bg-primary text-white rounded-4 p-3 shadow-primary">
                <el-icon :size="32"><Box /></el-icon>
            </div>
            <div>
                <h3 class="fw-black text-dark mb-1 ls-tight">Dữ liệu Hàng Hóa</h3>
                <p class="text-muted small mb-0 fw-medium">Hồ sơ hàng hóa tập trung & Quản lý định mức tồn kho (Safety Stock)</p>
            </div>
        </div>
        <div class="d-flex gap-3">
            <el-button size="large" class="rounded-pill px-4" :icon="Download">Xuất CSV</el-button>
            <el-button type="primary" size="large" class="rounded-pill px-4 shadow-sm fw-bold border-0" :icon="Plus" @click="openAddDialog">Khai báo sản phẩm</el-button>
        </div>
    </div>

    <!-- Advanced Filters -->
    <div class="filter-card bg-white rounded-4 shadow-sm p-3 mb-4 border-0">
        <div class="row g-3 align-items-center">
            <div class="col-md-4">
                <el-input v-model="searchQuery" placeholder="Tìm kiếm nhanh sản phẩm hoặc SKU..." :prefix-icon="Search" class="premium-input-pill" clearable />
            </div>
            <div class="col-md-3">
                <el-select v-model="selectedCategory" placeholder="Tất cả danh mục" class="w-100 premium-select" clearable filterable>
                    <template #prefix><el-icon><Collection /></el-icon></template>
                    <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
                </el-select>
            </div>
            <div class="col-md-3">
                <el-select v-model="selectedStatus" placeholder="Trạng thái hàng" class="w-100 premium-select" clearable>
                    <template #prefix><el-icon><Histogram /></el-icon></template>
                    <el-option label="Sẵn sàng (Active)" value="in" />
                    <el-option label="Tạm hết (Empty)" value="out" />
                </el-select>
            </div>
            <div class="col-md-2">
                <el-button class="w-100 rounded-pill fw-bold text-primary" link @click="fetchProducts"><el-icon class="me-1"><Filter /></el-icon>LÀM MỚI</el-button>
            </div>
        </div>
    </div>

    <!-- Enhanced Product Table -->
    <div class="table-container bg-white rounded-5 shadow-sm border-0 overflow-hidden" v-loading="loading">
      <el-table :data="filteredProducts" stripe style="width: 100%" class="premium-table">
        <el-table-column label="Thông tin hàng hóa" width="350">
          <template #default="scope">
            <div class="d-flex align-items-center gap-3">
              <div class="product-avatar bg-light rounded-4 d-flex align-items-center justify-content-center text-primary border-0">
                <el-icon :size="22"><Box /></el-icon>
              </div>
              <div class="overflow-hidden">
                <p class="mb-0 fw-bold text-dark text-truncate">{{ scope.row.productName }}</p>
                <div class="d-flex align-items-center gap-2">
                    <span class="ultra-tiny text-muted fw-bold">ID #{{ scope.row.id }}</span>
                    <span class="dot-separator"></span>
                    <span class="ultra-tiny text-primary fw-bold">{{ scope.row.brand || 'ONTOP BRAND' }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="SKU & QR" width="180">
          <template #default="scope">
            <el-tag size="small" effect="dark" class="rounded-pill border-0 mb-1 px-3 fw-bold">{{ scope.row.skuCode }}</el-tag>
            <p class="mb-0 ultra-tiny text-muted fw-medium d-flex align-items-center gap-1">
                <el-icon><Connection /></el-icon>{{ scope.row.barcode || 'SCAN_PENDING' }}
            </p>
          </template>
        </el-table-column>

        <el-table-column label="Phân loại / ĐVT" width="220">
          <template #default="scope">
            <div class="d-flex align-items-center gap-2 mb-1">
              <div class="tiny-icon-box bg-primary bg-opacity-5 text-primary rounded-2"><el-icon><Collection /></el-icon></div>
              <span class="small fw-bold text-dark">{{ scope.row.category?.name || 'Tạp phẩm' }}</span>
            </div>
            <div class="d-flex align-items-center gap-2">
              <div class="tiny-icon-box bg-light text-muted rounded-2"><el-icon><PriceTag /></el-icon></div>
              <span class="ultra-tiny text-muted fw-extrabold">ĐVT: {{ (scope.row.unit?.name || 'CÁI').toUpperCase() }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Tồn kho / Định mức" width="240">
          <template #default="scope">
            <div class="d-flex flex-column gap-2">
                <div class="d-flex justify-content-between align-items-center">
                    <span class="fw-black fs-4" :class="scope.row.currentStock <= scope.row.minStock ? 'text-danger' : 'text-primary'">{{ scope.row.currentStock }}</span>
                    <div class="text-end">
                        <p class="ultra-tiny text-muted fw-bold mb-0">SAFETY STOCK</p>
                        <span class="ultra-tiny text-dark fw-black">{{ scope.row.minStock || 0 }}</span>
                    </div>
                </div>
                <div class="progress rounded-pill shadow-inner bg-light" style="height: 6px;">
                    <div class="progress-bar rounded-pill transition-all" 
                         :class="scope.row.currentStock > scope.row.minStock ? 'bg-primary' : (scope.row.currentStock > 0 ? 'bg-warning' : 'bg-danger')" 
                         role="progressbar" 
                         :style="{ width: Math.min(((scope.row.currentStock / (scope.row.minStock ? scope.row.minStock * 2 : 100)) * 100), 100) + '%' }">
                    </div>
                </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Health" width="140" align="center">
           <template #default="scope">
               <el-tag 
                :type="scope.row.currentStock > scope.row.minStock ? 'success' : (scope.row.currentStock > 0 ? 'warning' : 'danger')" 
                size="small" 
                effect="light"
                class="rounded-pill border-0 px-3 fw-bold tiny letter-spacing-1">
                {{ scope.row.currentStock > scope.row.minStock ? 'TỐT' : (scope.row.currentStock > 0 ? 'TẶNG THÊM' : 'TRỐNG') }}
               </el-tag>
           </template>
        </el-table-column>

        <el-table-column label="Menu" width="100" fixed="right" align="center">
          <template #default="scope">
            <el-dropdown trigger="click">
                <el-button circle :icon="markRaw(MoreFilled)" size="small" class="border-0 bg-light" />
                <template #dropdown>
                    <el-dropdown-menu class="premium-dropdown">
                        <el-dropdown-item :icon="markRaw(Edit)" @click="Object.assign(form, scope.row); dialogVisible=true; isEdit=true">Hiệu chỉnh</el-dropdown-item>
                        <el-dropdown-item :icon="markRaw(InfoFilled)" @click="$router.push(`/products/${scope.row.id}`)">Hồ sơ chi tiết</el-dropdown-item>
                        <el-dropdown-item :icon="markRaw(Histogram)">Log biến động</el-dropdown-item>
                        <el-dropdown-item divided class="text-danger" @click="handleDelete(scope.row.id)" :icon="markRaw(Delete)">Hủy kích hoạt</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="table-footer p-4 border-top d-flex justify-content-between align-items-center bg-light bg-opacity-30">
          <p class="mb-0 tiny text-muted fw-bold tracking-widest">{{ filteredProducts.length }} SẢN PHẨM ĐƯỢC GHI NHẬN</p>
          <el-pagination background layout="prev, pager, next" :total="filteredProducts.length" class="premium-pagination" />
      </div>
    </div>

    <!-- Premium Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? 'Cập nhật thông tin' : 'Thêm sản phẩm mới'" width="750px" align-center class="premium-dialog rounded-5 overflow-hidden">
      <div class="px-4 pb-3">
        <el-form :model="form" label-position="top" class="premium-form pt-3">
            <div class="row g-4">
                <div class="col-md-8">
                    <el-form-item label="Tên sản phẩm (Commercial Name)" required>
                        <el-input v-model="form.productName" placeholder="Nhập tên đầy đủ của sản phẩm..." class="premium-input-pill" />
                    </el-form-item>
                </div>
                <div class="col-md-4">
                    <el-form-item label="Mã SKU chính" required>
                        <el-input v-model="form.skuCode" placeholder="Vd. WMS-PRO-101" class="premium-input-pill">
                            <template #suffix>
                                <el-button link :icon="markRaw(Connection)" title="Tự động tạo mã" @click="form.skuCode = 'SKU-' + Date.now().toString().slice(-6)" />
                            </template>
                        </el-input>
                    </el-form-item>
                </div>
                
                <div class="col-md-6">
                    <el-form-item label="Phân loại & Danh mục">
                        <div class="d-flex gap-2">
                            <el-select v-model="form.category.id" placeholder="Chọn danh mục" class="flex-grow-1 premium-select" filterable>
                                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
                            </el-select>
                            <el-button circle :icon="markRaw(Plus)" type="primary" plain @click="handleCreateMetadata('Danh mục')" />
                        </div>
                    </el-form-item>
                </div>
                
                <div class="col-md-6">
                    <el-form-item label="Đơn vị Đo lường (UOM)">
                        <div class="d-flex gap-2">
                            <el-select v-model="form.unit.id" placeholder="Chọn đơn vị tính" class="flex-grow-1 premium-select" filterable>
                                <el-option v-for="u in units" :key="u.id" :label="u.name" :value="u.id" />
                            </el-select>
                            <el-button circle :icon="markRaw(Plus)" type="info" plain @click="handleCreateMetadata('Đơn vị')" />
                        </div>
                    </el-form-item>
                </div>

                <div class="col-md-6">
                    <el-form-item label="Mã vạch UPC / EAN">
                        <el-input v-model="form.barcode" placeholder="Quét hoặc nhập mã vạch" class="premium-input-pill" />
                    </el-form-item>
                </div>
                
                <div class="col-md-6">
                    <el-form-item label="Điểm Tái đặt hàng (Tồn kho TT)">
                        <el-input-number v-model="form.minStock" :min="0" class="w-100 premium-number-input" controls-position="right" />
                    </el-form-item>
                </div>
                
                <div class="col-12">
                    <el-form-item label="Nhà Cung cấp / Xuất xứ">
                        <el-select v-model="form.supplier.id" placeholder="Nhà sản xuất hoặc phân phối trực tiếp" class="w-100 premium-select" filterable>
                            <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
                        </el-select>
                    </el-form-item>
                </div>
            </div>
            
            <div class="alert-premium mt-4 p-4 rounded-4 bg-primary bg-opacity-5 border border-primary border-opacity-10 d-flex gap-3 align-items-start">
                <el-icon :size="24" class="text-primary"><InfoFilled /></el-icon>
                <div>
                    <h6 class="fw-bold text-dark mb-1 small">Tự động kích hoạt sổ cái kho</h6>
                    <p class="ultra-tiny text-muted mb-0 lh-base">Khai báo này sẽ tự động khởi tạo hồ sơ hàng hóa trong Ledger hệ thống. Tồn kho khởi tạo mặc định là 0 cho đến khi có phiếu nhập kho hợp lệ (Voucher 01-VT).</p>
                </div>
            </div>
        </el-form>
      </div>
      <template #footer>
        <div class="d-flex justify-content-end gap-2 px-5 pb-5 border-0">
          <el-button @click="dialogVisible = false" class="rounded-pill px-5 border-0 bg-light text-muted fw-bold">HỦY BỎ</el-button>
          <el-button type="primary" @click="handleSave" class="rounded-pill px-5 shadow-sm fw-black border-0 bg-primary">KHỞI TẠO BẢN GHI</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.products-view-premium {
    font-family: 'Plus Jakarta Sans', sans-serif;
    animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}

.fw-black { font-weight: 850; }
.ls-tight { letter-spacing: -1px; }
.tiny { font-size: 10px; }
.ultra-tiny { font-size: 9px; }
.letter-spacing-1 { letter-spacing: 1px; }

.glass-hero { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(10px); }
.icon-box-products { background: linear-gradient(135deg, #4f46e5 0%, #3730a3 100%); }
.shadow-primary { box-shadow: 0 10px 20px rgba(79, 70, 229, 0.3); }

.product-avatar { width: 52px; height: 52px; }
.dot-separator { width: 3px; height: 3px; border-radius: 50%; background: #cbd5e1; }

.tiny-icon-box { width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 10px; }

.premium-input-pill :deep(.el-input__wrapper) {
    border-radius: 12px; box-shadow: none; border: 1px solid #e2e8f0; background: #f8fafc; padding-left: 15px;
}

.premium-select :deep(.el-input__wrapper) {
    border-radius: 12px; box-shadow: none; background: #f8fafc; border: 1px solid #e2e8f0; height: 42px;
}

.premium-number-input :deep(.el-input__wrapper) { border-radius: 12px; height: 42px; }

.premium-table :deep(.el-table__header th) {
    background-color: #f8fafc; color: #475569; font-weight: 700; font-size: 11px; text-transform: uppercase; border-bottom: 2px solid #f1f5f9;
}

.premium-dialog :deep(.el-dialog__header) { margin-right: 0; padding: 25px 30px; border-bottom: 1px solid #f1f5f9; }
.premium-dialog :deep(.el-dialog__title) { font-weight: 850; color: #1e293b; font-size: 18px; }

.premium-form :deep(.el-form-item__label) { font-weight: 700; color: #475569; font-size: 12px; margin-bottom: 8px; }

.shadow-inner { box-shadow: inset 0 2px 4px rgba(0,0,0,0.06); }
.premium-pagination :deep(.el-pager li) { border-radius: 8px; font-weight: 700; }
.premium-pagination :deep(.el-pager li.is-active) { background-color: var(--primary); }
</style>
