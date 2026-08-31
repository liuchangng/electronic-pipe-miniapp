/**
 * 收藏相关 API
 */
import { get, post } from '@/utils/request'

// 获取收藏ID列表
export const getFavoriteList = () => {
  return get('/api/favorite/list')
}

// 获取收藏曲谱详情列表
export const getFavoriteDetail = () => {
  return get('/api/favorite/detail')
}

// 添加收藏
export const addFavorite = (songId: number) => {
  return post('/api/favorite/add', { songId })
}

// 取消收藏
export const removeFavorite = (songId: number) => {
  return post('/api/favorite/remove', { songId })
}

// 获取曲谱的收藏用户列表
export const getFavoriteUsers = (songId: number) => {
  return get(`/api/favorite/users?songId=${songId}`)
}
