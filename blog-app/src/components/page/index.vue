<template>
<div>
	<div class="topbg">
		<div class="head-guide">
			<div class="guide-info">
				<h2 class="guide_title">{{this.$myName}}</h2>
			</div>
			<div class="guide-text-ico">
				<h2 class="guide_text">
				<div class=" container-text">
	
						<div class="my-text1"></div>

				</div>
				</h2>
				<div class="guide_ico">
					<ul>
						<li><a @click="showQQ" class="ico-a"><img src="../../../static/user/qq.png"/></a></li>
						<li><a class="ico-a" @mouseenter="ShowWechat" @mouseleave="HideWechat"><img src="../../../static/user/wechat.png"/></a></li>
						<li><a @click="showBiliBili" class="ico-a"><img src="../../../static/user/bilibili.png"/></a></li>
						<li><a @click="showMusic" class="ico-a"><img src="../../../static/user/wangyiyun.png"/></a></li>
					</ul>
				</div>
			</div>
			<div class="sanjioxing">
				<div class="tttt"></div>
			</div>
			<div class="Wechat">
				<div class="ShowWechat Wechat-transition-start">
					<img src="../../../static/user/mywx.png"/>
				</div>
			</div>
		</div>
	</div>
</div>
</template>
<script>
import $ from 'jquery'
	export default {
		name: "index",
		mounted() {
			this.loadDazi()
		},
		methods:{
			showQQ(){
				location.href="tencent://message/?uin=2693398551&Site=&Menu=yes";
			},
			showBiliBili(){
				window.open("https://space.bilibili.com/36932814","_blank")
				
			},
			showMusic(){
				window.open("https://music.163.com/#/user/home?id=342473756","_blank")
				
			},
			ShowWechat(){
				//悬浮
				//$(".ShowWechat").slideDown("slow");
				$(".ShowWechat").addClass("Wechat-transition");
			},
			HideWechat(){
				//悬浮离开
				//$(".ShowWechat").slideUp("slow");
				$(".ShowWechat").removeClass("Wechat-transition");
			},
			loadDazi(){
				class TextScramble {
				  constructor(el) {
				    this.el = el
				    this.chars = '!<>-_\\/[]{}—=+*^?#________'
				    this.update = this.update.bind(this)
				  }
				  setText(newText) {
				    const oldText = this.el.innerText
				    const length = Math.max(oldText.length, newText.length)
				    const promise = new Promise((resolve) => this.resolve = resolve)
				    this.queue = []
				    for (let i = 0; i < length; i++) {
				      const from = oldText[i] || ''
				      const to = newText[i] || ''
				      const start = Math.floor(Math.random() * 40)
				      const end = start + Math.floor(Math.random() * 40)
				      this.queue.push({ from, to, start, end })
				    }
				    cancelAnimationFrame(this.frameRequest)
				    this.frame = 0
				    this.update()
				    return promise
				  }
				  update() {
				    let output = ''
				    let complete = 0
				    for (let i = 0, n = this.queue.length; i < n; i++) {
				      let { from, to, start, end, char } = this.queue[i]
				      if (this.frame >= end) {
				        complete++
				        output += to
				      } else if (this.frame >= start) {
				        if (!char || Math.random() < 0.28) {
				          char = this.randomChar()
				          this.queue[i].char = char
				        }
				        output += `<span class="dud">${char}</span>`
				      } else {
				        output += from
				      }
				    }
				    this.el.innerHTML = output
				    if (complete === this.queue.length) {
				      this.resolve()
				    } else {
				      this.frameRequest = requestAnimationFrame(this.update)
				      this.frame++
				    }
				  }
				  randomChar() {
				    return this.chars[Math.floor(Math.random() * this.chars.length)]
				  }
				}
	
				const phrases = [
				  '《摩西十诫》',
				  '独自一人',
				  '跪拜而臣',
				  '沉寂无声',
				  '静止以惰',
				  '侮蔑世人',
				  '折磨不杀',
				  '欲望之兽',
				  '夺取一切',
				  '真实之象',
				  '吞噬万物',
				  '男者守戒 通过以福音 女者破戒  试炼使灾祸​',
				]
				
				const el = document.querySelector('.my-text1')
				const fx = new TextScramble(el)
				
				let counter = 0
				const next = () => {
				  fx.setText(phrases[counter]).then(() => {
				    setTimeout(next, 800)
				  })
				  counter = (counter + 1) % phrases.length
				}
				next()
			}
		}
	}
</script>

<style scoped>
	.topbg{
		width: 100%;
		height: 100vh;
		margin-top: 0px!important;
		margin: 0;
		padding: 0;
		background-position: top center;
		background-repeat: no-repeat;
		background-attachment: fixed;
		background-size: cover;
		z-index: -1;
		background-image: url("http://cos.myo.pub/cover/72e934d0-df25-48dd-817e-429144bca9ab.png");
		
	}
	.topbg::before{
		content: '';
		position: absolute;
		height: 100vh;
		top: 0;
		bottom: 0;
		left: 0;
		right: 0;
		z-index: 3;
		background-attachment: fixed;
	}
	.topbg::before{
		background-image:url(http://cos.myo.pub/BgImage/dot.gif);
	}
	
	.head-guide{
		    position: relative;
			top: 40%;
		    max-width: 800px;
		    padding: 0 10px;
		    text-align: center;
		    z-index: 99;
			height: 200px;
			margin: 0 auto;
			
	}

	.guide_title{
		font-size: 60px;
		font-family: "Chancery", cursive, LiSu, sans-serif;
		color: #fff;
		mix-blend-mode: overlay;
	}

	.guide-text-ico{
		width: 60%;
		margin: auto;
		font-size: 16px;
		color: #EAEADF;
		background: rgba(0, 0, 0, .5);
		padding: 15px;
		margin-top: 20px;
		letter-spacing: 0;
		line-height: 30px;
		border-radius: 8px;
		box-sizing: initial;
		white-space: nowrap;
	}
	.ico-a{
		position: relative;
		left:-15px;
	}
	.guide_ico{
		margin-top: 10px;
		margin-left: 10px;
		overflow: hidden;
	}
	.guide_ico>ul{
		list-style:none;margin:0;
	}
	.guide_ico>ul li{
		display: inline-block;
		margin: 10px;
	}

	.guide_ico>a>img{
		width: 30px;
		height: 30px;
	}
	.Wechat{
	    margin: auto;
	    font-size: 16px;
	    color: #EAEADF;
	    letter-spacing: 0;
	    line-height: 30px;
	    border-radius: 8px;
	}
	.ShowWechat{
		width: 18%;
		margin: 0px auto 0px 38%;
		background: rgba(0, 0, 0, .5);
		/* border-top: 3px #000000 solid;*/
	}
	.ShowWechat>img{
		width:90px ;
		height:90px;
	}
	.Wechat-transition-start{
		transform: translateY(8%);
		opacity: 0;
		transition: all .5s;
	}
	.Wechat-transition{
		transform: translateY(0);
		opacity: 1;
		transition: all .5s;
	}
	.sanjioxing{
		/* margin: 0px auto 0px 38%;
		width: 100px;
		height: 30px;
		background-color: #00C8C8; */
	}

	.container-text{
		max-height: 30px;
		overflow: hidden;
		
	}
	.my-text-controller{
		
	}

	@media only screen and (max-width:800px) {
		.head-guide{
			padding: 0px;
		}
		.guide-text-ico{
			width: 100%;
			padding: 0px;
		}

		.my-text1{
			font-size: 20px;
		}
		
	}

	
	
</style>
