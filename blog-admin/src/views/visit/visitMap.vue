<template>
  <div class="map-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>访客地图</span>
          <span class="subtitle">按城市聚合独立访客数（UV）</span>
        </div>
      </template>

      <div v-loading="loading" style="height: 580px;">
        <div ref="mapRef" style="width: 100%; height: 100%;"></div>
      </div>

      <!-- 城市排行 -->
      <el-divider>城市排行 Top 10</el-divider>
      <el-table :data="topCities" border stripe size="small" style="max-width: 600px; margin: 0 auto;">
        <el-table-column label="排名" width="70" align="center">
          <template #default="scope">
            <span :class="['rank-num', scope.$index < 3 ? 'rank-top' : '']">
              {{ scope.$index + 1 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" />
        <el-table-column prop="uv" label="UV" width="100" align="center">
          <template #default="scope">
            <span class="uv-num">{{ scope.row.uv }}</span>
          </template>
        </el-table-column>
        <el-table-column label="占比" width="200" align="center">
          <template #default="scope">
            <el-progress
              :percentage="calcPercent(scope.row.uv)"
              :show-text="false"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getCityMap } from '../../api/visit'

const loading = ref(false)
const mapRef = ref<HTMLElement>()
const cityData = ref<any[]>([])
const topCities = ref<any[]>([])
let chart: echarts.ECharts | null = null

const calcPercent = (uv: number) => {
  if (!topCities.value.length) return 0
  const max = topCities.value[0]?.uv || 1
  return Math.round((uv / max) * 100)
}

// 初始化中国地图（需要注册地图数据）
const initMap = async () => {
  if (!mapRef.value) return

  // 动态加载中国地图 GeoJSON
  let geoJson: any
  try {
    const resp = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    geoJson = await resp.json()
  } catch (e) {
    console.error('地图数据加载失败', e)
    return
  }

  echarts.registerMap('china', geoJson)
  chart?.dispose()
  chart = echarts.init(mapRef.value)

  // 将城市数据转为散点坐标（使用城市名匹配）
  const scatterData = cityData.value
    .filter(d => d.city && d.uv > 0)
    .map(d => ({
      name: d.city,
      value: d.uv
    }))

  chart.setOption({
    backgroundColor: '#fff',
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        return `${params.name}<br/>UV: <b>${params.value || 0}</b>`
      }
    },
    visualMap: {
      min: 0,
      max: cityData.value.length ? Math.max(...cityData.value.map((d: any) => d.uv)) : 100,
      left: 'left',
      top: 'bottom',
      text: ['高', '低'],
      inRange: { color: ['#e0f3ff', '#006edd'] },
      show: true,
      calculable: true
    },
    series: [
      {
        name: '访客分布',
        type: 'map',
        map: 'china',
        roam: true,
        zoom: 1.2,
        label: {
          show: false,
          fontSize: 10,
          color: '#333'
        },
        emphasis: {
          label: { show: true },
          itemStyle: { areaColor: '#409EFF' }
        },
        data: scatterData,
        itemStyle: {
          areaColor: '#f0f7ff',
          borderColor: '#ccc',
          borderWidth: 0.5
        }
      }
    ]
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCityMap()
    if (res.data.success) {
      cityData.value = res.data.data || []
      // 取 Top 10
      topCities.value = [...cityData.value]
        .sort((a, b) => b.uv - a.uv)
        .slice(0, 10)
      await nextTick()
      initMap()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleResize = () => chart?.resize()

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.map-container { padding: 20px; }
.card-header { display: flex; align-items: center; gap: 12px; font-weight: bold; }
.subtitle { font-size: 13px; color: #999; font-weight: normal; }
.rank-num { font-weight: bold; color: #909399; }
.rank-top { color: #E6A23C; }
.uv-num { font-weight: bold; color: #409EFF; }
</style>
