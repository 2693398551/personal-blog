<template>
  <div class="me-area">
    <div class="me-footer">
      <span class="my-food">Designed by :
        <strong>
          <span class="me-login-design-color" @click="resume">{{this.$myName}}</span>
		  <div class="hh"></div>
			<!-- <a @click="tiaole" class="beian">湘ICP备2022004529号</a> -->
			<a href="https://beian.miit.gov.cn/">湘ICP备2022004529号</a>
        </strong>
      </span>
    </div>
  </div>

</template>

<script>

  export default {
    name: 'BaseFooter',
    data() {
      return {}
    },
    methods: {
		tiaole(){
			window.location.href = 'https://beian.miit.gov.cn/'; 
		},
		resume(){
			this.$router.push({path: `/Resume`})
		}
	},
    mounted() {
    }
  }
</script>

<style scoped>
	.my-food{
		font-family: "Lucida Calligraphy", cursive, serif, sans-serif;
	}

  .el-footer {
	position: relative;
	bottom: 0px!important;
    min-width: 100%;
    box-shadow: 0 -2px 3px hsla(0, 0%, 7%, .1), 0 0 0 1px hsla(0, 0%, 7%, .1);
    bottom: 0;
    left: 0;
    z-index: 1024;
  }

  .me-footer {
    text-align: center;
    line-height: 60px;
    font-size: 18px;

  }

  .me-login-design-color {
    color: #4de8e8 !important;
	cursor:pointer;
  }
  .hh{
	  position: relative;
	  top: 4px;
	  display: inline-block;
	  background-color: #000000;
	  width: 3px;
	  height: 20px;
  }
  .beian{
	  color: #339a9a;
	  font-size: 18px;

	  
  }

</style>
