<template>
  <view class="page">
    <!-- 空状态 -->
    <view v-if="!loading && requests.length === 0" class="empty-state">
      <text class="empty-icon">✉</text>
      <text class="empty-text">还没有求谱记录</text>
      <text class="empty-hint">去提交一个求谱申请吧</text>
      <view class="empty-action" @click="goToRequest">
        <text class="empty-action-text">去求谱</text>
      </view>
    </view>

    <!-- 求谱列表 -->
    <scroll-view v-else class="request-list" scroll-y :show-scrollbar="false">
      <view
        v-for="item in requests"
        :key="item.id"
        class="request-card"
      >
        <view class="card-header">
          <text class="card-title">{{ item.songName }}</text>
          <view class="status-tag" :class="item.status">
            <text class="status-text">{{ getStatusLabel(item.status) }}</text>
          </view>
        </view>

        <view class="card-info" v-if="item.artist">
          <text class="info-label">作者：</text>
          <text class="info-value">{{ item.artist }}</text>
        </view>

        <view class="card-info" v-if="item.category">
          <text class="info-label">分类：</text>
          <text class="info-value">{{ getCategoryLabel(item.category) }}</text>
        </view>

        <view class="card-info" v-if="item.description">
          <text class="info-label">说明：</text>
          <text class="info-value desc">{{ item.description }}</text>
        </view>

        <!-- 管理员回复 -->
        <view v-if="item.reply" class="reply-section">
          <text class="reply-label">管理员回复：</text>
          <text class="reply-content">{{ item.reply }}</text>
        </view>

        <text class="card-time">{{ formatTime(item.createdAt) }}</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 底部安全区 -->
      <view class="safe-area"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyRequests } from '@/api/songRequest'

const requests = ref<any[]>([])
const loading = ref(false)

onShow(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateBack({ fail: () => { uni.switchTab({ url: '/pages/profile/profile' }) } })
    }, 1500)
    return
  }
  loadRequests()
})

const loadRequests = async () => {
  loading.value = true
  try {
    const data = await getMyRequests()
    requests.value = data || []
  } catch (e) {
    console.error('加载求谱列表失败', e)
  } finally {
    loading.value = false
  }
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待处理',
    accepted: '已采纳',
    rejected: '未采纳'
  }
  return map[status] || status
}

const getCategoryLabel = (category: string) => {
  const map: Record<string, string> = {
    pop: '流行',
    classical: '古典',
    folk: '民谣',
    movie: '影视',
    other: '其他'
  }
  return map[category] || category
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

const goToRequest = () => {
  uni.navigateTo({ url: '/pages/song-request/song-request' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 0 40px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: var(--color-gray-4);
}

.empty-text {
  font-size: var(--font-size-title3);
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.empty-hint {
  font-size: var(--font-size-footnote);
  color: var(--text-tertiary);
  margin-bottom: 24px;
}

.empty-action {
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  padding: 10px 32px;
  border-radius: var(--radius-full);
}

.empty-action-text {
  color: #fff;
  font-size: var(--font-size-subhead);
  font-weight: var(--font-weight-medium);
}

.request-list {
  height: 100vh;
  padding: 12px 16px;
}

.request-card {
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-card);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.card-title {
  font-size: var(--font-size-subhead);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  flex: 1;
  margin-right: 12px;
}

.status-tag {
  padding: 3px 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.status-tag.pending {
  background-color: rgba(255, 149, 0, 0.1);
}

.status-tag.accepted {
  background-color: rgba(52, 199, 89, 0.1);
}

.status-tag.rejected {
  background-color: rgba(255, 59, 48, 0.1);
}

.status-text {
  font-size: var(--font-size-caption2);
  font-weight: var(--font-weight-medium);
}

.status-tag.pending .status-text {
  color: var(--color-warning);
}

.status-tag.accepted .status-text {
  color: var(--color-success);
}

.status-tag.rejected .status-text {
  color: var(--color-danger);
}

.card-info {
  display: flex;
  margin-bottom: 6px;
}

.info-label {
  font-size: var(--font-size-footnote);
  color: var(--text-tertiary);
  flex-shrink: 0;
  width: 56px;
}

.info-value {
  font-size: var(--font-size-footnote);
  color: var(--text-secondary);
  flex: 1;
}

.info-value.desc {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.reply-section {
  margin-top: 10px;
  padding: 10px 12px;
  background-color: var(--color-primary-light);
  border-radius: var(--radius-sm);
}

.reply-label {
  display: block;
  font-size: var(--font-size-caption1);
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
  margin-bottom: 4px;
}

.reply-content {
  font-size: var(--font-size-footnote);
  color: var(--text-secondary);
  line-height: 1.5;
}

.card-time {
  display: block;
  margin-top: 10px;
  font-size: var(--font-size-caption2);
  color: var(--color-gray-4);
}

.loading {
  padding: 20px;
  text-align: center;
}

.loading-text {
  font-size: var(--font-size-footnote);
  color: var(--text-tertiary);
}
</style>