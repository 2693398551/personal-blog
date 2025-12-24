<template>

<div v-title :data-title="title" >
	<!-- 首页布局 -->
	<div class="container">

		<div class="row">
			 <div class="content-left">
			 </div>
			  <div class="content-main">
				    <main-title></main-title>
					<article-scroll-page v-bind="article"></article-scroll-page>
			  </div>

			  <div class="content-right">
			  <div class="card-list">

				<card-me class="me-area"></card-me>



				<card-tag :tags="hotTags"></card-tag>

				<card-article cardHeader="最热" :articles="hotArticles"></card-article>

				<card-article cardHeader="最新" :articles="newArticles"></card-article>

				<card-technology cardHeader="暂时？" :archives="archives2"></card-technology>
			  </div>
			</div>
		</div>
	</div>
</div>

</template>

<script>
  import mainTitle from '@/components/article/MainTitle'
  import cardMe from '@/components/card/cardMe'
  import cardArticle from '@/components/card/cardArticle'
  import CardTechnology from '@/components/card/CardTechnology'
  import cardTag from '@/components/card/cardTag'
  import ArticleScrollPage from '@/views/common/ArticleScrollPage'
  import headertop from '@/components/page'



  import {getArticles, getHotArtices, getNewArtices} from '@/api/article'
  import {getWebinfo} from '@/api/utils'
  import {getHotTags} from '@/api/tag'
  import {listArchives} from '@/api/article'

  export default {
    name: 'Index',
    created() {
      this.getHotArtices()
      this.getNewArtices()
      this.getHotTags()
      this.listArchives()
    },
    data() {
      return {
		webinfo:[],
        hotTags: [],
        hotArticles: [],
        newArticles: [],
        article: {
          query: {
            month: this.$route.params.month,
            year: this.$route.params.year
          }
        },
        archives: [],
		archives2:[]
      }
    },
		computed: {
		  title() {
		    return `${this.$myName}の里世界`
		  }
		},
    methods: {
      getHotArtices() {
        let that = this
        getHotArtices().then(data => {
          that.hotArticles = data.data
        }).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '最热文章加载失败!', showClose: true})
          }

        })

      },
      getNewArtices() {
        let that = this
        getNewArtices().then(data => {
          that.newArticles = data.data
        }).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '最新文章加载失败!', showClose: true})
          }

        })

      },
      getHotTags() {
        let that = this
        getHotTags().then(data => {
          that.hotTags = data.data
        }).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '最热标签加载失败!', showClose: true})
          }

        })
      },
      listArchives() {
        listArchives().then((data => {
          this.archives = data.data
		  this.archives2 = data.data.reverse()
        })).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '文章归档加载失败!', showClose: true})
          }
        })
      }
    },
    components: {
      'card-me': cardMe,
      'card-article': cardArticle,
      'card-tag': cardTag,
      ArticleScrollPage,
      CardTechnology,

	  mainTitle,
	  	headertop,

    }
  }
</script>

<style>
/* 页面容器：居中，最大宽度限制 */
.container {
  max-width: 1200px; /* 稍微调宽一点，视觉更舒服 */
  margin: 0 auto;
  padding: 20px 15px;
  box-sizing: border-box; /* 确保 padding 不会撑大宽度 */
}

/* Flex 布局容器 */
.row {
  display: flex;
  justify-content: space-between; /* 让左右两边分开对齐 */
  gap: 20px; /* 这一行很关键：设置列与列之间的间距 */
  align-items: flex-start; /* 顶部对齐 */
}

/* 1. 左侧空占位 (如果你以后要用，可以给它宽度) */
.content-left {
  display: none; /* 暂时隐藏，因为它是空的，占位置会影响布局 */
  /* 如果你要三栏布局，可以把上面这行删掉，改成 width: 200px; */
}

/* 2. 中间主要内容区 (文章列表) */
.content-main {
  flex: 1; /* 关键：让它占据剩余的所有空间 */
  min-width: 0; /* 关键：防止文章内部长图或代码块撑爆布局 */
  /* 这里的 padding 可以去掉，由 gap 控制间距更现代 */
}

/* 3. 右侧侧边栏 (个人卡片等) */
.content-right {
  width: 300px; /* 固定宽度 */
  flex-shrink: 0; /* 关键：防止屏幕变窄时侧边栏被挤扁 */
}

/* 侧边栏内部的卡片列表 */
.card-list {
  width: 100%;
}

/* 卡片样式微调 */
.el-card {
  border-radius: 8px;
  border: none; /* 可选：去掉边框，看起来更干净 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 加一点现代阴影 */
  margin-bottom: 20px; /* 统一卡片间距 */
}

/* 覆盖 el-card 的 margin-top，改用 margin-bottom 统一控制 */
.el-card:not(:first-child) {
  margin-top: 0;
}

/* --- 响应式适配 --- */

/* 当屏幕小于 960px 时 (平板/手机) */
@media only screen and (max-width: 960px) {
  .row {
    flex-direction: column; /* 改为上下排列 */
  }

  .content-right {
    width: 100%; /* 侧边栏变更为 100% 宽度 */
    margin-top: 20px; /* 和上面内容拉开距离 */
  }

  .card-list {
    max-width: 100%; /* 取消最大宽度限制 */
  }

  /* 如果想在手机端把侧边栏放到下面，column 默认就是这样。
     如果想在手机端隐藏侧边栏，可以在这里加 .content-right { display: none; }
  */
  .content-right { display: none; }
}
</style>
