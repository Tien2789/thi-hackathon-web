<script setup>
import { ref, onMounted, computed, markRaw } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Van, Plus, Delete, Check, Search, 
  Box, UserFilled, Connection, Timer, 
  MagicStick, WarningFilled, DocumentOutlined 
} from '@element-plus/icons-vue'

const vouchers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const products = ref([])
const categories = ref([])
const units = ref([])

// Form state
const form = ref({
    voucherCode: '',
    receiveDate: new Date().toISOString().split('T')[0],
    deliverer: '',
    warehouseName: '',
    details: []
})

// New detail item state
const newDetail = ref({
    productId: null,
    productName: '',
    skuCode: '',
    barcode: '',
    categoryId: null,
    categoryName: '',
    unitId: null,
    unitName: '',
    quantity: 1,
    unitPrice: 0,
    remark: ''
})

const fetchVouchers = async () => {
    try {
        loading.value = true
        const response = await api.get('/inventory/inbound')
        vouchers.value = response.data
    } catch (error) {
        ElMessage.error('Không thể tải danh sách phiếu nhập')
    } finally {
        loading.value = false
    }
}

const fetchMetadata = async () => {
    try {
        const [prodRes, catRes, unitRes] = await Promise.all([
            api.get('/products'), 
            api.get('/categories'), 
            api.get('/units')
        ])
        products.value = prodRes.data
        categories.value = catRes.data
        units.value = unitRes.data
    } catch (error) {
        console.error('Lỗi tải metadata:', error)
    }
}

const generateSku = () => {
    if (!newDetail.value.productName) {
        ElMessage.warning('Vui lòng nhập tên sản phẩm trước khi tạo SKU')
        return
    }
    const slug = newDetail.value.productName.toLowerCase().replace(/[^a-z0-9]/g, '-').slice(0, 10)
    const timestamp = Date.now().toString().slice(-4)
    newDetail.value.skuCode = `SKU-${slug.toUpperCase()}-${timestamp}`
    ElMessage.success('Đã tạo mã SKU thông minh')
}

const addDetail = () => {
    if ((!newDetail.value.productId && !newDetail.value.productName) || newDetail.value.quantity <= 0) {
        ElMessage.warning('Vui lòng nhập đầy đủ thông tin sản phẩm')
        return
    }
    form.value.details.push({ ...newDetail.value })
    // Reset form
    newDetail.value = {
        productId: null, productName: '', skuCode: '', barcode: '',
        categoryId: null, categoryName: '', unitId: null, unitName: '',
        quantity: 1, unitPrice: 0, remark: ''
    }
}

const handleCreate = async () => {
    if (form.value.details.length === 0) {
        ElMessage.warning('Vui lòng thêm ít nhất một sản phẩm vào phiếu')
        return
    }
    try {
        await api.post('/inventory/inbound', form.value)
        ElMessage.success('Đã lập phiếu nhập kho thành công')
        dialogVisible.value = false
        fetchVouchers()
    } catch (error) {
        ElMessage.error('Lỗi khi lưu phiếu nhập')
    }
}

onMounted(() => {
    fetchVouchers()
    fetchMetadata()
})
</script>

