<template>
  <div>
    <!-- 筛选栏 -->
    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-select v-model:value="statusFilter" placeholder="评论状态" clearable :options="statusOptions" style="width: 120px" @update:value="handleSearch" />
        <n-button type="primary" :loading="loading" @click="handleSearch">搜索</n-button>
      </n-space>
    </n-card>

    <!-- 数据表格 -->
    <n-card>
      <n-data-table :columns="columns" :data="comments" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="pagination.page" v-model:page-size="pagination.pageSize" :item-count="pagination.total" :page-sizes="[10, 20, 50]" show-size-picker @update:page="loadData" @update:page-size="handlePageSizeChange" />
      </div>
    </n-card>

    <!-- 视频详情弹窗 -->
    <n-modal v-model:show="showVideoModal" title="视频详情" preset="card" style="width: 500px">
      <n-descriptions v-if="videoDetail" bordered :column="1" label-placement="left">
        <n-descriptions-item label="ID">{{ videoDetail.id }}</n-descriptions-item>
        <n-descriptions-item label="标题">{{ videoDetail.title }}</n-descriptions-item>
        <n-descriptions-item label="描述">{{ videoDetail.description || '-' }}</n-descriptions-item>
        <n-descriptions-item label="关联曲谱">{{ videoDetail.songTitle || '-' }}</n-descriptions-item>
        <n-descriptions-item label="播放">{{ videoDetail.playCount || 0 }}</n-descriptions-item>
        <n-descriptions-item label="👍">{{ videoDetail.likeCount || 0 }}</n-descriptions-item>
        <n-descriptions-item label="♡">{{ videoDetail.favoriteCount || 0 }}</n-descriptions-item>
        <n-descriptions-item label="💬">{{ videoDetail.commentCount || 0 }}</n-descriptions-item>
        <n-descriptions-item label="↗">{{ videoDetail.shareCount || 0 }}</n-descriptions-item>
      </n-descriptions>
      <n-empty v-else description="视频不存在" />
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NSpace, NTag, NModal, NDescriptions, NDescriptionsItem, NEmpty, useMessage, useDialog } from 'naive-ui'
import { getCommentList, reviewComment, deleteComment } from '../api/comment'
import { getVideoDetail } from '../api/video'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const comments = ref([])
const statusFilter = ref(null)
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

// 视频详情弹窗
const showVideoModal = ref(false)
const videoDetail = ref(null)

const statusOptions = [
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已拒绝', value: 'rejected' }
]

const statusMap = {
  pending: { text: '待审核', type: 'warning' },
  approved: { text: '已通过', type: 'success' },
  rejected: { text: '已拒绝', type: 'error' }
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '视频ID', key: 'videoId', width: 80, render: (row) => h('a', { style: 'color:#1890ff;cursor:pointer', onClick: () => openVideoDetail(row.videoId) }, row.videoId) },
  { title: '用户', key: 'nickname', width: 100, render: (row) => row.nickname || `用户${row.userId}` },
  { title: '评论内容', key: 'content', ellipsis: { tooltip: true } },
  { title: '回复', key: 'parentId', width: 70, render: (row) => row.parentId ? `#${row.parentId}` : '-' },
  {
    title: '状态', key: 'status', width: 90,
    render: (row) => {
      const info = statusMap[row.status] || statusMap.pending
      return h(NTag, { size: 'small', type: info.type }, { default: () => info.text })
    }
  },
  { title: '时间', key: 'createdAt', width: 160 },
  {
    title: '操作', key: 'actions', width: 200,
    render: (row) => h(NSpace, null, {
      default: () => [
        row.status === 'pending' ? h(NButton, { size: 'small', type: 'success', onClick: () => handleReview(row.id, 'approved') }, { default: () => '通过' }) : null,
        row.status === 'pending' ? h(NButton, { size: 'small', type: 'warning', onClick: () => handleReview(row.id, 'rejected') }, { default: () => '拒绝' }) : null,
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
      ].filter(Boolean)
    })
  }
]

const openVideoDetail = async (videoId) => {
  showVideoModal.value = true
  videoDetail.value = null
  try {
    const res = await getVideoDetail(videoId)
    videoDetail.value = res.data || res
  } catch (e) {
    videoDetail.value = null
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCommentList({
      status: statusFilter.value || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    comments.value = res.data?.list || []
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

const handleReview = async (id, status) => {
  try {
    await reviewComment({ id, status })
    message.success(status === 'approved' ? '已通过' : '已拒绝')
    loadData()
  } catch (e) {
    // handled
  }
}

const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除评论"${row.content?.substring(0, 20)}..."吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteComment(row.id)
        message.success('删除成功')
        loadData()
      } catch (e) {
        // handled
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>