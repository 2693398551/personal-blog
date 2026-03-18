<template>
  <div id="app">
	  <!-- 需要缓存的视图组件 -->
<!--     <keep-alive>
        <router-view v-if="$route.meta.keepAlive"></router-view>
     </keep-alive> -->
        <!-- 不需要缓存的视图组件 !$route.meta.keepAlive-->
        <router-view ></router-view>
         <go-top></go-top>


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
    // 页面一加载，立刻执行 INIT_GUEST
    // 如果 LocalStorage 里有数据，Vuex 就会立刻被填满
    this.$store.commit('INIT_GUEST');
    // 网站初始化时获取全局配置信息
    this.$store.dispatch('getWebInfo')

  },
  mounted(){

  },
	methods:{


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

#app {
/*  background-image: url('../static/img/anime-sunset-art-wallpaper-2560x1080_14.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed; !* 固定背景，不隨內容滾動 *!
  background-repeat: no-repeat;*/
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
