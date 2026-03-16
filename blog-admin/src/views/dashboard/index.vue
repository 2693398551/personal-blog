<template>
  <div class="dashboard-container" v-loading="loading">

    <!-- 顶部 4 张数据卡片：flex + gap，不用 el-row 避免负 margin 挤压 -->
    <div class="card-row">
      <div class="stat-card">
        <div class="stat-label">文章总数</div>
        <div class="stat-value">{{ data.articleCount ?? '--' }}</div>
        <div class="stat-sub">本月新增 <span class="accent">{{ data.monthArticleCount ?? 0 }}</span> 篇</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">总浏览量</div>
        <div class="stat-value">{{ formatNum(data.totalViewCount) }}</div>
        <div class="stat-sub">累计访问量</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">评论总数</div>
        <div class="stat-value">{{ formatNum(data.commentCount) }}</div>
        <div class="stat-sub">本月新增 <span class="accent">{{ data.monthCommentCount ?? 0 }}</span> 条</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">注册用户</div>
        <div class="stat-value">{{ formatNum(data.userCount) }}</div>
        <div class="stat-sub">本月新增 <span class="accent">{{ data.monthUserCount ?? 0 }}</span> 人</div>
      </div>
    </div>

    <!-- 第二行：折线图 + 热门文章 -->
    <div class="two-col mid-row">
      <el-card shadow="never" class="panel flex-2">
        <template #header>
          <span class="panel-title">近 30 天浏览趋势</span>
        </template>
        <div ref="trendChartRef" class="chart-box" style="height: 240px;"></div>
      </el-card>
      <el-card shadow="never" class="panel flex-1">
        <template #header>
          <span class="panel-title">最热文章 Top 5</span>
        </template>
        <ul class="hot-list">
          <li v-for="(item, idx) in data.hotArticles" :key="item.id" class="hot-item">
            <span :class="['rank', idx < 3 ? 'rank-top' : '']">{{ idx + 1 }}</span>
            <span class="hot-title" :title="item.title">{{ item.title }}</span>
            <span class="hot-views">{{ formatNum(item.viewCounts) }}</span>
          </li>
          <li v-if="!data.hotArticles?.length" class="empty-tip">暂无数据</li>
        </ul>
      </el-card>
    </div>

    <!-- 第三行：分类占比 + 最新评论 -->
    <div class="two-col">
      <el-card shadow="never" class="panel flex-1">
        <template #header>
          <span class="panel-title">分类文章占比</span>
        </template>
        <div ref="pieChartRef" class="chart-box" style="height: 220px;"></div>
      </el-card>
      <el-card shadow="never" class="panel flex-15">
        <template #header>
          <span class="panel-title">最新评论</span>
        </template>
        <ul class="comment-list">
          <li v-for="item in data.recentComments" :key="item.id" class="comment-item">
            <el-avatar :size="32" :src="item.authorAvatar" class="c-avatar">
              {{ item.authorNickname?.charAt(0) }}
            </el-avatar>
            <div class="c-body">
              <div class="c-meta">
                <span class="c-nick">{{ item.authorNickname }}</span>
                <span class="c-time">{{ formatTime(item.createDate) }}</span>
              </div>
              <div class="c-content">{{ item.content }}</div>
            </div>
          </li>
          <li v-if="!data.recentComments?.length" class="empty-tip">暂无评论</li>
        </ul>
      </el-card>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { getDashboardData } from '../../api/dashboardApi.ts'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const data = ref<any>({})

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const formatNum = (n?: number | null) => {
  if (n == null) return '--'
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  return n.toLocaleString()
}

const formatTime = (ts?: number | null) => {
  if (!ts) return ''
  return dayjs(ts).fromNow()
}

const COLORS = ['#378ADD', '#1D9E75', '#EF9F27', '#D85A30', '#7F77DD', '#D4537E']

