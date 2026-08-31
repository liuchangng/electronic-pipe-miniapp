<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-container" @click="goToSearch">
      <view class="search-bar">
        <view class="search-icon-wrap">
          <view class="search-icon-circle"></view>
          <view class="search-icon-handle"></view>
        </view>
        <text class="search-placeholder-text">搜索曲谱、作者</text>
      </view>
    </view>

    <!-- Banner 轮播 -->
    <view class="banner-container">
      <swiper 
        class="banner-swiper" 
        :indicator-dots="false"
        :autoplay="true" 
        :interval="4000" 
        :duration="500"
        :circular="true"
        @change="onBannerChange"
      >
        <swiper-item v-for="item in bannerList" :key="item.id">
          <view class="banner-card" @click="handleBannerClick(item)">
            <view class="banner-gradient" :style="{ background: getGradient(item.id) }">
              <view class="banner-content">
                <text class="banner-title">{{ item.title }}</text>
                <view class="banner-action">
                  <text class="banner-subtitle">了解更多</text>
                  <text class="banner-arrow">→</text>
                </view>
              </view>
            </view>
          </view>
        </swiper-item>
      </swiper>
      <!-- 自定义指示器 -->
      <view class="banner-dots">
        <view 
          v-for="(item, index) in bannerList" 
          :key="item.id"
          :class="['dot', currentBanner === index ? 'active' : '']"
        ></view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-entry">
      <view class="quick-entry-inner">
        <view class="quick-entry-item" @click="goToKnowledge">
          <view class="quick-icon bg-knowledge">
            <text class="quick-icon-text">识</text>
          </view>
          <text class="quick-label">基础知识</text>
        </view>
        <view class="quick-entry-item" @click="goToFavorite">
          <view class="quick-icon bg-favorite">
            <text class="quick-icon-text">藏</text>
          </view>
          <text class="quick-label">我的收藏</text>
        </view>
        <view class="quick-entry-item" @click="goToRequestScore">
          <view class="quick-icon bg-request">
            <text class="quick-icon-text">谱</text>
          </view>
          <text class="quick-label">我要求谱</text>
        </view>
        <view class="quick-entry-item" @click="goToAbout">
          <view class="quick-icon bg-about">
            <text class="quick-icon-text">关于</text>
          </view>
          <text class="quick-label">关于我们</text>
        </view>
      </view>
    </view>

    <!-- 分类 Tab -->
    <view class="segment-control">
      <view 
        v-for="tab in tabs" 
        :key="tab.value"
        :class="['segment-item', activeTab === tab.value ? 'active' : '']"
        @click="switchTab(tab.value)"
      >
        <text class="segment-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 曲谱列表 -->
    <scroll-view 
      class="song-list" 
      scroll-y 
      :show-scrollbar="false"
      @scrolltolower="loadMore"
    >
      <view 
        v-for="song in songList" 
        :key="song.id" 
        class="song-cell card card-interactive"
        @click="goToDetail(song.id)"
      >
        <view class="song-icon" :style="{ backgroundColor: song.color || 'var(--color-primary)' }">
          <text class="song-icon-text">{{ song.icon || song.title.charAt(0) }}</text>
        </view>
        <view class="song-content">
          <text class="song-title">{{ song.title }}</text>
          <text class="song-subtitle">{{ song.description || song.author }}</text>
        </view>
        <view class="song-meta">
          <view class="song-fav-badge">
            <text class="song-fav-icon">♥</text>
            <text class="song-count">{{ song.favoriteCount || 0 }}</text>
          </view>
          <text class="song-arrow">›</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading && songList.length === 0" class="loading-indicator">
        <view class="loading-spinner"></view>
      </view>
      <view v-else-if="songList.length === 0" class="empty-state">
        <text class="empty-icon">🎵</text>
        <text class="empty-text">暂无曲谱</text>
        <text class="empty-hint">换个分类看看</text>
      </view>
      <view v-else-if="!hasMore && songList.length > 0" class="no-more">
        <text class="no-more-text">没有更多了</text>
      </view>
      <view v-if="loading && songList.length > 0" class="loading-indicator">
        <view class="loading-spinner"></view>
      </view>
      
      <!-- 底部安全区 -->
      <view class="safe-area-bottom"></view>
    </scroll-view>

    <!-- TabBar -->
    <TabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { getSongPage } from '@/api/song'
import { getBannerList } from '@/api/banner'
import { useLogin } from '@/hooks/useLogin'
import TabBar from '@/components/TabBar.vue'

