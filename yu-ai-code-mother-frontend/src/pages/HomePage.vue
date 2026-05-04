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
const appDeployBaseUrl = import.meta.env.VITE_APP_DEPLOY_BASE_URL || 'http://localhost'

const joinUrl = (baseUrl: string, path: string) => {
  return `${baseUrl.replace(/\/$/, '')}/${path.replace(/^\//, '')}`
}

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

const viewChat = (appId?: string | number) => {
  if (!appId) {
    return
  }
  router.push({
    path: `/app/chat/${appId}`,
    query: {
      view: '1',
    },
  })
}

const getWorkUrl = (deployKey?: string) => {
  if (!deployKey) {
    return ''
  }
  return joinUrl(appDeployBaseUrl, deployKey)
}

const viewWork = (deployKey?: string) => {
  const workUrl = getWorkUrl(deployKey)
  if (!workUrl) {
    return
  }
  window.open(workUrl, '_blank')
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
            <a-card hoverable>
              <template #cover>
                <img v-if="item.cover" :src="item.cover" alt="应用封面" class="app-cover" />
                <div v-else class="app-cover app-cover-placeholder">暂无封面</div>
              </template>
              <div class="app-info">
                <a-avatar :src="item.user?.userAvatar">
                  {{ item.user?.userName?.slice(0, 1) || '我' }}
                </a-avatar>
                <div class="app-info-content">
                  <div class="app-title">{{ item.appName || `应用 #${item.id}` }}</div>
                  <div class="muted">{{ item.user?.userName || '我' }}</div>
                </div>
              </div>
              <a-space class="card-actions">
                <a-button type="link" @click="viewChat(item.id)">查看对话</a-button>
                <a-button v-if="item.deployKey" type="link" @click="viewWork(item.deployKey)">
                  查看作品
                </a-button>
              </a-space>
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
            <a-card hoverable>
              <template #cover>
                <img v-if="item.cover" :src="item.cover" alt="应用封面" class="app-cover" />
                <div v-else class="app-cover app-cover-placeholder">暂无封面</div>
              </template>
              <div class="app-info">
                <a-avatar :src="item.user?.userAvatar">
                  {{ item.user?.userName?.slice(0, 1) || '匿' }}
                </a-avatar>
                <div class="app-info-content">
                  <div class="app-title">{{ item.appName || `应用 #${item.id}` }}</div>
                  <div class="muted">{{ item.user?.userName || '无名' }}</div>
                </div>
              </div>
              <a-space class="card-actions">
                <a-button type="link" @click="viewChat(item.id)">查看对话</a-button>
                <a-button v-if="item.deployKey" type="link" @click="viewWork(item.deployKey)">
                  查看作品
                </a-button>
              </a-space>
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

.app-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.app-info-content {
  min-width: 0;
}

.app-title {
  overflow: hidden;
  color: #222;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.app-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  background: #f5f5f5;
  font-size: 14px;
}

.card-actions {
  margin-top: 8px;
}
</style>
