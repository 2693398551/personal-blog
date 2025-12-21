<template>
	<div class="notice">
	  <el-alert
	    title="网站提示:"
		:description="label"
	    type="success"
		
	    :closable="true"
		close-text="点击关闭"
		>
	  </el-alert>
	</div>
</template>

<script>
	export default {
	  name: 'MainTitle',
	  data() {
		  return{
			  type:'danger',
			  label:'网站已启用加密，部分文章对于未登录不可见...',
		  }
	  }
	  
	  }
</script>

<style>
.notice {
	max-width: 780px;
	margin: 0 auto;
	margin-bottom: 8px;
}
.notice-label{
	    display: -webkit-box;
	    -webkit-line-clamp: 1;
	    -webkit-box-orient: vertical;
	    overflow: hidden;
}
</style>
