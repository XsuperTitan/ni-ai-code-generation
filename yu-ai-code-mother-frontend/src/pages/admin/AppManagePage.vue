<template>
  <div id="appManagePage">
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" />
      </a-form-item>
      <a-form-item label="应用类型">
        <a-input v-model:value="searchParams.codeGenType" placeholder="输入 codeGenType" />
      </a-form-item>
      <a-form-item label="创建者ID">
        <a-input-number v-model:value="searchParams.userId" placeholder="输入用户 id" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <a-table :columns="columns" :data-source="data" :pagination="pagination" @change="doTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image v-if="record.cover" :src="record.cover" :width="120" />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag :color="record.priority === 99 ? 'gold' : 'blue'">
            {{ record.priority === 99 ? '精选' : record.priority }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="router.push(`/app/edit/${record.id}`)">编辑</a-button>
            <a-button danger type="link" @click="doDelete(record.id)">删除</a-button>
            <a-button type="link" @click="doGood(record)">精选</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { deleteAppByAdmin, listAppVoByPageByAdmin, updateAppByAdmin } from '@/api/appController'

const router = useRouter()

const columns = [
  { title: 'id', dataIndex: 'id' },
  { title: '应用名称', dataIndex: 'appName' },
  { title: '封面', dataIndex: 'cover' },
  { title: '类型', dataIndex: 'codeGenType' },
  { title: '优先级', dataIndex: 'priority' },
  { title: '创建者ID', dataIndex: 'userId' },
  { title: '创建时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action' },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const fetchData = async () => {
  const res = await listAppVoByPageByAdmin({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = Number(res.data.data.totalRow ?? 0)
    return
  }
  message.error('获取应用列表失败，' + res.data.message)
}

const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (all: number) => `共 ${all} 条`,
  }
})

const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

const doDelete = async (id: number) => {
  const res = await deleteAppByAdmin({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
    return
  }
  message.error('删除失败，' + res.data.message)
}

const doGood = async (record: API.AppVO) => {
  const res = await updateAppByAdmin({
    id: record.id,
    appName: record.appName,
    cover: record.cover,
    priority: 99,
  })
  if (res.data.code === 0) {
    message.success('设置精选成功')
    fetchData()
    return
  }
  message.error('设置精选失败，' + res.data.message)
}

onMounted(() => {
  fetchData()
})
</script>
