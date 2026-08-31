<template>
  <div>
    <!-- 搜索和操作栏 -->
    <n-card style="margin-bottom: 16px">
      <n-space justify="space-between">
        <n-space>
          <n-select v-model:value="categoryFilter" placeholder="分类筛选" clearable :options="categoryOptions" style="width: 150px" @update:value="handleSearch" />
          <n-button type="primary" @click="handleSearch">搜索</n-button>
        </n-space>
        <n-button type="primary" @click="openModal(null)">新增知识</n-button>
      </n-space>
    </n-card>

    <!-- 数据表格 -->
    <n-card>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="pagination.page" v-model:page-size="pagination.pageSize" :item-count="pagination.total" :page-sizes="[10, 20, 50]" show-size-picker @update:page="loadData" @update:page-size="handlePageSizeChange" />
      </div>
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="editingId ? '编辑知识' : '新增知识'" preset="card" style="width: 700px">
      <n-form ref="formRef" :model="form" label-placement="left" label-width="80">
        <n-form-item label="标题" required>
          <n-input v-model:value="form.title" placeholder="请输入知识标题" />
        </n-form-item>
        <n-form-item label="副标题">
          <n-input v-model:value="form.subtitle" placeholder="请输入副标题" />
        </n-form-item>
        <n-form-item label="分类" required>
          <n-select v-model:value="form.category" :options="categoryOptions" placeholder="请选择分类" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 120px" />
        </n-form-item>
        <n-form-item label="内容">
          <n-input v-model:value="form.content" type="textarea" :rows="10" placeholder="请输入HTML内容" />
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
import { getKnowledgeList, addKnowledge, updateKnowledge, deleteKnowledge } from '../api/knowledge'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)
const deleting = ref(null)
const list = ref([])
const categoryFilter = ref(null)
const showModal = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const categoryOptions = [
  { label: '入门介绍', value: 'introduction' },
  { label: '历史渊源', value: 'history' },
  { label: '品牌介绍', value: 'brand' },
  { label: '指法教学', value: 'fingering' }
]

const categoryMap = {
  introduction: '入门介绍',
  history: '历史渊源',
  brand: '品牌介绍',
  fingering: '指法教学'
}

const defaultForm = () => ({
  title: '', subtitle: '', category: 'introduction',
  content: '', sortOrder: 0
})

const form = ref(defaultForm())

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '副标题', key: 'subtitle', width: 120, ellipsis: { tooltip: true } },
  { title: '分类', key: 'category', width: 100, render: (row) => h(NTag, { size: 'small', type: getCategoryTagType(row.category) }, { default: () => categoryMap[row.category] || row.category }) },
  { title: '排序', key: 'sortOrder', width: 70 },
  { title: '更新时间', key: 'updatedAt', width: 170, render: (row) => row.updatedAt ? new Date(row.updatedAt).toLocaleString() : '-' },
  { title: '操作', key: 'actions', width: 150, render: (row) => h(NSpace, null, { default: () => [h(NButton, { size: 'small', disabled: deleting.value === row.id, onClick: () => openModal(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', loading: deleting.value === row.id, disabled: deleting.value === row.id, onClick: () => handleDelete(row) }, { default: () => '删除' })] }) }
]

const getCategoryTagType = (category) => {
  const map = { introduction: 'success', history: 'info', brand: 'warning', fingering: 'error' }
  return map[category] || 'default'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList({ category: categoryFilter.value, page: pagination.page, pageSize: pagination.pageSize })
    list.value = res.data?.list || []
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
    form.value = { title: row.title || '', subtitle: row.subtitle || '', category: row.category || 'introduction', content: row.content || '', sortOrder: row.sortOrder || 0 }
  } else {
    editingId.value = null
    form.value = defaultForm()
  }
  showModal.value = true
}

const handleSave = async () => {
  if (!form.value.title) {
    message.warning('请输入知识标题')
    return
  }
  if (!form.value.category) {
    message.warning('请选择分类')
    return
  }
  saving.value = true
  try {
    const data = { ...form.value }
    delete data.createdAt
    delete data.updatedAt
    delete data.deleted
    if (editingId.value) {
      data.id = editingId.value
      await updateKnowledge(data)
      message.success('更新成功')
    } else {
      await addKnowledge(data)
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
    content: `确定要删除知识"${row.title}"吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      deleting.value = row.id
      try {
        await deleteKnowledge(row.id)
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