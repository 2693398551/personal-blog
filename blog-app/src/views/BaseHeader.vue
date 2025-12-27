<template>
  <header class="site-header no-select" id="header" @mouseenter="ShowHeader" @mouseleave="HideHeader">
	  <div class="header-header">
		  <!--左边-->
		  <div class="header-left">
				<ul>
					<li class="header-left-li" @click="home"><i  class="el-icon-edit"></i>{{this.$myName}}<div class="XuanZhuanZi">の</div>Blog</li>
				</ul>
		  </div>
		  <!--右边-->
		  <div class="header-right  menu-transition menu-transition-start">

			    <div class="header-container">
			      <nav class="header-nav">
			        <ul>
			          <li class="header-right-li" @click="home">首页</li>
			          <li class="header-right-li" @click="archives">Archives</li>
			          <li class="header-right-li" @click="tag">标签</li>

			          <li class="header-right-li" @click="nav">导航</li>
			          <!-- <li class="header-right-li" @click="log">日志</li> -->
			          <li class="header-right-li" @click="write">文章</li>
			          <li class="header-right-li" @click="resume">关于</li>
			          <li class="header-right-li"  v-if="!user.login&&mac" @click="login">秘密社</li>
			          <li class="header-right-li"  v-if="user.login&&mac" @click="logout">退出</li>
			        </ul>
			      </nav>
			    </div>

		  </div>



	</div>




  </header>

</template>

<script>

import {getToken} from '@/request/token.js'
import {getMAC} from '@/api/article'
import $ from 'jquery'
  export default {
    name: 'BaseHeader',
    props: {
      activeIndex: String,
      simple: {
        type: Boolean,
        default: false
      }
    },
	mounted() {

		this.getMAC()
		/* window.addEventListener('scroll', this.handleScroll, true); */

	},
    data() {
      return {
		  drawer: false,
		  mac:false,
	  }
    },
    computed: {
      user() {
        let login = this.$store.state.account.length != 0
        let avatar = this.$store.state.avatar
		let id=this.$store.state.id
        return {
          login, avatar,id
        }
      },
    },
    methods: {
		open(){
			if ($('#mo-nav').is('.open')) {
				$("#mo-nav").addClass("yyc");
				$("#mo-nav").removeClass("open");
				//$(".unfold").css("display","block")

			}else{
				$("#mo-nav").removeClass("yyc");
				$("#mo-nav").addClass("open");
				//$(".unfold").css("display","none")

			}
		},
	  handleScroll() {
		  let top=document.documentElement.scrollTop;
		  if(top==0){
			$(".site-header").removeClass("yya");
			$(".header-right").removeClass("menu-transition");

		  }else{
			$(".site-header").addClass("yya");
			$(".header-right").addClass("menu-transition");
		  }
	  },
	  //只有固定mac的才可以有秘密社权限
		getMAC(){
			  getMAC().then((data => {
				this.mac=data.data
				console.debug('访问者:'+data.data)
			  })).catch(error => {
				  if (error !== 'error') {
					that.$message({type: 'error', message: 'mac!', showClose: true})
				  }
				})

		},
      logout() {
        let that = this
        this.$store.dispatch('logout').then(() => {
          this.$router.push({path: '/'})
        }).catch((error) => {
          if (error !== 'error') {
            that.$message({message: error, type: 'error', showClose: true});
          }
        })
      },
	  home(){
		this.$router.push({path: `/`})
	  },
	  archives(){
		  this.$router.push({path: `/archives`})
	  },
	  tag(){
		  this.$router.push({path: `/tag/all`})
	  },
	  nav(){
		  this.$router.push({path: `/nav`})
	  },
	  write(){
		  this.$router.push({path: `/write`})
	  },
	  resume(){
		  this.$router.push({path: `/Resume`})
	  },
	  log(){
		  this.$router.push({path: `/log`})
	  },
	  login(){

		  this.$router.push({path: `/login`})
	  },
	  ShowHeader(){
		 //悬浮
		 let top=document.documentElement.scrollTop;
		 if(top==0){$(".header-right").addClass("menu-transition");}

	  },
	  HideHeader(){
		 //悬浮离开
		 let top=document.documentElement.scrollTop;
		 if(top==0){
			 $(".header-right").removeClass("menu-transition");

		 }
	  }

    }
  }
</script>

