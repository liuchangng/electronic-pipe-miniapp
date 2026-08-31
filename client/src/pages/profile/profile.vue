<template>
  <view class="page">
    <!-- 头部用户信息区 -->
    <view class="header">
      <!-- 未登录：点击触发登录 -->
      <view class="avatar-wrapper" @click="handleLogin" v-if="!isLoggedIn">
        <view class="avatar-placeholder">
          <text class="avatar-text">用</text>
        </view>
        <view class="avatar-mask">
          <text class="avatar-tip">点击登录</text>
        </view>
      </view>
      <!-- 已登录：使用button open-type=chooseAvatar获取真实头像 -->
      <button v-else class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
        <image v-if="userInfo.avatar" class="avatar" :src="userInfo.avatar" mode="aspectFill" />
        <view v-else class="avatar-placeholder">
          <text class="avatar-text">{{ userInfo.nickname ? userInfo.nickname[0] : '用' }}</text>
        </view>
        <view class="avatar-edit-icon">
          <text class="edit-icon-text">✎</text>
        </view>
      </button>
      <view class="user-detail" v-if="isLoggedIn">
        <input
          v-if="isEditingNickname"
          class="nickname-input"
          type="text"
          :value="userInfo.nickname || ''"
          @blur="onNicknameConfirm"
          @confirm="onNicknameConfirm"
          focus
          placeholder="请输入昵称"
        />
        <view v-else class="nickname-row" @click="startEditNickname">
          <text class="nickname">{{ userInfo.nickname || '点击填写昵称' }}</text>
          <text class="nickname-edit-icon">✎</text>
        </view>
      </view>
      <view class="login-btn" v-else @click="handleLogin">
        <text class="login-text">登录 / 注册</text>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-card">
      <view class="menu-item" @click="goToFavorite">
        <view class="menu-icon-wrap fav">
          <text class="menu-icon-text">♥</text>
        </view>
        <text class="menu-label">我的收藏</text>
        <view class="menu-right">
          <text class="menu-value">{{ favoriteCount }}首</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="menu-item" @click="showMemberInfo">
        <view class="menu-icon-wrap member">
          <text class="menu-icon-text">★</text>
        </view>
        <text class="menu-label">会员类型</text>
        <view class="menu-right">
          <text class="menu-value member-tag">{{ pointInfo.levelName || 'Lv1 新手' }}</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="menu-item" @click="handleCheckIn">
        <view class="menu-icon-wrap checkin">
          <text class="menu-icon-text">📅</text>
        </view>
        <text class="menu-label">每日签到</text>
        <view class="menu-right">
          <text class="menu-value">{{ pointInfo.point || 0 }}积分</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="menu-item" @click="goToPointLogs">
        <view class="menu-icon-wrap points">
          <text class="menu-icon-text">📊</text>
        </view>
        <text class="menu-label">积分记录</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="handleRequestScore">
        <view class="menu-icon-wrap request">
          <text class="menu-icon-text">✉</text>
        </view>
        <text class="menu-label">我要求谱</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goToMyRequests">
        <view class="menu-icon-wrap myrequest">
          <text class="menu-icon-text">📋</text>
        </view>
        <text class="menu-label">我的求谱</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goToAbout">
        <view class="menu-icon-wrap about">
          <text class="menu-icon-text">i</text>
        </view>
        <text class="menu-label">关于我们</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goToFeedback">
        <view class="menu-icon-wrap feedback">
          <text class="menu-icon-text">✎</text>
        </view>
        <text class="menu-label">意见反馈</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item">
        <view class="menu-icon-wrap version">
          <text class="menu-icon-text">v</text>
        </view>
        <text class="menu-label">当前版本</text>
        <text class="menu-value">v1.0.0</text>
      </view>
      <view class="menu-item last-item" @click="handleLogout" v-if="isLoggedIn">
        <view class="menu-icon-wrap logout">
          <text class="menu-icon-text">↩</text>
        </view>
        <text class="menu-label">退出登录</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 联系客服按钮 -->
    <view class="contact-section">
      <view class="contact-btn" @click="handleContactService">
        <text class="contact-text">联系客服</text>
      </view>
    </view>

    <!-- 底部说明 -->
    <view class="footer">
      <text class="footer-text">所有曲谱均由合作老师根据电吹管演奏技巧精心制作，为电吹管爱好者提供参考，如有侵权请联系删除。</text>
      <text class="footer-sub">备案号：豫ICP备2023001578号-1</text>
    </view>

    <!-- 底部 TabBar -->
    <TabBar />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

// 配置分享给朋友
onShareAppMessage(() => {
  return {
    title: '电吹管曲谱 - 我的',
    path: '/pages/profile/profile'
  }
})

