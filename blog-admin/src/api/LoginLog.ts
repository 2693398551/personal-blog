import request from '../utils/request'

// 分页查询登录日志
export function getLoginLogList(params: any) {
    return request({
        url: '/admin/loginLog/list',
        method: 'post',
        data: params
    })
}