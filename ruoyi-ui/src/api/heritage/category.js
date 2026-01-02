import request from '@/utils/request'

// 查询非遗分类列表
export function listCategory(query) {
  return request({
    url: '/heritage/category/list',
    method: 'get',
    params: query
  })
}

// 查询非遗分类详细
export function getCategory(categoryId) {
  return request({
    url: '/heritage/category/' + categoryId,
    method: 'get'
  })
}

// 新增非遗分类
export function addCategory(data) {
  return request({
    url: '/heritage/category',
    method: 'post',
    data: data
  })
}

// 修改非遗分类
export function updateCategory(data) {
  return request({
    url: '/heritage/category',
    method: 'put',
    data: data
  })
}

// 删除非遗分类
export function delCategory(categoryId) {
  return request({
    url: '/heritage/category/' + categoryId,
    method: 'delete'
  })
}
