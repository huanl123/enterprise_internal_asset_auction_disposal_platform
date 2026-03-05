<template>
  <div class="department-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增部门
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="departmentList"
        stripe
        border
      >
        <el-table-column prop="name" label="部门名称" width="200" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="leaderPhone" label="联系方式" width="130" />
        <el-table-column prop="description" label="部门描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="员工" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.employeeNames && row.employeeNames.length ? row.employeeNames.join('、') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="employeeCount" label="员工数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系方式" prop="leaderPhone">
          <el-input v-model="form.leaderPhone" placeholder="请输入负责人联系方式" />
        </el-form-item>
        <el-form-item label="部门描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '@/api'

const loading = ref(false)
const departmentList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const isEdit = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '',
  leader: '',
  leaderPhone: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  leader: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  leaderPhone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const loadDepartments = async () => {
  loading.value = true
  try {
    const res = await getDepartments()
    departmentList.value = res.data
  } catch (error) {
    console.error('加载部门列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = '新增部门'
  isEdit.value = false
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '',
    leader: '',
    leaderPhone: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑部门'
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    leader: row.leader,
    leaderPhone: row.leaderPhone,
    description: row.description || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${row.name}"吗？删除后该部门的员工将不受影响。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    loadDepartments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateDepartment({
        id: editingId.value,
        ...form
      })
      ElMessage.success('更新成功')
    } else {
      await createDepartment(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadDepartments()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  loadDepartments()
})
</script>

<style scoped>
.department-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
