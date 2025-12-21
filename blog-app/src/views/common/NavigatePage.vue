<template>
	<div>
		<navigation v-for="(l,index) in links" :key="l.id" v-bind="l" :index="index"></navigation>
	</div>
</template>

<script>
	import navigation from '@/components/navigation/navigation'
	import {getLink} from '@/api/link'
    export default {
        name: "NavigatePage",
        data(){
            return {
				links:[]
			}
        },
		mounted() {
			this.load()
		},
		methods:{
			load(){
				this.getLink()
			},
			getLink(){
				let that=this;
				getLink().then(data => {
					that.links=data.data;
					console.log(data.data)
					
				})
			}
		},
		components:{
			navigation,
		}
    }
</script>

<style>


</style>
