<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { School as SchoolIcon, MapLocation as MapLocationIcon, User as UserIcon, PieChart as PieChartIcon, Back as BackIcon, Box as BoxIcon, Connection as ConnectionIcon, Warning as WarningIcon, Edit as EditIcon } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()
const warehouseId = route.params.id
const warehouse = ref({})
const products = ref([])
const loading = ref(false)

const fetchWarehouseDetails = async () => {
  try {
    loading.value = true
    // Fetch warehouse info
    const whResponse = await api.get('/warehouses')
    warehouse.value = whResponse.data.find(w => w.id === parseInt(warehouseId)) || {}

    // Fetch products in this warehouse
    const productsResponse = await api.get(`/warehouses/${warehouseId}/bin-stock`)
    products.value = productsResponse.data
  } catch (error) {
    console.error('Lỗi tải chi tiết kho:', error)
    ElMessage.error('Không thể tải thông tin chi tiết kho')
  } finally {
    loading.value = false
  }
}

const totalStock = computed(() => {
  return products.value.reduce((sum, p) => sum + (p.currentStock || 0), 0)
})

const totalSku = computed(() => {
  return products.value.length
})

onMounted(() => {
  fetchWarehouseDetails()
})
</script>

<template>
  <div class="warehouse-detail" v-loading="loading">
    <div class="d-flex align-items-center mb-4 gap-3">
      <el-button :icon="BackIcon" circle @click="router.push('/warehouses')" />
      <div>
        <h4 class="fw-bold mb-1">Chi tiết kho: {{ warehouse.name }}</h4>
        <p class="text-muted small mb-0">Xem sơ đồ và danh sách sản phẩm lưu trữ</p>
      </div>
    </div>

    <!-- Stats Row -->
    <div class="row g-4 mb-4">
      <div class="col-12 col-md-4">
        <div class="card border-0 shadow-sm p-4 h-100">
          <div class="d-flex align-items-center gap-3">
            <div class="stat-icon bg-primary bg-opacity-10 text-primary p-3 rounded-3">
              <el-icon class="fs-4"><BoxIcon /></el-icon>
            </div>
            <div>
              <p class="text-muted small mb-0">Tổng SKU lưu trữ</p>
              <h3 class="fw-bold mb-0">{{ totalSku }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="card border-0 shadow-sm p-4 h-100">
          <div class="d-flex align-items-center gap-3">
            <div class="stat-icon bg-success bg-opacity-10 text-success p-3 rounded-3">
              <el-icon class="fs-4"><ConnectionIcon /></el-icon>
            </div>
            <div>
              <p class="text-muted small mb-0">Tổng tồn kho vật lý</p>
              <h3 class="fw-bold mb-0">{{ totalStock.toLocaleString() }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="card border-0 shadow-sm p-4 h-100">
          <div class="d-flex align-items-center gap-3">
            <div class="stat-icon bg-warning bg-opacity-10 text-warning p-3 rounded-3">
              <el-icon class="fs-4"><PieChartIcon /></el-icon>
            </div>
            <div>
              <p class="text-muted small mb-0">Công suất hiện tại</p>
              <h3 class="fw-bold mb-0">{{ warehouse.capacity || 0 }}%</h3>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="row g-4">
      <!-- Left: Warehouse Info -->
      <div class="col-12 col-xl-4">
        <div class="card border-0 shadow-sm p-4 mb-4">
          <h6 class="fw-bold mb-4">Thông tin cơ sở</h6>
          <div class="info-item mb-3">
            <label class="small text-muted d-block">Mã định danh</label>
            <span class="fw-bold">{{ warehouse.code }}</span>
          </div>
          <div class="info-item mb-3">
            <label class="small text-muted d-block">Vị trí địa lý</label>
            <span class="fw-bold text-primary"><el-icon class="me-1"><MapLocationIcon /></el-icon>{{ warehouse.location }}</span>
          </div>
          <div class="info-item mb-3">
            <label class="small text-muted d-block">Quản lý trưởng</label>
            <span class="fw-bold"><el-icon class="me-1"><UserIcon /></el-icon>{{ warehouse.manager }}</span>
          </div>
        </div>
      </div>

      <!-- Right: Product List -->
      <div class="col-12 col-xl-8">
        <div class="card border-0 shadow-sm overflow-hidden">
          <div class="card-header bg-white p-4 border-0 d-flex justify-content-between align-items-center">
            <h6 class="fw-bold mb-0">Danh sách sản phẩm trong kho</h6>
            <el-tag type="info" size="small">{{ products.length }} mặt hàng</el-tag>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
                <tr>
                  <th class="ps-4">Sản phẩm</th>
                  <th>Mã SKU</th>
                  <th>Vị trí (Bin)</th>
                  <th>Tồn kho</th>
                  <th class="text-end pe-4">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in products" :key="p.id">
                  <td class="ps-4">
                    <div class="fw-bold">{{ p.productName }}</div>
                    <div class="text-muted small">{{ p.category?.name }}</div>
                  </td>
                  <td><code class="text-primary">{{ p.skuCode }}</code></td>
                  <td>
                    <el-tag size="small" effect="dark">{{ p.binLocation?.binCode || 'N/A' }}</el-tag>
                  </td>
                  <td><strong>{{ p.currentStock }}</strong> {{ p.unit?.name }}</td>
                  <td class="text-end pe-4">
                    <el-tag :type="p.currentStock <= p.minStock ? 'danger' : 'success'" size="small">
                      {{ p.currentStock <= p.minStock ? 'Cần nhập hàng' : 'Ổn định' }}
                    </el-tag>
                  </td>
                </tr>
                <tr v-if="products.length === 0">
                  <td colspan="5" class="text-center p-5 text-muted">
                    <el-icon class="fs-1 mb-2"><WarningIcon /></el-icon>
                    <p>Không có sản phẩm nào được lưu trữ tại kho này</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.warehouse-detail {
  padding: 10px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}
th {
  padding: 15px 10px;
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
}
td {
  padding: 15px 10px;
}
</style>
