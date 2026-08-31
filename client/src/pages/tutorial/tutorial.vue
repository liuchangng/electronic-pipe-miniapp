<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-container">
      <view class="search-bar">
        <view class="search-icon-wrap">
          <view class="search-icon-circle"></view>
          <view class="search-icon-handle"></view>
        </view>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索视频教程"
          placeholder-class="search-placeholder"
          confirm-type="search"
        />
        <text v-if="searchKeyword" class="search-clear" @click="clearSearch">✕</text>
      </view>
    </view>

    <!-- 曲谱分类标签 -->
    <scroll-view v-if="songs.length > 0" class="song-tabs" scroll-x enhanced :show-scrollbar="false">
      <view class="song-tabs-inner">
        <view
          class="song-tab"
          :class="{ active: activeSongId === null }"
          @click="selectSong(null)"
        >
          <text class="song-tab-text">全部</text>
        </view>
        <view
          v-for="song in songs"
          :key="song.id"
          class="song-tab"
          :class="{ active: activeSongId === song.id }"
          @click="selectSong(song.id)"
        >
          <text class="song-tab-text">{{ song.title }}</text>
        </view>
        <!-- 加载更多状态 -->
        <view v-if="loadingMore" class="load-more">
          <view class="loading-spinner-small"></view>
          <text class="load-more-text">加载中...</text>
        </view>
        <view v-else-if="!hasMore && videos.length > 0" class="load-more">
          <text class="load-more-text">没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 搜索结果提示 -->
    <view v-if="searchKeyword && filteredVideos.length > 0" class="search-hint">
      <text class="search-hint-text">找到 {{ filteredVideos.length }} 个结果</text>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && filteredVideos.length === 0" class="empty-state">
      <text class="empty-icon">{{ searchKeyword ? '🔍' : '🎬' }}</text>
      <text class="empty-text">{{ searchKeyword ? '未找到相关视频' : (activeSongId ? '该曲谱暂无视频' : '暂无视频教程') }}</text>
      <text class="empty-hint">{{ searchKeyword ? '试试其他关键词' : '敬请期待更多内容' }}</text>
      <view v-if="searchKeyword" class="empty-action" @click="clearSearch">
        <text class="empty-action-text">清除搜索</text>
      </view>
      <view v-else-if="activeSongId" class="empty-action" @click="selectSong(null)">
        <text class="empty-action-text">查看全部视频</text>
      </view>
    </view>

    <!-- 视频列表 -->
    <scroll-view
      v-else
      class="video-list"
      scroll-y
      enhanced
      :show-scrollbar="false"
      @scrolltolower="loadMoreVideos"
    >
      <view class="list-content">
        <view
          v-for="video in filteredVideos"
          :key="video.id"
          class="video-card"
        >
          <!-- 视频播放区 -->
          <view class="card-video-area" @click="togglePlay(video)">
            <video
              v-if="activeVideoId === video.id"
              :id="'video-' + video.id"
              :src="video.videoUrl"
              :autoplay="true"
              :controls="true"
              :show-fullscreen-btn="false"
              :show-play-btn="true"
              :enable-progress-gesture="true"
              :enable-play-gesture="true"
              :loop="false"
              :muted="false"
              object-fit="contain"
              class="card-video"
              @ended="onVideoEnded(video)"
              @error="onVideoError"
              @fullscreenchange="onFullscreenChange"
            />
            <!-- 自定义全屏按钮（覆盖在视频控制栏上方） -->
            <view v-if="activeVideoId === video.id" class="fullscreen-btn" @click.stop="requestFullscreen(video)">
              <text class="fullscreen-icon">⛶</text>
            </view>
            <view v-else class="video-poster" :style="{ backgroundColor: getThumbColor(video.id) }">
              <text class="poster-text">{{ video.title.charAt(0) }}</text>
              <view class="play-overlay">
                <text class="play-icon">▶</text>
              </view>
            </view>
          </view>

          <!-- 视频信息区 -->
          <view class="card-info">
            <text class="card-title">{{ video.title }}</text>
            <text v-if="video.songTitle" class="card-song-title">♪ {{ video.songTitle }}</text>
            <text v-if="video.description" class="card-desc">{{ video.description }}</text>
          </view>

          <!-- 互动操作栏 -->
          <view class="card-actions">
            <view class="action-btn" @click.stop="handleLike(video)">
              <text :class="video._liked ? 'action-emoji liked' : 'action-emoji'">👍</text>
              <text class="action-text">{{ video.likeCount || 0 }}</text>
            </view>
            <view class="action-btn" @click.stop="handleFavorite(video)">
              <text :class="video._favorited ? 'action-emoji favorited' : 'action-emoji'">{{ video._favorited ? '♥' : '♡' }}</text>
              <text class="action-text">{{ video.favoriteCount || 0 }}</text>
            </view>
            <view class="action-btn" @click.stop="handleComment(video)">
              <text class="action-emoji">💬</text>
              <text class="action-text">{{ video.commentCount || 0 }}</text>
            </view>
            <view class="action-btn" @click.stop="handleShare(video)">
              <text class="action-emoji">↗</text>
              <text class="action-text">{{ video.shareCount || 0 }}</text>
            </view>
          </view>
        </view>
        <!-- 加载更多状态 -->
        <view v-if="loadingMore" class="load-more">
          <view class="loading-spinner-small"></view>
          <text class="load-more-text">加载中...</text>
        </view>
        <view v-else-if="!hasMore && videos.length > 0" class="load-more">
          <text class="load-more-text">没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 底部 TabBar -->
    <TabBar />

    <!-- 评论弹窗 -->
    <view v-if="showCommentPopup" class="comment-popup" @click="closeCommentPopup">
      <view class="comment-panel" @click.stop>
        <view class="comment-header">
          <text class="comment-title">评论 ({{ commentCount }})</text>
          <text class="comment-close" @click="closeCommentPopup">✕</text>
        </view>
        <scroll-view class="comment-list" scroll-y :show-scrollbar="false">
          <view v-if="comments.length === 0" class="comment-empty">
            <text class="comment-empty-text">暂无评论，来说点什么吧</text>
          </view>
          <view v-for="comment in comments" :key="comment.id" class="comment-item">
            <view class="comment-avatar-wrap">
              <image v-if="comment.avatar" class="comment-avatar" :src="comment.avatar" mode="aspectFill" />
              <view v-else class="comment-avatar-placeholder">
                <text class="comment-avatar-text">{{ (comment.nickname || '用').charAt(0) }}</text>
              </view>
            </view>
            <view class="comment-body">
              <text class="comment-nickname">{{ comment.nickname || '用户' }}</text>
              <text class="comment-content">{{ comment.content }}</text>
              <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
              <!-- 子评论（回复） -->
              <view v-if="comment.children && comment.children.length > 0" class="comment-replies">
                <view v-for="reply in comment.children" :key="reply.id" class="reply-item">
                  <view class="reply-avatar-wrap">
                    <image v-if="reply.avatar" class="reply-avatar" :src="reply.avatar" mode="aspectFill" />
                    <view v-else class="reply-avatar-placeholder">
                      <text class="reply-avatar-text">{{ (reply.nickname || '用').charAt(0) }}</text>
                    </view>
                  </view>
                  <view class="reply-body">
                    <text class="reply-nickname">{{ reply.nickname || '用户' }}</text>
                    <text class="reply-content">{{ reply.content }}</text>
                    <text class="reply-time">{{ formatTime(reply.createdAt) }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="comment-input-bar">
          <input class="comment-input" v-model="commentText" placeholder="说点什么..." :maxlength="500" confirm-type="send" @confirm="submitComment" />
          <view class="comment-send-btn" @click="submitComment">
            <text class="comment-send-text">发送</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { onShow, onHide, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

// 配置分享给朋友
onShareAppMessage(() => {
  return {
    title: '电吹管视频教程',
    path: '/pages/tutorial/tutorial'
  }
})

// 配置分享到朋友圈
onShareTimeline(() => {
  return {
    title: '电吹管视频教程'
  }
})
import { getVideoPage, getVideoSongs } from '@/api/video'
import { toggleVideoLike, toggleVideoFavorite, getLikedVideoIds, getVideoFavoriteList, recordVideoShare } from '@/api/videoInteraction'
import { addComment, getCommentList, getCommentCount } from '@/api/comment'
import TabBar from '@/components/TabBar.vue'

interface SongItem {
  id: number
  title: string
}

const videos = ref<any[]>([])
const songs = ref<SongItem[]>([])
const activeSongId = ref<number | null>(null)
const loading = ref(false)
const loadingMore = ref(false)
const currentPage = ref(1)
const pageSize = 10
const hasMore = ref(true)
const searchKeyword = ref('')
const activeVideoId = ref<number | null>(null)

// 按关键词过滤视频（曲谱筛选已由后端分页API处理）
const filteredVideos = computed(() => {
  if (!searchKeyword.value.trim()) return videos.value
  const keyword = searchKeyword.value.trim().toLowerCase()
  return videos.value.filter(v =>
    v.title?.toLowerCase().includes(keyword) ||
    v.description?.toLowerCase().includes(keyword) ||
    v.songTitle?.toLowerCase().includes(keyword)
  )
})

// 搜索关键词变化时重置播放状态
watch(searchKeyword, () => {
  pauseActiveVideo()
  activeVideoId.value = null
})

onShow(() => {
  loadSongs()
  reloadVideos()
  loadInteractionStatus()
})

onHide(() => {
  pauseActiveVideo()
})

const loadSongs = async () => {
  try {
    const data = await getVideoSongs()
    songs.value = (data || []).map((s: any) => ({ id: s.id, title: s.title }))
  } catch (e) {
    console.error('加载曲谱列表失败', e)
  }
}

// 重新加载视频（切换曲谱分类或首次加载）
const reloadVideos = async () => {
  loading.value = true
  currentPage.value = 1
  hasMore.value = true
  try {
    const params: any = { page: 1, pageSize }
    if (activeSongId.value !== null) {
      params.songId = activeSongId.value
    }
    const data = await getVideoPage(params)
    const list = data?.list || []
    videos.value = list.map((v: any) => ({
      ...v,
      _liked: false,
      _favorited: false
    }))
    hasMore.value = list.length >= pageSize
  } catch (e) {
    console.error('加载视频列表失败', e)
  } finally {
    loading.value = false
  }
}

// 加载更多视频
const loadMoreVideos = async () => {
  if (loadingMore.value || !hasMore.value || loading.value) return
  loadingMore.value = true
  currentPage.value++
  try {
    const params: any = { page: currentPage.value, pageSize }
    if (activeSongId.value !== null) {
      params.songId = activeSongId.value
    }
    const data = await getVideoPage(params)
    const list = data?.list || []
    videos.value.push(...list.map((v: any) => ({
      ...v,
      _liked: false,
      _favorited: false
    })))
    hasMore.value = list.length >= pageSize
  } catch (e) {
    console.error('加载更多视频失败', e)
    currentPage.value--
  } finally {
    loadingMore.value = false
  }
}

// 选择曲谱分类
const selectSong = (songId: number | null) => {
  activeSongId.value = songId
  pauseActiveVideo()
  activeVideoId.value = null
  reloadVideos()
}

// 点击卡片切换播放
const togglePlay = (video: any) => {
  if (activeVideoId.value === video.id) return
  pauseActiveVideo()
  activeVideoId.value = video.id
}

// 暂停当前活跃视频
const pauseActiveVideo = () => {
  if (activeVideoId.value !== null) {
    try {
      const ctx = uni.createVideoContext('video-' + activeVideoId.value)
      ctx.pause()
    } catch (e) {
      // 组件可能已销毁
    }
  }
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 视频播放结束，自动播放下一个
const onVideoEnded = (video: any) => {
  const currentIndex = filteredVideos.value.findIndex(v => v.id === video.id)
  if (currentIndex >= 0 && currentIndex < filteredVideos.value.length - 1) {
    const nextVideo = filteredVideos.value[currentIndex + 1]
    activeVideoId.value = nextVideo.id
  } else {
    activeVideoId.value = null
  }
}

const onVideoError = () => {
  uni.showToast({ title: '视频加载失败', icon: 'none' })
}

// 全屏变化处理
const isFullscreen = ref(false)
const onFullscreenChange = (e: any) => {
  isFullscreen.value = e.detail.fullScreen
}

// 请求全屏播放（通过 VideoContext API 解决原生全屏渲染问题）
const requestFullscreen = (video: any) => {
  try {
    const ctx = uni.createVideoContext('video-' + video.id)
    ctx.requestFullScreen({ direction: 90 })
  } catch (e) {
    console.error('进入全屏失败', e)
  }
}

// 互动操作
const handleLike = async (video: any) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const data = await toggleVideoLike(video.id)
    video._liked = data?.liked ?? !video._liked
    video.likeCount = (video.likeCount || 0) + (video._liked ? 1 : -1)
    uni.showToast({ title: video._liked ? '已点赞' : '取消点赞', icon: 'none' })
  } catch (e) {
    console.error('点赞操作失败', e)
  }
}

const handleFavorite = async (video: any) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const data = await toggleVideoFavorite(video.id)
    video._favorited = data?.favorited ?? !video._favorited
    video.favoriteCount = (video.favoriteCount || 0) + (video._favorited ? 1 : -1)
    uni.showToast({ title: video._favorited ? '已收藏' : '取消收藏', icon: 'none' })
  } catch (e) {
    console.error('收藏操作失败', e)
  }
}

