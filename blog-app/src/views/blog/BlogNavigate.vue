<template>
	<div>
		<div class="navigate-page">
			<navigate-page></navigate-page>
		</div>
		
	</div>
</template>

<script>
	import NavigatePage from '@/views/common/NavigatePage'
    export default {
        name: "BlogNavigate",
        data(){
            return {
				
			}
        },
		components:{
			NavigatePage,
		}
    }
</script>

<style>
	.navigate-page{
		padding-top: 80px;
		padding-left: 20px;
	}
</style>
