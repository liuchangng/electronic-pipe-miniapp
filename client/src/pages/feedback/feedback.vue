<template>
  <view class="page">
    <!-- 表单区域 -->
    <view class="form-card">
      <view class="form-header">
        <text class="form-title">意见反馈</text>
        <text class="form-subtitle">您的反馈对我们非常重要，帮助我们持续改进</text>
      </view>

      <view class="form-group">
        <text class="form-label">反馈类型 <text class="required">*</text></text>
        <view class="type-row">
          <view
            v-for="item in feedbackTypes"
            :key="item.value"
            class="type-tag"
            :class="{ active: form.type === item.value }"
            @click="form.type = item.value"
          >
            <text class="type-icon">{{ item.icon }}</text>
            <text class="type-text">{{ item.label }}</text>
          </view>
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">反馈内容 <text class="required">*</text></text>
        <textarea
          class="form-textarea"
          v-model="form.content"
          placeholder="请详细描述您的建议或遇到的问题..."
          :maxlength="500"
        />
        <text class="char-count">{{ form.content.length }}/500</text>
      </view>

      <view class="form-group">
        <text class="form-label">联系方式</text>
        <input
          class="form-input"
          v-model="form.contact"
          placeholder="选填，方便我们与您沟通"
          :maxlength="128"
        />
      </view>

      <view class="submit-btn" :class="{ disabled: submitting }" @click="handleSubmit">
        <text class="submit-text">{{ submitting ? '提交中...' : '提交反馈' }}</text>
      </view>
    </view>

    <!-- 我的反馈历史 -->
    <view class="history-card" v-if="feedbacks.length > 0">
      <text class="history-title">我的反馈</text>
      <view class="feedback-item" v-for="item in feedbacks" :key="item.id">
        <view class="feedback-header">
          <view class="feedback-type-tag" :class="item.type">
            <text class="feedback-type-text">{{ getTypeLabel(item.type) }}</text>
          </view>
          <text class="feedback-status" :class="item.status">{{ item.status === 'resolved' ? '已回复' : '待处理' }}</text>
        </view>
        <text class="feedback-content">{{ item.content }}</text>
        <view class="feedback-reply" v-if="item.reply">
          <text class="reply-label">回复：</text>
          <text class="reply-text">{{ item.reply }}</text>
        </view>
        <text class="feedback-time">{{ formatTime(item.createdAt) }}</text>
      </view>
    </view>

    <!-- 提示说明 -->
    <view class="tips-card">
      <text class="tips-title">温馨提示</text>
      <text class="tips-item">• 提交后我们会尽快查看并处理</text>
      <text class="tips-item">• 如需紧急联系，请拨打客服电话</text>
      <text class="tips-item">• 感谢您的支持与反馈！</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { submitFeedback, getMyFeedbacks } from '@/api/feedback'
import { useLogin } from '@/hooks/useLogin'

const { isLoggedIn } = useLogin()

const form = ref({
  type: 'suggestion',
  content: '',
  contact: ''
})

const submitting = ref(false)
const feedbacks = ref<any[]>([])

const feedbackTypes = [
  { label: '功能建议', value: 'suggestion', icon: '💡' },
  { label: '问题反馈', value: 'bug', icon: '🐛' },
  { label: '其他', value: 'other', icon: '📝' }
]

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = { suggestion: '建议', bug: '问题', other: '其他' }
  return map[type] || type
}

const formatTime = (time: string) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onShow(() => {
  if (isLoggedIn.value) {
    loadFeedbacks()
  }
})

const loadFeedbacks = async () => {
  try {
    const data = await getMyFeedbacks()
    feedbacks.value = data || []
  } catch (e) {
    feedbacks.value = []
  }
}

