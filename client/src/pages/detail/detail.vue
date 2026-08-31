<template>
  <view class="page">
    <!-- 歌曲信息卡片 -->
    <view class="song-card">
      <view class="song-header-row">
        <view class="song-info">
          <text class="song-title">{{ song.title || '加载中...' }}</text>
          <text class="song-author">{{ song.author || '未知作者' }}</text>
        </view>
        <view class="song-actions">
          <view class="action-item" @click="handleDownload">
            <view class="action-icon-wrap">
              <text class="action-icon">⬇</text>
            </view>
            <text class="action-label">下载</text>
          </view>
          <view class="action-item" @click="handleFavorite">
            <view class="action-icon-wrap" :class="isFavorited ? 'favorited' : ''">
              <text class="action-icon">{{ isFavorited ? '♥' : '♡' }}</text>
            </view>
            <text class="action-label">{{ isFavorited ? '已藏' : '收藏' }}</text>
          </view>
          <view class="action-item" @click="showFavoriteUsers">
            <view class="action-icon-wrap">
              <text class="action-icon">{{ song.favoriteCount || 0 }}</text>
            </view>
            <text class="action-label">收藏数</text>
          </view>
          <view v-if="song.videoUrl" class="action-item" @click="goToVideo">
            <view class="action-icon-wrap video">
              <text class="action-icon">▶</text>
            </view>
            <text class="action-label">视频</text>
          </view>
          <view class="action-item" @click="handleShare">
            <view class="action-icon-wrap">
              <text class="action-icon">↗</text>
            </view>
            <text class="action-label">分享</text>
          </view>
          <view class="action-item" @click="goToRequestScore">
            <view class="action-icon-wrap request">
              <text class="action-icon">✉</text>
            </view>
            <text class="action-label">求谱</text>
          </view>
        </view>
      </view>
      <text v-if="song.description" class="song-desc">{{ song.description }}</text>
    </view>

    <!-- 曲谱内容 -->
    <view class="score-content">
      <image 
        v-if="song.imageUrl" 
        :src="song.imageUrl" 
        mode="widthFix" 
        class="score-image"
        @click="previewImage"
      />
      <view v-else class="score-placeholder">
        <text class="placeholder-icon">🎵</text>
        <text class="placeholder-text">暂无曲谱图片</text>
        <text class="placeholder-hint">曲谱图片正在制作中</text>
      </view>
    </view>

    <!-- 收藏用户列表弹窗 -->
    <view v-if="showFavUsers" class="fav-users-overlay" @click="showFavUsers = false">
      <view class="fav-users-panel" @click.stop>
        <view class="fav-users-header">
          <text class="fav-users-title">收藏用户 ({{ favoriteUsers.length }})</text>
          <text class="fav-users-close" @click="showFavUsers = false">✕</text>
        </view>
        <scroll-view scroll-y class="fav-users-list">
          <view v-if="favoriteUsers.length === 0" class="fav-users-empty">
            <text>暂无收藏用户</text>
          </view>
          <view v-for="user in favoriteUsers" :key="user.id" class="fav-user-item">
            <image v-if="user.avatar" :src="user.avatar" class="fav-user-avatar" mode="aspectFill" />
            <view v-else class="fav-user-avatar-placeholder">
              <text>{{ (user.nickname || '?')[0] }}</text>
            </view>
            <text class="fav-user-name">{{ user.nickname || '匿名用户' }}</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 底部安全区 -->
    <view class="safe-area"></view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { getSongDetail } from '@/api/song'
import { getFavoriteList, addFavorite, removeFavorite, getFavoriteUsers } from '@/api/favorite'

// 配置分享给朋友
onShareAppMessage(() => {
  return {
    title: song.value.title ? `${song.value.title} - ${song.value.author || '电吹管曲谱'}` : '电吹管曲谱',
    path: `/pages/detail/detail?id=${songId.value}`,
    imageUrl: song.value.imageUrl || ''
  }
})

// 配置分享到朋友圈
onShareTimeline(() => {
  return {
    title: song.value.title ? `${song.value.title} - ${song.value.author || '电吹管曲谱'}` : '电吹管曲谱',
    query: `id=${songId.value}`,
    imageUrl: song.value.imageUrl || ''
  }
})

const song = ref<any>({})
const isFavorited = ref(false)
const songId = ref(0)
const showFavUsers = ref(false)
const favoriteUsers = ref<any[]>([])

onLoad((options: any) => {
  songId.value = Number(options?.id || 0)
  if (songId.value) {
    loadSongDetail()
    checkFavorite()
  }
})

const loadSongDetail = async () => {
  try {
    const data = await getSongDetail(songId.value)
    song.value = data || {}
  } catch (e) {
    console.error('加载曲谱详情失败', e)
  }
}

const checkFavorite = async () => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) return
    const data = await getFavoriteList()
    isFavorited.value = (data || []).includes(songId.value)
  } catch (e) {
    console.error('检查收藏状态失败', e)
  }
}

