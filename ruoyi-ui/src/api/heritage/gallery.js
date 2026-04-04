import request from '@/utils/request'

// 查询图集管理列表
export function listGallery(query) {
  return request({
    url: '/heritage/gallery/list',
    method: 'get',
    params: query
  })
}

// 查询图集管理详细
export function getGallery(galleryId) {
  return request({
    url: '/heritage/gallery/' + galleryId,
    method: 'get'
  })
}

// 新增图集管理
export function addGallery(data) {
  return request({
    url: '/heritage/gallery',
    method: 'post',
    data: data
  })
}

// 修改图集管理
export function updateGallery(data) {
  return request({
    url: '/heritage/gallery',
    method: 'put',
    data: data
  })
}

// 删除图集管理
export function delGallery(galleryId) {
  return request({
    url: '/heritage/gallery/' + galleryId,
    method: 'delete'
  })
}
