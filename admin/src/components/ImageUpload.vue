<template>
  <div class="image-upload">
    <!-- 已上传：显示图片+悬浮操作 -->
    <div v-if="modelValue" class="image-preview" @mouseenter="hover = true" @mouseleave="hover = false">
      <img :src="modelValue" class="preview-img" />
      <div class="preview-overlay" v-if="hover">
        <n-button circle size="small" @click="handlePreview">
          <template #icon>🔍</template>
        </n-button>
        <n-button circle size="small" @click="handleReupload">
          <template #icon>🔄</template>
        </n-button>
        <n-button circle size="small" @click="handleDelete">
          <template #icon>🗑</template>
        </n-button>
      </div>
    </div>
    <!-- 未上传：上传按钮 -->
    <div v-else class="upload-area" @click="triggerUpload">
      <n-button size="small">上传图片</n-button>
    </div>
    <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
    <!-- 图片预览弹窗 -->
    <n-modal v-model:show="showPreview">
      <div style="display:flex;justify-content:center;align-items:center;min-height:400px">
        <img :src="modelValue" style="max-width:90vw;max-height:80vh;object-fit:contain;border-radius:8px" />
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import { uploadFile } from '../api/upload'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])
const message = useMessage()
const hover = ref(false)
const showPreview = ref(false)
const fileInput = ref(null)

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await uploadFile(formData)
    emit('update:modelValue', res.data)
    message.success('上传成功')
  } catch (err) {
    message.error('上传失败')
  }
  e.target.value = ''
}

const handlePreview = () => {
  showPreview.value = true
}

const handleReupload = () => {
  triggerUpload()
}

const handleDelete = () => {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  display: inline-block;
}

.image-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  cursor: pointer;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: opacity 0.2s;
}

.upload-area {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-area:hover {
  border-color: #1890ff;
}
</style>