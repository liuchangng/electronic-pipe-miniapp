<template>
  <div>
    <n-card style="margin-bottom: 16px">
      <n-space justify="space-between">
        <n-space>
          <n-input v-model:value="keyword" placeholder="搜索视频标题" clearable style="width: 240px" @keyup.enter="handleSearch" />
          <n-button type="primary" :loading="loading" @click="handleSearch">搜索</n-button>
        </n-space>
        <n-button type="primary" @click="openUploadModal">上传视频</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="videos" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="pagination.page" v-model:page-size="pagination.pageSize" :item-count="pagination.total" :page-sizes="[10, 20, 50]" show-size-picker @update:page="loadData" @update:page-size="handlePageSizeChange" />
      </div>
    </n-card>

    <!-- 编辑视频弹窗 -->
    <n-modal v-model:show="showModal" :title="editingId ? '编辑视频' : '新增视频'" preset="card" style="width: 600px">
      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="标题" required>
          <n-input v-model:value="form.title" placeholder="请输入视频标题" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </n-form-item>
        <n-form-item label="关联曲谱">
          <n-select v-model:value="form.songId" :options="songOptions" placeholder="选择关联曲谱" clearable filterable />
        </n-form-item>
        <n-form-item label="视频URL">
          <n-input v-model:value="form.videoUrl" placeholder="视频URL（M3U8）" />
        </n-form-item>
        <n-form-item label="缩略图">
          <n-input v-model:value="form.thumbnailUrl" placeholder="缩略图URL" />
        </n-form-item>
        <n-form-item label="时长(秒)">
          <n-input-number v-model:value="form.duration" :min="0" style="width: 120px" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 120px" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button :disabled="saving" @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" :disabled="saving" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 上传视频弹窗 -->
    <n-modal v-model:show="showUploadModal" title="上传视频" preset="card" style="width: 600px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="视频文件" required>
          <n-upload
            :max="1"
            :default-upload="false"
            :show-file-list="false"
            accept="video/*"
            @change="handleFileChange"
          >
            <n-button>选择视频文件</n-button>
          </n-upload>
          <n-text v-if="uploadFile" depth="3" style="margin-left: 8px">{{ uploadFile.name }}</n-text>
        </n-form-item>
        <n-form-item label="标题">
          <n-input v-model:value="uploadForm.title" placeholder="留空则使用文件名" />
        </n-form-item>
        <n-form-item label="关联曲谱">
          <n-select v-model:value="uploadForm.songId" :options="songOptions" placeholder="选择关联曲谱" clearable filterable />
        </n-form-item>
      </n-form>
      <n-alert v-if="uploadStatus" :type="uploadStatusType" style="margin-top: 12px">
        {{ uploadStatusText }}
      </n-alert>
      <template #footer>
        <n-space justify="end">
          <n-button :disabled="uploading" @click="showUploadModal = false">取消</n-button>
          <n-button type="primary" :loading="uploading" :disabled="!uploadFile || uploading" @click="handleUpload">上传</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 互动记录弹窗 -->
    <n-modal v-model:show="showInteractionModal" :title="interactionTitle" preset="card" style="width: 700px">
      <n-data-table :columns="interactionColumns" :data="interactionData" :loading="interactionLoading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="interactionPagination.page" v-model:page-size="interactionPagination.pageSize" :item-count="interactionPagination.total" @update:page="handleInteractionPageChange" />
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { NButton, NSpace, NTag, NTooltip, NModal, NDataTable, NAvatar, useMessage, useDialog } from 'naive-ui'
import { getVideoList, addVideo, updateVideo, deleteVideo, uploadVideo, getTranscodeStatus, uploadChunk, mergeChunks, retryTranscode, getVideoLikeList, getVideoFavoriteList, getVideoShareList } from '../api/video'
import { getSongSelectList } from '../api/song'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const deleting = ref(null)  // 正在删除的行ID
const retrying = ref(null)   // 正在重试转码的行ID
const videos = ref([])
const keyword = ref('')
const showModal = ref(false)
const showUploadModal = ref(false)
const editingId = ref(null)
const uploadFile = ref(null)
const uploadStatus = ref('')  // uploading/merging/processing/done/failed
const uploadProgress = ref(0)
const uploadVideoId = ref(null)
const uploadError = ref('')
const songOptions = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

// 互动记录弹窗
const showInteractionModal = ref(false)
const interactionTitle = ref('')
const interactionLoading = ref(false)
const interactionData = ref([])
const interactionPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const interactionVideoId = ref(null)
const interactionType = ref('like') // like/favorite/share

const defaultForm = () => ({
  title: '', description: '', songId: null, videoUrl: '',
  thumbnailUrl: '', duration: 0, sortOrder: 0
})

const form = ref(defaultForm())
const uploadForm = reactive({ title: '', songId: null })

// 转码状态显示
const transcodeStatusMap = {
  pending: { text: '待转码', type: 'warning' },
  processing: { text: '转码中', type: 'info' },
  done: { text: '已完成', type: 'success' },
  failed: { text: '失败', type: 'error' },
  none: { text: '无', type: 'default' }
}

