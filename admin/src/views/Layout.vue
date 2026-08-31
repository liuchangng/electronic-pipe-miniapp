<template>
  <n-message-provider>
    <n-dialog-provider>
      <n-layout has-sider style="height: 100vh">
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="240" :collapsed="collapsed" show-trigger @collapse="collapsed = true" @expand="collapsed = false">
      <div class="sider-header">
        <div class="sider-logo" v-if="!collapsed">
          <view class="logo-icon">♫</view>
          <span class="sider-title">电子Pipe</span>
        </div>
        <view class="logo-icon-mini" v-else>♫</view>
      </div>
      <n-menu :collapsed="collapsed" :collapsed-width="64" :collapsed-icon-size="22" :options="menuOptions" :value="activeKey" :render-icon="renderMenuIcon" @update:value="handleMenuSelect" />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered style="height: 56px; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; background: #fff">
        <span class="header-title">{{ currentTitle }}</span>
        <n-button quaternary size="small" @click="handleLogout">
          <span class="logout-text">退出登录</span>
        </n-button>
      </n-layout-header>
      <n-layout-content style="padding: 24px; background: #F2F2F7; overflow: auto">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
    </n-dialog-provider>
  </n-message-provider>
</template>

<script setup>
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import { SpeedometerOutline, MusicalNotesOutline, VideocamOutline, ImagesOutline, PeopleOutline, MailOutline, BookOutline, ChatbubblesOutline, ChatboxOutline } from '@vicons/ionicons5'
import { logout } from '../api/admin'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const activeKey = computed(() => route.name)
const currentTitle = computed(() => route.meta?.title || '仪表盘')

// 菜单图标映射
const iconMap = {
  Dashboard: SpeedometerOutline,
  Songs: MusicalNotesOutline,
  Videos: VideocamOutline,
  Banners: ImagesOutline,
  Knowledge: BookOutline,
  Users: PeopleOutline,
  SongRequests: MailOutline,
  Feedbacks: ChatbubblesOutline,
  Comments: ChatboxOutline
}

const renderMenuIcon = (option) => {
  const icon = iconMap[option.key]
  if (icon) {
    return h(NIcon, null, { default: () => h(icon) })
  }
  return null
}

const menuOptions = [
  { label: '仪表盘', key: 'Dashboard' },
  { label: '曲谱管理', key: 'Songs' },
  { label: '视频管理', key: 'Videos' },
  { label: 'Banner管理', key: 'Banners' },
  { label: '知识管理', key: 'Knowledge' },
  { label: '用户管理', key: 'Users' },
  { label: '求谱管理', key: 'SongRequests' },
  { label: '反馈管理', key: 'Feedbacks' },
  { label: '评论管理', key: 'Comments' }
]

const handleMenuSelect = (key) => {
  router.push({ name: key })
}

const handleLogout = async () => {
  try {
    await logout()
  } catch (e) {
    // ignore
  }
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  router.push('/login')
}
</script>

<style scoped>
.sider-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #E5E5EA;
}

.sider-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
}

.logo-icon-mini {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.sider-title {
  font-size: 17px;
  font-weight: 700;
  color: #1C1C1E;
  letter-spacing: 0.5px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1C1C1E;
}

.logout-text {
  font-size: 13px;
  color: #8E8E93;
}
</style>