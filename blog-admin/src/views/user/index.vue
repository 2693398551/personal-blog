<template>
  <div class="index-container">
    <el-card class="index-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <h3>用户管理</h3>
            <span class="subtitle">共 {{ total }} 位用户</span>
          </div>
          <el-button type="primary" :loading="loading" @click="fetchData">刷新列表</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" border stripe>
        <el-table-column label="头像" width="80" align="center">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" shape="square" size="small" />
          </template>
        </el-table-column>

        <el-table-column prop="account" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />

        <el-table-column label="账号状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 99" type="danger">已封禁</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="warning">警告</el-tag>
            <el-tag v-else-if="scope.row.status === 0" type="success">正常</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="在线状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.online" type="success" effect="plain" round size="small">在线</el-tag>
            <el-tag v-else type="info" effect="plain" round size="small">离线</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="最后活跃" width="160">
          <template #default="scope">
            <div v-if="scope.row.lastLogin">
              <div class="time-text">{{ formatTime(scope.row.lastLogin) }}</div>
              <div class="time-ago">{{ formatRelativeTime(scope.row.lastLogin) }}</div>
            </div>
            <span v-else class="no-data">从未登录</span>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createDate) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑用户资料' : '用户详细信息'"
        width="560px"
        :close-on-click-modal="false"
        @close="handleDialogClose"
    >
      <el-form :model="editForm" label-width="80px">

        <!-- 头像 -->
        <el-form-item label="头像">
          <el-avatar :src="editForm.avatar" shape="square" :size="48" />
        </el-form-item>

        <!-- 账号（只读） -->
        <el-form-item label="账号">
          <el-input v-model="editForm.account" disabled />
        </el-form-item>

        <!-- 昵称 -->
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" :disabled="!isEditMode" />
        </el-form-item>

        <!-- 个人简介 -->
        <el-form-item label="个人简介">
          <el-input
              v-model="editForm.bio"
              type="textarea"
              :rows="2"
              :disabled="!isEditMode"
              placeholder="未填写"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>

        <!-- 生日 + 个人主页 两列 -->
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="生日">
              <el-date-picker
                  v-model="editForm.birthday"
                  type="date"
                  placeholder="未填写"
                  :disabled="!isEditMode"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主页">
              <el-input
                  v-model="editForm.website"
                  :disabled="!isEditMode"
                  placeholder="未填写"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 邮箱 + 手机号 两列 -->
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="editForm.email" placeholder="未绑定" :disabled="!isEditMode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="editForm.mobilePhoneNumber" placeholder="未绑定" :disabled="!isEditMode" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 最近IP（只读） -->
        <el-form-item label="最近IP">
          <el-input v-model="editForm.lastIpaddr" disabled />
        </el-form-item>

        <!-- 注册来源（只读） -->
        <el-form-item label="注册来源">
          <el-tag :type="sourceTagType(editForm.source)" size="small">
            {{ sourceLabel(editForm.source) }}
          </el-tag>
        </el-form-item>

        <!-- 最后更新时间（只读） -->
        <el-form-item label="最后更新" v-if="editForm.updateDate">
          <span class="readonly-text">{{ formatTime(editForm.updateDate) }}</span>
        </el-form-item>

        <!-- 账号状态 -->
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status" :disabled="!isEditMode">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">警告</el-radio>
            <el-radio value="99">封禁</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 封禁到期时间（状态为99时显示） -->
        <el-form-item label="封禁到期" v-if="editForm.status === '99'">
          <el-date-picker
              v-model="editForm.banExpireTime"
              type="datetime"
              placeholder="不填=永久封禁"
              :disabled="!isEditMode"
              style="width: 100%"
              value-format="x"
          />
          <div class="field-tip">不填写则永久封禁</div>
        </el-form-item>

        <!-- 处理理由 -->
        <el-form-item label="处理理由" v-if="editForm.status === '1' || editForm.status === '99'">
          <el-input
              v-model="editForm.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入警告或封禁的理由"
              :disabled="!isEditMode"
          />
        </el-form-item>

      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <template v-if="!isEditMode">
            <el-button @click="dialogVisible = false">关闭</el-button>
            <el-button type="primary" @click="enableEditMode">修改信息</el-button>
          </template>
          <template v-else>
            <el-button @click="cancelEdit">取消编辑</el-button>
            <el-button
                type="primary"
                :loading="submitLoading"
                :disabled="!isFormChanged"
                @click="submitEdit"
            >保存修改</el-button>
          </template>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getUserList, updateUser } from '../../api/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10 })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEditMode = ref(false)

