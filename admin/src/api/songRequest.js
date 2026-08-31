import request from '../utils/request'

// 获取求谱列表
export const getSongRequestList = (params) => request.get('/api/admin/song-request/list', { params })

// 处理求谱申请
export const handleSongRequest = (data) => request.post('/api/admin/song-request/handle', data)