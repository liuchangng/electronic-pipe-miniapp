/**
 * 搜索相关 API
 */
import { get, post, del } from '@/utils/request'

// 获取热门搜索
export const getHotSearches = () => {
  return get('/api/search/hot')
}

// 获取搜索历史
export const getSearchHistory = () => {
  return get('/api/search/history')
}

// 保存搜索历史
export const saveSearchHistory = (keyword: string) => {
  return post('/api/search/history', { keyword })
}

// 清空搜索历史
export const clearSearchHistory = () => {
  return del('/api/search/history')
}