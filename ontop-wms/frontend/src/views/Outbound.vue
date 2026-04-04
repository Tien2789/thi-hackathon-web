<script setup>
import { ref, onMounted, markRaw } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ShoppingCart, Plus, Delete, Check, Search, 
  Box, UserFilled, Connection, Timer, 
  MagicStick, WarningFilled, DocumentOutlined,
  Sell
} from '@element-plus/icons-vue'

const vouchers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const products = ref([])
const categories = ref([])
const units = ref([])

const form = ref({
    voucherCode: '',
    releaseDate: new Date().toISOString().split('T')[0],
    receiver: '',
    reason: '',
    warehouseName: '',
    details: []
})

const newDetail = ref({
    productId: null,
    productName: '',
    skuCode: '',
    quantity: 1,
    remark: ''
})

const fetchVouchers = async () => {
    try {
        loading.value = true
        const response = await api.get('/inventory/outbound')
        vouchers.value = response.data
    } catch (error) {
        ElMessage.error('Không thể tải danh sách phiếu xuất')
    } finally {
        loading.value = false
    }
}

const fetchProducts = async () => {
    try {
        const response = await api.get('/products')
        products.value = response.data
    } catch (error) {
        console.error('Lỗi tải sản phẩm:', error)
    }
}

const addDetail = () => {
    if (!newDetail.value.productName || newDetail.value.quantity <= 0) {
        ElMessage.warning('Vui lòng chọn sản phẩm và số lượng')
        return
    }
    form.value.details.push({ ...newDetail.value })
    newDetail.value = { productId: null, productName: '', skuCode: '', quantity: 1, remark: '' }
}

const handleCreate = async () => {
    if (form.value.details.length === 0) {
        ElMessage.warning('Vui lòng thêm ít nhất một sản phẩm')
        return
    }
    try {
        await api.post('/inventory/outbound', form.value)
        ElMessage.success('Đã lập phiếu xuất kho thành công')
        dialogVisible.value = false
        fetchVouchers()
    } catch (error) {
        ElMessage.error('Lỗi khi lưu phiếu xuất')
    }
}

onMounted(() => {
    fetchVouchers()
    fetchProducts()
})
</script>

