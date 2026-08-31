<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-bar">
        <view class="search-icon-wrap">
          <view class="search-icon-circle"></view>
          <view class="search-icon-handle"></view>
        </view>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索曲谱、作者"
          placeholder-class="search-placeholder"
          confirm-type="search"
          focus
          @confirm="handleSearch"
        />
        <text v-if="keyword" class="clear-btn" @click="clearKeyword">✕</text>
      </view>
      <text class="cancel-btn" @click="goBack">取消</text>
    </view>

    <!-- 搜索结果 -->
    <view v-if="hasSearched" class="search-results">
      <view v-if="songList.length > 0" class="result-header">
        <text class="result-count">找到 {{ songList.length }} 个结果</text>
      </view>
      <view v-for="song in songList" :key="song.id" class="song-cell" @click="goToDetail(song.id)">
        <view class="song-icon" :style="{ backgroundColor: song.color || 'var(--color-primary)' }">
          <text class="song-icon-text">{{ song.icon || song.title.charAt(0) }}</text>
        </view>
        <view class="song-content">
          <text class="song-title">{{ song.title }}</text>
          <text class="song-subtitle">{{ song.author }}</text>
        </view>
        <view class="song-meta">
          <text class="song-count">{{ song.favoriteCount || 0 }}</text>
          <text class="song-arrow">›</text>
        </view>
      </view>
      <view v-if="songList.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">未找到相关曲谱</text>
        <text class="empty-hint">试试其他关键词</text>
      </view>
    </view>

    <!-- 搜索前内容 -->
    <view v-else class="search-suggest">
      <!-- 热门搜索 -->
      <view v-if="hotSearches.length > 0" class="section">
        <view class="section-header">
          <text class="section-title">热门搜索</text>
        </view>
        <view class="tag-list">
          <view
            v-for="(item, index) in hotSearches"
            :key="index"
            class="tag-item"
            @click="searchByKeyword(item)"
          >
            <text class="tag-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <!-- 搜索历史 -->
      <view v-if="searchHistory.length > 0" class="section">
        <view class="section-header">
          <text class="section-title">搜索历史</text>
          <text class="clear-history" @click="handleClearHistory">清空</text>
        </view>
        <view class="history-list">
          <view
            v-for="(item, index) in searchHistory"
            :key="index"
            class="history-item"
            @click="searchByKeyword(item)"
          >
            <text class="history-icon">⏱</text>
            <text class="history-text">{{ item }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSongPage } from '@/api/song'
import { getHotSearches, getSearchHistory, saveSearchHistory, clearSearchHistory } from '@/api/search'
import { useLogin } from '@/hooks/useLogin'

const { isLoggedIn } = useLogin()
const keyword = ref('')
const hasSearched = ref(false)
const loading = ref(false)
const songList = ref<any[]>([])
const hotSearches = ref<string[]>([])
const searchHistory = ref<string[]>([])

onMounted(() => {
  loadHotSearches()
  loadSearchHistory()
})

const loadHotSearches = async () => {
  try {
    const data = await getHotSearches()
    hotSearches.value = data || []
  } catch (e) {
    console.error('加载热门搜索失败', e)
  }
}

const loadSearchHistory = async () => {
  if (!isLoggedIn.value) return
  try {
    const data = await getSearchHistory()
    searchHistory.value = data || []
  } catch (e) {
    console.error('加载搜索历史失败', e)
  }
}

const handleSearch = async () => {
  if (!keyword.value.trim()) return
  await doSearch(keyword.value.trim())
}

const searchByKeyword = async (kw: string) => {
  keyword.value = kw
  await doSearch(kw)
}

const doSearch = async (kw: string) => {
  hasSearched.value = true
  loading.value = true
  try {
    const data = await getSongPage({ keyword: kw, page: 1, pageSize: 50 })
    songList.value = data?.list || []
    // 保存搜索历史
    if (isLoggedIn.value) {
      saveSearchHistory(kw).then(() => {
        loadSearchHistory()
      }).catch(() => {})
    }
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    loading.value = false
  }
}

const clearKeyword = () => {
  keyword.value = ''
  hasSearched.value = false
  songList.value = []
  // 重新加载热门搜索和搜索历史
  loadHotSearches()
  loadSearchHistory()
}