// 上传状态提示
const uploadStatusType = computed(() => {
  if (uploadStatus.value === 'uploading') return 'info'
  if (uploadStatus.value === 'merging') return 'info'
  if (uploadStatus.value === 'processing') return 'info'
  if (uploadStatus.value === 'done') return 'success'
  if (uploadStatus.value === 'failed') return 'error'
  return 'info'
})

const uploadStatusText = computed(() => {
  if (uploadStatus.value === 'uploading') return `分片上传中... ${uploadProgress.value}%`
  if (uploadStatus.value === 'merging') return '分片合并中...'
  if (uploadStatus.value === 'processing') return '视频已提交，等待转码队列处理...'
  if (uploadStatus.value === 'done') return '转码完成！'
  if (uploadStatus.value === 'failed') return `转码失败${uploadError.value ? '：' + uploadError.value : ''}`
  return ''
})

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '关联曲谱', key: 'songTitle', width: 150, render: (row) => row.songTitle || '-' },
  {
    title: '转码状态', key: 'transcodeStatus', width: 100,
    render: (row) => {
      const status = row.transcodeStatus || 'none'
      const info = transcodeStatusMap[status] || transcodeStatusMap.none
      const tag = h(NTag, { type: info.type, size: 'small' }, { default: () => info.text })
      // 失败时显示错误原因
      if (status === 'failed' && row.transcodeError) {
        return h(NTooltip, null, {
          trigger: () => tag,
          default: () => row.transcodeError
        })
      }
      return tag
    }
  },
  { title: '时长', key: 'duration', width: 80, render: (row) => `${Math.floor((row.duration || 0) / 60)}:${String((row.duration || 0) % 60).padStart(2, '0')}` },
  { title: '排序', key: 'sortOrder', width: 70 },
  { title: '👍', key: 'likeCount', width: 55, render: (row) => h('a', { style: 'color:#1890ff;cursor:pointer', onClick: () => openInteraction(row.id, 'like', row.title) }, row.likeCount || 0) },
  { title: '♡', key: 'favoriteCount', width: 55, render: (row) => h('a', { style: 'color:#1890ff;cursor:pointer', onClick: () => openInteraction(row.id, 'favorite', row.title) }, row.favoriteCount || 0) },
  { title: '💬', key: 'commentCount', width: 55, render: (row) => row.commentCount || 0 },
  { title: '↗', key: 'shareCount', width: 55, render: (row) => h('a', { style: 'color:#1890ff;cursor:pointer', onClick: () => openInteraction(row.id, 'share', row.title) }, row.shareCount || 0) },
  {
    title: '操作', key: 'actions', width: 200,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', disabled: deleting.value === row.id, onClick: () => openModal(row) }, { default: () => '编辑' }),
        row.transcodeStatus === 'failed' ? h(NButton, { size: 'small', type: 'warning', loading: retrying.value === row.id, disabled: retrying.value === row.id, onClick: () => handleRetryTranscode(row) }, { default: () => '重试转码' }) : null,
        h(NButton, { size: 'small', type: 'error', loading: deleting.value === row.id, disabled: deleting.value === row.id, onClick: () => handleDelete(row) }, { default: () => '删除' })
      ].filter(Boolean)
    })
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getVideoList({ keyword: keyword.value, page: pagination.page, pageSize: pagination.pageSize })
    videos.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

// 互动记录弹窗
const typeLabelMap = { like: '点赞', favorite: '收藏', share: '分享' }

const openInteraction = (videoId, type, title) => {
  interactionVideoId.value = videoId
  interactionType.value = type
  interactionTitle.value = `${title} - ${typeLabelMap[type]}列表`
  interactionPagination.page = 1
  showInteractionModal.value = true
  loadInteractionData()
}

const loadInteractionData = async () => {
  interactionLoading.value = true
  try {
    const params = { videoId: interactionVideoId.value, page: interactionPagination.page, pageSize: interactionPagination.pageSize }
    let res
    if (interactionType.value === 'like') {
      res = await getVideoLikeList(params)
    } else if (interactionType.value === 'favorite') {
      res = await getVideoFavoriteList(params)
    } else {
      res = await getVideoShareList(params)
    }
    interactionData.value = res.data?.list || []
    interactionPagination.total = res.data?.total || 0
  } catch (e) {
    // handled
  } finally {
    interactionLoading.value = false
  }
}

const handleInteractionPageChange = (page) => {
  interactionPagination.page = page
  loadInteractionData()
}

const interactionColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '头像', key: 'avatar', width: 60, render: (row) => h(NAvatar, { size: 32, src: row.avatar || undefined }, { default: () => row.nickname?.[0] || '?' }) },
  { title: '昵称', key: 'nickname', render: (row) => row.nickname || '匿名用户' },
  { title: '时间', key: 'createdAt', width: 160 }
]

