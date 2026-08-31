/**
 * request.ts 工具模块测试
 * 使用 vi.mock 模拟 uni-app 全局对象
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

// 模拟 uni 全局对象
const mockRequest = vi.fn()
const mockGetStorageSync = vi.fn(() => '')
const mockShowToast = vi.fn()
const mockRemoveStorageSync = vi.fn()

;(globalThis as any).uni = {
  request: mockRequest,
  getStorageSync: mockGetStorageSync,
  setStorageSync: vi.fn(),
  removeStorageSync: mockRemoveStorageSync,
  showToast: mockShowToast,
}

// 静态导入，uni 已在全局设置
const requestModule = await import('@/utils/request')
const { get, post } = requestModule

describe('request 工具', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockGetStorageSync.mockReturnValue('')
  })

  describe('get 请求', () => {
    it('应发送 GET 请求并返回数据', async () => {
      mockRequest.mockImplementation((opts: any) => {
        opts.success({ statusCode: 200, data: { code: 200, data: { list: [] } } })
      })

      const result = await get('/api/song/list', { tab: 'hot' })

      expect(mockRequest).toHaveBeenCalledOnce()
      const callOpts = mockRequest.mock.calls[0][0]
      expect(callOpts.method).toBe('GET')
      expect(callOpts.url).toContain('/api/song/list')
      expect(callOpts.data).toEqual({ tab: 'hot' })
      expect(result).toEqual({ list: [] })
    })

    it('应携带 token 请求', async () => {
      mockGetStorageSync.mockReturnValue('test-token')
      mockRequest.mockImplementation((opts: any) => {
        opts.success({ statusCode: 200, data: { code: 200, data: {} } })
      })

      await get('/api/user/info')

      const callOpts = mockRequest.mock.calls[0][0]
      expect(callOpts.header.Authorization).toBe('test-token')
    })
  })

  describe('post 请求', () => {
    it('应发送 POST 请求', async () => {
      mockRequest.mockImplementation((opts: any) => {
        opts.success({ statusCode: 200, data: { code: 200, data: { id: 1 } } })
      })

      const result = await post('/api/favorite/add', { songId: 1 })

      const callOpts = mockRequest.mock.calls[0][0]
      expect(callOpts.method).toBe('POST')
      expect(callOpts.data).toEqual({ songId: 1 })
      expect(result).toEqual({ id: 1 })
    })
  })

  describe('错误处理', () => {
    it('应处理业务错误（code != 200）', async () => {
      mockRequest.mockImplementation((opts: any) => {
        opts.success({ statusCode: 200, data: { code: 500, message: '服务器错误' } })
      })

      await expect(get('/api/song/list')).rejects.toEqual({ code: 500, message: '服务器错误' })
      expect(mockShowToast).toHaveBeenCalledWith({
        title: '服务器错误',
        icon: 'none',
      })
    })

    it('应处理 401 未授权', async () => {
      mockRequest.mockImplementation((opts: any) => {
        opts.success({ statusCode: 401, data: {} })
      })

      await expect(get('/api/user/info')).rejects.toBeTruthy()
      expect(mockRemoveStorageSync).toHaveBeenCalledWith('token')
      expect(mockRemoveStorageSync).toHaveBeenCalledWith('userInfo')
    })

    it('应处理网络错误', async () => {
      mockRequest.mockImplementation((opts: any) => {
        opts.success({ statusCode: 500, data: {} })
      })

      await expect(get('/api/song/list')).rejects.toBeTruthy()
      expect(mockShowToast).toHaveBeenCalledWith({
        title: '网络错误',
        icon: 'none',
      })
    })

    it('应处理请求失败', async () => {
      mockRequest.mockImplementation((opts: any) => {
        opts.fail(new Error('网络连接失败'))
      })

      await expect(get('/api/song/list')).rejects.toBeTruthy()
      expect(mockShowToast).toHaveBeenCalledWith({
        title: '网络连接失败',
        icon: 'none',
      })
    })
  })
})