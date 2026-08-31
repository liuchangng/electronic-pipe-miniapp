import request from '../utils/request'

// 获取系统配置（管理端，需登录）
export const getConfig = () => request.get('/api/admin/config')

// 更新系统配置
export const updateConfig = (data) => request.post('/api/admin/config', data)

// 获取公开配置（无需登录）
export const getPublicConfig = () => request.get('/api/config/public')