const initTrendChart = () => {
  if (!trendChartRef.value || !data.value.trendData?.length) return
  trendChart?.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const trend = data.value.trendData
  const labels = trend.map((d: any) => d.date)
  const views  = trend.map((d: any) => d.views)
  const comments = trend.map((d: any) => d.comments)

  trendChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: {
      data: ['浏览量', '评论数'],
      left: 0,      // 图例靠左，不与右侧 Y 轴标签叠压
      top: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 12, color: '#888' }
    },
    // left/right 留足两侧 Y 轴数字的空间，top 留图例，bottom 留 X 轴标签
    grid: { left: 50, right: 55, top: 36, bottom: 28 },
    xAxis: {
      type: 'category',
      data: labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: {
        fontSize: 11,
        color: '#aaa',
        interval: Math.floor(labels.length / 7)
      }
    },
    yAxis: [
      {
        type: 'value',
        axisLabel: { fontSize: 11, color: '#aaa' },
        splitLine: { lineStyle: { color: '#f0f0f0' } },
        axisLine: { show: false },
        axisTick: { show: false }
      },
      {
        type: 'value',
        axisLabel: { fontSize: 11, color: '#1D9E75' },
        splitLine: { show: false },
        axisLine: { show: false },
        axisTick: { show: false }
      }
    ],
    series: [
      {
        name: '浏览量',
        type: 'line',
        data: views,
        smooth: true,
        symbol: 'none',
        lineStyle: { color: '#378ADD', width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(55,138,221,0.15)' },
            { offset: 1, color: 'rgba(55,138,221,0.01)' }
          ])
        },
        yAxisIndex: 0
      },
      {
        name: '评论数',
        type: 'line',
        data: comments,
        smooth: true,
        symbol: 'none',
        lineStyle: { color: '#1D9E75', width: 2 },
        yAxisIndex: 1
      }
    ]
  })
}

const initPieChart = () => {
  if (!pieChartRef.value || !data.value.categoryStats?.length) return
  pieChart?.dispose()
  pieChart = echarts.init(pieChartRef.value)

  const stats = data.value.categoryStats
  const pieData = stats.map((s: any, i: number) => ({
    name: s.categoryName,
    value: s.count,
    itemStyle: { color: COLORS[i % COLORS.length] }
  }))

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 篇 ({d}%)' },
    legend: {
      orient: 'vertical',
      right: 0,
      top: 'middle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 12, color: '#888' }
    },
    series: [{
      type: 'pie',
      radius: ['50%', '72%'],
      center: ['38%', '50%'],
      data: pieData,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 13, fontWeight: 500 } }
    }]
  })
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDashboardData()
    if (res.data.success) {
      data.value = res.data.data
      await nextTick()
      initTrendChart()
      initPieChart()
    }
  } catch (e) {
    console.error('仪表盘数据加载失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  trendChart?.dispose()
  pieChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

/* 顶部卡片行：flex + gap，与 el-row 完全解耦 */
.card-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  flex: 1;
  min-width: 0;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 18px 20px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
}
.stat-sub {
  font-size: 12px;
  color: #c0c4cc;
}
.stat-sub .accent {
  color: #409EFF;
  font-weight: 500;
}

/* 两列布局 */
.two-col {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.mid-row { margin-bottom: 16px; }

.flex-2  { flex: 2;   min-width: 0; }
.flex-1  { flex: 1;   min-width: 0; }
.flex-15 { flex: 1.5; min-width: 0; }

.panel { width: 100%; }
.panel-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.chart-box { width: 100%; }

/* 热门文章 */
.hot-list { list-style: none; padding: 0; margin: 0; }
.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f2f2f2;
  font-size: 13px;
}
.hot-item:last-child { border-bottom: none; }
.rank {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #f2f2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 500;
  color: #909399;
  flex-shrink: 0;
}
.rank-top { background: #FAC775; color: #633806; }
.hot-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #303133;
}
.hot-views { color: #909399; font-size: 12px; flex-shrink: 0; }

/* 最新评论 */
.comment-list { list-style: none; padding: 0; margin: 0; }
.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f2f2f2;
}
.comment-item:last-child { border-bottom: none; }
.c-avatar { flex-shrink: 0; }
.c-body { flex: 1; min-width: 0; }
.c-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.c-nick { font-size: 13px; font-weight: 500; color: #303133; }
.c-time { font-size: 11px; color: #c0c4cc; }
.c-content {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-tip {
  color: #c0c4cc;
  font-size: 13px;
  padding: 20px 0;
  text-align: center;
}
</style>