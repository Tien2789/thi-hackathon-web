<script setup>
import { ref, onMounted, markRaw } from 'vue'
import {
  Document, Download, PieChart, Plus,
  View, ArrowUp, ArrowDown, Warning, Checked, 
  Histogram, UserFilled, DataLine, Files,
  TrendCharts, Timer, List, Printer
} from '@element-plus/icons-vue'
import VueApexCharts from 'vue3-apexcharts'
import api from '../api'

const inventoryStatus = ref([])
const alerts = ref([])
const loading = ref(false)
const theKhoVisible = ref(false)
const selectedProduct = ref(null)

// Inventory Distribution Chart
const donutOptions = ref({
  chart: { type: 'donut', toolbar: { show: false } },
  labels: ['Smartphone', 'Tablet', 'Laptop', 'Accessories', 'IoT'],
  colors: ['#4f46e5', '#818cf8', '#10b981', '#f59e0b', '#ec4899'],
  stroke: { show: false },
  legend: { position: 'bottom', labels: { colors: '#64748b', useSeriesColors: false } },
  plotOptions: { pie: { donut: { size: '75%', labels: { show: true, name: { show: true, fontSize: '12px', fontWeight: 700, color: '#64748b' }, value: { show: true, fontSize: '20px', fontWeight: 900, color: '#1e293b' }, total: { show: true, label: 'TOTAL UNITS', fontSize: '10px', color: '#94a3b8' } } } } },
  dataLabels: { enabled: false },
  tooltip: { theme: 'dark' }
})
const donutSeries = ref([450, 200, 300, 150, 80])

const fetchReportsData = async () => {
  try {
    loading.value = true
    const [invRes, alertRes] = await Promise.all([
      api.get('/reports/inventory-status'),
      api.get('/reports/alerts')
    ])
    inventoryStatus.value = invRes.data 
    alerts.value = alertRes.data 
  } catch (error) {
    console.error('Lỗi tải báo cáo:', error)
  } finally {
    loading.value = false
  }
}

const openTheKho = (product) => {
    selectedProduct.value = product
    theKhoVisible.value = true
}

const ledgers = ref([
    { date: '2026-03-01', docNo: 'PNK-001', reason: 'Nhập từ Apple VN', in: 100, out: 0, balance: 100 },
    { date: '2026-03-05', docNo: 'PXK-002', reason: 'Xuất bán lẻ Store A', in: 0, out: 20, balance: 80 },
    { date: '2026-03-10', docNo: 'PXK-005', reason: 'Chuyển kho nội bộ', in: 0, out: 10, balance: 70 },
])

const reportHistory = ref([
  { id: 1, name: 'Báo cáo Nhập-Xuất-Tồn tháng 02/2026', date: '2026-03-01', size: '2.4 MB', type: 'PDF' },
  { id: 2, name: 'Báo cáo Kiểm kê Kho Tổng Miền Bắc', date: '2026-03-05', size: '1.1 MB', type: 'XLSX' },
  { id: 3, name: 'Phân tích SKU tiêu thụ nhanh Q1', date: '2026-03-08', size: '0.8 MB', type: 'PDF' },
])

onMounted(() => {
  fetchReportsData()
})
</script>

