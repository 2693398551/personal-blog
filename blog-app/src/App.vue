<template>
  <div id="app">
	  <!-- 需要缓存的视图组件 --> 
<!--     <keep-alive>
        <router-view v-if="$route.meta.keepAlive"></router-view>
     </keep-alive> -->
        <!-- 不需要缓存的视图组件 !$route.meta.keepAlive-->
        <router-view ></router-view>
		<!-- <apayer></apayer> -->
    <go-top></go-top>
	<theme></theme>
  </div>
</template>

<script>
import GoTop from '@/components/gotop/GoTop'
import apayer from '@/components/apayer'
import theme from '@/components/theme'
export default {
  name: 'App',
  components: { GoTop,apayer ,theme},
  created () {
	  let scrollTop=localStorage.getItem("scrollTop");
/* 	  setTimeout(() => {
      window.L2Dwidget.init({
        pluginRootPath: '../static/live2d/',//插件在站点上的根目录(相对路径)
        pluginJsPath: 'lib/', //脚本文件相对与插件根目录路径
        pluginModelPath: 'model/histoire/',//模型文件相对与插件根目录路径
        tagMode: false,//标签模式, 是否仅替换 live2d tag标签而非插入到所有页面中
        debug: false,//调试, 是否在控制台输出日志
        model: { 
			jsonPath: '../static/live2dw/model/histoire/model.json' ,//主文件路径
		},
        display: { position: 'left', width: 150, height: 250 ,hOffset:30,},
        mobile: { show: true },
        log: false,
		dialog:{
		    enable:true,//显示人物对话框
		    //hitokoto:true,//使用一言API 
		  },
      })
    }, 1000) */
  },
  mounted(){
	
  },
	methods:{
		bgimage(){
			
		}
		
	}
}
</script>
	


<style>
* { margin: 0; padding: 0; }
	::-webkit-scrollbar {
	    width: 10px;
	    height: 10px;
	    background-color: #eee;
	}
	::-webkit-scrollbar-thumb {
	    background-color: #69d2e7;
	}
	
	::-moz-selection{background:#444444;color: #FFFFFF;}
	::selection {background:#444444;color: #FFFFFF;}
	code::-moz-selection {background:#444444;color: #FFFFFF;}
	code::selection {background:#444444;color: #FFFFFF;}
body {
	
	background-color: #f5f5f5;
	font-weight: 400;
	font-family: "Helvetica Neue",Helvetica,"PingFang SC","Hiragino Sans GB","Microsoft YaHei","微软雅黑",Arial,sans-serif;
	line-height: 1.5;
	/*background-image: url(../static/Anime-Wallpaper-master/96.jpg);
	background-repeat :no-repeat; */
	/* background-size:100% 100%;
	-moz-background-size:100% 100%; */
	
}

a {
    cursor: pointer;
    text-decoration: none;
    transition: none 86ms ease-out;
}
a:hover {
	color: #00aaff;
}
.me-area{
	background-color: #fff;
}
html{height:100%;}
body{min-height:100%;}
body{position:relative;}

.me-pull-right {
	float: right;
}
</style>
