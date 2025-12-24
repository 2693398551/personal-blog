<template>
	<!-- 归档ArticleItem -->
	<div class="sss">
		<article-item v-for="(a,index) in articles" :key="a.id" v-bind="a" :index="index"></article-item>
		  <div class="block">
		    <el-pagination
		      @size-change="handleSizeChange"
		      @current-change="handleCurrentChange"
		      :current-page.sync="currentPage"
		      :page-sizes="pageSizes"
		      :page-size="innerPage.pageSize"
		      layout="total, sizes, prev, pager, next, jumper"
		      :total="ArticlesLength">
		    </el-pagination>
		  </div>
	</div>
</template>
<script>
  import ArticleItem from '@/components/article'
  import {getListArticleCount} from '@/api/article'
  import {getArticles} from '@/api/article'
  export default {
    name: "index",
	year:'year',
	month:'month',
    props: {
	year:Number,
	month:Number,
      page: {
        type: Object,
        default() {
          return {}
        }
      },
      query: {
        type: Object,
        default() {
          return {

		  }
        }
      }
    },
	computed:{
		Article(){
			let length=this.articles.length;
			let currentPage=1;
			return {
			  length, currentPage,
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
      this.load()
    },
	mounted(){

	},
    data() {
      return {
		token:undefined,
		ArticlesLength:0,
		currentPage:1,
		pageSizes:[3,6,9],
        loading: false,
        noData: false,
        innerPage: {
          pageNumber: 1,
		  pageSize:3,
          name: 'a.createDate',
          sort: 'desc',
		  year: this.year,
		  month: this.month,
        },
        articles: []
      }
    },
    methods: {
      load() {
        this.getArticles()
		this.getListArticleCount()
      },
      view(id) {
        this.$router.push({path: `/view/${id}`})
      },

	  getListArticleCount(){
		  getListArticleCount(this.$store.state.token).then(data => {
			  this.ArticlesLength=data.data
		  }).catch(error => {

		  })
		  },
      getArticles() {
        let that = this
        that.loading = true
        getArticles(that.query, that.innerPage,this.$store.state.token).then(data => {
          that.articles= data.data
        }).catch(error => {
          if (error !== 'error') {
            that.$message({type: 'error', message: '文章加载失败!', showClose: true})
          }
        }).finally(() => {
          that.loading = false
        })
      },
		handleSizeChange(val) {

		  this.articles=[];
		  //当改变就回到第一页查询
		  this.innerPage.pageNumber=1;
		  this.currentPage=1;
		  this.innerPage.pageSize=val;
		  this.getArticles();
		},
		handleCurrentChange(val) {

		  this.articles=[];

		  this.innerPage.pageNumber=val;
		  this.getArticles()
		}
    },
    components: {
      'article-item': ArticleItem,
    }

  }


</script>

<style>

</style>
