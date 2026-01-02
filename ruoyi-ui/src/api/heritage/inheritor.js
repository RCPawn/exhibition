import request from '@/utils/request'

// 获取知识图谱数据
export function getKnowledgeGraph() {
    return request({
        url: '/heritage/inheritor/getKnowledgeGraph',
        method: 'get'
    })
}


// 查询非遗传承人列表
export function listInheritor(query) {
  return request({
    url: '/heritage/inheritor/list',
    method: 'get',
    params: query
  })
}

// 查询非遗传承人详细
export function getInheritor(inheritorId) {
  return request({
    url: '/heritage/inheritor/' + inheritorId,
    method: 'get'
  })
}

// 新增非遗传承人
export function addInheritor(data) {
  return request({
    url: '/heritage/inheritor',
    method: 'post',
    data: data
  })
}

// 修改非遗传承人
export function updateInheritor(data) {
  return request({
    url: '/heritage/inheritor',
    method: 'put',
    data: data
  })
}

// 删除非遗传承人
export function delInheritor(inheritorId) {
  return request({
    url: '/heritage/inheritor/' + inheritorId,
    method: 'delete'
  })
}
