<script setup>
import { ref, onMounted, markRaw } from 'vue'
import {
  Timer as TimerIcon, ArrowUp, ArrowDown, TrendCharts,
  Box as BoxIcon, Van as VanIcon, Warning as WarningIcon, Connection as ConnectionIcon
} from '@element-plus/icons-vue'
import api from '../api'

const stats = ref([])
const recentActivities = ref([])
const alerts = ref([])
const loading = ref(false)
const value = ref('30d')

const fetchDashboardData = async () => {
  try {
    loading.value = true
    const userRole = localStorage.getItem('userRole')
    
    // Fetch stats
    if (userRole === 'ADMIN') {
      const statsRes = await api.get('/reports/dashboard-stats')
      stats.value = statsRes.data.map(s => ({
        ...s,
        icon: markRaw(s.title.includes('Sản phẩm') ? BoxIcon :
          s.title.includes('Kho') ? ConnectionIcon :
            s.title.includes('Nhập') ? VanIcon : WarningIcon)
      }))
    }

    // Fetch alerts/notifications
    const alertRes = await api.get('/notifications/unread')
    alerts.value = alertRes.data

    // Fetch activities
    if (['ADMIN', 'MANAGER'].includes(userRole)) {
      const activityRes = await api.get('/reports/recent-activities')
      recentActivities.value = activityRes.data
    }
  } catch (error) {
    console.error('Lỗi tải Dashboard:', error)
  } finally {
    loading.value = false
  }
}

const markAsRead = async (id) => {
  try {
    await api.post(`/notifications/${id}/read`)
    alerts.value = alerts.value.filter(a => a.id !== id)
  } catch (error) {
    console.error('Lỗi đánh dấu đã đọc:', error)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold mb-1">Tổng quan hệ thống</h3>
        <p class="text-muted small mb-0">Chào mừng trở lại! Dưới đây là dữ liệu mới nhất hôm nay.</p>
      </div>
      <div class="d-flex gap-2">
        <el-button type="primary" plain :icon="TimerIcon">Thực hiện báo cáo</el-button>
        <el-button type="primary">Xuất dữ liệu Excel</el-button>
      </div>
    </div>

    <!-- Critical Inventory Alerts -->
    <div v-if="alerts.length > 0" class="alerts-section mb-4">
      <el-alert
        v-for="alert in alerts"
        :key="alert.id"
        :title="alert.message"
        :type="alert.type === 'EXPIRED' ? 'error' : 'warning'"
        show-icon
        class="mb-2 shadow-sm border-0 animate__animated animate__headShake"
        @close="markAsRead(alert.id)"
      >
        <template #default>
          <div class="d-flex justify-content-between align-items-center w-100 mt-1">
            <span class="small text-muted">{{ new Date(alert.createdAt).toLocaleString() }}</span>
            <el-link type="primary" :underline="false" class="small fw-bold">Xem chi tiết lô hàng</el-link>
          </div>
        </template>
      </el-alert>
    </div>

    <!-- Stats Cards -->
    <div class="row g-4 mb-4">
      <div v-for="stat in stats" :key="stat.title" class="col-12 col-md-6 col-lg-3">
        <div class="card border-0 shadow-sm h-100 stat-card transition-all">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div
                :class="`icon-shape bg-${stat.color || 'primary'} bg-opacity-10 text-${stat.color || 'primary'} rounded-3 p-3`">
                <el-icon class="fs-4">
                  <component :is="stat.icon" />
                </el-icon>
              </div>
              <span
                :class="`badge rounded-pill bg-${stat.isUp ? 'success' : 'danger'} bg-opacity-10 text-${stat.isUp ? 'success' : 'danger'} border-0 px-2 py-1 small`">
                <el-icon class="me-1">
                  <component :is="stat.isUp ? ArrowUp : ArrowDown" />
                </el-icon>
                {{ stat.trend }}
              </span>
            </div>
            <h6 class="text-muted fw-normal mb-1">{{ stat.title }}</h6>
            <h3 class="fw-bold mb-0 text-dark">{{ stat.value }}</h3>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <!-- Chart Placeholder Section -->
      <div class="col-12 col-lg-8">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-0 p-4 d-flex justify-content-between align-items-center">
            <h5 class="fw-bold mb-0">Biểu đồ tăng trưởng sản phẩm</h5>
            <el-select v-model="value" placeholder="Chọn thời gian" style="width: 150px;">
              <el-option label="7 ngày qua" value="7d" />
              <el-option label="30 ngày qua" value="30d" />
            </el-select>
          </div>
          <div class="card-body p-4 pt-0">
            <div
              class="chart-box rounded-4 bg-light d-flex align-items-center justify-content-center flex-column py-5 mt-2 border border-dashed text-primary">
              <el-icon class="fs-1 mb-2">
                <TrendCharts />
              </el-icon>
              <p class="fw-medium mb-0">Đang khởi tạo dữ liệu trực quan...</p>
              <span class="text-muted small">Tích hợp với Spring Boot API để hiển thị dữ liệu thực</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activities -->
      <div class="col-12 col-lg-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-0 p-4">
            <h5 class="fw-bold mb-0">Hoạt động gần đây</h5>
          </div>
          <div class="card-body p-4 pt-0">
            <div class="timeline-activity">
              <div v-for="activity in recentActivities" :key="activity.id"
                class="activity-item d-flex gap-3 mb-4 last-child-mb-0">
                <div class="activity-icon-container">
                  <div :class="`activity-dot bg-${activity.type === 'alert' ? 'danger' : 'primary'}`"></div>
                </div>
                <div class="activity-content">
                  <p class="mb-1 text-dark fw-medium small">{{ activity.text }}</p>
                  <div class="d-flex justify-content-between align-items-center">
                    <span class="text-muted" style="font-size: 11px;"><el-icon class="me-1">
                        <TimerIcon />
                      </el-icon>{{ activity.time }}</span>
                    <span class="badge bg-light text-muted fw-normal border" style="font-size: 10px;">{{ activity.user
                      }}</span>
                  </div>
                </div>
              </div>
            </div>
            <button class="btn btn-light w-100 mt-3 text-primary fw-bold small">Xem tất cả hoạt động</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05) !important;
}

.icon-shape {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-box {
  min-height: 250px;
  border-style: dashed !important;
  border-width: 2px !important;
}

.activity-item {
  position: relative;
}

.activity-icon-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 5px;
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.05);
}

.activity-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 15px;
  width: 2px;
  height: calc(100% + 15px);
  background-color: #f1f5f9;
  z-index: 0;
}

.last-child-mb-0:last-child {
  margin-bottom: 0 !important;
}

.activity-content {
  flex: 1;
}
</style>