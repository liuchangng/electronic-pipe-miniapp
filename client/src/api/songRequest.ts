/**
 * 求谱相关 API
 */
import { get, post } from '@/utils/request'

// 提交求谱申请
export const submitRequest = (data: {
  songName: string
  artist?: string
  category?: string
  description?: string
}) => {
  return post('/api/song-request/submit', data)
}

// 获取我的求谱列表
export const getMyRequests = () => {
  return get('/api/song-request/my')
}