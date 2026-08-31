import request from '../utils/request'

// 管理员登录
export const login = (data) => request.post('/api/admin/login', data)

// 获取管理员信息
export const getAdminInfo = () => request.get('/api/admin/info')

// 退出登录
export const logout = () => request.post('/api/admin/logout')

// 统计数据
export const getStats = () => request.get('/api/admin/stats')