<template>
	<div>
		<music></music>
		
	</div>
</template>

<script>
	import music from '@/components/music'
    export default {
        name: "BlogMusic",
        data(){
            return {
				
			}
        },
		mounted() {
			console.log("1233")
		},
		components:{
			music,
		},
		
    }
</script>

<style>
</style>
