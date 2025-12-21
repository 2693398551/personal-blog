<template>
  <el-card :body-style="{ padding: '8px 18px' }">
    <div slot="header" class="me-category-header">
      <span v-if="this.cardHeader=='最热'"><!-- {{cardHeader}} -->
			<i class="iconfont icon-huore"></i>浏览最多
	  </span>
	  <span v-if="this.cardHeader=='最新'"><!-- {{cardHeader}} -->
	  		<i class="iconfont icon-xin"></i>最新发布
	  </span>
    </div>
	
    <ul class="me-category-list">
      <li v-for="a in articles" @click="view(a.id)" :style="itemStyle" :key="a.id" class="me-category-item"><a>{{a.title}}</a>
      </li>
    </ul>
  </el-card>

</template>

<script>
  export default {
    name: 'cardArticle',
    props: {
      cardHeader: {
        type: String,
        required: true
      },
      articles: {
        type: Array,
        required: true
      },
      itemStyle: Object
    },
    data() {
      return {}
    },
    methods: {
      view(id) {
        this.$router.push({path: `/view/${id}`})
      }
    }
  }
</script>

<style scoped>
	@import url("https://at.alicdn.com/t/font_3295543_mha9e45b61k.css");
  .me-category-header {
    font-weight: 600;
  }

  .me-category-list {
    list-style-type: none;
  }

  .me-category-item {
    padding: 4px;
    font-size: 14px;
    
	color:#000000;
  }

  .me-category-item a:hover {
    text-decoration: underline;
	color:#409EFF;
  }
  .icon-huore{
	  color: red;
  }
  .icon-xin{
	  color:#33CCFF;
  }
</style>
