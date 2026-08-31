import request from '../utils/request'

// 获取知识列表（分页，支持分类筛选）
export const getKnowledgeList = (params) => request.get('/api/admin/knowledge/list', { params })

// 新增知识
export const addKnowledge = (data) => request.post('/api/admin/knowledge/add', data)

// 更新知识
export const updateKnowledge = (data) => request.post('/api/admin/knowledge/update', data)

// 删除知识（逻辑删除）
export const deleteKnowledge = (id) => request.post(`/api/admin/knowledge/delete/${id}`)