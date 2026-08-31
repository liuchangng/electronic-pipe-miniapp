<template>
  <div>
    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-input v-model:value="keyword" placeholder="搜索用户昵称/openid" clearable style="width: 300px" @keyup.enter="handleSearch" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
      </n-space>
    </n-card>

    <n-card>
      <n-data-table :columns="columns" :data="users" :loading="loading" :bordered="false" />
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination v-model:page="pagination.page" v-model:page-size="pagination.pageSize" :item-count="pagination.total" :page-sizes="[10, 20, 50]" show-size-picker @update:page="loadData" @update:page-size="handlePageSizeChange" />
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NTag, NButton, NSpace, useMessage, useDialog } from 'naive-ui'
import { getUserList, updateUserStatus } from '../api/user'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const toggling = ref(null)
const users = ref([])
const keyword = ref('')
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '昵称', key: 'nickname', width: 150, render: (row) => row.nickname || '未设置' },
  { title: 'OpenID', key: 'openid', width: 200, ellipsis: { tooltip: true } },
  { title: '头像', key: 'avatar', width: 80, render: (row) => row.avatar ? h('img', { src: row.avatar, style: 'width:32px;height:32px;border-radius:50%' }) : '-' },
  { title: '会员类型', key: 'memberType', width: 100, render: (row) => row.memberType || '普通用户' },
  { title: '注册时间', key: 'createdAt', width: 170 },
  { title: '状态', key: 'deleted', width: 80, render: (row) => h(NTag, { size: 'small', type: row.deleted === 0 ? 'success' : 'error' }, { default: () => row.deleted === 0 ? '正常' : '禁用' }) },
  {
    title: '操作', key: 'actions', width: 100,
    render: (row) => h(NButton, {
      size: 'small',
      type: row.deleted === 0 ? 'warning' : 'success',
      loading: toggling.value === row.id,
      disabled: toggling.value === row.id,
      onClick: () => handleToggleStatus(row)
    }, { default: () => row.deleted === 0 ? '禁用' : '启用' })
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ keyword: keyword.value, page: pagination.page, pageSize: pagination.pageSize })
    users.value = res.data?.list || []
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

const handleToggleStatus = (row) => {
  const newStatus = row.deleted === 0 ? 1 : 0
  const action = newStatus === 1 ? '禁用' : '启用'
  dialog.warning({
    title: `确认${action}`,
    content: `确定要${action}用户"${row.nickname || row.openid}"吗？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      toggling.value = row.id
      try {
        await updateUserStatus({ id: row.id, status: newStatus })
        message.success(`${action}成功`)
        loadData()
      } catch (e) {
        // handled
      } finally {
        toggling.value = null
      }
    }
  })
}

onMounted(loadData)
</script>