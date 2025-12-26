<template>
  <div class="diary-wrap" >
    <div
      class="diary-card"
      :class="[themeClass, textureClass, { 'is-reverse': textalign(index) === 'right' }]"
      :style="{ transform: `rotate(${rotateDeg}deg)` }"
      @click="view(id)"
    >

      <div class="corner-doodle top-left"></div>
      <div class="corner-doodle bottom-right"></div>

      <div class="diary-img-box">
        <div class="polaroid-frame">
          <img :src="Imgview(index)" class="diary-cover" alt="cover">

          <div class="tape-serial-label">
            <span class="tape-text">No.{{ index + 1 < 10 ? '0' + (index + 1) : index + 1 }}</span>
          </div>

          <div class="photo-pin-doodle"></div>
        </div>

        <div class="falling-decor">
          <i></i><i></i><i></i>
        </div>
      </div>

      <div class="diary-content">
        <div class="meta-header">
           <span class="date-tag">
             <i class="el-icon-time" style="margin-right:4px;"></i>{{ createDate }}
           </span>
        </div>

        <h3 class="diary-title">{{ title }}</h3>

        <div class="divider-hand-drawn">- - - - - - - ✂ - - - - - - -</div>

        <div class="diary-summary">{{ summary }}</div>

        <div class="diary-footer">
          <div class="tags-group">
            <span v-for="tag in tags" :key="tag.id" class="marker-tag">#{{ tag.tagName }}</span>
          </div>
          <div class="stats-group"><span>♥ {{ viewCounts }} / {{ commentCounts }}</span></div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { formatTime } from "@/utils/time";

export default {
  name: 'ArticleItem',
  props: {
    id: String, weight: Number, title: String, commentCounts: Number, viewCounts: Number,
    summary: String, author: String, tags: Array, createDate: String, cover: String, index: Number,
  },
  data() {
    return {
      themesConfig: [
        { name: 'Sakura', class: 'theme-sakura', texture: 'texture-line' },
        { name: 'Blue',   class: 'theme-blue',   texture: 'texture-grid' },
        { name: 'Mint',   class: 'theme-mint',   texture: 'texture-dot' },
        { name: 'Lemon',  class: 'theme-lemon',  texture: 'texture-grid' },
        { name: 'Taro',   class: 'theme-taro',   texture: 'texture-line' },
        { name: 'Cocoa',  class: 'theme-cocoa',  texture: 'texture-kraft' },
        { name: 'Peach',  class: 'theme-peach',  texture: 'texture-dot' },
        { name: 'Gray',   class: 'theme-gray',   texture: 'texture-grid' },
        { name: 'Berry',  class: 'theme-berry',  texture: 'texture-line' },
        { name: 'Soda',   class: 'theme-soda',   texture: 'texture-dot' }
      ]
    }
  },
  computed: {
    currentThemeConfig() { return this.themesConfig[this.index % this.themesConfig.length]; },
    themeClass() { return this.currentThemeConfig.class; },
    textureClass() { return this.currentThemeConfig.texture; },
    themeName() { return this.currentThemeConfig.name; },
    rotateDeg() { return (this.index % 2 === 0 ? -1 : 1); }
  },
  methods: {
    view(id) { this.$router.push({ path: `/view/${id}` }) },
    Imgview(index) { return "https://www.loliapi.com/acg/?uuid=" + index },
    textalign(index) { return index % 2 != 0 ? "right" : "left"; },
    formatTime
  }
}
</script>

<style scoped>
@import '../../assets/css/macaron-themes.css';
</style>
