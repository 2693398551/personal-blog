<template>
 <section v-title data-title="图片" class="sectionImg">
	<div class="card2" v-for="s in 10">
		<div class="box">
			<div class="imgBx">
				<img src="http://cos.myo.pub/cover10.jpg" alt="">
			</div>
			<div class="contentBx">
				<div>
					<h2>这是一个标题</h2>
					<p>这是一段描述</p>
				</div>
			</div>
		</div>
	</div>
</section>
</template>

<script>
    export default {
        name: "Picture",
        data(){
            return {}
        },
    }
</script>

<style>
 .sectionImg{
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    /*transform-style: preserve-3d;*/
    
}
section .card2{
    position: relative;
    width: 250px;
    height: 250px;
    margin: 20px;
    transform-style: preserve-3d;
    perspective: 1000px;
}
section .card2 .box{
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    transform-style: preserve-3d;
    transition: 1s ease;
}
section .card2:hover .box{
    transform: rotateY(180deg);
}
section .card2 .box .imgBx{
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}
section .card2 .box .imgBx img{
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
}
section .card2 .box .contentBx{
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: #ffb030;
    backface-visibility: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    transform-style: preserve-3d;
    transform: rotateY(180deg);
}
section .card2 .box .contentBx div{
    transform-style: preserve-3d;
    padding: 20px;
    background: linear-gradient(45deg,#aaffff,#aa55ff);
    transform: translateZ(100px);
}
section .card2 .box .contentBx div h2{
    color: #fff;
    font-size: 20px;
    letter-spacing: 1px;
}
section .card2 .box .contentBx div p{
    color: #fff;
    font-size: 16px;
} 
</style>
