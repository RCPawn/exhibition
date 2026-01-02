import request from '@/utils/request'

// 查询评论列表
export function listComment(query) {
    return request({
        url: '/heritage/comment/list',
        method: 'get',
        params: query
    })
}

// 新增评论
export function addComment(data) {
    return request({
        url: '/heritage/comment',
        method: 'post',
        data: data
    })
}