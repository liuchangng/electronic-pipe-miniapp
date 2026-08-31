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
        <n-select
          v-model:value="typeFilter"
          :options="typeOptions"
          placeholder="全部类型"
          clearable
          style="width: 160px"
          @update:value="handleSearch"
        />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="pagedData" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :item-count="totalCount"
          :page-sizes="[10, 20, 50]"
          show-size-picker
          @update:page="loadData"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-card>

    <!-- 回复弹窗 -->
    <n-modal v-model:show="showReplyModal" preset="dialog" title="回复反馈" positive-text="确认回复" negative-text="取消" @positive-click="confirmReply">
      <n-form ref="replyFormRef" :model="replyForm">
        <n-form-item label="反馈内容">
          <n-input :value="replyForm.content" type="textarea" :rows="3" readonly />
        </n-form-item>
        <n-form-item label="回复内容" required>
          <n-input v-model:value="replyForm.reply" type="textarea" placeholder="请输入回复内容" :rows="4" />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { NTag, NButton, NSpace, useMessage } from 'naive-ui'
import { getFeedbackList, replyFeedback } from '../api/feedback'

const message = useMessage()
const loading = ref(false)
const feedbacks = ref([])
const statusFilter = ref(null)
const typeFilter = ref(null)
const showReplyModal = ref(false)
const replyForm = reactive({ id: null, content: '', reply: '' })

const pagination = reactive({ page: 1, pageSize: 20 })

const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '已处理', value: 'resolved' }
]

const typeOptions = [
  { label: '建议', value: 'suggestion' },
  { label: '问题', value: 'bug' },
  { label: '其他', value: 'other' }
]

const getTypeLabel = (type) => {
  const map = { suggestion: '建议', bug: '问题', other: '其他' }
  return map[type] || type
}

const getTypeColor = (type) => {
  const map = { suggestion: 'info', bug: 'error', other: 'default' }
  return map[type] || 'default'
}

const getStatusLabel = (status) => {
  const map = { pending: '待处理', resolved: '已处理' }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { pending: 'warning', resolved: 'success' }
  return map[status] || 'default'
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  {
    title: '类型', key: 'type', width: 80,
    render: (row) => h(NTag, { size: 'small', type: getTypeColor(row.type) }, { default: () => getTypeLabel(row.type) })
  },
  { title: '反馈内容', key: 'content', width: 220, ellipsis: { tooltip: true } },
  { title: '联系方式', key: 'contact', width: 130, render: (row) => row.contact || '-' },
  {
    title: '状态', key: 'status', width: 90,
    render: (row) => h(NTag, { size: 'small', type: getStatusType(row.status) }, { default: () => getStatusLabel(row.status) })
  },
  { title: '回复', key: 'reply', width: 200, ellipsis: { tooltip: true }, render: (row) => row.reply || '-' },
  { title: '提交时间', key: 'createdAt', width: 170 },
  {
    title: '操作', key: 'actions', width: 120,
    render: (row) => {
      if (row.status === 'resolved') return h(NTag, { size: 'small', type: 'info' }, { default: () => '已回复' })
      return h(NButton, { size: 'small', type: 'primary', onClick: () => openReplyModal(row) }, { default: () => '回复' })
    }
  }
]

// 前端分页+筛选
const filteredData = computed(() => {
  let data = feedbacks.value
  if (statusFilter.value) {
    data = data.filter(f => f.status === statusFilter.value)
  }
  if (typeFilter.value) {
    data = data.filter(f => f.type === typeFilter.value)
  }
  return data
})

const pagedData = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredData.value.slice(start, start + pagination.pageSize)
})

const totalCount = computed(() => filteredData.value.length)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getFeedbackList()
    feedbacks.value = res.data || []
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
}

const handleSearch = () => {
  pagination.page = 1
}

const openReplyModal = (row) => {
  replyForm.id = row.id
  replyForm.content = row.content
  replyForm.reply = row.reply || ''
  showReplyModal.value = true
}

const confirmReply = async () => {
  if (!replyForm.reply.trim()) {
    message.warning('请输入回复内容')
    return false
  }
  try {
    await replyFeedback({ id: replyForm.id, reply: replyForm.reply })
    message.success('回复成功')
    loadData()
  } catch (e) {
    message.error('回复失败')
  }
  return true
}

onMounted(() => {
  loadData()
})
</script>