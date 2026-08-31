/**
 * 视频互动相关 API（点赞、收藏）
 */
import { get, post } from '@/utils/request'

// 切换点赞状态
export const toggleVideoLike = (videoId: number) => {
  return post('/api/video/like/toggle', { videoId })
}

// 检查是否已点赞
export const checkVideoLike = (videoId: number) => {
  return get(`/api/video/like/check?videoId=${videoId}`)
}

// 获取用户点赞的视频ID列表
export const getLikedVideoIds = () => {
  return get('/api/video/like/batch-check')
}

// 切换收藏状态
export const toggleVideoFavorite = (videoId: number) => {
  return post('/api/video/favorite/toggle', { videoId })
}

// 检查是否已收藏
export const checkVideoFavorite = (videoId: number) => {
  return get(`/api/video/favorite/check?videoId=${videoId}`)
}

// 获取用户收藏的视频ID列表
export const getVideoFavoriteList = () => {
  return get('/api/video/favorite/list')
}

// 获取用户收藏的视频详情列表
export const getVideoFavoriteDetail = () => {
  return get('/api/video/favorite/detail')
}

// 记录分享（无需登录）
export const recordVideoShare = (videoId: number) => {
  return post('/api/video/share/record', { videoId })
}

// 获取视频的点赞用户列表
export const getVideoLikeUsers = (videoId: number) => {
  return get(`/api/video/like/users?videoId=${videoId}`)
}

// 获取视频的收藏用户列表
export const getVideoFavoriteUsers = (videoId: number) => {
  return get(`/api/video/favorite/users?videoId=${videoId}`)
}