/**
 * API 模块测试
 * 使用 vi.mock 模拟 request 模块，验证 API 调用参数
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

// 模拟 request 模块
const mockGet = vi.fn()
const mockPost = vi.fn()

vi.mock('@/utils/request', () => ({
  get: mockGet,
  post: mockPost,
}))

// 模拟 uni 全局对象（API 模块不直接使用，但导入链可能需要）
;(globalThis as any).uni = {
  getStorageSync: vi.fn(() => ''),
  setStorageSync: vi.fn(),
  removeStorageSync: vi.fn(),
  request: vi.fn(),
  showToast: vi.fn(),
}

describe('API 模块', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('song API', () => {
    it('getSongList 应调用 GET /api/song/list', async () => {
      mockGet.mockResolvedValue([{ id: 1, title: '天空之城' }])
      const { getSongList } = await import('@/api/song')

      const result = await getSongList({ tab: 'hot' })

      expect(mockGet).toHaveBeenCalledWith('/api/song/list', { tab: 'hot' })
      expect(result).toEqual([{ id: 1, title: '天空之城' }])
    })

    it('getSongDetail 应调用 GET /api/song/detail/:id', async () => {
      mockGet.mockResolvedValue({ id: 1, title: '天空之城' })
      const { getSongDetail } = await import('@/api/song')

      const result = await getSongDetail(1)

      expect(mockGet).toHaveBeenCalledWith('/api/song/detail/1')
    })
  })

  describe('favorite API', () => {
    it('getFavoriteList 应调用 GET /api/favorite/list', async () => {
      mockGet.mockResolvedValue([])
      const { getFavoriteList } = await import('@/api/favorite')

      await getFavoriteList()

      expect(mockGet).toHaveBeenCalledWith('/api/favorite/list')
    })

    it('addFavorite 应调用 POST /api/favorite/add', async () => {
      mockPost.mockResolvedValue(null)
      const { addFavorite } = await import('@/api/favorite')

      await addFavorite(1)

      expect(mockPost).toHaveBeenCalledWith('/api/favorite/add', { songId: 1 })
    })

    it('removeFavorite 应调用 POST /api/favorite/remove', async () => {
      mockPost.mockResolvedValue(null)
      const { removeFavorite } = await import('@/api/favorite')

      await removeFavorite(1)

      expect(mockPost).toHaveBeenCalledWith('/api/favorite/remove', { songId: 1 })
    })
  })

  describe('banner API', () => {
    it('getBannerList 应调用 GET /api/banner/list', async () => {
      mockGet.mockResolvedValue([])
      const { getBannerList } = await import('@/api/banner')

      await getBannerList()

      expect(mockGet).toHaveBeenCalledWith('/api/banner/list')
    })
  })

  describe('user API', () => {
    it('wxLogin 应调用 POST /api/user/login', async () => {
      mockPost.mockResolvedValue({ token: 'abc' })
      const { wxLogin } = await import('@/api/user')

      await wxLogin('test-code')

      expect(mockPost).toHaveBeenCalledWith('/api/user/login', { code: 'test-code' })
    })

    it('getUserInfo 应调用 GET /api/user/info', async () => {
      mockGet.mockResolvedValue({ id: 1, nickname: '测试' })
      const { getUserInfo } = await import('@/api/user')

      await getUserInfo()

      expect(mockGet).toHaveBeenCalledWith('/api/user/info')
    })

    it('logout 应调用 POST /api/user/logout', async () => {
      mockPost.mockResolvedValue(null)
      const { logout } = await import('@/api/user')

      await logout()

      expect(mockPost).toHaveBeenCalledWith('/api/user/logout')
    })
  })

  describe('knowledge API', () => {
    it('getKnowledgeList 应调用 GET /api/knowledge/list', async () => {
      mockGet.mockResolvedValue([])
      const { getKnowledgeList } = await import('@/api/knowledge')

      await getKnowledgeList('introduction')

      expect(mockGet).toHaveBeenCalledWith('/api/knowledge/list', { category: 'introduction' })
    })
  })

  describe('video API', () => {
    it('getVideoList 应调用 GET /api/video/list', async () => {
      mockGet.mockResolvedValue([])
      const { getVideoList } = await import('@/api/video')

      await getVideoList(1)

      expect(mockGet).toHaveBeenCalledWith('/api/video/list', { songId: 1 })
    })

    it('getVideoList 不传参应仅传songId', async () => {
      mockGet.mockResolvedValue([])
      const { getVideoList } = await import('@/api/video')

      await getVideoList(42)

      expect(mockGet).toHaveBeenCalledWith('/api/video/list', { songId: 42 })
    })
  })
})