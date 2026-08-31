<template>
  <view class="tabbar">
    <view 
      v-for="tab in tabs" 
      :key="tab.path"
      :class="['tab-item', currentPath === tab.path ? 'active' : '']"
      @click="switchTab(tab.path)"
    >
      <view class="tab-icon-container">
        <!-- 首页图标 - 房子 -->
        <view v-if="tab.icon === 'home'" class="tab-icon">
          <view :class="['icon-home', currentPath === tab.path ? 'active' : '']">
            <view class="icon-home-roof"></view>
            <view class="icon-home-body"></view>
          </view>
        </view>
        <!-- 教程图标 - 播放按钮 -->
        <view v-else-if="tab.icon === 'video'" class="tab-icon">
          <view :class="['icon-video', currentPath === tab.path ? 'active' : '']">
            <view class="icon-video-triangle"></view>
          </view>
        </view>
        <!-- 我的图标 - 人物 -->
        <view v-else-if="tab.icon === 'person'" class="tab-icon">
          <view :class="['icon-person', currentPath === tab.path ? 'active' : '']">
            <view class="icon-person-head"></view>
            <view class="icon-person-body"></view>
          </view>
        </view>
      </view>
      <text :class="['tab-label', currentPath === tab.path ? 'active' : '']">{{ tab.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const tabs = [
  { path: '/pages/index/index', label: '首页', icon: 'home' },
  { path: '/pages/tutorial/tutorial', label: '教程', icon: 'video' },
  { path: '/pages/profile/profile', label: '我的', icon: 'person' }
]

const currentPath = ref('')

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  currentPath.value = '/' + currentPage.route
})

const switchTab = (path: string) => {
  if (currentPath.value === path) return
  // 使用 switchTab 切换 tabbar 页面，兼容小程序端
  uni.switchTab({ url: path })
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 83px;
  background-color: rgba(249, 249, 249, 0.94);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  display: flex;
  justify-content: space-around;
  align-items: flex-start;
  padding-top: 8px;
  border-top: 0.5px solid var(--color-gray-3);
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 64px;
  gap: 2px;
}

.tab-icon-container {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-label {
  font-size: 10px;
  font-weight: 500;
  color: var(--text-tertiary);
  letter-spacing: -0.24px;
}

.tab-label.active {
  color: var(--color-primary);
}

/* Home Icon */
.icon-home {
  width: 28px;
  height: 28px;
  position: relative;
}

.icon-home-roof {
  position: absolute;
  top: 3px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 11px solid transparent;
  border-right: 11px solid transparent;
  border-bottom: 9px solid var(--text-tertiary);
}

.icon-home.active .icon-home-roof {
  border-bottom-color: var(--color-primary);
}

.icon-home-body {
  position: absolute;
  bottom: 3px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 13px;
  border: 2px solid var(--text-tertiary);
  border-top: none;
  border-radius: 0 0 2px 2px;
}

.icon-home.active .icon-home-body {
  border-color: var(--color-primary);
}

/* Video Icon - 播放按钮 */
.icon-video {
  width: 28px;
  height: 28px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-video-triangle {
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-left: 13px solid var(--text-tertiary);
  margin-left: 3px;
}

.icon-video.active .icon-video-triangle {
  border-left-color: var(--color-primary);
}

/* Person Icon */
.icon-person {
  width: 28px;
  height: 28px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.icon-person-head {
  width: 12px;
  height: 12px;
  border: 2px solid var(--text-tertiary);
  border-radius: 50%;
}

.icon-person.active .icon-person-head {
  border-color: var(--color-primary);
}

.icon-person-body {
  width: 18px;
  height: 9px;
  border: 2px solid var(--text-tertiary);
  border-bottom: none;
  border-radius: 9px 9px 0 0;
  margin-top: -1px;
}

.icon-person.active .icon-person-body {
  border-color: var(--color-primary);
}
</style>
