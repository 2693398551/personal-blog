import request from '../utils/request'

// 今日 PV / UV
export function getTodayStats() {
    return request({
        url: '/admin/visit/today',
        method: 'get'
    })
}

// 最近 N 天折线图数据
export function getRecentDays(days: number = 30) {
    return request({
        url: `/admin/visit/recent?days=${days}`,
        method: 'get'
    })
}

// 自定义日期范围
export function getDateRange(startDate: string, endDate: string) {
    return request({
        url: `/admin/visit/range?startDate=${startDate}&endDate=${endDate}`,
        method: 'get'
    })
}

// 按城市聚合 UV（地图打点）
export function getCityMap() {
    return request({
        url: '/admin/visit/map',
        method: 'get'
    })
}

// 分页查询访客列表
export function getVisitorList(params: { page: number; pageSize: number; keyword?: string }) {
    return request({
        url: `/admin/visit/visitors?page=${params.page}&pageSize=${params.pageSize}&keyword=${params.keyword || ''}`,
        method: 'get'
    })
}

// 查看某访客的行为明细
export function getVisitLogs(visitorUuid: string, page: number = 1, pageSize: number = 20) {
    return request({
        url: `/admin/visit/logs?visitorUuid=${visitorUuid}&page=${page}&pageSize=${pageSize}`,
        method: 'get'
    })
}
