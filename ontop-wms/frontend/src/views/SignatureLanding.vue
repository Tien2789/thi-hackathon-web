<script setup>
import { ref, onMounted, markRaw } from 'vue';
import { useRoute } from 'vue-router';
import { 
  InfoFilled, Stamp, SuccessFilled, Lock, 
  Checked, Management, DocumentChecked, 
  WarningFilled, Connection
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const route = useRoute();
const token = route.params.token;

const signature = ref(null);
const loading = ref(true);
const signing = ref(false);
const error = ref(null);
const signerName = ref('');

const fetchSignature = async () => {
  try {
    const res = await api.get(`/signatures/verify?token=${token}`);
    signature.value = res.data;
    signerName.value = signature.value.signerName || '';
  } catch (err) {
    error.value = "Link xác thực signature không hợp lệ hoặc đã hết hạn truy cập.";
  } finally {
    loading.value = false;
  }
};

const handleSign = async () => {
  if (!signerName.value) {
    ElMessage.warning('Vui lòng xác nhận danh tính bằng họ tên đầy đủ');
    return;
  }

  signing.value = true;
  try {
    const res = await api.post('/signatures/sign', {
      token: token,
      signerName: signerName.value
    });
    signature.value = res.data;
    ElMessage.success('Xác nhận ký số thành công. Chứng từ đã được khóa.');
  } catch (err) {
    ElMessage.error('Có lỗi trong quá trình đóng dấu điện tử');
  } finally {
    signing.value = false;
  }
};

const getRoleTitle = (role) => {
  const titles = {
    'PERSON': 'Đại diện Giao/Nhận',
    'KEEPER': 'Thủ kho trực ban',
    'ACCOUNTANT': 'Kế toán trưởng',
    'DIRECTOR': 'Ban Giám đốc'
  };
  return titles[role] || role;
};

const formatDate = (val) => {
  if (!val) return '';
  return new Date(val).toLocaleString('vi-VN');
};

onMounted(() => {
  fetchSignature();
});
</script>

<template>
  <div class="signature-premium-landing d-flex vh-100 overflow-hidden position-relative">
    
    <!-- Deep Mesh Background (Formal Version) -->
    <div class="mesh-bg position-absolute top-0 start-0 w-100 h-100 z-n1">
        <div class="mesh-blob blob-1"></div>
        <div class="mesh-blob blob-2"></div>
    </div>

    <!-- Main Content Container -->
    <div class="container d-flex align-items-center justify-content-center h-100 px-4">
        
        <div v-if="loading" class="text-center">
            <el-skeleton :rows="5" animated class="glass-skeleton p-5 rounded-5" />
        </div>

        <div v-else-if="error" class="error-container text-center p-5 rounded-5 glass-morphism border border-danger border-opacity-20 shadow-2xl">
            <el-icon :size="64" color="#ef4444" class="mb-4"><WarningFilled /></el-icon>
            <h3 class="fw-black text-dark mb-2">Truy Cập Bị Từ Chối</h3>
            <p class="text-muted mb-4 fs-6 fw-medium">{{ error }}</p>
            <el-button type="primary" size="large" class="rounded-pill px-5 fw-bold shadow-sm" @click="$router.push('/')">Về Trang Chủ</el-button>
        </div>

        <div v-else class="signature-main-card w-100" style="max-width: 600px;">
            
            <div class="glass-morphism rounded-5 p-5 shadow-2xl border border-white border-opacity-30 position-relative overflow-hidden">
                
                <!-- Verification Badges -->
                <div class="top-badges d-flex justify-content-between align-items-center mb-5">
                    <div class="trust-pill bg-success bg-opacity-10 text-success rounded-pill px-3 py-1 ultra-tiny fw-extrabold tracking-widest d-flex align-items-center gap-2">
                        <el-icon><Checked /></el-icon> ENCRYPTED
                    </div>
                    <div class="system-tag ultra-tiny fw-bold text-muted text-uppercase tracking-widest">ontop-wms-v2</div>
                </div>

                <!-- Header Content -->
                <div class="text-center mb-5">
                    <div class="legal-icon mb-4 d-inline-flex bg-primary bg-opacity-5 p-4 rounded-circle border border-primary border-opacity-10">
                        <el-icon :size="48" color="#4f46e5"><DocumentChecked /></el-icon>
                    </div>
                    <h2 class="fw-black text-dark mb-1 ls-tight">Xác Nhận Ký Số</h2>
                    <p class="text-muted small fw-medium">Phê duyệt chứng từ lưu kho kỹ thuật số vĩnh viễn</p>
                </div>

                <!-- Document Context -->
                <div class="doc-context-card p-4 rounded-4 bg-light bg-opacity-50 border border-white mb-5 transition-all">
                    <div class="row text-center">
                        <div class="col-6 border-end">
                            <p class="ultra-tiny text-muted fw-bold mb-1 uppercase tracking-wider">PHÂN LOẠI</p>
                            <p class="mb-0 fw-black text-dark small">{{ signature.transactionType }}</p>
                        </div>
                        <div class="col-6">
                            <p class="ultra-tiny text-muted fw-bold mb-1 uppercase tracking-wider">ID CHỨNG TỪ</p>
                            <p class="mb-0 fw-black text-primary small">#{{ signature.transactionId }}</p>
                        </div>
                    </div>
                    <div class="text-center mt-3 pt-3 border-top border-white">
                        <span class="badge rounded-pill bg-primary bg-opacity-5 text-primary small fw-bold px-3 py-1">Vai trò: {{ getRoleTitle(signature.signerRole) }}</span>
                    </div>
                </div>

                <!-- Pending Logic -->
                <div v-if="signature.status === 'PENDING'" class="signing-flow anim-up">
                    <div class="alert-info-premium p-4 rounded-4 bg-primary bg-opacity-5 border border-primary border-opacity-10 mb-5 text-start">
                        <p class="tiny text-primary fw-extrabold mb-1 d-flex align-items-center gap-2">
                            <el-icon><InfoFilled /></el-icon> ĐIỀU KHOẢN KÝ DUYỆT
                        </p>
                        <p class="ultra-tiny text-muted mb-0 lh-base fw-medium">Bằng việc xác nhận, bạn cam kết các số liệu trên là chính xác. Chữ ký này có giá trị xác nhận trách nhiệm trong hệ thống nội bộ ONTOP-WMS.</p>
                    </div>

                    <el-form label-position="top">
                        <el-form-item class="field-wrapper">
                            <label class="ultra-tiny fw-bold text-muted text-uppercase mb-2 tracking-wider ps-1">Họ tên người thực hiện ký</label>
                            <el-input v-model="signerName" placeholder="Nhập họ tên đầy đủ (Không dấu hoặc có dấu)" class="signature-input-pill" />
                        </el-form-item>
                        
                        <button class="sign-btn-premium w-100 rounded-pill py-3 fw-black tracking-widest text-uppercase shadow-sm border-0 mt-3" :disabled="signing" @click="handleSign">
                            <template v-if="!signing">
                                <el-icon class="me-2"><Stamp /></el-icon> CHẤP THUẬN & ĐÓNG DẤU
                            </template>
                            <template v-else><el-icon class="is-loading"><Connection /></el-icon> ĐANG MÃ HÓA...</template>
                        </button>
                    </el-form>
                </div>

                <!-- Success Logic -->
                <div v-else class="success-flow text-center anim-up">
                    <div class="success-icon-box mb-4 d-inline-flex bg-success bg-opacity-5 p-4 rounded-circle border border-success border-opacity-10">
                        <el-icon :size="56" color="#10b981"><SuccessFilled /></el-icon>
                    </div>
                    <h3 class="fw-black text-dark mb-1">Ký Duyệt Hoàn Tất</h3>
                    <p class="text-muted ultra-tiny fw-bold mb-5 uppercase">{{ formatDate(signature.signedAt) }}</p>

                    <div class="audit-info p-4 rounded-4 bg-light border border-white text-start shadow-inner">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <p class="ultra-tiny text-muted fw-extrabold mb-0">CHI TIẾT CHỮ KÝ ĐIỆN TỬ</p>
                            <el-icon class="text-muted"><Lock /></el-icon>
                        </div>
                        <p class="small text-dark mb-1"><strong>Người ký:</strong> {{ signature.signerName }}</p>
                        <p class="small text-dark mb-1"><strong>IP Address:</strong> {{ signature.ipAddress }}</p>
                        <p class="ultra-tiny text-muted fw-bold">TOKEN: {{ signature.signatureToken }}</p>
                    </div>
                </div>

                <div class="mt-5 pt-4 text-center border-top border-light opacity-50">
                    <p class="ultra-tiny text-muted fw-bold">SECURE CHANNEL PROTECTED • TLS 1.3</p>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script>
export default { name: 'SignatureLanding' }
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');

.signature-premium-landing {
    font-family: 'Plus Jakarta Sans', sans-serif;
    background-color: #0f172a;
}

.fw-black { font-weight: 850; }
.ls-tight { letter-spacing: -1px; }
.tiny { font-size: 10px; }
.ultra-tiny { font-size: 9px; }
.uppercase { text-transform: uppercase; }
.tracking-wider { letter-spacing: 1px; }
.tracking-widest { letter-spacing: 2px; }

/* Formal Mesh Background */
.mesh-bg { filter: blur(100px); overflow: hidden; }

.mesh-blob {
    position: absolute; width: 800px; height: 800px; border-radius: 50%;
    animation: slowMove 30s infinite alternate;
}

.blob-1 { background: rgba(79, 70, 229, 0.2); top: -20%; left: -20%; }
.blob-2 { background: rgba(16, 185, 129, 0.1); bottom: -20%; right: -20%; animation-delay: -10s; }

@keyframes slowMove {
    0% { transform: translate(0, 0) scale(1); }
    100% { transform: translate(100px, 150px) scale(1.05); }
}

/* Glass & UI */
.glass-morphism {
    background: rgba(255, 255, 255, 0.88);
    backdrop-filter: blur(30px);
}

.doc-context-card:hover { transform: translateY(-3px); background: #fff !important; }

.signature-input-pill :deep(.el-input__wrapper) {
    border-radius: 14px; padding: 10px 15px; background: #f8fafc;
    box-shadow: none; border: 1px solid #e2e8f0;
}

.sign-btn-premium {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    color: #fff; transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 10px 20px rgba(16, 185, 129, 0.2);
}

.sign-btn-premium:hover { transform: translateY(-2px); box-shadow: 0 15px 30px rgba(16, 185, 129, 0.3); }
.sign-btn-premium:active { transform: scale(0.98); }

.anim-up { animation: fadeInUp 0.8s ease-out both; }
@keyframes fadeInUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.shadow-inner { box-shadow: inset 0 2px 4px rgba(0,0,0,0.05); }
</style>
