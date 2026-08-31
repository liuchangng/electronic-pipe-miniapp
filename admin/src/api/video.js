import request from '../utils/request'

// 获取视频列表
export const getVideoList = (params) => request.get('/api/admin/video/list', { params })

// 新增视频
export const addVideo = (data) => request.post('/api/admin/video/add', data)

// 更新视频
export const updateVideo = (data) => request.post('/api/admin/video/update', data)

// 删除视频
export const deleteVideo = (id) => request.post(`/api/admin/video/delete/${id}`)

// 上传视频（FormData）
export const uploadVideo = (formData) => request.post('/api/admin/video/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' },
  timeout: 300000  // 5分钟超时，视频文件较大
})

// 查询转码状态
export const getTranscodeStatus = (id) => request.get(`/api/admin/video/transcode-status/${id}`)

// 分片上传：上传单个分片
export const uploadChunk = (formData) => request.post('/api/admin/video/upload-chunk', formData, {
  headers: { 'Content-Type': 'multipart/form-data' },
  timeout: 120000
})

// 分片上传：合并分片
export const mergeChunks = (data) => request.post('/api/admin/video/merge-chunks', data, {
  timeout: 300000
})

// 重试转码
export const retryTranscode = (id) => request.post(`/api/admin/video/retry-transcode/${id}`)

// 获取视频详情（管理端，不增加播放计数）
export const getVideoDetail = (id) => request.get(`/api/admin/video/detail/${id}`)

// 获取视频点赞记录（分页）
export const getVideoLikeList = (params) => request.get('/api/admin/video/like/list', { params })

// 获取视频收藏记录（分页）
export const getVideoFavoriteList = (params) => request.get('/api/admin/video/favorite/list', { params })

// 获取视频分享记录（分页）
export const getVideoShareList = (params) => request.get('/api/admin/video/share/list', { params })