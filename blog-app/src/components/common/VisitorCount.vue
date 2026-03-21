<template>
  <div v-if="show" class="visitor-count">
    <span class="item">
      <i class="el-icon-view"></i>
      访问量 <span class="num">{{ formatNum(pv) }}</span>
    </span>
    <span class="divider">·</span>
    <span class="item">
      <i class="el-icon-user"></i>
      访客数 <span class="num">{{ formatNum(uv) }}</span>
    </span>
  </div>
</template>

<script>
import { getVisitTotal } from '@/api/visit'

export default {
  name: 'VisitorCount',
  data() {
    return {
      show: false,
      pv: 0,
      uv: 0
    }
  },
  mounted() {
    this.fetchTotal()
  },
  methods: {
    async fetchTotal() {
      try {
        const res = await getVisitTotal()
        if (res.success && res.data) {
          this.pv = res.data.pv
          this.uv = res.data.uv
          this.show = true
        }
        // res.data 为 null 说明后台关闭了 show_visitor_count 开关，不展示
      } catch (e) {}
    },
    formatNum(n) {
      if (!n) return '0'
      if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
      return n.toLocaleString()
    }
  }
}
</script>

<style scoped>
.visitor-count {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  color: #aaa;
  letter-spacing: 1px;
}
.item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.num {
  color: #d4af37;
  font-weight: 600;
}
.divider {
  color: #d4af37;
  opacity: 0.4;
}
</style>
