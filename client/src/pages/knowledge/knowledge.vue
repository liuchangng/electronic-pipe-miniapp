<template>
  <view class="page">
    <!-- 横向 Tab -->
    <scroll-view class="tab-scroll" scroll-x :scroll-into-view="'tab-' + activeTab">
      <view class="tab-list">
        <view 
          v-for="tab in tabs" 
          :key="tab.value"
          :id="'tab-' + tab.value"
          :class="['tab-item', activeTab === tab.value ? 'active' : '']"
          @click="switchTab(tab.value)"
        >
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 内容区域 -->
    <scroll-view class="content-scroll" scroll-y>
      <view class="content-inner">
        <!-- 加载状态 -->
        <view v-if="loading" class="loading-state">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>
        <!-- 内容列表 -->
        <view v-else-if="currentItems.length > 0">
          <view v-for="item in currentItems" :key="item.id" class="content-section">
            <text class="content-title">{{ item.title }}</text>
            <text v-if="item.subtitle" class="content-subtitle">{{ item.subtitle }}</text>
            <rich-text :nodes="item.content" class="content-body" />
          </view>
        </view>
        <!-- 空状态 -->
        <view v-else class="empty">
          <text class="empty-icon">📖</text>
          <text class="empty-text">暂无内容</text>
          <text class="empty-hint">敬请期待更多知识内容</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部 TabBar -->
    <TabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { getKnowledgeList } from '@/api/knowledge'
import TabBar from '@/components/TabBar.vue'

// 配置分享给朋友
onShareAppMessage(() => {
  return {
    title: '电吹管基础知识',
    path: '/pages/knowledge/knowledge'
  }
})

// 配置分享到朋友圈
onShareTimeline(() => {
  return {
    title: '电吹管基础知识'
  }
})

// Tab 配置
const tabs = [
  { label: '电吹管介绍', value: 'introduction' },
  { label: '发展历史', value: 'history' },
  { label: '品牌详解', value: 'brand' },
  { label: '通用指法', value: 'fingering' }
]

// 当前 Tab
const activeTab = ref('introduction')

// 加载状态
const loading = ref(false)

// 知识数据（按分类分组，每个分类存储数组）
const knowledgeData = ref<Record<string, any[]>>({})

// 当前分类的所有内容
const currentItems = computed(() => {
  return knowledgeData.value[activeTab.value] || []
})

onMounted(() => {
  loadKnowledge()
})

// 加载知识数据
const loadKnowledge = async () => {
  loading.value = true
  try {
    const data = await getKnowledgeList()
    if (data && Array.isArray(data)) {
      // 按分类分组（每个分类可有多条内容）
      const grouped: Record<string, any[]> = {}
      data.forEach((item: any) => {
        if (!grouped[item.category]) {
          grouped[item.category] = []
        }
        grouped[item.category].push(item)
      })
      knowledgeData.value = grouped
    }
  } catch (e) {
    console.error('加载知识数据失败', e)
  } finally {
    loading.value = false
  }
}

// 切换 Tab
const switchTab = (tab: string) => {
  activeTab.value = tab
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary);
}

/* Tab 栏 - 底线式 */
.tab-scroll {
  background-color: var(--bg-secondary);
  white-space: nowrap;
  border-bottom: 0.5px solid var(--color-gray-3);
}

.tab-list {
  display: inline-flex;
  padding: 0 16px 8px;
  gap: 24px;
}

.tab-item {
  display: inline-block;
  padding: 8px 0;
  font-size: var(--font-size-footnote, 15px);
  color: var(--text-tertiary);
  flex-shrink: 0;
  position: relative;
  border-bottom: 2px solid transparent;
}

.tab-item.active {
  color: var(--text-primary);
  font-weight: 600;
  border-bottom-color: var(--color-primary);
}

/* 内容区域 */
.content-scroll {
  height: calc(100vh - 60px);
}

.content-inner {
  padding: 24px 16px;
}

.content-section {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow-card, 0 2px 12px rgba(0, 0, 0, 0.06));
  margin-bottom: 12px;
}

.content-title {
  display: block;
  font-size: var(--font-size-title3, 20px);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.content-subtitle {
  display: block;
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary, #8E8E93);
  margin-bottom: 12px;
}

.content-body {
  font-size: var(--font-size-callout, 17px);
  color: var(--text-primary);
  line-height: 1.6;
}

.content-body p {
  margin-bottom: 16px;
}

.content-body h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  margin-top: 16px;
}

.content-body ul, .content-body ol {
  margin-bottom: 16px;
  padding-left: 20px;
}

.content-body li {
  margin-bottom: 4px;
}

.content-body img {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 160px);
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

/* 空状态 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 160px);
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
}
</style>
