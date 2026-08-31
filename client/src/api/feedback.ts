/**
 * 反馈相关 API
 */
import { get, post } from '@/utils/request'

// 提交反馈
export const submitFeedback = (data: { type: string; content: string; contact?: string }) => {
  return post('/api/feedback/submit', data)
}

// 获取我的反馈列表
export const getMyFeedbacks = () => {
  return get('/api/feedback/my')
}