<template>
    <div class="inbound-premium p-4">
        <!-- Hero Header -->
        <div class="glass-hero d-flex justify-content-between align-items-center mb-5 p-4 rounded-4 shadow-sm border border-white">
            <div class="d-flex align-items-center gap-4">
                <div class="icon-box-inbound bg-success text-white rounded-4 p-3 shadow-success">
                    <el-icon :size="32"><Van /></el-icon>
                </div>
                <div>
                    <h3 class="fw-black text-dark mb-1 ls-tight">Nhập Kho Hàng Hóa</h3>
                    <p class="text-muted small mb-0 fw-medium">Quản lý luồng hàng đến và cập nhật tồn kho tức thời</p>
                </div>
            </div>
            <div class="d-flex gap-3">
                <div class="search-pill d-flex align-items-center px-3 rounded-pill bg-white border">
                    <el-icon class="text-muted"><Search /></el-icon>
                    <input type="text" placeholder="Tìm số phiếu..." class="form-control-plaintext ps-2 small">
                </div>
                <el-button type="success" size="large" class="rounded-pill px-4 shadow-sm fw-bold" :icon="Plus" @click="dialogVisible = true">Lập phiếu nhập mới</el-button>
            </div>
        </div>

        <!-- Recent Inbound Vouchers -->
        <div class="row g-4" v-loading="loading">
            <div v-for="v in vouchers" :key="v.id" class="col-12 col-md-6 col-xl-4">
                <div class="vocation-card bg-white rounded-4 shadow-sm border-0 transition-all">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <span class="badge rounded-pill bg-success bg-opacity-10 text-success border-0 px-3 py-1 tiny fw-bold text-uppercase ls-wide mb-2">Verified</span>
                                <h5 class="fw-bold text-dark mb-0 ls-tight">{{ v.voucherCode }}</h5>
                            </div>
                            <div class="text-end">
                                <p class="text-muted tiny fw-bold mb-0">{{ v.receiveDate }}</p>
                                <p class="text-muted ultra-tiny">{{ v.warehouseName || 'General Stock' }}</p>
                            </div>
                        </div>

                        <div class="deliverer-info bg-light rounded-3 p-3 mb-3 d-flex align-items-center gap-3">
                            <el-avatar :size="36" icon="User" class="bg-white text-success shadow-sm" />
                            <div>
                                <p class="text-muted ultra-tiny fw-bold mb-0">người giao hàng</p>
                                <p class="text-dark fw-bold small mb-0">{{ v.deliverer }}</p>
                            </div>
                        </div>

                        <div class="items-summary mb-3">
                            <p class="text-muted tiny fw-bold mb-2">CHI TIẾT HÀNG HÓA</p>
                            <div class="d-flex flex-wrap gap-2">
                                <el-tag v-for="item in v.details?.slice(0, 3)" :key="item.id" size="small" effect="light" class="rounded-pill border-0 px-3">
                                    {{ item.productName }} x{{ item.quantity }}
                                </el-tag>
                                <el-tag v-if="v.details?.length > 3" size="small" type="info" class="rounded-pill border-0">
                                    +{{ v.details.length - 3 }} món khác
                                </el-tag>
                            </div>
                        </div>

                        <div class="card-footer-v2 d-flex justify-content-between align-items-center pt-3 border-top">
                           <div class="d-flex align-items-center gap-2">
                               <el-icon class="text-success"><Timer /></el-icon>
                               <span class="text-muted ultra-tiny fw-bold">Duyệt lúc: {{ v.receiveDate }}</span>
                           </div>
                           <el-button type="primary" link icon="DocumentOutlined" size="small" class="fw-bold">XEM PHIẾU</el-button>
                        </div>
                    </div>
                </div>
            </div>
            
            <div v-if="vouchers.length === 0" class="col-12 text-center py-5">
                <el-empty description="Chưa có dữ liệu nhập kho" />
            </div>
        </div>

        <!-- Create Voucher Dialog -->
        <el-dialog v-model="dialogVisible" title="Lập Phiếu Nhập Kho Thông Minh" width="900px" align-center class="premium-dialog rounded-4">
            <el-form :model="form" label-position="top" class="premium-form p-3">
                <div class="row g-3">
                    <div class="col-md-4">
                        <el-form-item label="Mã phiếu nhập">
                            <el-input v-model="form.voucherCode" placeholder="Tự động" />
                        </el-form-item>
                    </div>
                    <div class="col-md-4">
                        <el-form-item label="Ngày nhập">
                            <el-date-picker v-model="form.receiveDate" type="date" value-format="YYYY-MM-DD" class="w-100" />
                        </el-form-item>
                    </div>
                    <div class="col-md-4">
                        <el-form-item label="Người giao hàng">
                            <el-input v-model="form.deliverer" placeholder="Họ và tên" />
                        </el-form-item>
                    </div>
                </div>

                <div class="detail-builder bg-light rounded-4 p-4 mt-4 mb-4 border border-white">
                    <h6 class="fw-bold text-dark mb-3"><el-icon class="me-2 text-primary"><Box /></el-icon>Khai báo hàng hóa nhập mới</h6>
                    <div class="row g-3">
                        <div class="col-md-4">
                            <el-form-item label="Chọn sản phẩm hiện có (hoặc nhập tên mới)">
                                <el-select v-model="newDetail.productName" filterable allow-create placeholder="Chọn hoặc nhập tên" class="w-100">
                                    <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.productName" />
                                </el-select>
                            </el-form-item>
                        </div>
                        <div class="col-md-4">
                            <el-form-item label="Mã SKU">
                                <el-input v-model="newDetail.skuCode" placeholder="Auto-generate" class="skubox">
                                    <template #append>
                                        <el-button @click="generateSku"><el-icon><MagicStick /></el-icon></el-button>
                                    </template>
                                </el-input>
                            </el-form-item>
                        </div>
                        <div class="col-md-2">
                            <el-form-item label="Số lượng">
                                <el-input-number v-model="newDetail.quantity" :min="1" class="w-100" />
                            </el-form-item>
                        </div>
                        <div class="col-md-2 d-flex align-items-end p-2">
                             <el-button type="primary" class="w-100 rounded-pill shadow-sm" :icon="Plus" @click="addDetail">Thêm</el-button>
                        </div>
                        
                        <div class="col-md-3">
                             <el-form-item label="Danh mục">
                                <el-select v-model="newDetail.categoryName" filterable allow-create placeholder="Chọn/Thêm loại" class="w-100">
                                    <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name" />
                                </el-select>
                             </el-form-item>
                        </div>
                        <div class="col-md-3">
                             <el-form-item label="Đơn vị">
                                <el-select v-model="newDetail.unitName" filterable allow-create placeholder="Chọn/Thêm ĐVT" class="w-100">
                                    <el-option v-for="u in units" :key="u.id" :label="u.name" :value="u.name" />
                                </el-select>
                             </el-form-item>
                        </div>
                        <div class="col-md-6">
                             <el-form-item label="Ghi chú (Tùy chọn)">
                                <el-input v-model="newDetail.remark" placeholder="Lý do nhập, tình trạng..." />
                             </el-form-item>
                        </div>
                    </div>
                </div>

                <!-- Preview Table -->
                <div v-if="form.details.length > 0" class="preview-list p-2">
                    <p class="text-muted tiny fw-bold mb-2">DANH SÁCH CHỜ NHẬP KHO ({{ form.details.length }})</p>
                    <el-table :data="form.details" stripe class="premium-table rounded-3 overflow-hidden border">
                        <el-table-column prop="productName" label="Sản phẩm" width="200" />
                        <el-table-column prop="skuCode" label="SKU" width="150" />
                        <el-table-column prop="quantity" label="SL" width="80" align="center" />
                        <el-table-column prop="categoryName" label="Danh mục" />
                        <el-table-column label="Action" width="80" align="center">
                            <template #default="scope">
                                <el-button type="danger" link :icon="Delete" @click="form.details.splice(scope.$index, 1)" />
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-form>
            <template #footer>
                <div class="p-3 d-flex justify-content-end gap-3 border-top">
                    <el-button @click="dialogVisible = false" class="rounded-pill px-4">Đóng lại</el-button>
                    <el-button type="success" :icon="Check" class="rounded-pill px-4 shadow-sm fw-bold" @click="handleCreate">XÁC NHẬN NHẬP KHO & KÝ TÊN</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.inbound-premium {
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

.icon-box-inbound {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.shadow-success { box-shadow: 0 10px 20px rgba(16, 185, 129, 0.3); }

.vocation-card {
    transition: all 0.3s ease;
}

.vocation-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 15px 30px rgba(0,0,0,0.08) !important;
}

.search-pill {
    width: 280px;
}

.detail-builder {
    background: rgba(248, 250, 252, 1);
}

.premium-dialog :deep(.el-dialog__title) {
    font-weight: 800;
}

.premium-form :deep(.el-form-item__label) {
    font-weight: 700;
    color: #475569;
    font-size: 12px;
}

.skubox :deep(.el-input-group__append) {
    background: #4f46e5;
    color: #fff;
    padding: 0 15px;
    cursor: pointer;
}
</style>
