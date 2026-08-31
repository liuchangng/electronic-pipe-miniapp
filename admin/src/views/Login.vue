<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <div class="logo-icon">{{ config.system_logo || '♫' }}</div>
      </div>
      <h1 class="login-title">{{ config.system_name || '电子Pipe管理后台' }}</h1>
      <p class="login-subtitle">{{ config.system_subtitle || '曲谱管理系统' }}</p>
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="请输入用户名" input-props="{ autocomplete: 'username' }" @keyup.enter="handleLogin" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="请输入密码" input-props="{ autocomplete: 'current-password' }" @keyup.enter="handleLogin" />
        </n-form-item>
        <n-button type="primary" block :disabled="loading" @click="handleLogin" style="margin-top: 8px; height: 44px; font-size: 16px; font-weight: 600; border-radius: 10px">
          {{ loading ? '登录中...' : '登 录' }}
        </n-button>
      </n-form>
      <p class="login-version" v-if="config.system_version">v{{ config.system_version }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/admin'
import { getPublicConfig } from '../api/config'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const config = reactive({
  system_name: '',
  system_subtitle: '',
  system_version: '',
  system_logo: ''
})

onMounted(async () => {
  try {
    const res = await getPublicConfig()
    const data = res.data || {}
    config.system_name = data.system_name || ''
    config.system_subtitle = data.system_subtitle || ''
    config.system_version = data.system_version || ''
    config.system_logo = data.system_logo || ''
  } catch (e) {
    // 使用默认值
  }
})

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

const handleLogin = async () => {
  try {
    loading.value = true
    const res = await login(form.value)
    const { token, adminInfo } = res.data
    localStorage.setItem('admin_token', token)
    localStorage.setItem('admin_info', JSON.stringify(adminInfo))
    router.push('/')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.login-page::before {
  content: '';
  position: absolute;
  top: -120px;
  right: -120px;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.login-page::after {
  content: '';
  position: absolute;
  bottom: -80px;
  left: -80px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

.login-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.3);
}

.login-title {
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: #1C1C1E;
  margin: 0 0 4px;
}

.login-subtitle {
  text-align: center;
  color: #8E8E93;
  font-size: 14px;
  margin: 0 0 28px;
}

.login-version {
  text-align: center;
  color: #C7C7CC;
  font-size: 12px;
  margin: 16px 0 0;
}
</style>