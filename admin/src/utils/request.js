import axios from 'axios'

let isRedirecting = false

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

// 请求拦截
request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

// 响应拦截
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 401 未登录：清除token并跳转登录页
      if (res.code === 401 || res.message?.includes('未登录')) {
        handleUnauthorized()
        return Promise.reject(new Error(res.message || '未登录'))
      }
      console.warn('请求失败:', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      handleUnauthorized()
    } else {
      console.error('请求异常:', error.message)
    }
    return Promise.reject(error)
  }
)

/**
 * 处理未授权：清除token，跳转登录页（防重复跳转）
 */
function handleUnauthorized() {
  if (isRedirecting) return
  isRedirecting = true
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  // 延迟跳转，避免多个401并发触发
  setTimeout(() => {
    window.location.href = '/login'
    isRedirecting = false
  }, 100)
}

export default request