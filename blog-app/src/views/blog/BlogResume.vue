<template>

<div v-title data-title="关于我" >
<div class="site-content">
	<div class="main-content">
		<div>
			<main class="site-main">
				<div class="moe-mashiro">
					<div class="meta-title">关于 | About</div>
					<div class="markdown-body ">
						<p>Hi，我是{{this.$myName}}，这里是我的主页！</p>

						<p >很荣幸您能访问我的网站！</p>

						<p>这里收集了个人认为日常实用的代码， 个人所学知识的复习资料， 网上收集的实用代码以及个人感兴趣的各种知识记录。 以下为我给自己设立的要求：</p>

						<p>1.记录日常复习中带有重复利用价值的代码及介绍</p>
						<p>2.记录日常突发奇想的算法及想法</p>
						<p>3.记录自学过程中的代码及介绍</p>
						<p>4.记录解决难题过程中的代码及介绍</p>
						<p>5.积极生产小工具和复用性极高的代码以形成自己的个人代码库</p>
						<p>6.以上要求做不到的话， 就随便写点什么吧_(:з」∠)_</p>
						<p>从前的我没有把知识记录下来的习惯， 希望现在可以通过我的个人网站来养成这种习惯。</p>
						<div class="About-title">Information about the website / 有关网站的信息</div>
						<p>本站创立于2022年4月</p>
						<p>当前网站只有首页做了响应式,毕竟前端样式也不是咱们后端工程师专门弄得,自学了一点皮毛,弄得样式可能不太好看后面可能会改QAQ</p>
						<div class="About-title">Information About Me / 关于我的信息</div>
						<p>昵称： {{this.$myName}}</p>
						<p>目前职业：  码农</p>
						<p>爱好： 看书、 玩游戏、 看动漫、 躺着、 敲代码</p>
						<div class="Contact-title">Contact / 联系方式</div>
						<p>QQ: 2693398551</p>
						<p>Email: liange2693398551@gmail.com</p>

					</div>
				</div>
				<div class="popcontainer"></div>
			</main>
		</div>
	</div>
</div>
</div>

</template>

<script>
	export default {
	  name: 'BlogResume',
	  }

</script>


<style>

.site-content{
	margin: 0 auto ;
	border-radius: 8px;
	background-color: rgba(255,255,255,.8);
}
.main-content{
	max-width: 800px;
	height: auto;
	padding: 0 10px;
	margin-left: auto;
	margin-right: auto;
	background-color: rgba(255, 255, 255, .8);
	border-radius: 8px;
	}
.site-main{

	padding: 30px ;

}
.moe-mashiro{
	width: 100%;
}
.markdown-body p{
	margin-bottom: 10px;
}
.About-title{
	text-align: center;
	font-size: 2rem;
	padding-bottom: 1rem;
}
.Contact-title{
	text-align: center;
	font-size: 1.5rem;
	padding-bottom: 1rem;
}













/*打字*/
@keyframes typing {
    from {
        width: 0;
    }
}
/*打字end*/
.meBox-title p {
    font-size: 1.2rem;
    border-right: .1em solid;
    width: 12ch;
    white-space: nowrap;
    overflow: hidden;
    animation: typing 2s steps(20, end), blink-caret .3s step-end infinite alternate;
    margin: -30px 0 0 90px;
    transition: all 0.3s;
}
.meta-title{
	text-align: center;
	font-size: 3rem;
	    line-height: 1.5;
	    padding-bottom: 1rem;
}












</style>