<style>
@import url("https://at.alicdn.com/t/font_3295543_z56pra7uql.css");
.open {
	width: 50%;
	height: 100vh;
	background-color: #FFFFFF;
}
.yyc{
	display: none;
}
.nav-opne{
	display: none;
}
header {
	position: fixed;
	height: 60px;
	z-index: 1024;
	min-width: 100%;
	/* box-shadow: 0 2px 3px hsla(0, 0%, 7%, .1), 0 0 0 1px hsla(0, 0%, 7%, .1); */
}
.unfold{
	display: none;
}



.yya {
    position: fixed;
    left: 0;
    box-shadow: 0 1px 40px -8px rgb(0 0 0 / 50%);
	background: rgba(255, 255, 255, .95);
}



.site-header {
    width: 100%;
    height: 75px;
    -webkit-transition: all .4s ease;
    transition: all .4s ease;
    position: fixed;
    z-index: 1024;
    top: 0;
}
.site-header:hover{
	background:rgba(255, 255, 255, .95);
}
.no-select {
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    -khtml-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
}
/****/
.header-header{

}
.header-left{
	float: left;
	position: relative;
	height: 75px;
	line-height: 75px;
	margin-left: 40px;
}
.header-left-li{
	color: #647d98;
	list-style-type: none;
	cursor:pointer;
	font-size: 20px;
	font-family: 'Mochiy Pop One', sans-serif;
}

.site-name{
	font-size: 18px;
	margin-right: 30px;
}
.header-right{
	float: right;
	margin-right: 30px;
	position: relative;
	height: 75px;
	line-height: 75px;
}
.header-right-list{
	list-style-type: none;
}
.header-right-li{
	float:left;
	margin-right: 30px;
	font-size: 18px;
	cursor:pointer;
}
.menu-transition-start {
    transform: translateX(5%);
    opacity: 0;
    transition: all .5s;
}
.menu-transition {
    transform: translateX(0);
    opacity: 1;
    transition: all .5s;
}

.XuanZhuanZi{
	display: inline-block;
	 -webkit-transition-property: -webkit-transform;
	    -webkit-transition-duration: 1s;
	    -moz-transition-property: -moz-transform;
	    -moz-transition-duration: 1s;
	    -webkit-animation: rotate 3s linear infinite;
	    -moz-animation: rotate 3s linear infinite;
	    -o-animation: rotate 3s linear infinite;
	    animation: rotate 3s linear infinite;

}
/*导航栏  */
.header-nav {

}
.header-nav ul li {
  list-style: none;
  display: inline-block;

  font-size: 18px;
  font-weight: 500;
  color: #777;
  cursor: pointer;
  position: relative;
  z-index: 2;
  transition: color 0.5s;
}
.header-nav ul li::after {
  content: '';
  background: #f44566;
  width: 100%;
  height: 60%;
  border-radius: 30px;
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: -1;
  opacity: 0;
  transition: top 0.5s,opacity 0.5s;
}
.header-nav ul li:hover{
  color: #fff;
}
.header-nav ul li:hover::after{
  top: 50%;
  opacity: 1;
}

/*  */
@-webkit-keyframes rotate{from{-webkit-transform: rotate(0deg)}
    to{-webkit-transform: rotate(360deg)}
}
@-moz-keyframes rotate{from{-moz-transform: rotate(0deg)}
    to{-moz-transform: rotate(359deg)}
}
@-o-keyframes rotate{from{-o-transform: rotate(0deg)}
    to{-o-transform: rotate(359deg)}
}
@keyframes rotate{from{transform: rotate(0deg)}
    to{transform: rotate(359deg)}
}

@media only screen and (max-width:800px) {

	.header-left{
		float: right;
		margin-right: 40px;
		margin-left: 0px;

	}
	.header-right{
		float: left;
		position: relative;
		font-weight: 20;

	}
	.header-right-li{
		cursor:pointer;
	}
	.el-icon-s-unfold{
		font-size: 35px;
	}
	.header-left-li{
		font-size: 18px;
	}

	.unfold{
		display:block ;
		float: left;
		line-height: 75px;
	}
	.el-button--primary{
		background-color: #000000!important;
	}
	.el-button--primary:hover{
		/* background-color: #aaff7f!important; */
	}
	.el-button--primary:active{
		background-color: #ffaa7f!important;
	}

}
@media only screen and (max-width:300px) {
	.header-left{
		display: none;
	}


}






</style>
