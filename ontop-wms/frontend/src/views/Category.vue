<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Search, Edit, Delete, Folder, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const categories = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEditMode = ref(false)
const searchQuery = ref('')
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: 'Vui lòng nhập tên danh mục', trigger: 'blur' },
    { min: 2, max: 50, message: 'Độ dài từ 2 đến 50 ký tự', trigger: 'blur' }
  ]
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const response = await api.get('/categories')
    categories.value = response.data
  } catch (error) {
    console.error('Error fetching categories:', error)
    ElMessage.error('Không thể tải danh sách danh mục')
  } finally {
    loading.value = false
  }
}

const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value
  const query = searchQuery.value.toLowerCase()
  return categories.value.filter(cat => 
    cat.name.toLowerCase().includes(query) || 
    (cat.description && cat.description.toLowerCase().includes(query))
  )
})

const openDialog = (category = null) => {
  if (category) {
    isEditMode.value = true
    Object.assign(form, category)
  } else {
    isEditMode.value = false
    form.id = null
    form.name = ''
    form.description = ''
  }
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEditMode.value) {
          await api.put(`/categories/${form.id}`, form)
          ElMessage.success('Cập nhật thành công')
        } else {
          await api.post('/categories', form)
          ElMessage.success('Thêm mới thành công')
        }
        showDialog.value = false
        fetchCategories()
      } catch (error) {
        ElMessage.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message))
      }
    }
  })
}

const handleDelete = (category) => {
  ElMessageBox.confirm(
    `Bạn có chắc chắn muốn xóa danh mục "${category.name}"?`,
    'Cảnh báo',
    { confirmButtonText: 'Xóa', cancelButtonText: 'Hủy', type: 'warning' }
  ).then(async () => {
    try {
      await api.delete(`/categories/${category.id}`)
      ElMessage.success('Đã xóa danh mục')
      fetchCategories()
    } catch (error) {
      ElMessage.error('Không thể xóa danh mục này')
    }
  })
}

onMounted(fetchCategories)
</script>

<template>
  <div class="category-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-bold mb-1">Phân loại sản phẩm</h4>
        <p class="text-muted small mb-0">Quản lý các nhóm ngành hàng trong hệ thống</p>
      </div>
      <div class="d-flex gap-2">
        <el-input v-model="searchQuery" placeholder="Tìm kiếm danh mục..." :prefix-icon="Search" style="width: 250px" />
        <el-button type="primary" :icon="Plus" @click="openDialog()">Thêm mới</el-button>
      </div>
    </div>

    <div class="card border-0 shadow-sm" v-loading="loading">
      <el-table :data="filteredCategories" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="Tên danh mục" width="250">
          <template #default="{ row }">
            <div class="d-flex align-items-center gap-2">
              <el-icon class="text-primary"><Folder /></el-icon>
              <span class="fw-bold">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="Mô tả" />
        <el-table-column label="Thao tác" width="150" align="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" circle plain type="primary" @click="openDialog(row)" />
            <el-button size="small" :icon="Delete" circle plain type="danger" @click="handleDelete(row)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" :title="isEditMode ? 'Cập nhật danh mục' : 'Thêm danh mục mới'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="Tên danh mục" prop="name">
          <el-input v-model="form.name" placeholder="Ví dụ: Điện tử" />
        </el-form-item>
        <el-form-item label="Mô tả" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="Mô tả chi tiết về danh mục..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDialog = false">Hủy</el-button>
          <el-button type="primary" @click="handleSubmit">Lưu lại</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.card {
  border-radius: 8px;
  overflow: hidden;
}
</style>