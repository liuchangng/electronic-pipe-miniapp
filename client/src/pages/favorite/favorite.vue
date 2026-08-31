<template>
  <view class="page">
    <!-- Tab切换 -->
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: activeTab === 'song' }" @click="switchTab('song')">
        <text class="tab-text">曲谱收藏</text>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'video' }" @click="switchTab('video')">
        <text class="tab-text">视频收藏</text>
      </view>
    </view>

    <!-- 曲谱收藏 -->
    <template v-if="activeTab === 'song'">
      <view v-if="!loading && songs.length === 0" class="empty-state">
        <text class="empty-icon">♡</text>
        <text class="empty-text">还没有收藏曲谱</text>
        <text class="empty-hint">去首页发现喜欢的曲谱吧</text>
        <view class="empty-action" @click="goToIndex">
          <text class="empty-action-text">去首页看看</text>
        </view>
      </view>
      <scroll-view v-else class="favorite-list" scroll-y :show-scrollbar="false">
        <view v-for="song in songs" :key="'s-'+song.id" class="song-cell">
          <view class="song-main" @click="goToSongDetail(song.id)">
            <view class="song-icon" :style="{ backgroundColor: song.color || 'var(--color-primary)' }">
              <text class="song-icon-text">{{ song.icon || song.title.charAt(0) }}</text>
            </view>
            <view class="song-content">
              <text class="song-title">{{ song.title }}</text>
              <text class="song-subtitle">{{ song.author }}</text>
            </view>
            <text class="song-arrow">›</text>
          </view>
          <view class="song-unfav" @click="handleUnfavoriteSong(song)">
            <text class="unfav-text">取消收藏</text>
          </view>
        </view>
        <view v-if="loading" class="loading">
          <text class="loading-text">加载中...</text>
        </view>
        <view class="safe-area"></view>
      </scroll-view>
    </template>

    <!-- 视频收藏 -->
    <template v-if="activeTab === 'video'">
      <view v-if="!videoLoading && videos.length === 0" class="empty-state">
        <text class="empty-icon">🎬</text>
        <text class="empty-text">还没有收藏视频</text>
        <text class="empty-hint">去教程页发现喜欢的视频吧</text>
        <view class="empty-action" @click="goToTutorial">
          <text class="empty-action-text">去教程看看</text>
        </view>
      </view>
      <scroll-view v-else class="favorite-list" scroll-y :show-scrollbar="false">
        <view v-for="video in videos" :key="'v-'+video.id" class="video-cell">
          <view class="video-main" @click="goToVideoDetail(video)">
            <view class="video-thumb" :style="{ backgroundColor: getThumbColor(video.id) }">
              <text class="thumb-text">{{ video.title.charAt(0) }}</text>
              <view class="play-overlay-small">
                <text class="play-icon-small">▶</text>
              </view>
            </view>
            <view class="video-content">
              <text class="video-title">{{ video.title }}</text>
              <text v-if="video.songTitle" class="video-subtitle">♪ {{ video.songTitle }}</text>
              <view class="video-stats">
                <text class="stat-item">👍 {{ video.likeCount || 0 }}</text>
                <text class="stat-item">♡ {{ video.favoriteCount || 0 }}</text>
              </view>
            </view>
            <text class="song-arrow">›</text>
          </view>
          <view class="song-unfav" @click="handleUnfavoriteVideo(video)">
            <text class="unfav-text">取消收藏</text>
          </view>
        </view>
        <view v-if="videoLoading" class="loading">
          <text class="loading-text">加载中...</text>
        </view>
        <view class="safe-area"></view>
      </scroll-view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getFavoriteDetail, removeFavorite } from '@/api/favorite'
import { getVideoFavoriteDetail, toggleVideoFavorite } from '@/api/videoInteraction'

const activeTab = ref<'song' | 'video'>('song')
const songs = ref<any[]>([])
const videos = ref<any[]>([])
const loading = ref(false)
const videoLoading = ref(false)

onLoad(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateBack({ fail: () => { uni.switchTab({ url: '/pages/profile/profile' }) } })
    }, 1500)
    return
  }
  loadSongs()
})

const switchTab = (tab: 'song' | 'video') => {
  activeTab.value = tab
  if (tab === 'video' && videos.value.length === 0) {
    loadVideos()
  }
}

