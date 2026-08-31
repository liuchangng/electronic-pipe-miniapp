<template>
  <div>
    <n-card style="margin-bottom: 16px">
      <n-space justify="end">
        <n-button type="primary" @click="openModal(null)">新增Banner</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="banners" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="pagination.page" v-model:page-size="pagination.pageSize" :item-count="pagination.total" :page-sizes="[10, 20, 50]" show-size-picker @update:page="loadData" @update:page-size="handlePageSizeChange" />
      </div>
    </n-card>

    <n-modal v-model:show="showModal" :title="editingId ? '编辑Banner' : '新增Banner'" preset="card" style="width: 600px">
      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="标题" required>
          <n-input v-model:value="form.title" placeholder="请输入Banner标题" />
        </n-form-item>
        <n-form-item label="图片">
          <ImageUpload v-model:model-value="form.imageUrl" />
        </n-form-item>
        <n-form-item label="链接URL">
          <n-input v-model:value="form.linkUrl" placeholder="点击跳转链接" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NSpace, NTag, useMessage, useDialog } from 'naive-ui'
import { getBannerList, addBanner, updateBanner, deleteBanner } from '../api/banner'
import ImageUpload from '../components/ImageUpload.vue'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)
const deleting = ref(null)
const banners = ref([])
const showModal = ref(false)
const editingId = ref(null)
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const defaultForm = () => ({
  title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1, statusBool: true
})

const form = ref(defaultForm())

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '图片', key: 'imageUrl', width: 200, render: (row) => row.imageUrl ? h('img', { src: row.imageUrl, style: 'width:160px;height:60px;object-fit:cover;border-radius:4px' }) : '-' },
  { title: '链接', key: 'linkUrl', width: 200, ellipsis: { tooltip: true } },
  { title: '排序', key: 'sortOrder', width: 70 },
  { title: '状态', key: 'status', width: 70, render: (row) => h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'default' }, { default: () => row.status === 1 ? '启用' : '禁用' }) },
  { title: '操作', key: 'actions', width: 150, render: (row) => h(NSpace, null, { default: () => [h(NButton, { size: 'small', disabled: deleting.value === row.id, onClick: () => openModal(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', loading: deleting.value === row.id, disabled: deleting.value === row.id, onClick: () => handleDelete(row) }, { default: () => '删除' })] }) }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBannerList({ page: pagination.page, pageSize: pagination.pageSize })
    banners.value = res.data?.list || []
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
    message.warning('请输入Banner标题')
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
      await updateBanner(data)
      message.success('更新成功')
    } else {
      await addBanner(data)
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
    content: `确定要删除Banner"${row.title}"吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      deleting.value = row.id
      try {
        await deleteBanner(row.id)
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
</script>