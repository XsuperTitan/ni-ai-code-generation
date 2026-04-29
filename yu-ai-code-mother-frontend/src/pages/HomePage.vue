<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'

const router = useRouter()
const creating = ref(false)
const initPrompt = ref('')

const myParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  appName: '',
})
const myKeyword = ref('')
const myApps = ref<API.AppVO[]>([])
const myTotal = ref(0)

const goodParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  appName: '',
})
const goodKeyword = ref('')
const goodApps = ref<API.AppVO[]>([])
const goodTotal = ref(0)

const doCreateApp = async () => {
  if (!initPrompt.value.trim()) {
    message.warning('请输入应用需求提示词')
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt: initPrompt.value })
    if (res.data.code === 0 && res.data.data) {
      await router.push({
        path: `/app/chat/${res.data.data}`,
        query: {
          initPrompt: initPrompt.value,
        },
      })
      return
    }
    message.error('创建应用失败，' + res.data.message)
  } finally {
    creating.value = false
  }
}

const fetchMyApps = async () => {
  const res = await listMyAppVoByPage(myParams)
  if (res.data.code === 0 && res.data.data) {
    myApps.value = res.data.data.records ?? []
    myTotal.value = Number(res.data.data.totalRow ?? 0)
    return
  }
  message.error('获取我的应用失败，' + res.data.message)
}

const fetchGoodApps = async () => {
  const res = await listGoodAppVoByPage(goodParams)
  if (res.data.code === 0 && res.data.data) {
    goodApps.value = res.data.data.records ?? []
    goodTotal.value = Number(res.data.data.totalRow ?? 0)
    return
  }
  message.error('获取精选应用失败，' + res.data.message)
}

const doSearchMy = () => {
  myParams.pageNum = 1
  myParams.appName = myKeyword.value.trim()
  fetchMyApps()
}

const doSearchGood = () => {
  goodParams.pageNum = 1
  goodParams.appName = goodKeyword.value.trim()
  fetchGoodApps()
}

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<template>
  <div id="homePage">
    <a-typography-title :level="2">一句话，生成你想要的网站应用</a-typography-title>
    <a-typography-paragraph type="secondary">
      输入你的需求，AI 会帮你生成可预览、可部署的网站应用
    </a-typography-paragraph>

    <a-card class="prompt-card">
      <a-textarea
        v-model:value="initPrompt"
        placeholder="例如：帮我生成一个酒店预订官网，包含首页、房型列表和在线预约表单"
        :rows="4"
      />
      <div class="prompt-action">
        <a-button type="primary" :loading="creating" @click="doCreateApp">开始创建应用</a-button>
      </div>
    </a-card>

    <a-card title="我的应用" class="list-card">
      <div class="search-row">
        <a-input
          v-model:value="myKeyword"
          allow-clear
          placeholder="按应用名称搜索"
          @pressEnter="doSearchMy"
        />
        <a-button type="primary" @click="doSearchMy">搜索</a-button>
      </div>
      <a-list :grid="{ gutter: 16, column: 3 }" :data-source="myApps">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card hoverable @click="router.push(`/app/chat/${item.id}`)">
              <a-card-meta :title="item.appName || `应用 #${item.id}`">
                <template #description>
                  <div class="muted">创建者：{{ item.user?.userName || '我' }}</div>
                </template>
              </a-card-meta>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
      <a-empty v-if="myApps.length === 0" description="暂无应用" />
      <a-pagination
        class="pager"
        v-model:current="myParams.pageNum"
        v-model:pageSize="myParams.pageSize"
        :total="myTotal"
        :page-size-options="['6', '12', '20']"
        show-size-changer
        @change="fetchMyApps"
      />
    </a-card>

    <a-card title="精选应用" class="list-card">
      <div class="search-row">
        <a-input
          v-model:value="goodKeyword"
          allow-clear
          placeholder="按应用名称搜索"
          @pressEnter="doSearchGood"
        />
        <a-button type="primary" @click="doSearchGood">搜索</a-button>
      </div>
      <a-list :grid="{ gutter: 16, column: 3 }" :data-source="goodApps">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card hoverable @click="router.push(`/app/chat/${item.id}`)">
              <a-card-meta :title="item.appName || `应用 #${item.id}`">
                <template #description>
                  <div class="muted">作者：{{ item.user?.userName || '-' }}</div>
                </template>
              </a-card-meta>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
      <a-empty v-if="goodApps.length === 0" description="暂无精选应用" />
      <a-pagination
        class="pager"
        v-model:current="goodParams.pageNum"
        v-model:pageSize="goodParams.pageSize"
        :total="goodTotal"
        :page-size-options="['6', '12', '20']"
        show-size-changer
        @change="fetchGoodApps"
      />
    </a-card>
  </div>
</template>

<style scoped>
#homePage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prompt-card,
.list-card {
  border-radius: 12px;
}

.prompt-action {
  margin-top: 12px;
  text-align: right;
}

.search-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-bottom: 12px;
}

.pager {
  margin-top: 12px;
  text-align: right;
}

.muted {
  color: #888;
}
</style>
