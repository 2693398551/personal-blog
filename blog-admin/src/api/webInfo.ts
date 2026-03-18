import request from '../utils/request'

// 获取网站全局配置信息
export function getWebInfo() {
    return request({
        url: '/webInfo',
        method: 'get'
    })
}

// 更新网站全局配置信息
export function updateWebInfo(data: any) {
    return request({
        url: '/webInfo/update',
        method: 'post',
        data
    })
}