<template>
  <div class="visitor-container">
    <el-card class="visitor-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <h2>访客管理</h2>
            <span class="subtitle">共 {{ total }} 位访客</span>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索 IP / UUID / 城市 / 浏览器"
            clearable
            style="width: 280px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 访客列表 -->
      <el-table
        :data="visitorList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        height="calc(100vh - 300px)"
      >
        <el-table-column label="UUID" width="220" show-overflow-tooltip>
          <template #default="scope">
            <span class="uuid-text">{{ scope.row.uuid }}</span>
          </template>
        </el-table-column>

        <el-table-column label="IP 地址" width="140">
          <template #default="scope">
            <div>{{ scope.row.ip || '-' }}</div>
            <div class="cell-sub">{{ scope.row.city || scope.row.province || '未知' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="归属地" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.ipLocation">{{ scope.row.ipLocation }}</span>
            <span v-else class="text-gray">未知</span>
          </template>
        </el-table-column>

        <el-table-column label="设备" min-width="160">
          <template #default="scope">
            <div>{{ scope.row.browser || '-' }}</div>
            <div class="cell-sub">{{ scope.row.os || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="关联用户" width="140" align="center">
          <template #default="scope">
            <div v-if="scope.row.userId" style="display:flex;align-items:center;gap:6px;justify-content:center">
              <el-avatar :size="24" :src="scope.row.userAvatar" />
              <span style="font-size:12px">{{ scope.row.userNickname }}</span>
            </div>
            <el-tag v-else type="info" size="small">匿名</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="累计 PV" width="90" align="center">
          <template #default="scope">
            <span class="pv-num">{{ scope.row.pv }}</span>
          </template>
        </el-table-column>

        <el-table-column label="首次访问" width="160" align="center">
          <template #default="scope">
            {{ formatTime(scope.row.firstVisit) }}
          </template>
        </el-table-column>

        <el-table-column label="最后访问" width="160" align="center">
          <template #default="scope">
            {{ formatTime(scope.row.lastVisit) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="showLogs(scope.row)">
              查看记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 行为明细弹窗 -->
    <el-dialog
      v-model="logVisible"
      :title="`访问记录 - ${currentUuid}`"
      width="860px"
    >
      <el-table :data="logList" v-loading="logLoading" border height="450">
        <el-table-column label="行为类型" width="130" align="center">
          <template #default="scope">
            <el-tag :type="behaviorTagType(scope.row.behavior)" size="small">
              {{ behaviorLabel(scope.row.behavior) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.content">{{ scope.row.content }}</span>
            <span v-else class="text-gray">{{ scope.row.uri }}</span>
          </template>
        </el-table-column>

        <el-table-column label="设备" width="200">
          <template #default="scope">
            <div>{{ scope.row.browser || '-' }}</div>
            <div class="cell-sub">{{ scope.row.os || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="来源" width="160" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.referer" class="referer-text">{{ scope.row.referer }}</span>
            <span v-else class="text-gray">直接访问</span>
          </template>
        </el-table-column>

        <el-table-column label="访问时间" width="160" align="center">
          <template #default="scope">
            {{ formatTime(scope.row.createDate) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" style="margin-top: 12px;">
        <el-pagination
          v-model:current-page="logPage"
          :page-size="20"
          background
          layout="total, prev, pager, next"
          :total="logTotal"
          @current-change="fetchLogs"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getVisitorList, getVisitLogs } from '../../api/visit'

const loading = ref(false)
const visitorList = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 20,
  keyword: ''
})

// 行为明细弹窗
const logVisible = ref(false)
const logLoading = ref(false)
const logList = ref<any[]>([])
const logTotal = ref(0)
const logPage = ref(1)
const currentUuid = ref('')

const formatTime = (ts: number) => {
  if (!ts) return '-'
  return dayjs(ts).format('YYYY-MM-DD HH:mm')
}

const behaviorLabel = (behavior: string) => {
  const map: Record<string, string> = {
    PAGE_VIEW: '浏览页面',
    VIEW_ARTICLE: '查看文章',
    VIEW_CATEGORY: '查看分类',
    COMMENT: '发表评论',
    OTHER: '其他'
  }
  return map[behavior] || behavior
}

const behaviorTagType = (behavior: string) => {
  const map: Record<string, string> = {
    PAGE_VIEW: '',
    VIEW_ARTICLE: 'success',
    VIEW_CATEGORY: 'warning',
    COMMENT: 'danger',
    OTHER: 'info'
  }
  return map[behavior] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getVisitorList(queryParams)
    if (res.data.success) {
      visitorList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const resetSearch = () => {
  queryParams.keyword = ''
  queryParams.page = 1
  fetchData()
}

const showLogs = (row: any) => {
  currentUuid.value = row.uuid
  logPage.value = 1
  logList.value = []
  logVisible.value = true
  fetchLogs(1)
}

const fetchLogs = async (page: number) => {
  logLoading.value = true
  logPage.value = page
  try {
    const res = await getVisitLogs(currentUuid.value, page, 20)
    if (res.data.success) {
      logList.value = res.data.data.records || []
      logTotal.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    logLoading.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.visitor-container { padding: 20px; height: 100%; box-sizing: border-box; }
.visitor-card { height: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.left { display: flex; align-items: center; gap: 10px; }
.left h2 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #999; }
.search-form { margin-bottom: 15px; background: #f9f9f9; padding: 15px; border-radius: 4px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.uuid-text { font-family: monospace; font-size: 12px; color: #606266; }
.cell-sub { font-size: 12px; color: #909399; margin-top: 2px; }
.text-gray { color: #c0c4cc; font-size: 12px; }
.pv-num { font-weight: bold; color: #409EFF; }
.referer-text { font-size: 12px; color: #606266; }
</style>