const loadSongs = async () => {
  loading.value = true
  try {
    const data = await getFavoriteDetail()
    songs.value = data || []
  } catch (e) {
    console.error('加载曲谱收藏失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadVideos = async () => {
  videoLoading.value = true
  try {
    const data = await getVideoFavoriteDetail()
    videos.value = data || []
  } catch (e) {
    console.error('加载视频收藏失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    videoLoading.value = false
  }
}

const handleUnfavoriteSong = (song: any) => {
  uni.showModal({
    title: '提示',
    content: `确定取消收藏「${song.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await removeFavorite(song.id)
          songs.value = songs.value.filter(s => s.id !== song.id)
          uni.showToast({ title: '已取消收藏', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const handleUnfavoriteVideo = (video: any) => {
  uni.showModal({
    title: '提示',
    content: `确定取消收藏「${video.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await toggleVideoFavorite(video.id)
          videos.value = videos.value.filter(v => v.id !== video.id)
          uni.showToast({ title: '已取消收藏', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const goToSongDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}

const goToVideoDetail = (video: any) => {
  const url = `/pages/detail/video?url=${encodeURIComponent(video.videoUrl || '')}&title=${encodeURIComponent(video.title)}&desc=${encodeURIComponent(video.description || '')}&id=${video.id}`
  uni.navigateTo({ url })
}

const goToIndex = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

const goToTutorial = () => {
  uni.switchTab({ url: '/pages/tutorial/tutorial' })
}

const thumbColors = ['#007AFF', '#5856D6', '#34C759', '#FF9500', '#FF3B30', '#AF52DE', '#5AC8FA', '#FF2D55']
const getThumbColor = (id: number) => {
  return thumbColors[(id - 1) % thumbColors.length]
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary, #F2F2F7);
}

/* ============ Tab切换 ============ */
.tab-bar {
  display: flex;
  background-color: var(--bg-secondary, #FFFFFF);
  border-bottom: 0.5px solid var(--color-gray-3, #E5E5EA);
  padding: 0 16px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  position: relative;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 30%;
  right: 30%;
  height: 2px;
  background-color: var(--color-primary, #007AFF);
  border-radius: 1px;
}

.tab-text {
  font-size: 15px;
  color: var(--text-tertiary, #8E8E93);
  font-weight: 500;
}

.tab-item.active .tab-text {
  color: var(--color-primary, #007AFF);
  font-weight: 600;
}

/* ============ 空状态 ============ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 120px;
}

.empty-icon {
  font-size: 64px;
  color: var(--text-placeholder, #C7C7CC);
  margin-bottom: 16px;
}

.empty-text {
  font-size: var(--font-size-subhead, 16px);
  color: var(--text-tertiary, #8E8E93);
  margin-bottom: 8px;
}

.empty-hint {
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-placeholder, #C7C7CC);
  margin-bottom: 20px;
}

.empty-action {
  padding: 10px 32px;
  background-color: var(--color-primary, #007AFF);
  border-radius: 20px;
}

.empty-action:active {
  opacity: 0.85;
}

.empty-action-text {
  font-size: var(--font-size-footnote, 15px);
  color: #FFFFFF;
  font-weight: 500;
}

/* ============ 收藏列表 ============ */
.favorite-list {
  height: calc(100vh - 50px);
  padding: 0;
}

/* 曲谱行 */
.song-cell {
  background-color: var(--bg-secondary, #FFFFFF);
  border-bottom: 0.5px solid var(--color-gray-3, #E5E5EA);
}

.song-main {
  display: flex;
  align-items: center;
  padding: 14px 16px;
}

.song-main:active {
  background-color: var(--color-primary-lighter, rgba(0, 122, 255, 0.04));
}

.song-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.song-icon-text {
  font-size: 20px;
  color: #FFFFFF;
  font-weight: 600;
}

.song-content {
  flex: 1;
  min-width: 0;
}

.song-title {
  display: block;
  font-size: var(--font-size-subhead, 16px);
  color: var(--text-primary, #1C1C1E);
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-subtitle {
  display: block;
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary, #8E8E93);
}

.song-arrow {
  font-size: 16px;
  color: var(--text-placeholder, #C7C7CC);
  flex-shrink: 0;
  margin-left: 8px;
}

.song-unfav {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 16px 10px;
}

.unfav-text {
  font-size: var(--font-size-caption2, 12px);
  color: var(--color-danger, #FF3B30);
  padding: 4px 12px;
  border: 0.5px solid var(--color-danger, #FF3B30);
  border-radius: 14px;
}

.song-unfav:active {
  opacity: 0.7;
}

/* 视频行 */
.video-cell {
  background-color: var(--bg-secondary, #FFFFFF);
  border-bottom: 0.5px solid var(--color-gray-3, #E5E5EA);
}

.video-main {
  display: flex;
  align-items: center;
  padding: 12px 16px;
}

.video-main:active {
  background-color: var(--color-primary-lighter, rgba(0, 122, 255, 0.04));
}

.video-thumb {
  width: 80px;
  height: 45px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  position: relative;
}

.thumb-text {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.2);
  font-weight: 700;
}

.play-overlay-small {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 28px;
  height: 28px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon-small {
  font-size: 12px;
  color: #FFFFFF;
  margin-left: 2px;
}

.video-content {
  flex: 1;
  min-width: 0;
}

.video-title {
  display: block;
  font-size: var(--font-size-footnote, 15px);
  color: var(--text-primary, #1C1C1E);
  font-weight: 500;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-subtitle {
  display: block;
  font-size: var(--font-size-caption2, 12px);
  color: var(--color-primary, #007AFF);
  margin-bottom: 3px;
}

.video-stats {
  display: flex;
  gap: 10px;
}

.stat-item {
  font-size: var(--font-size-caption2, 11px);
  color: var(--text-tertiary, #8E8E93);
}

/* ============ 加载状态 ============ */
.loading {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.loading-text {
  font-size: var(--font-size-footnote, 14px);
  color: var(--text-tertiary, #8E8E93);
}

.safe-area {
  height: 40px;
}
</style>