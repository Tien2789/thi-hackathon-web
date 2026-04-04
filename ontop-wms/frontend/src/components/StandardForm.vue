<template>
  <div class="standard-form-container" ref="formContent">
    <div class="form-header">
      <div class="header-left">
        <div class="company-name">ONTOP WMS SOLUTION</div>
        <div class="company-address">123 Logistics Way, Ho Chi Minh City</div>
      </div>
      <div class="header-right text-right">
        <div class="form-model">Mẫu số: {{ modelNumber }}</div>
        <div class="form-circular">(Ban hành theo Thông tư số 200/2014/TT-BTC)</div>
      </div>
    </div>

    <div class="form-title text-center">
      <h2>{{ title.toUpperCase() }}</h2>
      <div class="form-date">Số: {{ code }} - Ngày {{ formattedDate }}</div>
    </div>

    <div class="form-info info-grid">
      <div v-for="(val, key) in headInfo" :key="key" class="info-item">
        <span class="info-label">{{ key }}: </span>
        <span class="info-value">{{ val }}</span>
      </div>
    </div>

    <div class="form-body">
      <el-table :data="items" border style="width: 100%" class="standard-table">
        <el-table-column type="index" label="STT" width="50" align="center" />
        <el-table-column prop="productName" label="Tên, nhãn hiệu, quy cách, phẩm chất vật tư" min-width="200" />
        <el-table-column prop="skuCode" label="Mã số" width="100" />
        <el-table-column prop="unit" label="Đơn vị tính" width="100" align="center" />
        
        <!-- Quantity columns for Inbound (01-VT) -->
        <template v-if="type === 'INBOUND'">
          <el-table-column label="Số lượng">
            <el-table-column prop="documentQty" label="Theo chứng từ" width="100" align="right" />
            <el-table-column prop="actualQty" label="Thực nhập" width="100" align="right" />
          </el-table-column>
        </template>

        <!-- Quantity columns for Outbound (02-VT) -->
        <template v-if="type === 'OUTBOUND'">
          <el-table-column label="Số lượng">
            <el-table-column prop="requestedQty" label="Yêu cầu" width="100" align="right" />
            <el-table-column prop="actualQty" label="Thực xuất" width="100" align="right" />
          </el-table-column>
        </template>

        <el-table-column prop="unitPrice" label="Đơn giá" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.unitPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="Thành tiền" width="150" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.unitPrice * scope.row.actualQty) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="form-footer">
      <div v-for="sig in signatures" :key="sig.signerRole" class="signature-box text-center">
        <div class="signature-role">{{ getRoleTitle(sig.signerRole) }}</div>
        <div class="signature-note">(Ký, họ tên)</div>
        <div v-if="sig.status === 'SIGNED'" class="signature-stamp">
          <el-icon color="#67C23A" size="40"><CircleCheckFilled /></el-icon>
          <div class="stamp-signed">ĐÃ KÝ</div>
          <div class="stamp-date">{{ formatDate(sig.signedAt) }}</div>
          <div class="stamp-name">{{ sig.signerName }}</div>
        </div>
        <div v-else class="signature-placeholder"></div>
      </div>
    </div>
    
    <div class="form-actions no-print">
      <el-button type="primary" @click="printForm">Xuất PDF / In phiếu</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { CircleCheckFilled } from '@element-plus/icons-vue';

const props = defineProps({
  type: String, // INBOUND, OUTBOUND, STOCKCARD
  code: String,
  date: String,
  headInfo: Object,
  items: Array,
  signatures: Array
});

const modelNumber = computed(() => {
  if (props.type === 'INBOUND') return '01-VT';
  if (props.type === 'OUTBOUND') return '02-VT';
  return '06-VT';
});

const title = computed(() => {
  if (props.type === 'INBOUND') return 'Phiếu Nhập Kho';
  if (props.type === 'OUTBOUND') return 'Phiếu Xuất Kho';
  return 'Thẻ Kho';
});

const formattedDate = computed(() => {
  const d = new Date(props.date);
  return `${d.getDate()} tháng ${d.getMonth() + 1} năm ${d.getFullYear()}`;
});

const getRoleTitle = (role) => {
  const titles = {
    'PERSON': 'Người giao/nhận',
    'KEEPER': 'Thủ kho',
    'ACCOUNTANT': 'Kế toán trưởng',
    'DIRECTOR': 'Giám đốc'
  };
  return titles[role] || role;
};

const formatCurrency = (val) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
};

const formatDate = (val) => {
  if (!val) return '';
  const d = new Date(val);
  return d.toLocaleString('vi-VN');
};

const printForm = () => {
  window.print();
};
</script>

<style scoped>
.standard-form-container {
  padding: 30px;
  background: #fff;
  border: 1px solid #ddd;
  font-family: 'Times New Roman', Times, serif;
  color: #000;
  max-width: 1000px;
  margin: 0 auto;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.form-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.company-name { font-weight: bold; font-size: 1.2rem; }
.form-model { font-weight: bold; }
.form-circular { font-style: italic; font-size: 0.9rem; }

.form-title h2 { margin: 10px 0; font-weight: bold; }
.form-date { margin-bottom: 20px; font-style: italic; }

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}

.info-label { font-weight: bold; }

.form-body { margin-bottom: 30px; }

.standard-table :deep(th) {
  background-color: #f5f7fa !important;
  color: #000;
  font-weight: bold;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
  min-height: 150px;
}

.signature-box { flex: 1; }
.signature-role { font-weight: bold; margin-bottom: 5px; }
.signature-note { font-style: italic; font-size: 0.85rem; margin-bottom: 15px; }

.signature-stamp {
  border: 2px dashed #67C23A;
  padding: 10px;
  border-radius: 8px;
  background: #f0f9eb;
  position: relative;
}

.stamp-signed {
  color: #67C23A;
  font-weight: bold;
  letter-spacing: 2px;
  margin: 5px 0;
}
.stamp-date { font-size: 0.8rem; color: #666; }
.stamp-name { font-weight: bold; margin-top: 5px; }

@media print {
  .no-print { display: none; }
  .standard-form-container {
    box-shadow: none;
    border: none;
    padding: 0;
  }
}
</style>
