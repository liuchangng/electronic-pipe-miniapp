import request from '../utils/request'

// 上传文件
export const uploadFile = (formData) => request.post('/api/file/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})