<script setup>
import { ref, markRaw } from 'vue'
import { useRoute } from 'vue-router'
import { 
  Back, Box, Timer, Edit, InfoFilled, 
  TrendCharts, History, CollectionTag, 
  PriceTag, Compass, Monitor, Histogram
} from '@element-plus/icons-vue'

const route = useRoute()
const productId = route.params.id

// Mock data (Simulating a fetched product)
const product = ref({
  id: productId,
  name: 'iPhone 15 Pro Max',
  sku: 'IPHONE-15-PRO-256-VN',
  category: 'Smartphone',
  brand: 'Apple',
  price: '32,990,000',
  stock: 45,
  minStock: 20,
  warehouse: 'Kho tổng Quận 1',
  description: 'Sản phẩm flagship đời mới nhất của Apple với khung viền Titan và chip A17 Pro mạnh mẽ.',
  specs: [
    { key: 'Màn hình', value: '6.7 inch, Super Retina XDR' },
    { key: 'Chipset', value: 'Apple A17 Pro (3nm)' },
    { key: 'Bộ nhớ', value: '256GB NVMe' },
    { key: 'Hệ điều hành', value: 'iOS 17' },
    { key: 'Pin', value: '4,422 mAh' }
  ]
})
</script>

<template>
  <div class="product-detail-premium p-4">
    <!-- Glass Hero Header -->
    <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
        <div class="d-flex align-items-center gap-4">
            <router-link to="/products" class="back-link d-flex align-items-center justify-content-center rounded-circle shadow-sm border">
                <el-icon :size="20"><Back /></el-icon>
            </router-link>
            <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                    <h3 class="fw-black text-dark mb-0 ls-tight">{{ product.name }}</h3>
                    <el-tag size="small" type="primary" effect="dark" class="rounded-pill border-0 px-3 fw-bold tiny tracking-widest">#{{ product.sku }}</el-tag>
                </div>
                <p class="text-muted small mb-0 fw-medium">Phân tích chuyên sâu & Hồ sơ kỹ thuật hàng hóa</p>
            </div>
        </div>
        <div class="d-flex gap-3">
            <el-button type="primary" plain class="rounded-pill px-4" :icon="markRaw(History)">Lịch sử kho</el-button>
            <el-button type="primary" class="rounded-pill px-4 shadow-sm fw-bold" :icon="markRaw(Edit)">Hiệu chỉnh thông tin</el-button>
        </div>
    </div>

    <div class="row g-4">
      <!-- Main Content Column -->
      <div class="col-12 col-lg-8">
        <!-- Visualization Card -->
        <div class="product-main-card bg-white rounded-5 shadow-sm border-0 overflow-hidden mb-4">
          <div class="card-body p-5">
            <div class="row g-5">
              <div class="col-md-5">
                <div class="visual-gallery rounded-4 bg-light d-flex flex-column align-items-center justify-content-center p-5 border border-dashed border-2 position-relative overflow-hidden">
                    <el-icon class="display-1 text-primary opacity-25"><Box /></el-icon>
                    <p class="text-muted ultra-tiny fw-bold mt-3 tracking-widest text-uppercase">DIGITAL TWIN PREVIEW</p>
                    <div class="shimmer-effect"></div>
                </div>
              </div>
              <div class="col-md-7">
                <div class="d-flex align-items-center gap-2 mb-3">
                    <span class="badge rounded-pill bg-primary bg-opacity-10 text-primary border-0 fs-10 fw-bold px-3">ACTIVE STOCK</span>
                </div>
                <h2 class="fw-black text-dark mb-3 ls-tight">{{ product.name }}</h2>
                <p class="text-muted lh-base mb-4 fw-medium">{{ product.description }}</p>
                
                <div class="detail-grid row g-3">
                  <div class="col-6">
                    <div class="p-3 rounded-4 bg-light bg-opacity-50 border border-white">
                        <p class="ultra-tiny text-muted fw-bold mb-1 uppercase tracking-wider">THƯƠNG HIỆU</p>
                        <p class="mb-0 fw-bold text-dark d-flex align-items-center"><el-icon class="me-2 text-primary"><Compass /></el-icon>{{ product.brand }}</p>
                    </div>
                  </div>
                  <div class="col-6">
                    <div class="p-3 rounded-4 bg-light bg-opacity-50 border border-white">
                        <p class="ultra-tiny text-muted fw-bold mb-1 uppercase tracking-wider">PHÂN LOẠI</p>
                        <p class="mb-0 fw-bold text-dark d-flex align-items-center"><el-icon class="me-2 text-primary"><CollectionTag /></el-icon>{{ product.category }}</p>
                    </div>
                  </div>
                  <div class="col-12">
                    <div class="p-3 rounded-4 bg-primary bg-opacity-5 d-flex align-items-center justify-content-between border border-primary border-opacity-10">
                        <div class="d-flex align-items-center gap-3">
                           <el-icon :size="24" class="text-primary"><PriceTag /></el-icon>
                           <div>
                              <p class="ultra-tiny text-muted fw-bold mb-0">GIÁ TRỊ ƯỚC TÍNH (MSRP)</p>
                              <h5 class="fw-black text-primary mb-0">{{ product.price }} VNĐ</h5>
                           </div>
                        </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Specifications Layout -->
        <div class="specs-card bg-white rounded-5 shadow-sm border-0">
          <div class="p-5">
            <h5 class="fw-bold text-dark mb-4 d-flex align-items-center"><el-icon class="me-2 text-primary"><Monitor /></el-icon>Thông số kỹ thuật chi tiết</h5>
            <div class="row g-3">
                <div v-for="spec in product.specs" :key="spec.key" class="col-md-6">
                    <div class="spec-row p-3 rounded-4 bg-light d-flex justify-content-between align-items-center border border-white transition-all">
                        <span class="small fw-bold text-muted">{{ spec.key }}</span>
                        <span class="small fw-black text-dark text-end">{{ spec.value }}</span>
                    </div>
                </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar Column -->
      <div class="col-12 col-lg-4">
        <!-- High-Impact Stock Gauge -->
        <div class="gauge-card rounded-5 p-5 bg-gradient-premium text-white shadow-lg mb-4 text-center">
            <p class="ultra-tiny fw-bold tracking-widest text-uppercase opacity-75 mb-4">STORAGE METRICS</p>
            <div class="stock-circle mx-auto mb-4 d-flex flex-column align-items-center justify-content-center">
                <h2 class="display-1 fw-black mb-0">{{ product.stock }}</h2>
                <span class="tiny fw-bold opacity-75">TỒN KHO</span>
            </div>
            
            <div class="capacity-stats py-3 px-4 rounded-4 bg-white bg-opacity-10 mb-4 border border-white border-opacity-10">
                <div class="d-flex justify-content-between mb-2">
                    <span class="ultra-tiny fw-bold">OPERATIONAL NODE:</span>
                    <span class="ultra-tiny fw-bold">ONLINE</span>
                </div>
                <p class="mb-0 fw-bold small text-start">{{ product.warehouse }}</p>
                <div class="progress rounded-pill mt-3 shadow-sm" style="height: 8px;">
                    <div class="progress-bar bg-white rounded-pill" :style="{ width: (product.stock / 100 * 100) + '%' }"></div>
                </div>
            </div>
            
            <el-button class="w-100 rounded-pill border-0 shadow-sm fw-bold bg-white text-primary hover-bg-light" size="large">XUẤT KHO NGAY</el-button>
        </div>

        <!-- Trend Analysis -->
        <div class="trend-card bg-white rounded-5 shadow-sm border-0 h-100 flex-column overflow-hidden">
          <div class="p-4 border-bottom">
            <h5 class="fw-bold text-dark mb-0 d-flex align-items-center"><el-icon class="me-2 text-primary"><Histogram /></el-icon>Phân tích biến động</h5>
          </div>
          <div class="p-4 flex-grow-1 flex-column align-items-center justify-content-center d-flex opacity-50 py-5">
              <el-icon :size="48" class="text-primary mb-3"><TrendCharts /></el-icon>
              <p class="small fw-bold text-muted mb-0">Dữ liệu phân tích AI</p>
              <p class="ultra-tiny text-muted">Đang kết nối Real-time...</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail-premium {
  font-family: 'Plus Jakarta Sans', sans-serif;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.fw-black { font-weight: 850; }
.ls-tight { letter-spacing: -1px; }
.tiny { font-size: 10px; }
.ultra-tiny { font-size: 9px; }
.fs-10 { font-size: 10px; }

.glass-hero { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(10px); }

.back-link { width: 42px; height: 42px; color: #64748b; background: #fff; transition: all 0.2s; }
.back-link:hover { transform: translateX(-5px); color: var(--primary); }

.visual-gallery { min-height: 280px; }

.shimmer-effect {
    position: absolute; top: 0; left: 0; width: 100%; height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
    animation: shimmer 3s infinite;
}

@keyframes shimmer { 0% { transform: translateX(-100%); } 100% { transform: translateX(100%); } }

.spec-row:hover { background: #f8fafc; border-color: #e2e8f0; transform: translateX(3px); }

.bg-gradient-premium { background: linear-gradient(135deg, #4f46e5 0%, #3730a3 100%); }

.stock-circle {
    width: 140px; height: 140px; border-radius: 50%;
    border: 8px solid rgba(255,255,255,0.1);
    box-shadow: 0 0 30px rgba(0,0,0,0.1);
}

.uppercase { text-transform: uppercase; }
.tracking-wider { letter-spacing: 1px; }
.tracking-widest { letter-spacing: 2px; }
</style>