// 加载用户互动状态（点赞、收藏）
const loadInteractionStatus = async () => {
  const token = uni.getStorageSync('token')
  if (!token) return
  try {
    const [likedIds, favoritedIds] = await Promise.all([
      getLikedVideoIds().catch(() => []),
      getVideoFavoriteList().catch(() => [])
    ])
    const likedSet = new Set(likedIds || [])
    const favoritedSet = new Set(favoritedIds || [])
    videos.value.forEach(v => {
      v._liked = likedSet.has(v.id)
      v._favorited = favoritedSet.has(v.id)
    })
  } catch (e) {
    console.error('加载互动状态失败', e)
  }
}

const handleComment = (video: any) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  currentCommentVideo.value = video
  commentText.value = ''
  showCommentPopup.value = true
  loadComments(video.id)
}

const handleShare = async (video: any) => {
  // 记录分享计数
  try {
    await recordVideoShare(video.id)
    video.shareCount = (video.shareCount || 0) + 1
  } catch (e) {
    // 忽略计数失败
  }
  uni.showToast({ title: '点击右上角「...」分享给朋友', icon: 'none' })
}

// ==================== 评论功能 ====================
const showCommentPopup = ref(false)
const currentCommentVideo = ref<any>(null)
const commentText = ref('')
const comments = ref<any[]>([])
const commentCount = ref(0)

