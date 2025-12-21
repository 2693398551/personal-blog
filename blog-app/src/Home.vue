<template>
  <div id="home">

		<base-header :activeIndex="activeIndex" class="me-header " @func="showdeng"></base-header>

		<headertop class="header-top" v-if="$route.meta.requireShow"></headertop>
		<div class="layou-content">
			<router-view/>
		</div>
		
		<base-footer v-if="this.$router.currentRoute.path=='/'"></base-footer> 
		
  </div>
</template>




<script>
	
import BaseFooter from '@/components/BaseFooter'
import BaseHeader from '@/views/BaseHeader'
import headertop from '@/components/page'
import $ from 'jquery'
/* import apayer from '@/components/apayer' */
export default {
  name: 'Home',
  data (){
  	return {
  			activeIndex: '/',
  			footerShow:true
  	}
  },
  components:{
  	'base-header':BaseHeader,
  	'base-footer':BaseFooter,
	headertop,

	 
  },
  mounted() {
  	
  },
  beforeRouteEnter (to, from, next){
  	 next(vm => {
    	vm.activeIndex = to.path
  	})
  },
  beforeRouteUpdate (to, from, next) {
	  if(to.path == '/'){
	  	this.footerShow = true
	  }else{
	  	this.footerShow = false
	  }
	  this.activeIndex = to.path
	  next()
	},
	methods:{
		showdeng(data){
			if(data%2==0){
				//关
				$('.my-container').addClass('dark-theme');
			}else{
				
				$('.my-container').removeClass('dark-theme');
			}
		}
	}
}
</script>

<style>
	
	

	
	
	



	
body{
	background: url("http://cos.myo.pub/BgImage/bg1.jpg");
	background-repeat :no-repeat; 
	background-position: center;
	background-size:cover;
	background-attachment: fixed;
	
	
}
:root {
  --main-bg: brown;
}

.layou-content{
	min-height: 100vh;
	background-color: var(--myo-bg);
	transition: margin .8s, background .8s;
}

html{
	transition: background .8s;
}

html,body{
	scroll-behavior: smooth; /*平滑的过渡 */ 
	
}



/* 光亮主题 */
.light-theme{
	--myo-bg:rgba(255, 255, 255, .5);
	
}
/* 暗黑主题 */
.dark-theme{
	--myo-bg:rgba(0, 0, 0, .5);
}




 	@media only screen and (max-width:800px) {
		.my-container{
			overflow: hidden;
		}
	} 






</style>
