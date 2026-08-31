/**
 * Vitest 全局 setup 文件
 * 模拟 uni-app 运行时 API
 */

// 模拟 uni-app 全局对象
const uni = {
  getStorageSync: vi.fn((key: string) => {
    if (key === 'token') return ''
    return ''
  }),
  setStorageSync: vi.fn(),
  removeStorageSync: vi.fn(),
  request: vi.fn(),
  showToast: vi.fn(),
  showLoading: vi.fn(),
  hideLoading: vi.fn(),
  navigateTo: vi.fn(),
  redirectTo: vi.fn(),
  switchTab: vi.fn(),
  navigateBack: vi.fn(),
  getSystemInfoSync: vi.fn(() => ({
    windowWidth: 375,
    windowHeight: 667,
    pixelRatio: 2,
  })),
}

// 注入全局
;(globalThis as any).uni = uni