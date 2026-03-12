<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  Search, Plus, Filter, Download,
  Edit, Delete, MoreFilled, View, Box
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
      api.get('/products/categories'),
      api.get('/products/suppliers'),
      api.get('/products/units')
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
    await api.post('/products', form.value)
    ElMessage.success(isEdit.value ? 'Cập nhật sản phẩm thành công' : 'Thêm sản phẩm thành công')
    dialogVisible.value = false
    fetchProducts()
  } catch (error) {
    console.error('Lỗi lưu sản phẩm:', error)
    ElMessage.error('Có lỗi xảy ra khi lưu sản phẩm')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn xóa sản phẩm này?', 'Xác nhận xóa', {
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    type: 'warning'
  }).then(async () => {
    try {
      ElMessage.info('Chức năng xóa sẽ được cập nhật sớm')
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
  <div class="products-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-bold mb-1">Danh mục sản phẩm</h4>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0">
            <li class="breadcrumb-item small"><a href="#" class="text-decoration-none">Kho</a></li>
            <li class="breadcrumb-item active small" aria-current="page">Sản phẩm</li>
          </ol>
        </nav>
      </div>
      <div class="d-flex gap-2">
        <el-button :icon="Download">Xuất dữ liệu</el-button>
        <el-button type="primary" :icon="Plus" @click="openAddDialog">Thêm sản phẩm mới</el-button>
      </div>
    </div>

    <!-- Filters & Search -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body p-3">
        <div class="row g-3">
          <div class="col-12 col-md-4">
            <el-input v-model="searchQuery" placeholder="Tìm theo tên sản phẩm hoặc SKU..." :prefix-icon="Search"
              clearable />
          </div>
          <div class="col-12 col-md-3">
            <el-select v-model="selectedCategory" placeholder="Tất cả danh mục" class="w-100" clearable>
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>
          <div class="col-12 col-md-2">
            <el-select v-model="selectedStatus" placeholder="Trạng thái" class="w-100" clearable>
              <el-option label="Còn hàng" value="in" />
              <el-option label="Hết hàng" value="out" />
            </el-select>
          </div>
          <div class="col-12 col-md-3 text-end">
            <el-button :icon="Filter" type="info" plain @click="fetchProducts">Tải lại</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card border-0 shadow-sm overflow-hidden" v-loading="loading">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 custom-table">
          <thead class="bg-light">
            <tr>
              <th class="ps-4">Sản phẩm</th>
              <th>SKU / Barcode</th>
              <th>Danh mục / Đơn vị</th>
              <th>Nhà cung cấp</th>
              <th>Tồn kho / Định mức</th>
              <th>Trạng thái</th>
              <th class="text-end pe-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in filteredProducts" :key="product.id">
              <td class="ps-4">
                <div class="d-flex align-items-center gap-3">
                  <div
                    class="product-img-placeholder rounded-3 bg-light d-flex align-items-center justify-content-center text-muted">
                    <el-icon>
                      <Box />
                    </el-icon>
                  </div>
                  <div>
                    <h6 class="mb-0 fw-bold">{{ product.productName }}</h6>
                    <span class="text-muted small">ID: #{{ product.id }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="d-flex flex-column">
                  <span class="badge bg-light text-dark border fw-medium mb-1">{{ product.skuCode }}</span>
                  <span class="text-muted" style="font-size: 11px;">{{ product.barcode || 'N/A' }}</span>
                </div>
              </td>
              <td>
                <div class="d-flex flex-column">
                  <span>{{ product.category?.name || 'N/A' }}</span>
                  <span class="text-muted small">{{ product.unit?.name || '---' }}</span>
                </div>
              </td>
              <td>{{ product.supplier?.name || 'N/A' }}</td>
              <td>
                <div class="d-flex flex-column gap-1">
                  <div class="d-flex align-items-center gap-2">
                    <span class="fw-bold">{{ product.currentStock }}</span>
                    <div class="progress flex-grow-1" style="height: 6px; width: 60px;">
                      <div class="progress-bar"
                        :class="product.currentStock > product.minStock ? 'bg-success' : 'bg-danger'" role="progressbar"
                        :style="{ width: Math.min((product.currentStock / 100 * 100), 100) + '%' }"></div>
                    </div>
                  </div>
                  <span class="text-muted" style="font-size: 11px;">Min: {{ product.minStock || 0 }}</span>
                </div>
              </td>
              <td>
                <span :class="['badge rounded-pill',
                  product.currentStock > product.minStock ? 'bg-success bg-opacity-10 text-success' :
                    (product.currentStock > 0 ? 'bg-warning bg-opacity-10 text-warning' : 'bg-danger bg-opacity-10 text-danger')
                ]">
                  {{ product.currentStock > product.minStock ? 'Ổn định' : (product.currentStock > 0 ? 'Sắp hết' : 'Hết
                  hàng') }}
                </span>
              </td>
              <td class="text-end pe-4">
                <div class="d-flex justify-content-end gap-1">
                  <el-tooltip content="Xem chi tiết" placement="top">
                    <el-button circle size="small" :icon="View" />
                  </el-tooltip>
                  <el-dropdown trigger="click">
                    <el-button circle size="small" :icon="MoreFilled" />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item><el-icon>
                            <Edit />
                          </el-icon> Chỉnh sửa</el-dropdown-item>
                        <el-dropdown-item>Lịch sử thay đổi</el-dropdown-item>
                        <el-dropdown-item divided class="text-danger" @click="handleDelete(product.id)"><el-icon>
                            <Delete />
                          </el-icon> Xóa sản phẩm</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer bg-white border-top p-3 d-flex justify-content-between align-items-center">
        <span class="text-muted small">Hiển thị {{ filteredProducts.length }} sản phẩm</span>
        <el-pagination background layout="prev, pager, next" :total="filteredProducts.length" :page-size="10" />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới'" width="600px">
      <el-form :model="form" label-position="top">
        <div class="row">
          <div class="col-md-6">
            <el-form-item label="Tên sản phẩm" required>
              <el-input v-model="form.productName" placeholder="Nhập tên sản phẩm" />
            </el-form-item>
          </div>
          <div class="col-md-6">
            <el-form-item label="SKU Code" required>
              <el-input v-model="form.skuCode" placeholder="Mã định danh SKU" />
            </el-form-item>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <el-form-item label="Barcode">
              <el-input v-model="form.barcode" placeholder="Mã vạch" />
            </el-form-item>
          </div>
          <div class="col-md-6">
            <el-form-item label="Danh mục">
              <el-select v-model="form.category.id" placeholder="Chọn danh mục" class="w-100">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <el-form-item label="Đơn vị tính">
              <el-select v-model="form.unit.id" placeholder="Chọn đơn vị" class="w-100">
                <el-option v-for="u in units" :key="u.id" :label="u.name" :value="u.id" />
              </el-select>
            </el-form-item>
          </div>
          <div class="col-md-6">
            <el-form-item label="Nhà cung cấp">
              <el-select v-model="form.supplier.id" placeholder="Chọn nhà cung cấp" class="w-100">
                <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <el-form-item label="Tồn kho tối đa (Định mức)">
              <el-input-number v-model="form.minStock" :min="0" class="w-100" />
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="handleSave">Xác nhận</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.custom-table thead th {
  padding-top: 15px;
  padding-bottom: 15px;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  color: #64748b;
  border-bottom: 1px solid #f1f5f9;
}

.custom-table tbody td {
  padding-top: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f8fafc;
}

.product-img-placeholder {
  width: 42px;
  height: 42px;
  border: 1px solid #f1f5f9;
}

.breadcrumb-item+.breadcrumb-item::before {
  content: "›";
  font-size: 18px;
  line-height: 1;
  vertical-align: middle;
}
</style>