const handleDownload = () => {
  if (!song.value.imageUrl) {
    uni.showToast({ title: '暂无曲谱图片', icon: 'none' })
    return
  }
  const fullUrl = song.value.imageUrl
  uni.showLoading({ title: '下载中...' })
  uni.downloadFile({
    url: fullUrl,
    success: (res) => {
      if (res.statusCode === 200) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.showToast({ title: '已保存到相册', icon: 'success' })
          },
          fail: (err) => {
            if (err.errMsg?.includes('auth deny') || err.errMsg?.includes('authorize')) {
              uni.showModal({
                title: '提示',
                content: '需要您授权保存图片到相册',
                confirmText: '去授权',
                success: (res) => {
                  if (res.confirm) {
                    uni.openSetting({})
                  }
                }
              })
            } else {
              uni.showToast({ title: '保存失败', icon: 'none' })
            }
          }
        })
      } else {
        uni.showToast({ title: '下载失败', icon: 'none' })
      }
    },
    fail: () => {
      uni.showToast({ title: '下载失败', icon: 'none' })
    },
    complete: () => {
      uni.hideLoading()
    }
  })
}

const handleFavorite = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    if (isFavorited.value) {
      await removeFavorite(songId.value)
      isFavorited.value = false
      song.value.favoriteCount = Math.max(0, (song.value.favoriteCount || 1) - 1)
      uni.showToast({ title: '已取消收藏', icon: 'success' })
    } else {
      await addFavorite(songId.value)
      isFavorited.value = true
      song.value.favoriteCount = (song.value.favoriteCount || 0) + 1
      uni.showToast({ title: '收藏成功', icon: 'success' })
    }
  } catch (e) {
    console.error('收藏操作失败', e)
  }
}

const showFavoriteUsers = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const data = await getFavoriteUsers(songId.value)
    favoriteUsers.value = data || []
    showFavUsers.value = true
  } catch (e) {
    console.error('获取收藏用户列表失败', e)
  }
}

const handleShare = () => {
  // 微信小程序通过onShareAppMessage自动处理分享
  // 此按钮仅作视觉提示
  uni.showToast({ title: '点击右上角「...」分享给朋友', icon: 'none' })
}

const goToRequestScore = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/song-request/song-request' })
}

const goToVideo = () => {
  if (!song.value.videoUrl) return
  const url = encodeURIComponent(song.value.videoUrl)
  const title = encodeURIComponent(song.value.title || '')
  const desc = encodeURIComponent(song.value.description || '')
  uni.navigateTo({
    url: `/pages/detail/video?url=${url}&title=${title}&desc=${desc}&id=${song.value.videoId || ''}`
  })
}

const previewImage = () => {
  if (song.value.imageUrl) {
    const fullUrl = song.value.imageUrl
    uni.previewImage({ urls: [fullUrl], current: fullUrl })
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary);
}

/* ============ 歌曲信息卡片 ============ */
.song-card {
  background-color: var(--bg-secondary);
  padding: 24px 16px 16px;
}

/* 标题与操作按钮同行布局 */
.song-header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.song-info {
  flex: 1;
  min-width: 0;
}

.song-title {
  display: block;
  font-size: var(--font-size-title2);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin-bottom: 6px;
  line-height: 1.3;
}

.song-author {
  display: block;
  font-size: var(--font-size-footnote);
  color: var(--text-tertiary);
}

/* 操作按钮 - 右侧竖排 */
.song-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 4px;
}

.action-item:active {
  opacity: 0.6;
}

.action-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background-color: var(--color-gray-6);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--animation-fast);
}

.action-icon-wrap.favorited {
  background-color: rgba(255, 59, 48, 0.1);
}

.action-icon-wrap.video {
  background-color: var(--color-primary-light);
}

.action-icon-wrap.request {
  background-color: rgba(255, 149, 0, 0.1);
}

.action-icon {
  font-size: 16px;
  color: var(--text-secondary);
}

.action-icon-wrap.favorited .action-icon {
  color: var(--color-danger);
}

.action-icon-wrap.video .action-icon {
  color: var(--color-primary);
}

.action-label {
  font-size: 10px;
  color: var(--text-tertiary);
  font-weight: var(--font-weight-medium);
}

/* 描述文字 */
.song-desc {
  display: block;
  font-size: var(--font-size-caption1);
  color: var(--text-secondary);
  line-height: 1.6;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 0.5px solid var(--color-gray-3);
}

/* ============ 曲谱内容 ============ */
.score-content {
  margin-top: 10px;
  background-color: var(--bg-secondary);
  padding: 16px 0;
}

.score-image {
  width: 100%;
  display: block;
}

.score-placeholder {
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.placeholder-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.placeholder-text {
  font-size: var(--font-size-subhead);
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
  margin-bottom: 6px;
}

.placeholder-hint {
  font-size: var(--font-size-caption1);
  color: var(--text-tertiary);
}

/* ============ 收藏用户弹窗 ============ */
.fav-users-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.fav-users-panel {
  width: 100%;
  max-height: 60vh;
  background-color: var(--bg-secondary);
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
}

.fav-users-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 0.5px solid var(--color-gray-3);
}

.fav-users-title {
  font-size: var(--font-size-headline);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
}

.fav-users-close {
  font-size: 20px;
  color: var(--text-tertiary);
  padding: 4px 8px;
}

.fav-users-list {
  max-height: 50vh;
  padding: 8px 16px;
}

.fav-users-empty {
  padding: 40px 0;
  text-align: center;
  color: var(--text-tertiary);
  font-size: var(--font-size-subhead);
}

.fav-user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 0.5px solid var(--color-gray-3);
}

.fav-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.fav-user-avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--color-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 16px;
  font-weight: var(--font-weight-bold);
}

.fav-user-name {
  font-size: var(--font-size-body);
  color: var(--text-primary);
}

.safe-area {
  height: 40px;
}
</style>