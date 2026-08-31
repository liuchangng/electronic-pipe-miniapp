<template>
  <div>
    <n-grid :cols="3" :x-gap="16" :y-gap="16">
      <n-gi>
        <n-card class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon-wrap blue">
              <span class="stat-emoji">🎵</span>
            </div>
            <n-statistic label="曲谱总数" :value="stats.songCount || 0" />
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon-wrap green">
              <span class="stat-emoji">🎬</span>
            </div>
            <n-statistic label="视频总数" :value="stats.videoCount || 0" />
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon-wrap orange">
              <span class="stat-emoji">👤</span>
            </div>
            <n-statistic label="用户总数" :value="stats.userCount || 0" />
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon-wrap purple">
              <span class="stat-emoji">🖼</span>
            </div>
            <n-statistic label="Banner总数" :value="stats.bannerCount || 0" />
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon-wrap cyan">
              <span class="stat-emoji">👁</span>
            </div>
            <n-statistic label="总浏览量" :value="stats.totalViewCount || 0" />
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon-wrap red">
              <span class="stat-emoji">▶️</span>
            </div>
            <n-statistic label="总播放量" :value="stats.totalPlayCount || 0" />
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <n-card title="系统信息" style="margin-top: 16px">
      <template #header-extra>
        <n-button type="primary" size="small" @click="handleSaveConfig" :loading="savingConfig" :disabled="savingConfig">保存配置</n-button>
      </template>
      <n-form label-placement="left" label-width="100">
        <n-form-item label="系统名称">
          <n-input v-model:value="configForm.system_name" placeholder="请输入系统名称" />
        </n-form-item>
        <n-form-item label="系统副标题">
          <n-input v-model:value="configForm.system_subtitle" placeholder="登录页副标题" />
        </n-form-item>
        <n-form-item label="版本号">
          <n-input v-model:value="configForm.system_version" placeholder="如 1.0.0" />
        </n-form-item>
        <n-form-item label="Logo图标">
          <n-input v-model:value="configForm.system_logo" placeholder="Logo文字（如 ♫）或图片URL" />
        </n-form-item>
        <n-form-item label="后端框架">
          <n-input value="Spring Boot + MyBatis-Flex" disabled />
        </n-form-item>
        <n-form-item label="前端框架">
          <n-input value="Vue 3 + Naive UI" disabled />
        </n-form-item>
        <n-form-item label="数据库">
          <n-input value="SQLite" disabled />
        </n-form-item>
        <n-form-item label="认证方式">
          <n-input value="Sa-Token" disabled />
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getStats } from '../api/admin'
import { getConfig, updateConfig } from '../api/config'

const message = useMessage()
const stats = ref({})
const savingConfig = ref(false)

const configForm = reactive({
  system_name: '',
  system_subtitle: '',
  system_version: '',
  system_logo: ''
})

onMounted(async () => {
  // 加载统计
  try {
    const res = await getStats()
    stats.value = res.data || {}
  } catch (e) { /* ignore */ }

  // 加载配置
  try {
    const res = await getConfig()
    const data = res.data || {}
    configForm.system_name = data.system_name || '电子Pipe曲谱小程序'
    configForm.system_subtitle = data.system_subtitle || '曲谱管理系统'
    configForm.system_version = data.system_version || '1.0.0'
    configForm.system_logo = data.system_logo || '♫'
  } catch (e) { /* ignore */ }
})

const handleSaveConfig = async () => {
  savingConfig.value = true
  try {
    await updateConfig(configForm)
    message.success('配置保存成功')
  } catch (e) {
    message.error('保存失败')
  } finally {
    savingConfig.value = false
  }
}
</script>

<style scoped>
.stat-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-wrap.blue {
  background-color: rgba(0, 122, 255, 0.1);
}

.stat-icon-wrap.green {
  background-color: rgba(52, 199, 89, 0.1);
}

.stat-icon-wrap.orange {
  background-color: rgba(255, 149, 0, 0.1);
}

.stat-icon-wrap.purple {
  background-color: rgba(88, 86, 214, 0.1);
}

.stat-icon-wrap.cyan {
  background-color: rgba(0, 188, 212, 0.1);
}

.stat-icon-wrap.red {
  background-color: rgba(244, 67, 54, 0.1);
}

.stat-emoji {
  font-size: 24px;
}
</style>