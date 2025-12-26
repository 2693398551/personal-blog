
import Vue from 'vue'
import App from './App'
import axios from 'axios'
import router from './router'
import store from './store'
import 'default-passive-events'



import lodash from 'lodash'

import ElementUI from 'element-ui'
import '@/assets/theme/index.css'
import 'element-ui/lib/theme-chalk/index.css'


import {formatTime} from "./utils/time";
import animate from 'animate.css'


import nprogress from 'nprogress' // 进度条
import 'nprogress/nprogress.css' //进度条样式



Vue.use(animate)

Vue.config.productionTip = false
Vue.prototype.$axios=axios
// 设置全局变量
Vue.prototype.$myName = '寶菈';

Vue.use(ElementUI)
Object.defineProperty(Vue.prototype, '$_', { value: lodash })


Vue.directive('title',  function (el, binding) {
  document.title = el.dataset.title
})
// 格式话时间
Vue.filter('format', formatTime)

//进度条
router.beforeEach((to, from , next) => {
	//每次切换页面时，调用进度条
	nprogress.start();
	// 这个一定要加，没有next()页面不会跳转的
	next();
});
router.afterEach(() => {
	// 在即将进入新的页面组件前，关闭掉进度条
	nprogress.done()
})

new Vue({
  el: '#app',
  router,
  store,
  template: '<App/>',
  components: { App }
})
