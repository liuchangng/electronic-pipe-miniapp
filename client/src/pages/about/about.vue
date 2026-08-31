<template>
  <view class="page">
    <!-- 公司简介 -->
    <view class="section">
      <text class="section-title">公司简介</text>
      <text class="section-content">
        我们是一家专注于电吹管音乐教育的科技公司，致力于为广大电吹管爱好者提供优质的曲谱资源和学习平台。通过互联网技术，让更多人能够便捷地学习和享受电吹管音乐的魅力。
      </text>
    </view>

    <!-- 五大核心业务 -->
    <view class="section">
      <text class="section-title">五大核心业务</text>
      <view class="business-list">
        <view class="business-item">
          <view class="business-icon bg-primary">
            <text class="business-icon-text">谱</text>
          </view>
          <view class="business-info">
            <text class="business-name">曲谱库</text>
            <text class="business-desc">海量电吹管曲谱资源，持续更新</text>
          </view>
        </view>
        <view class="business-item">
          <view class="business-icon bg-success">
            <text class="business-icon-text">教</text>
          </view>
          <view class="business-info">
            <text class="business-name">视频教程</text>
            <text class="business-desc">专业老师录制的教学视频</text>
          </view>
        </view>
        <view class="business-item">
          <view class="business-icon bg-warning">
            <text class="business-icon-text">识</text>
          </view>
          <view class="business-info">
            <text class="business-name">基础知识</text>
            <text class="business-desc">从入门到精通的完整知识体系</text>
          </view>
        </view>
        <view class="business-item">
          <view class="business-icon bg-primary">
            <text class="business-icon-text">社</text>
          </view>
          <view class="business-info">
            <text class="business-name">社区交流</text>
            <text class="business-desc">与全国爱好者互动交流</text>
          </view>
        </view>
        <view class="business-item">
          <view class="business-icon bg-danger">
            <text class="business-icon-text">赛</text>
          </view>
          <view class="business-info">
            <text class="business-name">赛事活动</text>
            <text class="business-desc">定期举办线上线下的比赛和活动</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 联系我们 -->
    <view class="section">
      <text class="section-title">联系我们</text>
      <view class="contact-list">
        <view class="contact-item">
          <text class="contact-label">客服微信</text>
          <text class="contact-value" @click="copyText('score_service')">score_service</text>
        </view>
        <view class="contact-item">
          <text class="contact-label">客服电话</text>
          <text class="contact-value" @click="callPhone('400-123-4567')">400-123-4567</text>
        </view>
        <view class="contact-item">
          <text class="contact-label">工作时间</text>
          <text class="contact-value">周一至周五 9:00-18:00</text>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="footer">
      <text class="version">版本 v1.0.0</text>
      <text class="copyright">© 2026 电吹管曲谱库 All Rights Reserved</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

// 配置分享给朋友
onShareAppMessage(() => {
  return {
    title: '电吹管曲谱 - 关于我们',
    path: '/pages/about/about'
  }
})

// 配置分享到朋友圈
onShareTimeline(() => {
  return {
    title: '电吹管曲谱 - 关于我们'
  }
})

// 复制文本
const copyText = (text: string) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// 拨打电话
const callPhone = (phone: string) => {
  uni.makePhoneCall({
    phoneNumber: phone
  })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 50px;
}

.section {
  background-color: var(--bg-secondary);
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 12px;
}

.section-title {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.section-content {
  display: block;
  font-size: var(--font-size-callout, 17px);
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 业务列表 - 卡片式 */
.business-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.business-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background-color: var(--bg-secondary);
  border-radius: 12px;
  box-shadow: var(--shadow-card, 0 2px 12px rgba(0, 0, 0, 0.06));
  gap: 24px;
  transition: transform 0.2s ease;
}

.business-item:active {
  transform: scale(0.98);
}

.business-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bg-primary { background-color: var(--color-primary); }
.bg-success { background-color: var(--color-success); }
.bg-warning { background-color: var(--color-warning); }
.bg-danger { background-color: var(--color-danger); }

.business-icon-text {
  color: var(--color-white);
  font-size: 20px;
  font-weight: 600;
}

.business-info {
  flex: 1;
}

.business-name {
  display: block;
  font-size: var(--font-size-callout, 17px);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.business-desc {
  display: block;
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary);
}

/* 联系方式 */
.contact-list {
  margin-top: 12px;
}

.contact-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 0.5px solid var(--color-gray-3);
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-label {
  font-size: var(--font-size-callout, 17px);
  color: var(--text-primary);
}

.contact-value {
  font-size: var(--font-size-callout, 17px);
  color: var(--color-primary);
}

/* 底部 */
.footer {
  padding: 24px 16px;
  text-align: center;
}

.version {
  display: block;
  font-size: var(--font-size-caption1, 13px);
  color: var(--text-tertiary);
  margin-bottom: 4px;
}

.copyright {
  display: block;
  font-size: var(--font-size-caption2, 11px);
  color: var(--text-placeholder);
}
</style>