const editForm = reactive({
  id: '',
  account: '',
  nickname: '',
  bio: '',
  birthday: null as string | null,
  website: '',
  email: '',
  mobilePhoneNumber: '',
  avatar: '',
  lastIpaddr: '',
  source: 1,
  updateDate: null as number | null,
  status: '0',
  banExpireTime: null as number | null,
  remark: '',
})

const backupForm = reactive({ ...editForm })

const isFormChanged = computed(() => {
  return editForm.nickname      !== backupForm.nickname
      || editForm.bio           !== backupForm.bio
      || editForm.birthday      !== backupForm.birthday
      || editForm.website       !== backupForm.website
      || editForm.email         !== backupForm.email
      || editForm.mobilePhoneNumber !== backupForm.mobilePhoneNumber
      || editForm.status        !== backupForm.status
      || editForm.banExpireTime !== backupForm.banExpireTime
      || editForm.remark        !== backupForm.remark
})

const formatTime = (ts: number) => ts ? dayjs(ts).format('YYYY-MM-DD HH:mm') : ''
const formatRelativeTime = (ts: number) => ts ? dayjs(ts).fromNow() : ''

// 注册来源标签
const sourceLabel = (source: number) => {
  const map: Record<number, string> = { 1: '账号注册', 2: 'QQ登录', 3: '微信登录' }
  return map[source] || '未知'
}
const sourceTagType = (source: number) => {
  const map: Record<number, string> = { 1: 'info', 2: 'primary', 3: 'success' }
  return map[source] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getUserList(queryParams)
    if (res.data.success && res.data.data) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => { queryParams.pageSize = val; fetchData() }
const handleCurrentChange = (val: number) => { queryParams.page = val; fetchData() }

const handleDetail = (row: any) => {
  editForm.id                 = row.id
  editForm.account            = row.account
  editForm.nickname           = row.nickname
  editForm.bio                = row.bio || ''
  editForm.birthday           = row.birthday || null
  editForm.website            = row.website || ''
  editForm.email              = row.email || ''
  editForm.mobilePhoneNumber  = row.mobilePhoneNumber || ''
  editForm.avatar             = row.avatar || ''
  editForm.lastIpaddr         = row.lastIpaddr || row.ipaddr || ''
  editForm.source             = row.source || 1
  editForm.updateDate         = row.updateDate || null
  editForm.status             = String(row.status ?? '0')
  editForm.banExpireTime      = row.banExpireTime || null
  editForm.remark             = row.remark || ''

  Object.assign(backupForm, editForm)
  isEditMode.value = false
  dialogVisible.value = true
}

const enableEditMode = () => { isEditMode.value = true }

const cancelEdit = () => {
  Object.assign(editForm, backupForm)
  isEditMode.value = false
}

const submitEdit = async () => {
  submitLoading.value = true
  try {
    const res: any = await updateUser(editForm)
    if (res.data.success) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  } catch (error) {
    ElMessage.error('系统异常')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => { isEditMode.value = false }

onMounted(() => fetchData())
</script>

<style scoped>
.index-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.subtitle { margin-left: 10px; font-size: 13px; color: #999; }
.left { display: flex; align-items: center; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.time-text { font-size: 13px; line-height: 1.2; }
.time-ago { font-size: 12px; color: #999; margin-top: 2px; }
.no-data { color: #ccc; font-size: 12px; }
.readonly-text { font-size: 13px; color: #909399; }
.field-tip { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
</style>