/**
 * 登录状态管理 Hook
 * 
 * 核心逻辑：
 * 1. 启动时从本地存储快速恢复状态（同步）
 * 2. App.onLaunch 中调 validateToken 异步验证 token 有效性
 * 3. token 无效则清除，页面按需引导登录
 * 4. 登录失败直接提示，不做降级
 */
import { ref } from 'vue'
import { wxLogin, getUserInfo } from '@/api/user'

export function useLogin() {
  const isLoggedIn = ref(false)
  const userInfo = ref<any>({})
  const loading = ref(false)

  /**
   * 同步检查本地登录状态（仅读取本地缓存，用于初始渲染）
   */
  const checkLoginStatus = (): boolean => {
    const token = uni.getStorageSync('token')
    if (token) {
      isLoggedIn.value = true
      const storedUser = uni.getStorageSync('userInfo')
      if (storedUser) {
        try {
          userInfo.value = JSON.parse(storedUser)
        } catch (e) {
          // 缓存数据异常，清除
          uni.removeStorageSync('userInfo')
        }
      }
      return true
    }
    return false
  }

  /**
   * 异步验证 token 有效性（调后端 /api/user/info）
   * 有效 → 更新用户信息；无效 → 清除本地状态
   * 应在 App.onLaunch 中调用
   */
  const validateToken = async (): Promise<boolean> => {
    const token = uni.getStorageSync('token')
    if (!token) {
      isLoggedIn.value = false
      userInfo.value = {}
      return false
    }
    try {
      const data = await getUserInfo()
      userInfo.value = data
      uni.setStorageSync('userInfo', JSON.stringify(data))
      isLoggedIn.value = true
      return true
    } catch (e) {
      // token 已失效，清除本地状态
      console.warn('token 已失效，清除登录状态')
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      isLoggedIn.value = false
      userInfo.value = {}
      return false
    }
  }

  /**
   * 微信登录
   */
  const login = async (): Promise<boolean> => {
    loading.value = true
    try {
      // 获取微信登录凭证
      const loginRes = await new Promise<any>((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: resolve,
          fail: reject
        })
      })

      const code = loginRes.code

      // 调用后端登录接口
      const res = await wxLogin(code)

      // 保存 token 和用户信息
      uni.setStorageSync('token', res.token)
      uni.setStorageSync('userInfo', JSON.stringify(res.userInfo))

      isLoggedIn.value = true
      userInfo.value = res.userInfo

      return true
    } catch (e) {
      console.error('登录失败', e)
      uni.showToast({
        title: '登录失败，请检查网络后重试',
        icon: 'none'
      })
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 刷新用户信息
   */
  const refreshUserInfo = async (): Promise<void> => {
    try {
      const data = await getUserInfo()
      userInfo.value = data
      uni.setStorageSync('userInfo', JSON.stringify(data))
    } catch (e) {
      console.error('刷新用户信息失败', e)
    }
  }

  /**
   * 退出登录
   */
  const logout = (): void => {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    isLoggedIn.value = false
    userInfo.value = {}
  }

  // 初始化时从本地存储快速恢复状态
  checkLoginStatus()

  return {
    isLoggedIn,
    userInfo,
    loading,
    checkLoginStatus,
    validateToken,
    login,
    refreshUserInfo,
    logout
  }
}