<template>
  <div>
    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-select
          v-model:value="statusFilter"
          :options="statusOptions"
          placeholder="全部状态"
          clearable
          style="width: 160px"
          @update:value="handleSearch"
        />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="requests" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :item-count="pagination.total"
          :page-sizes="[10, 20, 50]"
          show-size-picker
          @update:page="loadData"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-card>

    <!-- 处理弹窗 -->
    <n-modal v-model:show="showHandleModal" preset="dialog" :title="handleTitle" positive-text="确认" negative-text="取消" @positive-click="confirmHandle">
      <n-form ref="handleFormRef" :model="handleForm">
        <n-form-item label="回复内容">
          <n-input v-model:value="handleForm.reply" type="textarea" placeholder="请输入回复内容（选填）" :rows="3" />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NTag, NButton, NSpace, useMessage, useDialog } from 'naive-ui'
import { getSongRequestList, handleSongRequest } from '../api/songRequest'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const requests = ref([])
const statusFilter = ref(null)
const showHandleModal = ref(false)
const handleTitle = ref('')
const handleForm = reactive({ id: null, status: '', reply: '' })

const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '已采纳', value: 'accepted' },
  { label: '未采纳', value: 'rejected' }
]

const getStatusLabel = (status) => {
  const map = { pending: '待处理', accepted: '已采纳', rejected: '未采纳' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { pending: 'warning', accepted: 'success', rejected: 'error' }
  return map[status] || 'default'
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '曲谱名称', key: 'songName', width: 160 },
  { title: '作者', key: 'artist', width: 120, render: (row) => row.artist || '-' },
  { title: '分类', key: 'category', width: 80, render: (row) => {
    const map = { pop: '流行', classical: '古典', folk: '民谣', movie: '影视', other: '其他' }
    return map[row.category] || row.category || '-'
  }},
  { title: '说明', key: 'description', width: 200, ellipsis: { tooltip: true }, render: (row) => row.description || '-' },
  {
    title: '状态', key: 'status', width: 90,
    render: (row) => h(NTag, { size: 'small', type: getStatusType(row.status) }, { default: () => getStatusLabel(row.status) })
  },
  { title: '回复', key: 'reply', width: 200, ellipsis: { tooltip: true }, render: (row) => row.reply || '-' },
  { title: '提交时间', key: 'createdAt', width: 170 },
  {
    title: '操作', key: 'actions', width: 180,
    render: (row) => {
      if (row.status !== 'pending') return h(NTag, { size: 'small', type: 'info' }, { default: () => '已处理' })
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'success', onClick: () => openHandleModal(row, 'accepted') }, { default: () => '采纳' }),
          h(NButton, { size: 'small', type: 'warning', onClick: () => openHandleModal(row, 'rejected') }, { default: () => '拒绝' })
        ]
      })
    }
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSongRequestList({
      status: statusFilter.value || undefined,
      page: pagination.page,
      size: pagination.pageSize
    })
    requests.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // handled
  } finally {
    loading.value = false
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

const openHandleModal = (row, status) => {
  handleForm.id = row.id
  handleForm.status = status
  handleForm.reply = ''
  handleTitle.value = status === 'accepted' ? '采纳求谱' : '拒绝求谱'
  showHandleModal.value = true
}

const confirmHandle = async () => {
  try {
    await handleSongRequest({
      id: handleForm.id,
      status: handleForm.status,
      reply: handleForm.reply
    })
    message.success('处理成功')
    loadData()
  } catch (e) {
    message.error('处理失败')
  }
}

onMounted(() => {
  loadData()
})
</script>