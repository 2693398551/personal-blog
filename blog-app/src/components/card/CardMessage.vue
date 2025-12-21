<template>
  <el-card :body-style="{ padding: '8px 18px' }">
    <div slot="header" class="me-message-header">
      <span><i class="iconfont icon-biaoqian"></i>{{cardHeader}}</span>
    </div>
	<div class="webinfo">
		<div class="webinfo-item">
			<div>文章数目:</div>
			<div>null</div>
		</div>
		<div class="webinfo-item">
			<div>已运行时间:</div>
			<div>null</div>
		</div>
		<div class="webinfo-item">
			<div>本站访客数：</div>
			<div>null</div>
		</div>
		<div class="webinfo-item">
			<div>本站总访问量：</div>
			<div>null</div>
		</div>
		<div class="webinfo-item">
			<div>最后活动时间：</div>
			<div>null</div>
		</div>
	</div>
 
  </el-card>
</template>
<script>

  export default {
    name: "CardMessage",
    props: {
      cardHeader: {
        type: String,
        required: true
      },
	  webinfo:{
		  type: Array,
		  required: true
	  }
    },
	mounted() {
		this.showIn();
	},
    methods: {
      showIn(){

	  }

    }
  }
</script>

<style scoped>
	
.webinfo .webinfo-item {
    display: -webkit-box;
    display: -moz-box;
    display: block;
    padding: 4px 0 0;
}
.webinfo .webinfo-item div:first-child {
    display: -webkit-box;
    display: -moz-box;
    display: inline-block;
}
.webinfo .webinfo-item div:last-child {
    display: -webkit-box;
    display: -moz-box;
    display: inline-block;
    float: right;
}
  


</style>
