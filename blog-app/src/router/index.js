import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/Home'
import {Message} from 'element-ui';
import store from '@/store'
import {getToken} from '@/request/token'
import request from '@/request'

Vue.use(Router)

const router = new Router({
  /*mode: 'history',*/
  scrollBehavior: function (to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      if (from.meta.keepAlive) {
        from.meta.savedPosition = document.body.scrollTop;
      }
      return {x: 0, y: to.meta.savedPosition || 0}
    }
  },
  routes: [
    {
      path: '/write/:id?',
      component: r => require.ensure([], () => r(require('@/views/blog/BlogWrite')), 'blogwrite'),
      meta: {
        requireLogin: true,

      },
    },

    {
      path: '',
      /* name: 'Home', */
      component: Home,
      children: [
        {
          path: '/',
          component: r => require.ensure([], () => r(require('@/views/Index')), 'index'),
          meta: {
            keepAlive: true,// 需要缓存
            requireShow: true,
          }
        },
        {
          path: '/log',
          component: r => require.ensure([], () => r(require('@/views/Log')), 'log')
        },
        {
          path: '/articles/page/:page',
          component: r => require.ensure([], () => r(require('@/views/blog/BlogArchive')), 'articles')
        },
        {
          path: '/articles/:year?/:month?',
          component: r => require.ensure([], () => r(require('@/views/blog/BlogArchive')), 'articles')
        },
        {
          path: '/nav',
          component: r => require.ensure([], () => r(require('@/views/blog/BlogNavigate')), 'blogNavigate')
        },
        {
          path: '/Resume',
          component: r => require.ensure([], () => r(require('@/views/blog/BlogResume')), 'BlogResume')
        },
        {
          path: '/Log',
          component: r => require.ensure([], () => r(require('@/views/Log')), 'Log')
        },
        {
          path: '/messageBoard',
          component: r => require.ensure([], () => r(require('@/views/MessageBoard')), 'messageboard')
        },
        {
          path: '/view/:id',
          component: r => require.ensure([], () => r(require('@/views/blog/BlogView')), 'blogview')
        },
        {
          path: '/space/:id',
          component: () => import('@/views/space/UserSpace'),
          meta: { title: '我的文章' }
        },
        {
          path: '/:type/all',
          component: r => require.ensure([], () => r(require('@/views/blog/BlogAllCategoryTag')), 'blogallcategorytag')
        },
        {
          path: '/:type/:id',
          component: r => require.ensure([], () => r(require('@/views/blog/BlogCategoryTag')), 'blogcategorytag')
        },
        {
          path: '/login',
          component: r => require.ensure([], () => r(require('@/views/Login')), 'login')
        },
        {
          path: '/register',
          component: r => require.ensure([], () => r(require('@/views/Login')), 'register')
        },



      ]
    },

  ],
  /*   scrollBehavior(to, from, savedPosition) {
       console.debug(savedPosition)
        if (savedPosition) {
          return savedPosition
        } else {
          return { top: 0 }
        }
      }, */
});
//导航守卫
router.beforeEach((to, from, next) => {
	//to：你要去哪里
	//from:你来自哪里
	//获取滚动条位置
	/* let dom = document.documentElement.scrollTop
	sessionStorage.setItem("scrollTop",dom) */
  if (getToken()) {
    if (to.path === '/login') {
      next({path: '/'})
    } else {
      if (store.state.account.length === 0) {
        store.dispatch('getUserInfo').then(data => { //获取用户信息
            next()
        }).catch(() => {
          Message({
            type: 'warning',
            showClose: true,
            message: '登录已过期,请重新登录~'
          })
          next({path: '/'})
        })
      } else {

        next()
      }
    }
  } else {
    if (to.matched.some(r => r.meta.requireLogin)) {
      Message({
        type: 'warning',
        showClose: true,
        message: '暂未登录'
      })

    }
    else {
      next();
    }
  }
})

//路由跳转后上报页面访问
router.afterEach((to, from) => {
  // 排除不需要统计的页面
  const excludePaths = ['/login', '/register', '/write']
  if (excludePaths.some(p => to.path.startsWith(p))) return

  const uuid = localStorage.getItem('visitor_uuid')
  const headers = uuid ? { 'Visitor-UUID': uuid } : {}

  request({
    url: '/visit/record',
    method: 'post',
    data: { uri: to.path },
    headers
  }).then(res => {
    // 后端首次返回新 UUID，存起来
    if (res && res.newUuid) {
      localStorage.setItem('visitor_uuid', res.newUuid)
    }
  }).catch(() => {})  // 统计失败静默处理，不影响用户
})


 const VueRouterPush = Router.prototype.push
Router.prototype.push = function push (to) {
  return VueRouterPush.call(this, to).catch(err => err)
}


export default router
