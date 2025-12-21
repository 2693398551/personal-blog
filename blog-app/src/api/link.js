import request from '@/request'

export function getLink() {
  return request({
    url: '/link',
    method: 'get',
  })
}