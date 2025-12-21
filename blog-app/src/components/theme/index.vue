<template>
	<div>
		<div>
			<button class="btn-test" id="changskin" @click="test()" >
				<i class="el-icon-setting"></i>
			</button>
		</div>
		<div class="skin-menu no-select" :class="show">
			<div class="footer-shuo"></div>
			<div class="theme-controls row-container">
				<div class="radio-input">
					  <input value="value-1" name="value-radio" id="value-1" type="radio">
					  <label class="theme-label" for="value-1">光亮</label>
				
					  <input value="value-2" name="value-radio" id="value-2" type="radio">
					  <label class="theme-label" for="value-2">暗黑</label>
				  
					  <input value="value-3"  name="value-radio" id="value-3" type="radio">
					  <label class="theme-label" for="value-3">置灰</label>
				  
					  <input value="value-4" name="value-radio" id="value-4" type="radio">
					  <label class="theme-label" for="value-4">初音</label>
				</div>
					

				
			</div>

		</div>
	</div>
</template>

<script>
	import {setAllGray} from '@/utils/new_file.js'
	export default {
		name: "index",
		data() {
		  return {
			  conut:0,
			  show:"",
		  }
		},
		mounted() {
			/**
			 * 等到整个视图都渲染完毕
			 */
			this.$nextTick(function () {
			  window.addEventListener('scroll', this.needToTop);
			});
		},
		methods:{
			test(){

				let el = document.querySelector('body')
				let name = '';
				if(this.conut%2==0){
					name="dark-theme"
					this.show="show"
				}else{
					name="light-theme"
					this.show=""
				}
				this.conut++
				el.className = [name].join(' ')
				/* console.log(el) */
				
			},
			needToTop() {
			  let curHeight = document.documentElement.scrollTop || document.body.scrollTop;
					var changskin = document.querySelector("#changskin");
					
			  if (curHeight > 0) {
					  changskin.style.transform="scale(1)"
			  } else {
					  changskin.style.transform="scale(0)"
			  }
			
			}
		}
		
	}
</script>

<style>
	#changskin {
	    position: fixed;
	    bottom: 15px;
	    right: 10px;
	    z-index: 99;
	    border: 0;
	    outline: 0;
	    box-shadow: 0 1px 30px -4px #e8e8e8;
	    background: rgba(255, 255, 255, 0.65);
	    color: #7D7D7D;
	    padding: 15px;
	    border-radius: 10px;
	    transform: scale(0);
	    transition: all 0.8s ease !important;
	    border: 1px solid #FFFFFF;
	}
	.el-icon-setting{
		font-size: 20px;
		-webkit-transition-property: -webkit-transform;
		-webkit-transition-duration: 1s;
		-moz-transition-property: -moz-transform;
		-moz-transition-duration: 1s;
		-webkit-animation: rotate 3s linear infinite;
		-moz-animation: rotate 3s linear infinite;
		-o-animation: rotate 3s linear infinite;
		animation: rotate 3s linear infinite;
	}
	.btn-test{
		cursor: pointer;
	}
	.skin-menu {
	    position: fixed;
	    left: auto;
	    bottom: 15px;
	    height: auto;
	    width: auto;
		
	    padding: 16px;
	    padding-bottom: 0;
	    border-radius: var(--style_menu_selection_radius, 15px);
	    font-size: larger;
	    text-align: center;
	    box-shadow: none;
	    display: flex;
	    flex-direction: row-reverse;
	    justify-content: space-between;
	    flex-direction: column;
	    z-index: 99;
	    transform: translateY(100%);
	    transition: transform 0.4s ease-in-out;
	    right: 50px;
	    max-height: 85%;
	    overflow-y: auto;
	}
	.skin-menu.show {
	    transform: translateY(0);
	    transition: transform 0.4s ease-in-out;
	    right: 50px;
	}
	.skin-menu .theme-controls{
		background-color: white;
	}
	.skin-menu .row-container {
		
	    height: auto;
	    margin: 0;
	    padding: 0;
	    text-align: center;
	    flex-grow: 1;
	    display: flex;
	    flex-direction: row-reverse;
	}


	
	
	/* 开关 */
	.radio-input {
	  display: flex;
	  flex-direction: row;
	  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
	  font-size: 16px;
	  font-weight: 600;
	  color: white;
	  
	}
	
	.radio-input input[type="radio"] {
	  display: none;
	}
	
	.radio-input .theme-label {
	  display: flex;
	  align-items: center;
	  padding: 10px;
	  border: 1px solid #ccc;
	  background-color: #212121;
	  border-radius: 5px;
	  margin-right: 12px;
	  cursor: pointer;
	  position: relative;
	  transition: all 0.3s ease-in-out;
	}
	/* 选择的theme-label是连续的可以 用+ 号，不连续的必须用 ~ */
	.radio-input .theme-label:not(.theme-label~.theme-label){
	  margin-left: 12px;
	}
	
	.radio-input label:before {
	  content: "";
	  display: block;
	  position: absolute;
	  top: 50%;
	  left: 0;
	  transform: translate(-50%, -50%);
	  width: 10px;
	  height: 10px;
	  border-radius: 50%;
	  background-color: #fff;
	  border: 2px solid #ccc;
	  transition: all 0.3s ease-in-out;
	}
	
	.radio-input input[type="radio"]:checked + label:before {
	  background-color: green;
	  top: 0;
	}
	
	.radio-input input[type="radio"]:checked + label {
	  background-color: royalblue;
	  color: #fff;
	  border-color: rgb(129, 235, 129);
	  animation: radio-translate 0.5s ease-in-out;
	}
	
	@keyframes radio-translate {
	  0% {
	    transform: translateX(0);
	  }
	
	  50% {
	    transform: translateY(-10px);
	  }
	
	  100% {
	    transform: translateX(0);
	  }
	}

	/* 开关end */
</style>
