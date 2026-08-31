/**
 * API 请求工具
 * 基于 uni.request 封装
 */

// 后端地址
// 开发环境：使用 localhost（模拟器）
// 真机调试：改为局域网IP，如 'http://192.168.0.104:8080'
const BASE_URL = 'http://localhost:8080'

// 请求拦截器
const request = (options: any): Promise<any> => {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token || ''
      },
      success: (res: any) => {
        if (res.statusCode === 200) {
          const data = res.data
          if (data.code === 200) {
            resolve(data.data)
          } else {
            uni.showToast({
              title: data.message || '请求失败',
              icon: 'none'
            })
            reject(data)
          }
        } else if (res.statusCode === 401) {
          // Token 过期，跳转登录
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({
            title: '请重新登录',
            icon: 'none'
          })
          reject(res)
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err: any) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// GET 请求 - 过滤掉 undefined 值的参数
export const get = (url: string, data?: any) => {
  const filteredData: Record<string, any> = {}
  if (data) {
    for (const key in data) {
      if (data[key] !== undefined && data[key] !== null && data[key] !== '') {
        filteredData[key] = data[key]
      }
    }
  }
  return request({ url, method: 'GET', data: filteredData })
}

// POST 请求
export const post = (url: string, data?: any) => {
  return request({ url, method: 'POST', data })
}

// DELETE 请求
export const del = (url: string, data?: any) => {
  return request({ url, method: 'DELETE', data })
}

// 文件上传
export const uploadFile = (url: string, filePath: string): Promise<any> => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: BASE_URL + url,
      filePath,
      name: 'file',
      header: {
        'Authorization': token || ''
      },
      success: (res: any) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            uni.showToast({ title: data.message || '上传失败', icon: 'none' })
            reject(data)
          }
        } else {
          uni.showToast({ title: '上传失败', icon: 'none' })
          reject(res)
        }
      },
      fail: (err: any) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default {
  get,
  post,
  del,
  uploadFile
}
