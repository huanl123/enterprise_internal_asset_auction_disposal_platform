<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="工号">
          <el-input v-model="queryForm.username" placeholder="请输入工号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="请选择角色" clearable>
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
            <el-option label="资产专员" value="ASSET_SPECIALIST" />
            <el-option label="财务专员" value="FINANCE_SPECIALIST" />
            <el-option label="普通员工" value="NORMAL_USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
      >
        <el-table-column prop="username" label="工号" width="120" />
        <el-table-column label="姓名" width="100">
          <template #default="{ row }">
            {{ row.name || row.realName || row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系方式" width="130" />
        <el-table-column label="所属部门" width="150">
          <template #default="{ row }">
            {{ row.departmentName || row.department || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === true ? 'success' : 'danger'">
              {{ row.status === true ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              text
              :type="row.status === true ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === true ? '禁用' : '启用' }}
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              @click="handleResetPassword(row)"
            >
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
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
        <el-form-item label="工号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系方式" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="资产专员" value="ASSET_SPECIALIST" />
            <el-option label="财务专员" value="FINANCE_SPECIALIST" />
            <el-option label="普通员工" value="NORMAL_USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="请选择部门" style="width: 100%">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
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
import { useUserStore } from '@/stores/user'
import { getDepartments, getUsers, createUser, updateUser, toggleUserStatus, resetPassword } from '@/api'

const userStore = useUserStore()

const loading = ref(false)
const userList = ref([])
const departments = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const isEdit = ref(false)
const editingId = ref(null)

const PAGE_SIZE_KEY = 'user_page_size'
const getUserKey = () => userStore.user?.id || userStore.user?.username || userStore.user?.account || 'guest'
const getPageSizeKey = () => `${PAGE_SIZE_KEY}_${getUserKey()}`
const getSavedPageSize = () => Number(localStorage.getItem(getPageSizeKey())) || 10

const queryForm = reactive({
  username: '',
  name: '',
  role: '',
  status: '',
  page: 1,
  pageSize: getSavedPageSize()
})

const form = reactive({
  username: '',
  password: '',
  name: '',
  phone: '',
  role: '',
  departmentId: ''
})

const rules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

const normalizeRole = (role) => {
  if (!role) return ''
  const roleText = String(role)
  if (/^[\u4e00-\u9fa5]+$/.test(roleText)) return roleText
  return roleText.replace(/^ROLE_/i, '').trim().toLowerCase()
}

const getRoleType = (role) => {
  const normalized = normalizeRole(role)
  const roleMap = {
    'admin': 'danger',
    'system_admin': 'danger',
    'asset_specialist': 'warning',
    'finance_specialist': 'success',
    'employee': 'info',
    'normal_user': 'info',
    'user': 'info'
  }
  if (/^[\u4e00-\u9fa5]+$/.test(normalized)) {
    return normalized === '系统管理员'
      ? 'danger'
      : normalized === '资产专员'
        ? 'warning'
        : normalized === '财务专员'
          ? 'success'
          : 'info'
  }
  return roleMap[normalized] || 'info'
}

const getRoleText = (role) => {
  const normalized = normalizeRole(role)
  const roleMap = {
    'admin': '系统管理员',
    'system_admin': '系统管理员',
    'asset_specialist': '资产专员',
    'finance_specialist': '财务专员',
    'employee': '普通员工',
    'normal_user': '普通员工',
    'user': '普通员工'
  }
  if (/^[\u4e00-\u9fa5]+$/.test(normalized)) return normalized
  return roleMap[normalized] || '未知'
}

const toRoleCode = (role) => {
  const normalized = normalizeRole(role)
  const roleMap = {
    'admin': 'SYSTEM_ADMIN',
    'system_admin': 'SYSTEM_ADMIN',
    'asset_specialist': 'ASSET_SPECIALIST',
    'finance_specialist': 'FINANCE_SPECIALIST',
    'employee': 'NORMAL_USER',
    'normal_user': 'NORMAL_USER',
    'user': 'NORMAL_USER',
    '系统管理员': 'SYSTEM_ADMIN',
    '资产专员': 'ASSET_SPECIALIST',
    '财务专员': 'FINANCE_SPECIALIST',
    '普通员工': 'NORMAL_USER'
  }
  return roleMap[normalized] || role
}

const loadDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data
  } catch (error) {
    console.error('加载部门失败:', error)
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    const status = queryForm.status === ''
      ? undefined
      : queryForm.status === 'active'
    const res = await getUsers({
      username: queryForm.username || undefined,
      name: queryForm.name || undefined,
      role: queryForm.role || undefined,
      status,
      page: queryForm.page,
      size: queryForm.pageSize
    })
    userList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryForm.page = 1
  loadUsers()
}

const handleReset = () => {
  Object.assign(queryForm, {
    username: '',
    name: '',
    role: '',
    status: '',
    page: 1,
    pageSize: getSavedPageSize()
  })
  loadUsers()
}

const handleSizeChange = (pageSize) => {
  queryForm.pageSize = pageSize
  queryForm.page = 1
  localStorage.setItem(getPageSizeKey(), String(pageSize))
  loadUsers()
}

const handleCurrentChange = (page) => {
  queryForm.page = page
  loadUsers()
}

const handleCreate = () => {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  formRef.value?.resetFields()
  Object.assign(form, {
    username: '',
    password: '',
    name: '',
    phone: '',
    role: '',
    departmentId: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    name: row.name,
    phone: row.phone,
    role: toRoleCode(row.role),
    departmentId: row.departmentId
  })
  dialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.status === true ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}用户"${row.name}"吗？${action === '禁用' ? '禁用后该用户将无法登录。' : ''}`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await toggleUserStatus(row.id, !row.status)
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户"${row.name}"的密码吗？重置后密码为：123456`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await resetPassword(row.id, '123456')
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateUser(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  loadDepartments()
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
