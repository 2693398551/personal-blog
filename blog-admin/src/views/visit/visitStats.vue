<template>
  <div class="stats-container">

    <!-- 今日数据卡片 -->
    <div class="card-row">
      <div class="stat-card">
        <div class="stat-label">今日 PV</div>
        <div class="stat-value">{{ formatNum(todayStats.pv) }}</div>
        <div class="stat-sub">页面访问量</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日 UV</div>
        <div class="stat-value">{{ formatNum(todayStats.uv) }}</div>
        <div class="stat-sub">独立访客数</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">近 30 天 PV</div>
        <div class="stat-value">{{ formatNum(totalPv) }}</div>
        <div class="stat-sub">累计页面访问</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">近 30 天 UV</div>
        <div class="stat-value">{{ formatNum(totalUv) }}</div>
        <div class="stat-sub">累计独立访客</div>
      </div>
    </div>

    <!-- 折线图 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="chart-header">
          <span class="panel-title">访问趋势</span>
          <div class="date-btns">
            <el-button
              v-for="d in dayOptions"
              :key="d"
              :type="activeDays === d ? 'primary' : ''"
              size="small"
              plain
              @click="changeDays(d)"
            >
              近 {{ d }} 天
            </el-button>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 240px; margin-left: 10px;"
              @change="handleDateRange"
            />
          </div>
        </div>
      </template>
      <div ref="trendChartRef" style="height: 300px; width: 100%;" v-loading="chartLoading"></div>
    </el-card>

    <!-- 新访客折线 -->
    <el-card shadow="never" class="chart-card" style="margin-top: 16px;">
      <template #header>
        <span class="panel-title">新访客趋势</span>
      </template>
      <div ref="newVisitorChartRef" style="height: 220px; width: 100%;"></div>
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getTodayStats, getRecentDays, getDateRange } from '../../api/visit'

// ---- 数据 ----
const todayStats = ref({ pv: 0, uv: 0 })
const totalPv = ref(0)
const totalUv = ref(0)
const chartLoading = ref(false)
const activeDays = ref(30)
const dayOptions = [7, 14, 30]
const dateRange = ref<[string, string] | null>(null)

const trendChartRef = ref<HTMLElement>()
const newVisitorChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let newVisitorChart: echarts.ECharts | null = null

const formatNum = (n?: number | null) => {
  if (n == null) return '--'
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  return n.toLocaleString()
}

// ---- 折线图初始化 ----
const initTrendChart = (list: any[]) => {
  if (!trendChartRef.value || !list.length) return
  trendChart?.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const labels = list.map((d: any) => d.date)
  const pvData  = list.map((d: any) => d.pv)
  const uvData  = list.map((d: any) => d.uv)

  // 更新近 30 天汇总
  totalPv.value = pvData.reduce((a: number, b: number) => a + b, 0)
  totalUv.value = uvData.reduce((a: number, b: number) => a + b, 0)

  trendChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['PV', 'UV'], left: 0, top: 0, itemWidth: 10, itemHeight: 10 },
    grid: { left: 50, right: 55, top: 36, bottom: 28 },
    xAxis: {
      type: 'category',
      data: labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: {
        fontSize: 11, color: '#aaa',
        interval: Math.floor(labels.length / 7)
      }
    },
    yAxis: [
      { type: 'value', axisLabel: { fontSize: 11, color: '#aaa' }, splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLine: { show: false }, axisTick: { show: false } },
      { type: 'value', axisLabel: { fontSize: 11, color: '#1D9E75' }, splitLine: { show: false }, axisLine: { show: false }, axisTick: { show: false } }
    ],
    series: [
      {
        name: 'PV', type: 'line', data: pvData,
        smooth: true, symbol: 'none',
        lineStyle: { color: '#378ADD', width: 2 },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(55,138,221,0.15)' },
          { offset: 1, color: 'rgba(55,138,221,0.01)' }
        ])},
        yAxisIndex: 0
      },
      {
        name: 'UV', type: 'line', data: uvData,
        smooth: true, symbol: 'none',
        lineStyle: { color: '#1D9E75', width: 2 },
        yAxisIndex: 1
      }
    ]
  })
}

const initNewVisitorChart = (list: any[]) => {
  if (!newVisitorChartRef.value || !list.length) return
  newVisitorChart?.dispose()
  newVisitorChart = echarts.init(newVisitorChartRef.value)

  const labels   = list.map((d: any) => d.date)
  const nvData   = list.map((d: any) => d.newVisitor)

  newVisitorChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 20, bottom: 28 },
    xAxis: {
      type: 'category', data: labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: { fontSize: 11, color: '#aaa', interval: Math.floor(labels.length / 7) }
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 11, color: '#aaa' },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLine: { show: false }, axisTick: { show: false }
    },
    series: [{
      name: '新访客', type: 'bar',
      data: nvData,
      barMaxWidth: 30,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#f6d365' },
          { offset: 1, color: '#fda085' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
}

// ---- 数据获取 ----
const fetchToday = async () => {
  try {
    const res = await getTodayStats()
    if (res.data.success) {
      todayStats.value = res.data.data
    }
  } catch (e) { console.error(e) }
}

const fetchRecentDays = async (days: number) => {
  chartLoading.value = true
  try {
    const res = await getRecentDays(days)
    if (res.data.success) {
      const list = (res.data.data || []).reverse() // 后端倒序，前端翻转为正序
      await nextTick()
      initTrendChart(list)
      initNewVisitorChart(list)
    }
  } catch (e) { console.error(e) }
  finally { chartLoading.value = false }
}

const fetchRange = async (start: string, end: string) => {
  chartLoading.value = true
  try {
    const res = await getDateRange(start, end)
    if (res.data.success) {
      const list = res.data.data || []
      await nextTick()
      initTrendChart(list)
      initNewVisitorChart(list)
    }
  } catch (e) { console.error(e) }
  finally { chartLoading.value = false }
}

const changeDays = (days: number) => {
  activeDays.value = days
  dateRange.value = null
  fetchRecentDays(days)
}

const handleDateRange = (val: [string, string] | null) => {
  if (!val) return
  fetchRange(val[0], val[1])
}

const handleResize = () => {
  trendChart?.resize()
  newVisitorChart?.resize()
}

onMounted(() => {
  fetchToday()
  fetchRecentDays(30)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  trendChart?.dispose()
  newVisitorChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.stats-container { padding: 20px; }

.card-row {
  display: flex; gap: 16px; margin-bottom: 16px;
}
.stat-card {
  flex: 1; min-width: 0;
  background: #f5f7fa; border-radius: 8px; padding: 18px 20px;
}
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 500; color: #303133; margin-bottom: 6px; }
.stat-sub   { font-size: 12px; color: #c0c4cc; }

.chart-card { width: 100%; }
.chart-header {
  display: flex; justify-content: space-between; align-items: center;
}
.panel-title { font-size: 14px; font-weight: 500; color: #303133; }
.date-btns { display: flex; align-items: center; gap: 6px; }
</style>
