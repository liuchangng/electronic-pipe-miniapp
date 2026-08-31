/**
 * 视频相关 API
 */
import { get } from '@/utils/request'

// 获取曲谱关联的视频列表
export const getVideoList = (songId: number) => {
  return get('/api/video/list', { songId })
}

// 获取全部视频列表
export const getVideoAll = () => {
  return get('/api/video/all')
}

// 获取有视频的曲谱列表（用于教程页分类筛选）
export const getVideoSongs = () => {
  return get('/api/video/songs')
}

// 视频分页列表（支持曲谱筛选）
export const getVideoPage = (params: { songId?: number; page?: number; pageSize?: number }) => {
  return get('/api/video/page', params)
}
