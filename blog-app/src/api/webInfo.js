import request from '@/request'

export function getWebInfo() {
  return request({
    url: '/webInfo',
    method: 'get'
  })
}