const handleSubmit = async () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  if (!form.value.content.trim()) {
    uni.showToast({ title: '请输入反馈内容', icon: 'none' })
    return
  }

  if (submitting.value) return
  submitting.value = true

  try {
    await submitFeedback({
      type: form.value.type,
      content: form.value.content.trim(),
      contact: form.value.contact.trim() || undefined
    })
    uni.showToast({ title: '提交成功，感谢反馈！', icon: 'success' })
    form.value.content = ''
    form.value.contact = ''
    form.value.type = 'suggestion'
    loadFeedbacks()
  } catch (e: any) {
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding: 16px;
}

.form-card {
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 24px 16px;
  box-shadow: var(--shadow-card);
}

.form-header {
  margin-bottom: 24px;
  text-align: center;
}

.form-title {
  display: block;
  font-size: var(--font-size-title3);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: 8px;
}

.form-subtitle {
  display: block;
  font-size: var(--font-size-footnote);
  color: var(--text-tertiary);
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: var(--font-size-subhead);
  color: var(--text-primary);
  margin-bottom: 8px;
  font-weight: var(--font-weight-medium);
}

.required {
  color: var(--color-danger);
}

.type-row {
  display: flex;
  gap: 10px;
}

.type-tag {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border-radius: var(--radius-md);
  background-color: var(--color-gray-7);
  border: 1px solid var(--color-gray-5);
  transition: all var(--animation-fast);
}

.type-tag.active {
  background-color: var(--color-primary-light);
  border-color: var(--color-primary);
}

.type-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.type-text {
  font-size: var(--font-size-footnote);
  color: var(--text-secondary);
}

.type-tag.active .type-text {
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}

.form-textarea {
  width: 100%;
  height: 120px;
  border: 1px solid var(--color-gray-5);
  border-radius: var(--radius-sm);
  padding: 12px;
  font-size: var(--font-size-subhead);
  color: var(--text-primary);
  box-sizing: border-box;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.form-input {
  width: 100%;
  height: 48px;
  border: 1px solid var(--color-gray-5);
  border-radius: var(--radius-sm);
  padding: 0 12px;
  font-size: var(--font-size-subhead);
  color: var(--text-primary);
  box-sizing: border-box;
  transition: border-color var(--animation-fast);
}

.form-input:focus {
  border-color: var(--color-primary);
}

.submit-btn {
  margin-top: 8px;
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.3);
  transition: opacity var(--animation-fast);
}

.submit-btn:active,
.submit-btn.disabled {
  opacity: 0.7;
}

.submit-text {
  font-size: var(--font-size-callout);
  color: #fff;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 1px;
}

.history-card {
  margin-top: 16px;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-card);
}

.history-title {
  display: block;
  font-size: var(--font-size-subhead);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: 12px;
}

.feedback-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--color-gray-7);
}

.feedback-item:last-child {
  border-bottom: none;
}

.feedback-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.feedback-type-tag {
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 12px;
}

.feedback-type-tag.suggestion {
  background-color: #E8F5E9;
}

.feedback-type-tag.bug {
  background-color: #FFF3E0;
}

.feedback-type-tag.other {
  background-color: #E3F2FD;
}

.feedback-type-text {
  font-size: 12px;
  color: var(--text-secondary);
}

.feedback-status {
  font-size: 12px;
  color: var(--text-tertiary);
}

.feedback-status.resolved {
  color: #4CAF50;
}

.feedback-content {
  font-size: var(--font-size-footnote);
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 6px;
}

.feedback-reply {
  background-color: var(--color-gray-7);
  border-radius: var(--radius-sm);
  padding: 8px 10px;
  margin-bottom: 6px;
}

.reply-label {
  font-size: 12px;
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}

.reply-text {
  font-size: var(--font-size-footnote);
  color: var(--text-secondary);
  line-height: 1.5;
}

.feedback-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.tips-card {
  margin-top: 16px;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-card);
}

.tips-title {
  display: block;
  font-size: var(--font-size-subhead);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: 10px;
}

.tips-item {
  display: block;
  font-size: var(--font-size-footnote);
  color: var(--text-tertiary);
  line-height: 2;
}
</style>