<template>
  <div class="reports-premium p-4" v-loading="loading">
    <!-- Hero Header -->
    <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
        <div class="d-flex align-items-center gap-4">
            <div class="icon-box-reports bg-indigo text-white rounded-4 p-3 shadow-indigo">
                <el-icon :size="32"><DataLine /></el-icon>
            </div>
            <div>
                <h3 class="fw-black text-dark mb-1 ls-tight">Báo cáo Vận Hành</h3>
                <p class="text-muted small mb-0 fw-medium">Phân tích hiệu năng vận hành và báo cáo tuân thủ thông tư 200/2014/TT-BTC</p>
            </div>
        </div>
        <div class="d-flex gap-3">
            <el-button size="large" class="rounded-pill px-4" :icon="PieChart">Góc nhìn AI</el-button>
            <el-button type="primary" size="large" class="rounded-pill px-4 shadow-sm fw-bold" :icon="Plus">Tạo báo cáo</el-button>
        </div>
    </div>

    <div class="row g-4 mb-5">
        <!-- Distribution Analytics -->
        <div class="col-12 col-xl-4">
            <div class="distribution-card bg-white rounded-4 shadow-sm border-0 p-4 h-100">
                <h5 class="fw-bold text-dark mb-4">Phân bố tồn kho</h5>
                <div class="chart-box d-flex justify-content-center">
                    <VueApexCharts :options="donutOptions" :series="donutSeries" type="donut" height="320" />
                </div>
            </div>
        </div>

        <!-- KPI Grid -->
        <div class="col-12 col-xl-8">
            <div class="row g-4 h-100">
                <div class="col-6">
                    <div class="kpi-card-v2 p-4 rounded-4 bg-white shadow-sm border-0 h-100 transition-all d-flex flex-column justify-content-between">
                        <div>
                            <p class="text-muted tiny fw-bold text-uppercase tracking-widest mb-2">TỈ LỆ LỖI</p>
                            <h2 class="fw-black text-success mb-1 ls-tight">0.00%</h2>
                        </div>
                        <div class="spark-line-placeholder mt-3 bg-success bg-opacity-5 rounded-4 p-2">
                           <div class="d-flex align-items-end gap-1 h-30px">
                              <div v-for="i in 10" :key="i" class="bg-success flex-grow-1 rounded-pill" :style="{ height: (Math.random()*20+10)+'px' }"></div>
                           </div>
                        </div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="kpi-card-v2 p-4 rounded-4 bg-primary text-white shadow-sm border-0 h-100 transition-all d-flex flex-column justify-content-between">
                        <div>
                            <p class="tiny fw-bold text-uppercase tracking-widest mb-2 opacity-75">XOAY VÒNG KHO</p>
                            <h2 class="fw-black mb-1 ls-tight text-white">4.2<span class="fs-6 fw-bold">x</span></h2>
                        </div>
                        <div class="spark-line-placeholder mt-3 bg-white bg-opacity-10 rounded-4 p-2">
                           <div class="d-flex align-items-end gap-1 h-30px">
                              <div v-for="i in 10" :key="i" class="bg-white flex-grow-1 rounded-pill" :style="{ height: (Math.random()*20+10)+'px' }"></div>
                           </div>
                        </div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="mini-kpi p-3 rounded-4 bg-white shadow-sm border-0 d-flex align-items-center gap-3">
                         <div class="mini-icon rounded-circle bg-warning bg-opacity-10 text-warning p-2"><el-icon><Timer /></el-icon></div>
                         <div><p class="ultra-tiny text-muted fw-bold mb-0">THỜI GIAN CHỌN</p><h5 class="fw-black text-dark mb-0 ls-tight">14m</h5></div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="mini-kpi p-3 rounded-4 bg-white shadow-sm border-0 d-flex align-items-center gap-3">
                         <div class="mini-icon rounded-circle bg-info bg-opacity-10 text-info p-2"><el-icon><Checked /></el-icon></div>
                         <div><p class="ultra-tiny text-muted fw-bold mb-0">ACCURACY</p><h5 class="fw-black text-dark mb-0 ls-tight">99.8%</h5></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Inventory Ledger Table -->
    <div class="report-main-card bg-white rounded-4 shadow-sm border-0 overflow-hidden mb-5">
        <div class="p-4 d-flex justify-content-between align-items-center border-bottom bg-light bg-opacity-30">
            <h5 class="fw-bold text-dark mb-0 ls-tight">Theo dõi Biến động Tồn kho & Định mức</h5>
            <el-button type="info" link :icon="Histogram" class="fw-bold tiny">Xuất báo cáo Excel</el-button>
        </div>
        <el-table :data="inventoryStatus" stripe class="premium-table">
            <el-table-column label="Sản phẩm" width="300">
                <template #default="scope">
                    <div class="d-flex align-items-center gap-3">
                        <div class="report-icon-box p-2 bg-light rounded-3 text-primary"><el-icon><Histogram /></el-icon></div>
                        <span class="fw-bold text-dark">{{ scope.row.productName }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="Tồn kho" width="150" align="center">
                <template #default="scope"><span class="fw-black">{{ scope.row.currentStock }}</span></template>
            </el-table-column>
            <el-table-column label="Trạng thái" width="150" align="center">
                 <template #default="scope">
                    <el-tag :type="scope.row.currentStock <= (scope.row.minStock || 0) ? 'danger' : 'success'" size="small" class="rounded-pill border-0 px-3 fw-bold tiny">
                        {{ scope.row.currentStock <= (scope.row.minStock || 0) ? 'CẢNH BÁO' : 'AN TOÀN' }}
                    </el-tag>
                 </template>
            </el-table-column>
            <el-table-column label="Chứng từ tiêu chuẩn" align="right">
                <template #default="scope">
                    <el-button size="small" :icon="markRaw(List)" plain class="rounded-pill px-3" @click="openTheKho(scope.row)">Mẫu 06-VT</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>

    <!-- Circular 200: Thẻ Kho Dialog -->
    <el-dialog v-model="theKhoVisible" title="THẺ KHO (Mẫu số 06-VT)" width="950px" align-center class="standard-form-dialog">
        <div class="standard-form-header text-center mb-4">
             <div class="d-flex justify-content-between align-items-start mb-4">
                <div class="text-start ultra-tiny fw-bold">
                    Đơn vị: ONTOP LOGISTICS<br>
                    Địa chỉ: Warehouse Node A1
                </div>
                <div class="text-end ultra-tiny fw-bold border p-2">
                    Mẫu số 06-VT<br>
                    (Ban hành theo Thông tư số 200/2014/TT-BTC)
                </div>
             </div>
             <h4 class="fw-black ls-tight mb-2">THẺ KHO</h4>
             <p class="small text-muted mb-0">Ngày lập thẻ: {{ new Date().toLocaleDateString() }}</p>
             <p class="fw-bold mt-2">Tên sản phẩm: {{ selectedProduct?.productName }}</p>
        </div>

        <div class="standard-table-wrapper border">
            <table class="w-100 standard-form-table">
                <thead>
                    <tr>
                        <th rowspan="2">Ngày tháng</th>
                        <th colspan="2">Chứng từ</th>
                        <th rowspan="2">Diễn giải</th>
                        <th colspan="3">Số lượng</th>
                        <th rowspan="2">Ký xác nhận</th>
                    </tr>
                    <tr>
                        <th>Số hiệu</th>
                        <th>Ngày</th>
                        <th>Nhập</th>
                        <th>Xuất</th>
                        <th>Tồn</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(log, idx) in ledgers" :key="idx">
                        <td class="text-center">{{ log.date }}</td>
                        <td class="text-center fw-bold">{{ log.docNo }}</td>
                        <td class="text-center">{{ log.date }}</td>
                        <td>{{ log.reason }}</td>
                        <td class="text-center text-success fw-bold">{{ log.in > 0 ? log.in : '' }}</td>
                        <td class="text-center text-danger fw-bold">{{ log.out > 0 ? log.out : '' }}</td>
                        <td class="text-center fw-black">{{ log.balance }}</td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="standard-form-footer row mt-5 mb-4 text-center">
            <div class="col-4">
                <p class="fw-bold mb-5 small">Người lập thẻ</p>
                <p class="ultra-tiny text-muted mt-5">(Ký, họ tên)</p>
            </div>
            <div class="col-4">
                <p class="fw-bold mb-5 small">Kế toán trưởng</p>
                <p class="ultra-tiny text-muted mt-5">(Ký, họ tên)</p>
            </div>
            <div class="col-4">
                <p class="fw-bold mb-1 small">Ngày ... tháng ... năm ...</p>
                <p class="fw-bold mb-5 small">Giám đốc</p>
                <p class="ultra-tiny text-muted mt-5">(Ký, họ tên, đóng dấu)</p>
            </div>
        </div>

        <template #footer>
            <div class="d-flex justify-content-end gap-2 p-3 border-top">
                <el-button @click="theKhoVisible = false">Đóng</el-button>
                <el-button type="primary" :icon="markRaw(Printer)">In báo cáo</el-button>
            </div>
        </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.reports-premium { font-family: 'Plus Jakarta Sans', sans-serif; }
.fw-black { font-weight: 850; }
.ls-tight { letter-spacing: -1px; }
.tiny { font-size: 10px; }
.ultra-tiny { font-size: 9px; }

.glass-hero { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(10px); }
.icon-box-reports { background: linear-gradient(135deg, #6366f1 0%, #4338ca 100%); }
.shadow-indigo { box-shadow: 0 10px 20px rgba(99, 102, 241, 0.3); }

/* Standard Form Style for Circular 200 */
.standard-form-table { border-collapse: collapse; font-family: sans-serif; font-size: 12px; }
.standard-form-table th, .standard-form-table td { border: 1px solid #1e293b; padding: 10px; }
.standard-form-table th { background: #f8fafc; text-transform: uppercase; font-weight: 700; text-align: center; }

.bg-indigo { background-color: #6366f1; }
.text-indigo { color: #6366f1; }

.premium-table :deep(.el-table__header th) {
    background-color: #f8fafc; color: #475569; font-weight: 700; font-size: 11px; text-transform: uppercase;
}
</style>