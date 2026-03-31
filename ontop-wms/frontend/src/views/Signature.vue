<template>
  <div class="signature-container">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-state text-center py-5">
      <el-result icon="error" title="Lỗi" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">Về trang chủ</el-button>
        </template>
      </el-result>
    </div>

    <div v-else class="signature-content pt-4 pb-5 px-3">
      <div class="card shadow-sm border-0 mb-4">
        <div class="card-body text-center">
          <h4 class="fw-bold text-primary mb-2">Xác nhận ký duyệt chứng từ</h4>
          <p class="text-muted small mb-0">Hệ thống Quản lý Kho Thông minh OnTop WMS</p>
        </div>
      </div>

      <div class="document-preview-box mb-4">
        <!-- Reusing the Circular 200 form for preview -->
        <FormCircular200 
          :type="signature.transactionType.toLowerCase()" 
          :data="transactionData" 
          :signatures="allSignatures"
        />
      </div>

      <div v-if="!signature.signed" class="action-box sticky-bottom-mobile">
        <div class="card border-primary shadow-lg">
          <div class="card-body">
            <h5 class="fw-bold mb-3">Xác nhận của bạn</h5>
            <p class="small text-muted mb-4">
              Bằng việc nhấn "Đồng ý ký", bạn xác nhận rằng thông tin trên chứng từ là chính xác và đồng ý xác thực bằng chữ ký số của mình.
            </p>
            <el-button 
              type="primary" 
              size="large" 
              class="w-100 py-3 fw-bold" 
              :loading="signing"
              @click="handleSign"
            >
              <el-icon class="me-2"><Check /></el-icon> ĐỒNG Ý KÝ DUYỆT
            </el-button>
          </div>
        </div>
      </div>

      <div v-else class="success-box">
        <el-result icon="success" title="Ký duyệt thành công" sub-title="Bạn đã hoàn tất việc ký duyệt chứng từ này.">
          <template #extra>
            <p class="text-muted small">Thời gian: {{ new Date(signature.signedAt).toLocaleString('vi-VN') }}</p>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Check } from '@element-plus/icons-vue';
import api from '../api';
import FormCircular200 from '../components/FormCircular200.vue';

const route = useRoute();
const token = route.params.token;

const loading = ref(true);
const signing = ref(false);
const error = ref(null);
const signature = ref(null);
const allSignatures = ref([]);
const transactionData = reactive({});

const fetchSignatureInfo = async () => {
  try {
    const res = await api.get(`/signatures/${token}`);
    signature.value = res.data;
    
    // Fetch transaction details (Mocking for now based on type)
    // In a real app, you'd have a specific endpoint for preview
    const type = signature.value.transactionType === 'INBOUND' ? 'inbounds' : 'outbounds';
    const transRes = await api.get(`/inventory/${type}`);
    const currentTrans = transRes.data.find(t => t.id === signature.value.transactionId);
    
    if (currentTrans) {
      Object.assign(transactionData, currentTrans);
      // Mock details if missing
      if (!transactionData.details) {
        transactionData.details = [
          { productName: 'Sản phẩm mẫu', skuCode: 'SKU-001', unitName: 'Cái', quantity: 10, unitPrice: 50000 }
        ];
      }
    }

    // Fetch all signatures for this transaction to show in preview
    // Note: You'd need a backend endpoint for this too
    allSignatures.value = [signature.value]; 

    loading.value = false;
  } catch (err) {
    console.error(err);
    error.value = "Không thể tải thông tin chứng từ hoặc token đã hết hạn.";
    loading.value = false;
  }
};

const handleSign = async () => {
  signing.value = true;
  try {
    await api.post(`/signatures/${token}/confirm`);
    signature.value.signed = true;
    signature.value.signedAt = new Date().toISOString();
    ElMessage.success('Đã ký duyệt chứng từ thành công!');
  } catch (err) {
    ElMessage.error(err.response?.data?.message || 'Có lỗi xảy ra khi ký.');
  } finally {
    signing.value = false;
  }
};

onMounted(fetchSignatureInfo);
</script>

<style scoped>
.signature-container {
  max-width: 1000px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.document-preview-box {
  overflow-x: auto;
}

.sticky-bottom-mobile {
  position: sticky;
  bottom: 20px;
  z-index: 100;
}

@media (max-width: 768px) {
  .signature-content {
    padding-bottom: 120px !important;
  }
}
</style>
