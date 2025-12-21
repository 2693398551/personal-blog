<template>
	<div>
			<carousel></carousel>
			<picture1></picture1>
	</div>
</template>

<script>
	import Picture from '../../components/waterfall/Picture'
	import Carousel from '../../components/waterfall'
	export default {
	  name: 'blogGallery',
	  components: {
	    'picture1': Picture,
		'carousel':Carousel,
		
	  }
	  }
</script>

<style>

	
	
</style>
