<template>
	<div data-title="归档">
		<div class="archives-content">
			<div class="archives-content-main">
				<articlescrollpage></articlescrollpage>
			</div>
		</div>
	</div>
	<!--  -->


</template>

<script>
  import articlescrollpage from '@/views/common'
  import {listArchives} from '@/api/article'
  import $ from 'jquery'

  export default {
    name: "BlogArchive",
    components: {
      articlescrollpage
    },
    
  }
</script>

<style >
.archives-content{
	
}
.archives-content-main{
	padding-top: 80px;
}
</style>
