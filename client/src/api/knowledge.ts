/**
 * 知识相关 API
 */
import { get } from '@/utils/request'

// 获取知识列表
export const getKnowledgeList = (category?: string) => {
  return get('/api/knowledge/list', { category })
}
