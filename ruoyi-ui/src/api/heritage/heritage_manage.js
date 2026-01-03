import request from '@/utils/request'


// 在该文件中添加以下函数
export function updateHeritageCount(type, itemId) {
    return request({
        url: `/heritage/heritage_manage/count/${type}/${itemId}`,
        method: 'post'
    })
}

// 查询非遗展品列表
export function listHeritage_manage(query) {
    return request({
        url: '/heritage/heritage_manage/list',
        method: 'get',
        params: query
    })
}

// 查询非遗展品详细
export function getHeritage_manage(itemId) {
    return request({
        url: '/heritage/heritage_manage/' + itemId,
        method: 'get'
    })
}

// 新增非遗展品
export function addHeritage_manage(data) {
    return request({
        url: '/heritage/heritage_manage',
        method: 'post',
        data: data
    })
}

// 修改非遗展品
export function updateHeritage_manage(data) {
    return request({
        url: '/heritage/heritage_manage',
        method: 'put',
        data: data
    })
}

// 删除非遗展品
export function delHeritage_manage(itemId) {
    return request({
        url: '/heritage/heritage_manage/' + itemId,
        method: 'delete'
    })
}

// 查询我的收藏列表
export function listMyCollection(query) {
    return request({
        url: '/heritage/heritage_manage/list/collection',
        method: 'get',
        params: query
    })
}

// 查询个人列表
export function listMyPublish(query) {
    return request({url: '/heritage/heritage_manage/myList', method: 'get', params: query})
}

// 获取个人发布详情
export function getMyPublish(itemId) {
    return request({url: '/heritage/heritage_manage/myInfo/' + itemId, method: 'get'})
}

// 个人发布
export function addMyPublish(data) {
    return request({url: '/heritage/heritage_manage/myAdd', method: 'post', data: data})
}

// 个人修改
export function updateMyPublish(data) {
    return request({url: '/heritage/heritage_manage/myEdit', method: 'put', data: data})
}

// 删除个人发布
export function delMyPublish(itemId) {
    return request({
        url: '/heritage/heritage_manage/myDel/' + itemId,
        method: 'delete'
    })
}