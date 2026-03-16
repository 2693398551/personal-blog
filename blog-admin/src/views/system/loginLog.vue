<template>
  <div class="log-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>登录日志</h2>
          <span class="subtitle">系统登录行为审计</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="账号">
          <el-input v-model="queryParams.keyword" placeholder="搜索账号..." clearable
                    style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logList" v-loading="loading" border stripe style="width: 100%">

        <!-- 账号 + 角色 -->
        <el-table-column label="登录账号" width="150">
          <template #default="scope">
            <div class="account-name">{{ scope.row.account }}</div>
            <div class="account-role">{{ scope.row.roleName }}</div>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 登录地点：归属地 + IP -->
        <el-table-column label="登录地点" min-width="180">
          <template #default="scope">
            <div>{{ scope.row.ipLocation || '未知' }}</div>
            <div class="cell-sub">{{ scope.row.ip }}</div>
          </template>
        </el-table-column>

        <!-- 登录设备：浏览器 + 操作系统 -->
        <el-table-column label="登录设备" min-width="160">
          <template #default="scope">
            <div>{{ scope.row.browser || '-' }}</div>
            <div class="cell-sub">{{ scope.row.os || '-' }}</div>
          </template>
        </el-table-column>

        <!-- 操作信息 -->
        <el-table-column label="操作信息" min-width="160">
          <template #default="scope">
            <span v-if="scope.row.status === 1" class="msg-success">登录成功</span>
            <span v-else class="msg-fail">{{ scope.row.msg || '登录失败' }}</span>
          </template>
        </el-table-column>

        <!-- 登录时间：日期 + 时间分两行 -->
        <el-table-column label="登录时间" width="160" align="center">
          <template #default="scope">
            <div>{{ formatDate(scope.row.createDate) }}</div>
            <div class="cell-sub">{{ formatHms(scope.row.createDate) }}</div>
          </template>
        </el-table-column>

      </el-table>

      <div class="pagination-wrap">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[20, 50, 100]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="fetchData"
            @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLoginLogList } from '../../api/LoginLog'
import dayjs from 'dayjs'

const loading = ref(false)
const logList = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 20,
  keyword: '',
  status: undefined as number | undefined
})

const formatDate = (ts: number) => ts ? dayjs(ts).format('YYYY-MM-DD') : '-'
const formatHms  = (ts: number) => ts ? dayjs(ts).format('HH:mm:ss') : ''

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLoginLogList(queryParams)
    if (res.data.success) {
      logList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const resetSearch = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.page = 1
  fetchData()
}

onMounted(() => fetchData())
</script>

<style scoped>
.log-container { padding: 20px; }
.card-header { display: flex; align-items: center; gap: 10px; }
.card-header h2 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #999; }
.search-form { margin-bottom: 15px; background: #f9f9f9; padding: 15px; border-radius: 4px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }

/* 账号两行 */
.account-name { font-size: 13px; font-weight: 500; color: #303133; }
.account-role { font-size: 12px; color: #909399; margin-top: 2px; }

/* 通用副行（灰色小字） */
.cell-sub { font-size: 12px; color: #909399; margin-top: 2px; }

/* 操作信息 */
.msg-success { font-size: 12px; color: #67c23a; }
.msg-fail    { font-size: 12px; color: #f56c6c; }
</style>