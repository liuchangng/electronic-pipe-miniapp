import request from '../utils/request'

// 获取反馈列表
export const getFeedbackList = (params) => request.get('/api/feedback/list', { params })

// 回复反馈
export const replyFeedback = (data) => request.post('/api/feedback/reply', data)