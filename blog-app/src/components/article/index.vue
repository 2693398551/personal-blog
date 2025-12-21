<template>
<!--归档-->
<div>
		<vue-timeline-update
			:date="date"
			:title="title"
			:description="summary"
			:thumbnail="cover"
			category="announcement"
			icon="code"
			:color="color"
			@click:title="view(id)"
			/>
</div>
<!---->

</template>



<script>
  import { formatTime } from "../../utils/time";
  import $ from 'jquery'
  export default {
    name: 'index',
	index:'index',
	archives:'archives',
    props: {
      id: String,
      weight: Number,
      title: String,
      commentCounts: Number,
      viewCounts: Number,//阅读数
      summary: String,
      author: String,
      tags: Array,
      createDate: String,
	  cover: String,
	  index:Number,
	  archives:String,
    },
	mounted() {
		
		 /* $(".article").eq(0).css("margin-top","60px") */
	},
    data() {
      return {
		img:"http://cos.myo.pub/007.jpg",
		lengths:0,
	  }
    },
	computed: {
	  date(){
	    return new Date(this.createDate)
	  },
		color(){
			let color=["black", "blue", "green", "orange", "purple", "red", "turquoise", "white"];
			let i=Math.floor(Math.random()*color.length);
			return color[i]
		}
	},
    methods: {
      view(id) {
        this.$router.push({path: `/view/${id}`})
      },
	  Imgview(index){
		  return "http://cos.myo.pub/cover"+index+".jpg"
	  },
    }

  }
</script>

<style>
.gb-vue-timeline-update__right{
	max-width: 40%!important;
}
.gb-vue-timeline-update__description{
	 display: -webkit-box; /* （兼容性） */
	 -webkit-line-clamp:3;/* （指定显示行数） */
	 -webkit-box-orient: vertical;/* （文本垂直排列） */
	 overflow: hidden; /* （多余部分隐藏） */
}
.gb-vue-timeline-update__right{
	overflow: hidden; 
}
.gb-vue-timeline-update__title{
	 display: -webkit-box; /* （兼容性） */
	-webkit-line-clamp:2;/* （指定显示行数） */
	-webkit-box-orient: vertical;/* （文本垂直排列） */
	overflow: hidden; /* （多余部分隐藏） */
}







@media only screen and (max-width:800px) {
	
	
}
</style>
