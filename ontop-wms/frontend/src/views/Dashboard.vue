<script setup>
import { ref, onMounted, markRaw } from 'vue'
import {
  Timer, ArrowUp, ArrowDown, TrendCharts,
  Box, Van, Warning, Connection, ShoppingCart, 
  Histogram, UserFilled, MoreFilled
} from '@element-plus/icons-vue'
import VueApexCharts from 'vue3-apexcharts'
import api from '../api'

const stats = ref([])
const recentActivities = ref([])
const loading = ref(false)
const timeRange = ref('30d')

const chartOptions = ref({
  chart: { type: 'area', toolbar: { show: false }, sparkline: { enabled: false }, zoom: { enabled: false } },
  colors: ['#4f46e5', '#10b981'],
  stroke: { curve: 'smooth', width: 3 },
  fill: { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.45, opacityTo: 0.05, stops: [20, 100] } },
  dataLabels: { enabled: false },
  xaxis: { categories: ['01/03', '05/03', '10/03', '15/03', '20/03', '25/03', '30/03'], labels: { style: { colors: '#94a3b8', fontSize: '10px', fontWeight: 600 } }, axisBorder: { show: false }, axisTicks: { show: false } },
  yaxis: { show: false },
  grid: { show: false },
  tooltip: { theme: 'dark', x: { show: true } }
})

const chartSeries = ref([
  { name: 'Inbound', data: [31, 40, 28, 51, 42, 109, 100] },
  { name: 'Outbound', data: [11, 32, 45, 32, 34, 52, 41] }
])

