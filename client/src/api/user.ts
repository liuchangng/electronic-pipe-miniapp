/**
 * 用户相关 API
 */
import { get, post, uploadFile } from '@/utils/request'

// 微信登录
export const wxLogin = (code: string) => {
  return post('/api/user/login', { code })
}

// 获取用户信息
export const getUserInfo = () => {
  return get('/api/user/info')
}

// 退出登录
export const logout = () => {
  return post('/api/user/logout')
}

// 更新用户资料
export const updateProfile = (data: { avatar?: string; nickname?: string }) => {
  return post('/api/user/updateProfile', data)
}

// 上传文件（头像等）
export const uploadAvatar = (filePath: string) => {
  return uploadFile('/api/file/upload', filePath)
}
