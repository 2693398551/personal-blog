<template>
<div>
	<div id="percentageCounter"  :style="{'width':percentageCounter}"></div>
	<transition>
		<button class="btn-test" id="moblieGoTop" @click="toTop" >
			 <i class="el-icon-caret-top"></i>
		</button>
	</transition>
</div>
</template>

<script>
  export default {
    name: 'GoTop',
    data() {
      return {
        percentageCounter:0,
      }
    },
    methods: {
      toTop() {
		document.body.scrollTop =  0;
        document.documentElement.scrollTop = 0;
      },
      needToTop() {
		//距离顶部的高度
        let docHeight = document.documentElement.scrollTop;
		//网页全文的高度
		let bodyHeight=document.body.scrollHeight;
		//当前屏幕高度
		let winHeight=window.innerHeight ;
		
		let sum=bodyHeight-winHeight==0?100:bodyHeight-winHeight;
		if(sum==100){
			this.percentageCounter=0+"%";
		}else{
			this.percentageCounter=docHeight/sum*100+"%";
		}
		
		
		let moblieGoTop = document.querySelector("#moblieGoTop");
        if (docHeight > 0) {
		  moblieGoTop.style.transform="scale(1)"
        } else {
		  moblieGoTop.style.transform="scale(0)"
        }

      }
    },
    mounted() {
      /**
       * 等到整个视图都渲染完毕
       */
      this.$nextTick(function () {
        window.addEventListener('scroll', this.needToTop);
      });
    }
  }
</script>

<style>
  .me-to-top {
    background-color: #fff;
    position: fixed;
    right: 15px;
    bottom: 150px;
    width: 40px;
    height: 40px;
    border-radius: 20px;
    cursor: pointer;
    transition: .3s;
    box-shadow: 0 0 6px rgba(0, 0, 0, .12);
    z-index: 5;
  }

  .me-to-top i {
    color: #ff0000;
    display: block;
    line-height: 40px;
    text-align: center;
    font-size: 18px;
  }
	#moblieGoTop {
	    position: fixed;
	    bottom: 65px;
	    right: 10px;
	    font-size: 16px;
	    z-index: 99;
	    border: 0;
	    outline: 0;
	    box-shadow: 0 1px 30px -4px #e8e8e8;
	    background: rgba(255, 255, 255, 0.65);
	    color: #7D7D7D;
	    padding: 6px 16px;
	    border-radius: 10px;
	    transform: scale(0);
	    transition: all 0.8s ease !important;
	    border: 1px solid #FFFFFF;
		
	}
	#percentageCounter {
	    position: fixed;
	    z-index: 9999;
		top: 0;
	    left: 0;
	    height: 3px;
	    border-radius: 1.5px;
	    background: linear-gradient(to right, #4cd964);
	    transition: width 0.45s;
	}


	

</style>
