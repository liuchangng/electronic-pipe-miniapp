/**
 * 首页组件逻辑测试
 * @vitest-environment happy-dom
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import IndexPage from '@/pages/index/index.vue'

// 模拟 @dcloudio/uni-app 生命周期钩子
vi.mock('@dcloudio/uni-app', () => ({
  onShareAppMessage: vi.fn(),
  onShareTimeline: vi.fn(),
  onShow: vi.fn(),
  onLoad: vi.fn(),
  onReady: vi.fn(),
  onHide: vi.fn(),
  onUnload: vi.fn(),
  onPullDownRefresh: vi.fn(),
  onReachBottom: vi.fn(),
}))

// 模拟 API
vi.mock('@/api/song', () => ({
  getSongPage: vi.fn(() => Promise.resolve({
    list: [
      { id: 1, title: '天空之城', author: '久石让', favoriteCount: 100 },
      { id: 2, title: '千与千寻', author: '久石让', favoriteCount: 80 },
    ],
    total: 2,
  })),
  getSongList: vi.fn(() => Promise.resolve([
    { id: 1, title: '天空之城', author: '久石让', favoriteCount: 100 },
    { id: 2, title: '千与千寻', author: '久石让', favoriteCount: 80 },
  ])),
}))

vi.mock('@/api/banner', () => ({
  getBannerList: vi.fn(() => Promise.resolve([
    { id: 1, title: '推荐曲谱' },
    { id: 2, title: '新手入门' },
  ])),
}))

vi.mock('@/api/favorite', () => ({
  getFavoriteList: vi.fn(() => Promise.resolve([])),
}))

// 模拟 uni-app API
const mockNavigateTo = vi.fn()
;(globalThis as any).uni = {
  navigateTo: mockNavigateTo,
  switchTab: vi.fn(),
  showToast: vi.fn(),
  getStorageSync: vi.fn(() => ''),
}

// 模拟 getCurrentPages
;(globalThis as any).getCurrentPages = vi.fn(() => [
  { route: 'pages/index/index' },
])

describe('首页组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应渲染3个分类Tab', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    const items = wrapper.findAll('.segment-item')
    expect(items).toHaveLength(3)
  })

  it('默认选中热门Tab', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    const items = wrapper.findAll('.segment-item')
    expect(items[0].classes()).toContain('active')
  })

  it('应渲染曲谱列表', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    const cells = wrapper.findAll('.song-cell')
    expect(cells.length).toBeGreaterThanOrEqual(1)
  })

  it('点击曲谱应导航到详情页', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    const cells = wrapper.findAll('.song-cell')
    if (cells.length > 0) {
      await cells[0].trigger('click')
      expect(mockNavigateTo).toHaveBeenCalled()
      const callArg = mockNavigateTo.mock.calls[0][0]
      expect(callArg.url).toMatch(/\/pages\/detail\/detail\?id=/)
    }
  })

  it('应渲染Banner轮播区域', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    expect(wrapper.find('.banner-container').exists()).toBe(true)
  })

  it('getGradient应返回渐变色字符串', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    const vm = wrapper.vm as any
    const gradient = vm.getGradient(1)
    expect(gradient).toContain('linear-gradient')
    expect(gradient).toContain('#007AFF')
  })

  it('getGradient不同id应返回不同渐变', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    const vm = wrapper.vm as any
    const g1 = vm.getGradient(1)
    const g2 = vm.getGradient(2)
    expect(g1).not.toBe(g2)
  })

  it('应渲染搜索栏', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    expect(wrapper.find('.search-bar').exists()).toBe(true)
  })

  it('应渲染TabBar组件', async () => {
    const wrapper = mount(IndexPage)
    await flushPromises()
    expect(wrapper.findComponent({ name: 'TabBar' }).exists() || wrapper.find('.tabbar').exists()).toBe(true)
  })
})