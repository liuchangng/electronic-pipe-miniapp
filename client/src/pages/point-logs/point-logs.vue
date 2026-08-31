<template>
  <view class="page">
    <!-- 积分概览 -->
    <view class="summary-card">
      <text class="summary-label">当前积分</text>
      <text class="summary-point">{{ pointInfo.point || 0 }}</text>
      <text class="summary-level">{{ pointInfo.levelName || 'Lv1 新手' }}</text>
      <view v-if="pointInfo.nextLevelPoint > 0" class="progress-bar">
        <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
      </view>
      <text v-if="pointInfo.nextLevelPoint > 0" class="progress-hint">
        距{{ pointInfo.nextLevelName }}还需 {{ (pointInfo.nextLevelPoint || 0) - (pointInfo.point || 0) }} 积分
      </text>
    </view>

    <!-- 积分流水 -->
    <view class="log-section">
      <text class="section-title">积分记录</text>
      <view v-if="logs.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无积分记录</text>
      </view>
      <view v-for="log in logs" :key="log.id" class="log-item">
        <view class="log-info">
          <text class="log-desc">{{ log.description }}</text>
          <text class="log-time">{{ formatTime(log.createdAt) }}</text>
        </view>
        <text :class="log.amount > 0 ? 'log-amount positive' : 'log-amount negative'">
          {{ log.amount > 0 ? '+' : '' }}{{ log.amount }}
        </text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 底部安全区 -->
    <view class="safe-area"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getPointInfo, getPointLogs } from '@/api/point'

const pointInfo = ref<any>({})
const logs = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const hasMore = ref(true)

const progressPercent = computed(() => {
  const point = pointInfo.value.point || 0
  const level = pointInfo.value.level || 1
  const thresholds = [0, 100, 500, 2000]
  if (level >= thresholds.length) return 100
  const currentThreshold = thresholds[level - 1] || 0
  const nextThreshold = thresholds[level] || 2000
  return Math.min(100, Math.round(((point - currentThreshold) / (nextThreshold - currentThreshold)) * 100))
})

onLoad(() => {
  loadPointInfo()
  loadLogs()
})

const loadPointInfo = async () => {
  try {
    const data = await getPointInfo()
    pointInfo.value = data || {}
  } catch (e) {
    // ignore
  }
}

const loadLogs = async (loadMore = false) => {
  if (loading.value) return
  loading.value = true
  try {
    const data = await getPointLogs(currentPage.value, 20)
    const list = data || []
    if (loadMore) {
      logs.value = [...logs.value, ...list]
    } else {
      logs.value = list
    }
    hasMore.value = list.length >= 20
    currentPage.value++
  } catch (e) {
    console.error('加载积分记录失败', e)
  } finally {
    loading.value = false
  }
}

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadLogs(true)
  }
})

const formatTime = (time: string) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary, #F2F2F7);
}

.summary-card {
  background: linear-gradient(135deg, #007AFF, #5856D6);
  padding: 24px;
  text-align: center;
  color: #fff;
}

.summary-label {
  font-size: 14px;
  opacity: 0.8;
  display: block;
  margin-bottom: 8px;
}

.summary-point {
  font-size: 48px;
  font-weight: 700;
  display: block;
  margin-bottom: 4px;
}

.summary-level {
  font-size: 16px;
  opacity: 0.9;
  display: block;
  margin-bottom: 16px;
}

.progress-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  margin: 0 40px 8px;
}

.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: 3px;
  transition: width 0.3s;
}

.progress-hint {
  font-size: 12px;
  opacity: 0.7;
}

.log-section {
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: 12px;
  display: block;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.empty-text {
  font-size: 14px;
  color: var(--text-tertiary, #8E8E93);
}

.log-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: var(--bg-secondary, #fff);
  border-radius: 10px;
  margin-bottom: 8px;
}

.log-info {
  flex: 1;
}

.log-desc {
  font-size: 15px;
  color: var(--text-primary, #333);
  display: block;
  margin-bottom: 4px;
}

.log-time {
  font-size: 12px;
  color: var(--text-tertiary, #8E8E93);
}

.log-amount {
  font-size: 18px;
  font-weight: 600;
}

.log-amount.positive {
  color: #34C759;
}

.log-amount.negative {
  color: #FF3B30;
}

.loading {
  padding: 20px;
  text-align: center;
}

.loading-text {
  font-size: 14px;
  color: var(--text-tertiary, #8E8E93);
}

.safe-area {
  height: env(safe-area-inset-bottom);
}
</style>