import request from '../utils/request'

// 获取评论列表
export const getCommentList = (params) => request.get('/api/admin/comment/list', { params })

// 审核评论
export const reviewComment = (data) => request.post('/api/admin/comment/review', data)

// 删除评论
export const deleteComment = (id) => request.post(`/api/admin/comment/delete/${id}`)