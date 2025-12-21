<template>
<!---->
<div ref="article" class="article-list-thumb-list">
<div class="article-list-thumb">
	<div class="post-list-thumb" :style="{'flex-direction':index%2!=0?'row-reverse':''}">
		<!-- :style="{'flex-direction':index%2!=0?'row-reverse':'row'}" -->
		<div class="post-thumb" @click="view(id)" >
				<div class="focus-img">
					<img class="lszy"  :src="cover">
					<p>sss</p>
					</img>
				</div>
		</div>
		<div class="post-thumb-content":style=
		"{
			
		}">
			<div class="post-content-wrap" :style=
		"{
			'text-align':index%2==0?'right':'left',
		}">
				<div class="post-content">
						<div class="me-artile-date">
							<i class="iconfont icon-31shijian"></i>&nbsp;{{createDate | format}}
						</div>
						<div class="me-article-title">
							<h3 @click="view(id)">{{title}}</h3>
						</div>

						<div class="main-food">
							<div class="main-food-item">
								
								<i class="iconfont icon-liulan"></i>&nbsp;<span>{{viewCounts}}</span><i class="iconfont icon-centigrade"></i>
								
								<i class="iconfont icon-biaoqian food-item"></i>&nbsp;
									<span v-for="(t,index) in tags" :key="index" size="mini" type="success" class="tagName">
										<el-link :underline="false"  type="success" v-if="1==1">
										{{t.tagName}}
										</el-link>
									</span>
								
								<i class="iconfont icon-jiqiren_o food-item"></i>&nbsp;{{author}}
							</div>
						</div>
						
						<div class="float-content">
							<p class="me-artile-summary">
								{{summary}}
							</p>
							
						</div>
					
				</div>
			</div>
		</div>
	</div>
</div>
</div>	
<!---->

</template>

<script>

</script>

<script>
  import { formatTime } from "@/utils/time";
  import {getToken} from '@/request/token.js'
  import $ from 'jquery'
  export default {
    name: 'ArticleItem',
	index:'index',
    props: {
      id: String,
      weight: Number,
      title: String,
      commentCounts: Number,
      viewCounts: Number,//阅读数
      summary: String,
      author: String,
      tags: Array,
      createDate: String,
	  cover: String,
	  viewKeys:String,
	  index:Number,
    },
	mounted() {
		
		 this.loads();
	},
    data() {
      return {
		img:"http://cos.myo.pub/007.jpg",
		lengths:0,
		token:undefined,
	  }
    },
    methods: {
		loads(){
			this.ArticleShow()
			
			
		},
	  ArticleShow(){
		  
	  },
      view(id) {
        this.$router.push({path: `/view/${id}`})
      },
	  Imgview(index){
		  return "http://cos.myo.pub/cover"+index+".jpg"
	  },
	  textalign(index){
		  if(index%2!=0){
			  return "right"
		  }
		  return "left"
	  }
    }

  }
</script>

<style>
* {
	box-sizing: border-box;
}

.article-list-thumb-list{
	max-width: 780px;
	margin: 0 auto;
	margin-bottom:40px;
	
}
.article-list-thumb{
	background-color: #ffffff;;
	opacity: 1; 
}

.article-list-thumb-show{
	opacity: 1; 
	transition: all 0.8s ease !important;
}

 .article-list-thumb-list:nth-of-type(1){
	border-top: 1px solid #67C23A;
}
.post-list-thumb{
	display: flex;
	flex-direction: row
}

.post-thumb {
	overflow: hidden;
	flex: 55;
	
}

.post-thumb-content{
	overflow: hidden;
	flex: 45;
}
.lszy{
	
}
.lszy:hover{
	transform: scale(1.1);
}

.post-content-wrap{
	padding-top: 20px;
	width: 88%;
	margin: 0 auto;
	
}
.post-content{
	
}
.main-food{
	
}

.float-content{
	position: relative;
	right: 0;
	margin: 0;
	margin-top: 10px;
	z-index: 50;
	color: rgba(0, 0, 0, .66);
	transition: all 0.8s ease !important;
}

.me-artile-date{
		background-color: #00ff8226;
	    font-size: 15px;
	    width: max-content;
	    padding: 4px 10px 4px 10px;
	    border-radius: 6px;
	    color: #67C23A;
	    white-space: nowrap;
	    font-weight: 420;
	    transition: all 0.8s ease !important;
}
.me-article-title {
	
}
.me-article-title h3{
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
	font-size: 20px;
	word-wrap: break-word;
	cursor:pointer ;
}
.me-artile-summary{
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
	overflow: hidden;
}


.focus-img{
	height: 300px;
	position: relative;
	display: block;
	background-repeat: no-repeat;
	background-size: cover;
	overflow: hidden;
}

.lszy{
	width: 100%;
	height: 100%;
	object-fit: cover;
	transition: all .6s;
}



@media only screen and (max-width:800px) {
	.post-thumb{
		
		
	}
	.post-thumb-content{
		min-height: 220px;
		
	}

	.post-list-thumb{
		flex-direction: column!important;
	}
	.post-content-wrap {
	    width: 95%;
		text-align: left!important;
	}
	.focus-img {
		height: 210px;
	}
	
	
	
}
</style>
