import request from '../utils/request'

// 获取用户列表
export const getUserList = (params) => request.get('/api/admin/user/list', { params })

// 更新用户状态
export const updateUserStatus = (data) => request.post('/api/admin/user/updateStatus', data)