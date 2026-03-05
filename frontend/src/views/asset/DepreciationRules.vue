<template>
  <div class="depreciation-rules">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>折旧规则管理</span>
          <el-button 
            v-if="userStore.hasAnyRole('资产专员', '系统管理员')" 
            type="primary" 
            @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增规则
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="rulesList"
        stripe
        border
      >
        <el-table-column prop="name" label="规则名称" width="200" />
        <el-table-column prop="usefulLife" label="预计使用年限" width="150" align="center">
          <template #default="{ row }">
            {{ row.usefulLife }} 年
          </template>
        </el-table-column>
        <el-table-column prop="salvageRate" label="残值率" width="120" align="center">
          <template #default="{ row }">
            {{ row.salvageRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="assetCount" label="关联资产数" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'">
              {{ row.status ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status"
              text
              type="warning"
              size="small"
              @click="handleDisable(row)"
            >
              停用
            </el-button>
            <el-button
              v-else
              text
              type="success"
              size="small"
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button
              v-if="row.status && row.assetCount === 0"
              text
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.assetCount === 0"
              text
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <span v-else-if="row.assetCount > 0" style="color: #999; font-size: 12px">
              已关联资产
            </span>
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
        label-width="120px"
      >
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="使用年限" prop="usefulLife">
          <el-input-number
            v-model="form.usefulLife"
            :min="1"
            :max="30"
            controls-position="right"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999">年</span>
        </el-form-item>
        <el-form-item label="残值率" prop="salvageRate">
          <el-input-number
            v-model="form.salvageRate"
            :min="0"
            :max="100"
            :precision="1"
            :step="0.5"
            controls-position="right"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999">%</span>
        </el-form-item>
        <el-form-item label="说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入说明"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getDepreciationRules,
  createDepreciationRule,
  updateDepreciationRule,
  disableDepreciationRule,
  enableDepreciationRule,
  deleteDepreciationRule
} from '@/api'

const userStore = useUserStore()

const loading = ref(false)
const rulesList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const isEdit = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '',
  usefulLife: 5,
  salvageRate: 5,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  usefulLife: [
    { required: true, message: '请输入使用年限', trigger: 'blur' }
  ],
  salvageRate: [
    { required: true, message: '请输入残值率', trigger: 'blur' }
  ]
}

const loadRules = async () => {
  loading.value = true
  try {
    const res = await getDepreciationRules()
    // 修复：API返回分页数据，需要取content部分
    if (res.data && typeof res.data === 'object') {
      rulesList.value = res.data.content || res.data.list || res.data.data || []
    } else {
      rulesList.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    console.error('加载折旧规则失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = '新增折旧规则'
  isEdit.value = false
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '',
    usefulLife: 5,
    salvageRate: 5,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑折旧规则'
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    usefulLife: row.usefulLife,
    salvageRate: row.salvageRate,
    description: row.description || ''
  })
  dialogVisible.value = true
}

const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要停用折旧规则"${row.name}"吗？停用后仍可用于已有资产，但不能用于新资产。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await disableDepreciationRule(row.id)
    ElMessage.success('停用成功')
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用失败:', error)
    }
  }
}

const handleEnable = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要启用折旧规则"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await enableDepreciationRule(row.id)
    ElMessage.success('启用成功')
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('启用失败:', error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除折旧规则"${row.name}"吗？此操作不可恢复。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteDepreciationRule(row.id)
    ElMessage.success('删除成功')
    loadRules()
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
      await updateDepreciationRule({
        id: editingId.value,
        ...form
      })
      ElMessage.success('更新成功')
    } else {
      await createDepreciationRule(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.depreciation-rules {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
