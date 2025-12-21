<template>
	<div>
		<div class="si1">
			手机号 <el-input  type="number" v-model="mobile" placeholder="手机号"></el-input>
			金额	<el-input type="number" v-model="customized_price" placeholder="金额"></el-input>
			<el-button @click="as"> 按钮</el-button>
		</div>
	</div>
</template>

<script>
	import axios from 'axios'
	export default {
	  name: 'index',
	  data() {
	    return {
			mobile: '18244861913',
			customized_price:'',
			diamond_id:'666666',
			source:'5',
			way:'0',
			aid:'1128',
			open_id:'',
			fp:'verify_kwo95axu_ZwXiLOvz_jt6N_4f2P_9mJb_TJ9MmZO7orJm',
			platform:'iphone',
			extra:'%7B%22domin_type%22:1%7D',
			guide_source:'false',
	    }
	  },
	  methods:{
		  as(){
			console.log("按钮")
			this.testget()

			
		  },
		  testget(){
			  console.log("按钮2")
			      axios.get('https://www.baidu.com/').then(response => {
			          console.log('ad')
			      })
		  },
	  },
	  mounted() {
	  	console.log("1")
	  }
	}
</script>

<style>
	.si1{
		margin-top: 80px;
	}
	
</style>
