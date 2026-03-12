<script setup>
import { ref, onMounted } from 'vue'
import {
  Document, Download, PieChart, Plus,
  View, ArrowUp, ArrowDown, Warning, Checked
} from '@element-plus/icons-vue'
import api from '../api'

const inventoryStatus = ref([])
const alerts = ref([])
const loading = ref(false)

const fetchReportsData = async () => {
  try {
    loading.value = true
    const [invRes, alertRes] = await Promise.all([
      api.get('/reports/inventory-status'),
      api.get('/reports/alerts')
    ])
    inventoryStatus.value = invRes.data // [{ productName: '...', currentStock: 10, ... }]
    alerts.value = alertRes.data // [{ message: '...', severity: '...' }]
  } catch (error) {
    console.error('Lỗi tải báo cáo:', error)
  } finally {
    loading.value = false
  }
}

const reports = ref([
  { id: 1, name: 'Báo cáo Nhập-Xuất-Tồn tháng 02/2026', date: '2026-03-01', type: 'Tổng hợp', status: 'Ready' },
  { id: 2, name: 'Báo cáo Kiểm kê Kho Tổng', date: '2026-03-05', type: 'Kiểm kê', status: 'Ready' },
  { id: 3, name: 'Phân tích SKU tiêu thụ nhanh', date: '2026-03-08', type: 'Phân tích', status: 'Ready' },
])

onMounted(() => {
  fetchReportsData()
})
</script>

<template>
  <div class="reports-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-bold mb-1">Báo cáo & Thống kê</h4>
        <p class="text-muted small mb-0">Dữ liệu vận hành thời gian thực từ hệ thống</p>
      </div>
      <div class="d-flex gap-2">
        <el-button :icon="PieChart" type="info" plain>Phân tích AI</el-button>
        <el-button type="primary" :icon="Plus">Yêu cầu báo cáo mới</el-button>
      </div>
    </div>

    <!-- Inventory Alerts -->
    <div class="row mb-4" v-if="alerts.length > 0">
      <div class="col-12">
        <div class="card border-0 shadow-sm border-start border-4 border-danger">
          <div class="card-body p-3">
            <h6 class="fw-bold text-danger mb-3 d-flex align-items-center">
              <el-icon class="me-2">
                <Warning />
              </el-icon> Cảnh báo tồn kho quan trọng
            </h6>
            <div class="d-flex flex-wrap gap-2">
              <el-tag v-for="(alert, idx) in alerts" :key="idx" type="danger" effect="light" class="rounded-pill">
                {{ alert.message }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-8">
        <!-- Operation summary with real data -->
        <div class="card border-0 shadow-sm p-4 h-100">
          <h5 class="fw-bold mb-4">Trạng thái tồn kho hiện tại</h5>
          <div class="table-responsive" v-loading="loading">
            <table class="table table-hover align-middle mb-0">
              <thead class="small text-muted uppercase">
                <tr>
                  <th>Sản phẩm</th>
                  <th class="text-center">Tồn kho</th>
                  <th class="text-center">Định mức</th>
                  <th class="text-end">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in inventoryStatus.slice(0, 10)" :key="idx">
                  <td class="fw-bold">{{ item.productName }}</td>
                  <td class="text-center">{{ item.currentStock }}</td>
                  <td class="text-center text-muted">{{ item.minStock || 0 }}</td>
                  <td class="text-end">
                    <span
                      :class="['badge rounded-pill', item.currentStock <= (item.minStock || 0) ? 'bg-danger bg-opacity-10 text-danger' : 'bg-success bg-opacity-10 text-success']">
                      {{ item.currentStock <= (item.minStock || 0) ? 'Thấp' : 'An toàn' }} </span>
                  </td>
                </tr>
                <tr v-if="inventoryStatus.length === 0">
                  <td colspan="4" class="text-center py-4 text-muted small">Không có dữ liệu tồn kho</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <!-- Static reports list -->
        <h6 class="fw-bold mb-3 d-flex align-items-center">
          <el-icon class="me-2 text-primary">
            <Document />
          </el-icon> Tài liệu báo cáo
        </h6>
        <div v-for="report in reports" :key="report.id" class="card border-0 shadow-sm mb-3 p-3 report-mini-card">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="small fw-bold">{{ report.name }}</div>
              <div class="text-muted" style="font-size: 11px;">{{ report.date }} • {{ report.type }}</div>
            </div>
            <el-button size="small" type="primary" :icon="Download" circle plain></el-button>
          </div>
        </div>
        <el-button class="w-100 mt-2" plain>Xem tất cả lưu trữ</el-button>
      </div>
    </div>

    <!-- Detailed Analytics Summary -->
    <div class="card border-0 shadow-sm p-4 mb-4">
      <h5 class="fw-bold mb-4 d-flex align-items-center justify-content-between">
        Tóm tắt vận hành
        <span class="small fw-normal text-muted" style="font-size: 12px;">Cập nhật: {{ new Date().toLocaleString()
          }}</span>
      </h5>
      <div class="row text-center g-4">
        <div class="col-6 col-md-3">
          <p class="text-muted small mb-1 uppercase ls-1">Sản phẩm lỗi</p>
          <h3 class="fw-extrabold text-success">0</h3>
          <span class="text-success small fw-bold"><el-icon>
              <Checked />
            </el-icon> Tuyệt vời</span>
        </div>
        <div class="col-6 col-md-3">
          <p class="text-muted small mb-1 uppercase ls-1">Tỷ lệ xoay vòng</p>
          <h3 class="fw-extrabold text-primary">4.2</h3>
          <span class="text-muted small">Lần/Tháng</span>
        </div>
        <div class="col-6 col-md-3">
          <p class="text-muted small mb-1 uppercase ls-1">Thời gian nhập trung bình</p>
          <h3 class="fw-extrabold text-warning">14</h3>
          <span class="text-muted small">Phút/Lô</span>
        </div>
        <div class="col-6 col-md-3">
          <p class="text-muted small mb-1 uppercase ls-1">Độ chính xác tồn</p>
          <h3 class="fw-extrabold text-success">99.8%</h3>
          <span class="text-success small fw-bold">Rất cao</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fw-extrabold {
  font-weight: 800;
}

.ls-1 {
  letter-spacing: 1px;
}

.report-mini-card:hover {
  transform: translateX(5px);
  border-left: 3px solid var(--el-color-primary) !important;
  background: #f8fafc;
  transition: all 0.2s ease;
}

th {
  padding-bottom: 15px;
  border-bottom: 1px solid #f1f5f9;
}

td {
  padding-top: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dotted #f1f5f9;
  font-size: 14px;
}

.uppercase {
  text-transform: uppercase;
}
</style>