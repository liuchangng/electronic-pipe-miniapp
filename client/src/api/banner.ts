/**
 * Banner 相关 API
 */
import { get } from '@/utils/request'

// 获取 Banner 列表
export const getBannerList = () => {
  return get('/api/banner/list')
}
