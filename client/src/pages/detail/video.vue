<template>
  <view class="page">
    <!-- 视频播放器区域 -->
    <view class="video-container">
      <video
        v-if="videoUrl"
        id="detailVideoPlayer"
        :src="videoUrl"
        :autoplay="true"
        :controls="true"
        :show-fullscreen-btn="false"
        :show-play-btn="true"
        :enable-progress-gesture="true"
        :enable-play-gesture="true"
        :loop="false"
        :muted="false"
        object-fit="contain"
        class="video-player"
        @error="onError"
        @fullscreenchange="onFullscreenChange"
      />
      <!-- 自定义全屏按钮 -->
      <view v-if="videoUrl" class="fullscreen-btn" @click.stop="requestFullscreen">
        <text class="fullscreen-icon">⛶</text>
      </view>
      <view v-else class="no-video">
        <text class="no-video-icon">⚠</text>
        <text class="no-video-text">视频加载失败</text>
      </view>
    </view>

    <!-- 视频信息区 -->
    <view class="info-section">
      <text class="info-title">{{ videoTitle }}</text>
      <view class="info-meta">
        <text class="meta-tag">教程视频</text>
      </view>
    </view>

    <!-- 互动操作栏 -->
    <view class="action-bar">
      <view class="action-item" @click="handleLike">
        <text :class="isLiked ? 'action-icon liked' : 'action-icon'">👍</text>
        <text class="action-label">{{ likeCount }}</text>
      </view>
      <view class="action-item" @click="showLikeUsersPanel">
        <text class="action-icon">👥</text>
        <text class="action-label">赞列表</text>
      </view>
      <view class="action-item" @click="handleFavorite">
        <text :class="isFavorited ? 'action-icon favorited' : 'action-icon'">{{ isFavorited ? '♥' : '♡' }}</text>
        <text class="action-label">{{ favoriteCount }}</text>
      </view>
      <view class="action-item" @click="showFavUsersPanel">
        <text class="action-icon">👥</text>
        <text class="action-label">藏列表</text>
      </view>
      <view class="action-item" @click="handleShare">
        <text class="action-icon">↗</text>
        <text class="action-label">{{ shareCount }}</text>
      </view>
    </view>

    <!-- 视频描述 -->
    <view v-if="videoDesc" class="desc-section">
      <text class="desc-title">简介</text>
      <text class="desc-content">{{ videoDesc }}</text>
    </view>

    <!-- 点赞用户列表弹窗 -->
    <view v-if="showLikePanel" class="users-overlay" @click="showLikePanel = false">
      <view class="users-panel" @click.stop>
        <view class="users-header">
          <text class="users-title">点赞用户 ({{ likeUsers.length }})</text>
          <text class="users-close" @click="showLikePanel = false">✕</text>
        </view>
        <scroll-view scroll-y class="users-list">
          <view v-if="likeUsers.length === 0" class="users-empty">
            <text>暂无点赞用户</text>
          </view>
          <view v-for="user in likeUsers" :key="user.id" class="user-item">
            <image v-if="user.avatar" :src="user.avatar" class="user-avatar" mode="aspectFill" />
            <view v-else class="user-avatar-placeholder">
              <text>{{ (user.nickname || '?')[0] }}</text>
            </view>
            <text class="user-name">{{ user.nickname || '匿名用户' }}</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 收藏用户列表弹窗 -->
    <view v-if="showFavPanel" class="users-overlay" @click="showFavPanel = false">
      <view class="users-panel" @click.stop>
        <view class="users-header">
          <text class="users-title">收藏用户 ({{ favUsers.length }})</text>
          <text class="users-close" @click="showFavPanel = false">✕</text>
        </view>
        <scroll-view scroll-y class="users-list">
          <view v-if="favUsers.length === 0" class="users-empty">
            <text>暂无收藏用户</text>
          </view>
          <view v-for="user in favUsers" :key="user.id" class="user-item">
            <image v-if="user.avatar" :src="user.avatar" class="user-avatar" mode="aspectFill" />
            <view v-else class="user-avatar-placeholder">
              <text>{{ (user.nickname || '?')[0] }}</text>
            </view>
            <text class="user-name">{{ user.nickname || '匿名用户' }}</text>
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
import { toggleVideoLike, toggleVideoFavorite, checkVideoLike, checkVideoFavorite, recordVideoShare, getVideoLikeUsers, getVideoFavoriteUsers } from '@/api/videoInteraction'

// 配置分享给朋友
onShareAppMessage(() => {
  return {
    title: videoTitle.value ? `${videoTitle.value} - 电吹管教程` : '电吹管视频教程',
    path: `/pages/detail/video?url=${encodeURIComponent(videoUrl.value)}&title=${encodeURIComponent(videoTitle.value)}&desc=${encodeURIComponent(videoDesc.value)}&id=${videoId.value}`,
    imageUrl: ''
  }
})

// 配置分享到朋友圈
onShareTimeline(() => {
  return {
    title: videoTitle.value ? `${videoTitle.value} - 电吹管教程` : '电吹管视频教程',
    query: `url=${encodeURIComponent(videoUrl.value)}&title=${encodeURIComponent(videoTitle.value)}&desc=${encodeURIComponent(videoDesc.value)}&id=${videoId.value}`
  }
})

// 视频基础数据
const videoUrl = ref('')
const videoTitle = ref('')
const videoDesc = ref('')
const videoId = ref(0)

// 互动状态
const isLiked = ref(false)
const isFavorited = ref(false)
const likeCount = ref(0)
const favoriteCount = ref(0)
const shareCount = ref(0)

