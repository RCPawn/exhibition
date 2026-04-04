import request from '@/utils/request'

// 查询音频档案列表
export function listAudio(query) {
  return request({
    url: '/heritage/audio/list',
    method: 'get',
    params: query
  })
}

// 查询音频档案详细
export function getAudio(audioId) {
  return request({
    url: '/heritage/audio/' + audioId,
    method: 'get'
  })
}

// 新增音频档案
export function addAudio(data) {
  return request({
    url: '/heritage/audio',
    method: 'post',
    data: data
  })
}

// 修改音频档案
export function updateAudio(data) {
  return request({
    url: '/heritage/audio',
    method: 'put',
    data: data
  })
}

// 删除音频档案
export function delAudio(audioId) {
  return request({
    url: '/heritage/audio/' + audioId,
    method: 'delete'
  })
}

// 获取展示端专用的已发布音频列表 (包含关联模型数据)
export function listArchiveAudio(query) {
    return request({
        url: '/heritage/audio/listArchive',
        method: 'get',
        params: query
    })
}
