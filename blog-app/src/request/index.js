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
  return config
}, error => {
  Promise.reject(error)
})

// response拦截器
service.interceptors.response.use(
  response => {

    // 全局统一处理 Session超时
    if (response.headers['session_time_out'] == 'timeout') {
      store.dispatch('fedLogOut')
    }

    const res = response.data;

    // 0 为成功状态
    if (res.code !== 200) {
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
    // 网络错误或连接超时
    myMessage.error('连接超时或网络异常')
    return Promise.reject('error')
  }
)

export default service
