<template>
  <el-card class="el-card-me">
	  <!-- 头像 -->
	<div class="headPhoto"></div>
    <h1 class="me-author-name me-name-text" >{{this.$myName}}の</h1>
    <div class="me-author-description">
      <p class="pname phitokoto" v-if="this.hitokoto!=null||''" v-text="this.hitokoto"></p>
	  <p class="pname" v-if="this.from!=null||''" v-text="'出处:'+this.from"></p>
	  <p class="pname" v-if="this.from_who!=null||''" v-text="'作者:'+this.from_who"></p>
    </div>
    <div class="me-author-tool">
	  <a @click="showQQ"><img class="QQ" src="../../../static/img/qq.png"/></a>
	  <a @click="showQQ"><img class="QQ" src="../../../static/img/qq.png"/></a>
	  <a @click="showQQ"><img class="QQ" src="../../../static/img/qq.png"/></a>
	  <a @click="showQQ"><img class="QQ" src="../../../static/img/qq.png"/></a>
    </div>
  </el-card>

</template>

<script>
import axios from 'axios'
  export default {
    name: 'cardMe',
    data() {
      return {
        qq: {title: 'QQ', message: 'qq:2693398551'},
		hitokoto:'',//语句
		from:'',//出处
		from_who:'',//作者
      }
    },
	mounted() {
		this.showDazi()
	},
    methods: {
      showTool(tool) {
        this.$message({
          duration: 0,
          showClose: true,
          dangerouslyUseHTMLString: true,
          message: '<strong>' + tool.message + '</strong>'
        });
      },
		showQQ(){
			location.href="tencent://message/?uin=2693398551&Site=&Menu=yes";
		},
		showDazi(){
			let that=this;
			axios.get('https://v1.hitokoto.cn/')
			  .then(function (data) {
				  that.hitokoto=data.data.hitokoto;
				  that.from=data.data.from;
				  that.from_who=data.data.from_who;
			  })
			  .catch(function (error) {
			    console.log(error);
			  });
		}
    }
  }
</script>

<style scoped>
	

  .el-card-me{
		/* margin-top: 60px; */
	}
  .me-author-name {
    text-align: center;
    font-size: 30px;
    border-bottom: 1px solid #409EFF;
  }
  
  .me-name-text{
  	    border-right: 0.1em solid;
  	    width: 16.5em;
  	    width: 16ch;
  	    white-space: nowrap;
  	    overflow: hidden;
  	    animation: typing 3s steps(26, end),
  	        cursor-blink 0.5s step-end infinite alternate;
  	}
  	
  	@keyframes typing {
  	    from {
  	        width: 0;
  	    }
  	}
  	
  	@keyframes cursor-blink {
  	    50% {
  	        border-right-color: transparent;
  	    }
  	}

  .me-author-description {
    padding: 8px 0;
	text-align:center
  }

  .me-icon-job {
    padding-left: 16px;
  }

  .me-author-tool {
    text-align: center;
    padding-top: 10px;
  }

  .me-author-tool i {
    cursor: pointer;
    padding: 4px 10px;
    font-size: 30px;
  }
  .QQ{
	  width: 50px;
  }
  
  .headPhoto {
      width: 80px;
      height: 80px;
      background: url(../../../static/img/f.jpg) no-repeat;
      background-size: cover;
      border-radius: 50%;
      position: relative;
	  bottom: 15px;
      left: 56%;
      margin-left: -4rem;
      transition: all 0.3s;
  }
  
  .headPhoto:hover{
      transform: rotate(360deg);
  }
</style>
