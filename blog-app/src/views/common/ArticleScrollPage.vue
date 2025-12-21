<template>
  <scroll-page :loading="loading" :offset="offset" :no-data="noData" v-on:load="load">
    <article-item v-for="(a,index) in articles" :key="a.id" v-bind="a" :index="index"></article-item>
  </scroll-page>
</template>
<script>
  import ArticleItem from '@/components/article/ArticleItem'
  import ScrollPage from '@/components/scrollpage'
  import {getArticles} from '@/api/article'
  export default {
    name: "ArticleScrollPage",
    props: {
      offset: {
        type: Number,
        default: 100
      },
      page: {
        type: Object,
        default() {
          return {}
        }
      },
      query: {
        type: Object,
        default() {
          return {}
        }
      }
    },
    watch: {
      'query': {
        handler() {
          this.noData = false
          this.articles = []
          this.innerPage.pageNumber = 1
          this.getArticles()
        },
        deep: true
      },
      'page': {
        handler() {
          this.noData = false
          this.articles = []
          this.innerPage = this.page
          this.getArticles()
        },
        deep: true
      }
    },
    created() {
      this.getArticles()
	
    },
	mounted() {
		
		   /* let _this = this;
		    window.onscroll = function(){
		      //变量scrollTop是滚动条滚动时，距离顶部的距离
		      var scrollTop = document.documentElement.scrollTop||document.body.scrollTop;
		      //变量windowHeight是可视区的高度
		      var windowHeight = document.documentElement.clientHeight || document.body.clientHeight;
		      //变量scrollHeight是滚动条的总高度
		      var scrollHeight = document.documentElement.scrollHeight||document.body.scrollHeight;
		        //滚动条到底部的条件
		        if(scrollTop+windowHeight == scrollHeight){
		        //到了这个就可以进行业务逻辑加载后台数据了
		          _this.isBottom = true;
		          console.log("到了底部");
		        }else{
		          _this.isBottom = false;
		        } 
		      } */
	},
    data() {
      return {
        loading: false,
        noData: false,
        innerPage: {
          pageSize: 5,
          pageNumber: 1,
          name: 'a.createDate',
          sort: 'desc'
        },
        articles: []
      }
    },
    methods: {

      load() {
        this.getArticles()
      },
      view(id) {
        this.$router.push({path: `/view/${id}`})
      },
      getArticles() {
        let that = this
        that.loading = true

        getArticles(that.query, that.innerPage,this.$store.state.token).then(data => {
          let newArticles = data.data
		 
          if (newArticles && newArticles.length > 0) {
            that.innerPage.pageNumber += 1
			for(var i=0;i<newArticles.length;i++){
				let t=newArticles[i].summary.slice(0, 1)=='■';
				if(t){
					let NewSummary='该文章以加密请登录后才看';
					newArticles[i].summary=NewSummary;
				}
			}
			
			
            that.articles = that.articles.concat(newArticles)
			
			
          } else {
            that.noData = true
          }

        }).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '文章加载失败!', showClose: true})
          }
        }).finally(() => {
          that.loading = false
        })
      }
    },
    components: {
      'article-item': ArticleItem,
      'scroll-page': ScrollPage
    }
  }
</script>

<style scoped>
  .el-card {
    border-radius: 0;
  }

  .el-card:not(:first-child) {
    margin-top: 10px;
  }
  
</style>
