import request from '@/request'

export function getVisitTotal() {
  return request({
    url: '/visit/total',
    method: 'get'
  })
}