// 配置分享给朋友
onShareAppMessage(() => {
  return {
    title: '电吹管曲谱 - 发现好曲谱',
    path: '/pages/index/index'
  }
})

// 配置分享到朋友圈
onShareTimeline(() => {
  return {
    title: '电吹管曲谱 - 发现好曲谱'
  }
})

const activeTab = ref('hot')
const currentBanner = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const hasMore = ref(true)
const PAGE_SIZE = 10

const tabs = [
  { label: '热门', value: 'hot' },
  { label: '最新', value: 'new' }
]

const bannerList = ref<any[]>([])
const songList = ref<any[]>([])

// Banner 渐变色（主色系，禁止紫色渐变）
const gradients = [
  'linear-gradient(135deg, #007AFF 0%, #5856D6 100%)',
  'linear-gradient(135deg, #34C759 0%, #007AFF 100%)',
  'linear-gradient(135deg, #FF9500 0%, #FF2D55 100%)',
]

const getGradient = (id: number) => {
  return gradients[(id - 1) % gradients.length]
}

onMounted(() => {
  loadBanner()
  loadSongs()
})

const onBannerChange = (e: any) => {
  currentBanner.value = e.detail.current
}

const loadBanner = async () => {
  try {
    const data = await getBannerList()
    bannerList.value = data || []
  } catch (e) {
    console.error('加载 Banner 失败', e)
  }
}

const loadSongs = async (loadMore = false) => {
  if (loading.value) return
  loading.value = true
  try {
    if (!loadMore) {
      currentPage.value = 1
      songList.value = []
    }
    const data = await getSongPage({ tab: activeTab.value, page: currentPage.value, pageSize: PAGE_SIZE })
    const list = data?.list || []
    if (loadMore) {
      songList.value = [...songList.value, ...list]
    } else {
      songList.value = list
    }
    hasMore.value = list.length >= PAGE_SIZE
    currentPage.value++
  } catch (e) {
    console.error('加载曲谱失败', e)
  } finally {
    loading.value = false
  }
}

const goToSearch = () => {
  uni.navigateTo({ url: '/pages/search/search' })
}

const switchTab = (tab: string) => {
  activeTab.value = tab
  loadSongs()
}

const loadMore = () => {
  if (!hasMore.value || loading.value) return
  loadSongs(true)
}

const goToDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${id}`
  })
}

const handleBannerClick = (item: any) => {
  if (!item.linkUrl) return
  if (item.linkUrl.startsWith('/pages/')) {
    uni.navigateTo({ url: item.linkUrl })
  } else if (item.linkUrl.startsWith('http')) {
    uni.showToast({ title: '暂不支持外部链接', icon: 'none' })
  }
}

// 快捷入口跳转
const goToKnowledge = () => {
  uni.navigateTo({ url: '/pages/knowledge/knowledge' })
}

const goToFavorite = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/profile/profile' })
    }, 1500)
    return
  }
  uni.navigateTo({ url: '/pages/favorite/favorite' })
}

const goToRequestScore = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/profile/profile' })
    }, 1500)
    return
  }
  uni.navigateTo({ url: '/pages/song-request/song-request' })
}

const goToAbout = () => {
  uni.navigateTo({ url: '/pages/about/about' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary);
}

/* ============ 搜索栏 ============ */
.search-container {
  padding: 12px 16px 8px;
  background-color: var(--bg-secondary);
}

.search-bar {
  display: flex;
  align-items: center;
  background-color: var(--color-gray-7);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  height: 40px;
  transition: background-color var(--animation-fast);
}

.search-bar:active {
  background-color: var(--color-gray-6);
}

.search-icon-wrap {
  position: relative;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  flex-shrink: 0;
}

.search-icon-circle {
  position: absolute;
  top: 0;
  left: 0;
  width: 11px;
  height: 11px;
  border: 2px solid var(--text-tertiary);
  border-radius: 50%;
  opacity: 0.5;
}

.search-icon-handle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 6px;
  height: 2px;
  background: var(--text-tertiary);
  transform: rotate(45deg);
  opacity: 0.5;
}

.search-placeholder-text {
  color: var(--text-placeholder);
  font-size: var(--font-size-subhead);
  flex: 1;
}

/* ============ Banner 轮播 ============ */
.banner-container {
  position: relative;
  padding: 12px 16px 16px;
}

.banner-swiper {
  height: 170px;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.banner-card {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.banner-gradient {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px 20px;
  box-sizing: border-box;
  position: relative;
}

/* 毛玻璃纹理叠加层 */
.banner-gradient::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.06);
  pointer-events: none;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: var(--font-size-title2);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.banner-action {
  display: flex;
  align-items: center;
  gap: 4px;
}

.banner-subtitle {
  font-size: var(--font-size-caption1);
  color: rgba(255, 255, 255, 0.85);
  font-weight: var(--font-weight-medium);
}

.banner-arrow {
  font-size: var(--font-size-caption1);
  color: rgba(255, 255, 255, 0.85);
  transition: transform var(--animation-fast);
}

.banner-dots {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background-color: rgba(255, 255, 255, 0.4);
  transition: all var(--animation-normal);
}

.dot.active {
  width: 20px;
  background-color: var(--color-white);
}

/* ============ 快捷入口 ============ */
.quick-entry {
  padding: 0 16px 12px;
  background-color: var(--bg-secondary);
}

.quick-entry-inner {
  display: flex;
  justify-content: space-around;
  padding: 12px 0 4px;
}

.quick-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-icon-text {
  color: var(--color-white);
  font-size: 20px;
  font-weight: 600;
}

.quick-label {
  font-size: 11px;
  color: var(--text-secondary);
  font-weight: 500;
}

.quick-icon.bg-knowledge {
  background: linear-gradient(135deg, #007AFF, #5AC8FA);
}

.quick-icon.bg-favorite {
  background: linear-gradient(135deg, #FF2D55, #FF6482);
}

.quick-icon.bg-request {
  background: linear-gradient(135deg, #34C759, #30D158);
}

.quick-icon.bg-about {
  background: linear-gradient(135deg, #5856D6, #AF52DE);
}

/* ============ 分段控件 ============ */
.segment-control {
  display: flex;
  margin: 0 16px 12px;
  background-color: var(--color-gray-7);
  border-radius: var(--radius-sm);
  padding: 3px;
}

.segment-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 7px;
  transition: all var(--animation-normal);
}

.segment-item.active {
  background-color: var(--bg-secondary);
  box-shadow: var(--shadow-sm);
}

.segment-text {
  font-size: var(--font-size-footnote);
  font-weight: var(--font-weight-medium);
  color: var(--text-tertiary);
}

.segment-item.active .segment-text {
  color: var(--text-primary);
  font-weight: var(--font-weight-semibold);
}

/* ============ 曲谱列表 ============ */
.song-list {
  height: calc(100vh - 430px);
}

.song-cell {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  margin: 0 16px 10px;
  gap: 12px;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.song-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.song-icon-text {
  color: var(--color-white);
  font-size: 22px;
  font-weight: var(--font-weight-bold);
}

.song-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.song-title {
  display: block;
  font-size: var(--font-size-subhead);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.song-subtitle {
  display: block;
  font-size: var(--font-size-caption1);
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.song-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.song-fav-badge {
  display: flex;
  align-items: center;
  gap: 3px;
  background-color: var(--color-primary-light);
  padding: 3px 8px;
  border-radius: var(--radius-full);
}

.song-fav-icon {
  font-size: 11px;
  color: var(--color-danger);
}

.song-count {
  font-size: var(--font-size-caption2);
  color: var(--text-tertiary);
  font-weight: var(--font-weight-medium);
}

.song-arrow {
  font-size: 18px;
  color: var(--color-gray-4);
  font-weight: 300;
}

/* ============ 加载状态 ============ */
.loading-indicator {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 2.5px solid var(--color-gray-6);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0 40px;
  text-align: center;
  width: 100%;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: var(--font-size-subhead);
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
}

.empty-hint {
  font-size: var(--font-size-caption1);
  color: var(--text-tertiary);
  margin-top: 4px;
}

.no-more {
  display: flex;
  justify-content: center;
  padding: 16px 0 8px;
}

.no-more-text {
  font-size: var(--font-size-caption1);
  color: var(--text-tertiary);
}

.safe-area-bottom {
  height: 100px;
}
</style>

<!-- #ifdef H5 -->
<style>
uni-view.song-cell {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  margin: 0 16px 10px;
  gap: 12px;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  box-sizing: border-box;
  transition: transform var(--animation-fast), box-shadow var(--animation-fast);
}

uni-view.song-cell:active {
  transform: scale(0.98);
}

uni-view.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0 40px;
  text-align: center;
  width: 100%;
}
</style>
<!-- #endif -->