// 用户列表面板
const showLikePanel = ref(false)
const showFavPanel = ref(false)
const likeUsers = ref<any[]>([])
const favUsers = ref<any[]>([])

onLoad((options: any) => {
  if (options?.url) {
    videoUrl.value = decodeURIComponent(options.url)
  }
  if (options?.title) {
    videoTitle.value = decodeURIComponent(options.title)
  }
  if (options?.desc) {
    videoDesc.value = decodeURIComponent(options.desc)
  }
  if (options?.id) {
    videoId.value = Number(options.id)
  }
  // 初始化计数（从URL参数或默认0）
  likeCount.value = Number(options?.likeCount) || 0
  favoriteCount.value = Number(options?.favoriteCount) || 0
  shareCount.value = Number(options?.shareCount) || 0
  // 加载互动状态
  loadInteractionStatus()
})

const onError = () => {
  uni.showToast({ title: '视频播放出错', icon: 'none' })
}

// 全屏处理
const isFullscreen = ref(false)
const onFullscreenChange = (e: any) => {
  isFullscreen.value = e.detail.fullScreen
}

const requestFullscreen = () => {
  try {
    const ctx = uni.createVideoContext('detailVideoPlayer')
    ctx.requestFullScreen({ direction: 90 })
  } catch (e) {
    console.error('进入全屏失败', e)
  }
}

// 加载互动状态
const loadInteractionStatus = async () => {
  const token = uni.getStorageSync('token')
  if (!token || !videoId.value) return
  try {
    const [likeData, favData] = await Promise.all([
      checkVideoLike(videoId.value).catch(() => null),
      checkVideoFavorite(videoId.value).catch(() => null)
    ])
    if (likeData) isLiked.value = likeData.liked ?? false
    if (favData) isFavorited.value = favData.favorited ?? false
  } catch (e) {
    console.error('加载互动状态失败', e)
  }
}

// 互动操作
const handleLike = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (!videoId.value) return
  try {
    const data = await toggleVideoLike(videoId.value)
    isLiked.value = data?.liked ?? !isLiked.value
    likeCount.value += isLiked.value ? 1 : -1
    uni.showToast({ title: isLiked.value ? '已点赞' : '取消点赞', icon: 'none' })
  } catch (e) {
    console.error('点赞操作失败', e)
  }
}

const handleFavorite = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (!videoId.value) return
  try {
    const data = await toggleVideoFavorite(videoId.value)
    isFavorited.value = data?.favorited ?? !isFavorited.value
    favoriteCount.value += isFavorited.value ? 1 : -1
    uni.showToast({ title: isFavorited.value ? '已收藏' : '取消收藏', icon: 'none' })
  } catch (e) {
    console.error('收藏操作失败', e)
  }
}

const handleShare = async () => {
  // 记录分享计数
  if (videoId.value) {
    try {
      await recordVideoShare(videoId.value)
      shareCount.value += 1
    } catch (e) {
      // 忽略计数失败
    }
  }
  uni.showToast({ title: '点击右上角「...」分享给朋友', icon: 'none' })
}

// 点赞用户列表面板
const showLikeUsersPanel = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (!videoId.value) return
  try {
    const data = await getVideoLikeUsers(videoId.value)
    likeUsers.value = data || []
    showLikePanel.value = true
  } catch (e) {
    console.error('获取点赞用户列表失败', e)
  }
}

// 收藏用户列表面板
const showFavUsersPanel = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (!videoId.value) return
  try {
    const data = await getVideoFavoriteUsers(videoId.value)
    favUsers.value = data || []
    showFavPanel.value = true
  } catch (e) {
    console.error('获取收藏用户列表失败', e)
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #fff;
}

/* ============ 视频播放器区域 ============ */
.video-container {
  width: 100%;
  height: 56.25vw;
  background-color: #000;
  position: relative;
}

.video-player {
  width: 100%;
  height: 100%;
}

/* 自定义全屏按钮 */
.fullscreen-btn {
  position: absolute;
  right: 8px;
  bottom: 44px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  z-index: 10;
}

.fullscreen-btn:active {
  opacity: 0.7;
}

.fullscreen-icon {
  font-size: 18px;
  color: #FFFFFF;
}

.no-video {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #000;
}

.no-video-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.no-video-text {
  font-size: 14px;
  color: #999;
}

/* ============ 视频信息区 ============ */
.info-section {
  padding: 16px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.info-title {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.info-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-tag {
  font-size: 12px;
  color: #07c160;
  background-color: rgba(7, 193, 96, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

/* ============ 互动操作栏 ============ */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 60px;
}

.action-item:active {
  opacity: 0.7;
}

.action-icon {
  font-size: 22px;
  color: #999;
}

.action-icon.liked {
  color: #ff9500;
}

.action-icon.favorited {
  color: #ff4d4f;
}

.action-label {
  font-size: 11px;
  color: #999;
}

/* ============ 视频描述 ============ */
.desc-section {
  padding: 16px;
  background-color: #fff;
  margin-top: 8px;
}

.desc-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.desc-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.safe-area {
  height: 40px;
}

/* ============ 用户列表弹窗 ============ */
.users-overlay {
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

.users-panel {
  width: 100%;
  max-height: 60vh;
  background-color: #fff;
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
}

.users-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.users-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.users-close {
  font-size: 20px;
  color: #999;
  padding: 4px 8px;
}

.users-list {
  max-height: 50vh;
  padding: 8px 16px;
}

.users-empty {
  padding: 40px 0;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.user-avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(7, 193, 96, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #07c160;
  font-size: 16px;
  font-weight: 700;
}

.user-name {
  font-size: 14px;
  color: #333;
}
</style>