const loadSongOptions = async () => {
  try {
    const res = await getSongSelectList()
    songOptions.value = (res.data || []).map(s => ({
      label: `${s.title}${s.author ? ' - ' + s.author : ''}`,
      value: s.id
    }))
  } catch (e) {
    // handled
  }
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const openModal = (row) => {
  if (row) {
    editingId.value = row.id
    form.value = { ...row }
  } else {
    editingId.value = null
    form.value = defaultForm()
  }
  showModal.value = true
}

const openUploadModal = () => {
  uploadForm.title = ''
  uploadForm.songId = null
  uploadFile.value = null
  uploadStatus.value = ''
  uploadProgress.value = 0
  uploadVideoId.value = null
  uploadError.value = ''
  showUploadModal.value = true
}

const handleFileChange = ({ file }) => {
  uploadFile.value = file?.file || null
}

const handleSave = async () => {
  if (!form.value.title) {
    message.warning('请输入视频标题')
    return
  }
  saving.value = true
  try {
    const data = { ...form.value }
    delete data.createdAt
    delete data.updatedAt
    delete data.deleted
    delete data.songTitle
    delete data.transcodeStatus
    delete data.transcodeError
    if (editingId.value) {
      data.id = editingId.value
      await updateVideo(data)
      message.success('更新成功')
    } else {
      await addVideo(data)
      message.success('新增成功')
    }
    showModal.value = false
    loadData()
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}

const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB per chunk

const handleUpload = async () => {
  if (!uploadFile.value) {
    message.warning('请选择视频文件')
    return
  }
  uploading.value = true
  uploadStatus.value = ''
  uploadError.value = ''
  try {
    const file = uploadFile.value
    const fileSize = file.size

    // 小于10MB的文件直接上传
    if (fileSize < 10 * 1024 * 1024) {
      const formData = new FormData()
      formData.append('file', file)
      if (uploadForm.title) formData.append('title', uploadForm.title)
      if (uploadForm.songId) formData.append('songId', String(uploadForm.songId))
      const res = await uploadVideo(formData)
      uploadVideoId.value = res.data?.id
      uploadStatus.value = 'processing'
      message.info('视频已上传，已提交转码队列')
      showUploadModal.value = false
      loadData()
      pollTranscodeStatus(uploadVideoId.value)
      return
    }

    // 大文件分片上传
    const totalChunks = Math.ceil(fileSize / CHUNK_SIZE)
    const uploadId = Date.now().toString(36) + Math.random().toString(36).substring(2, 8)
    uploadStatus.value = 'uploading'
    message.info(`大文件分片上传中，共${totalChunks}片...`)

    for (let i = 0; i < totalChunks; i++) {
      const start = i * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, fileSize)
      const chunk = file.slice(start, end)
      const formData = new FormData()
      formData.append('file', chunk, file.name)
      formData.append('uploadId', uploadId)
      formData.append('chunkIndex', String(i))
      formData.append('totalChunks', String(totalChunks))
      await uploadChunk(formData)
      uploadProgress.value = Math.round(((i + 1) / totalChunks) * 100)
    }

    // 合并分片
    uploadStatus.value = 'merging'
    const mergeRes = await mergeChunks({
      uploadId,
      fileName: file.name,
      totalChunks,
      title: uploadForm.title || file.name,
      songId: uploadForm.songId
    })
    uploadVideoId.value = mergeRes.data?.id
    uploadStatus.value = 'processing'
    uploadProgress.value = 100
    message.info('分片合并完成，已提交转码队列')
    showUploadModal.value = false
    loadData()
    pollTranscodeStatus(uploadVideoId.value)
  } catch (e) {
    message.error('上传失败：' + (e.message || '未知错误'))
    uploadStatus.value = 'failed'
    uploadError.value = e.message || '未知错误'
  } finally {
    uploading.value = false
  }
}

const pollTranscodeStatus = (videoId) => {
  const timer = setInterval(async () => {
    try {
      const res = await getTranscodeStatus(videoId)
      const status = res.data?.status
      const error = res.data?.error
      if (status === 'done') {
        uploadStatus.value = 'done'
        message.success('视频转码完成！')
        clearInterval(timer)
        loadData()
      } else if (status === 'failed') {
        uploadStatus.value = 'failed'
        uploadError.value = error || ''
        message.error('视频转码失败' + (error ? '：' + error : ''))
        clearInterval(timer)
        loadData()
      }
    } catch (e) {
      clearInterval(timer)
    }
  }, 5000)  // 5秒轮询
  // 最多轮询10分钟
  setTimeout(() => clearInterval(timer), 600000)
}

const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除视频"${row.title}"吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      deleting.value = row.id
      try {
        await deleteVideo(row.id)
        message.success('删除成功')
        loadData()
      } catch (e) {
        // handled
      } finally {
        deleting.value = null
      }
    }
  })
}

const handleRetryTranscode = async (row) => {
  retrying.value = row.id
  try {
    await retryTranscode(row.id)
    message.success('已重新提交转码')
    loadData()
  } catch (e) {
    // handled
  } finally {
    retrying.value = null
  }
}

onMounted(() => {
  loadData()
  loadSongOptions()
})
</script>