const fetchDashboardData = async () => {
  try {
    loading.value = true
    const statsRes = await api.get('/reports/dashboard-stats')
    
    // Premium Color Mapping & Icon Assignment
    const colorMatrix = {
      'Sản phẩm': { color: '#6366f1', gradient: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)', icon: Box },
      'Kho': { color: '#f59e0b', gradient: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)', icon: Connection },
      'Nhập': { color: '#10b981', gradient: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)', icon: Van },
      'Xuất': { color: '#ef4444', gradient: 'linear-gradient(135deg, #ef4444 0%, #f87171 100%)', icon: ShoppingCart }
    }

    stats.value = statsRes.data.map(s => {
      const config = Object.entries(colorMatrix).find(([key]) => s.title.includes(key))?.[1] || { 
        color: '#3b82f6', 
        gradient: 'linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%)', 
        icon: Warning 
      }
      return {
        ...s,
        icon: markRaw(config.icon),
        color: config.color,
        gradient: config.gradient
      }
    })

    const activityRes = await api.get('/reports/recent-activities')
    recentActivities.value = activityRes.data
  } catch (error) {
    console.error('Lỗi tải Dashboard:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<template>
  <div class="dashboard-premium" v-loading="loading">
    <!-- Header Section -->
    <div class="d-flex justify-content-between align-items-end mb-5">
      <div>
        <h2 class="fw-black text-dark mb-1 tracking-tight">System Overview</h2>
        <p class="text-muted mb-0 small fw-medium">Real-time warehouse health & inventory analytics</p>
      </div>
      <div class="d-flex gap-2 p-1 bg-white rounded-pill shadow-sm border">
        <el-button type="primary" link class="px-4 py-2 small fw-bold">Live Feed</el-button>
        <el-button type="info" link class="px-4 py-2 small fw-bold text-muted">History</el-button>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="row g-4 mb-5">
      <div v-for="stat in stats" :key="stat.title" class="col-12 col-md-6 col-xl-3">
        <div class="stat-card-v2 bg-white rounded-4 shadow-sm border-0 position-relative overflow-hidden h-100">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start mb-4">
              <div class="icon-holder rounded-4 d-flex align-items-center justify-content-center shadow-sm" :style="{ background: stat.gradient }">
                <el-icon :size="24" class="text-white"><component :is="stat.icon" /></el-icon>
              </div>
              <div :class="['trend-pill px-2 py-1 rounded-pill small fw-bold d-flex align-items-center', stat.isUp ? 'text-success bg-success-light' : 'text-danger bg-danger-light']">
                <el-icon class="me-1"><component :is="stat.isUp ? ArrowUp : ArrowDown" /></el-icon>
                {{ stat.trend }}
              </div>
            </div>
            
            <p class="text-muted tiny text-uppercase fw-bold tracking-widest mb-1">{{ stat.title }}</p>
            <h2 class="fw-black text-dark mb-0 ls-tight">{{ stat.value }}</h2>
            
            <div class="mini-chart mt-3 opacity-25">
               <div v-for="i in 12" :key="i" class="bar" :style="{ height: Math.random() * 20 + 5 + 'px' }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <!-- Main Visual Analytics with ApexCharts -->
      <div class="col-12 col-lg-8">
        <div class="analytics-card bg-white rounded-4 shadow-sm border-0 h-100 overflow-hidden">
          <div class="p-4 d-flex justify-content-between align-items-center">
            <div>
              <h5 class="fw-bold text-dark mb-1">Inventory Performance</h5>
              <p class="text-muted tiny mb-0">Aggregate throughput across all nodes</p>
            </div>
            <el-select v-model="timeRange" class="premium-select" style="width: 130px">
               <el-option label="7 days" value="7d" />
               <el-option label="30 days" value="30d" />
            </el-select>
          </div>
          
          <div class="p-4 pt-0">
            <div class="chart-container rounded-4 bg-light bg-opacity-50 p-2">
               <VueApexCharts :options="chartOptions" :series="chartSeries" height="300" />
            </div>
            
            <div class="row mt-4 pt-2 border-top g-4">
               <div class="col-4 text-center">
                  <p class="text-muted tiny fw-bold mb-1">ACCURACY</p>
                  <h5 class="fw-bold text-success mb-0">99.8%</h5>
               </div>
               <div class="col-4 text-center border-start border-end">
                  <p class="text-muted tiny fw-bold mb-1">AVRG LOAD</p>
                  <h5 class="fw-bold text-primary mb-0">2.4s</h5>
               </div>
               <div class="col-4 text-center">
                  <p class="text-muted tiny fw-bold mb-1">NODES</p>
                  <h5 class="fw-bold text-warning mb-0">14 Active</h5>
               </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Activity Timeline -->
      <div class="col-12 col-lg-4">
        <div class="activity-card bg-white rounded-4 shadow-sm border-0 h-100 flex-column overflow-hidden">
          <div class="p-4 d-flex justify-content-between align-items-center border-bottom border-light">
             <h5 class="fw-bold text-dark mb-0">Operational Logs</h5>
             <el-button circle :icon="MoreFilled" size="small" type="info" link />
          </div>
          
          <div class="p-4 flex-grow-1 custom-scrollbar overflow-auto" style="max-height: 400px">
            <div class="modern-timeline ps-3">
              <div v-for="activity in recentActivities" :key="activity.id" class="activity-node pb-4 position-relative">
                <div :class="['node-dot', activity.type === 'alert' ? 'bg-danger' : 'bg-primary']"></div>
                <div class="node-content bg-light p-3 rounded-4 border border-white">
                   <p class="mb-1 text-dark fw-bold small">{{ activity.text }}</p>
                   <div class="d-flex justify-content-between align-items-center">
                      <span class="text-muted ultra-tiny fw-bold"><el-icon class="me-1"><Timer /></el-icon>{{ activity.time }}</span>
                      <el-tag size="small" effect="light" class="border-0 rounded-pill ultra-tiny fw-bold">{{ activity.user }}</el-tag>
                   </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="p-3 bg-light bg-opacity-50 text-center">
             <el-button type="primary" link class="fw-bold small">VIEW FULL AUDIT TRAIL</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-premium {
  font-family: 'Plus Jakarta Sans', sans-serif;
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.fw-black { font-weight: 850; }
.tracking-tight { letter-spacing: -1px; }
.tracking-widest { letter-spacing: 2px; }
.ls-tight { letter-spacing: -0.5px; }
.tiny { font-size: 10px; }
.ultra-tiny { font-size: 9px; }

.icon-holder {
  width: 52px;
  height: 52px;
}

.stat-card-v2 {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card-v2:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.08) !important;
}

.bg-success-light { background: rgba(16, 185, 129, 0.1); }
.bg-danger-light { background: rgba(239, 68, 68, 0.1); }

.mini-chart {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 30px;
}

.mini-chart .bar {
  flex: 1;
  background: currentColor;
  border-radius: 2px;
}

/* Modern Timeline */
.modern-timeline {
  border-left: 2px solid #f1f5f9;
}

.activity-node::before {
  content: '';
  position: absolute;
  left: -2px;
  top: 0;
  width: 2px;
  height: 100%;
  background: var(--primary);
  opacity: 0.1;
}

.node-dot {
  position: absolute;
  left: -7px;
  top: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
  z-index: 1;
}

.premium-select :deep(.el-input__wrapper) {
  border-radius: 99px;
  background: #f8fafc;
  box-shadow: none;
  border: 1px solid #e2e8f0;
}
</style>