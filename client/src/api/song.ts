/**
 * 曲谱相关 API
 */
import { get } from '@/utils/request'

// 获取曲谱列表
export const getSongList = (params?: {
  keyword?: string
  tab?: string
}) => {
  return get('/api/song/list', params)
}

// 获取曲谱分页列表
export const getSongPage = (params?: {
  keyword?: string
  tab?: string
  page?: number
  pageSize?: number
}) => {
  return get('/api/song/page', params)
}

// 获取曲谱详情
export const getSongDetail = (id: number) => {
  return get(`/api/song/detail/${id}`)
}
