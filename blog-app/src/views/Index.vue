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
				<card-message cardHeader="站点信息" :webinfo="webinfo"></card-message>
				<card-clock class="card_clock"></card-clock>
				
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
  import cardClock from '@/components/card/cardClock'
  import CardMessage from '@/components/card/CardMessage'

 
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
	  CardMessage,
	  mainTitle,
	  	headertop,
		'card-clock':cardClock,
    }
  }
</script>

<style>
/* @import url("https://unpkg.com/element-ui@2.15.2/lib/theme-chalk/index.css"); */


	.container {
		max-width: 1170px;
		padding-top: 20px;
		padding-right: 15px;
		padding-left: 15px;
		
		margin-right: auto;
		margin-left: auto;
	}
	.row{
		display: flex;
		
		margin-right: -15px;
		margin-left: -15px;
	}

	.content-main{
		
		
		min-height: 1px;
		padding-right: 15px;
		padding-left: 15px;
	}
	.content-right{
		
		
		min-height: 1px;
		padding-right: 15px;
		padding-left: 15px;
	}
	
	.card-list{
		max-width: 300px;
	}
	.el-card {
		border-radius: 8px;
	}
	.el-card:not(:first-child) {
		margin-top: 20px;
	}

	@media only screen and (max-width:1120px) {
		.row{
			flex-direction: column;
		}
		
			
	}
	

	

</style>