const handleClearHistory = async () => {
  if (!isLoggedIn.value) return
  uni.showModal({
    title: '提示',
    content: '确定清空搜索历史吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await clearSearchHistory()
          searchHistory.value = []
          uni.showToast({ title: '已清空', icon: 'success' })
        } catch (e) {
          console.error('清空搜索历史失败', e)
        }
      }
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}

const goToDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary, #F2F2F7);
}

/* ============ 搜索头部 ============ */
.search-header {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: var(--bg-secondary, #FFFFFF);
  border-bottom: 0.5px solid var(--color-gray-3, #E5E5EA);
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  height: 36px;
  background-color: var(--color-gray-7, #F2F2F7);
  border-radius: 10px;
  padding: 0 12px;
}

.search-icon-wrap {
  width: 16px;
  height: 16px;
  margin-right: 8px;
  position: relative;
  flex-shrink: 0;
}

.search-icon-circle {
  width: 10px;
  height: 10px;
  border: 2px solid var(--text-tertiary, #8E8E93);
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
  opacity: 0.6;
}

.search-icon-handle {
  width: 6px;
  height: 2px;
  background-color: var(--text-tertiary, #8E8E93);
  position: absolute;
  bottom: 1px;
  right: 0;
  transform: rotate(45deg);
  opacity: 0.6;
}

.search-input {
  flex: 1;
  font-size: var(--font-size-subhead, 16px);
  color: var(--text-primary, #1C1C1E);
  height: 36px;
}

.search-placeholder {
  color: var(--text-placeholder, #C7C7CC);
  font-size: var(--font-size-subhead, 16px);
}

.clear-btn {
  font-size: 16px;
  color: var(--text-tertiary, #8E8E93);
  padding: 4px 8px;
}

.cancel-btn {
  font-size: var(--font-size-footnote, 14px);
  color: var(--color-primary, #007AFF);
  margin-left: 12px;
  padding: 4px;
}

/* ============ 搜索结果 ============ */
.search-results {
  padding: 0;
}

.result-header {
  padding: 12px 16px;
  background-color: var(--bg-secondary, #FFFFFF);
}

.result-count {
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary, #8E8E93);
}

.song-cell {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background-color: var(--bg-secondary, #FFFFFF);
  border-bottom: 0.5px solid var(--color-gray-3, #E5E5EA);
  transition: background-color 0.15s ease;
}

.song-cell:active {
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-subtitle {
  display: block;
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary, #8E8E93);
  margin-top: 4px;
}

.song-meta {
  display: flex;
  align-items: center;
  gap: 4px;
}

.song-count {
  font-size: var(--font-size-caption2, 11px);
  color: var(--text-placeholder, #C7C7CC);
}

.song-arrow {
  font-size: 16px;
  color: var(--text-placeholder, #C7C7CC);
}

/* ============ 空状态 ============ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
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

/* ============ 搜索建议 ============ */
.search-suggest {
  padding: 16px;
}

.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: var(--font-size-subhead, 16px);
  font-weight: 600;
  color: var(--text-primary, #1C1C1E);
}

.clear-history {
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary, #8E8E93);
}

/* 热门搜索标签 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 6px 14px;
  background-color: var(--bg-secondary, #FFFFFF);
  border-radius: 16px;
  border: 0.5px solid var(--color-gray-3, #E5E5EA);
  transition: all 0.15s ease;
}

.tag-item:active {
  background-color: var(--color-primary-light, rgba(0, 122, 255, 0.08));
  border-color: var(--color-primary, #007AFF);
}

.tag-text {
  font-size: var(--font-size-footnote, 14px);
  color: var(--text-primary, #1C1C1E);
}

/* 搜索历史 */
.history-list {
  background-color: var(--bg-secondary, #FFFFFF);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-card, 0 2px 12px rgba(0, 0, 0, 0.06));
}

.history-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 0.5px solid var(--color-gray-3, #E5E5EA);
  transition: background-color 0.15s ease;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item:active {
  background-color: var(--color-primary-lighter, rgba(0, 122, 255, 0.04));
}

.history-icon {
  font-size: 14px;
  margin-right: 10px;
  color: var(--text-placeholder, #C7C7CC);
}

.history-text {
  font-size: var(--font-size-footnote, 15px);
  color: var(--text-primary, #1C1C1E);
}
</style>