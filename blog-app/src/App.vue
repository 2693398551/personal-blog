<template>
  <div id="app">
	  <!-- 需要缓存的视图组件 -->
<!--     <keep-alive>
        <router-view v-if="$route.meta.keepAlive"></router-view>
     </keep-alive> -->
        <!-- 不需要缓存的视图组件 !$route.meta.keepAlive-->
        <router-view ></router-view>
    <go-top></go-top>
	<theme></theme>

  </div>
</template>

<script>
import GoTop from '@/components/gotop/GoTop'

import theme from '@/components/theme'

export default {
  name: 'App',
  components: { GoTop ,theme},
  created () {
	  let scrollTop=localStorage.getItem("scrollTop");

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


}

a {
    cursor: pointer;
    text-decoration: none;
    transition: none 86ms ease-out;
}
a:hover {
	color: #00aaff;
}


</style>
