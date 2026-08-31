<template>
  <view class="page">
    <!-- 表单区域 -->
    <view class="form-card">
      <view class="form-header">
        <text class="form-title">我要求谱</text>
        <text class="form-subtitle">告诉我们你想要的曲谱，我们会尽快制作</text>
      </view>

      <view class="form-group">
        <text class="form-label">曲谱名称 <text class="required">*</text></text>
        <input
          class="form-input"
          v-model="form.songName"
          placeholder="请输入想要的曲谱名称"
          :maxlength="128"
        />
      </view>

      <view class="form-group">
        <text class="form-label">原曲作者</text>
        <input
          class="form-input"
          v-model="form.artist"
          placeholder="如知道作者请填写（选填）"
          :maxlength="128"
        />
      </view>

      <view class="form-group">
        <text class="form-label">分类</text>
        <view class="category-row">
          <view
            v-for="cat in categories"
            :key="cat.value"
            class="category-tag"
            :class="{ active: form.category === cat.value }"
            @click="form.category = cat.value"
          >
            <text class="category-text">{{ cat.label }}</text>
          </view>
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">补充说明</text>
        <textarea
          class="form-textarea"
          v-model="form.description"
          placeholder="其他需求说明...（选填）"
          :maxlength="500"
        />
      </view>

      <view class="submit-btn" :class="{ disabled: submitting }" @click="handleSubmit">
        <text class="submit-text">{{ submitting ? '提交中...' : '提交求谱' }}</text>
      </view>
    </view>

    <!-- 提示说明 -->
    <view class="tips-card">
      <text class="tips-title">温馨提示</text>
      <text class="tips-item">• 提交后管理员会尽快审核处理</text>
      <text class="tips-item">• 采纳后我们会尽快制作对应曲谱</text>
      <text class="tips-item">• 可在"我的求谱"中查看处理进度</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { submitRequest } from '@/api/songRequest'
import { useLogin } from '@/hooks/useLogin'

const { isLoggedIn } = useLogin()

onLoad(() => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateBack({ fail: () => { uni.switchTab({ url: '/pages/profile/profile' }) } })
    }, 1500)
  }
})

const form = ref({
  songName: '',
  artist: '',
  category: '',
  description: ''
})

const submitting = ref(false)

const categories = [
  { label: '流行', value: 'pop' },
  { label: '古典', value: 'classical' },
  { label: '民谣', value: 'folk' },
  { label: '影视', value: 'movie' },
  { label: '其他', value: 'other' }
]

const handleSubmit = async () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  if (!form.value.songName.trim()) {
    uni.showToast({ title: '请输入曲谱名称', icon: 'none' })
    return
  }

  if (submitting.value) return
  submitting.value = true

  try {
    await submitRequest({
      songName: form.value.songName.trim(),
      artist: form.value.artist.trim() || undefined,
      category: form.value.category || undefined,
      description: form.value.description.trim() || undefined
    })
    uni.showToast({ title: '提交成功，感谢反馈！', icon: 'success' })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
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

.category-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  padding: 8px 16px;
  border-radius: var(--radius-full);
  background-color: var(--color-gray-7);
  border: 1px solid var(--color-gray-5);
  transition: all var(--animation-fast);
}

.category-tag.active {
  background-color: var(--color-primary-light);
  border-color: var(--color-primary);
}

.category-text {
  font-size: var(--font-size-footnote);
  color: var(--text-secondary);
}

.category-tag.active .category-text {
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
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