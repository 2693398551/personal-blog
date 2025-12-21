import request from '@/request'

/* 站点信息 */
export function getWebinfo() {
  return request({
    url: '/utils', 
    method: 'get',
  })
}