// 配置分享到朋友圈
onShareTimeline(() => {
  return {
    title: '电吹管曲谱 - 我的'
  }
})
import { getFavoriteList } from '@/api/favorite'
import { getVideoFavoriteList } from '@/api/videoInteraction'
import TabBar from '@/components/TabBar.vue'
import { useLogin } from '@/hooks/useLogin'
import { getPointInfo, checkIn } from '@/api/point'
import { updateProfile, uploadAvatar } from '@/api/user'

const { isLoggedIn, userInfo, login, logout } = useLogin()
const favoriteCount = ref(0)
const pointInfo = ref<any>({})
const isEditingNickname = ref(false)

/** 获取头像完整URL — 后端已返回完整URL，直接使用 */
const getFullAvatarUrl = (url: string | undefined) => url || ''

onShow(() => {
  if (isLoggedIn.value) {
    loadFavoriteCount()
    loadPointInfo()
  }
})

const loadFavoriteCount = async () => {
  try {
    const [songIds, videoIds] = await Promise.all([
      getFavoriteList().catch(() => []),
      getVideoFavoriteList().catch(() => [])
    ])
    favoriteCount.value = ((songIds || []).length + (videoIds || []).length)
  } catch (e) {
    favoriteCount.value = 0
  }
}

const loadPointInfo = async () => {
  try {
    const data = await getPointInfo()
    pointInfo.value = data || {}
  } catch (e) {
    // 未登录或接口异常
  }
}

const handleLogin = async () => {
  if (isLoggedIn.value) return
  await login()
  if (isLoggedIn.value) {
    uni.showToast({ title: '登录成功', icon: 'success' })
    await loadFavoriteCount()
    await loadPointInfo()
  }
}

/** 选择微信头像 - 先上传到服务器获取永久URL */
const onChooseAvatar = async (e: any) => {
  const tempPath = e.detail.avatarUrl
  if (!tempPath) return

  // 先本地预览
  userInfo.value = { ...userInfo.value, avatar: tempPath }
  uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))

  try {
    // 上传临时文件到服务器，获取永久URL（相对路径如 /uploads/xxx.jpeg）
    const serverUrl = await uploadAvatar(tempPath)
    // 拼接完整URL用于显示
    const fullUrl = getFullAvatarUrl(serverUrl)
    userInfo.value = { ...userInfo.value, avatar: fullUrl }
    uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))
    // 同步到后端（存相对路径）
    await updateProfile({ avatar: serverUrl })
    uni.showToast({ title: '头像更新成功', icon: 'success' })
  } catch (err) {
    console.error('上传头像失败', err)
    uni.showToast({ title: '头像更新失败', icon: 'none' })
  }
}

/** 开始编辑昵称 */
const startEditNickname = () => {
  isEditingNickname.value = true
}

/** 确认昵称 */
const onNicknameConfirm = async (e: any) => {
  isEditingNickname.value = false
  const nickname = e.detail?.value || e.target?.value
  if (nickname && nickname !== userInfo.value.nickname) {
    userInfo.value = { ...userInfo.value, nickname }
    uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))
    // 同步到后端
    try {
      await updateProfile({ nickname })
    } catch (err) {
      console.error('更新昵称失败', err)
    }
  }
}

const goToFavorite = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/favorite/favorite' })
}

const showMemberInfo = () => {
  const info = pointInfo.value
  const levelName = info.levelName || 'Lv1 新手'
  const point = info.point || 0
  const nextPoint = info.nextLevelPoint
  const nextName = info.nextLevelName
  let content = `当前等级：${levelName}\n当前积分：${point}分`
  if (nextPoint > 0 && nextName) {
    content += `\n距离${nextName}还需：${nextPoint - point}分`
  } else {
    content += '\n已达最高等级！'
  }
  uni.showModal({
    title: '会员等级',
    content,
    showCancel: false,
    confirmText: '知道了'
  })
}

const handleCheckIn = async () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const data = await checkIn()
    uni.showToast({ title: data?.message || '签到成功', icon: 'success' })
    loadPointInfo()
  } catch (e: any) {
    uni.showToast({ title: e.message || '签到失败', icon: 'none' })
  }
}

const goToPointLogs = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/point-logs/point-logs' })
}

const handleRequestScore = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/song-request/song-request' })
}

const goToMyRequests = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/my-requests/my-requests' })
}

const handleContactService = () => {
  uni.makePhoneCall({ phoneNumber: '400-000-0000' })
}

const goToAbout = () => {
  uni.navigateTo({ url: '/pages/about/about' })
}

const goToFeedback = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/feedback/feedback' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        await logout()
        favoriteCount.value = 0
        uni.showToast({ title: '已退出', icon: 'success' })
      }
    }
  })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 120px;
}

/* ============ 头部 ============ */
.header {
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  padding: 48px 16px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 16px;
  position: relative;
  background-color: rgba(255, 255, 255, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.4);
}

