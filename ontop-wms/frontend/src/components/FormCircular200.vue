<template>
  <div class="circular-200-form" :class="type">
    <div class="form-header text-center mb-4">
      <div class="row align-items-center">
        <div class="col-4 text-start">
          <div class="brand-logo mb-2">
            <h2 class="fw-bold text-primary m-0">ONTOP <span class="text-dark">WMS</span></h2>
          </div>
          <p class="small text-muted m-0">Đơn vị: ...........................</p>
          <p class="small text-muted m-0">Địa chỉ: .........................</p>
        </div>
        <div class="col-4">
          <h3 class="fw-bold m-0">{{ title }}</h3>
          <p class="m-0">Ngày .... tháng .... năm 20...</p>
          <p class="small m-0">Số: {{ code }}</p>
        </div>
        <div class="col-4 text-end">
          <div class="form-standard-tag border p-2 d-inline-block">
            <p class="fw-bold m-0">{{ standardCode }}</p>
            <p class="small m-0 text-muted">(Ban hành theo Thông tư 200/2014/TT-BTC)</p>
          </div>
        </div>
      </div>
    </div>

    <div class="form-body">
      <div class="row mb-3">
        <div class="col-6">
          <p v-if="type === 'inbound'">- Họ và tên người giao hàng: <strong>{{ data.delivererName }}</strong></p>
          <p v-if="type === 'outbound'">- Họ và tên người nhận hàng: <strong>{{ data.receiverName }}</strong></p>
          <p>- Theo chứng từ số: <strong>{{ data.documentNumber || data.issueCode }}</strong></p>
        </div>
        <div class="col-6">
          <p>- Nhập/Xuất tại kho: <strong>{{ data.warehouseName }}</strong></p>
          <p v-if="type === 'outbound'">- Lý do xuất kho: <strong>{{ data.reason }}</strong></p>
        </div>
      </div>

      <table class="table table-bordered border-dark custom-table">
        <thead class="table-light">
          <tr>
            <th rowspan="2" class="text-center align-middle">STT</th>
            <th rowspan="2" class="text-center align-middle">Tên, nhãn hiệu, quy cách, phẩm chất vật tư, dụng cụ, sản phẩm, hàng hóa</th>
            <th rowspan="2" class="text-center align-middle">Mã số</th>
            <th rowspan="2" class="text-center align-middle">Đơn vị tính</th>
            <th colspan="2" class="text-center">Số lượng</th>
            <th rowspan="2" class="text-center align-middle">Đơn giá</th>
            <th rowspan="2" class="text-center align-middle">Thành tiền</th>
          </tr>
          <tr>
            <th class="text-center">Theo chứng từ</th>
            <th class="text-center">Thực nhập/xuất</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in data.details" :key="index">
            <td class="text-center">{{ index + 1 }}</td>
            <td>{{ item.productName }}</td>
            <td class="text-center">{{ item.skuCode }}</td>
            <td class="text-center">{{ item.unitName }}</td>
            <td class="text-end">{{ item.quantity }}</td>
            <td class="text-end fw-bold">{{ item.actualQuantity || item.quantity }}</td>
            <td class="text-end">{{ formatCurrency(item.unitPrice) }}</td>
            <td class="text-end">{{ formatCurrency(item.totalPrice || (item.unitPrice * item.quantity)) }}</td>
          </tr>
          <tr v-if="!data.details || data.details.length === 0">
            <td colspan="8" class="text-center py-4 text-muted">Chưa có dữ liệu chi tiết</td>
          </tr>
        </tbody>
        <tfoot v-if="data.details && data.details.length > 0">
          <tr>
            <td colspan="7" class="text-end fw-bold">Tổng cộng:</td>
            <td class="text-end fw-bold text-danger">{{ formatCurrency(calculateTotal()) }}</td>
          </tr>
        </tfoot>
      </table>
    </div>

    <div class="form-footer mt-5">
      <div class="row text-center footer-signatures">
        <div class="col-3" v-for="role in signatureRoles" :key="role.key">
          <p class="fw-bold mb-1">{{ role.label }}</p>
          <p class="small text-muted mb-4">(Ký, họ tên)</p>
          <div class="signature-box" :class="{ 'is-signed': isSigned(role.key) }">
            <div v-if="isSigned(role.key)" class="signed-content animate__animated animate__zoomIn">
              <div class="tick-circle">
                <i class="el-icon-check"><el-icon><CheckIcon /></el-icon></i>
              </div>
              <p class="signer-info m-0 fw-bold text-success">ĐÃ KÝ DUYỆT</p>
              <p class="signature-time text-muted">{{ getSignTime(role.key) }}</p>
            </div>
            <div v-else class="unsigned-placeholder">.......................</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Check as CheckIcon } from '@element-plus/icons-vue';

const props = defineProps({
  type: {
    type: String,
    default: 'inbound' // 'inbound', 'outbound', 'storage'
  },
  data: {
    type: Object,
    required: true
  },
  signatures: {
    type: Array,
    default: () => []
  }
});

const title = computed(() => {
  if (props.type === 'inbound') return 'PHIẾU NHẬP KHO';
  if (props.type === 'outbound') return 'PHIẾU XUẤT KHO';
  return 'THẺ KHO';
});

const code = computed(() => {
  return props.data.receiptCode || props.data.issueCode || 'N/A';
});

const standardCode = computed(() => {
  if (props.type === 'inbound') return 'Mẫu số 01-VT';
  if (props.type === 'outbound') return 'Mẫu số 02-VT';
  return 'Mẫu số 06-VT';
});

const signatureRoles = computed(() => {
  const common = [
    { key: 'STOREKEEPER', label: 'Thủ kho' },
    { key: 'ACCOUNTANT', label: 'Kế toán trưởng' },
    { key: 'DIRECTOR', label: 'Giám đốc' }
  ];
  if (props.type === 'inbound') {
    return [{ key: 'DELIVERER', label: 'Người giao hàng' }, ...common];
  } else {
    return [{ key: 'RECEIVER', label: 'Người nhận hàng' }, ...common];
  }
});

const isSigned = (role) => {
  return props.signatures.some(s => s.role === role && s.signed);
};

const getSignTime = (role) => {
  const sig = props.signatures.find(s => s.role === role && s.signed);
  if (!sig) return '';
  return new Date(sig.signedAt).toLocaleString('vi-VN');
};

const formatCurrency = (value) => {
  if (!value) return '0 đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const calculateTotal = () => {
  if (!props.data.details) return 0;
  return props.data.details.reduce((sum, item) => sum + (item.totalPrice || (item.unitPrice * item.quantity)), 0);
};
</script>

<style scoped>
.circular-200-form {
  padding: 40px;
  background: white;
  color: #333;
  font-family: 'Times New Roman', Times, serif;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  border-radius: 8px;
  max-width: 1000px;
  margin: 0 auto;
}

.custom-table th, .custom-table td {
  border-color: #333 !important;
  vertical-align: middle;
}

.signature-box {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.signed-content {
  text-align: center;
}

.tick-circle {
  width: 40px;
  height: 40px;
  background: #67C23A;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 5px;
  font-size: 20px;
  box-shadow: 0 4px 10px rgba(103, 194, 58, 0.3);
}

.signature-time {
  font-size: 11px;
  font-style: italic;
}

.unsigned-placeholder {
  color: #ccc;
}

@media print {
  .circular-200-form {
    box-shadow: none;
    padding: 20px;
  }
}
</style>