<template>
    <div class="outbound-premium p-4">
        <!-- Hero Header -->
        <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
            <div class="d-flex align-items-center gap-4">
                <div class="icon-box-outbound bg-danger text-white rounded-4 p-3 shadow-danger">
                    <el-icon :size="32"><ShoppingCart /></el-icon>
                </div>
                <div>
                    <h3 class="fw-black text-dark mb-1 ls-tight">Xuất Kho Vận Hành</h3>
                    <p class="text-muted small mb-0 fw-medium">Điều phối xuất hàng, quản lý đơn vận và cập nhật tồn kho xuất</p>
                </div>
            </div>
            <div class="d-flex gap-3">
                <div class="search-pill d-flex align-items-center px-3 rounded-pill bg-white border">
                    <el-icon class="text-muted"><Search /></el-icon>
                    <input type="text" placeholder="Tìm số phiếu xuất..." class="form-control-plaintext ps-2 small">
                </div>
                <el-button type="danger" size="large" class="rounded-pill px-4 shadow-sm fw-bold" :icon="Plus" @click="dialogVisible = true">Tạo lệnh xuất kho</el-button>
            </div>
        </div>

        <!-- Recent Outbound Vouchers -->
        <div class="row g-4" v-loading="loading">
            <div v-for="v in vouchers" :key="v.id" class="col-12 col-md-6 col-xl-4">
                <div class="vocation-card bg-white rounded-4 shadow-sm border-0 transition-all">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <span class="badge rounded-pill bg-danger bg-opacity-10 text-danger border-0 px-3 py-1 tiny fw-bold text-uppercase ls-wide mb-2">Released</span>
                                <h5 class="fw-bold text-dark mb-0 ls-tight">{{ v.voucherCode }}</h5>
                            </div>
                            <div class="text-end">
                                <p class="text-muted tiny fw-bold mb-0 text-uppercase">{{ v.releaseDate }}</p>
                                <p class="text-muted ultra-tiny">{{ v.warehouseName || 'Main Distribution' }}</p>
                            </div>
                        </div>

                        <div class="deliverer-info bg-light rounded-3 p-3 mb-3 d-flex align-items-center gap-3">
                            <el-avatar :size="36" icon="User" class="bg-white text-danger shadow-sm" />
                            <div>
                                <p class="text-muted ultra-tiny fw-bold mb-0">người nhận hàng</p>
                                <p class="text-dark fw-bold small mb-0">{{ v.receiver }}</p>
                            </div>
                        </div>

                        <div class="items-summary mb-3">
                            <p class="text-muted tiny fw-bold mb-2">DANH MỤC XUẤT</p>
                            <div class="d-flex flex-wrap gap-2">
                                <el-tag v-for="item in v.details?.slice(0, 3)" :key="item.id" size="small" type="danger" effect="light" class="rounded-pill border-0 px-3">
                                    {{ item.productName }} x{{ item.quantity }}
                                </el-tag>
                                <el-tag v-if="v.details?.length > 3" size="small" type="info" class="rounded-pill border-0">
                                    +{{ v.details.length - 3 }} món khác
                                </el-tag>
                            </div>
                        </div>

                        <div class="reason-section pt-2 mb-3">
                             <p class="text-muted ultra-tiny mb-1 fw-bold">LÝ DO XUẤT:</p>
                             <p class="text-dark small mb-0 fst-italic">{{ v.reason || 'Sản xuất / Kinh doanh' }}</p>
                        </div>

                        <div class="card-footer-v2 d-flex justify-content-between align-items-center pt-3 border-top">
                           <div class="d-flex align-items-center gap-2">
                               <el-icon class="text-danger"><Timer /></el-icon>
                               <span class="text-muted ultra-tiny fw-bold">Release: {{ v.releaseDate }}</span>
                           </div>
                           <el-button type="danger" link icon="DocumentOutlined" size="small" class="fw-bold">XUẤT FILE</el-button>
                        </div>
                    </div>
                </div>
            </div>
            
            <div v-if="vouchers.length === 0" class="col-12 text-center py-5">
                <el-empty description="Chưa có lệnh xuất kho nào" />
            </div>
        </div>

        <!-- Create Outbound Dialog -->
        <el-dialog v-model="dialogVisible" title="Lệnh Điều Phối Xuất Kho" width="900px" align-center class="premium-dialog rounded-4">
            <el-form :model="form" label-position="top" class="premium-form p-3">
                <div class="row g-3">
                    <div class="col-md-4">
                        <el-form-item label="Số lệnh xuất">
                            <el-input v-model="form.voucherCode" placeholder="PXK-AUTO" />
                        </el-form-item>
                    </div>
                    <div class="col-md-4">
                        <el-form-item label="Ngày xuất">
                            <el-date-picker v-model="form.releaseDate" type="date" value-format="YYYY-MM-DD" class="w-100" />
                        </el-form-item>
                    </div>
                    <div class="col-md-4">
                        <el-form-item label="Người nhận hàng">
                            <el-input v-model="form.receiver" placeholder="Tên đơn vị/cá nhân" />
                        </el-form-item>
                    </div>
                    <div class="col-12">
                        <el-form-item label="Lý do xuất kho (Mục đích)">
                            <el-input v-model="form.reason" type="textarea" placeholder="Nhập chi tiết lý do điều phối hàng hóa..." />
                        </el-form-item>
                    </div>
                </div>

                <div class="detail-builder bg-error-light rounded-4 p-4 mt-4 mb-4 border border-white">
                    <h6 class="fw-bold text-dark mb-3"><el-icon class="me-2 text-danger"><Box /></el-icon>Chọn hàng hóa xuất kho</h6>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <el-form-item label="Sản phẩm trong kho">
                                <el-select v-model="newDetail.productName" filterable placeholder="Tìm theo tên hoặc SKU" class="w-100">
                                    <el-option v-for="p in products" :key="p.id" :label="`${p.productName} (Tồn: ${p.currentStock})`" :value="p.productName" />
                                </el-select>
                            </el-form-item>
                        </div>
                        <div class="col-md-3">
                            <el-form-item label="Số lượng xuất">
                                <el-input-number v-model="newDetail.quantity" :min="1" class="w-100" />
                            </el-form-item>
                        </div>
                        <div class="col-md-3 d-flex align-items-end p-2">
                             <el-button type="danger" class="w-100 rounded-pill shadow-sm fw-bold" :icon="Plus" @click="addDetail">Thêm vào lệnh</el-button>
                        </div>
                    </div>
                </div>

                <div v-if="form.details.length > 0" class="preview-list p-2">
                    <p class="text-muted tiny fw-bold mb-2">DANH SÁCH LỆNH XUẤT ({{ form.details.length }})</p>
                    <el-table :data="form.details" stripe class="premium-table rounded-3 overflow-hidden border">
                        <el-table-column prop="productName" label="Sản phẩm" width="300" />
                        <el-table-column prop="quantity" label="SL Xuất" width="100" align="center" />
                        <el-table-column prop="remark" label="Ghi chú" />
                        <el-table-column label="Gỡ bỏ" width="80" align="center">
                            <template #default="scope">
                                <el-button type="danger" link :icon="Delete" @click="form.details.splice(scope.$index, 1)" />
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-form>
            <template #footer>
                <div class="p-3 d-flex justify-content-end gap-3 border-top">
                    <el-button @click="dialogVisible = false" class="rounded-pill px-4">Hủy lệnh</el-button>
                    <el-button type="danger" :icon="Check" class="rounded-pill px-4 shadow-sm fw-bold" @click="handleCreate">XÁC NHẬN XUẤT KHO & KÝ TÊN</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.outbound-premium {
    font-family: 'Plus Jakarta Sans', sans-serif;
}

.fw-black { font-weight: 850; }
.ls-tight { letter-spacing: -1px; }
.ls-wide { letter-spacing: 1px; }
.tiny { font-size: 10px; }
.ultra-tiny { font-size: 9px; }

.glass-hero {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
}

.icon-box-outbound {
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.shadow-danger { box-shadow: 0 10px 20px rgba(239, 68, 68, 0.3); }

.vocation-card {
    transition: all 0.3s ease;
}

.vocation-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 15px 30px rgba(0,0,0,0.08) !important;
}

.bg-error-light { background: rgba(239, 68, 68, 0.05); }

.premium-dialog :deep(.el-dialog__title) {
    font-weight: 800;
}

.premium-form :deep(.el-form-item__label) {
    font-weight: 700;
    color: #475569;
    font-size: 12px;
}
</style>