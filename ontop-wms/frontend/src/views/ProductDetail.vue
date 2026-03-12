<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { 
  Back, Box, Timer, Edit, InfoFilled, 
  TrendCharts, History, CollectionTag 
} from '@element-plus/icons-vue'

const route = useRoute()
const productId = route.params.id

const product = ref({
  id: productId,
  name: 'iPhone 15 Pro Max',
  sku: 'IPHONE-15-PRO-256-VN',
  category: 'Smartphone',
  brand: 'Apple',
  price: '32,990,000đ',
  stock: 45,
  warehouse: 'Kho tổng Quận 1',
  description: 'Sản phẩm flagship đời mới nhất của Apple với khung viền Titan và chip A17 Pro.',
  specs: [
    { key: 'Màn hình', value: '6.7 inch, Super Retina XDR' },
    { key: 'Chipset', value: 'Apple A17 Pro' },
    { key: 'Dung lượng', value: '256GB' },
    { key: 'Pin', value: '4,422 mAh' }
  ]
})
</script>

<template>
  <div class="product-detail-view">
    <div class="d-flex align-items-center gap-3 mb-4">
      <router-link to="/products" class="btn btn-outline-secondary border-0 rounded-circle p-2">
        <el-icon class="fs-4"><Back /></el-icon>
      </router-link>
      <div>
        <div class="d-flex align-items-center gap-2">
          <h3 class="fw-bold mb-0">{{ product.name }}</h3>
          <span class="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-10">#{{ product.sku }}</span>
        </div>
        <p class="text-muted small mb-0">Chi tiết thông tin hàng hóa trong hệ thống</p>
      </div>
    </div>

    <div class="row g-4">
      <div class="col-12 col-lg-8">
        <!-- Info Card -->
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-body p-4">
            <div class="row">
              <div class="col-md-4 mb-4 mb-md-0">
                <div class="product-gallery rounded-4 bg-light d-flex align-items-center justify-content-center p-5 border h-100">
                  <el-icon class="display-1 text-muted opacity-25"><Box /></el-icon>
                </div>
              </div>
              <div class="col-md-8">
                <div class="d-flex justify-content-between mb-3">
                  <span class="text-muted fw-bold small text-uppercase ls-1">Thông tin cơ bản</span>
                  <el-button type="primary" link :icon="Edit">Chỉnh sửa</el-button>
                </div>
                <h5 class="fw-bold mb-3">{{ product.name }}</h5>
                <p class="text-muted mb-4">{{ product.description }}</p>
                
                <div class="row g-3">
                  <div class="col-6">
                    <p class="small text-muted mb-1">Danh mục</p>
                    <p class="fw-bold"><el-icon class="me-1 text-primary"><CollectionTag /></el-icon>{{ product.category }}</p>
                  </div>
                  <div class="col-6">
                    <p class="small text-muted mb-1">Thương hiệu</p>
                    <p class="fw-bold"><el-icon class="me-1 text-primary"><InfoFilled /></el-icon>{{ product.brand }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Specs Card -->
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-0 p-4">
            <h5 class="fw-bold mb-0">Thông số kỹ thuật</h5>
          </div>
          <div class="card-body p-4 pt-0">
            <div class="table-responsive">
              <table class="table table-striped table-borderless">
                <tbody>
                  <tr v-for="spec in product.specs" :key="spec.key">
                    <td class="text-muted py-3" style="width: 200px;">{{ spec.key }}</td>
                    <td class="fw-bold py-3">{{ spec.value }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-4">
        <!-- Stock Card -->
        <div class="card border-0 shadow-sm mb-4 bg-primary text-white">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="small fw-bold text-uppercase opacity-75">Tồn kho hiện tại</span>
              <el-icon class="fs-4"><History /></el-icon>
            </div>
            <h2 class="display-5 fw-extrabold mb-1">{{ product.stock }}</h2>
            <p class="small mb-4 opacity-75">Đơn vị tính: CÁI</p>
            <div class="p-3 rounded-3 bg-white bg-opacity-10 border border-white border-opacity-10 mb-2">
              <p class="small mb-1 opacity-75">Vị trí lưu kho</p>
              <p class="fw-bold mb-0">{{ product.warehouse }}</p>
            </div>
          </div>
        </div>

        <!-- History Card -->
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-0 p-4">
            <h5 class="fw-bold mb-0">Biến động tồn kho</h5>
          </div>
          <div class="card-body p-4 pt-0">
            <div class="chart-placeholder rounded-4 bg-light p-5 text-center border border-dashed text-primary">
              <el-icon class="fs-1 mb-2"><TrendCharts /></el-icon>
              <p class="small fw-medium mb-0">Biểu đồ đang tải...</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-gallery { min-height: 250px; }
.ls-1 { letter-spacing: 1px; }
.display-5 { font-size: 3rem; }
.chart-placeholder { border-style: dashed !important; border-width: 2px !important; }
</style>