const loadComments = async (videoId: number) => {
  try {
    const [list, countData] = await Promise.all([
      getCommentList(videoId).catch(() => []),
      getCommentCount(videoId).catch(() => ({ count: 0 }))
    ])
    comments.value = list || []
    commentCount.value = countData?.count || 0
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

const submitComment = async () => {
  if (!currentCommentVideo.value) return
  if (!commentText.value.trim()) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' })
    return
  }
  try {
    await addComment({
      videoId: currentCommentVideo.value.id,
      content: commentText.value.trim()
    })
    commentText.value = ''
    uni.showToast({ title: '评论成功', icon: 'success' })
    // 更新视频卡片评论数
    currentCommentVideo.value.commentCount = (currentCommentVideo.value.commentCount || 0) + 1
    loadComments(currentCommentVideo.value.id)
  } catch (e: any) {
    uni.showToast({ title: e.message || '评论失败', icon: 'none' })
  }
}

const closeCommentPopup = () => {
  showCommentPopup.value = false
  currentCommentVideo.value = null
  comments.value = []
  commentText.value = ''
}

const formatTime = (time: string) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 缩略图占位颜色
const thumbColors = ['#007AFF', '#5856D6', '#34C759', '#FF9500', '#FF3B30', '#AF52DE', '#5AC8FA', '#FF2D55']
const getThumbColor = (id: number) => {
  return thumbColors[(id - 1) % thumbColors.length]
}
</script>

<style scoped>
.page {
  height: 100vh;
  background-color: var(--bg-primary, #F2F2F7);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ============ 搜索栏 ============ */
.search-container {
  padding: 12px 16px;
  background-color: var(--bg-secondary, #FFFFFF);
}

.search-bar {
  display: flex;
  align-items: center;
  background-color: var(--color-gray-7, #F2F2F7);
  border-radius: 10px;
  padding: 10px 12px;
  height: 36px;
}

.search-icon-wrap {
  position: relative;
  width: 14px;
  height: 14px;
  margin-right: 8px;
  flex-shrink: 0;
}

.search-icon-circle {
  position: absolute;
  top: 0;
  left: 0;
  width: 10px;
  height: 10px;
  border: 2px solid var(--text-tertiary, #8E8E93);
  border-radius: 50%;
  opacity: 0.6;
}

.search-icon-handle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 5px;
  height: 2px;
  background: var(--text-tertiary, #8E8E93);
  transform: rotate(45deg);
  opacity: 0.6;
}

.search-input {
  flex: 1;
  font-size: var(--font-size-footnote, 15px);
  color: var(--text-primary, #1C1C1E);
  height: 36px;
}

.search-placeholder {
  color: var(--text-placeholder, #C7C7CC);
  font-size: var(--font-size-footnote, 15px);
}

.search-clear {
  font-size: 15px;
  color: var(--text-tertiary, #8E8E93);
  padding: 4px;
  flex-shrink: 0;
}

/* ============ 搜索提示 ============ */

/* ============ 曲谱分类标签 ============ */
.song-tabs {
  background-color: var(--bg-secondary, #FFFFFF);
  padding: 8px 0;
  flex-shrink: 0;
  border-bottom: 0.5px solid var(--color-gray-3, #E5E5EA);
}

.song-tabs-inner {
  display: flex;
  padding: 0 12px;
  gap: 8px;
  white-space: nowrap;
}

.song-tab {
  flex-shrink: 0;
  padding: 6px 14px;
  border-radius: 16px;
  background-color: var(--bg-secondary, #FFFFFF);
  border: 0.5px solid var(--color-gray-3, #E5E5EA);
  display: flex;
  align-items: center;
  justify-content: center;
}

.song-tab:active {
  opacity: 0.85;
}

.song-tab.active {
  background-color: var(--color-primary, #007AFF);
  border-color: var(--color-primary, #007AFF);
}

.song-tab-text {
  font-size: var(--font-size-footnote, 14px);
  color: var(--text-primary, #1C1C1E);
  white-space: nowrap;
}

.song-tab.active .song-tab-text {
  color: #FFFFFF;
  font-weight: 600;
}
.search-hint {
  padding: 4px 16px 6px;
  background-color: var(--bg-secondary, #FFFFFF);
  flex-shrink: 0;
}

.search-hint-text {
  font-size: var(--font-size-caption2, 12px);
  color: var(--text-tertiary, #8E8E93);
}

/* ============ 空状态 ============ */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-primary, #F2F2F7);
}

.empty-icon {
  font-size: 48px;
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
  margin-bottom: 16px;
}

.empty-action {
  padding: 8px 24px;
  background-color: var(--color-gray-7, #F2F2F7);
  border-radius: 18px;
}

.empty-action:active {
  opacity: 0.85;
}

.empty-action-text {
  font-size: var(--font-size-footnote, 14px);
  color: var(--text-secondary, #3C3C43);
}

/* ============ 视频列表 ============ */
.video-list {
  flex: 1;
  height: 0;
}

.list-content {
  padding: 8px 10px 80px;
}

/* ============ 单个视频卡片 ============ */
.video-card {
  background-color: var(--bg-secondary, #FFFFFF);
  margin-bottom: 10px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-card, 0 2px 12px rgba(0, 0, 0, 0.06));
}

/* 视频播放区 */
.card-video-area {
  width: 100%;
  height: 44vw;
  background-color: #000000;
  position: relative;
}

.card-video {
  width: 100%;
  height: 100%;
}

.video-poster {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.poster-text {
  font-size: 36px;
  color: #FFFFFF;
  font-weight: 700;
  opacity: 0.2;
}

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 48px;
  height: 48px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  font-size: 20px;
  color: #FFFFFF;
  margin-left: 3px;
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

/* 视频信息区 */
.card-info {
  padding: 10px 12px 6px;
}

.card-title {
  display: block;
  font-size: var(--font-size-footnote, 15px);
  font-weight: 600;
  color: var(--text-primary, #1C1C1E);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  display: block;
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary, #8E8E93);
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-song-title {
  display: block;
  font-size: var(--font-size-caption2, 12px);
  color: var(--color-primary, #007AFF);
  margin-top: 2px;
}

/* 互动操作栏 */
.card-actions {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 6px 12px 10px;
  border-top: 0.5px solid var(--color-gray-3, #E5E5EA);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
}

.action-btn:active {
  opacity: 0.7;
}

.action-emoji {
  font-size: 16px;
  color: var(--text-tertiary, #8E8E93);
}

.action-emoji.liked {
  color: var(--color-warning, #FF9500);
}

.action-emoji.favorited {
  color: var(--color-danger, #FF3B30);
}

.action-text {
  font-size: var(--font-size-caption2, 11px);
  color: var(--text-placeholder, #C7C7CC);
}

/* ============ 加载状态 ============ */
.loading-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 100;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-gray-7, #E5E5EA);
  border-top-color: var(--color-primary, #007AFF);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary, #8E8E93);
}

/* ============ 加载更多 ============ */
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 0 24px;
  gap: 6px;
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-gray-7, #E5E5EA);
  border-top-color: var(--color-primary, #007AFF);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.load-more-text {
  font-size: var(--font-size-caption2, 12px);
  color: var(--text-placeholder, #C7C7CC);
}

/* ============ 评论弹窗 ============ */
.comment-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.comment-panel {
  width: 100%;
  max-height: 70vh;
  background-color: #fff;
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--color-gray-7, #F2F2F7);
}

.comment-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #333);
}

.comment-close {
  font-size: 20px;
  color: var(--text-tertiary, #8E8E93);
  padding: 4px;
}

.comment-list {
  flex: 1;
  max-height: 50vh;
  padding: 0 16px;
}

.comment-empty {
  padding: 40px 0;
  text-align: center;
}

.comment-empty-text {
  font-size: 14px;
  color: var(--text-tertiary, #8E8E93);
}

.comment-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-gray-7, #F2F2F7);
}

.comment-avatar-wrap {
  margin-right: 10px;
  flex-shrink: 0;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.comment-avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: var(--color-primary, #007AFF);
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-avatar-text {
  font-size: 16px;
  color: #fff;
  font-weight: 600;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-nickname {
  font-size: 13px;
  color: var(--text-secondary, #666);
  margin-bottom: 4px;
}

.comment-content {
  font-size: 15px;
  color: var(--text-primary, #333);
  line-height: 1.5;
  display: block;
  margin-bottom: 4px;
}

.comment-time {
  font-size: 12px;
  color: var(--text-tertiary, #8E8E93);
}

/* 子评论（回复） */
.comment-replies {
  margin-top: 8px;
  background-color: var(--bg-primary, #F2F2F7);
  border-radius: 8px;
  padding: 8px;
}

.reply-item {
  display: flex;
  padding: 6px 0;
}

.reply-item + .reply-item {
  border-top: 0.5px solid var(--color-gray-3, #E5E5EA);
}

.reply-avatar-wrap {
  margin-right: 8px;
  flex-shrink: 0;
}

.reply-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.reply-avatar-placeholder {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: var(--color-primary, #007AFF);
  opacity: 0.7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reply-avatar-text {
  font-size: 12px;
  color: #fff;
  font-weight: 600;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-nickname {
  display: block;
  font-size: 12px;
  color: var(--text-secondary, #3C3C43);
  font-weight: 500;
}

.reply-content {
  display: block;
  font-size: 13px;
  color: var(--text-primary, #1C1C1E);
  line-height: 1.4;
  margin-top: 2px;
}

.reply-time {
  display: block;
  font-size: 10px;
  color: var(--text-placeholder, #C7C7CC);
  margin-top: 2px;
}

.comment-input-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid var(--color-gray-7, #F2F2F7);
  background-color: #fff;
}

.comment-input {
  flex: 1;
  height: 36px;
  background-color: var(--color-gray-7, #F2F2F7);
  border-radius: 18px;
  padding: 0 14px;
  font-size: 14px;
}

.comment-send-btn {
  margin-left: 10px;
  background-color: var(--color-primary, #007AFF);
  border-radius: 18px;
  padding: 0 16px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-send-btn:active {
  opacity: 0.7;
}

.comment-send-text {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
}
</style>