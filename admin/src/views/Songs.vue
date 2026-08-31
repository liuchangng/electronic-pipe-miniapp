<template>
  <div>
    <!-- 搜索和操作栏 -->
    <n-card style="margin-bottom: 16px">
      <n-space justify="space-between">
        <n-space>
          <n-input v-model:value="keyword" placeholder="搜索曲谱名称/作者" clearable style="width: 240px" @keyup.enter="handleSearch" />
          <n-select v-model:value="category" placeholder="分类" clearable :options="categoryOptions" style="width: 120px" @update:value="handleSearch" />
          <n-select v-model:value="statusFilter" placeholder="状态" clearable :options="statusOptions" style="width: 120px" @update:value="handleSearch" />
          <n-button type="primary" @click="handleSearch">搜索</n-button>
        </n-space>
        <n-button type="primary" @click="openModal(null)">新增曲谱</n-button>
      </n-space>
    </n-card>

    <!-- 数据表格 -->
    <n-card>
      <n-data-table :columns="columns" :data="songs" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="pagination.page" v-model:page-size="pagination.pageSize" :item-count="pagination.total" :page-sizes="[10, 20, 50]" show-size-picker @update:page="loadData" @update:page-size="handlePageSizeChange" />
      </div>
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="editingId ? '编辑曲谱' : '新增曲谱'" preset="card" style="width: 600px">
      <n-form ref="formRef" :model="form" label-placement="left" label-width="80">
        <n-form-item label="标题" required>
          <n-input v-model:value="form.title" placeholder="请输入曲谱标题" />
        </n-form-item>
        <n-form-item label="作者">
          <n-input v-model:value="form.author" placeholder="请输入作者" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </n-form-item>
        <n-form-item label="分类">
          <n-select v-model:value="form.category" :options="categoryOptions" />
        </n-form-item>
        <n-form-item label="图标文字">
          <n-input v-model:value="form.icon" placeholder="如: 🎵" style="width: 100px" />
        </n-form-item>
        <n-form-item label="图标颜色">
          <n-color-picker v-model:value="form.color" :modes="['hex']" :swatches="['#07c160', '#1890ff', '#ff4d4f', '#faad14', '#722ed1']" />
        </n-form-item>
        <n-form-item label="曲谱图片">
          <ImageUpload v-model:model-value="form.imageUrl" />
        </n-form-item>

        <n-form-item label="视频URL">
          <n-input v-model:value="form.videoUrl" placeholder="视频URL（M3U8）" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 120px" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.statusBool" @update:value="v => form.status = v ? 1 : 0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button :disabled="saving" @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" :disabled="saving" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 收藏记录弹窗 -->
    <n-modal v-model:show="showFavModal" :title="favTitle" preset="card" style="width: 700px">
      <n-data-table :columns="favColumns" :data="favData" :loading="favLoading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="favPagination.page" v-model:page-size="favPagination.pageSize" :item-count="favPagination.total" @update:page="handleFavPageChange" />
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NSpace, NTag, NModal, NDataTable, NAvatar, NPagination, useMessage, useDialog } from 'naive-ui'
import { getSongList, addSong, updateSong, deleteSong, getSongFavoriteList } from '../api/song'
import ImageUpload from '../components/ImageUpload.vue'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)
const deleting = ref(null)
const songs = ref([])
const keyword = ref('')
const category = ref(null)
const statusFilter = ref(null)
const showModal = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const categoryOptions = [
  { label: '热门', value: 'hot' },
  { label: '最新', value: 'new' },
  { label: '其他', value: 'other' }
]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const defaultForm = () => ({
  title: '', author: '', description: '', category: 'hot',
  icon: '🎵', color: '#07c160', imageUrl: '',
  videoUrl: '', sortOrder: 0, status: 1, statusBool: true
})

const form = ref(defaultForm())

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '作者', key: 'author', width: 120, ellipsis: { tooltip: true } },
  { title: '封面', key: 'imageUrl', width: 80, render: (row) => row.imageUrl ? h('img', { src: row.imageUrl, style: 'width:60px;height:60px;object-fit:cover;border-radius:4px' }) : h('span', { style: 'color:#999' }, row.icon || '-') },
  { title: '分类', key: 'category', width: 80, render: (row) => h(NTag, { size: 'small', type: row.category === 'hot' ? 'error' : row.category === 'new' ? 'info' : 'default' }, { default: () => row.category === 'hot' ? '热门' : row.category === 'new' ? '最新' : '其他' }) },
  { title: '浏览', key: 'viewCount', width: 70 },
  { title: '收藏', key: 'favoriteCount', width: 70, render: (row) => h('a', { style: 'color:#1890ff;cursor:pointer', onClick: () => openSongFavorite(row.id, row.title) }, row.favoriteCount || 0) },
  { title: '状态', key: 'status', width: 70, render: (row) => h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'default' }, { default: () => row.status === 1 ? '启用' : '禁用' }) },
  { title: '操作', key: 'actions', width: 150, render: (row) => h(NSpace, null, { default: () => [h(NButton, { size: 'small', disabled: deleting.value === row.id, onClick: () => openModal(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', loading: deleting.value === row.id, disabled: deleting.value === row.id, onClick: () => handleDelete(row) }, { default: () => '删除' })] }) }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSongList({ keyword: keyword.value, category: category.value, status: statusFilter.value, page: pagination.page, pageSize: pagination.pageSize })
    songs.value = res.data?.list || []
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

const openModal = (row) => {
  if (row) {
    editingId.value = row.id
    form.value = { ...row, statusBool: row.status === 1 }
  } else {
    editingId.value = null
    form.value = defaultForm()
  }
  showModal.value = true
}

const handleSave = async () => {
  if (!form.value.title) {
    message.warning('请输入曲谱标题')
    return
  }
  saving.value = true
  try {
    const data = { ...form.value }
    delete data.statusBool
    delete data.createdAt
    delete data.updatedAt
    delete data.deleted
    if (editingId.value) {
      data.id = editingId.value
      await updateSong(data)
      message.success('更新成功')
    } else {
      await addSong(data)
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

const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除曲谱"${row.title}"吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      deleting.value = row.id
      try {
        await deleteSong(row.id)
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

onMounted(loadData)

// 曲谱收藏记录弹窗
const showFavModal = ref(false)
const favTitle = ref('')
const favLoading = ref(false)
const favData = ref([])
const favPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const favSongId = ref(null)

const openSongFavorite = (songId, title) => {
  favSongId.value = songId
  favTitle.value = `${title} - 收藏列表`
  favPagination.page = 1
  showFavModal.value = true
  loadFavData()
}

const loadFavData = async () => {
  favLoading.value = true
  try {
    const res = await getSongFavoriteList({ songId: favSongId.value, page: favPagination.page, pageSize: favPagination.pageSize })
    favData.value = res.data?.list || []
    favPagination.total = res.data?.total || 0
  } catch (e) {
    // handled
  } finally {
    favLoading.value = false
  }
}

const handleFavPageChange = (page) => {
  favPagination.page = page
  loadFavData()
}

const favColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '头像', key: 'avatar', width: 60, render: (row) => h(NAvatar, { size: 32, src: row.avatar || undefined }, { default: () => row.nickname?.[0] || '?' }) },
  { title: '昵称', key: 'nickname', render: (row) => row.nickname || '匿名用户' },
  { title: '时间', key: 'createdAt', width: 160 }
]
</script>