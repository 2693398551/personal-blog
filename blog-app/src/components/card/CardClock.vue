<template>
	<div class="clock_main clock_main__header clock_main__body">
		<div class="clock_cls">
			<p class="clock_date">{{ date }}</p>
			<p class="clock_time">{{ time }}</p>
			<p class="clock_text">当前时间</p>
		</div>
	</div>
</template>

<script>
	
	export default {
	  name: 'cardMe',
	  data() {
	    return {
		date:'',
		time:'',
	    }
	  },
	mounted() {
		this.showClock();
		
	},
	methods: {
		
		showClock(){
			var sar=setInterval(this.updateTime, 1000);
			this.updateTime()
		},
		
	     updateTime() {
			var week = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
	        var cd = new Date();
	        this.time = this.zeroPadding(cd.getHours(), 2) + ':' + this.zeroPadding(cd.getMinutes(), 2) + ':' + this.zeroPadding(cd.getSeconds(), 2);
	        this.date = this.zeroPadding(cd.getFullYear(), 4) + '-' + this.zeroPadding(cd.getMonth()+1, 2) + '-' + this.zeroPadding(cd.getDate(), 2) + ' ' + week[cd.getDay()];
	    },
	    
	     zeroPadding(num, digit) {
	        var zero = '';
	        for(var i = 0; i < digit; i++) {
	            zero += '0';
	        }
	        return (zero + num).slice(-digit);
	    }
	}
	}
	
</script>

<style>

	.clock_main{
		border-radius:4px;border:1px solid #ebeef5;
		background-color:#fff;overflow:hidden;
		-webkit-box-shadow:0 2px 12px 0 rgba(0,0,0,.1);
		box-shadow:0 2px 12px 0 rgba(0,0,0,.1);
		color:#303133
	}
	.clock_main__header{
		padding:18px 20px;
		animation: clock_rotate 3s linear  infinite;
	}

	
	@keyframes clock_rotate {
		0% {
			border-top:5px solid #c055f5;
			border-right:5px solid #f5f551;
			border-bottom:5px solid #53f581;
		    border-left:5px solid #62f5df;
		    -webkit-box-sizing:border-box;
		    box-sizing:border-box;
		  }
		  25% {
		   border-top:5px solid #62f5df;
		   border-right:5px solid #c055f5;
		   border-bottom:5px solid #f5f551;
		   border-left:5px solid #53f581;
		    -webkit-box-sizing:border-box;
		    box-sizing:border-box;
		  }
		  50% {
		    border-top:5px solid #53f581;
		    border-right:5px solid #62f5df;
		    border-bottom:5px solid #c055f5;
		    border-left:5px solid #f5f551;
		    -webkit-box-sizing:border-box;
		    box-sizing:border-box;
		  }
		  75% {
		    border-top:5px solid #f5f551;
		    border-right:5px solid #53f581;
		    border-bottom:5px solid #62f5df;
		    border-left:5px solid #c055f5;
		    -webkit-box-sizing:border-box;
		    box-sizing:border-box;
		    
		  }
		  100% {
		    border-top:5px solid #c055f5;
		    border-right:5px solid #f5f551;
		    border-bottom:5px solid #53f581;
		    border-left:5px solid #62f5df;
		    -webkit-box-sizing:border-box;
		    box-sizing:border-box;
		  }

/* 		from {
			transform: rotate(0deg);
		}
		to {
			transform: rotate(360deg);
		} */
/* 		0% {
		transform: translateX(0);
		}
		100% {
		transform: translateX(100%);
		} */
	}
	.clock_main__body{
		margin-top: 20px;
		padding:0px
	}	
	
	p {
	    margin: 0;
	    padding: 0;
	}
	.clock_cls {
		background: radial-gradient(ellipse at center,  #0a2e38  0%, #000000 70%);
		height: 100%;
		border-color:red;
	    font-family: 'Share Tech Mono', monospace;
	    color: #ffffff;
	    text-align: center;
	    color: #daf6ff;
	    text-shadow: 0 0 0px rgba(10, 175, 230, 1),  0 0 0px rgba(10, 175, 230, 0);
		}
	    .clock_time {
	        letter-spacing: 0.05em;
	        font-size: 60px;
	        padding: 5px 0;
	    }
	    .clock_date {
	        letter-spacing: 0.1em;
	        font-size: 18px;
	    }
	    .clock_text {
	        letter-spacing: 0.1em;
	        font-size: 9px;
	        padding: 20px 0 0;
	    }
	
</style>
