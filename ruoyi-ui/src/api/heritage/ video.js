import request from '@/utils/request'

export function listVideo(query) {
    return request({
        url: '/heritage/video/list',
        method: 'get',
        params: query
    })
}

export function getVideo(videoId) {
    return request({
        url: '/heritage/video/' + videoId,
        method: 'get'
    })
}

export function addVideo(data) {
    return request({
        url: '/heritage/video',
        method: 'post',
        data: data
    })
}

export function updateVideo(data) {
    return request({
        url: '/heritage/video',
        method: 'put',
        data: data
    })
}

export function delVideo(videoId) {
    return request({
        url: '/heritage/video/' + videoId,
        method: 'delete'
    })
}

export function listArchiveVideo(query) {
    return request({
        url: '/heritage/video/listArchive',
        method: 'get',
        params: query
    })
}
