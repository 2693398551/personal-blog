import axios from 'axios'
import myMessage from '@/utils/Message'
import store from '@/store'
import {getToken} from '@/request/token'

const service = axios.create({
  baseURL: '/api',/*process.env.BASE_API*/
  timeout: 10000
})

// request拦截器
service.interceptors.request.use(config => {

  if (store.state.token) {
    config.headers['Authorization'] = getToken()
  }
  // 访客 UUID
  const uuid = localStorage.getItem('visitor_uuid')
  if (uuid) {
    config.headers['Visitor-UUID'] = uuid
  }
  return config
}, error => {
  Promise.reject(error)
})

// response拦截器
service.interceptors.response.use(
  response => {
    //如果后端返回了新 UUID，存到 localStorage
    const newUuid = response.headers['set-visitor-uuid']
    if (newUuid) {
      localStorage.setItem('visitor_uuid', newUuid)
    }
    // 全局统一处理 Session超时
    if (response.headers['session_time_out'] == 'timeout') {
      store.dispatch('fedLogOut')
    }

    const res = response.data;

    // 0 为成功状态
    if (res.code !== 200) {
      if (res.code === 10003) {
        myMessage.warning('登录状态已失效，请重新登录')
        // 触发Vuex中的前端登出，清理本地Token
        store.dispatch('fedLogOut')
        // 延迟1秒刷新当前页面，应用会重新走路由守卫，变成未登录的游客状态
        setTimeout(() => {
          window.location.reload()
        }, 1000)
        return Promise.reject('error');
      }
      // 90001 Session超时
      if (res.code === 90001) {
        return Promise.reject('error');
      }

      // 90002 用户未登录
      if (res.code === 90002) {
        myMessage.warning('未登录，请登录在进行操作哦')
        return Promise.reject('error');
      }

      // 70001 权限认证错误
      if (res.code === 70001) {
        myMessage.warning('你没有权限访问哦')
        return Promise.reject('error');
      }

      // 其他业务接口返回的错误，也用自定义的 error 弹窗展示
      myMessage.error(res.msg || '操作失败');
      return Promise.reject(res.msg);

    } else {
      return response.data; // 返回完整的响应对象
    }
  },
  error => {
    // 网络错误
    myMessage.error('网络异常')
    return Promise.reject('error')
  }
)

export default service
