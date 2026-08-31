/**
 * TabBar 组件测试
 * @vitest-environment happy-dom
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import TabBar from '@/components/TabBar.vue'

// 模拟 uni-app API
const mockSwitchTab = vi.fn()
;(globalThis as any).uni = {
  switchTab: mockSwitchTab,
  showToast: vi.fn(),
}

// 模拟 getCurrentPages
const mockGetCurrentPages = vi.fn(() => [
  { route: 'pages/index/index' },
])
;(globalThis as any).getCurrentPages = mockGetCurrentPages

describe('TabBar 组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockGetCurrentPages.mockReturnValue([
      { route: 'pages/index/index' },
    ])
  })

  it('应渲染3个Tab项', () => {
    const wrapper = mount(TabBar)
    const items = wrapper.findAll('.tab-item')
    expect(items).toHaveLength(3)
  })

  it('应显示正确的Tab标签', () => {
    const wrapper = mount(TabBar)
    const labels = wrapper.findAll('.tab-label')
    expect(labels[0].text()).toBe('首页')
    expect(labels[1].text()).toBe('教程')
    expect(labels[2].text()).toBe('我的')
  })

  it('当前页面Tab应高亮', async () => {
    mockGetCurrentPages.mockReturnValue([
      { route: 'pages/index/index' },
    ])
    const wrapper = mount(TabBar)
    await flushPromises()
    const items = wrapper.findAll('.tab-item')
    expect(items[0].classes()).toContain('active')
    expect(items[1].classes()).not.toContain('active')
    expect(items[2].classes()).not.toContain('active')
  })

  it('教程页Tab应高亮', async () => {
    mockGetCurrentPages.mockReturnValue([
      { route: 'pages/tutorial/tutorial' },
    ])
    const wrapper = mount(TabBar)
    await flushPromises()
    const items = wrapper.findAll('.tab-item')
    expect(items[0].classes()).not.toContain('active')
    expect(items[1].classes()).toContain('active')
    expect(items[2].classes()).not.toContain('active')
  })

  it('点击非当前Tab应调用uni.reLaunch', async () => {
    mockGetCurrentPages.mockReturnValue([
      { route: 'pages/index/index' },
    ])
    const wrapper = mount(TabBar)
    const items = wrapper.findAll('.tab-item')

    await items[1].trigger('click')
    expect(mockSwitchTab).toHaveBeenCalledWith({
      url: '/pages/tutorial/tutorial',
    })
  })

  it('点击当前Tab不应调用uni.reLaunch', async () => {
    mockGetCurrentPages.mockReturnValue([
      { route: 'pages/index/index' },
    ])
    const wrapper = mount(TabBar)
    const items = wrapper.findAll('.tab-item')

    await items[0].trigger('click')
    expect(mockSwitchTab).not.toHaveBeenCalled()
  })

  it('应渲染3种图标类型', () => {
    const wrapper = mount(TabBar)
    expect(wrapper.find('.icon-home').exists()).toBe(true)
    expect(wrapper.find('.icon-video').exists()).toBe(true)
    expect(wrapper.find('.icon-person').exists()).toBe(true)
  })

  it('激活态Tab标签应有active类', async () => {
    mockGetCurrentPages.mockReturnValue([
      { route: 'pages/profile/profile' },
    ])
    const wrapper = mount(TabBar)
    await flushPromises()
    const labels = wrapper.findAll('.tab-label')
    expect(labels[2].classes()).toContain('active')
  })
})