.avatar-btn {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 16px;
  position: relative;
  padding: 0;
  border: 3px solid rgba(255, 255, 255, 0.4);
  background-color: rgba(255, 255, 255, 0.2);
  line-height: normal;
}

.avatar-btn::after {
  border: none;
}

.avatar {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
}

.avatar-text {
  font-size: 32px;
  color: #fff;
  font-weight: bold;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.avatar-tip {
  color: #fff;
  font-size: var(--font-size-caption1);
}

.avatar-edit-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background-color: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}

.edit-icon-text {
  color: #fff;
  font-size: 12px;
  line-height: 1;
}

.user-detail {
  text-align: center;
}

.nickname {
  display: block;
  font-size: var(--font-size-title3);
  font-weight: var(--font-weight-semibold);
  color: #fff;
  margin-bottom: 8px;
}

.nickname-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.nickname-edit-icon {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.nickname-input {
  font-size: var(--font-size-title3);
  font-weight: var(--font-weight-semibold);
  color: #fff;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
  padding: 4px 8px;
  min-width: 120px;
}

.login-btn {
  margin-top: 12px;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 8px 24px;
  border-radius: var(--radius-full);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.login-text {
  font-size: var(--font-size-subhead);
  color: #fff;
  font-weight: var(--font-weight-medium);
}

/* ============ 菜单卡片 ============ */
.menu-card {
  margin: 16px 16px 0;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 0.5px solid var(--color-gray-6);
  transition: background-color var(--animation-fast);
}

.menu-item:active {
  background-color: var(--color-gray-7);
}

.menu-item:last-child,
.last-item {
  border-bottom: none;
}

.menu-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.menu-icon-wrap.fav {
  background-color: rgba(255, 59, 48, 0.1);
}

.menu-icon-wrap.member {
  background-color: rgba(255, 149, 0, 0.1);
}

.menu-icon-wrap.request {
  background-color: var(--color-primary-light);
}

.menu-icon-wrap.myrequest {
  background-color: rgba(52, 199, 89, 0.1);
}

.menu-icon-wrap.about {
  background-color: var(--color-primary-light);
}

.menu-icon-wrap.feedback {
  background-color: rgba(255, 149, 0, 0.1);
}

.menu-icon-wrap.version {
  background-color: var(--color-gray-7);
}

.menu-icon-wrap.logout {
  background-color: rgba(255, 59, 48, 0.08);
}

.menu-icon-text {
  font-size: 14px;
  font-weight: var(--font-weight-semibold);
}

.menu-icon-wrap.fav .menu-icon-text {
  color: var(--color-danger);
}

.menu-icon-wrap.member .menu-icon-text {
  color: var(--color-warning);
}

.menu-icon-wrap.request .menu-icon-text,
.menu-icon-wrap.about .menu-icon-text {
  color: var(--color-primary);
}

.menu-icon-wrap.feedback .menu-icon-text {
  color: #FF9500;
}

.menu-icon-wrap.myrequest .menu-icon-text {
  color: var(--color-success);
}

.menu-icon-wrap.checkin .menu-icon-text {
  color: #FF6482;
}

.menu-icon-wrap.points .menu-icon-text {
  color: #5856D6;
}

.menu-icon-wrap.version .menu-icon-text {
  color: var(--text-tertiary);
}

.menu-icon-wrap.logout .menu-icon-text {
  color: var(--color-danger);
}

.menu-label {
  flex: 1;
  font-size: var(--font-size-subhead);
  color: var(--text-primary);
  font-weight: var(--font-weight-medium);
}

.menu-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.menu-value {
  font-size: var(--font-size-footnote);
  color: var(--text-tertiary);
}

.member-tag {
  background-color: rgba(255, 149, 0, 0.1);
  color: var(--color-warning);
  border-radius: var(--radius-full);
  padding: 2px 10px;
  font-size: var(--font-size-caption2);
  font-weight: var(--font-weight-medium);
}

.arrow {
  font-size: 16px;
  color: var(--color-gray-4);
  margin-left: 4px;
  font-weight: 300;
}

/* ============ 联系客服 ============ */
.contact-section {
  padding: 20px 16px;
}

.contact-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.3);
  transition: opacity var(--animation-fast);
}

.contact-btn:active {
  opacity: 0.85;
}

.contact-text {
  font-size: var(--font-size-callout);
  color: #fff;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 1px;
}

/* ============ 底部说明 ============ */
.footer {
  padding: 0 16px 20px;
  text-align: center;
}

.footer-text {
  display: block;
  font-size: var(--font-size-caption2);
  color: var(--text-tertiary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.footer-sub {
  display: block;
  font-size: 10px;
  color: var(--color-gray-4);
}
</style>