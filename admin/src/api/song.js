import request from '../utils/request'

// 获取曲谱列表
export const getSongList = (params) => request.get('/api/admin/song/list', { params })

// 获取曲谱详情
export const getSongDetail = (id) => request.get(`/api/admin/song/detail/${id}`)

// 新增曲谱
export const addSong = (data) => request.post('/api/admin/song/add', data)

// 更新曲谱
export const updateSong = (data) => request.post('/api/admin/song/update', data)

// 删除曲谱
export const deleteSong = (id) => request.post(`/api/admin/song/delete/${id}`)

// 获取曲谱下拉列表（供视频关联）
export const getSongSelectList = () => request.get('/api/admin/song/select-list')

// 获取曲谱收藏记录（分页）
export const getSongFavoriteList = (params) => request.get('/api/admin/song/favorite/list', { params })